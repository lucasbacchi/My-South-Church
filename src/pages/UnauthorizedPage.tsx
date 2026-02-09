import { Button } from "@/components/ui/button";
import { Link } from "react-router";

export default function UnauthorizedPage() {
    return (
        <div className="flex flex-col items-center justify-center min-h-[60vh] p-6">
            <div className="text-center max-w-md space-y-6">
                {/* Error Icon */}
                <div className="flex justify-center">
                    <span className="material-symbols-outlined text-6xl! text-destructive">block</span>
                </div>

                {/* Error Message */}
                <div className="space-y-2">
                    <h1 className="text-4xl font-bold text-foreground">Access Denied</h1>
                    <p className="text-lg text-muted-foreground">You don&apos;t have permission to access this page.</p>
                </div>

                {/* Additional Info */}
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

                {/* Action Buttons */}
                <div className="flex flex-col sm:flex-row gap-4 justify-center pt-4">
                    <Link to="/">
                        <Button variant="default" className="w-full sm:w-auto p-6">
                            <span className="material-symbols-outlined text-sm mr-2">home</span>
                            Go to Home
                        </Button>
                    </Link>
                    <Link to="/account">
                        <Button
                            variant="outline"
                            className="w-full p-6 sm:w-auto hover:bg-transparent ring-1 hover:ring-2 hover:ring-primary hover:text-white"
                        >
                            <span className="material-symbols-outlined text-sm mr-2">person</span>
                            View My Account
                        </Button>
                    </Link>
                </div>
            </div>
        </div>
    );
}
