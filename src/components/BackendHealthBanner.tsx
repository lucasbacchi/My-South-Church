import { Alert, AlertDescription } from "@/components/ui/alert";
import { useEffect, useState } from "react";
import { subscribeToHealthStatus } from "@/lib/backendHealth";

/**
 * Global toast-style notification that appears when backend is unavailable
 * Shows in the bottom left corner to warn users of connectivity issues
 */
export default function BackendHealthBanner() {
    const [isDown, setIsDown] = useState(false);

    useEffect(() => {
        const unsubscribe = subscribeToHealthStatus((status) => {
            setIsDown(status === "down");
        });

        return unsubscribe;
    }, []);

    if (!isDown) {
        return null;
    }

    return (
        <div className="fixed bottom-4 left-4 right-4 z-50 max-w-md">
            <Alert variant="destructive" className="shadow-lg border-2 w-full">
                <div className="flex items-start w-full gap-3 col-span-full">
                    <span className="material-symbols-outlined text-xl shrink-0 mt-0.5">cloud_off</span>
                    <AlertDescription className="flex flex-col w-full! min-w-0">
                        <div className="font-semibold mb-1 text-lg">System Unavailable</div>
                        <div className="text-sm">
                            The backend server is currently down. If the issue persists, please contact{" "}
                            <a
                                href="mailto:help@southchurch.com"
                                className="underline hover:text-destructive-foreground font-medium"
                            >
                                help@southchurch.com
                            </a>
                            .
                        </div>
                    </AlertDescription>
                </div>
            </Alert>
        </div>
    );
}
