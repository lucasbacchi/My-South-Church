import { redirect } from "react-router";

// TODO: Implement protected loader for admin routes
export function clientLoader() {
    const isAdmin = false; // Replace with actual admin check logic

    if (!isAdmin) {
        return redirect("/signin");
    }

    return null;
}
