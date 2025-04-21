import {ApolloProvider} from "@apollo/client";
import {Provider as ReduxProvider} from "react-redux";
import {store} from "@/app/store.ts";
import {apolloClient} from "@/lib/apollo-client";
import {AppRouter} from "@/app/router";

import './App.css'

function App() {
    return (
        <ReduxProvider store={store}>
            <ApolloProvider client={apolloClient}>
                <AppRouter/>
            </ApolloProvider>
        </ReduxProvider>
    )
}

export default App
