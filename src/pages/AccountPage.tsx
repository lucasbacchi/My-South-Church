import { Link } from "react-router";
import { Button } from "@/components/ui/button";
import { useContext, useEffect, useState } from "react";
import { UserContext } from "../contexts/UserContextDefinition";
import { requireAuthClientLoader } from "../lib/clientLoaders";
import { getCurrentUser, roles } from "../lib/api";
import { getPersonByFirebaseUID, updatePerson } from "../lib/people";
import type { PersonUpsertInput } from "../types/people";

// eslint-disable-next-line react-refresh/only-export-components
export const clientLoader = requireAuthClientLoader;

type SaveMessage = { type: "success" | "error"; text: string } | null;

export default function AccountPage() {
    const [user, , cachedPhotoURL] = useContext(UserContext);
    const [userRoles, setUserRoles] = useState<roles[]>([]);
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [primaryEmail, setPrimaryEmail] = useState("");
    const [secondaryEmail, setSecondaryEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [personId, setPersonId] = useState<string | null>(null);
    const [isSaving, setIsSaving] = useState(false);
    const [saveMessage, setSaveMessage] = useState<SaveMessage>(null);

    useEffect(() => {
        if (user?.uid) {
            void getCurrentUser()
                .then((currentUser) => {
                    if (currentUser) {
                        setUserRoles(currentUser.roles);
                    }
                })
                .catch((error) => {
                    console.error("Failed to fetch user roles:", error);
                });
        }
    }, [user?.uid]);

    useEffect(() => {
        if (user?.uid && !personId) {
            void (async () => {
                try {
                    const person = await getPersonByFirebaseUID(user.uid);
                    if (!person) {
                        console.error("No person found for current user");
                        return;
                    }
                    setPersonId(person.id);
                    setFirstName(person.firstName ?? "");
                    setLastName(person.lastName ?? "");
                    setPrimaryEmail(person.primaryEmail ?? "");
                    setSecondaryEmail(person.secondaryEmail ?? "");
                    setPhone(person.phoneNumber ?? "");
                    // Note: address fields are not in the Person model
                    // If you need these, you'll need to add them to the backend
                } catch (error) {
                    console.error("Failed to load person data:", error);
                }
            })();
        }
    }, [user?.uid, personId]);

    useEffect(() => {
        if (user?.email) {
            setPrimaryEmail(user.email);
        }
    }, [user?.email]);

    // Account Form Functions
    function handleFirstNameChange(e: React.ChangeEvent<HTMLInputElement>) {
        setFirstName(e.target.value);
    }
    function handleLastNameChange(e: React.ChangeEvent<HTMLInputElement>) {
        setLastName(e.target.value);
    }
    function handleSecondaryEmailChange(e: React.ChangeEvent<HTMLInputElement>) {
        setSecondaryEmail(e.target.value);
    }
    function handlePhoneChange(e: React.ChangeEvent<HTMLInputElement>) {
        setPhone(e.target.value);
    }

    async function handleSaveChanges() {
        if (!personId) {
            setSaveMessage({ type: "error", text: "Unable to save: Person ID not found" });
            return;
        }

        setIsSaving(true);
        setSaveMessage(null);

        try {
            const payload: PersonUpsertInput = {
                firstName,
                lastName,
                primaryEmail: user?.email ?? primaryEmail,
                secondaryEmail: secondaryEmail === "" ? null : secondaryEmail,
                phoneNumber: phone || null,
                dateOfBirth: null,
                firebaseUID: user?.uid ?? null,
                roles: userRoles,
            };

            await updatePerson(personId, payload);
            setSaveMessage({ type: "success", text: "Changes saved successfully!" });
            setTimeout(() => setSaveMessage(null), 3000);
        } catch (error) {
            console.error("Failed to save changes:", error);
            setSaveMessage({
                type: "error",
                text: error instanceof Error ? error.message : "Failed to save changes",
            });
        } finally {
            setIsSaving(false);
        }
    }

    function handleDiscard() {
        void (async () => {
            try {
                const person = await getPersonByFirebaseUID(user?.uid ?? "");
                if (person) {
                    setFirstName(person.firstName ?? "");
                    setLastName(person.lastName ?? "");
                    setSecondaryEmail(person.secondaryEmail ?? "");
                    setPhone(person.phoneNumber ?? "");
                }
            } catch (error) {
                console.error("Failed to reload person data:", error);
            }
        })();
        setSaveMessage(null);
    }

    return (
        <div className="flex flex-col gap-6 sm:gap-8">
            {!user && (
                <div className="bg-linear-to-br from-destructive/10 to-accent/10 border border-destructive/20 rounded-xl p-6 sm:p-8 text-center">
                    <h1 className="text-2xl sm:text-3xl font-bold text-foreground">You are not signed in</h1>
                    <p className="text-foreground/70 mb-6">Please sign in to access your account information.</p>
                    <Link
                        to="/"
                        className="inline-flex items-center gap-2 px-6 py-3 bg-primary hover:bg-primary/90 text-primary-foreground font-semibold rounded-lg transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
                    >
                        Go Home
                        <span className="material-symbols-outlined">arrow_forward</span>
                    </Link>
                </div>
            )}
            {user ? (
                <div className="flex flex-col gap-6 sm:gap-8">
                    <div>
                        <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold bg-linear-to-r from-primary to-secondary bg-clip-text text-transparent">
                            Your Account
                        </h1>
                        <div className="h-1 w-16 sm:w-20 bg-linear-to-r from-primary to-secondary rounded-full" />
                    </div>

                    <div className="bg-card border border-border rounded-2xl p-6 sm:p-8 shadow-lg flex flex-col gap-6">
                        <div className="text-center pb-6 sm:pb-8">
                            <img
                                key={cachedPhotoURL ?? user.photoURL ?? "fallback"}
                                src={(cachedPhotoURL ?? user.photoURL)?.replace(/=s\d+-c$/, "=s240-c") ?? ""}
                                alt="user profile"
                                className="mx-auto mb-4 sm:mb-6 rounded-full w-24 h-24 sm:w-28 sm:h-28 md:w-32 md:h-32 object-cover shadow-md ring-3"
                            />
                            <p className="text-2xl sm:text-3xl font-bold text-foreground">{user.displayName}</p>
                            <p className="text-muted-foreground mt-2">{user.email}</p>
                            <div className="mt-4 flex flex-wrap gap-2 justify-center">
                                {userRoles.includes(roles.SUPER_ADMIN) && (
                                    <div className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-purple-500/15 border border-purple-500/40 rounded-full text-sm text-purple-700 dark:text-purple-400 font-medium">
                                        Super Admin
                                    </div>
                                )}
                                {userRoles.includes(roles.ADMIN) && !userRoles.includes(roles.SUPER_ADMIN) && (
                                    <div className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-blue-500/15 border border-blue-500/40 rounded-full text-sm text-blue-700 dark:text-blue-400 font-medium">
                                        Admin
                                    </div>
                                )}
                                {user.emailVerified ? (
                                    <div className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-green-500/15 border border-green-500/40 rounded-full text-sm text-green-700 dark:text-green-400 font-medium">
                                        <span className="material-symbols-outlined text-base">verified</span>
                                        Email Verified
                                    </div>
                                ) : (
                                    <div className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-amber-500/15 border border-amber-500/40 rounded-full text-sm text-amber-700 dark:text-amber-400 font-medium">
                                        <span className="material-symbols-outlined text-base">pending</span>
                                        Email Not Verified
                                    </div>
                                )}
                            </div>
                        </div>
                        <div className="border-t border-border" />

                        {!saveMessage
                            ? null
                            : (() => {
                                  const msg = { ...saveMessage };
                                  const isSuccess = msg.type === "success";
                                  return (
                                      <div
                                          className={`p-4 rounded-lg ${
                                              isSuccess
                                                  ? "bg-green-500/15 border border-green-500/40 text-green-700 dark:text-green-400"
                                                  : "bg-red-500/15 border border-red-500/40 text-red-700 dark:text-red-400"
                                          }`}
                                      >
                                          {msg.text}
                                      </div>
                                  );
                              })()}

                        <div className="pt-2">
                            <h3 className="text-lg font-semibold text-foreground mb-4">Personal Information</h3>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6">
                                <div className="form-group">
                                    <label
                                        htmlFor="firstName"
                                        className="text-sm font-medium text-foreground mb-2 block"
                                    >
                                        First Name
                                    </label>
                                    <input
                                        type="text"
                                        placeholder="First Name"
                                        value={firstName}
                                        onChange={handleFirstNameChange}
                                        className="w-full border border-border rounded-lg px-4 py-2.5 bg-input text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-transparent transition-all"
                                    />
                                </div>
                                <div className="form-group">
                                    <label
                                        htmlFor="lastName"
                                        className="text-sm font-medium text-foreground mb-2 block"
                                    >
                                        Last Name
                                    </label>
                                    <input
                                        type="text"
                                        placeholder="Last Name"
                                        value={lastName}
                                        onChange={handleLastNameChange}
                                        className="w-full border border-border rounded-lg px-4 py-2.5 bg-input text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-transparent transition-all"
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="pt-2">
                            <h3 className="text-lg font-semibold text-foreground mb-4">Contact Information</h3>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6">
                                <div className="form-group sm:col-span-2">
                                    <label
                                        htmlFor="primaryEmail"
                                        className="text-sm font-medium text-foreground mb-2 block"
                                    >
                                        Primary Email
                                    </label>
                                    <input
                                        type="email"
                                        placeholder="Primary Email"
                                        value={primaryEmail}
                                        readOnly
                                        className="w-full border border-border rounded-lg px-4 py-2.5 bg-muted text-foreground placeholder-muted-foreground opacity-75 cursor-not-allowed"
                                    />
                                    <p className="text-xs text-muted-foreground mt-2">
                                        Your primary email is tied to your Google account. To change it, update your
                                        Google account settings.
                                    </p>
                                </div>
                                <div className="form-group sm:col-span-2">
                                    <label
                                        htmlFor="secondaryEmail"
                                        className="text-sm font-medium text-foreground mb-2 block"
                                    >
                                        Secondary Email
                                    </label>
                                    <input
                                        type="email"
                                        placeholder="Secondary Email"
                                        value={secondaryEmail}
                                        onChange={handleSecondaryEmailChange}
                                        className="w-full border border-border rounded-lg px-4 py-2.5 bg-input text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-transparent transition-all"
                                    />
                                </div>
                                <div className="form-group sm:col-span-2">
                                    <label htmlFor="phone" className="text-sm font-medium text-foreground mb-2 block">
                                        Phone Number
                                    </label>
                                    <input
                                        type="tel"
                                        placeholder="Phone Number"
                                        value={phone}
                                        onChange={handlePhoneChange}
                                        className="w-full border border-border rounded-lg px-4 py-2.5 bg-input text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-transparent transition-all"
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="flex gap-3 pt-6 border-t border-border">
                            <Button
                                onClick={() => void handleSaveChanges()}
                                disabled={isSaving}
                                className="flex-1 bg-primary hover:bg-primary/90 text-primary-foreground font-semibold py-2.5 rounded-lg transition-all disabled:opacity-50"
                            >
                                <span className="material-symbols-outlined mr-2">save</span>
                                {isSaving ? "Saving..." : "Save Changes"}
                            </Button>
                            <Button
                                onClick={handleDiscard}
                                variant="outline"
                                className="flex-1 font-semibold py-2.5 rounded-lg transition-all"
                            >
                                <span className="material-symbols-outlined mr-2">close</span>
                                Discard
                            </Button>
                        </div>
                    </div>
                </div>
            ) : null}
        </div>
    );
}
