import { Link } from "react-router";
import { useUser } from "../contexts/UserContextDefinition";
import { Button } from "../components/ui/button";

export default function HomePage() {
    const [user] = useUser();

    return (
        <div className="flex flex-col gap-6 sm:gap-8">
            <title>Home | My South Church</title>

            {/* Hero Section */}
            <div>
                <h1 className="text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-bold bg-linear-to-r from-primary to-secondary bg-clip-text text-transparent">
                    {user
                        ? `Welcome back${user.displayName ? `, ${user.displayName.split(" ")[0]}` : ""}!`
                        : "Welcome to My South Church!"}
                </h1>
                <div className="h-1 w-16 sm:w-20 bg-linear-to-r from-primary to-secondary rounded-full" />
                <p className="text-base sm:text-lg text-muted-foreground mt-3 sm:mt-4 max-w-2xl">
                    {user
                        ? "Your gateway to South Church community, resources, and fellowship"
                        : "Connect with our community and manage your church groups and resources"}
                </p>
            </div>

            {/* Login Prompt for Unauthenticated Users */}
            {!user && (
                <div className="bg-linear-to-r from-primary/10 via-secondary/10 to-accent/10 border-2 border-primary/30 rounded-xl p-6 sm:p-8">
                    <div className="flex flex-col md:flex-row items-center gap-6">
                        <div className="shrink-0">
                            <span className="material-symbols-outlined text-6xl sm:text-7xl text-primary">login</span>
                        </div>
                        <div className="grow text-center md:text-left">
                            <h2 className="text-2xl font-bold text-foreground mb-2">Sign In to Get Started</h2>
                            <p className="text-foreground/80 mb-4">
                                Access your groups, manage drive permissions, and stay connected with the South Church
                                community. Please sign in with your primary email address.
                            </p>
                            <Link to="/signin">
                                <Button size="lg" className="gap-2">
                                    <span className="material-symbols-outlined text-sm">account_circle</span>
                                    Sign In with Google
                                </Button>
                            </Link>
                        </div>
                    </div>
                </div>
            )}

            {/* Main Content Card */}
            <div className="card">
                <p className="text-lg text-foreground leading-relaxed">
                    My South Church is a community platform that allows members of South Church in Andover to access
                    information about the church and its ministries all in one place.
                </p>
                <p className="text-foreground/80 leading-relaxed mt-4">
                    {user
                        ? "Explore your groups, access shared resources, and stay connected with our church community."
                        : "Sign in to explore groups, access shared resources, and stay connected with our church community. The platform is actively being developed with new features coming regularly."}
                </p>
            </div>

            {/* Feature Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
                {user ? (
                    <>
                        <Link to="/groups" className="no-underline">
                            <div className="feature-card group">
                                <span className="material-symbols-outlined text-5xl text-primary mb-2 block group-hover:text-secondary transition-colors">
                                    groups
                                </span>
                                <h3 className="text-xl font-bold text-foreground">Groups</h3>
                                <p className="text-sm text-muted-foreground">
                                    Connect with church groups and ministries
                                </p>
                                <span className="material-symbols-outlined text-lg mt-4 text-primary group-hover:translate-x-1 transition-transform">
                                    arrow_forward
                                </span>
                            </div>
                        </Link>

                        <Link to="/drive" className="no-underline">
                            <div className="feature-card group">
                                <span className="material-symbols-outlined text-5xl text-secondary mb-2 block group-hover:text-accent transition-colors">
                                    folder_open
                                </span>
                                <h3 className="text-xl font-bold text-foreground">Resources</h3>
                                <p className="text-sm text-muted-foreground">Access shared files and documents</p>
                                <span className="material-symbols-outlined text-lg mt-4 text-secondary group-hover:translate-x-1 transition-transform">
                                    arrow_forward
                                </span>
                            </div>
                        </Link>

                        <Link to="/account" className="no-underline">
                            <div className="feature-card group">
                                <span className="material-symbols-outlined text-5xl text-accent mb-2 block group-hover:text-primary transition-colors">
                                    person
                                </span>
                                <h3 className="text-xl font-bold text-foreground">My Account</h3>
                                <p className="text-sm text-muted-foreground">Manage your account settings</p>
                                <span className="material-symbols-outlined text-lg mt-4 text-accent group-hover:translate-x-1 transition-transform">
                                    arrow_forward
                                </span>
                            </div>
                        </Link>
                    </>
                ) : (
                    <>
                        <div className="feature-card">
                            <span className="material-symbols-outlined text-5xl text-primary mb-2 block">groups</span>
                            <h3 className="text-xl font-bold text-foreground">Groups</h3>
                            <p className="text-sm text-muted-foreground">
                                Connect with church groups and ministries. Sign in to view your groups.
                            </p>
                        </div>

                        <div className="feature-card">
                            <span className="material-symbols-outlined text-5xl text-secondary mb-2 block">
                                folder_open
                            </span>
                            <h3 className="text-xl font-bold text-foreground">Resources</h3>
                            <p className="text-sm text-muted-foreground">
                                Access shared files and documents. Sign in to manage your permissions.
                            </p>
                        </div>

                        <div className="feature-card">
                            <span className="material-symbols-outlined text-5xl text-accent mb-2 block">security</span>
                            <h3 className="text-xl font-bold text-foreground">Secure Access</h3>
                            <p className="text-sm text-muted-foreground">
                                Your data is protected. Sign in with your church email to get started.
                            </p>
                        </div>
                    </>
                )}
            </div>

            {/* Call to Action Section - Only for authenticated users */}
            {user ? (
                <div
                    className="bg-linear-to-r from-primary/10 via-secondary/10 to-accent/10 border border-primary/20 rounded-xl p-8 mt-8 animate-fade-in-up"
                    style={{ animationDelay: "0.5s" }}
                >
                    <div className="grid md:grid-cols-2 gap-8 items-center">
                        <div>
                            <h2 className="text-3xl font-bold text-foreground mb-3">Explore Your Groups</h2>
                            <p className="text-foreground/80 mb-6">
                                View your groups, find resources, and stay connected with our church community.
                            </p>
                            <Link
                                to="/groups"
                                className="inline-flex items-center gap-2 px-6 py-3 bg-primary hover:bg-primary/90 text-primary-foreground font-semibold rounded-lg transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
                            >
                                Explore Groups
                                <span className="material-symbols-outlined">arrow_forward</span>
                            </Link>
                        </div>
                        <div className="text-center">
                            <span className="material-symbols-outlined text-8xl text-primary/30 block">
                                celebration
                            </span>
                        </div>
                    </div>
                </div>
            ) : null}

            {/* About Section - For unauthenticated users */}
            {!user && (
                <Link to="/about" className="no-underline">
                    <div className="bg-muted/50 border border-border rounded-xl p-6 hover:border-primary/30 transition-all">
                        <div className="flex items-center gap-4">
                            <span className="material-symbols-outlined text-5xl text-accent">info</span>
                            <div>
                                <h3 className="text-xl font-bold text-foreground">Learn More About Us</h3>
                                <p className="text-sm text-muted-foreground">
                                    Discover our mission, values, and what makes South Church special.
                                </p>
                            </div>
                            <span className="material-symbols-outlined text-2xl text-accent ml-auto">
                                arrow_forward
                            </span>
                        </div>
                    </div>
                </Link>
            )}
        </div>
    );
}
