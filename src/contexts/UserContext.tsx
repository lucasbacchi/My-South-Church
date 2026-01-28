import { useEffect, useState } from "react";
import { type User, onAuthStateChanged } from "firebase/auth";
import { auth } from "../firebase";
import { UserContext } from "./UserContextDefinition";

export function UserProvider({ children }: { children: React.ReactNode }) {
    // Initialize as undefined to distinguish between "loading" (undefined) and "not logged in" (null) if needed,
    // but here we just follow the type signature.
    const [user, setUser] = useState<User | undefined | null>(undefined);

    useEffect(() => {
        const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
            setUser(currentUser);
        });
        return () => unsubscribe();
    }, []);

    return <UserContext.Provider value={[user, setUser]}>{children}</UserContext.Provider>;
}
