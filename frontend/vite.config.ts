import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Development proxy: forward /api/<svc> prefixes to backend service ports.
// Prefixes are intentionally long to avoid conflicts with client routes.
const services: Record<string, string> = {
  '/api/music': 'http://localhost:8081', // Music_management
  '/api/reward': 'http://localhost:8080', // Multi_reward
  '/api/prize': 'http://localhost:8082', // PrizeCenter for coin/gem/coupon by prize code
}

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': new URL('./src', import.meta.url).pathname },
  },
  server: {
    port: 5173,
    proxy: {
      ...Object.fromEntries(
        Object.entries(services).map(([prefix, target]) => [
          prefix,
          { target, changeOrigin: true, rewrite: (p: string) => p.replace(prefix, '') },
        ]),
      ),
      // Keep MCPChatController same-origin in development. Its public path is /chatbot/chat.
      '/api/chatbot': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        rewrite: (p: string) => p.replace(/^\/api\/chatbot/, '/chatbot'),
      },
    },
  },
})
