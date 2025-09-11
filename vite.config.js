import { scalaMetadata } from "./scala-metadata"
import { createHtmlPlugin } from 'vite-plugin-html'

import { defineConfig } from 'vite'

const scalaVersion = scalaMetadata.scalaVersion

// https://vitejs.dev/config/
export default defineConfig(({ command, mode, ssrBuild }) => {
    const mainJS = `/target/scala-${scalaVersion}/dictee-the-game-${mode === "production" ? "opt" : "fastopt"
        }/main.js`
    console.log("mainJS", mainJS)
    const script = `<script type="module" src="${mainJS}"></script>`

    return {
        publicDir: "./public",
        plugins: createHtmlPlugin({
            minify: process.env.NODE_ENV === 'production',
            inject: {
                data: {
                    script
                }
            }
        }),
        base: "/dictee-the-game",
        server: {
            open: '/dictee-the-game'
        },
        build: {
            "chunkSizeWarningLimit": 5000
        }
    }
})
