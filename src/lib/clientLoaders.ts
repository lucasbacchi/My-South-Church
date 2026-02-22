import { redirect } from "react-router";
import { getUser } from "../firebase";
import { getCurrentUser, roles } from "./api";
import { getHealthStatus } from "./backendHealth";

/* eslint-disable @typescript-eslint/only-throw-error */

export type ErrorType = "unauthorized" | "service_unavailable" | "not_found";

export interface LoaderError {
    type: ErrorType;
}

export interface AuthLoaderData {
    user: Awaited<ReturnType<typeof getUser>>;
    error?: never;
}

export interface AdminLoaderData {
    user: Awaited<ReturnType<typeof getUser>>;
    currentUser: Awaited<ReturnType<typeof getCurrentUser>>;
    error?: never;
}

export interface ErrorLoaderData {
    error: LoaderError;
    user?: never;
    currentUser?: never;
}

/**
 * Client loader for routes that require authentication.
 * Runs in the browser before the route component renders.
 */
export async function requireAuthClientLoader({ request }: { request: Request }): Promise<AuthLoaderData> {
    const user = await getUser();

    if (!user) {
        const url = new URL(request.url);
        const redirectTo = url.pathname + url.search;
        const searchParams = new URLSearchParams({ redirect: redirectTo });
        throw redirect(`/signin?${searchParams.toString()}`);
    }

    return { user };
}

/**
 * Client loader for routes that require admin access.
 * First checks authentication, then checks permissions.
 */
export async function requireAdminClientLoader({
    request,
}: {
    request: Request;
}): Promise<AdminLoaderData | ErrorLoaderData> {
    const url = new URL(request.url);
    const originalPath = url.pathname + url.search;

    // Check if backend is down before proceeding
    const healthStatus = getHealthStatus();
    if (healthStatus === "down") {
        return {
            error: {
                type: "service_unavailable",
            },
        };
    }

    const user = await getUser();

    if (!user) {
        const searchParams = new URLSearchParams({ redirect: originalPath });
        throw redirect(`/signin?${searchParams.toString()}`);
    }

    // Check permissions
    try {
        const currentUser = await getCurrentUser();

        if (!currentUser?.roles.includes(roles.ADMIN) && !currentUser?.roles.includes(roles.SUPER_ADMIN)) {
            // User is authenticated but doesn't have admin permissions
            return {
                error: {
                    type: "unauthorized",
                },
            };
        }

        return { user, currentUser };
    } catch {
        // If getCurrentUser fails due to backend being down, show service unavailable
        return {
            error: {
                type: "service_unavailable",
            },
        };
    }
}
