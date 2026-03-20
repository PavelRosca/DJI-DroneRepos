import vue from '@vitejs/plugin-vue'
// config alias
import path from 'path'
import { ConfigEnv, defineConfig, UserConfigExport } from 'vite'
import Components from 'unplugin-vue-components/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'
// Introduce eslint plugin
import eslintPlugin from 'vite-plugin-eslint'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import { viteVConsole } from 'vite-plugin-vconsole'

// https://vitejs.dev/config/
export default ({ command, mode }: ConfigEnv): UserConfigExport => defineConfig({
  plugins: [
    vue(),
    eslintPlugin({
      fix: true
    }),
    Components({
      resolvers: [AntDesignVueResolver()],
    }),
    createSvgIconsPlugin({
      // 指定需要缓存的图标文件夹
      iconDirs: [path.resolve(process.cwd(), 'src/assets/icons')],
      // 指定symbolId格式
      symbolId: 'icon-[dir]-[name]',
    }),
    viteVConsole({
      entry: path.resolve(__dirname, './src/main.ts'), // 入口文件
      localEnabled: command === 'serve', // serve开发环境下
      // enabled: command !== 'serve' || mode === 'test', // 打包环境下/发布测试包,
      config: { // vconsole 配置项
        maxLogNumber: 1000,
        theme: 'light'
      }
    }),
  ],
  server: {
    open: true,
    host: '0.0.0.0',
    port: 8080
  },
  envDir: './env',
  resolve: {
    alias: [{
      // https://github.com/vitejs/vite/issues/279#issuecomment-635646269
      find: '/@',
      replacement: path.resolve(__dirname, './src'),
    }
    ]
  },
  css: {
    preprocessorOptions: {
      scss: {
        // example : additionalData: `@import "./src/design/styles/variables";`
        // dont need include file extend .scss
        additionalData: '@import "./src/styles/variables";'
      },
    }
  },
  base: '/',
  build: {
    target: ['es2015'], // 最低支持 es2015
    sourcemap: true
  }
})
