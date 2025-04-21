import React from 'react';
import {Link} from 'react-router-dom';
import {Button} from '@/components/ui/button';
import {useAuth} from '@/hooks/useAuth';

const HomePage: React.FC = () => {
    const {isAuthenticated} = useAuth();

    return (
        <div className="space-y-20 py-10">
            {/* Hero Section */}
            <section className="text-center space-y-6">
                <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold">
                    Spring Boot GraphQL JWT Template
                </h1>
                <p className="text-xl text-muted-foreground max-w-3xl mx-auto">
                    A production-ready template with JWT authentication, GraphQL API, and MongoDB persistence.
                </p>
                <div className="flex justify-center gap-4">
                    {isAuthenticated ? (
                        <Button asChild size="lg">
                            <Link to="/dashboard">Go to Dashboard</Link>
                        </Button>
                    ) : (
                        <>
                            <Button asChild size="lg">
                                <Link to="/register">Get Started</Link>
                            </Button>
                            <Button asChild size="lg" variant="outline">
                                <Link to="/login">Sign In</Link>
                            </Button>
                        </>
                    )}
                </div>
            </section>

            {/* Features Section */}
            <section>
                <h2 className="text-3xl font-bold text-center mb-10">Key Features</h2>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    <div className="bg-white dark:bg-slate-800 rounded-lg shadow p-6">
                        <h3 className="text-xl font-semibold mb-2">GraphQL API</h3>
                        <p className="text-muted-foreground">
                            Modern API design with Netflix DGS GraphQL framework for efficient data fetching.
                        </p>
                    </div>

                    <div className="bg-white dark:bg-slate-800 rounded-lg shadow p-6">
                        <h3 className="text-xl font-semibold mb-2">Secure Authentication</h3>
                        <p className="text-muted-foreground">
                            JWT-based authentication with HTTP-only cookies and refresh token rotation.
                        </p>
                    </div>

                    <div className="bg-white dark:bg-slate-800 rounded-lg shadow p-6">
                        <h3 className="text-xl font-semibold mb-2">Modern Stack</h3>
                        <p className="text-muted-foreground">
                            Spring Boot, React, MongoDB, and more - all configured and ready to use.
                        </p>
                    </div>
                </div>
            </section>

            {/* Technology Stack Section */}
            <section>
                <h2 className="text-3xl font-bold text-center mb-10">Technology Stack</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div className="bg-white dark:bg-slate-800 rounded-lg shadow p-6">
                        <h3 className="text-xl font-semibold mb-4">Backend</h3>
                        <ul className="space-y-2">
                            <li className="flex items-center">
                                <span className="w-4 h-4 rounded-full bg-green-500 mr-2"></span>
                                <span>Java 21 with modern language features</span>
                            </li>
                            <li className="flex items-center">
                                <span className="w-4 h-4 rounded-full bg-green-500 mr-2"></span>
                                <span>Spring Boot 3.4.3 with Spring Security 6</span>
                            </li>
                            <li className="flex items-center">
                                <span className="w-4 h-4 rounded-full bg-green-500 mr-2"></span>
                                <span>Netflix DGS GraphQL framework</span>
                            </li>
                            <li className="flex items-center">
                                <span className="w-4 h-4 rounded-full bg-green-500 mr-2"></span>
                                <span>MongoDB for data persistence</span>
                            </li>
                            <li className="flex items-center">
                                <span className="w-4 h-4 rounded-full bg-green-500 mr-2"></span>
                                <span>JWT authentication with RSA key-pair signatures</span>
                            </li>
                        </ul>
                    </div>

                    <div className="bg-white dark:bg-slate-800 rounded-lg shadow p-6">
                        <h3 className="text-xl font-semibold mb-4">Frontend</h3>
                        <ul className="space-y-2">
                            <li className="flex items-center">
                                <span className="w-4 h-4 rounded-full bg-blue-500 mr-2"></span>
                                <span>React 19 with TypeScript</span>
                            </li>
                            <li className="flex items-center">
                                <span className="w-4 h-4 rounded-full bg-blue-500 mr-2"></span>
                                <span>Apollo Client for GraphQL integration</span>
                            </li>
                            <li className="flex items-center">
                                <span className="w-4 h-4 rounded-full bg-blue-500 mr-2"></span>
                                <span>Redux Toolkit for state management</span>
                            </li>
                            <li className="flex items-center">
                                <span className="w-4 h-4 rounded-full bg-blue-500 mr-2"></span>
                                <span>Tailwind CSS with shadcn/ui components</span>
                            </li>
                            <li className="flex items-center">
                                <span className="w-4 h-4 rounded-full bg-blue-500 mr-2"></span>
                                <span>Vite for lightning-fast builds</span>
                            </li>
                        </ul>
                    </div>
                </div>
            </section>

            {/* Call to Action */}
            <section className="text-center space-y-6 py-10">
                <h2 className="text-3xl font-bold">Ready to Get Started?</h2>
                <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
                    Start building your secure, modern application today with our production-ready template.
                </p>
                <div>
                    {isAuthenticated ? (
                        <Button asChild size="lg">
                            <Link to="/dashboard">Go to Dashboard</Link>
                        </Button>
                    ) : (
                        <Button asChild size="lg">
                            <Link to="/register">Create Account</Link>
                        </Button>
                    )}
                </div>
            </section>
        </div>
    );
};

export default HomePage;