import { redirect } from "react-router";
import { getUser } from "../firebase";
import { getCurrentUser, roles } from "./api";

/* eslint-disable @typescript-eslint/only-throw-error */

/**
 * Client loader for routes that require authentication.
 * Runs in the browser before the route component renders.
 */
export async function requireAuthClientLoader({ request }: { request: Request }) {
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
export async function requireAdminClientLoader({ request }: { request: Request }) {
    const user = await getUser();

    if (!user) {
        const url = new URL(request.url);
        const redirectTo = url.pathname + url.search;
        const searchParams = new URLSearchParams({ redirect: redirectTo });
        throw redirect(`/signin?${searchParams.toString()}`);
    }

    // Check permissions
    const currentUser = await getCurrentUser();

    if (!currentUser?.roles.includes(roles.ADMIN) && !currentUser?.roles.includes(roles.SUPER_ADMIN)) {
        // User is authenticated but doesn't have admin permissions
        // Redirect to unauthorized page
        throw redirect("/unauthorized");
    }

    return { user, currentUser };
}
