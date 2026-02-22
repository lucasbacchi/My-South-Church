import { Outlet, useLoaderData } from "react-router";
import { type AdminLoaderData, type ErrorLoaderData, requireAdminClientLoader } from "../../lib/clientLoaders";
import ErrorPage from "../ErrorPage";

// eslint-disable-next-line react-refresh/only-export-components
export const clientLoader = requireAdminClientLoader;

export default function AdminLayout() {
    const data = useLoaderData<AdminLoaderData | ErrorLoaderData>();

    // If loader returned an error, render ErrorPage instead of the admin content
    if ("error" in data && data.error) {
        return <ErrorPage errorType={data.error.type} />;
    }

    return <Outlet />;
}
