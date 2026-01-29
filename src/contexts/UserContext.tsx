import { useEffect, useState } from "react";
import { type User, onAuthStateChanged } from "firebase/auth";
import { auth } from "../firebase";
import { UserContext } from "./UserContextDefinition";

// Extended user type with cached photo
interface CachedUserData {
    photoDataURL: string;
    originalURL: string;
    timestamp: number;
}

const CACHE_DURATION = 3 * 24 * 60 * 60 * 1000; // 3 days in milliseconds

async function fetchAndCachePhoto(photoURL: string, uid: string): Promise<string> {
    try {
        // Modify the URL to get a larger photo (256px instead of default 96px)
        const largePhotoURL = photoURL.replace(/=s\d+-c$/, "=s256-c");

        const response = await fetch(largePhotoURL);
        const blob = await response.blob();

        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onloadend = () => {
                const dataURL = reader.result as string;
                const cacheData: CachedUserData = {
                    photoDataURL: dataURL,
                    originalURL: photoURL,
                    timestamp: Date.now(),
                };
                localStorage.setItem(`user_photo_${uid}`, JSON.stringify(cacheData));
                resolve(dataURL);
            };
            reader.onerror = reject;
            reader.readAsDataURL(blob);
        });
    } catch (error) {
        console.error("Failed to cache photo:", error);
        return photoURL; // Fallback to original URL
    }
}

export function UserProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<User | undefined | null>(undefined);

    useEffect(() => {
        const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
            void (async () => {
                // For debugging: Log the ID token
                // currentUser
                //     ?.getIdToken()
                //     .then((token) => {
                //         console.log("User ID Token:", token);
                //     })
                //     .catch((error) => {
                //         console.error("Failed to get ID token:", error);
                //     });

                if (currentUser?.photoURL) {
                    const cacheKey = `user_photo_${currentUser.uid}`;
                    const cachedData = localStorage.getItem(cacheKey);

                    if (cachedData) {
                        try {
                            const parsed: CachedUserData = JSON.parse(cachedData) as CachedUserData;
                            const cacheAge = Date.now() - parsed.timestamp;

                            // Use cached photo if it's fresh
                            if (cacheAge < CACHE_DURATION && parsed.originalURL === currentUser.photoURL) {
                                // Override photoURL with cached data URL
                                Object.defineProperty(currentUser, "photoURL", {
                                    value: parsed.photoDataURL,
                                    writable: true,
                                    configurable: true,
                                });
                                setUser(currentUser);
                                return;
                            }
                        } catch (e) {
                            console.error("Failed to parse cached photo data:", e);
                        }
                    }

                    // Cache is stale or doesn't exist - fetch and cache new photo
                    const cachedPhotoURL = await fetchAndCachePhoto(currentUser.photoURL, currentUser.uid);
                    Object.defineProperty(currentUser, "photoURL", {
                        value: cachedPhotoURL,
                        writable: true,
                        configurable: true,
                    });
                }

                setUser(currentUser);
            })();
        });
        return () => unsubscribe();
    }, []);

    return <UserContext.Provider value={[user, setUser]}>{children}</UserContext.Provider>;
}
