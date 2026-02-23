import { useEffect, useState } from "react";
import { useUser } from "../contexts/UserContextDefinition";
import { getCurrentUser, roles } from "../lib/api";

export function useCurrentUserRoles() {
    const [user] = useUser();
    const [userRoles, setUserRoles] = useState<roles[]>([]);

    useEffect(() => {
        let isMounted = true;

        if (!user?.uid) {
            setUserRoles([]);
            return () => {
                isMounted = false;
            };
        }

        void getCurrentUser()
            .then((currentUser) => {
                if (isMounted) {
                    setUserRoles(currentUser?.roles ?? []);
                }
            })
            .catch(() => {
                if (isMounted) {
                    setUserRoles([]);
                }
            });

        return () => {
            isMounted = false;
        };
    }, [user?.uid]);

    const isAdmin = userRoles.includes(roles.ADMIN) || userRoles.includes(roles.SUPER_ADMIN);

    return { userRoles, isAdmin };
}
