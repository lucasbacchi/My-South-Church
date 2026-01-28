import { isRouteErrorResponse, useNavigate, useRouteError } from "react-router";

export default function ErrorPage() {
    const error = useRouteError();
    const navigate = useNavigate();

    if (isRouteErrorResponse(error)) {
        let statusText = error.statusText;
        if (error.status === 401) {
            statusText = "Unauthorized";
        } else if (error.status === 404) {
            statusText = "Not Found";
        }

        const errorData = error.data as { message?: string } | undefined;

        return (
            <div className="min-h-screen bg-background flex items-center justify-center px-4">
                <div className="max-w-md w-full text-center space-y-8">
                    <div className="space-y-4">
                        <h1 className="text-9xl font-bold bg-clip-text text-transparent bg-linear-to-r from-accent to-secondary">
                            {error.status}
                        </h1>
                        <div className="h-1 w-16 bg-linear-to-r from-accent to-secondary rounded-full mx-auto" />
                    </div>
                    <div>
                        <h2 className="text-4xl font-bold text-foreground mb-3">{statusText}</h2>
                        <p className="text-muted-foreground text-lg leading-relaxed">
                            {errorData?.message ?? "An error occurred while processing your request."}
                        </p>
                    </div>
                    <div className="flex flex-col gap-3 sm:flex-row sm:gap-4 justify-center pt-4">
                        <button
                            onClick={() => void navigate(-1)}
                            className="flex items-center justify-center gap-2 px-6 py-3 bg-card hover:bg-card/80 text-foreground font-semibold rounded-lg transition-all duration-200 border border-border hover:shadow-lg hover:-translate-y-0.5"
                        >
                            <span className="material-symbols-outlined">arrow_back</span>
                            Go Back
                        </button>
                        <button
                            onClick={() => void navigate("/")}
                            className="flex items-center justify-center gap-2 px-6 py-3 bg-primary hover:bg-primary/90 text-primary-foreground font-semibold rounded-lg transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
                        >
                            Home
                            <span className="material-symbols-outlined">arrow_forward</span>
                        </button>
                    </div>
                </div>
            </div>
        );
    } else if (error instanceof Error) {
        return (
            <div className="min-h-screen bg-background flex items-center justify-center px-4">
                <div className="max-w-md w-full text-center space-y-8">
                    <div className="space-y-4">
                        <h1 className="text-6xl font-bold bg-clip-text text-transparent bg-linear-to-r from-destructive to-accent">
                            Error
                        </h1>
                        <div className="h-1 w-16 bg-linear-to-r from-destructive to-accent rounded-full mx-auto" />
                    </div>
                    <div>
                        <h2 className="text-4xl font-bold text-foreground mb-3">Unexpected Error</h2>
                        <p className="text-muted-foreground text-lg mb-2">Something went wrong.</p>
                        <p className="text-destructive/80 text-sm italic">{error.message}</p>
                    </div>
                    <div className="flex flex-col gap-3 sm:flex-row sm:gap-4 justify-center pt-4">
                        <button
                            onClick={() => void navigate(-1)}
                            className="flex items-center justify-center gap-2 px-6 py-3 bg-card hover:bg-card/80 text-foreground font-semibold rounded-lg transition-all duration-200 border border-border hover:shadow-lg hover:-translate-y-0.5"
                        >
                            <span className="material-symbols-outlined">arrow_back</span>
                            Go Back
                        </button>
                        <button
                            onClick={() => void navigate("/")}
                            className="flex items-center justify-center gap-2 px-6 py-3 bg-primary hover:bg-primary/90 text-primary-foreground font-semibold rounded-lg transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
                        >
                            Home
                            <span className="material-symbols-outlined">arrow_forward</span>
                        </button>
                    </div>
                </div>
            </div>
        );
    } else {
        return <></>;
    }
}
