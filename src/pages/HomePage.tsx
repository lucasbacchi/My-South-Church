// import SignIn from "../components/SignIn";
import { Link } from "react-router";

export default function HomePage() {
    return (
        <div className="flex flex-col gap-6 sm:gap-8">
            <title>Home | My South Church</title>

            {/* Hero Section */}
            <div>
                <h1 className="text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-bold bg-linear-to-r from-primary to-secondary bg-clip-text text-transparent">
                    Welcome to My South Church!
                </h1>
                <div className="h-1 w-16 sm:w-20 bg-linear-to-r from-primary to-secondary rounded-full" />
                <p className="text-base sm:text-lg text-muted-foreground mt-3 sm:mt-4 max-w-2xl">
                    Your gateway to South Church community, resources, and fellowship
                </p>
            </div>

            {/* Main Content Card */}
            <div className="card">
                <p className="text-lg text-foreground leading-relaxed">
                    My South Church is a community platform that allows members of South Church in Andover to access
                    information about the church and its ministries all in one place.
                </p>
                <p className="text-foreground/80 leading-relaxed mt-4">
                    Explore our groups, access shared resources, and stay connected with our church community. The
                    platform is actively being developed with new features coming regularly.
                </p>
            </div>

            {/* Feature Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
                <Link to="/groups" className="no-underline">
                    <div className="feature-card group">
                        <span className="material-symbols-outlined text-5xl text-primary mb-2 block group-hover:text-secondary transition-colors">
                            groups
                        </span>
                        <h3 className="text-xl font-bold text-foreground">Groups</h3>
                        <p className="text-sm text-muted-foreground">Connect with church groups and ministries</p>
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

                <Link to="/about" className="no-underline">
                    <div className="feature-card group">
                        <span className="material-symbols-outlined text-5xl text-accent mb-2 block group-hover:text-primary transition-colors">
                            info
                        </span>
                        <h3 className="text-xl font-bold text-foreground">Learn More</h3>
                        <p className="text-sm text-muted-foreground">Discover our mission and values</p>
                        <span className="material-symbols-outlined text-lg mt-4 text-accent group-hover:translate-x-1 transition-transform">
                            arrow_forward
                        </span>
                    </div>
                </Link>
            </div>

            {/* Call to Action Section */}
            <div
                className="bg-linear-to-r from-primary/10 via-secondary/10 to-accent/10 border border-primary/20 rounded-xl p-8 mt-8 animate-fade-in-up"
                style={{ animationDelay: "0.5s" }}
            >
                <div className="grid md:grid-cols-2 gap-8 items-center">
                    <div>
                        <h2 className="text-3xl font-bold text-foreground mb-3">Ready to Get Started?</h2>
                        <p className="text-foreground/80 mb-6">
                            Explore our groups, find resources, and become an active member of our church community.
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
                        <span className="material-symbols-outlined text-8xl text-primary/30 block">celebration</span>
                    </div>
                </div>
            </div>
        </div>
    );
}
