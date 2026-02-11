import { apiRequest, getAuthHeaders } from "./apiClient";

export enum roles {
    SUPER_ADMIN = "SUPER_ADMIN",
    ADMIN = "ADMIN",
    USER = "USER",
}

export interface CurrentUser {
    uid: string;
    email: string | null;
    displayName: string | null;
    photoURL: string | null;
    roles: roles[];
}

/**
 * Fetches the current user's information from the /people/me endpoint.
 */
export async function getCurrentUser(): Promise<CurrentUser | null> {
    try {
        const headers = await getAuthHeaders(true);
        return await apiRequest<CurrentUser>("/people/me", { method: "GET", headers });
    } catch (error) {
        console.error("Error fetching current user:", error);
        return null;
    }
}
