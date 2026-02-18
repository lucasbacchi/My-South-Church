import { useEffect, useMemo, useState, type SubmitEvent, type ChangeEvent } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { cn } from "@/lib/utils";
import { type PersonInput } from "@/types/people";
import { type PersonFormValues } from "@/components/admin/personFormUtils";
import { roles } from "@/lib/api";

interface PersonFormProps {
    initialValues: PersonFormValues;
    submitLabel: string;
    isSubmitting?: boolean;
    onSubmit: (payload: Omit<PersonInput, "lastLogin">) => void | Promise<void>;
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

    const onChange = (field: keyof PersonFormValues) => (event: ChangeEvent<HTMLInputElement>) => {
        setValues((prev) => ({ ...prev, [field]: event.target.value }));
    };

    const toggleRole = (role: string) => {
        const currentRoles = parsedRoles;
        const newRoles = currentRoles.includes(role) ? currentRoles.filter((r) => r !== role) : [...currentRoles, role];
        setValues((prev) => ({ ...prev, rolesInput: newRoles.join(", ") }));
    };

    const handleSubmit = (event: SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();
        const payload: Omit<PersonInput, "lastLogin"> = {
            firstName: values.firstName.trim(),
            lastName: values.lastName.trim(),
            primaryEmail: values.primaryEmail.trim(),
            secondaryEmail: normalizeOptional(values.secondaryEmail),
            phoneNumber: normalizeOptional(values.phoneNumber),
            dateOfBirth: normalizeOptional(values.dateOfBirth),
            firebaseUID: normalizeOptional(values.firebaseUID),
            roles: parsedRoles,
        };

        void onSubmit(payload);
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid gap-6 md:grid-cols-2">
                {showId ? (
                    <div className="space-y-2 md:col-span-2">
                        <Label htmlFor="person-id">Person ID</Label>
                        <Input id="person-id" value={values.id ?? emptyString} readOnly disabled={true} />
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
                    <Label>Roles</Label>
                    <div className="space-y-3">
                        {Object.values(roles).map((role) => (
                            <div key={role} className="flex items-center space-x-2">
                                <Checkbox
                                    id={`role-${role}`}
                                    checked={parsedRoles.includes(role)}
                                    onCheckedChange={() => toggleRole(role)}
                                />
                                <label
                                    htmlFor={`role-${role}`}
                                    className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                                >
                                    {role.replace("_", " ")}
                                </label>
                            </div>
                        ))}
                    </div>
                    <div className="flex flex-wrap gap-2 mt-3">
                        {parsedRoles.length > 0 ? (
                            parsedRoles.map((role) => (
                                <Badge key={role} variant="secondary">
                                    {role}
                                </Badge>
                            ))
                        ) : (
                            <span className={cn("text-xs text-muted-foreground")}>No roles selected</span>
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
