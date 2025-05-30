import path from 'path'
import tailwindcss from '@tailwindcss/vite'
import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react(), tailwindcss()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
        },
    },
    // Configure Vite to read .env file from the project root
    envDir: path.resolve(__dirname, '../../'),
    envPrefix: ['FE_'], // Only read environment variables that start with FE_
})
