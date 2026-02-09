import { auth } from "../firebase";

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

// Use localhost for local development, production URL otherwise
const API_BASE_URL =
    window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1"
        ? "http://localhost:8080"
        : "https://api.my.southchurch.com";

/**
 * Fetches the current user's information including their access level
 */
export async function getCurrentUser(): Promise<CurrentUser | null> {
    const user = auth.currentUser;
    if (!user) {
        return null;
    }

    try {
        const idToken = await user.getIdToken();

        const response = await fetch(`${API_BASE_URL}/currentUser`, {
            method: "GET",
            headers: {
                Authorization: `Bearer ${idToken}`,
                "Content-Type": "application/json",
            },
        });

        if (!response.ok) {
            console.error("Failed to fetch current user:", response.statusText);
            return null;
        }

        const data = (await response.json()) as CurrentUser;
        return data;
    } catch (error) {
        console.error("Error fetching current user:", error);
        return null;
    }
}
