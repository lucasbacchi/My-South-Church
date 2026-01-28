import { type User, getAuth, onAuthStateChanged, signOut } from "firebase/auth";
import { initializeApp } from "firebase/app";
import { ReCaptchaV3Provider, initializeAppCheck } from "firebase/app-check";
import { getAnalytics, isSupported as isAnalyticsSupported } from "firebase/analytics";
import { getPerformance } from "firebase/performance";
import { getStorage } from "firebase/storage";

// Firebase Config
const firebaseConfig = {
    apiKey: "AIzaSyD0_RFTt7klzFVeYZYRB0qFyTrcFvdrJsk",
    authDomain: "my.southchurch.com",
    projectId: "my-south-church",
    storageBucket: "my-south-church.appspot.com",
    messagingSenderId: "474652198626",
    appId: "1:474652198626:web:ef019c7e2e5a022f70c0bb",
    measurementId: "G-GC069KPHF1",
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Firebase Analytics - Only initialize in browser environment
let analytics: unknown = null;
if (typeof window !== "undefined") {
    isAnalyticsSupported()
        .then((supported) => {
            if (supported) {
                analytics = getAnalytics(app);
            }
        })
        .catch(() => {
            // Analytics not supported, ignore
        });
}

// Firebase App Check - Only initialize in browser environment
let appCheck: unknown = null;
if (typeof window !== "undefined" && typeof document !== "undefined") {
    try {
        appCheck = initializeAppCheck(app, {
            provider: new ReCaptchaV3Provider("6Lfi-KApAAAAAEfoDbnbbheiuPxBItepGTi3o_7k"),
        });
    } catch (error) {
        console.warn("App Check initialization failed:", error);
    }
}

// Firebase Auth
const auth = getAuth(app);

// Firebase Performance - Only initialize in browser environment
let performance: unknown = null;
if (typeof window !== "undefined") {
    try {
        performance = getPerformance(app);
    } catch (error) {
        console.warn("Performance monitoring initialization failed:", error);
    }
}

// Firebase Storage
const storage = getStorage(app);

// Custom Globals

function getUser(): Promise<User | null> {
    return new Promise((resolve, reject) => {
        const unsubscribe = onAuthStateChanged(
            auth,
            (user) => {
                unsubscribe();
                resolve(user);
            },
            (error) => {
                unsubscribe();
                reject(error);
            }
        );
    });
}

export function signOutUser() {
    signOut(auth)
        .then(() => {
            console.log("Signed out");
            // Reload the page for security
            window.location.href = "/";
        })
        .catch((error) => {
            console.error("Error signing out:", error);
        });
}

export { app, analytics, appCheck, auth, performance, storage };
export { getUser };
