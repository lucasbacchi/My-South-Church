import ErrorPage from "./pages/ErrorPage";

export default function CatchAll() {
    return <ErrorPage errorType="not_found" />;
}
