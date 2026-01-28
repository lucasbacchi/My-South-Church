import { useCallback, useContext, useEffect, useState } from "react";
import { GoogleAuthProvider, signInWithPopup } from "firebase/auth";
import { UserContext } from "../contexts/UserContextDefinition";
import { useNavigate } from "react-router";

import { auth } from "../firebase";

export default function SignIn() {
    const [user] = useContext(UserContext);
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Redirect if already logged in
    useEffect(() => {
        if (user) {
            void navigate("/");
        }
    }, [user, navigate]);

    // Handle Google Sign-In with popup
    const handleGoogleSignIn = useCallback(async () => {
        try {
            setIsLoading(true);
            setError(null);

            const provider = new GoogleAuthProvider();
            await signInWithPopup(auth, provider);
            // UserContext will automatically update via onAuthStateChanged
        } catch (err) {
            const errorMessage =
                err instanceof Error ? err.message : "Failed to sign in with Google. Please try again.";
            setError(errorMessage);
            console.error("Google Sign-In Error:", err);
        } finally {
            setIsLoading(false);
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
                <p className="text-center text-sm text-muted-foreground">Sign in with your Google account</p>

                {/* Custom Google Sign-In Button */}
                <button
                    onClick={void handleGoogleSignIn}
                    disabled={isLoading}
                    className="flex items-center justify-center gap-3 px-6 py-2.5 bg-white border border-gray-300 rounded-lg shadow-sm hover:shadow-md hover:bg-gray-50 transition-all duration-200 active:bg-gray-100 disabled:opacity-60 disabled:cursor-not-allowed"
                    aria-label="Sign in with Google"
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
                        {isLoading ? "Signing in..." : "Sign in with Google"}
                    </span>
                </button>
            </div>
        </div>
    );
}
