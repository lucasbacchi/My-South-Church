import { useNavigate } from "react-router";

export default function CatchAll() {
    const navigate = useNavigate();

    return (
        <div className="min-h-screen bg-background flex items-center justify-center px-3 sm:px-4">
            <div className="max-w-md w-full text-center flex flex-col gap-6 sm:gap-8">
                {/* 404 Number */}
                <div>
                    <h1 className="text-7xl sm:text-8xl md:text-9xl font-bold bg-clip-text text-transparent bg-linear-to-r from-primary to-accent drop-shadow-lg">
                        404
                    </h1>
                </div>

                {/* Heading */}
                <div className="flex flex-col gap-3">
                    <h2 className="text-2xl sm:text-3xl md:text-4xl font-bold text-foreground">Page Not Found</h2>
                    <div className="h-1 w-12 sm:w-16 bg-linear-to-r from-primary to-accent rounded-full mx-auto" />
                </div>

                {/* Description */}
                <p className="text-sm sm:text-base md:text-lg text-muted-foreground leading-relaxed px-2">
                    The page you&#39;re looking for doesn&#39;t exist or may have been moved.
                </p>

                {/* Navigation Buttons */}
                <div className="flex flex-col gap-3 sm:flex-row sm:gap-4 justify-center pt-2 sm:pt-4">
                    <button
                        onClick={() => {
                            void navigate(-1);
                        }}
                        className="flex items-center justify-center gap-2 px-4 sm:px-6 py-2.5 sm:py-3 bg-card hover:bg-card/80 text-foreground text-sm sm:text-base font-semibold rounded-lg transition-all duration-200 border border-border hover:shadow-lg hover:-translate-y-0.5"
                    >
                        <span className="material-symbols-outlined text-lg sm:text-xl">arrow_back</span>
                        Go Back
                    </button>
                    <button
                        onClick={() => {
                            void navigate("/");
                        }}
                        className="flex items-center justify-center gap-2 px-4 sm:px-6 py-2.5 sm:py-3 bg-primary hover:bg-primary/90 text-primary-foreground text-sm sm:text-base font-semibold rounded-lg transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
                    >
                        Home
                        <span className="material-symbols-outlined text-lg sm:text-xl">arrow_forward</span>
                    </button>
                </div>
            </div>
        </div>
    );
}
