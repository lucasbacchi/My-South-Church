import SignIn from "../components/SignIn";
import { useSearchParams } from "react-router";

export default function SignInPage() {
    const [searchParams] = useSearchParams();
    const redirectTo = searchParams.get("redirect");

    return (
        <div className="flex flex-col gap-6 sm:gap-8">
            {/* Hero Section */}
            <div className="text-center">
                <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold mb-2 bg-linear-to-r from-primary to-secondary bg-clip-text text-transparent">
                    {redirectTo ? "Sign In Required" : "Welcome Back"}
                </h1>
                <p className="text-base sm:text-lg text-muted-foreground">
                    {redirectTo ? "Please sign in to access this page" : "Sign in to access My South Church"}
                </p>
            </div>

            {/* Redirect Notice */}
            {redirectTo !== null && (
                <div className="max-w-md mx-auto w-full">
                    <div className="bg-primary/10 border border-primary/30 rounded-lg p-4 flex items-start gap-3">
                        <span className="material-symbols-outlined text-primary text-xl">info</span>
                        <div className="flex-1">
                            <p className="text-sm font-semibold text-foreground">Authentication Required</p>
                            <p className="text-sm text-muted-foreground mt-1">
                                You&apos;ll be redirected to your requested page after signing in.
                            </p>
                        </div>
                    </div>
                </div>
            )}

            {/* Sign In Card */}
            <div className="max-w-md mx-auto w-full">
                <div className="bg-card border border-border rounded-xl p-6 sm:p-8 shadow-lg">
                    <SignIn />
                </div>
                <p className="text-center text-sm text-muted-foreground mt-6">
                    Questions? Contact us at{" "}
                    <a href="mailto:help@southchurch.com" className="text-primary underline">
                        help@southchurch.com
                    </a>
                </p>
            </div>
        </div>
    );
}
