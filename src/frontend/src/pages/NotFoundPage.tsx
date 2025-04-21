import React from "react";
import {Link} from "react-router-dom";
import {Button} from "@/components/ui/button";

const NotFoundPage: React.FC = () => {
    return (
        <div className="flex flex-col items-center justify-center min-h-screen text-center">
            <h1 className="text-9xl font-bold text-primary">404</h1>
            <h2 className="mt-6 text-3xl font-bold">Page Not Found</h2>
            <p className="mt-4 text-muted-foreground max-w-md">
                The page you are looking for does not exist. It might have been removed, or you may have entered the
                wrong URL.
            </p>
            <div className="mt-8">
                <Button asChild>
                    <Link to="/">Return Home</Link>
                </Button>
            </div>
        </div>
    );
}

export default NotFoundPage;