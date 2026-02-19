import { apiRequest, apiRequestBlob, getAuthHeaders } from "./apiClient";
import { type ImportPeopleResult, type Person, type PersonUpsertInput } from "../types/people";

const PEOPLE_CACHE_TTL_MS = 60_000;

let peopleCache: Person[] | null = null;
let peopleCacheTime = 0;
let peopleInflight: Promise<Person[]> | null = null;

export function peekPeopleCache() {
    return peopleCache;
}

export function clearPeopleCache() {
    peopleCache = null;
    peopleCacheTime = 0;
}

export async function getPeople(options?: { force?: boolean }): Promise<Person[]> {
    const now = Date.now();
    const shouldUseCache = !options?.force && peopleCache !== null && now - peopleCacheTime < PEOPLE_CACHE_TTL_MS;

    if (shouldUseCache && peopleCache !== null) {
        return peopleCache;
    }

    if (peopleInflight) {
        return peopleInflight;
    }

    peopleInflight = (async () => {
        const headers = await getAuthHeaders();
        const data = await apiRequest<Person[]>("/people", { method: "GET", headers });
        peopleCache = data;
        peopleCacheTime = Date.now();
        return data;
    })();

    try {
        return await peopleInflight;
    } finally {
        peopleInflight = null;
    }
}

export async function getPerson(personId: string): Promise<Person> {
    const headers = await getAuthHeaders();
    return apiRequest<Person>(`/people/id/${personId}`, { method: "GET", headers });
}

export async function createPerson(payload: PersonUpsertInput): Promise<Person> {
    const headers = await getAuthHeaders(true);
    const created = await apiRequest<Person>("/people", {
        method: "POST",
        headers,
        body: JSON.stringify(payload),
    });

    if (peopleCache) {
        peopleCache = [...peopleCache, created];
        peopleCacheTime = Date.now();
    }

    return created;
}

export async function updatePerson(personId: string, payload: PersonUpsertInput): Promise<Person> {
    const headers = await getAuthHeaders(true);
    const updated = await apiRequest<Person>(`/people/${personId}`, {
        method: "PUT",
        headers,
        body: JSON.stringify(payload),
    });

    if (peopleCache) {
        peopleCache = peopleCache.map((person) => (person.id === updated.id ? updated : person));
        peopleCacheTime = Date.now();
    }

    return updated;
}

export async function deletePerson(personId: string): Promise<void> {
    const headers = await getAuthHeaders();
    await apiRequest<void>(`/people/${personId}`, { method: "DELETE", headers });

    if (peopleCache) {
        peopleCache = peopleCache.filter((person) => person.id !== personId);
        peopleCacheTime = Date.now();
    }
}

export async function importPeople(records: unknown[]): Promise<ImportPeopleResult> {
    const headers = await getAuthHeaders(true);
    return apiRequest<ImportPeopleResult>("/people/import", {
        method: "POST",
        headers,
        body: JSON.stringify(records),
    });
}

export async function exportPeople(): Promise<Blob> {
    const headers = await getAuthHeaders();
    return apiRequestBlob("/people/export", {
        method: "GET",
        headers,
    });
}

export async function verifyGoogleAccount(personId: string): Promise<Person> {
    const headers = await getAuthHeaders(true);
    const updated = await apiRequest<Person>(`/people/${personId}/verify-google-account`, {
        method: "POST",
        headers,
    });

    if (peopleCache) {
        peopleCache = peopleCache.map((person) => (person.id === updated.id ? updated : person));
        peopleCacheTime = Date.now();
    }

    return updated;
}

export async function verifyAllGoogleAccounts(): Promise<string> {
    const headers = await getAuthHeaders(true);
    return apiRequest<string>("/people/verify-all-google-accounts", {
        method: "POST",
        headers,
    });
}
