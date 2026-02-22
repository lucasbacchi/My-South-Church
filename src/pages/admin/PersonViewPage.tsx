import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { requireAdminClientLoader } from "@/lib/clientLoaders";
import { type GroupSummary, getGroupsForMemberEmail } from "@/lib/groups";
import { getPerson, verifyGoogleAccount } from "@/lib/people";
import { type Person } from "@/types/people";

// eslint-disable-next-line react-refresh/only-export-components
export const clientLoader = requireAdminClientLoader;

function formatDate(value: string | null | undefined) {
    if (!value) return "—";
    const parts = value.split("-");
    if (parts.length !== 3) return value;
    const [year, month, day] = parts.map(Number);
    const date = new Date(year, month - 1, day);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleDateString();
}

function formatDateTime(value: string | null | undefined) {
    if (!value) return "—";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
}

function formatVerifiedStatus(value: boolean | null | undefined) {
    if (value === true) return "Verified";
    if (value === false) return "Not a Google Account";
    return "Unknown";
}

function parseProblemDetailMessage(error: unknown) {
    if (!(error instanceof Error)) return null;
    const message = error.message?.trim();
    if (!message) return null;
    if (message.startsWith("{") && message.endsWith("}")) {
        try {
            const parsed = JSON.parse(message) as { detail?: string };
            if (typeof parsed?.detail === "string" && parsed.detail.trim()) {
                return parsed.detail.trim();
            }
        } catch {
            return message;
        }
    }
    return message;
}

export default function PersonViewPage() {
    const { personId } = useParams();
    const [person, setPerson] = useState<Person | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isVerifying, setIsVerifying] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [verifyMessage, setVerifyMessage] = useState<string | null>(null);
    const [groups, setGroups] = useState<GroupSummary[]>([]);
    const [groupsError, setGroupsError] = useState<string | null>(null);
    const [isGroupsLoading, setIsGroupsLoading] = useState(false);

    useEffect(() => {
        if (!personId) {
            setError("Missing person ID.");
            setIsLoading(false);
            return;
        }

        let isMounted = true;
        setIsLoading(true);
        setError(null);

        getPerson(personId)
            .then((data) => {
                if (!isMounted) return;
                setPerson(data);
            })
            .catch((fetchError) => {
                if (!isMounted) return;
                setError(fetchError instanceof Error ? fetchError.message : "Failed to load person.");
            })
            .finally(() => {
                if (!isMounted) return;
                setIsLoading(false);
            });

        return () => {
            isMounted = false;
        };
    }, [personId]);

    useEffect(() => {
        if (!person?.primaryEmail) {
            setGroups([]);
            setGroupsError(null);
            setIsGroupsLoading(false);
            return;
        }

        if (person.googleAccountVerified === false) {
            setGroups([]);
            setGroupsError("This person does not have a Google Account, so groups cannot be shown.");
            setIsGroupsLoading(false);
            return;
        }

        let isMounted = true;
        setIsGroupsLoading(true);
        setGroupsError(null);

        getGroupsForMemberEmail(person.primaryEmail)
            .then((data) => {
                if (!isMounted) return;
                setGroups(data ?? []);
            })
            .catch((groupsFetchError) => {
                if (!isMounted) return;
                const detail = parseProblemDetailMessage(groupsFetchError);
                if (detail?.toLowerCase().includes("does not have a google account")) {
                    setGroupsError("This person does not have a Google Account, so groups cannot be shown.");
                    return;
                }
                setGroupsError(detail ?? "Failed to load groups.");
            })
            .finally(() => {
                if (!isMounted) return;
                setIsGroupsLoading(false);
            });

        return () => {
            isMounted = false;
        };
    }, [person?.primaryEmail, person?.googleAccountVerified]);

    const handleVerifyClick = async () => {
        if (!person) return;
        setIsVerifying(true);
        setVerifyMessage(null);
        try {
            const updated = await verifyGoogleAccount(person.id);
            setPerson(updated);
            setVerifyMessage("Verification completed.");
        } catch (verifyError) {
            setVerifyMessage(verifyError instanceof Error ? verifyError.message : "Verification failed.");
        } finally {
            setIsVerifying(false);
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold">Person Details</h1>
                    <p className="text-sm text-muted-foreground">
                        View user details, groups, Drive files, and user-specific actions.
                    </p>
                </div>
                <div className="flex flex-wrap gap-2">
                    {person ? (
                        <Button asChild variant="outline">
                            <Link to={`/admin/people/${person.id}/edit`}>Edit Person</Link>
                        </Button>
                    ) : null}
                    <Button asChild variant="outline">
                        <Link to="/admin/people">Back to People</Link>
                    </Button>
                </div>
            </div>

            {error ? (
                <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-destructive">
                    {error}
                </div>
            ) : null}

            {isLoading ? (
                <div className="rounded-lg border border-border bg-card p-6 text-muted-foreground">
                    Loading person details...
                </div>
            ) : person ? (
                <div className="grid gap-6 lg:grid-cols-2">
                    <Card>
                        <CardHeader>
                            <CardTitle>
                                {person.firstName} {person.lastName}
                            </CardTitle>
                            <CardDescription>{person.primaryEmail}</CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-3 text-sm">
                            <div className="grid grid-cols-[140px_1fr] gap-2">
                                <div className="text-muted-foreground">Secondary Email</div>
                                <div>{person.secondaryEmail ?? "—"}</div>
                                <div className="text-muted-foreground">Phone</div>
                                <div>{person.phoneNumber ?? "—"}</div>
                                <div className="text-muted-foreground">Date of Birth</div>
                                <div>{formatDate(person.dateOfBirth)}</div>
                                <div className="text-muted-foreground">Firebase UID</div>
                                <div className="break-all">{person.firebaseUID ?? "—"}</div>
                                <div className="text-muted-foreground">Last Login</div>
                                <div>{formatDateTime(person.lastLogin)}</div>
                                <div className="text-muted-foreground">Google Account</div>
                                <div>{formatVerifiedStatus(person.googleAccountVerified)}</div>
                            </div>
                            <div className="flex flex-wrap gap-1">
                                {person.roles.length > 0 ? (
                                    person.roles.map((role) => (
                                        <Badge key={`${person.id}-${role}`} variant="secondary">
                                            {role}
                                        </Badge>
                                    ))
                                ) : (
                                    <span className="text-xs text-muted-foreground">No roles assigned</span>
                                )}
                            </div>
                        </CardContent>
                        <CardFooter className="flex flex-wrap gap-2">
                            <Button type="button" onClick={void handleVerifyClick} disabled={isVerifying}>
                                {isVerifying ? "Verifying..." : "Verify Google Account"}
                            </Button>
                            {verifyMessage ? (
                                <span className="text-sm text-muted-foreground">{verifyMessage}</span>
                            ) : null}
                        </CardFooter>
                    </Card>

                    <Card>
                        <CardHeader>
                            <CardTitle>Groups</CardTitle>
                            <CardDescription>Groups this user belongs to.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            {isGroupsLoading ? (
                                <div className="text-sm text-muted-foreground">Loading groups...</div>
                            ) : groupsError ? (
                                <div className="text-sm text-destructive">{groupsError}</div>
                            ) : groups.length > 0 ? (
                                <div className="space-y-3">
                                    {groups.map((group) => (
                                        <div
                                            key={group.id ?? group.email ?? "unknown"}
                                            className="rounded-md border border-border p-3"
                                        >
                                            <div className="text-sm font-medium">
                                                {group.name ?? group.email ?? "Unnamed group"}
                                            </div>
                                            {group.email ? (
                                                <div className="text-xs text-muted-foreground">{group.email}</div>
                                            ) : null}
                                            {group.description ? (
                                                <div className="text-xs text-muted-foreground">{group.description}</div>
                                            ) : null}
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <div className="text-sm text-muted-foreground">No groups found.</div>
                            )}
                        </CardContent>
                        <CardFooter>
                            <Button type="button" variant="outline" disabled>
                                Add to Group (coming soon)
                            </Button>
                        </CardFooter>
                    </Card>

                    <Card className="lg:col-span-2">
                        <CardHeader>
                            <CardTitle>Drive Files</CardTitle>
                            <CardDescription>Drive files shared with this user.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div className="text-sm text-muted-foreground">No Drive file data loaded yet.</div>
                        </CardContent>
                        <CardFooter>
                            <Button type="button" variant="outline" disabled>
                                Manage Drive Access (coming soon)
                            </Button>
                        </CardFooter>
                    </Card>
                </div>
            ) : null}
        </div>
    );
}
