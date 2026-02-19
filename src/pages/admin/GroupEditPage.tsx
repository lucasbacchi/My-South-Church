import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { requireAdminClientLoader } from "@/lib/clientLoaders";
import { getGroup, type Group } from "@/lib/groups";

// eslint-disable-next-line react-refresh/only-export-components
export const clientLoader = requireAdminClientLoader;

export default function GroupEditPage() {
    const { groupId } = useParams();
    const [group, setGroup] = useState<Group | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!groupId) {
            setError("Missing group ID.");
            setIsLoading(false);
            return;
        }

        let isMounted = true;
        setIsLoading(true);
        setError(null);

        getGroup(groupId)
            .then((data) => {
                if (!isMounted) return;
                setGroup(data);
            })
            .catch((fetchError) => {
                if (!isMounted) return;
                setError(fetchError instanceof Error ? fetchError.message : "Failed to load group.");
            })
            .finally(() => {
                if (!isMounted) return;
                setIsLoading(false);
            });

        return () => {
            isMounted = false;
        };
    }, [groupId]);

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold">Edit Group</h1>
                    <p className="text-sm text-muted-foreground">Update group settings and configuration.</p>
                </div>
                <Button asChild variant="outline">
                    <Link to="/admin/groups">Back to Groups</Link>
                </Button>
            </div>

            {error ? (
                <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-destructive">
                    {error}
                </div>
            ) : null}

            {isLoading ? (
                <div className="rounded-lg border border-border bg-card p-6 text-muted-foreground">
                    Loading group details...
                </div>
            ) : group ? (
                <Card>
                    <CardHeader>
                        <CardTitle>{group.name}</CardTitle>
                        <CardDescription>Edit group configuration and settings.</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div className="rounded-md bg-blue-50 p-4 text-sm text-blue-700">
                            Group editing features are coming soon. To make changes to group settings, please use the
                            Google Workspace Admin Console.
                        </div>
                    </CardContent>
                </Card>
            ) : null}
        </div>
    );
}
