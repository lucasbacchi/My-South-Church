import { useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import { type PersonInput } from "@/types/people";
import { type PersonFormValues } from "@/components/admin/personFormUtils";

interface PersonFormProps {
    initialValues: PersonFormValues;
    submitLabel: string;
    isSubmitting?: boolean;
    onSubmit: (payload: PersonInput) => void | Promise<void>;
    onCancel?: () => void;
    showId?: boolean;
    disableLastLogin?: boolean;
}

const emptyString = "";

function parseRolesInput(value: string) {
    const roles = value
        .split(",")
        .map((role) => role.trim())
        .filter((role) => role.length > 0);

    return Array.from(new Set(roles));
}

function normalizeOptional(value: string) {
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : null;
}

export default function PersonForm({
    initialValues,
    submitLabel,
    isSubmitting = false,
    onSubmit,
    onCancel,
    showId = false,
    disableLastLogin = true,
}: PersonFormProps) {
    const [values, setValues] = useState<PersonFormValues>(initialValues);

    useEffect(() => {
        setValues(initialValues);
    }, [initialValues]);

    const parsedRoles = useMemo(() => parseRolesInput(values.rolesInput), [values.rolesInput]);

    const onChange = (field: keyof PersonFormValues) => (event: React.ChangeEvent<HTMLInputElement>) => {
        setValues((prev) => ({ ...prev, [field]: event.target.value }));
    };

    const onChangeTextarea = (field: keyof PersonFormValues) => (event: React.ChangeEvent<HTMLTextAreaElement>) => {
        setValues((prev) => ({ ...prev, [field]: event.target.value }));
    };

    const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const payload: PersonInput = {
            firstName: values.firstName.trim(),
            lastName: values.lastName.trim(),
            primaryEmail: values.primaryEmail.trim(),
            secondaryEmail: normalizeOptional(values.secondaryEmail),
            phoneNumber: normalizeOptional(values.phoneNumber),
            dateOfBirth: normalizeOptional(values.dateOfBirth),
            firebaseUID: normalizeOptional(values.firebaseUID),
            lastLogin: normalizeOptional(values.lastLogin),
            roles: parsedRoles,
        };

        void onSubmit(payload);
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid gap-4 md:grid-cols-2">
                {showId ? (
                    <div className="space-y-2 md:col-span-2">
                        <Label htmlFor="person-id">Person ID</Label>
                        <Input id="person-id" value={values.id ?? emptyString} readOnly />
                    </div>
                ) : null}
                <div className="space-y-2">
                    <Label htmlFor="first-name">First Name</Label>
                    <Input id="first-name" value={values.firstName} onChange={onChange("firstName")} required />
                </div>
                <div className="space-y-2">
                    <Label htmlFor="last-name">Last Name</Label>
                    <Input id="last-name" value={values.lastName} onChange={onChange("lastName")} required />
                </div>
                <div className="space-y-2">
                    <Label htmlFor="primary-email">Primary Email</Label>
                    <Input
                        id="primary-email"
                        type="email"
                        value={values.primaryEmail}
                        onChange={onChange("primaryEmail")}
                        required
                    />
                </div>
                <div className="space-y-2">
                    <Label htmlFor="secondary-email">Secondary Email</Label>
                    <Input
                        id="secondary-email"
                        type="email"
                        value={values.secondaryEmail}
                        onChange={onChange("secondaryEmail")}
                    />
                </div>
                <div className="space-y-2">
                    <Label htmlFor="phone">Phone Number</Label>
                    <Input id="phone" type="tel" value={values.phoneNumber} onChange={onChange("phoneNumber")} />
                </div>
                <div className="space-y-2">
                    <Label htmlFor="dob">Date of Birth</Label>
                    <Input id="dob" type="date" value={values.dateOfBirth} onChange={onChange("dateOfBirth")} />
                </div>
                <div className="space-y-2 md:col-span-2">
                    <Label htmlFor="firebase-uid">Firebase UID</Label>
                    <Input id="firebase-uid" value={values.firebaseUID} onChange={onChange("firebaseUID")} />
                </div>
                <div className="space-y-2 md:col-span-2">
                    <Label htmlFor="last-login">Last Login</Label>
                    <Input
                        id="last-login"
                        type="datetime-local"
                        value={values.lastLogin}
                        onChange={onChange("lastLogin")}
                        disabled={disableLastLogin}
                    />
                </div>
                <div className="space-y-2 md:col-span-2">
                    <Label htmlFor="roles">Roles</Label>
                    <Textarea
                        id="roles"
                        value={values.rolesInput}
                        onChange={onChangeTextarea("rolesInput")}
                        placeholder="Comma-separated roles"
                        rows={3}
                    />
                    <div className="flex flex-wrap gap-2">
                        {parsedRoles.length > 0 ? (
                            parsedRoles.map((role) => (
                                <Badge key={role} variant="secondary">
                                    {role}
                                </Badge>
                            ))
                        ) : (
                            <span className={cn("text-xs text-muted-foreground")}>No roles set</span>
                        )}
                    </div>
                </div>
            </div>
            <div className="flex flex-wrap gap-3">
                <Button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? "Saving..." : submitLabel}
                </Button>
                {onCancel ? (
                    <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
                        Cancel
                    </Button>
                ) : null}
            </div>
        </form>
    );
}
