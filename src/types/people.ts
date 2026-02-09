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

export type PersonInput = Omit<Person, "id">;
