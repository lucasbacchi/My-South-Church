import { useState } from "react";
import { auth } from "../main";
import { GoogleAuthProvider, signInWithPopup } from "firebase/auth";
import axios from "axios";

const SignIn = () => {
    const [error, setError] = useState<string | null>(null);

    const handleGoogleSignIn = async () => {
        try {
            const provider = new GoogleAuthProvider();
            provider.addScope("email");
            provider.addScope("profile");
            provider.addScope("https://apps-apis.google.com/a/feeds/groups/");
            provider.addScope("https://www.googleapis.com/auth/groups");
            provider.addScope("https://www.googleapis.com/auth/cloud-platform");
            provider.addScope("https://www.googleapis.com/auth/admin.directory.group");
            const userCredential = await signInWithPopup(auth, provider);
            // The signed-in user info.
            const user = auth.currentUser;
            // This gives you a Facebook Access Token.
            const credential = GoogleAuthProvider.credentialFromResult(userCredential);
            const accessToken = credential?.accessToken; // Add null check for 'credential'
            const userKey = user?.providerData[0].uid;
            // User signed in successfully with Google
            console.log("User signed in with Google:", auth.currentUser);
            console.log("Access Token:", accessToken);
            console.log("User Key:", userKey);

            const fetchGroups = async () => {
                const response = await axios.get(
                    "https://cloudidentity.googleapis.com/v1/groups?parent=customers/C02kiuknt",
                    { headers: { Authorization: `Bearer ${accessToken}` } }
                );
                console.log(response.data);
            };

            if (accessToken) {
                fetchGroups();
            }
        } catch (error) {
            if (error instanceof Error) {
                setError(error.message);
            } else {
                setError("An unknown error occurred");
            }
        }
    };

    return (
        <div>
            <h1>Sign In</h1>
            {error && <p style={{ color: "red" }}>{error}</p>}
            <button onClick={handleGoogleSignIn}>Sign in with Google</button>
        </div>
    );
};

export default SignIn;
