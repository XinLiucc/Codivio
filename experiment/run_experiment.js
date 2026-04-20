/**
 * Codivio 断线重连性能实验脚本
 *
 * 用法：
 *   node run_experiment.js              # 跑混合方案（默认）
 *   node run_experiment.js --control    # 跑对照组（纯增量重放，需先注释 DocStateStore.java 里的 delete 那行）
 *
 * 输出：results_hybrid.csv 或 results_control.csv
 */

const puppeteer = require('puppeteer')
const fs = require('fs')
const path = require('path')

// ─── 配置 ─────────────────────────────────────────────────────────────────────
const BASE_URL    = 'http://localhost'
const PROJECT_ID  = '433950300722827264'
const FILE_ID     = '8e998323683c484cbe3eb333fff6e620'
const USERNAME    = 'xinliucc'
const PASSWORD    = 'asdjkl'
const USERNAME_B  = 'test'
const PASSWORD_B  = 'test123'
const USERNAME_G  = 'guard'
const PASSWORD_G  = 'asdjkl'

const EDITOR_URL  = `${BASE_URL}/projects/${PROJECT_ID}/files`
const LOGIN_URL   = `${BASE_URL}/login`

// 实验档位和重复次数
const INCREMENTS  = [50, 200, 500, 1000, 2000, 5000]   // 积压操作数
const REPEAT      = 10                      // 每档重复次数
const SNAPSHOT_INTERVAL = 50               // 与前端 CodeEditor.vue 一致

const isControl   = process.argv.includes('--control')
const outFile     = isControl ? 'results_control.csv' : 'results_hybrid.csv'
const modeLabel   = isControl ? '对照组（纯增量重放）' : '混合方案（快照+增量）'

// ─── 工具函数 ─────────────────────────────────────────────────────────────────

/** 等待指定毫秒 */
const sleep = ms => new Promise(r => setTimeout(r, ms))

/**
 * 登录并跳转到编辑器页面，返回 page
 * @param {import('puppeteer').Browser} browser
 * @param {boolean} headless - 是否无头（调试时设 false 可看到浏览器）
 */
async function openEditorPage(browser, username = USERNAME, password = PASSWORD) {
  const ctx = await browser.createBrowserContext()
  const page = await ctx.newPage()

  // 捕获控制台输出，找 [SYNC_TIME]
  const syncTimes = []
  page.on('console', msg => {
    const text = msg.text()
    if (text.startsWith('[SYNC_TIME]')) {
      const ms = parseInt(text.replace('[SYNC_TIME]', '').trim())
      if (!isNaN(ms)) syncTimes.push(ms)
      console.log(`  → 捕获 ${text}`)
    }
  })

  // 先加载首页（让 Vue 应用初始化，建立 localStorage 上下文）
  await page.goto(BASE_URL, { waitUntil: 'networkidle2', timeout: 15000 })

  // 直接调用登录 API 获取 token，注入到 localStorage（比 UI 登录更可靠）
  const loginResult = await page.evaluate(async (username, password) => {
    const res = await fetch('/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ loginId: username, password: password })
    })
    return res.json()
  }, username, password)

  if (!loginResult?.data?.token) {
    throw new Error(`登录失败：${JSON.stringify(loginResult)}`)
  }

  // Pinia persist 默认以 store ID 为 key 存 JSON
  await page.evaluate((token, user) => {
    localStorage.setItem('auth', JSON.stringify({ token, user, isLoading: false }))
  }, loginResult.data.token, loginResult.data.userInfo)

  // 导航到编辑器（已有 token，路由守卫会放行）
  await page.goto(EDITOR_URL, { waitUntil: 'networkidle2', timeout: 15000 })

  // 等待文件树侧边栏出现
  await page.waitForSelector('.ide-sidebar', { timeout: 15000 })
  await sleep(1000)

  // 等待"测试"文字出现在 DOM 中（不依赖 class 名，生产构建更可靠）
  await page.waitForFunction(
    () => Array.from(document.querySelectorAll('span')).some(el => el.textContent?.trim() === '测试'),
    { timeout: 30000 }
  ).catch(async () => {
    await page.screenshot({ path: path.join(__dirname, 'debug_screenshot.png') })
    throw new Error('找不到文件"测试"，截图已保存为 debug_screenshot.png')
  })
  await sleep(500)

  // 用鼠标坐标点击"测试"文件
  const fileBox = await page.evaluate(() => {
    const spans = Array.from(document.querySelectorAll('span'))
    const target = spans.find(el => el.textContent?.trim() === '测试')
    if (!target) return null
    const row = target.closest('.tree-row') || target
    const rect = row.getBoundingClientRect()
    return { x: rect.x + rect.width / 2, y: rect.y + rect.height / 2 }
  })

  if (!fileBox) throw new Error('找不到文件"测试"的坐标')
  await page.mouse.click(fileBox.x, fileBox.y)

  // 等待 Monaco 编辑器加载完成
  await page.waitForSelector('.monaco-editor .view-lines', { timeout: 15000 })
  await sleep(2000) // 等待 Yjs WebSocket 连接建立并完成初始同步

  return { page, ctx, syncTimes }
}

/**
 * 等待页面的 [SYNC_TIME] 出现，超时返回 null
 */
async function waitSyncTime(syncTimes, timeoutMs = 30000) {
  const startLen = syncTimes.length
  const deadline = Date.now() + timeoutMs
  while (Date.now() < deadline) {
    if (syncTimes.length > startLen) return syncTimes[syncTimes.length - 1]
    await sleep(200)
  }
  return null
}

/**
 * 在 page A 的编辑器中输入 n 个字符（用 keyboard.type，触发 Yjs update）
 */
async function typeInEditor(page, n) {
  // 点击编辑器内容区域获取焦点（.view-lines 是实际文字渲染区）
  await page.click('.monaco-editor .view-lines')
  await sleep(300)
  // 分批输入，避免单次 type 太长
  const chunk = 50
  for (let i = 0; i < n; i += chunk) {
    const batch = Math.min(chunk, n - i)
    await page.keyboard.type('a'.repeat(batch), { delay: 5 })
  }
}

/**
 * 用 Chrome DevTools Protocol 让 page 模拟离线/在线
 */
async function setOffline(page, offline) {
  const client = await page.createCDPSession()
  await client.send('Network.emulateNetworkConditions', {
    offline,
    latency: 0,
    downloadThroughput: offline ? 0 : -1,
    uploadThroughput: offline ? 0 : -1,
  })
  await client.detach()
}

/**
 * 清空 Redis（需要 docker 在运行）
 */
async function flushRedis() {
  const { execSync } = require('child_process')
  try {
    execSync('docker exec codivio-redis redis-cli FLUSHALL', { stdio: 'pipe' })
    console.log('  Redis 已清空')
  } catch (e) {
    console.warn('  ⚠️  Redis 清空失败（请确认 docker 容器名）:', e.message)
  }
}

// ─── 主流程 ───────────────────────────────────────────────────────────────────

async function runExperiment() {
  console.log(`\n=== Codivio 断线重连性能实验 ===`)
  console.log(`模式：${modeLabel}`)
  console.log(`档位：${INCREMENTS.join(', ')} 条增量，每档重复 ${REPEAT} 次`)
  console.log(`输出：${outFile}\n`)

  const browser = await puppeteer.launch({
    headless: false,          // 设为 true 可后台运行；调试建议保持 false
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
    defaultViewport: { width: 1280, height: 800 }
  })

  // 结果表：{ increment: number, times: number[] }
  const results = []

  for (const n of INCREMENTS) {
    console.log(`\n─── 档位：${n} 条增量 ───`)
    const times = []

    for (let round = 1; round <= REPEAT; round++) {
      console.log(`  第 ${round}/${REPEAT} 次`)

      // 1. 每轮重新打开守卫页面
      //    上一轮结束时守卫已关闭 → 房间空 → 服务端自动清 Redis
      //    重新打开 → 守卫 Yjs 内存从空开始，与 Redis 状态一致
      console.log('  启动守卫...')
      const { ctx: ctxGuard } = await openEditorPage(browser, USERNAME_G, PASSWORD_G)
      await sleep(1000)

      // 2. A 打开编辑器，输入 n 个字符（触发快照上传）
      const { page: pageA, ctx: ctxA } = await openEditorPage(browser)
      await sleep(1500)

      await typeInEditor(pageA, n)
      console.log(`  A 输入了 ${n} 个字符`)

      // 等待快照发送到服务器（每 50 次 update 触发一次）
      const snapshotsNeeded = Math.ceil(n / SNAPSHOT_INTERVAL)
      await sleep(snapshotsNeeded * 300 + 2000)

      // 3. 关闭 A（守卫还在，房间不清空，Redis 保留快照+增量）
      await ctxA.close()
      await sleep(500)

      // 4. B 新页面连入，测量同步耗时
      const { page: pageB, ctx: ctxB, syncTimes: syncTimesB } = await openEditorPage(browser, USERNAME_B, PASSWORD_B)

      if (syncTimesB.length > 0) {
        const syncMs = syncTimesB[syncTimesB.length - 1]
        times.push(syncMs)
        console.log(`  同步耗时：${syncMs} ms`)
      } else {
        const syncMs = await waitSyncTime(syncTimesB, 5000)
        if (syncMs !== null) {
          times.push(syncMs)
          console.log(`  同步耗时：${syncMs} ms`)
        } else {
          console.warn('  ⚠️  未捕获到 [SYNC_TIME]，本次跳过')
        }
      }

      // 5. 关闭 B 和守卫（守卫关闭后房间为空，服务端自动清 Redis，为下一轮准备）
      await ctxB.close()
      await sleep(200)
      await ctxGuard.close()
      await sleep(1000) // 等待服务端清空 Redis
    }

    results.push({ n, times })
    console.log(`  原始数据：[${times.join(', ')}]`)
  }

  await browser.close()

  // ─── 统计 & 输出 ────────────────────────────────────────────────────────────

  console.log('\n=== 统计结果（去掉最大最小值，取剩余均值）===')

  const csvLines = ['积压量,原始数据,有效次数,均值(ms),标准差(ms)']

  for (const { n, times } of results) {
    if (times.length < 3) {
      console.log(`${n} 条：数据不足，跳过统计`)
      csvLines.push(`${n},"${times.join(';')}",${times.length},N/A,N/A`)
      continue
    }
    const sorted = [...times].sort((a, b) => a - b)
    const trimmed = sorted.slice(1, -1)  // 去掉最大和最小
    const mean = trimmed.reduce((s, v) => s + v, 0) / trimmed.length
    const std  = Math.sqrt(trimmed.reduce((s, v) => s + (v - mean) ** 2, 0) / trimmed.length)
    console.log(`${n} 条：均值 ${mean.toFixed(1)} ms，标准差 ${std.toFixed(1)} ms（有效 ${trimmed.length} 次）`)
    csvLines.push(`${n},"${times.join(';')}",${trimmed.length},${mean.toFixed(1)},${std.toFixed(1)}`)
  }

  const outPath = path.join(__dirname, outFile)
  fs.writeFileSync(outPath, csvLines.join('\n'), 'utf8')
  console.log(`\n结果已保存到 ${outPath}`)
}

runExperiment().catch(err => {
  console.error('实验出错：', err)
  process.exit(1)
})
