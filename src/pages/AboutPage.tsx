export default function AboutPage() {
    return (
        <div className="flex flex-col gap-6 sm:gap-8">
            {/* Hero Section */}
            <div>
                <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold bg-linear-to-r from-primary to-secondary bg-clip-text text-transparent">
                    What is My South Church?
                </h1>
                <div className="h-1 w-16 sm:w-20 bg-linear-to-r from-primary to-secondary rounded-full" />
            </div>

            {/* Main Content */}
            <div className="bg-card border border-border rounded-xl p-6 sm:p-8 shadow-lg flex flex-col gap-6">
                <div>
                    <h2 className="text-xl sm:text-2xl font-semibold text-foreground mb-3 sm:mb-4">This Platform</h2>
                    <p className="text-sm sm:text-base text-foreground/80 leading-relaxed mb-4">
                        My South Church connects our members and provides easy access to church resources, groups, and
                        information. It&apos;s a place where our community can stay informed, connected, and engaged.
                    </p>
                </div>

                <div className="flex flex-col sm:flex-row gap-4 mt-6">
                    <a
                        href="https://southchurch.com"
                        target="_blank"
                        rel="noreferrer"
                        className="flex items-center justify-center gap-2 px-6 py-3 bg-primary hover:bg-primary/90 hover:text-primary-foreground! text-primary-foreground font-semibold rounded-lg transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
                    >
                        <span>Visit Main Website</span>
                        <span className="material-symbols-outlined text-lg">arrow_outward</span>
                    </a>
                    <a
                        href="https://southchurch.com/contact"
                        target="_blank"
                        rel="noreferrer"
                        className="flex items-center justify-center gap-2 px-6 py-3 bg-card border border-border hover:bg-primary/5 text-foreground font-semibold rounded-lg transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
                    >
                        <span>Contact Us</span>
                        <span className="material-symbols-outlined text-lg">mail</span>
                    </a>
                </div>
            </div>
        </div>
    );
}
