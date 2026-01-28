import { NavLink } from "react-router";

const sideNavBarPages: string[][] = [
    ["Home", "home", "/"],
    ["About", "info", "/about"],
    ["Google Groups", "email", "/groups"],
    ["Google Drive", "folder", "/drive"],
];

export default function SideNavBar() {
    return (
        <nav className="hidden md:flex flex-col top-0 left-0 w-48 lg:w-56 bg-card border-r border-border py-4 px-3 gap-2 shrink-0">
            {sideNavBarPages.map((page) => (
                <NavLink
                    key={page[1]}
                    to={page[2]}
                    className={({ isActive }) =>
                        `flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 relative overflow-hidden group ${
                            isActive
                                ? "bg-primary/15 text-primary font-semibold border-l-4 border-primary pl-3 shadow-md"
                                : "text-foreground hover:bg-primary/8 hover:text-primary"
                        }`
                    }
                >
                    <span className="material-symbols-outlined text-xl shrink-0 transition-transform duration-200 group-hover:scale-110">
                        {page[1]}
                    </span>
                    <span className="text-sm lg:text-base truncate">{page[0]}</span>
                </NavLink>
            ))}
        </nav>
    );
}
