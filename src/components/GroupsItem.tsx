import { type admin_directory_v1 } from "googleapis";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { ArrowRight } from "lucide-react";

export default function GroupsItem(group: admin_directory_v1.Schema$Group) {
    const { name, email, description } = group;
    if (!name || !email) {
        return null;
    }

    const getInitials = (email: string) => {
        const emailID = email.split("@")[0] ?? "";
        let initials = "";
        let start = 0;
        let end = emailID.length;
        for (let i = 0; i < emailID.length; i++) {
            if (emailID[i] === "." || emailID[i] === "_" || i === emailID.length - 1) {
                end = i;
                initials += emailID.substring(start, end).charAt(0).toUpperCase();
                start = i + 1;
                if (initials.length === 3) {
                    break;
                }
            }
        }
        return initials;
    };

    return (
        <a href={"/admin/groups/" + email.split("@")[0]} className="block">
            <div className="bg-card hover:bg-accent/50 text-card-foreground p-4 flex gap-4 transition items-center rounded-lg border">
                <Avatar>
                    <AvatarFallback>{getInitials(email)}</AvatarFallback>
                </Avatar>
                <div className="text-start align-top grow space-y-1">
                    <h2 className="font-semibold leading-none">{name}</h2>
                    <h4 className="text-sm text-muted-foreground">{email}</h4>
                    <p className="text-sm text-foreground/80 line-clamp-2">{description}</p>
                </div>
                <ArrowRight className="h-5 w-5 text-muted-foreground" />
            </div>
        </a>
    );
}
