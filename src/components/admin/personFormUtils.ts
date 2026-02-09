export interface PersonFormValues {
    id?: string;
    firstName: string;
    lastName: string;
    primaryEmail: string;
    secondaryEmail: string;
    phoneNumber: string;
    dateOfBirth: string;
    firebaseUID: string;
    lastLogin: string;
    rolesInput: string;
}

const emptyString = "";

function toDateInput(value: string | null | undefined) {
    if (!value) return emptyString;
    return value.slice(0, 10);
}

function toDateTimeLocalInput(value: string | null | undefined) {
    if (!value) return emptyString;
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return emptyString;
    const offsetMs = date.getTimezoneOffset() * 60_000;
    return new Date(date.getTime() - offsetMs).toISOString().slice(0, 16);
}

export function personToFormValues(person?: Partial<PersonFormValues>): PersonFormValues {
    return {
        id: person?.id,
        firstName: person?.firstName ?? emptyString,
        lastName: person?.lastName ?? emptyString,
        primaryEmail: person?.primaryEmail ?? emptyString,
        secondaryEmail: person?.secondaryEmail ?? emptyString,
        phoneNumber: person?.phoneNumber ?? emptyString,
        dateOfBirth: person?.dateOfBirth ? toDateInput(person.dateOfBirth) : emptyString,
        firebaseUID: person?.firebaseUID ?? emptyString,
        lastLogin: person?.lastLogin ? toDateTimeLocalInput(person.lastLogin) : emptyString,
        rolesInput: person?.rolesInput ?? emptyString,
    };
}
