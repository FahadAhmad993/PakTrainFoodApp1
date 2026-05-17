import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
import { getAuth } from "firebase/auth";

const firebaseConfig = {
  apiKey: "AIzaSyDeA29tUGfig2XLve_jbQfOr-U-yIgdAt4",
  authDomain: "paktrainfoodservice.firebaseapp.com",
  databaseURL: "https://paktrainfoodservice-default-rtdb.firebaseio.com",
  projectId: "paktrainfoodservice",
  storageBucket: "paktrainfoodservice.firebasestorage.app",
  messagingSenderId: "584020651389",
  appId: "1:584020651389:web:7a30af9e2cfa6455c1f014",
  measurementId: "G-NFYS1Q09TE"
};

const app = initializeApp(firebaseConfig);

export const db = getFirestore(app);
export const auth = getAuth(app);