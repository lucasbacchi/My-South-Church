import { type Dispatch, type SetStateAction, createContext, useContext } from "react";
import { type User } from "firebase/auth";

// Definition of the context data type matching the previous usage: [user, setUser]
// Although setUser might not be used for auth updates (since Firebase handles it),
// keeping the signature prevents breaking existing code that expects a tuple.
export type ReactSetter<Data> = Dispatch<SetStateAction<Data | undefined>>;
export type ContextData = [User | undefined | null, ReactSetter<User | undefined | null>];

export const UserContext = createContext<ContextData>([undefined, () => {}]);

export function useUser() {
    return useContext(UserContext);
}
