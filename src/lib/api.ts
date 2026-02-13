import { auth } from "../firebase";
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

interface CurrentUserCache {
    uid: string;
    expiresAt: string;
    user: CurrentUser;
}

const CURRENT_USER_CACHE_KEY = "current_user_cache";
const CURRENT_USER_CACHE_TTL_MS = 15 * 60 * 1000; // 15 minutes

export function clearCurrentUserCache() {
    if (typeof window === "undefined") {
        return;
    }

    try {
        localStorage.removeItem(CURRENT_USER_CACHE_KEY);
    } catch (error) {
        console.warn("Failed to clear current user cache:", error);
    }
}

function getCachedCurrentUser(uid: string): CurrentUser | null {
    if (typeof window === "undefined") {
        return null;
    }

    try {
        const raw = localStorage.getItem(CURRENT_USER_CACHE_KEY);
        if (!raw) {
            return null;
        }

        const cached = JSON.parse(raw) as CurrentUserCache;
        if (cached.uid !== uid) {
            return null;
        }

        const expiresAt = Date.parse(cached.expiresAt);
        if (!Number.isFinite(expiresAt) || Date.now() >= expiresAt) {
            return null;
        }

        return cached.user;
    } catch (error) {
        console.warn("Failed to read current user cache:", error);
        return null;
    }
}

function setCachedCurrentUser(uid: string, expiresAt: string, user: CurrentUser) {
    if (typeof window === "undefined") {
        return;
    }

    try {
        const payload: CurrentUserCache = { uid, expiresAt, user };
        localStorage.setItem(CURRENT_USER_CACHE_KEY, JSON.stringify(payload));
    } catch (error) {
        console.warn("Failed to write current user cache:", error);
    }
}

/**
 * Fetches the current user's information from the /people/me endpoint.
 */
export async function getCurrentUser(): Promise<CurrentUser | null> {
    try {
        const firebaseUser = auth.currentUser;
        if (!firebaseUser) {
            return null;
        }

        const cached = getCachedCurrentUser(firebaseUser.uid);
        if (cached) {
            return cached;
        }

        console.log("No valid cache found for current user, fetching from API...");

        const headers = await getAuthHeaders(true);
        const currentUser = await apiRequest<CurrentUser>("/people/me", { method: "GET", headers });

        const tokenResult = await firebaseUser.getIdTokenResult();
        const tokenExpiryMs = tokenResult.expirationTime ? Date.parse(tokenResult.expirationTime) : NaN;
        const ttlExpiryMs = Date.now() + CURRENT_USER_CACHE_TTL_MS;
        const cacheExpiryMs = Number.isFinite(tokenExpiryMs) ? Math.min(tokenExpiryMs, ttlExpiryMs) : ttlExpiryMs;
        setCachedCurrentUser(firebaseUser.uid, new Date(cacheExpiryMs).toISOString(), currentUser);

        return currentUser;
    } catch (error) {
        console.error("Error fetching current user:", error);
        return null;
    }
}
