import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router";
import { Button } from "@/components/ui/button";
import PersonForm from "@/components/admin/PersonForm";
import { personToFormValues } from "@/components/admin/personFormUtils";
import { requireAdminClientLoader } from "@/lib/clientLoaders";
import { createPerson } from "@/lib/people";

// eslint-disable-next-line react-refresh/only-export-components
export const clientLoader = requireAdminClientLoader;

export default function PersonCreatePage() {
    const navigate = useNavigate();
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const initialValues = useMemo(() => personToFormValues({ rolesInput: "" }), []);

    const handleSubmit = async (payload: Parameters<typeof createPerson>[0]) => {
        setIsSaving(true);
        setError(null);
        try {
            await createPerson(payload);
            void navigate("/admin/people");
        } catch (saveError) {
            setError(saveError instanceof Error ? saveError.message : "Failed to create person.");
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold">Create Person</h1>
                    <p className="text-sm text-muted-foreground">Add a new person record and set the initial roles.</p>
                </div>
                <Button asChild variant="outline">
                    <Link to="/admin/people">Back to People</Link>
                </Button>
            </div>

            {error ? (
                <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-destructive">
                    {error}
                </div>
            ) : null}

            <div className="rounded-lg border border-border bg-card p-6 shadow-sm">
                <PersonForm
                    initialValues={initialValues}
                    submitLabel="Create Person"
                    isSubmitting={isSaving}
                    onSubmit={handleSubmit}
                    onCancel={() => void navigate("/admin/people")}
                    disableLastLogin
                    showLastLogin={false}
                />
            </div>
        </div>
    );
}
