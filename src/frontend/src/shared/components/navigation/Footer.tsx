import React from 'react';
import {Link} from 'react-router-dom';

const Footer: React.FC = () => {
    return (
        <footer className="bg-white dark:bg-slate-900 border-t border-border py-6">
            <div className="container mx-auto px-4">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
                    <div className="space-y-4">
                        <h4 className="text-lg font-semibold">GraphQL Template</h4>
                        <p className="text-sm text-muted-foreground">
                            A production-ready Spring Boot application template with JWT authentication,
                            GraphQL API, and MongoDB persistence.
                        </p>
                    </div>

                    <div>
                        <h4 className="text-lg font-semibold mb-4">Links</h4>
                        <ul className="space-y-2">
                            <li>
                                <Link to="/" className="text-sm hover:text-primary">
                                    Home
                                </Link>
                            </li>
                            <li>
                                <Link to="/dashboard" className="text-sm hover:text-primary">
                                    Dashboard
                                </Link>
                            </li>
                            <li>
                                <a
                                    href="http://localhost:8097/graphql"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="text-sm hover:text-primary"
                                >
                                    GraphQL Playground
                                </a>
                            </li>
                        </ul>
                    </div>

                    <div>
                        <h4 className="text-lg font-semibold mb-4">Resources</h4>
                        <ul className="space-y-2">
                            <li>
                                <a
                                    href="https://spring.io/projects/spring-boot"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="text-sm hover:text-primary"
                                >
                                    Spring Boot
                                </a>
                            </li>
                            <li>
                                <a
                                    href="https://spring.io/projects/spring-security"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="text-sm hover:text-primary"
                                >
                                    Spring Security
                                </a>
                            </li>
                            <li>
                                <a
                                    href="https://netflix.github.io/dgs/"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="text-sm hover:text-primary"
                                >
                                    Netflix DGS
                                </a>
                            </li>
                            <li>
                                <a
                                    href="https://reactjs.org/"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="text-sm hover:text-primary"
                                >
                                    React
                                </a>
                            </li>
                        </ul>
                    </div>

                    <div>
                        <h4 className="text-lg font-semibold mb-4">Legal</h4>
                        <ul className="space-y-2">
                            <li>
                                <Link to="/privacy" className="text-sm hover:text-primary">
                                    Privacy Policy
                                </Link>
                            </li>
                            <li>
                                <Link to="/terms" className="text-sm hover:text-primary">
                                    Terms of Service
                                </Link>
                            </li>
                        </ul>
                    </div>
                </div>

                <div className="mt-8 pt-6 border-t border-border text-center">
                    <p className="text-sm text-muted-foreground">
                        &copy; {currentYear} Spring GraphQL Template. All rights reserved.
                    </p>
                </div>
            </div>
        </footer>
    );
}

export default Footer;