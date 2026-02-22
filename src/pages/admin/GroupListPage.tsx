import { type ChangeEvent, useEffect, useMemo, useState } from "react";
import { Link } from "react-router";
import { ArrowLeft, ChevronDown, ChevronUp, Pencil, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import {
    Pagination,
    PaginationContent,
    PaginationEllipsis,
    PaginationItem,
    PaginationLink,
    PaginationNext,
    PaginationPrevious,
} from "@/components/ui/pagination";
import { requireAdminClientLoader } from "@/lib/clientLoaders";
import { type Group, getGroups, peekGroupsCache } from "@/lib/groups";

// eslint-disable-next-line react-refresh/only-export-components
export const clientLoader = requireAdminClientLoader;

type SortDirection = "asc" | "desc";
type SortKey = "name" | "email" | "description";

interface SortState {
    key: SortKey;
    direction: SortDirection;
}

function normalizeText(value: string | null | undefined) {
    return (value ?? "").toLowerCase();
}

function groupMatches(group: Group, query: string) {
    const tokens = [group.name, group.email, group.description]
        .filter(Boolean)
        .map((value) => value?.toString() ?? "")
        .join(" ");
    return normalizeText(tokens).includes(query);
}

function compareValues(a: string, b: string, direction: SortDirection) {
    const comparison = a.localeCompare(b);
    return direction === "asc" ? comparison : -comparison;
}

export default function GroupListPage() {
    const [groups, setGroups] = useState<Group[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [filterQuery, setFilterQuery] = useState("");
    const [sortState, setSortState] = useState<SortState>({ key: "name", direction: "asc" });
    const [pageIndex, setPageIndex] = useState(1);

    useEffect(() => {
        let isMounted = true;
        const cachedGroups = peekGroupsCache();
        const hasCache = cachedGroups && cachedGroups.length > 0;

        if (hasCache) {
            setGroups(cachedGroups);
            setIsLoading(false);
        }

        const loadGroups = async () => {
            try {
                const data = await getGroups({ force: Boolean(hasCache) });
                if (!isMounted) return;
                setGroups(data);
                setError(null);
            } catch (fetchError) {
                if (!isMounted) return;
                if (!hasCache) {
                    setError(fetchError instanceof Error ? fetchError.message : "Failed to load groups.");
                }
            } finally {
                if (isMounted) {
                    setIsLoading(false);
                }
            }
        };

        void loadGroups();

        return () => {
            isMounted = false;
        };
    }, []);

    const normalizedQuery = normalizeText(filterQuery.trim());

    const filteredGroups = useMemo(() => {
        if (!normalizedQuery) return groups;
        return groups.filter((group) => groupMatches(group, normalizedQuery));
    }, [groups, normalizedQuery]);

    const sortedGroups = useMemo(() => {
        const sorted = [...filteredGroups];
        sorted.sort((a, b) => {
            switch (sortState.key) {
                case "name":
                    return compareValues(a.name ?? "", b.name ?? "", sortState.direction);
                case "email":
                    return compareValues(a.email ?? "", b.email ?? "", sortState.direction);
                case "description":
                    return compareValues(a.description ?? "", b.description ?? "", sortState.direction);
                default:
                    return 0;
            }
        });
        return sorted;
    }, [filteredGroups, sortState]);

    const totalPages = Math.max(1, Math.ceil(sortedGroups.length / 25));
    const pageStart = sortedGroups.length === 0 ? 0 : (pageIndex - 1) * 25 + 1;
    const pageEnd = Math.min(pageIndex * 25, sortedGroups.length);

    const pagedGroups = useMemo(() => {
        const start = (pageIndex - 1) * 25;
        return sortedGroups.slice(start, start + 25);
    }, [sortedGroups, pageIndex]);

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

    const SortHeader = ({ label, sortKey }: { label: string; sortKey: SortKey }) => (
        <TableHead>
            <button
                type="button"
                className="font-medium shadow-none hover:text-foreground cursor-pointer bg-transparent border-none p-0 text-inherit focus:outline-none flex items-center gap-1"
                onClick={() => toggleSort(sortKey)}
            >
                {label}
                {sortState.key === sortKey &&
                    (sortState.direction === "asc" ? (
                        <ChevronUp className="size-4" />
                    ) : (
                        <ChevronDown className="size-4" />
                    ))}
            </button>
        </TableHead>
    );

    return (
        <div className="flex flex-col gap-6 p-6">
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
                    <h1 className="text-2xl font-bold">Groups</h1>
                    <p className="text-sm text-muted-foreground">Manage groups, members, and settings.</p>
                </div>
                <div className="flex flex-wrap gap-2">
                    <Link to="/admin/groups/new">
                        <Button>
                            <Plus className="mr-2 h-4 w-4" />
                            New Group
                        </Button>
                    </Link>
                </div>
            </div>

            {error ? <div className="rounded-md bg-red-50 p-4 text-sm text-red-700">{error}</div> : null}

            <div className="flex items-center gap-4">
                <Input
                    placeholder="Search by name, email, or description..."
                    value={filterQuery}
                    onChange={(e: ChangeEvent<HTMLInputElement>) => setFilterQuery(e.target.value)}
                    className="w-full max-w-md"
                />
                <div className="text-sm text-gray-600">
                    {pagedGroups.length === 0 && sortedGroups.length === 0
                        ? "No groups"
                        : `${pageStart}–${pageEnd} of ${sortedGroups.length}`}
                </div>
            </div>

            {isLoading ? (
                <div className="rounded-md bg-blue-50 p-4 text-sm text-blue-700">Loading groups...</div>
            ) : (
                <>
                    <div className="overflow-x-auto rounded-md border">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <SortHeader label="Name" sortKey="name" />
                                    <SortHeader label="Email" sortKey="email" />
                                    <SortHeader label="Description" sortKey="description" />
                                    <TableHead
                                        className="sticky right-0 z-10 bg-background text-right"
                                        style={{ filter: "drop-shadow(-4px 0 4px rgba(0, 0, 0, 0.1))" }}
                                    >
                                        Actions
                                    </TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {pagedGroups.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={4} className="py-8 text-center text-muted-foreground">
                                            {sortedGroups.length === 0 ? "No groups found." : "No groups on this page."}
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    pagedGroups.map((group) => (
                                        <TableRow key={group.id}>
                                            <TableCell>
                                                <Link
                                                    to={`/admin/groups/${group.id}/view`}
                                                    className="font-medium hover:underline"
                                                >
                                                    {group.name ?? "—"}
                                                </Link>
                                            </TableCell>
                                            <TableCell>{group.email ?? "—"}</TableCell>
                                            <TableCell className="max-w-xs truncate">
                                                {group.description ?? "—"}
                                            </TableCell>
                                            <TableCell
                                                className="sticky right-0 z-10 bg-background text-right"
                                                style={{ filter: "drop-shadow(-4px 0 4px rgba(0, 0, 0, 0.1))" }}
                                            >
                                                <div className="flex justify-end gap-2">
                                                    <Link to={`/admin/groups/${group.id}/edit`}>
                                                        <Button variant="outline" size="sm">
                                                            <Pencil className="h-4 w-4" />
                                                        </Button>
                                                    </Link>
                                                </div>
                                            </TableCell>
                                        </TableRow>
                                    ))
                                )}
                            </TableBody>
                        </Table>
                    </div>

                    {totalPages > 1 && (
                        <div className="flex justify-center">
                            <Pagination>
                                <PaginationContent>
                                    {pageIndex > 1 && (
                                        <PaginationItem>
                                            <PaginationPrevious
                                                href="#"
                                                onClick={(e: React.MouseEvent) => {
                                                    e.preventDefault();
                                                    setPageIndex(pageIndex - 1);
                                                }}
                                            />
                                        </PaginationItem>
                                    )}
                                    {Array.from({ length: Math.min(5, totalPages) }).map((_, i) => {
                                        const pageNum = i + 1;
                                        return (
                                            <PaginationItem key={pageNum}>
                                                <PaginationLink
                                                    href="#"
                                                    isActive={pageNum === pageIndex}
                                                    onClick={(e: React.MouseEvent) => {
                                                        e.preventDefault();
                                                        setPageIndex(pageNum);
                                                    }}
                                                >
                                                    {pageNum}
                                                </PaginationLink>
                                            </PaginationItem>
                                        );
                                    })}
                                    {totalPages > 5 && pageIndex < totalPages - 2 && (
                                        <PaginationItem>
                                            <PaginationEllipsis />
                                        </PaginationItem>
                                    )}
                                    {pageIndex < totalPages && (
                                        <PaginationItem>
                                            <PaginationNext
                                                href="#"
                                                onClick={(e: React.MouseEvent) => {
                                                    e.preventDefault();
                                                    setPageIndex(pageIndex + 1);
                                                }}
                                            />
                                        </PaginationItem>
                                    )}
                                </PaginationContent>
                            </Pagination>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}
