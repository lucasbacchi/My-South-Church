import { Link } from "react-router";
import { Button } from "@/components/ui/button";
import { useContext, useState } from "react";
import { UserContext } from "../contexts/UserContextDefinition";

export default function AccountPage() {
    const [user] = useContext(UserContext);
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [primaryEmail, setPrimaryEmail] = useState("");
    const [secondaryEmail, setSecondaryEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [address, setAddress] = useState("");
    const [city, setCity] = useState("");
    const [state, setState] = useState("");
    const [zip, setZip] = useState("");

    // Account Form Functions
    function handleFirstNameChange(e: React.ChangeEvent<HTMLInputElement>) {
        setFirstName(e.target.value);
    }
    function handleLastNameChange(e: React.ChangeEvent<HTMLInputElement>) {
        setLastName(e.target.value);
    }
    function handlePrimaryEmailChange(e: React.ChangeEvent<HTMLInputElement>) {
        setPrimaryEmail(e.target.value);
    }
    function handleSecondaryEmailChange(e: React.ChangeEvent<HTMLInputElement>) {
        setSecondaryEmail(e.target.value);
    }
    function handlePhoneChange(e: React.ChangeEvent<HTMLInputElement>) {
        setPhone(e.target.value);
    }
    function handleAddressChange(e: React.ChangeEvent<HTMLInputElement>) {
        setAddress(e.target.value);
    }
    function handleCityChange(e: React.ChangeEvent<HTMLInputElement>) {
        setCity(e.target.value);
    }
    function handleStateChange(e: React.ChangeEvent<HTMLInputElement>) {
        setState(e.target.value);
    }
    function handleZipChange(e: React.ChangeEvent<HTMLInputElement>) {
        setZip(e.target.value);
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

                    <div className="bg-card border border-border rounded-xl p-6 sm:p-8 shadow-lg flex flex-col gap-6">
                        <div className="text-center border-b border-border pb-4 sm:pb-6">
                            <img
                                src={user.photoURL?.replace(/=s\d+-c$/, "=s240-c") ?? ""}
                                alt="user profile"
                                className="mx-auto mb-3 sm:mb-4 rounded-full w-24 h-24 sm:w-28 sm:h-28 md:w-32 md:h-32 object-cover ring-4 ring-primary/20"
                            />
                            <p className="text-xl sm:text-2xl font-bold text-foreground">{user.displayName}</p>
                            <p className="text-muted-foreground mt-1">{user.email}</p>
                            {user.emailVerified ? (
                                <div className="mt-3 inline-flex items-center gap-2 px-3 py-1 bg-green-500/10 border border-green-500/30 rounded-full text-sm text-green-600 dark:text-green-400">
                                    <span className="material-symbols-outlined text-base">check_circle</span>
                                    Email Verified
                                </div>
                            ) : (
                                <div className="mt-3 inline-flex items-center gap-2 px-3 py-1 bg-destructive/10 border border-destructive/30 rounded-full text-sm text-destructive">
                                    <span className="material-symbols-outlined text-base">error</span>
                                    Email Not Verified
                                </div>
                            )}
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6">
                            <div className="form-group">
                                <label htmlFor="firstName">First Name</label>
                                <input
                                    type="text"
                                    placeholder="First Name"
                                    value={firstName}
                                    onChange={handleFirstNameChange}
                                    className="w-full border border-border rounded-md px-3 py-2 bg-input text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="lastName">Last Name</label>
                                <input
                                    type="text"
                                    placeholder="Last Name"
                                    value={lastName}
                                    onChange={handleLastNameChange}
                                    className="w-full border border-border rounded-md px-3 py-2 bg-input text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="primaryEmail">Primary Email</label>
                                <input
                                    type="email"
                                    placeholder="Primary Email"
                                    value={primaryEmail}
                                    onChange={handlePrimaryEmailChange}
                                    className="w-full border border-border rounded-md px-3 py-2 bg-input text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="secondaryEmail">Secondary Email</label>
                                <input
                                    type="email"
                                    placeholder="Secondary Email"
                                    value={secondaryEmail}
                                    onChange={handleSecondaryEmailChange}
                                    className="w-full border border-border rounded-md px-3 py-2 bg-input text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="phone">Phone Number</label>
                                <input
                                    type="tel"
                                    placeholder="Phone Number"
                                    value={phone}
                                    onChange={handlePhoneChange}
                                    className="w-full border border-border rounded-md px-3 py-2 bg-input text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="address">Address</label>
                                <input
                                    type="text"
                                    placeholder="Address"
                                    value={address}
                                    onChange={handleAddressChange}
                                    className="w-full border border-border rounded-md px-3 py-2 bg-input text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="city">City</label>
                                <input
                                    type="text"
                                    placeholder="City"
                                    value={city}
                                    onChange={handleCityChange}
                                    className="w-full border border-border rounded-md px-3 py-2 bg-input text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="state">State</label>
                                <input
                                    type="text"
                                    placeholder="State"
                                    value={state}
                                    onChange={handleStateChange}
                                    className="w-full border border-border rounded-md px-3 py-2 bg-input text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="zip">Zip Code</label>
                                <input
                                    type="text"
                                    placeholder="Zip Code"
                                    value={zip}
                                    onChange={handleZipChange}
                                    className="w-full border border-border rounded-md px-3 py-2 bg-input text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                                />
                            </div>
                        </div>

                        <div className="flex gap-4 pt-4">
                            <Button onClick={() => alert("Save Changes clicked")} className="flex-1">
                                <span className="material-symbols-outlined mr-2">save</span>
                                Save Changes
                            </Button>
                        </div>
                    </div>
                </div>
            ) : null}
        </div>
    );
}
