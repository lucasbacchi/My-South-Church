import { useEffect, useMemo, useRef, useState, type ChangeEvent } from "react";
import { Link } from "react-router";
import { ArrowLeft, ChevronDown, ChevronUp } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import {
    Pagination,
    PaginationContent,
    PaginationEllipsis,
    PaginationItem,
    PaginationLink,
    PaginationNext,
    PaginationPrevious,
} from "@/components/ui/pagination";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { requireAdminClientLoader } from "@/lib/clientLoaders";
import { type ImportPeopleResult, type Person } from "@/types/people";
import { clearPeopleCache, deletePerson, exportPeople, getPeople, importPeople, peekPeopleCache } from "@/lib/people";

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
    // Parse date string as local date to avoid timezone conversion
    // Input format: "YYYY-MM-DD"
    const parts = value.split("-");
    if (parts.length !== 3) return value;
    const [year, month, day] = parts.map(Number);
    const date = new Date(year, month - 1, day); // month is 0-indexed
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
    // Parse dates as local dates to avoid timezone issues
    // For ISO date strings like "2000-01-15", parse as local date
    const parseDate = (dateStr: string | null) => {
        if (!dateStr) return 0;
        const parts = dateStr.split(/[-T:]/);
        if (parts.length >= 3) {
            // Create date in local timezone: [year, month-1, day, hour, minute, second]
            return new Date(
                parseInt(parts[0]),
                parseInt(parts[1]) - 1,
                parseInt(parts[2]),
                parseInt(parts[3] || "0"),
                parseInt(parts[4] || "0"),
                parseInt(parts[5] || "0")
            ).getTime();
        }
        return new Date(dateStr).getTime();
    };

    const dateA = parseDate(a);
    const dateB = parseDate(b);
    const comparison = dateA - dateB;
    return direction === "asc" ? comparison : -comparison;
}

export default function PeopleListPage() {
    const [people, setPeople] = useState<Person[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [filterQuery, setFilterQuery] = useState("");
    const [sortState, setSortState] = useState<SortState>({ key: "name", direction: "asc" });
    const [personToDelete, setPersonToDelete] = useState<Person | null>(null);
    const [isDeleting, setIsDeleting] = useState(false);
    const [isImporting, setIsImporting] = useState(false);
    const [isExporting, setIsExporting] = useState(false);
    const [importError, setImportError] = useState<string | null>(null);
    const [exportError, setExportError] = useState<string | null>(null);
    const [importResult, setImportResult] = useState<ImportPeopleResult | null>(null);
    const fileInputRef = useRef<HTMLInputElement | null>(null);
    const [pageIndex, setPageIndex] = useState(1);
    const [pageSize, setPageSize] = useState(25);

    useEffect(() => {
        let isMounted = true;
        const cachedPeople = peekPeopleCache();
        const hasCache = cachedPeople && cachedPeople.length > 0;

        if (hasCache) {
            setPeople(cachedPeople);
            setIsLoading(false);
        }

        const loadPeople = async () => {
            try {
                const data = await getPeople({ force: Boolean(hasCache) });
                if (!isMounted) return;
                setPeople(data);
                setError(null);
            } catch (fetchError) {
                if (!isMounted) return;
                if (!hasCache) {
                    setError(fetchError instanceof Error ? fetchError.message : "Failed to load people.");
                }
            } finally {
                if (isMounted) {
                    setIsLoading(false);
                }
            }
        };

        void loadPeople();

        return () => {
            isMounted = false;
        };
    }, []);

    const normalizedQuery = normalizeText(filterQuery.trim());
    const skippedExistingEmail = importResult?.skippedExistingEmail ?? 0;
    const skippedDuplicateEmail = importResult?.skippedDuplicateEmail ?? 0;
    const invalidMissingAttributes = importResult?.invalidMissingAttributes ?? 0;
    const invalidMissingRequired = importResult?.invalidMissingRequired ?? 0;
    const invalidMissingEmail = importResult?.invalidMissingEmail ?? 0;
    const invalidMissingFirstName = importResult?.invalidMissingFirstName ?? 0;
    const invalidMissingLastName = importResult?.invalidMissingLastName ?? 0;

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

    const totalPages = Math.max(1, Math.ceil(sortedPeople.length / pageSize));
    const pageStart = sortedPeople.length === 0 ? 0 : (pageIndex - 1) * pageSize + 1;
    const pageEnd = Math.min(pageIndex * pageSize, sortedPeople.length);

    const pagedPeople = useMemo(() => {
        const start = (pageIndex - 1) * pageSize;
        return sortedPeople.slice(start, start + pageSize);
    }, [sortedPeople, pageIndex, pageSize]);

    useEffect(() => {
        setPageIndex(1);
    }, [normalizedQuery, sortState.key, sortState.direction]);

    useEffect(() => {
        if (pageIndex > totalPages) {
            setPageIndex(totalPages);
        }
    }, [pageIndex, totalPages]);

    const toggleSort = (key: SortKey) => {
        setSortState((prev) => {
            if (prev.key === key) {
                return { key, direction: prev.direction === "asc" ? "desc" : "asc" };
            }
            return { key, direction: "asc" };
        });
    };

    const handleDeleteClick = (person: Person) => {
        setPersonToDelete(person);
    };

    const handleConfirmDelete = async () => {
        if (!personToDelete) return;

        setIsDeleting(true);
        try {
            await deletePerson(personToDelete.id);
            setPeople((prev) => prev.filter((item) => item.id !== personToDelete.id));
            setPersonToDelete(null);
        } catch (deleteError) {
            setError(deleteError instanceof Error ? deleteError.message : "Failed to delete person.");
        } finally {
            setIsDeleting(false);
        }
    };

    const handleCancelDelete = () => {
        setPersonToDelete(null);
    };

    const handleImportClick = () => {
        fileInputRef.current?.click();
    };

    const handleExportClick = async () => {
        setIsExporting(true);
        setExportError(null);

        try {
            const blob = await exportPeople();
            const url = URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            link.download = `people-export-${new Date().toISOString().slice(0, 10)}.json`;
            document.body.appendChild(link);
            link.click();
            link.remove();
            URL.revokeObjectURL(url);
        } catch (exportError) {
            setExportError(exportError instanceof Error ? exportError.message : "Failed to export people.");
        } finally {
            setIsExporting(false);
        }
    };

    const handleImportFileChange = async (event: ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (!file) return;

        setIsImporting(true);
        setImportError(null);
        setImportResult(null);

        try {
            const text = await file.text();
            const parsed = JSON.parse(text);
            const records = Array.isArray(parsed) ? parsed : parsed?.data;

            if (!Array.isArray(records)) {
                throw new Error("Expected a JSON array of records.");
            }

            const result = await importPeople(records);
            setImportResult(result);
            clearPeopleCache();
            const refreshed = await getPeople({ force: true });
            setPeople(refreshed);
        } catch (importError) {
            setImportError(importError instanceof Error ? importError.message : "Failed to import people.");
        } finally {
            setIsImporting(false);
            event.target.value = "";
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                    <div className="flex items-center gap-2 mb-2">
                        <Button variant="ghost" size="sm" asChild className="hover:bg-primary mb-4">
                            <Link to="/admin/dashboard" className="flex items-center gap-1">
                                <ArrowLeft className="size-4" />
                                Back to Dashboard
                            </Link>
                        </Button>
                    </div>
                    <h1 className="text-2xl font-bold">People</h1>
                    <p className="text-sm text-muted-foreground">
                        Manage people records, sort columns, and filter by name, email, or role.
                    </p>
                </div>
                <div className="flex flex-wrap gap-2">
                    <Button type="button" variant="outline" onClick={handleExportClick} disabled={isExporting}>
                        {isExporting ? "Exporting..." : "Export JSON"}
                    </Button>
                    <Button type="button" variant="outline" onClick={handleImportClick} disabled={isImporting}>
                        {isImporting ? "Importing..." : "Import JSON"}
                    </Button>
                    <input
                        ref={fileInputRef}
                        type="file"
                        accept="application/json"
                        className="hidden"
                        onChange={handleImportFileChange}
                    />
                    <Button asChild>
                        <Link to="/admin/people/new">New Person</Link>
                    </Button>
                </div>
            </div>

            <div className="flex flex-wrap items-center gap-3">
                <div className="w-full max-w-sm">
                    <Input
                        placeholder="Filter people..."
                        value={filterQuery}
                        onChange={(event) => setFilterQuery(event.target.value)}
                    />
                </div>
                <div className="text-sm text-muted-foreground">
                    Showing {pageStart} - {pageEnd} of {sortedPeople.length} result(s)
                </div>
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <span>Rows per page</span>
                    <select
                        className="h-9 rounded-md border border-border bg-transparent px-2"
                        value={pageSize}
                        onChange={(event) => setPageSize(Number(event.target.value))}
                    >
                        <option value={25}>25</option>
                        <option value={50}>50</option>
                        <option value={100}>100</option>
                    </select>
                </div>
            </div>

            {error ? (
                <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-destructive">
                    {error}
                </div>
            ) : null}

            {importError ? (
                <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-destructive">
                    {importError}
                </div>
            ) : null}

            {exportError ? (
                <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-destructive">
                    {exportError}
                </div>
            ) : null}

            {importResult ? (
                <div className="rounded-lg border border-border bg-card p-4 text-sm text-muted-foreground">
                    Imported {importResult.total} record(s): {importResult.created} created, {importResult.updated ?? 0}{" "}
                    updated, {importResult.skipped} skipped, {importResult.invalid} invalid.
                    {skippedExistingEmail || skippedDuplicateEmail ? (
                        <div className="mt-2 text-xs text-muted-foreground">
                            Skipped details: {skippedExistingEmail} existing email(s), {skippedDuplicateEmail} duplicate
                            email(s) in file.
                        </div>
                    ) : null}
                    {invalidMissingAttributes || invalidMissingRequired ? (
                        <div className="mt-1 text-xs text-muted-foreground">
                            Invalid details: {invalidMissingEmail} missing email, {invalidMissingFirstName} missing
                            first name, {invalidMissingLastName} missing last name, {invalidMissingAttributes} missing
                            attributes.
                        </div>
                    ) : null}
                </div>
            ) : null}

            <div className="rounded-lg border border-border bg-card shadow-sm">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>
                                <button
                                    type="button"
                                    className="font-medium shadow-none hover:text-foreground cursor-pointer bg-transparent border-none p-0 text-inherit focus:outline-none flex items-center gap-1"
                                    onClick={() => toggleSort("name")}
                                >
                                    Name
                                    {sortState.key === "name" &&
                                        (sortState.direction === "asc" ? (
                                            <ChevronUp className="size-4" />
                                        ) : (
                                            <ChevronDown className="size-4" />
                                        ))}
                                </button>
                            </TableHead>
                            <TableHead>
                                <button
                                    type="button"
                                    className="font-medium shadow-none hover:text-foreground cursor-pointer bg-transparent border-none p-0 text-inherit focus:outline-none flex items-center gap-1"
                                    onClick={() => toggleSort("primaryEmail")}
                                >
                                    Primary Email
                                    {sortState.key === "primaryEmail" &&
                                        (sortState.direction === "asc" ? (
                                            <ChevronUp className="size-4" />
                                        ) : (
                                            <ChevronDown className="size-4" />
                                        ))}
                                </button>
                            </TableHead>
                            <TableHead>
                                <button
                                    type="button"
                                    className="font-medium shadow-none hover:text-foreground cursor-pointer bg-transparent border-none p-0 text-inherit focus:outline-none flex items-center gap-1"
                                    onClick={() => toggleSort("phoneNumber")}
                                >
                                    Phone
                                    {sortState.key === "phoneNumber" &&
                                        (sortState.direction === "asc" ? (
                                            <ChevronUp className="size-4" />
                                        ) : (
                                            <ChevronDown className="size-4" />
                                        ))}
                                </button>
                            </TableHead>
                            <TableHead>
                                <button
                                    type="button"
                                    className="font-medium shadow-none hover:text-foreground cursor-pointer bg-transparent border-none p-0 text-inherit focus:outline-none flex items-center gap-1"
                                    onClick={() => toggleSort("dateOfBirth")}
                                >
                                    Date of Birth
                                    {sortState.key === "dateOfBirth" &&
                                        (sortState.direction === "asc" ? (
                                            <ChevronUp className="size-4" />
                                        ) : (
                                            <ChevronDown className="size-4" />
                                        ))}
                                </button>
                            </TableHead>
                            <TableHead>
                                <button
                                    type="button"
                                    className="font-medium shadow-none hover:text-foreground cursor-pointer bg-transparent border-none p-0 text-inherit focus:outline-none flex items-center gap-1"
                                    onClick={() => toggleSort("lastLogin")}
                                >
                                    Last Login
                                    {sortState.key === "lastLogin" &&
                                        (sortState.direction === "asc" ? (
                                            <ChevronUp className="size-4" />
                                        ) : (
                                            <ChevronDown className="size-4" />
                                        ))}
                                </button>
                            </TableHead>
                            <TableHead>
                                <button
                                    type="button"
                                    className="font-medium shadow-none hover:text-foreground cursor-pointer bg-transparent border-none p-0 text-inherit focus:outline-none flex items-center gap-1"
                                    onClick={() => toggleSort("roles")}
                                >
                                    Roles
                                    {sortState.key === "roles" &&
                                        (sortState.direction === "asc" ? (
                                            <ChevronUp className="size-4" />
                                        ) : (
                                            <ChevronDown className="size-4" />
                                        ))}
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
                        {pagedPeople.map((person) => (
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
                                            onClick={() => handleDeleteClick(person)}
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

            {sortedPeople.length > pageSize ? (
                <Pagination>
                    <PaginationContent>
                        <PaginationItem>
                            <PaginationPrevious
                                href="#"
                                onClick={(event) => {
                                    event.preventDefault();
                                    setPageIndex((prev) => Math.max(1, prev - 1));
                                }}
                            />
                        </PaginationItem>
                        {Array.from({ length: totalPages }, (_, index) => index + 1)
                            .filter((page) =>
                                totalPages <= 7
                                    ? true
                                    : page === 1 || page === totalPages || Math.abs(page - pageIndex) <= 1
                            )
                            .reduce<(number | "ellipsis")[]>((acc, page) => {
                                const prev = acc[acc.length - 1];
                                if (typeof prev === "number" && page - prev > 1) {
                                    acc.push("ellipsis");
                                }
                                acc.push(page);
                                return acc;
                            }, [])
                            .map((page, index) =>
                                page === "ellipsis" ? (
                                    <PaginationItem key={`ellipsis-${index}`}>
                                        <PaginationEllipsis />
                                    </PaginationItem>
                                ) : (
                                    <PaginationItem key={page}>
                                        <PaginationLink
                                            href="#"
                                            isActive={pageIndex === page}
                                            onClick={(event) => {
                                                event.preventDefault();
                                                setPageIndex(page);
                                            }}
                                        >
                                            {page}
                                        </PaginationLink>
                                    </PaginationItem>
                                )
                            )}
                        <PaginationItem>
                            <PaginationNext
                                href="#"
                                onClick={(event) => {
                                    event.preventDefault();
                                    setPageIndex((prev) => Math.min(totalPages, prev + 1));
                                }}
                            />
                        </PaginationItem>
                    </PaginationContent>
                </Pagination>
            ) : null}

            <AlertDialog open={personToDelete !== null} onOpenChange={handleCancelDelete}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Delete Person</AlertDialogTitle>
                        <AlertDialogDescription>
                            Are you sure you want to delete{" "}
                            <span className="font-semibold">
                                {personToDelete && `${personToDelete.firstName} ${personToDelete.lastName}`.trim()}
                            </span>
                            ? This action cannot be undone.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel disabled={isDeleting}>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                            variant="destructive"
                            disabled={isDeleting}
                            onClick={() => void handleConfirmDelete()}
                        >
                            {isDeleting ? "Deleting..." : "Delete"}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
