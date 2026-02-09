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
 * Fetches the current user's information including their access level
 */
export async function getCurrentUser(): Promise<CurrentUser | null> {
    try {
        const headers = await getAuthHeaders(true);
        return await apiRequest<CurrentUser>("/currentUser", { method: "GET", headers });
    } catch (error) {
        console.error("Error fetching current user:", error);
        return null;
    }
}
