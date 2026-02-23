const resources = [
    {
        title: "Lent 2026 Newsletter",
        href: "https://southchurch.com/wp/wp-content/uploads/2026/02/Digital-Newsletter-Lent-2026-compressed-1.pdf",
    },
    {
        title: "Annual Financial Report",
        href: "https://southchurch.com/wp/wp-content/uploads/2026/02/2025-Annual-Report-v2.pdf",
    },
    {
        title: "Annual Program Report",
        href: "https://southchurch.com/wp/wp-content/uploads/2025/06/2025.06.AnnualProgramReport.docx.pdf",
    },
    {
        title: "Behavioral Covenant",
        href: "https://southchurch.com/wp/wp-content/uploads/2024/10/Behavioral-Covenant.pdf",
    },
    {
        title: "Bylaws",
        href: "https://southchurch.com/wp/wp-content/uploads/2023/06/SC-Bylaws2023.pdf",
    },
    {
        title: "Ministry & Teams Chart",
        href: "https://southchurch.com/wp/wp-content/uploads/2025/06/Nominatng-Slate-2025-1024x766.jpg",
        note: {
            prefix: "Visit our online directory in ",
            href: "https://onrealm.org",
            linkText: "Realm",
            suffix: " for contact info.",
        },
        subNote: {
            text: "Learn more about each team's mission, responsibilities and goals",
            href: "https://southchurch.com/wp/wp-content/uploads/2021/07/Church-Charters-Combined.pdf",
            linkText: "here",
        },
    },
    {
        title: "Realm",
        href: "https://onrealm.org",
    },
    {
        title: "Reimbursement Form",
        href: "https://southchurch.com/wp/wp-content/uploads/2020/08/Check-Request-Rev2018.pdf",
    },
    {
        title: "Service Central",
        href: "https://bttr.im/r6fjk",
    },
];

export default function ResourcesPage() {
    return (
        <div className="flex flex-col gap-6 sm:gap-8">
            <title>Resources | My South Church</title>

            <div>
                <h1 className="text-3xl sm:text-4xl font-bold text-foreground">Resources</h1>
                <p className="text-muted-foreground max-w-2xl">
                    Quick access to public documents, reports, and tools for the South Church community.
                </p>
            </div>

            <div className="card">
                <ul className="space-y-4">
                    {resources.map((resource) => (
                        <li
                            key={resource.title}
                            className="space-y-2 border-b border-border/50 pb-4 last:border-b-0 last:pb-0"
                        >
                            <a
                                href={resource.href}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="inline-flex items-center gap-2 text-primary font-semibold hover:text-secondary transition-colors"
                            >
                                <span className="material-symbols-outlined text-base">open_in_new</span>
                                {resource.title}
                            </a>

                            {resource.note ? (
                                <p className="text-sm text-muted-foreground">
                                    {resource.note.prefix}
                                    <a
                                        href={resource.note.href}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="text-primary font-semibold hover:text-secondary transition-colors"
                                    >
                                        {resource.note.linkText}
                                    </a>
                                    {resource.note.suffix}
                                </p>
                            ) : null}

                            {resource.subNote ? (
                                <p className="text-sm text-muted-foreground">
                                    {resource.subNote.text}{" "}
                                    <a
                                        href={resource.subNote.href}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="text-primary font-semibold hover:text-secondary transition-colors"
                                    >
                                        {resource.subNote.linkText}.
                                    </a>
                                </p>
                            ) : null}
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
}
