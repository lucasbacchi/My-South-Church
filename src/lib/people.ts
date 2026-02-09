import { apiRequest, getAuthHeaders } from "./apiClient";
import { type Person, type PersonInput } from "../types/people";

export async function getPeople(): Promise<Person[]> {
    const headers = await getAuthHeaders();
    return apiRequest<Person[]>("/people", { method: "GET", headers });
}

export async function getPerson(personId: string): Promise<Person> {
    const headers = await getAuthHeaders();
    return apiRequest<Person>(`/people/${personId}`, { method: "GET", headers });
}

export async function createPerson(payload: PersonInput): Promise<Person> {
    const headers = await getAuthHeaders(true);
    return apiRequest<Person>("/people", {
        method: "POST",
        headers,
        body: JSON.stringify(payload),
    });
}

export async function updatePerson(personId: string, payload: PersonInput): Promise<Person> {
    const headers = await getAuthHeaders(true);
    return apiRequest<Person>(`/people/${personId}`, {
        method: "PUT",
        headers,
        body: JSON.stringify(payload),
    });
}

export async function deletePerson(personId: string): Promise<void> {
    const headers = await getAuthHeaders();
    await apiRequest<void>(`/people/${personId}`, { method: "DELETE", headers });
}
