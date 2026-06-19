import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
// import basicSsl from '@vitejs/plugin-basic-ssl'

const suppressViteLogs = {
  name: 'suppress-vite-logs',
  apply: 'serve',
  transformIndexHtml(html) {
    return html + `<script>
      const _log = console.log;
      console.log = (...a) => {
        if (typeof a[0] === 'string' && a[0].startsWith('[vite]')) return;
        _log(...a);
      };
    </script>`;
  },
};

export default defineConfig({
  plugins: [
    vue(),
    tailwindcss(),
    suppressViteLogs,
    // basicSsl()
  ],
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor': ['vue', 'vue-router', 'axios', 'dayjs'],
        }
      }
    }
  },
  server: {
    https: false,
    host: true,
    port: 5173,
    allowedHosts: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            if (req.url?.includes('/sse/')) {
              proxyReq.setHeader('X-Accel-Buffering', 'no');
            }
          });
        },
      },
    },
  }
})