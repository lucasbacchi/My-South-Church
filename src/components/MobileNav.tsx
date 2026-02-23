import { useState } from "react";
import { NavLink } from "react-router";

const navPages: string[][] = [
    ["Home", "home", "/"],
    ["About", "info", "/about"],
    ["Resources", "menu_book", "/resources"],
    ["Google Groups", "email", "/groups"],
    ["Google Drive", "folder", "/drive"],
];

export default function MobileNav() {
    const [isOpen, setIsOpen] = useState(false);

    const toggleMenu = () => setIsOpen(!isOpen);
    const closeMenu = () => setIsOpen(false);

    return (
        <div className="md:hidden">
            {/* Hamburger Button */}
            <button
                onClick={toggleMenu}
                className="flex items-center justify-center w-12 h-12 sm:w-14 sm:h-14 hover:bg-primary/10 transition-colors duration-200 cursor-pointer border-none p-0"
                type="button"
                aria-label="Toggle menu"
            >
                <span className="material-symbols-outlined text-foreground text-xl sm:text-2xl">
                    {isOpen ? "close" : "menu"}
                </span>
            </button>

            {/* Mobile Menu Overlay */}
            {isOpen ? (
                <div
                    className="fixed inset-0 bg-background/80 backdrop-blur-sm z-40"
                    onClick={closeMenu}
                    onKeyDown={(e) => e.key === "Escape" && closeMenu()}
                    role="button"
                    tabIndex={0}
                    aria-label="Close menu"
                />
            ) : null}

            {/* Mobile Menu Drawer */}
            <div
                className={`fixed top-14 sm:top-16 left-0 bottom-0 w-64 bg-card border-r border-border z-50 transform transition-transform duration-300 ease-in-out ${
                    isOpen ? "translate-x-0" : "-translate-x-full"
                }`}
            >
                <nav className="flex flex-col p-4 gap-2">
                    {navPages.map((page) => (
                        <NavLink
                            key={page[1]}
                            to={page[2]}
                            onClick={closeMenu}
                            className={({ isActive }) =>
                                `flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 ${
                                    isActive
                                        ? "bg-primary/15 text-primary font-semibold border-l-4 border-primary pl-3 shadow-md"
                                        : "text-foreground hover:bg-primary/8 hover:text-primary"
                                }`
                            }
                        >
                            <span className="material-symbols-outlined text-xl shrink-0">{page[1]}</span>
                            <span className="text-base">{page[0]}</span>
                        </NavLink>
                    ))}
                </nav>
            </div>
        </div>
    );
}
