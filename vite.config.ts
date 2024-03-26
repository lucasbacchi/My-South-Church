import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
    root: "public",
    plugins: [react()],
    build: {
        outDir: "../public/dist",
        emptyOutDir: true,
        rollupOptions: {
            input: {
                HomePage: "./public/src/pages/HomePage.tsx",
                AboutPage: "./public/src/pages/AboutPage.tsx",
                DrivePage: "./public/src/pages/DrivePage.tsx",
                GroupsPage: "./public/src/pages/GroupsPage.tsx"
            }
        }
    }
});
