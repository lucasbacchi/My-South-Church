import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
    root: "src",
    plugins: [react()],
    build: {
        outDir: "../public",
        emptyOutDir: true
    },
    assetsInclude: ["**/*.png", "**/*.jpg", "**/*.svg", "**/*.ico"]
});
