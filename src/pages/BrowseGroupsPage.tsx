import { Link } from "react-router";
import { useUser } from "../contexts/UserContextDefinition";

export default function BrowseGroupsPage() {
    const [user] = useUser();

    return (
        <div className="flex flex-col gap-6 sm:gap-8">
            <title>Browse Groups | My South Church</title>

            <div>
                <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold bg-linear-to-r from-primary to-secondary bg-clip-text text-transparent">
                    Browse Groups
                </h1>
                <div className="h-1 w-16 sm:w-20 bg-linear-to-r from-primary to-secondary rounded-full" />
                <p className="text-muted-foreground mt-3 sm:mt-4 max-w-2xl">
                    Explore public groups and ministries at South Church.
                </p>
            </div>

            <div className="card">
                <h2 className="text-xl sm:text-2xl font-semibold text-foreground mb-2">Public Groups</h2>
                <p className="text-sm sm:text-base text-muted-foreground leading-relaxed">
                    This page highlights public groups and ministries. More listings will be added over time.
                </p>
            </div>

            <div className="bg-linear-to-br from-primary/10 to-secondary/10 border border-primary/20 rounded-xl p-6 sm:p-8 shadow-md">
                <h2 className="text-xl sm:text-2xl font-semibold text-foreground mb-2">Looking for your groups?</h2>
                <p className="text-sm sm:text-base text-foreground/70 mb-4">
                    Sign in to see your member-only groups and tools.
                </p>
                {user ? (
                    <Link
                        to="/groups"
                        className="inline-flex items-center gap-2 px-4 py-2 bg-primary hover:bg-primary/90 text-primary-foreground font-semibold rounded-lg transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
                    >
                        <span>Go to My Groups</span>
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
