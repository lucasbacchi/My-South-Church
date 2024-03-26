import { onRequest } from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import axios from "axios";

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

export const helloWorld = onRequest((request, response) => {
    logger.info("Hello logs!", { structuredData: true });
    response.send("Hello from Firebase!");
});

export const fetchGroups = onRequest(
    {
        timeoutSeconds: 60,
        region: "us-east1"
    },
    async (request, response) => {
        const apiResponse = await axios.get(
            "https://admin.googleapis.com/admin/directory/v1/groups?userKey=" + request.query.userKey
            /* {
                headers: { Authorization: `Bearer ${request.query.accessToken}` }
            } */
        );
        logger.info(apiResponse.data);
        response.send(apiResponse.data);
    }
);
