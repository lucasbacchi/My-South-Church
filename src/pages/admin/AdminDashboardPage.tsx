import { Button } from "@/components/ui/button";
import { Link } from "react-router";

export default function AdminDashboardPage() {
    return (
        <>
            <h1 className="text-2xl font-bold mb-4">Admin Dashboard</h1>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="border border-border rounded-lg p-6 bg-card">
                    <h2 className="text-xl font-semibold mb-4 text-foreground">Execute Actions</h2>
                    <div className="flex flex-col gap-2">
                        {/* <Button onClick={getExternalSharedContacts}>Get External Shared Contacts</Button> */}
                        <p className="text-muted-foreground text-sm">Actions coming soon...</p>
                    </div>
                </div>
                <div className="border border-border rounded-lg p-6 bg-card">
                    <h2 className="text-xl font-semibold mb-4 text-foreground">View Resources</h2>
                    <div className="flex flex-col gap-2">
                        <Link to="/admin/groups" className="no-underline">
                            <Button className="w-full">View Groups</Button>
                        </Link>
                        <Link to="/admin/people" className="no-underline">
                            <Button className="w-full">View People</Button>
                        </Link>
                        <Link to="/admin/users" className="no-underline">
                            <Button className="w-full">View Users</Button>
                        </Link>
                    </div>
                </div>
            </div>
        </>
    );
}
