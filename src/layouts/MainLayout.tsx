import { Outlet } from "react-router";
import SideNavBar from "../components/SideNavBar";
import TopBar from "../components/TopBar";
import Footer from "../components/Footer";
import BackendHealthBanner from "../components/BackendHealthBanner";

export default function MainLayout({ children }: { children?: React.ReactNode }) {
    return (
        <>
            <BackendHealthBanner />
            <div className="flex flex-col min-h-screen bg-background text-foreground">
                <TopBar />
                <div className="relative flex-1 min-h-0 max-w-screen">
                    <SideNavBar />
                    <main className="flex-1 overflow-y-auto overflow-x-hidden md:pl-48 lg:pl-56">
                        <div className="w-full max-w-7xl mx-auto px-6 md:px-6 lg:px-8 py-4 sm:py-6 lg:py-8">
                            {children ?? <Outlet />}
                        </div>
                    </main>
                </div>
                <div className="md:pl-48 lg:pl-56">
                    <Footer />
                </div>
            </div>
        </>
    );
}
