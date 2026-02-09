import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { requireAdminClientLoader } from "@/lib/clientLoaders";
import { type Person } from "@/types/people";
import { deletePerson, getPeople } from "@/lib/people";

// eslint-disable-next-line react-refresh/only-export-components
export const clientLoader = requireAdminClientLoader;

type SortDirection = "asc" | "desc";

type SortKey = "name" | "primaryEmail" | "phoneNumber" | "dateOfBirth" | "lastLogin" | "roles";

interface SortState {
    key: SortKey;
    direction: SortDirection;
}

function normalizeText(value: string | null | undefined) {
    return (value ?? "").toLowerCase();
}

function formatDate(value: string | null | undefined) {
    if (!value) return "—";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleDateString();
}

function formatDateTime(value: string | null | undefined) {
    if (!value) return "—";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
}

function personMatches(person: Person, query: string) {
    const tokens = [
        person.firstName,
        person.lastName,
        person.primaryEmail,
        person.secondaryEmail,
        person.phoneNumber,
        person.firebaseUID,
        ...(person.roles ?? []),
    ]
        .filter(Boolean)
        .map((value) => value?.toString() ?? "")
        .join(" ");

    return normalizeText(tokens).includes(query);
}

function compareValues(a: string, b: string, direction: SortDirection) {
    const comparison = a.localeCompare(b);
    return direction === "asc" ? comparison : -comparison;
}

function compareDates(a: string | null, b: string | null, direction: SortDirection) {
    const dateA = a ? new Date(a).getTime() : 0;
    const dateB = b ? new Date(b).getTime() : 0;
    const comparison = dateA - dateB;
    return direction === "asc" ? comparison : -comparison;
}

export default function PeopleListPage() {
    const [people, setPeople] = useState<Person[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [filterQuery, setFilterQuery] = useState("");
    const [sortState, setSortState] = useState<SortState>({ key: "name", direction: "asc" });

    useEffect(() => {
        let isMounted = true;

        getPeople()
            .then((data) => {
                if (!isMounted) return;
                setPeople(data);
                setError(null);
                setIsLoading(false);
            })
            .catch((fetchError) => {
                if (!isMounted) return;
                setError(fetchError instanceof Error ? fetchError.message : "Failed to load people.");
                setIsLoading(false);
            });

        return () => {
            isMounted = false;
        };
    }, []);

    const normalizedQuery = normalizeText(filterQuery.trim());

    const filteredPeople = useMemo(() => {
        if (!normalizedQuery) return people;
        return people.filter((person) => personMatches(person, normalizedQuery));
    }, [people, normalizedQuery]);

    const sortedPeople = useMemo(() => {
        const sorted = [...filteredPeople];
        sorted.sort((a, b) => {
            switch (sortState.key) {
                case "name":
                    return compareValues(
                        `${a.lastName} ${a.firstName}`,
                        `${b.lastName} ${b.firstName}`,
                        sortState.direction
                    );
                case "primaryEmail":
                    return compareValues(a.primaryEmail, b.primaryEmail, sortState.direction);
                case "phoneNumber":
                    return compareValues(a.phoneNumber ?? "", b.phoneNumber ?? "", sortState.direction);
                case "dateOfBirth":
                    return compareDates(a.dateOfBirth, b.dateOfBirth, sortState.direction);
                case "lastLogin":
                    return compareDates(a.lastLogin, b.lastLogin, sortState.direction);
                case "roles":
                    return compareValues((a.roles ?? []).join(","), (b.roles ?? []).join(","), sortState.direction);
                default:
                    return 0;
            }
        });
        return sorted;
    }, [filteredPeople, sortState]);

    const toggleSort = (key: SortKey) => {
        setSortState((prev) => {
            if (prev.key === key) {
                return { key, direction: prev.direction === "asc" ? "desc" : "asc" };
            }
            return { key, direction: "asc" };
        });
    };

    const handleDelete = async (person: Person) => {
        const name = `${person.firstName} ${person.lastName}`.trim();
        const confirmed = window.confirm(`Delete ${name || "this person"}? This cannot be undone.`);
        if (!confirmed) return;

        try {
            await deletePerson(person.id);
            setPeople((prev) => prev.filter((item) => item.id !== person.id));
        } catch (deleteError) {
            setError(deleteError instanceof Error ? deleteError.message : "Failed to delete person.");
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold">People</h1>
                    <p className="text-sm text-muted-foreground">
                        Manage people records, sort columns, and filter by name, email, or role.
                    </p>
                </div>
                <Button asChild>
                    <Link to="/admin/people/new">New Person</Link>
                </Button>
            </div>

            <div className="flex flex-wrap items-center gap-3">
                <div className="w-full max-w-sm">
                    <Input
                        placeholder="Filter people..."
                        value={filterQuery}
                        onChange={(event) => setFilterQuery(event.target.value)}
                    />
                </div>
                <div className="text-sm text-muted-foreground">{filteredPeople.length} result(s)</div>
            </div>

            {error ? (
                <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-destructive">
                    {error}
                </div>
            ) : null}

            <div className="rounded-lg border border-border bg-card shadow-sm">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>
                                <button type="button" className="font-medium" onClick={() => toggleSort("name")}>
                                    Name
                                </button>
                            </TableHead>
                            <TableHead>
                                <button
                                    type="button"
                                    className="font-medium"
                                    onClick={() => toggleSort("primaryEmail")}
                                >
                                    Primary Email
                                </button>
                            </TableHead>
                            <TableHead>
                                <button type="button" className="font-medium" onClick={() => toggleSort("phoneNumber")}>
                                    Phone
                                </button>
                            </TableHead>
                            <TableHead>
                                <button type="button" className="font-medium" onClick={() => toggleSort("dateOfBirth")}>
                                    Date of Birth
                                </button>
                            </TableHead>
                            <TableHead>
                                <button type="button" className="font-medium" onClick={() => toggleSort("lastLogin")}>
                                    Last Login
                                </button>
                            </TableHead>
                            <TableHead>
                                <button type="button" className="font-medium" onClick={() => toggleSort("roles")}>
                                    Roles
                                </button>
                            </TableHead>
                            <TableHead>Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={7} className="py-8 text-center text-muted-foreground">
                                    Loading people...
                                </TableCell>
                            </TableRow>
                        ) : null}
                        {!isLoading && sortedPeople.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={7} className="py-8 text-center text-muted-foreground">
                                    No people found.
                                </TableCell>
                            </TableRow>
                        ) : null}
                        {sortedPeople.map((person) => (
                            <TableRow key={person.id}>
                                <TableCell>
                                    <div className="font-medium">
                                        {person.firstName} {person.lastName}
                                    </div>
                                    <div className="text-xs text-muted-foreground">{person.id}</div>
                                </TableCell>
                                <TableCell>{person.primaryEmail}</TableCell>
                                <TableCell>{person.phoneNumber ?? "—"}</TableCell>
                                <TableCell>{formatDate(person.dateOfBirth)}</TableCell>
                                <TableCell>{formatDateTime(person.lastLogin)}</TableCell>
                                <TableCell>
                                    <div className="flex flex-wrap gap-1">
                                        {person.roles.length > 0 ? (
                                            person.roles.map((role) => (
                                                <Badge key={`${person.id}-${role}`} variant="secondary">
                                                    {role}
                                                </Badge>
                                            ))
                                        ) : (
                                            <span className="text-xs text-muted-foreground">None</span>
                                        )}
                                    </div>
                                </TableCell>
                                <TableCell>
                                    <div className="flex flex-wrap gap-2">
                                        <Button asChild size="sm" variant="outline">
                                            <Link to={`/admin/people/${person.id}`}>Edit</Link>
                                        </Button>
                                        <Button
                                            size="sm"
                                            variant="destructive"
                                            onClick={() => void handleDelete(person)}
                                        >
                                            Delete
                                        </Button>
                                    </div>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>
        </div>
    );
}
