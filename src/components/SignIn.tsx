import { useCallback, useContext, useEffect, useRef, useState } from "react";
import {
    GoogleAuthProvider,
    browserLocalPersistence,
    getRedirectResult,
    setPersistence,
    signInWithCredential,
    signInWithPopup,
    signInWithRedirect,
} from "firebase/auth";
import { UserContext } from "../contexts/UserContextDefinition";
import { useNavigate, useSearchParams } from "react-router";

import { auth } from "../firebase";

// Type declaration for Google Sign-In
declare global {
    interface Window {
        google?: {
            accounts: {
                id: {
                    initialize: (config: Record<string, unknown>) => void;
                    prompt: (callback?: (notification: Record<string, unknown>) => void) => void;
                    renderButton: (element: HTMLElement, options: Record<string, unknown>) => void;
                    cancel: () => void;
                    disableAutoSelect: () => void;
                };
            };
        };
    }
}

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID as string | undefined;

export default function SignIn() {
    const [user] = useContext(UserContext);
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const oneTapInitializedRef = useRef(false);

    // Get the redirect URL from query params
    const redirectTo = searchParams.get("redirect") ?? "/";

    // Redirect if already logged in
    useEffect(() => {
        if (user) {
            void navigate(redirectTo);
        }
    }, [user, navigate, redirectTo]);

    // Handle redirect result from Google Sign-In (production only)
    useEffect(() => {
        let isMounted = true;

        const handleRedirectResult = async () => {
            try {
                await setPersistence(auth, browserLocalPersistence);
                const result = await getRedirectResult(auth);

                if (result && isMounted) {
                    setIsLoading(false);
                }
            } catch (err) {
                const errorMessage =
                    err instanceof Error ? err.message : "Failed to complete sign in. Please try again.";
                if (isMounted) {
                    setError(errorMessage);
                    setIsLoading(false);
                }
                console.error(err);
            }
        };

        void handleRedirectResult();

        return () => {
            isMounted = false;
        };
    }, []);

    // Handle One-Tap callback
    const handleOneTapCallback = useCallback(async (response: Record<string, unknown>) => {
        try {
            const idToken = typeof response.credential === "string" ? response.credential : null;
            if (!idToken) {
                throw new Error("Missing One-Tap credential.");
            }

            setIsLoading(true);
            setError(null);

            const credential = GoogleAuthProvider.credential(idToken);
            await signInWithCredential(auth, credential);
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : "Failed to sign in. Please try again.";
            setError(errorMessage);
            setIsLoading(false);
            console.error(err);
        }
    }, []);

    // Initialize Google One-Tap UI
    useEffect(() => {
        if (oneTapInitializedRef.current || !window.google || !GOOGLE_CLIENT_ID) return;

        oneTapInitializedRef.current = true;

        try {
            window.google.accounts.id.initialize({
                client_id: GOOGLE_CLIENT_ID,
                callback: handleOneTapCallback,
                auto_select: true,
                itp_support: true,
                use_fedcm_for_prompt: true,
            });

            // Display One-Tap UI (FedCM-compatible, no status callback needed)
            window.google.accounts.id.prompt();
        } catch (error) {
            console.error("Failed to initialize Google One-Tap:", error);
        }
    }, [handleOneTapCallback]);

    // Handle Google Sign-In button click
    // Uses popup on localhost (more reliable), redirect on production
    const handleRedirectSignIn = useCallback(async () => {
        try {
            setIsLoading(true);
            setError(null);

            // Cancel One-Tap to avoid interference
            window.google?.accounts.id.cancel();
            window.google?.accounts.id.disableAutoSelect();

            // Create provider with custom parameters
            const provider = new GoogleAuthProvider();
            provider.addScope("email");
            provider.addScope("profile");
            provider.setCustomParameters({
                prompt: "select_account",
            });

            await setPersistence(auth, browserLocalPersistence);

            // Use popup on localhost (avoids cross-origin storage issues), redirect on production
            const isLocalhost = window.location.hostname.includes("localhost");

            if (isLocalhost) {
                // Popup works reliably on localhost
                await signInWithPopup(auth, provider);
                setIsLoading(false);
            } else {
                // Redirect for better UX on production (same domain, no cross-origin issues)
                await signInWithRedirect(auth, provider);
            }
        } catch (err) {
            const errorMessage =
                err instanceof Error ? err.message : "Failed to sign in with Google. Please try again.";
            setError(errorMessage);
            setIsLoading(false);
            console.error(err);
        }
    }, []);

    return (
        <div className="w-full flex flex-col items-center gap-6">
            {/* Error Message */}
            {error ? (
                <div className="w-full bg-destructive/10 border border-destructive/30 rounded-lg p-4 flex items-start gap-3">
                    <div className="flex-1">
                        <p className="text-sm font-semibold text-destructive">Sign In Error</p>
                        <p className="text-sm text-destructive/80">{error}</p>
                    </div>
                </div>
            ) : null}

            {/* Google Sign-In Container */}
            <div className="flex flex-col items-center gap-4 w-full">
                <div className="text-center space-y-1">
                    <p className="text-sm text-muted-foreground">One-Tap Sign In Enabled</p>
                    <p className="text-xs text-muted-foreground/70">Look for the Google prompt in your browser</p>
                </div>

                {/* One-Tap UI Container - Google will inject here */}
                {/* <div id="g_container" className="w-full flex justify-center" /> */}

                {/* Fallback Button if One-Tap doesn't appear */}
                <div className="relative w-full">
                    <div className="absolute inset-0 flex items-center">
                        <div className="w-full border-t border-border" />
                    </div>
                    <div className="relative flex justify-center text-xs uppercase">
                        <span className="px-2 bg-background text-muted-foreground">OR</span>
                    </div>
                </div>

                {/* Google Account Chooser Button */}
                <button
                    onClick={() => void handleRedirectSignIn()}
                    disabled={isLoading}
                    className="w-full flex items-center justify-center gap-3 px-6 py-2.5 bg-white border border-gray-300 rounded-lg shadow-sm hover:shadow-md hover:bg-gray-50 transition-all duration-200 active:bg-gray-100 disabled:opacity-60 disabled:cursor-not-allowed"
                    aria-label="Choose Google account"
                >
                    {/* Google Logo */}
                    <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" className="w-5 h-5">
                        <path
                            fill="#EA4335"
                            d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"
                        />
                        <path
                            fill="#4285F4"
                            d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"
                        />
                        <path
                            fill="#FBBC05"
                            d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"
                        />
                        <path
                            fill="#34A853"
                            d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"
                        />
                        <path fill="none" d="M0 0h48v48H0z" />
                    </svg>

                    {/* Text */}
                    <span className="text-gray-700 font-medium text-sm">
                        {isLoading ? "Signing in..." : "Continue with Google"}
                    </span>
                </button>
            </div>
        </div>
    );
}
