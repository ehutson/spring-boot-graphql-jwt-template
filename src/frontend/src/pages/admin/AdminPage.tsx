import React from 'react';

const AdminPage: React.FC = () => {
    return (
        <div className="flex flex-col items-center justify-center min-h-screen text-center">
            <h1 className="text-9xl font-bold text-primary">Admin</h1>
            <p className="mt-4 text-muted-foreground max-w-md">
                This is the admin page.
            </p>
            {/* Add your admin content here */}
        </div>
    );
}

export default AdminPage;
