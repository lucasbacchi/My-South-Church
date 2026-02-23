import { useUser } from "../contexts/UserContextDefinition";
import { useCurrentUserRoles } from "../hooks/use-current-user-roles";

export default function Footer() {
    const [user] = useUser();
    const { isAdmin } = useCurrentUserRoles();
    const gridClasses = user
        ? "grid-cols-1 sm:grid-cols-2 lg:grid-cols-3"
        : "grid-cols-1 sm:grid-cols-2 lg:grid-cols-2";

    return (
        <footer className="bg-card/95 backdrop-blur-sm border-t border-border mt-auto shrink-0">
            <div className="max-w-6xl mx-auto px-8 py-8 sm:py-10 md:py-12">
                {/* Main Grid */}
                <div className={`grid ${gridClasses} gap-8 sm:gap-10 md:gap-12 mb-6 sm:mb-8`}>
                    {/* Brand Section */}
                    <div className="space-y-3">
                        <h3 className="text-base sm:text-lg font-bold text-primary cursor-default transition-colors">
                            My South Church
                        </h3>
                        <p className="text-xs sm:text-sm text-muted-foreground leading-relaxed">
                            Connecting our community with resources, information, and fellowship through a modern
                            digital platform.
                        </p>
                        <div className="flex gap-3 sm:gap-4 pt-2">
                            <a
                                href="https://southchurch.com"
                                target="_blank"
                                rel="noreferrer"
                                className="hover:text-primary transition-colors"
                            >
                                <span className="material-symbols-outlined text-lg sm:text-xl">language</span>
                            </a>
                            <a
                                href="https://southchurch.com/contact"
                                target="_blank"
                                rel="noreferrer"
                                className="hover:text-primary transition-colors"
                            >
                                <span className="material-symbols-outlined text-lg sm:text-xl">mail</span>
                            </a>
                        </div>
                    </div>

                    {/* Quick Links */}
                    <div className="space-y-3">
                        <h4 className="text-xs sm:text-sm font-bold text-foreground">Quick Links</h4>
                        <ul className="space-y-2 text-xs sm:text-sm">
                            <li>
                                <a
                                    href="/"
                                    className="text-primary hover:text-secondary transition-colors hover:translate-x-1 inline-block"
                                >
                                    Home
                                </a>
                            </li>
                            <li>
                                <a
                                    href="/about"
                                    className="text-primary hover:text-secondary transition-colors hover:translate-x-1 inline-block"
                                >
                                    About
                                </a>
                            </li>
                            <li>
                                <a
                                    href="/browse/groups"
                                    className="text-primary hover:text-secondary transition-colors hover:translate-x-1 inline-block"
                                >
                                    Browse Groups
                                </a>
                            </li>
                            <li>
                                <a
                                    href="/browse/files"
                                    className="text-primary hover:text-secondary transition-colors hover:translate-x-1 inline-block"
                                >
                                    Browse Files
                                </a>
                            </li>
                            <li>
                                <a
                                    href="/resources"
                                    className="text-primary hover:text-secondary transition-colors hover:translate-x-1 inline-block"
                                >
                                    Resources
                                </a>
                            </li>
                        </ul>
                    </div>

                    {user ? (
                        <div className="space-y-3">
                            <h4 className="text-xs sm:text-sm font-bold text-foreground">My Pages</h4>
                            <ul className="space-y-2 text-xs sm:text-sm">
                                <li>
                                    <a
                                        href="/groups"
                                        className="text-primary hover:text-secondary transition-colors hover:translate-x-1 inline-block"
                                    >
                                        My Groups
                                    </a>
                                </li>
                                <li>
                                    <a
                                        href="/files"
                                        className="text-primary hover:text-secondary transition-colors hover:translate-x-1 inline-block"
                                    >
                                        My Files
                                    </a>
                                </li>
                                {isAdmin ? (
                                    <li>
                                        <a
                                            href="/admin/dashboard"
                                            className="text-primary hover:text-secondary transition-colors hover:translate-x-1 inline-block"
                                        >
                                            Admin Dashboard
                                        </a>
                                    </li>
                                ) : null}
                            </ul>
                        </div>
                    ) : null}
                </div>

                {/* Divider */}
                <div className="border-t border-border pt-6 sm:pt-8">
                    <div className="flex flex-col sm:flex-row justify-between items-center gap-4 sm:gap-6 text-xs sm:text-sm">
                        <p className="text-muted-foreground text-center sm:text-left">©2026 South Church in Andover</p>
                    </div>
                </div>
            </div>
        </footer>
    );
}
