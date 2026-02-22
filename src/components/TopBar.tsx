import { useContext, useEffect, useState } from "react";
import { Link, useNavigation } from "react-router";
import { UserContext } from "../contexts/UserContextDefinition";
import AccountDrawer from "./AccountDrawer";
import MobileNav from "./MobileNav";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

export default function TopBar() {
    const [accountIsOpen, setAccountIsOpen] = useState(false);
    const [user, , cachedPhotoURL] = useContext(UserContext);
    const [theme, setTheme] = useState<"light" | "dark">(() => {
        if (typeof window === "undefined") return "dark";
        const stored = localStorage.getItem("theme");
        if (stored === "light" || stored === "dark") return stored;
        return window.matchMedia("(prefers-color-scheme: light)").matches ? "light" : "dark";
    });
    const navigation = useNavigation();
    const [showLoader, setShowLoader] = useState(false);
    const LOADER_DELAY_MS = 200; // Delay before showing loader to prevent flashing on fast loads

    const onAccountOpen = () => setAccountIsOpen(true);
    const onAccountClose = () => setAccountIsOpen(false);

    useEffect(() => {
        const root = document.documentElement;
        root.setAttribute("data-theme", theme);
        root.style.colorScheme = theme;
        localStorage.setItem("theme", theme);
    }, [theme]);

    // Show loading indicator after a delay to avoid flash for quick loads
    useEffect(() => {
        const isLoading = navigation.state === "loading";

        if (isLoading) {
            const timeoutId = setTimeout(() => {
                setShowLoader(true);
            }, LOADER_DELAY_MS);

            return () => clearTimeout(timeoutId);
        } else {
            const timeoutId = setTimeout(() => {
                setShowLoader(false);
            }, 0);

            return () => clearTimeout(timeoutId);
        }
    }, [navigation.state]);

    const toggleTheme = () => {
        setTheme((prev) => (prev === "dark" ? "light" : "dark"));
    };

    return (
        <>
            <div className="sticky top-0 left-0 z-30">
                <div className="flex relative bg-card/95 backdrop-blur-sm flex-row shadow-lg border-b border-border transition-all duration-200 items-stretch">
                    <MobileNav />
                    <Link
                        to="/"
                        className="flex items-center hover:opacity-85 transition-opacity duration-200 group px-3 sm:px-4 md:px-6"
                    >
                        <h1 className="text-primary text-xl py-5 sm:text-2xl md:text-4xl select-none font-bold group-hover:text-primary-lighter transition-colors">
                            <span className="hidden sm:inline">My South Church</span>
                            <span className="sm:hidden">MSC</span>
                        </h1>
                    </Link>
                    <div className="flex grow" />
                    <button
                        onClick={toggleTheme}
                        aria-label="Toggle color theme"
                        className="flex items-center shadow-none transform-none focus:outline-none gap-1 rounded-full hover:border-primary/50 transition-all duration-200 group mx-4 bg-transparent border-none p-0 cursor-pointer"
                        type="button"
                    >
                        <span
                            className={`material-symbols-outlined text-sm sm:text-base transition-transform duration-300 ${theme === "dark" ? "scale-100 opacity-100" : "scale-0 opacity-0"}`}
                        >
                            dark_mode
                        </span>
                        <div
                            className={`relative w-8 h-4 sm:w-10 sm:h-5 rounded-full transition-colors duration-300 ${theme === "dark" ? "bg-primary/30" : "bg-primary/30"}`}
                        >
                            <div
                                className={`absolute top-px w-3 h-3 sm:w-4.5 sm:h-4.5 rounded-full bg-primary transition-all duration-300 transform ${theme === "dark" ? "translate-x-0.5 sm:translate-x-0" : "translate-x-4 sm:translate-x-6"}`}
                            />
                        </div>
                        <span
                            className={`material-symbols-outlined text-black text-sm sm:text-base transition-transform duration-300 ${theme === "light" ? "scale-100 opacity-100" : "scale-0 opacity-0"}`}
                        >
                            light_mode
                        </span>
                    </button>
                    {!user ? (
                        <Link
                            to="/signin"
                            className="flex items-center justify-center size-16 sm:size-20 hover:bg-primary/10 transition-colors duration-200 cursor-pointer group"
                        >
                            <span className="material-symbols-outlined text-foreground text-2xl! sm:text-3xl! select-none group-hover:scale-110 transition-transform">
                                account_circle
                            </span>
                        </Link>
                    ) : (
                        <button
                            className="flex items-center hover:transform-none rounded-none bg-transparent shadow-none justify-center size-16 sm:size-20 hover:bg-primary/10 transition-colors duration-200 cursor-pointer border-none p-0 group"
                            onClick={onAccountOpen}
                            type="button"
                        >
                            <Avatar className="w-8 h-8 sm:w-9 sm:h-9 md:w-10 md:h-10 ring-2 transition-all duration-200 group-hover:shadow-lg">
                                <AvatarImage
                                    key={cachedPhotoURL ?? user.photoURL ?? "fallback"}
                                    src={cachedPhotoURL ?? user.photoURL ?? ""}
                                    alt={user.displayName ?? ""}
                                />
                                <AvatarFallback className="bg-linear-to-br from-primary-darker to-secondary-darker text-primary-foreground font-semibold">
                                    {user.displayName?.slice(0, 2).toUpperCase() ?? "U"}
                                </AvatarFallback>
                            </Avatar>
                        </button>
                    )}
                </div>

                {/* Progress bar for navigation loading */}
                {showLoader ? (
                    <div className="absolute bottom-0 left-0 w-full h-1 bg-transparent z-40 overflow-hidden">
                        <div
                            className="h-full bg-linear-to-r from-transparent via-primary to-transparent w-1/3"
                            style={{
                                animation: "shimmer 1s ease-in-out infinite",
                            }}
                        />
                    </div>
                ) : null}
            </div>

            <AccountDrawer onClose={onAccountClose} isOpen={accountIsOpen} />
        </>
    );
}
