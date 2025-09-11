import vue from 'eslint-plugin-vue'
import prettier from 'eslint-config-prettier'

export default [
  {
    ignores: ['dist/', 'node_modules/', '*.d.ts', 'auto-imports.d.ts', 'components.d.ts'],
  },
  ...vue.configs['flat/recommended'],
  {
    rules: {
      'vue/multi-word-component-names': 'off',
    },
  },
  prettier,
]
