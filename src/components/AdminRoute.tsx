import { type ReactNode, useContext, useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router";
import { UserContext } from "../contexts/UserContextDefinition";
import { type CurrentUser, getCurrentUser, roles } from "../lib/api";
import UnauthorizedPage from "../pages/UnauthorizedPage";

/**
 * Wrapper component that requires admin authentication.
 * First checks if user is authenticated, then checks if they have admin permissions.
 */
export default function AdminRoute({ children }: { children: ReactNode }) {
    const [user] = useContext(UserContext);
    const navigate = useNavigate();
    const location = useLocation();
    const [currentUser, setCurrentUser] = useState<CurrentUser | null | undefined>(undefined);
    const [isChecking, setIsChecking] = useState(true);

    useEffect(() => {
        // Only redirect if we know for sure there's no user (not undefined, which means loading)
        if (user === null) {
            const redirectTo = location.pathname + location.search;
            const searchParams = new URLSearchParams({ redirect: redirectTo });
            void navigate(`/signin?${searchParams.toString()}`, { replace: true });
        } else if (user) {
            // User is authenticated, now check permissions
            getCurrentUser()
                .then((userData) => {
                    setCurrentUser(userData);
                    setIsChecking(false);
                })
                .catch((error) => {
                    console.error("Error checking user permissions:", error);
                    setCurrentUser(null);
                    setIsChecking(false);
                });
        }
    }, [user, navigate, location]);

    // Show loading state while checking authentication
    if (user === undefined || isChecking) {
        return (
            <div className="flex flex-col items-center justify-center min-h-[60vh] p-6">
                <div className="text-center space-y-4">
                    <span className="material-symbols-outlined text-5xl text-primary animate-pulse">
                        hourglass_empty
                    </span>
                    <p className="text-lg text-muted-foreground">
                        {user === undefined ? "Checking authentication..." : "Verifying permissions..."}
                    </p>
                </div>
            </div>
        );
    }

    // Show nothing while redirecting to sign in
    if (user === null) {
        return null;
    }

    // User is authenticated but doesn't have admin permissions
    if (!currentUser?.roles.includes(roles.ADMIN) && !currentUser?.roles.includes(roles.SUPER_ADMIN)) {
        return <UnauthorizedPage />;
    }

    // User is authenticated and has admin permissions
    return <>{children}</>;
}
