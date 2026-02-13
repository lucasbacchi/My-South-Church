import { auth } from "../firebase";

// Use localhost for local development, production URL otherwise
const API_BASE_URL = (() => {
    if (typeof window === "undefined") {
        return "https://api.my.southchurch.com";
    }

    const { hostname } = window.location;
    return hostname === "localhost" || hostname === "127.0.0.1"
        ? "http://localhost:8080"
        : "https://api.my.southchurch.com";
})();

export async function getAuthHeaders(includeContentType = false): Promise<HeadersInit> {
    const user = auth.currentUser;
    if (!user) {
        throw new Error("User is not authenticated");
    }

    const idToken = await user.getIdToken();
    const headers: HeadersInit = {
        Authorization: `Bearer ${idToken}`,
    };

    if (includeContentType) {
        headers["Content-Type"] = "application/json";
    }

    return headers;
}

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${path}`, options);

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Request failed with status ${response.status}`);
    }

    if (response.status === 204) {
        return null as T;
    }

    return (await response.json()) as T;
}
