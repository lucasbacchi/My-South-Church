import { redirect } from "react-router";

// eslint-disable-next-line react-refresh/only-export-components
export function clientLoader() {
    return redirect("/admin/dashboard");
}

export default function AdminRedirect() {
    return null;
}
