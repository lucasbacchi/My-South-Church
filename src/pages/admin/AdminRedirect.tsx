import { redirect } from "react-router";

export function clientLoader() {
    return redirect("/admin/dashboard");
}

export default function AdminRedirect() {
    return null;
}
