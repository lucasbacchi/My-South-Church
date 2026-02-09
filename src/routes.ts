import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
    index("./pages/HomePage.tsx"),
    route("about", "./pages/AboutPage.tsx"),
    route("groups", "./pages/GroupsPage.tsx"),
    route("drive", "./pages/DrivePage.tsx"),
    route("signin", "./pages/SignInPage.tsx"),
    route("account", "./pages/AccountPage.tsx"),
    route("unauthorized", "./pages/UnauthorizedPage.tsx"),
    route("admin", "./pages/admin/AdminLayout.tsx", [
        route("dashboard", "./pages/admin/AdminDashboardPage.tsx"),
        route("people", "./pages/admin/PeopleListPage.tsx"),
        route("people/new", "./pages/admin/PersonCreatePage.tsx"),
        route("people/:personId", "./pages/admin/PersonEditPage.tsx"),
    ]),
    route("*?", "catchall.tsx"),
] satisfies RouteConfig;
