import {gql} from '@apollo/client';

export const LOGIN_MUTATION = gql`
    mutation Login($input: LoginInput!) {
        login(input: $input) {
            success
            message
            user {
                id
                username
                email
                firstName
                lastName
                roles {
                    id
                    name
                }
                activated
            }
        }
    }
`;

export const REGISTER_MUTATION = gql`
    mutation Register($input: RegisterInput!) {
        register(input: $input) {
            success
            message
            user {
                id
                username
                email
                firstName
                lastName
                roles {
                    id
                    name
                }
                activated
            }
        }
    }
`;

export const LOGOUT_MUTATION = gql`
    mutation Logout {
        logout
    }
`;

export const ME_QUERY = gql`
    query Me {
        me {
            id
            username
            email
            firstName
            lastName
            roles {
                id
                name
            }
            activated
        }
    }
`;