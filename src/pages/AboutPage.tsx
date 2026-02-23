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
                        <span>Contact the Staff</span>
                        <span className="material-symbols-outlined text-lg">mail</span>
                    </a>
                </div>
            </div>

            {/* Help & FAQ */}
            <div className="bg-card border border-border rounded-xl p-6 sm:p-8 shadow-lg flex flex-col gap-6">
                <div>
                    <h2 className="text-xl sm:text-2xl font-semibold text-foreground mb-2">Help & FAQs</h2>
                    <p className="text-sm sm:text-base text-muted-foreground">
                        Quick answers to common questions about using My South Church.
                    </p>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6">
                    <div className="border border-border rounded-lg p-4 sm:p-5 bg-muted/30">
                        <h3 className="text-base sm:text-lg font-semibold text-foreground mb-2">
                            Do I need to sign in?
                        </h3>
                        <p className="text-sm text-muted-foreground leading-relaxed">
                            Public resources are available without signing in. Sign in to access your groups, manage
                            files, and see member-only information.
                        </p>
                    </div>

                    <div className="border border-border rounded-lg p-4 sm:p-5 bg-muted/30">
                        <h3 className="text-base sm:text-lg font-semibold text-foreground mb-2">How do I sign in?</h3>
                        <p className="text-sm text-muted-foreground leading-relaxed">
                            Use the profile icon in the top bar and sign in with Google using your primary email
                            address. If you are not sure which email is on file, contact us at{" "}
                            <a
                                href="mailto:help@southchurch.com"
                                className="text-primary font-semibold hover:text-secondary transition-colors"
                            >
                                help@southchurch.com
                            </a>
                            .
                        </p>
                    </div>

                    <div className="border border-border rounded-lg p-4 sm:p-5 bg-muted/30">
                        <h3 className="text-base sm:text-lg font-semibold text-foreground mb-2">
                            I cannot see a group or file I need.
                        </h3>
                        <p className="text-sm text-muted-foreground leading-relaxed">
                            Permissions are managed through Google Groups using your Google Account. If something is
                            missing, make sure you are signed in with the correct email.
                        </p>
                    </div>

                    <div className="border border-border rounded-lg p-4 sm:p-5 bg-muted/30">
                        <h3 className="text-base sm:text-lg font-semibold text-foreground mb-2">
                            How do I change my primary email?
                        </h3>
                        <p className="text-sm text-muted-foreground leading-relaxed">
                            Your primary email is the one where you get all of your church email and it must be the same
                            as the one you use to sign in. If that account isn&apos;t attached to a Google Account, no
                            problem! We&apos;ve got{" "}
                            <a
                                target="_blank"
                                rel="noopener noreferrer"
                                href="https://docs.google.com/document/d/1BPyl5EQs-IHF9bSuhYElQvM7HgMyPyw8s5Dk-6vK6Wc"
                                className="underline"
                            >
                                a guide to help you
                            </a>
                            .
                        </p>
                    </div>

                    <div className="border border-border rounded-lg p-4 sm:p-5 bg-muted/30">
                        <h3 className="text-base sm:text-lg font-semibold text-foreground mb-2">
                            Where are the public documents?
                        </h3>
                        <p className="text-sm text-muted-foreground leading-relaxed">
                            Visit the Resources page to find newsletters, reports, and other public documents.
                        </p>
                    </div>

                    <div className="border border-border rounded-lg p-4 sm:p-5 bg-muted/30">
                        <h3 className="text-base sm:text-lg font-semibold text-foreground mb-2">
                            Still having trouble?
                        </h3>
                        <p className="text-sm text-muted-foreground leading-relaxed">
                            Start by emailing us at{" "}
                            <a
                                href="mailto:help@southchurch.com"
                                className="text-primary font-semibold hover:text-secondary transition-colors"
                            >
                                help@southchurch.com
                            </a>{" "}
                            and we will help get you setup.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}
