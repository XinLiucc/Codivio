import matplotlib.pyplot as plt
import matplotlib
import numpy as np

matplotlib.rcParams['font.family'] = ['Noto Sans CJK JP', 'DejaVu Sans']
matplotlib.rcParams['axes.unicode_minus'] = False

n = [50, 200, 500, 1000, 2000, 5000]

hybrid_mean = [113.8, 115.9, 116.8, 115.0, 115.8, 116.8]
hybrid_std  = [1.5,   2.5,   1.7,   1.2,   0.7,   1.4]

control_mean = [151.8, 216.5, 378.8, 531.5, 756.9, 1466.1]
control_std  = [2.0,   3.8,   2.6,   5.5,   14.9,  25.5]

fig, ax = plt.subplots(figsize=(8, 5))

ax.errorbar(n, hybrid_mean, yerr=hybrid_std,
            marker='o', linewidth=2, capsize=4,
            label='混合方案（快照+增量）', color='#2196F3')
ax.errorbar(n, control_mean, yerr=control_std,
            marker='s', linewidth=2, capsize=4,
            label='纯增量重放（对照组）', color='#FF9800')

ax.set_xscale('log')
ax.set_xticks(n)
ax.get_xaxis().set_major_formatter(matplotlib.ticker.ScalarFormatter())
ax.set_xlabel('增量积压量（条）', fontsize=12)
ax.set_ylabel('断线恢复耗时均值（ms）', fontsize=12)
ax.set_title('图6-1  断线重连恢复耗时对比', fontsize=13)
ax.legend(fontsize=11)
ax.grid(True, which='both', linestyle='--', alpha=0.5)

# 标注 5000 条的数值
ax.annotate('1466 ms', xy=(5000, 1466.1), xytext=(3000, 1300),
            arrowprops=dict(arrowstyle='->', color='#FF9800'),
            fontsize=9, color='#FF9800')
ax.annotate('117 ms', xy=(5000, 116.8), xytext=(3000, 250),
            arrowprops=dict(arrowstyle='->', color='#2196F3'),
            fontsize=9, color='#2196F3')

plt.tight_layout()
plt.savefig('reconnect_performance.png', dpi=150, bbox_inches='tight')
print('图表已保存：reconnect_performance.png')
