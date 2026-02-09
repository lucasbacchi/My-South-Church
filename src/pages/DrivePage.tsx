import { requireAuthClientLoader } from "../lib/clientLoaders";

// eslint-disable-next-line react-refresh/only-export-components
export const clientLoader = requireAuthClientLoader;

export default function DrivePage() {
    return (
        <div className="flex flex-col gap-6 sm:gap-8">
            {/* Hero Section */}
            <div>
                <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold bg-linear-to-r from-primary to-secondary bg-clip-text text-transparent">
                    Your Drive Folders
                </h1>
                <div className="h-1 w-16 sm:w-20 bg-linear-to-r from-primary to-secondary rounded-full" />
            </div>

            {/* Welcome Card */}
            <div className="bg-linear-to-br from-primary/10 to-accent/10 border border-primary/20 rounded-xl p-6 sm:p-8 shadow-md">
                <h2 className="text-xl sm:text-2xl font-semibold text-foreground mb-2">Shared Resources</h2>
                <p className="text-sm sm:text-base text-foreground/70 mb-4">
                    Access all shared documents and resources from South Church. Find what you need quickly and easily.
                </p>
                <button className="flex items-center gap-2 px-4 py-2 bg-primary hover:bg-primary/90 text-primary-foreground font-semibold rounded-lg transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5">
                    <span>Browse Drives</span>
                    <span className="material-symbols-outlined text-lg">arrow_forward</span>
                </button>
            </div>

            {/* Loading State */}
            <div className="bg-card border border-border rounded-xl p-8 sm:p-12 text-center">
                <div className="inline-flex items-center justify-center mb-4">
                    <span className="material-symbols-outlined text-4xl sm:text-5xl text-primary animate-pulse">
                        folder_open
                    </span>
                </div>
                <p className="text-base sm:text-lg text-muted-foreground">Loading your shared drives...</p>
            </div>
        </div>
    );
}
