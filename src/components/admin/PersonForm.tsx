import { type ChangeEvent, type SubmitEvent, useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { cn } from "@/lib/utils";
import { type PersonUpsertInput } from "@/types/people";
import { type PersonFormValues } from "@/components/admin/personFormUtils";
import { roles } from "@/lib/api";

interface PersonFormProps {
    initialValues: PersonFormValues;
    submitLabel: string;
    isSubmitting?: boolean;
    onSubmit: (payload: PersonUpsertInput) => void | Promise<void>;
    onCancel?: () => void;
    showId?: boolean;
    disableLastLogin?: boolean;
    showLastLogin?: boolean;
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

const getNowMs = () => Date.now();

function isDateValid(value: string, nowMs: number) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
    const [year, month, day] = value.split("-").map(Number);
    const date = new Date(year, month - 1, day);
    return (
        date.getFullYear() === year &&
        date.getMonth() === month - 1 &&
        date.getDate() === day &&
        date.getTime() <= nowMs
    );
}

export default function PersonForm({
    initialValues,
    submitLabel,
    isSubmitting = false,
    onSubmit,
    onCancel,
    showId = false,
    disableLastLogin = true,
    showLastLogin = true,
}: PersonFormProps) {
    const [values, setValues] = useState<PersonFormValues>(initialValues);
    const [errors, setErrors] = useState<Partial<Record<keyof PersonFormValues, string>>>({});

    useEffect(() => {
        const timeoutId = setTimeout(() => {
            setValues(initialValues);
            setErrors({});
        }, 0);

        return () => clearTimeout(timeoutId);
    }, [initialValues]);

    const parsedRoles = useMemo(() => parseRolesInput(values.rolesInput), [values.rolesInput]);

    const onChange = (field: keyof PersonFormValues) => (event: ChangeEvent<HTMLInputElement>) => {
        setValues((prev) => ({ ...prev, [field]: event.target.value }));
        setErrors((prev) => (prev[field] ? { ...prev, [field]: undefined } : prev));
    };

    const getFieldError = (current: PersonFormValues, field: keyof PersonFormValues) => {
        switch (field) {
            case "firstName":
                return current.firstName.trim() ? undefined : "First name is required.";
            case "lastName":
                return current.lastName.trim() ? undefined : "Last name is required.";
            case "primaryEmail":
                if (!current.primaryEmail.trim()) return "Primary email is required.";
                return isEmailValid(current.primaryEmail) ? undefined : "Enter a valid email address.";
            case "secondaryEmail":
                if (!current.secondaryEmail.trim()) return undefined;
                return isEmailValid(current.secondaryEmail) ? undefined : "Enter a valid email address.";
            case "phoneNumber":
                if (!current.phoneNumber.trim()) return undefined;
                return isPhoneValid(current.phoneNumber) ? undefined : "Enter a valid phone number.";
            case "dateOfBirth":
                if (!current.dateOfBirth.trim()) return undefined;
                return isDateValid(current.dateOfBirth, getNowMs()) ? undefined : "Enter a valid date in the past.";
            case "firebaseUID":
                if (!current.firebaseUID.trim()) return undefined;
                return isFirebaseUidValid(current.firebaseUID)
                    ? undefined
                    : "Use 1-128 letters, numbers, dashes, or underscores.";
            default:
                return undefined;
        }
    };

    const onBlur = (field: keyof PersonFormValues) => () => {
        setErrors((prev) => ({ ...prev, [field]: getFieldError(values, field) }));
    };

    const isEmailValid = (value: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim());

    const isPhoneValid = (value: string) => {
        const trimmed = value.trim();
        if (!/^[0-9+()\-\s.]+$/.test(trimmed)) return false;
        const digits = trimmed.replace(/\D/g, "");
        return digits.length >= 7 && digits.length <= 15;
    };

    const isFirebaseUidValid = (value: string) => /^[A-Za-z0-9_-]{1,128}$/.test(value.trim());

    const validateValues = (current: PersonFormValues) => {
        const nextErrors: Partial<Record<keyof PersonFormValues, string>> = {};

        if (!current.firstName.trim()) nextErrors.firstName = "First name is required.";
        if (!current.lastName.trim()) nextErrors.lastName = "Last name is required.";

        if (!current.primaryEmail.trim()) {
            nextErrors.primaryEmail = "Primary email is required.";
        } else if (!isEmailValid(current.primaryEmail)) {
            nextErrors.primaryEmail = "Enter a valid email address.";
        }

        if (current.secondaryEmail.trim() && !isEmailValid(current.secondaryEmail)) {
            nextErrors.secondaryEmail = "Enter a valid email address.";
        }

        if (current.phoneNumber.trim() && !isPhoneValid(current.phoneNumber)) {
            nextErrors.phoneNumber = "Enter a valid phone number.";
        }

        if (current.dateOfBirth.trim() && !isDateValid(current.dateOfBirth, getNowMs())) {
            nextErrors.dateOfBirth = "Enter a valid date in the past.";
        }

        if (current.firebaseUID.trim() && !isFirebaseUidValid(current.firebaseUID)) {
            nextErrors.firebaseUID = "Use 1-128 letters, numbers, dashes, or underscores.";
        }

        return nextErrors;
    };

    const toggleRole = (role: string) => {
        const currentRoles = parsedRoles;
        const newRoles = currentRoles.includes(role) ? currentRoles.filter((r) => r !== role) : [...currentRoles, role];
        setValues((prev) => ({ ...prev, rolesInput: newRoles.join(", ") }));
    };

    const handleSubmit = (event: SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();
        const validationErrors = validateValues(values);
        if (Object.values(validationErrors).some(Boolean)) {
            setErrors(validationErrors);
            return;
        }
        const payload: PersonUpsertInput = {
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
                    <Input
                        id="first-name"
                        value={values.firstName}
                        onChange={onChange("firstName")}
                        onBlur={onBlur("firstName")}
                        aria-invalid={Boolean(errors.firstName)}
                        aria-describedby={errors.firstName ? "first-name-error" : undefined}
                        required
                    />
                    {errors.firstName ? (
                        <p id="first-name-error" className="text-xs text-destructive">
                            {errors.firstName}
                        </p>
                    ) : null}
                </div>
                <div className="space-y-2">
                    <Label htmlFor="last-name">Last Name</Label>
                    <Input
                        id="last-name"
                        value={values.lastName}
                        onChange={onChange("lastName")}
                        onBlur={onBlur("lastName")}
                        aria-invalid={Boolean(errors.lastName)}
                        aria-describedby={errors.lastName ? "last-name-error" : undefined}
                        required
                    />
                    {errors.lastName ? (
                        <p id="last-name-error" className="text-xs text-destructive">
                            {errors.lastName}
                        </p>
                    ) : null}
                </div>
                <div className="space-y-2">
                    <Label htmlFor="primary-email">Primary Email</Label>
                    <Input
                        id="primary-email"
                        type="email"
                        value={values.primaryEmail}
                        onChange={onChange("primaryEmail")}
                        onBlur={onBlur("primaryEmail")}
                        aria-invalid={Boolean(errors.primaryEmail)}
                        aria-describedby={errors.primaryEmail ? "primary-email-error" : undefined}
                        required
                    />
                    {errors.primaryEmail ? (
                        <p id="primary-email-error" className="text-xs text-destructive">
                            {errors.primaryEmail}
                        </p>
                    ) : null}
                </div>
                <div className="space-y-2">
                    <Label htmlFor="secondary-email">Secondary Email</Label>
                    <Input
                        id="secondary-email"
                        type="email"
                        value={values.secondaryEmail}
                        onChange={onChange("secondaryEmail")}
                        onBlur={onBlur("secondaryEmail")}
                        aria-invalid={Boolean(errors.secondaryEmail)}
                        aria-describedby={errors.secondaryEmail ? "secondary-email-error" : undefined}
                    />
                    {errors.secondaryEmail ? (
                        <p id="secondary-email-error" className="text-xs text-destructive">
                            {errors.secondaryEmail}
                        </p>
                    ) : null}
                </div>
                <div className="space-y-2">
                    <Label htmlFor="phone">Phone Number</Label>
                    <Input
                        id="phone"
                        type="tel"
                        value={values.phoneNumber}
                        onChange={onChange("phoneNumber")}
                        onBlur={onBlur("phoneNumber")}
                        aria-invalid={Boolean(errors.phoneNumber)}
                        aria-describedby={errors.phoneNumber ? "phone-error" : undefined}
                    />
                    {errors.phoneNumber ? (
                        <p id="phone-error" className="text-xs text-destructive">
                            {errors.phoneNumber}
                        </p>
                    ) : null}
                </div>
                <div className="space-y-2">
                    <Label htmlFor="dob">Date of Birth</Label>
                    <Input
                        id="dob"
                        type="date"
                        value={values.dateOfBirth}
                        onChange={onChange("dateOfBirth")}
                        onBlur={onBlur("dateOfBirth")}
                        aria-invalid={Boolean(errors.dateOfBirth)}
                        aria-describedby={errors.dateOfBirth ? "dob-error" : undefined}
                    />
                    {errors.dateOfBirth ? (
                        <p id="dob-error" className="text-xs text-destructive">
                            {errors.dateOfBirth}
                        </p>
                    ) : null}
                </div>
                <div className="space-y-2 md:col-span-2">
                    <Label htmlFor="firebase-uid">Firebase UID</Label>
                    <Input
                        id="firebase-uid"
                        value={values.firebaseUID}
                        onChange={onChange("firebaseUID")}
                        onBlur={onBlur("firebaseUID")}
                        aria-invalid={Boolean(errors.firebaseUID)}
                        aria-describedby={errors.firebaseUID ? "firebase-uid-error" : undefined}
                    />
                    {errors.firebaseUID ? (
                        <p id="firebase-uid-error" className="text-xs text-destructive">
                            {errors.firebaseUID}
                        </p>
                    ) : null}
                </div>
                {showLastLogin ? (
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
                ) : null}
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
