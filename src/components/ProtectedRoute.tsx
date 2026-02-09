import { type ReactNode, useContext, useEffect } from "react";
import { useLocation, useNavigate } from "react-router";
import { UserContext } from "../contexts/UserContextDefinition";

/**
 * Wrapper component that requires authentication.
 * Redirects to sign-in page with redirect parameter if not authenticated.
 */
export default function ProtectedRoute({ children }: { children: ReactNode }) {
    const [user] = useContext(UserContext);
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        // Only redirect if we know for sure there's no user (not undefined, which means loading)
        if (user === null) {
            const redirectTo = location.pathname + location.search;
            const searchParams = new URLSearchParams({ redirect: redirectTo });
            void navigate(`/signin?${searchParams.toString()}`, { replace: true });
        }
    }, [user, navigate, location]);

    // Show loading state while checking authentication
    if (user === undefined) {
        return (
            <div className="flex flex-col items-center justify-center min-h-[60vh] p-6">
                <div className="text-center space-y-4">
                    <span className="material-symbols-outlined text-5xl text-primary animate-pulse">
                        hourglass_empty
                    </span>
                    <p className="text-lg text-muted-foreground">Checking authentication...</p>
                </div>
            </div>
        );
    }

    // Show nothing while redirecting
    if (user === null) {
        return null;
    }

    // User is authenticated, show the protected content
    return <>{children}</>;
}
