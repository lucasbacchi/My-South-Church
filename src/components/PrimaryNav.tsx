import { NavLink } from "react-router";
import { useUser } from "../contexts/UserContextDefinition";
import { useCurrentUserRoles } from "../hooks/use-current-user-roles";
import { cn } from "@/lib/utils";

const basePages: string[][] = [
    ["Home", "home", "/"],
    ["About", "info", "/about"],
    ["Resources", "menu_book", "/resources"],
    ["Browse Groups", "group", "/browse/groups"],
    ["Browse Files", "folder_open", "/browse/files"],
];

const authPages: string[][] = [
    ["My Groups", "groups", "/groups"],
    ["My Files", "folder", "/files"],
];

const adminPage: string[] = ["Admin Dashboard", "dashboard", "/admin/dashboard"];

interface PrimaryNavProps {
    variant: "side" | "mobile";
    className?: string;
    onNavigate?: () => void;
}

const baseItemClasses = "flex items-center gap-3 rounded-lg transition-all duration-200 relative overflow-hidden group";
const activeItemClasses = "bg-primary/15 text-primary font-semibold border-l-4 border-primary pl-3 shadow-md";
const inactiveItemClasses = "text-foreground hover:bg-primary/8 hover:text-primary";

export default function PrimaryNav({ variant, className, onNavigate }: PrimaryNavProps) {
    const [user] = useUser();
    const { isAdmin } = useCurrentUserRoles();
    const navPages = user ? [...basePages, ...authPages] : basePages;

    const itemSizeClasses = variant === "side" ? "px-4 min-h-12 py-3" : "px-4 py-3";
    const adminWrapperClasses =
        variant === "side" ? "mt-auto pt-4 border-t border-border/60" : "pt-4 mt-2 border-t border-border/60";

    return (
        <nav className={cn("flex flex-col gap-2", className)}>
            {navPages.map((page) => (
                <NavLink
                    key={page[1]}
                    to={page[2]}
                    onClick={onNavigate}
                    className={({ isActive }) =>
                        cn(baseItemClasses, itemSizeClasses, isActive ? activeItemClasses : inactiveItemClasses)
                    }
                >
                    <span className="material-symbols-outlined text-xl shrink-0 transition-transform duration-200 group-hover:scale-110">
                        {page[1]}
                    </span>
                    <span className="text-sm lg:text-base truncate">{page[0]}</span>
                </NavLink>
            ))}
            {isAdmin ? (
                <div className={adminWrapperClasses}>
                    <NavLink
                        to={adminPage[2]}
                        onClick={onNavigate}
                        className={({ isActive }) =>
                            cn(baseItemClasses, itemSizeClasses, isActive ? activeItemClasses : inactiveItemClasses)
                        }
                    >
                        <span className="material-symbols-outlined text-xl shrink-0 transition-transform duration-200 group-hover:scale-110">
                            {adminPage[1]}
                        </span>
                        <span className="text-sm lg:text-base truncate">{adminPage[0]}</span>
                    </NavLink>
                </div>
            ) : null}
        </nav>
    );
}
