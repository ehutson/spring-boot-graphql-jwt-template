import React from 'react';

const ProfilePage: React.FC = () => {
    return (
        <div className="flex flex-col items-center justify-center min-h-screen text-center">
            <h1 className="text-9xl font-bold text-primary">Profile</h1>
            <p className="mt-4 text-muted-foreground max-w-md">
                This is the profile page.
            </p>
            {/* Add your profile content here */}
        </div>
    );
}

export default ProfilePage;
