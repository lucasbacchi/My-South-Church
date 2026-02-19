import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { requireAdminClientLoader } from "@/lib/clientLoaders";
import {
    getGroup,
    getGroupMembers,
    getGroupSettings,
    type Group,
    type GroupMember,
    type GroupSettings,
} from "@/lib/groups";
import { getPeople } from "@/lib/people";
import type { Person } from "@/types/people";

// eslint-disable-next-line react-refresh/only-export-components
export const clientLoader = requireAdminClientLoader;

export default function GroupViewPage() {
    const { groupId } = useParams();
    const [group, setGroup] = useState<Group | null>(null);
    const [members, setMembers] = useState<GroupMember[]>([]);
    const [settings, setSettings] = useState<GroupSettings | null>(null);
    const [people, setPeople] = useState<Person[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isMembersLoading, setIsMembersLoading] = useState(false);
    const [isSettingsLoading, setIsSettingsLoading] = useState(false);
    const [isPeopleLoading, setIsPeopleLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [membersError, setMembersError] = useState<string | null>(null);
    const [settingsError, setSettingsError] = useState<string | null>(null);

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

    useEffect(() => {
        if (!groupId) return;

        let isMounted = true;
        setIsMembersLoading(true);
        setMembersError(null);

        getGroupMembers(groupId)
            .then((data) => {
                if (!isMounted) return;
                setMembers(data ?? []);
            })
            .catch((fetchError) => {
                if (!isMounted) return;
                setMembersError(fetchError instanceof Error ? fetchError.message : "Failed to load members.");
            })
            .finally(() => {
                if (!isMounted) return;
                setIsMembersLoading(false);
            });

        return () => {
            isMounted = false;
        };
    }, [groupId]);

    useEffect(() => {
        if (!groupId) return;

        let isMounted = true;
        setIsSettingsLoading(true);
        setSettingsError(null);

        getGroupSettings(groupId)
            .then((data) => {
                if (!isMounted) return;
                setSettings(data ?? null);
            })
            .catch((fetchError) => {
                if (!isMounted) return;
                setSettingsError(fetchError instanceof Error ? fetchError.message : "Failed to load settings.");
            })
            .finally(() => {
                if (!isMounted) return;
                setIsSettingsLoading(false);
            });

        return () => {
            isMounted = false;
        };
    }, [groupId]);

    useEffect(() => {
        let isMounted = true;
        setIsPeopleLoading(true);

        getPeople()
            .then((data) => {
                if (!isMounted) return;
                setPeople(data ?? []);
            })
            .catch((fetchError) => {
                if (!isMounted) return;
                // Silently fail - we can still show members, just without person details
                console.warn("Failed to load people:", fetchError);
            })
            .finally(() => {
                if (!isMounted) return;
                setIsPeopleLoading(false);
            });

        return () => {
            isMounted = false;
        };
    }, []);

    const formatBoolean = (value: boolean | string | undefined): string => {
        if (value === undefined || value === null) return "—";

        // Handle string booleans (in case API returns them as strings)
        if (typeof value === "string") {
            const lower = value.toLowerCase();
            if (lower === "true") return "Yes";
            if (lower === "false") return "No";
            return "—";
        }

        // Handle boolean primitives
        return value ? "Yes" : "No";
    };

    const formatEnumValue = (value: string | undefined): string => {
        if (!value) return "—";
        // Convert UPPER_SNAKE_CASE or camelCase to Title Case
        return value
            .replace(/_/g, " ") // Convert underscores to spaces
            .replace(/([a-z])([A-Z])/g, "$1 $2") // Add space in camelCase transitions
            .replace(/\b\w/g, (char) => char.toUpperCase()) // Title case
            .trim();
    };

    interface PermissionLevel {
        level: "restrictive" | "moderate" | "permissive" | "very_permissive";
        icon: string;
        color: string;
        bgColor: string;
    }

    const getPermissionLevel = (value: string | boolean | undefined): PermissionLevel => {
        // Handle booleans and string booleans
        if (typeof value === "boolean") {
            if (value === true) {
                return { level: "very_permissive", icon: "✓", color: "text-emerald-600", bgColor: "bg-emerald-50" };
            } else {
                return { level: "restrictive", icon: "✗", color: "text-red-600", bgColor: "bg-red-50" };
            }
        }

        // Handle string booleans (in case API returns them as strings)
        if (typeof value === "string") {
            const lower = value.toLowerCase();
            if (lower === "true") {
                return { level: "very_permissive", icon: "✓", color: "text-emerald-600", bgColor: "bg-emerald-50" };
            } else if (lower === "false") {
                return { level: "restrictive", icon: "✗", color: "text-red-600", bgColor: "bg-red-50" };
            }

            // Handle enum strings
            const upper = value.toUpperCase();

            // Restrictive (owner only, none)
            if (upper.includes("OWNER_ONLY") || upper === "NONE") {
                return { level: "restrictive", icon: "🔒", color: "text-red-600", bgColor: "bg-red-50" };
            }

            // Moderate (managers, specific roles only)
            if (upper.includes("MANAGER") && !upper.includes("AND")) {
                return { level: "moderate", icon: "👥", color: "text-blue-600", bgColor: "bg-blue-50" };
            }

            // Permissive (members, managers and members, in-domain)
            if (upper.includes("MEMBER") || upper.includes("AND") || upper.includes("IN_DOMAIN")) {
                return { level: "permissive", icon: "📢", color: "text-amber-600", bgColor: "bg-amber-50" };
            }

            // Very Permissive (anyone, all)
            if (
                upper.includes("ANY") ||
                upper.includes("ALL") ||
                upper.includes("ANYONE") ||
                upper.includes("EXTERNAL")
            ) {
                return { level: "very_permissive", icon: "🌐", color: "text-emerald-600", bgColor: "bg-emerald-50" };
            }
        }

        // Default moderate for undefined or unrecognized values
        return { level: "moderate", icon: "◯", color: "text-gray-500", bgColor: "bg-gray-100" };
    };

    const getPersonByEmail = (email: string): Person | undefined => {
        return people.find((p) => p.primaryEmail === email || p.secondaryEmail === email);
    };

    const getMemberDisplay = (member: GroupMember) => {
        const person = getPersonByEmail(member.email);

        if (!person) {
            return {
                name: member.email,
                displayName: member.email,
                icon: "❓",
                statusLabel: "Unknown",
                statusColor: "text-gray-600",
                statusBg: "bg-gray-100",
            };
        }

        if (person.googleAccountVerified === false) {
            return {
                name: `${person.firstName} ${person.lastName}`,
                displayName: `${person.firstName} ${person.lastName}`,
                icon: "❌",
                statusLabel: "Invalid Google Account",
                statusColor: "text-red-600",
                statusBg: "bg-red-50",
            };
        }

        if (person.googleAccountVerified === true) {
            return {
                name: `${person.firstName} ${person.lastName}`,
                displayName: `${person.firstName} ${person.lastName}`,
                icon: "✓",
                statusLabel: "Verified",
                statusColor: "text-emerald-600",
                statusBg: "bg-emerald-50",
            };
        }

        // googleAccountVerified is null/undefined - not yet checked
        return {
            name: `${person.firstName} ${person.lastName}`,
            displayName: `${person.firstName} ${person.lastName}`,
            icon: "⚠️",
            statusLabel: "Not Verified",
            statusColor: "text-orange-600",
            statusBg: "bg-orange-50",
        };
    };

    return (
        <div className="flex flex-col gap-6 p-6">
            <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold">Group Details</h1>
                    <p className="text-sm text-muted-foreground">View group information, members, and settings.</p>
                </div>
                <div className="flex flex-wrap gap-2">
                    {group ? (
                        <Button asChild variant="outline">
                            <Link to={`/admin/groups/${group.id}/edit`}>Edit Group</Link>
                        </Button>
                    ) : null}
                    <Button asChild variant="outline">
                        <Link to="/admin/groups">Back to Groups</Link>
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
                    Loading group details...
                </div>
            ) : group ? (
                <div className="grid gap-6 lg:grid-cols-2">
                    <Card>
                        <CardHeader>
                            <CardTitle>{group.name}</CardTitle>
                            <CardDescription>{group.email}</CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-3 text-sm">
                            <div className="grid grid-cols-[140px_1fr] gap-2">
                                <div className="text-muted-foreground">Name</div>
                                <div>{group.name ?? "—"}</div>
                                <div className="text-muted-foreground">Email</div>
                                <div className="break-all">{group.email ?? "—"}</div>
                                <div className="text-muted-foreground">Description</div>
                                <div className="truncate">{group.description ?? "—"}</div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card className="lg:col-span-2">
                        <CardHeader>
                            <CardTitle>Members ({members.length})</CardTitle>
                            <CardDescription>All members and their Google Account verification status.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            {isMembersLoading || isPeopleLoading ? (
                                <div className="text-sm text-muted-foreground">Loading members...</div>
                            ) : membersError ? (
                                <div className="text-sm text-destructive">{membersError}</div>
                            ) : members.length > 0 ? (
                                <div className="rounded-md border overflow-hidden">
                                    <Table>
                                        <TableHeader>
                                            <TableRow>
                                                <TableHead>Name</TableHead>
                                                <TableHead>Role</TableHead>
                                                <TableHead>Google Account Status</TableHead>
                                            </TableRow>
                                        </TableHeader>
                                        <TableBody>
                                            {members.map((member) => {
                                                const display = getMemberDisplay(member);
                                                return (
                                                    <TableRow key={member.email}>
                                                        <TableCell>
                                                            <div>
                                                                <div className="font-medium">{display.displayName}</div>
                                                                <div className="text-xs text-muted-foreground">
                                                                    {member.email}
                                                                </div>
                                                            </div>
                                                        </TableCell>
                                                        <TableCell>
                                                            <Badge variant="outline">{member.role}</Badge>
                                                        </TableCell>
                                                        <TableCell>
                                                            <div
                                                                className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium w-fit ${display.statusBg} ${display.statusColor}`}
                                                            >
                                                                <span>{display.icon}</span>
                                                                {display.statusLabel}
                                                            </div>
                                                        </TableCell>
                                                    </TableRow>
                                                );
                                            })}
                                        </TableBody>
                                    </Table>
                                </div>
                            ) : (
                                <div className="text-sm text-muted-foreground">No members found.</div>
                            )}
                        </CardContent>
                    </Card>

                    <Card className="lg:col-span-2">
                        <CardHeader>
                            <CardTitle>Group Settings</CardTitle>
                            <CardDescription>Complete group configuration and permissions.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            {isSettingsLoading ? (
                                <div className="text-sm text-muted-foreground">Loading settings...</div>
                            ) : settingsError ? (
                                <div className="text-sm text-destructive">{settingsError}</div>
                            ) : settings ? (
                                <div className="space-y-6 text-sm">
                                    {/* Membership & Access */}
                                    <div>
                                        <h3 className="font-semibold mb-3">Membership & Access</h3>
                                        <div className="grid grid-cols-2 gap-3">
                                            {settings.whoCanJoin && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Join
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(settings.whoCanJoin);
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanJoin)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.whoCanAdd && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Add Members
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(settings.whoCanAdd);
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanAdd)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.whoCanInvite && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Invite
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(settings.whoCanInvite);
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanInvite)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.whoCanLeaveGroup && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Leave Group
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(settings.whoCanLeaveGroup);
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanLeaveGroup)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.whoCanApproveMembers && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Approve Members
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.whoCanApproveMembers
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanApproveMembers)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.whoCanModifyMembers && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Modify Members
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.whoCanModifyMembers
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanModifyMembers)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.allowExternalMembers !== undefined && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Allow External Members
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.allowExternalMembers
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatBoolean(settings.allowExternalMembers)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    </div>

                                    {/* Visibility & Directory */}
                                    <div>
                                        <h3 className="font-semibold mb-3">Visibility & Directory</h3>
                                        <div className="grid grid-cols-2 gap-3">
                                            {settings.whoCanDiscoverGroup && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Discover Group
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.whoCanDiscoverGroup
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanDiscoverGroup)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.whoCanViewGroup && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can View Group
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(settings.whoCanViewGroup);
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanViewGroup)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.whoCanViewMembership && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can View Membership
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.whoCanViewMembership
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanViewMembership)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.showInGroupDirectory !== undefined && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Show in Group Directory
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.showInGroupDirectory
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatBoolean(settings.showInGroupDirectory)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.includeInGlobalAddressList !== undefined && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Include in Global Address List
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.includeInGlobalAddressList
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatBoolean(settings.includeInGlobalAddressList)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    </div>

                                    {/* Posting & Messaging */}
                                    <div>
                                        <h3 className="font-semibold mb-3">Posting & Messaging</h3>
                                        <div className="grid grid-cols-2 gap-3">
                                            {settings.whoCanPostMessage && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Post Message
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(settings.whoCanPostMessage);
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanPostMessage)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.whoCanPostAnnouncements && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Post Announcements
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.whoCanPostAnnouncements
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanPostAnnouncements)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.whoCanApproveMessages && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Approve Messages
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.whoCanApproveMessages
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanApproveMessages)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.whoCanDeleteAnyPost && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Delete Any Post
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.whoCanDeleteAnyPost
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanDeleteAnyPost)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.allowWebPosting !== undefined && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Allow Web Posting
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(settings.allowWebPosting);
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatBoolean(settings.allowWebPosting)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.membersCanPostAsTheGroup !== undefined && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Members Can Post as Group
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.membersCanPostAsTheGroup
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatBoolean(settings.membersCanPostAsTheGroup)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.whoCanContactOwner && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Contact Owner
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.whoCanContactOwner
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanContactOwner)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    </div>

                                    {/* Moderation */}
                                    <div>
                                        <h3 className="font-semibold mb-3">Moderation</h3>
                                        <div className="grid grid-cols-2 gap-3">
                                            {settings.messageModerationLevel && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Message Moderation Level
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.messageModerationLevel
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.messageModerationLevel)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.spamModerationLevel && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Spam Moderation Level
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.spamModerationLevel
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.spamModerationLevel)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.whoCanModerateContent && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Moderate Content
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.whoCanModerateContent
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanModerateContent)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.whoCanModerateMembers && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Who Can Moderate Members
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.whoCanModerateMembers
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.whoCanModerateMembers)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.sendMessageDenyNotification !== undefined && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Send Message Deny Notification
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.sendMessageDenyNotification
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatBoolean(
                                                                        settings.sendMessageDenyNotification
                                                                    )}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    </div>

                                    {/* General Settings */}
                                    <div>
                                        <h3 className="font-semibold mb-3">General Settings</h3>
                                        <div className="grid grid-cols-2 gap-3">
                                            {settings.isArchived !== undefined && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">Archived</div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(settings.isArchived);
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatBoolean(settings.isArchived)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.enableCollaborativeInbox !== undefined && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Enable Collaborative Inbox
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(
                                                                settings.enableCollaborativeInbox
                                                            );
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatBoolean(settings.enableCollaborativeInbox)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.replyTo && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">Reply To</div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(settings.replyTo);
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.replyTo)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                            {settings.defaultSender && (
                                                <div>
                                                    <div className="text-muted-foreground text-xs mb-1">
                                                        Default Sender
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {(() => {
                                                            const perm = getPermissionLevel(settings.defaultSender);
                                                            return (
                                                                <div
                                                                    className={`flex items-center gap-1 px-2 py-1 rounded text-sm font-medium ${perm.bgColor} ${perm.color}`}
                                                                >
                                                                    <span>{perm.icon}</span>
                                                                    {formatEnumValue(settings.defaultSender)}
                                                                </div>
                                                            );
                                                        })()}
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            ) : (
                                <div className="text-sm text-muted-foreground">No settings loaded.</div>
                            )}
                        </CardContent>
                    </Card>
                </div>
            ) : null}
        </div>
    );
}
