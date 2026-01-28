import { type admin_directory_v1 } from "googleapis";

type Member = admin_directory_v1.Schema$Member;

export default function GroupMemberItem(props: { member: Member }) {
    const { member } = props;
    const { email, role, delivery_settings, status } = member;
    if (!email) {
        return null;
    }
    return (
        <a
            href={"https://groups.google.com/a/southchurch.com/g/" + email.split("@")[0]}
            target="_blank"
            rel="noreferrer"
            className="no-underline"
        >
            <div className="bg-card hover:bg-card/80 border border-border p-4 flex gap-4 transition hover:cursor-pointer rounded-lg">
                <img
                    src="https://via.placeholder.com/100"
                    alt="placeholder"
                    className="w-16 h-16 rounded object-cover"
                />
                <div className="text-start align-top grow">
                    <h2 className="font-semibold text-foreground">{email}</h2>
                    <h4 className="text-sm text-muted-foreground">{role}</h4>
                    <p className="text-sm text-foreground/80">{delivery_settings}</p>
                    <p className="text-sm text-foreground/60">{status}</p>
                </div>
                <span className="material-symbols-outlined text-2xl text-muted-foreground">arrow_forward</span>
            </div>
        </a>
    );
}
