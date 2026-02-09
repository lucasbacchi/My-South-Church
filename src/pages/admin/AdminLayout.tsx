import { Outlet } from "react-router";
import { requireAdminClientLoader } from "../../lib/clientLoaders";

// eslint-disable-next-line react-refresh/only-export-components
export const clientLoader = requireAdminClientLoader;

export default function AdminLayout() {
    return <Outlet />;
}
