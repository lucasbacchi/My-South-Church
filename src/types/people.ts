export interface Person {
    id: string;
    firstName: string;
    lastName: string;
    primaryEmail: string;
    secondaryEmail: string | null;
    phoneNumber: string | null;
    dateOfBirth: string | null;
    firebaseUID: string | null;
    lastLogin: string | null;
    roles: string[];
}

export interface ImportPeopleResult {
    total: number;
    created: number;
    updated?: number;
    skipped: number;
    skippedExistingEmail?: number;
    skippedDuplicateEmail?: number;
    invalid: number;
    invalidMissingAttributes?: number;
    invalidMissingRequired?: number;
    invalidMissingEmail?: number;
    invalidMissingFirstName?: number;
    invalidMissingLastName?: number;
    issues: string[];
}

export type PersonInput = Omit<Person, "id">;
export type PersonUpsertInput = Omit<PersonInput, "lastLogin">;
