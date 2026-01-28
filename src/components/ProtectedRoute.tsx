import { type ReactNode, useContext } from "react";
import { Route } from "react-router";
import { UserContext } from "../contexts/UserContextDefinition";

export default function ProtectedRoute(props: { children: ReactNode }) {
    const user = useContext(UserContext)[0];

    if (!user) {
        return <h1>Unauthorized</h1>;
    }

    return <Route>{props.children}</Route>;
}
