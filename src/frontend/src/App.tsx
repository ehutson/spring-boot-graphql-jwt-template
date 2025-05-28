import {ApolloProvider} from "@apollo/client";
import {Provider as ReduxProvider} from "react-redux";
import {store} from "@/app/store";
import {apolloClient} from "@/app/apollo-client.ts";
import {AppRouter} from "@/app/router";
import {useTokenRefresh} from "@/features/auth/hooks/useRefreshToken.ts";
import './App.css'

function App() {
    useTokenRefresh();
    return (
        <ReduxProvider store={store}>
            <ApolloProvider client={apolloClient}>
                <AppRouter/>
            </ApolloProvider>
        </ReduxProvider>
    )
}

export default App
