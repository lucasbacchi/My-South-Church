import { Link } from "react-router";
import { useUser } from "../contexts/UserContextDefinition";

export default function BrowseFilesPage() {
    const [user] = useUser();

    return (
        <div className="flex flex-col gap-6 sm:gap-8">
            <title>Browse Files | My South Church</title>

            <div>
                <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold bg-linear-to-r from-primary to-secondary bg-clip-text text-transparent">
                    Browse Files
                </h1>
                <div className="h-1 w-16 sm:w-20 bg-linear-to-r from-primary to-secondary rounded-full" />
                <p className="text-muted-foreground mt-3 sm:mt-4 max-w-2xl">
                    Find public files, newsletters, and shared documents.
                </p>
            </div>

            <div className="card">
                <h2 className="text-xl sm:text-2xl font-semibold text-foreground mb-2">Public Files</h2>
                <p className="text-sm sm:text-base text-muted-foreground leading-relaxed">
                    This page highlights public files available to everyone. See the Resources page for official reports
                    and downloads.
                </p>
                <div className="mt-4">
                    <Link
                        to="/resources"
                        className="inline-flex items-center gap-2 text-primary font-semibold hover:text-secondary transition-colors"
                    >
                        <span className="material-symbols-outlined text-base">open_in_new</span>
                        View Resources
                    </Link>
                </div>
            </div>

            <div className="bg-linear-to-br from-primary/10 to-accent/10 border border-primary/20 rounded-xl p-6 sm:p-8 shadow-md">
                <h2 className="text-xl sm:text-2xl font-semibold text-foreground mb-2">Need your shared files?</h2>
                <p className="text-sm sm:text-base text-foreground/70 mb-4">
                    Sign in to access files shared with your ministry or group.
                </p>
                {user ? (
                    <Link
                        to="/files"
                        className="inline-flex items-center gap-2 px-4 py-2 bg-primary hover:bg-primary/90 text-primary-foreground font-semibold rounded-lg transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
                    >
                        <span>Go to My Files</span>
                        <span className="material-symbols-outlined text-lg">arrow_forward</span>
                    </Link>
                ) : (
                    <Link
                        to="/signin"
                        className="inline-flex items-center gap-2 px-4 py-2 bg-primary hover:bg-primary/90 text-primary-foreground font-semibold rounded-lg transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
                    >
                        <span>Sign In</span>
                        <span className="material-symbols-outlined text-lg">login</span>
                    </Link>
                )}
            </div>
        </div>
    );
}
