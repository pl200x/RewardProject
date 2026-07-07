import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'

// Development proxy: forward /api/<svc> prefixes to backend service ports.
// Prefixes are intentionally long to avoid conflicts with client routes.
const services: Record<string, string> = {
  '/api/music': 'http://localhost:8081', // Music_management
  '/api/reward': 'http://localhost:8080', // Multi_reward
  '/api/prize': 'http://localhost:8082', // PrizeCenter for coin/gem/coupon by prize code
}

// MinIO static assets such as default avatars. The bucket is anonymously readable.
// Production can route the same path through nginx/ALB to S3 or MinIO.
const minioTarget = 'http://localhost:9000'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': path.resolve(__dirname, 'src') },
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
      '/minio': {
        target: minioTarget,
        changeOrigin: true,
        rewrite: (p: string) => p.replace('/minio', ''),
      },
    },
  },
})
