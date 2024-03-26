import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App.tsx";
import "./index.css";
import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { ReCaptchaV3Provider, initializeAppCheck } from "firebase/app-check";
import { BrowserRouter } from "react-router-dom";

const firebaseConfig = {
    apiKey: "AIzaSyD0_RFTt7klzFVeYZYRB0qFyTrcFvdrJsk",
    authDomain: "my-south-church.firebaseapp.com",
    projectId: "my-south-church",
    storageBucket: "my-south-church.appspot.com",
    messagingSenderId: "474652198626",
    appId: "1:474652198626:web:ef019c7e2e5a022f70c0bb",
    measurementId: "G-GC069KPHF1"
};

const app = initializeApp(firebaseConfig);
const appCheck = initializeAppCheck(app, {
    provider: new ReCaptchaV3Provider("6Lfi-KApAAAAAEfoDbnbbheiuPxBItepGTi3o_7k")
});
const auth = getAuth(app);

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <BrowserRouter>
            <App />
        </BrowserRouter>
    </React.StrictMode>
);

export default app;
export { auth, appCheck };
