import {setContext} from "@apollo/client/link/context";

export const authLink = setContext((_, {headers}) => {
    // We don't need to set the token here, as it is already set in the cookie
    // The browser will automatically include the cookie in the request
    return {
        headers: {
            ...headers,
        },
    };
});
