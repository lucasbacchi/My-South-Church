import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
    index("./pages/HomePage.tsx"),
    route("about", "./pages/AboutPage.tsx"),
    route("resources", "./pages/ResourcesPage.tsx"),
    route("groups", "./pages/GroupsPage.tsx"),
    route("drive", "./pages/DrivePage.tsx"),
    route("signin", "./pages/SignInPage.tsx"),
    route("account", "./pages/AccountPage.tsx"),
    route("admin", "./pages/admin/AdminLayout.tsx", [
        index("./pages/admin/AdminRedirect.tsx"),
        route("dashboard", "./pages/admin/AdminDashboardPage.tsx"),
        route("people", "./pages/admin/PeopleListPage.tsx"),
        route("people/new", "./pages/admin/PersonCreatePage.tsx"),
        route("people/:personId/view", "./pages/admin/PersonViewPage.tsx"),
        route("people/:personId/edit", "./pages/admin/PersonEditPage.tsx"),
        route("groups", "./pages/admin/GroupListPage.tsx"),
        route("groups/new", "./pages/admin/GroupCreatePage.tsx"),
        route("groups/:groupId/view", "./pages/admin/GroupViewPage.tsx"),
        route("groups/:groupId/edit", "./pages/admin/GroupEditPage.tsx"),
    ]),
    route("*?", "catchall.tsx"),
] satisfies RouteConfig;
