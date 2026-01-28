import { Outlet } from "react-router";
import SideNavBar from "../components/SideNavBar";
import TopBar from "../components/TopBar";
import Footer from "../components/Footer";

export default function MainLayout({ children }: { children?: React.ReactNode }) {
    return (
        <div className="flex flex-col min-h-screen bg-background text-foreground">
            <TopBar />
            <div className="flex flex-row flex-1 min-h-0 max-w-screen">
                <SideNavBar />
                <main className="flex-1 overflow-y-auto overflow-x-hidden">
                    <div className="w-full max-w-7xl mx-auto px-3 sm:px-4 md:px-6 lg:px-8 py-4 sm:py-6 lg:py-8">
                        {children ?? <Outlet />}
                    </div>
                </main>
            </div>
            <Footer />
        </div>
    );
}
