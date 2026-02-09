import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router";
import { Button } from "@/components/ui/button";
import PersonForm from "@/components/admin/PersonForm";
import { type PersonFormValues, personToFormValues } from "@/components/admin/personFormUtils";
import { requireAdminClientLoader } from "@/lib/clientLoaders";
import { type Person } from "@/types/people";
import { getPerson, updatePerson } from "@/lib/people";

// eslint-disable-next-line react-refresh/only-export-components
export const clientLoader = requireAdminClientLoader;

function toFormValues(person?: Person | null): PersonFormValues {
    if (!person) {
        return personToFormValues({ rolesInput: "" });
    }

    return personToFormValues({
        id: person.id,
        firstName: person.firstName,
        lastName: person.lastName,
        primaryEmail: person.primaryEmail,
        secondaryEmail: person.secondaryEmail ?? "",
        phoneNumber: person.phoneNumber ?? "",
        dateOfBirth: person.dateOfBirth ?? "",
        firebaseUID: person.firebaseUID ?? "",
        lastLogin: person.lastLogin ?? "",
        rolesInput: person.roles.join(", "),
    });
}

export default function PersonEditPage() {
    const { personId } = useParams();
    const navigate = useNavigate();
    const [person, setPerson] = useState<Person | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!personId) {
            setError("Missing person ID.");
            setIsLoading(false);
            return;
        }

        let isMounted = true;
        setIsLoading(true);
        setError(null);

        getPerson(personId)
            .then((data) => {
                if (!isMounted) return;
                setPerson(data);
            })
            .catch((fetchError) => {
                if (!isMounted) return;
                setError(fetchError instanceof Error ? fetchError.message : "Failed to load person.");
            })
            .finally(() => {
                if (!isMounted) return;
                setIsLoading(false);
            });

        return () => {
            isMounted = false;
        };
    }, [personId]);

    const initialValues = useMemo(() => toFormValues(person), [person]);

    const handleSubmit = async (payload: Parameters<typeof updatePerson>[1]) => {
        if (!personId) return;
        setIsSaving(true);
        setError(null);
        try {
            const updated = await updatePerson(personId, payload);
            setPerson(updated);
            void navigate("/admin/people");
        } catch (saveError) {
            setError(saveError instanceof Error ? saveError.message : "Failed to update person.");
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold">Edit Person</h1>
                    <p className="text-sm text-muted-foreground">Update contact details, roles, and identity fields.</p>
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

            {isLoading ? (
                <div className="rounded-lg border border-border bg-card p-6 text-muted-foreground">
                    Loading person details...
                </div>
            ) : (
                <div className="rounded-lg border border-border bg-card p-6 shadow-sm">
                    <PersonForm
                        initialValues={initialValues}
                        submitLabel="Save Changes"
                        isSubmitting={isSaving}
                        onSubmit={handleSubmit}
                        onCancel={() => void navigate("/admin/people")}
                        showId
                    />
                </div>
            )}
        </div>
    );
}
