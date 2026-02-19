import { Link } from "react-router";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { requireAdminClientLoader } from "@/lib/clientLoaders";

// eslint-disable-next-line react-refresh/only-export-components
export const clientLoader = requireAdminClientLoader;

export default function GroupCreatePage() {
    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold">Create Group</h1>
                    <p className="text-sm text-muted-foreground">Create a new group from Google Workspace.</p>
                </div>
                <Button asChild variant="outline">
                    <Link to="/admin/groups">Back to Groups</Link>
                </Button>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Create Group</CardTitle>
                    <CardDescription>
                        This feature is coming soon. Groups are currently managed from Google Workspace Admin Console.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="rounded-md bg-blue-50 p-4 text-sm text-blue-700">
                        To create a new group, please use the Google Workspace Admin Console.
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
