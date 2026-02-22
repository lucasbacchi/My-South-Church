import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Link, useNavigate, useSearchParams } from "react-router";
import { useEffect, useState } from "react";
import { getCurrentUser, roles } from "@/lib/api";
import { getHealthStatus, subscribeToHealthStatus } from "@/lib/backendHealth";

type BackendStatus = "checking" | "up" | "down";
export type ErrorType = "unauthorized" | "service_unavailable" | "not_found";

interface ErrorConfig {
    icon: string;
    title: string;
    message: string;
    showContactInfo: boolean;
    checkPermissionsOnRecovery: boolean;
}

const ERROR_CONFIGS: Record<ErrorType, ErrorConfig> = {
    unauthorized: {
        icon: "block",
        title: "Access Denied",
        message: "You don't have permission to access this page.",
        showContactInfo: true,
        checkPermissionsOnRecovery: true,
    },
    service_unavailable: {
        icon: "cloud_off",
        title: "System Unavailable",
        message: "The system is currently down. Please try again later.",
        showContactInfo: false,
        checkPermissionsOnRecovery: false,
    },
    not_found: {
        icon: "search_off",
        title: "Page Not Found",
        message: "The page you're looking for doesn't exist or may have been moved.",
        showContactInfo: false,
        checkPermissionsOnRecovery: false,
    },
};

interface ErrorPageProps {
    errorType?: ErrorType;
}

export default function ErrorPage({ errorType: propErrorType }: ErrorPageProps = {}) {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    // Use props if provided, otherwise fall back to searchParams (for backward compatibility)
    const errorType = (propErrorType ?? (searchParams.get("type") as ErrorType)) || "unauthorized";

    const [backendStatus, setBackendStatus] = useState<BackendStatus>(() => getHealthStatus());

    const config = ERROR_CONFIGS[errorType] || ERROR_CONFIGS.unauthorized;

    useEffect(() => {
        // Subscribe to global health status
        const unsubscribe = subscribeToHealthStatus(setBackendStatus);

        return () => {
            unsubscribe();
        };
    }, []);

    useEffect(() => {
        // When backend comes up and we should check permissions, reload the page to retry
        if (backendStatus === "up" && config.checkPermissionsOnRecovery) {
            void (async () => {
                try {
                    const currentUser = await getCurrentUser();

                    if (currentUser?.roles.includes(roles.ADMIN) || currentUser?.roles.includes(roles.SUPER_ADMIN)) {
                        // User has admin permissions - reload to retry the page load
                        window.location.reload();
                    }
                } catch (error) {
                    console.error("Error checking user permissions:", error);
                }
            })();
        }
    }, [backendStatus, config.checkPermissionsOnRecovery]);

    // Override display if backend is down (for non-service_unavailable errors)
    const isBackendDown = backendStatus === "down";
    const displayTitle = isBackendDown && errorType !== "service_unavailable" ? "System Unavailable" : config.title;
    const displayMessage =
        isBackendDown && errorType !== "service_unavailable"
            ? "The system is currently down. Please try again later."
            : config.message;
    const displayIcon = isBackendDown && errorType !== "service_unavailable" ? "cloud_off" : config.icon;

    return (
        <div className="flex flex-col items-center justify-center min-h-[60vh] px-3 sm:px-4 md:px-6">
            <div className="text-center max-w-md w-full space-y-6 sm:space-y-8">
                {/* Error Icon or 404 Number */}
                {errorType === "not_found" ? (
                    <div>
                        <h1 className="text-7xl pb-0 sm:text-8xl md:text-9xl font-bold bg-clip-text text-transparent bg-linear-to-r from-primary to-accent drop-shadow-lg">
                            404
                        </h1>
                    </div>
                ) : (
                    <div className="flex justify-center">
                        <span className="material-symbols-outlined text-6xl! text-destructive">{displayIcon}</span>
                    </div>
                )}

                {/* Error Message */}
                <div className="space-y-2">
                    <h1
                        className={
                            errorType === "not_found"
                                ? "text-2xl sm:text-3xl md:text-4xl font-bold text-foreground"
                                : "text-4xl font-bold text-foreground"
                        }
                    >
                        {backendStatus === "checking" ? "Checking Access..." : displayTitle}
                    </h1>
                    {errorType === "not_found" && (
                        <div className="h-1 w-12 sm:w-16 bg-linear-to-r from-primary to-accent rounded-full mx-auto mb-4" />
                    )}
                    <p className="text-sm sm:text-base md:text-lg text-muted-foreground leading-relaxed px-2">
                        {backendStatus === "checking" ? "Verifying server status and permissions..." : displayMessage}
                    </p>
                </div>

                {/* Backend Status Alert */}
                {isBackendDown ? (
                    <Alert variant="destructive" className="flex">
                        <span className="material-symbols-outlined text-base mr-2">cloud_off</span>
                        <AlertDescription className="text-center block">
                            The backend server is currently down. If the issue persists, please contact{" "}
                            <a
                                href="mailto:help@southchurch.com"
                                className="m-auto inline text-primary hover:underline"
                            >
                                help@southchurch.com
                            </a>
                            .
                        </AlertDescription>
                    </Alert>
                ) : null}

                {backendStatus === "checking" && (
                    <Alert>
                        <span className="material-symbols-outlined text-base mr-2 animate-spin">refresh</span>
                        <AlertDescription>Checking server status and permissions...</AlertDescription>
                    </Alert>
                )}

                {/* Additional Info */}
                {!isBackendDown && config.showContactInfo ? (
                    <div className="bg-muted/50 border border-border rounded-lg p-4">
                        <p className="text-sm text-muted-foreground">
                            This page requires administrative privileges. If you believe you should have access, please
                            contact{" "}
                            <a href="mailto:help@southchurch.com" className="text-primary hover:underline">
                                help@southchurch.com
                            </a>{" "}
                            for assistance.
                        </p>
                    </div>
                ) : null}

                {/* Action Buttons */}
                <div className="flex flex-col gap-3 sm:flex-row sm:gap-4 justify-center pt-2 sm:pt-4">
                    {errorType === "not_found" ? (
                        <>
                            <Button
                                variant="outline"
                                className="w-full px-4 sm:px-6 py-3 sm:py-5 sm:w-auto hover:bg-transparent hover:text-forground ring-1 hover:ring-2 hover:ring-border"
                                onClick={() => {
                                    void navigate(-1);
                                }}
                            >
                                <span className="material-symbols-outlined text-lg sm:text-xl mr-2">arrow_back</span>
                                Go Back
                            </Button>
                            <Link to="/" className="w-full sm:w-auto">
                                <Button variant="default" className="w-full px-4 sm:px-6 py-3 sm:py-5">
                                    Home
                                    <span className="material-symbols-outlined text-lg sm:text-xl ml-2">
                                        arrow_forward
                                    </span>
                                </Button>
                            </Link>
                        </>
                    ) : (
                        <>
                            <Link to="/" className="w-full sm:w-auto">
                                <Button variant="default" className="w-full px-4 sm:px-6 py-3 sm:py-5">
                                    <span className="material-symbols-outlined text-sm mr-2">home</span>
                                    Go to Home
                                </Button>
                            </Link>
                            <Link to="/account" className="w-full sm:w-auto">
                                <Button
                                    variant="outline"
                                    className="w-full px-4 sm:px-6 py-2.5 sm:py-3 hover:bg-transparent ring-1 hover:ring-2 hover:ring-primary hover:text-white"
                                >
                                    <span className="material-symbols-outlined text-sm mr-2">person</span>
                                    View My Account
                                </Button>
                            </Link>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}
