import SignIn from "../components/SignIn";

export default function SignInPage() {
    return (
        <div className="flex flex-col gap-6 sm:gap-8">
            {/* Hero Section */}
            <div className="text-center">
                <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold mb-2 bg-linear-to-r from-primary to-secondary bg-clip-text text-transparent">
                    Welcome Back
                </h1>
                <p className="text-base sm:text-lg text-muted-foreground">Sign in to access My South Church</p>
            </div>

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
