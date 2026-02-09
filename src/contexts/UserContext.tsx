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

interface PhotoBackoffState {
    attempt: number;
    nextAttempt: number;
}

const CACHE_DURATION = 3 * 24 * 60 * 60 * 1000; // 3 days in milliseconds
const BACKOFF_BASE_MS = 30 * 1000; // 30 seconds
const BACKOFF_MAX_MS = 30 * 60 * 1000; // 30 minutes

function getBackoffState(uid: string): PhotoBackoffState | null {
    const raw = localStorage.getItem(`user_photo_backoff_${uid}`);
    if (!raw) return null;
    try {
        return JSON.parse(raw) as PhotoBackoffState;
    } catch {
        return null;
    }
}

function setBackoffState(uid: string, state: PhotoBackoffState) {
    localStorage.setItem(`user_photo_backoff_${uid}`, JSON.stringify(state));
}

function clearBackoffState(uid: string) {
    localStorage.removeItem(`user_photo_backoff_${uid}`);
}

async function fetchAndCachePhoto(photoURL: string, uid: string, fallbackPhotoURL?: string): Promise<string> {
    try {
        const backoffState = getBackoffState(uid);
        if (backoffState && Date.now() < backoffState.nextAttempt) {
            return fallbackPhotoURL ?? photoURL;
        }

        // Modify the URL to get a larger photo (256px instead of default 96px)
        const largePhotoURL = photoURL.replace(/=s\d+-c$/, "=s256-c");

        const response = await fetch(largePhotoURL, { cache: "no-store" });
        if (!response.ok) {
            console.warn("Photo fetch failed:", response.status, response.statusText);
            if (response.status === 429) {
                const attempt = (backoffState?.attempt ?? 0) + 1;
                const delay = Math.min(BACKOFF_MAX_MS, BACKOFF_BASE_MS * 2 ** (attempt - 1));
                setBackoffState(uid, { attempt, nextAttempt: Date.now() + delay });
            }
            return fallbackPhotoURL ?? photoURL;
        }

        const contentType = response.headers.get("content-type") ?? "";
        if (!contentType.startsWith("image/")) {
            console.warn("Photo fetch returned non-image content:", contentType);
            return fallbackPhotoURL ?? photoURL;
        }

        const blob = await response.blob();

        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onloadend = () => {
                const result = reader.result;
                if (typeof result !== "string" || !result.startsWith("data:image/")) {
                    console.warn("Photo cache produced invalid data URL.");
                    resolve(fallbackPhotoURL ?? photoURL);
                    return;
                }
                const dataURL = reader.result as string;
                const cacheData: CachedUserData = {
                    photoDataURL: dataURL,
                    originalURL: photoURL,
                    timestamp: Date.now(),
                };
                localStorage.setItem(`user_photo_${uid}`, JSON.stringify(cacheData));
                clearBackoffState(uid);
                resolve(dataURL);
            };
            reader.onerror = reject;
            reader.readAsDataURL(blob);
        });
    } catch (error) {
        console.error("Failed to cache photo:", error);
        return fallbackPhotoURL ?? photoURL; // Fallback to cached/original URL
    }
}

export function UserProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<User | undefined | null>(undefined);
    const [cachedPhotoURL, setCachedPhotoURL] = useState<string | null | undefined>(undefined);

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
                    let cachedPhotoFallback: string | undefined;

                    if (cachedData) {
                        try {
                            const parsed: CachedUserData = JSON.parse(cachedData) as CachedUserData;
                            const cacheAge = Date.now() - parsed.timestamp;
                            cachedPhotoFallback = parsed.photoDataURL;

                            // Use cached photo if it's fresh
                            if (cacheAge < CACHE_DURATION && parsed.originalURL === currentUser.photoURL) {
                                setCachedPhotoURL(parsed.photoDataURL);
                                setUser(currentUser);
                                return;
                            }
                        } catch (e) {
                            console.error("Failed to parse cached photo data:", e);
                        }
                    }

                    // Cache is stale or doesn't exist - fetch and cache new photo
                    const cachedPhotoURL = await fetchAndCachePhoto(
                        currentUser.photoURL,
                        currentUser.uid,
                        cachedPhotoFallback
                    );
                    setCachedPhotoURL(cachedPhotoURL);
                } else {
                    setCachedPhotoURL(null);
                }

                setUser(currentUser);
            })();
        });
        return () => unsubscribe();
    }, []);

    return <UserContext.Provider value={[user, setUser, cachedPhotoURL]}>{children}</UserContext.Provider>;
}
