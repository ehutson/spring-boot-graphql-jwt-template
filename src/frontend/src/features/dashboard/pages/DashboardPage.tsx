import React from "react";
import {useAuth} from "@/features/auth/hooks/useAuth.ts";

const DashboardPage: React.FC = () => {
    const {user} = useAuth();

    return (
        <div className="space-y-6">
            <div className="bg-white dark:bg-slate-800 rounded-lg shadow p-6">
                <h1 className="text-2xl font-bold">Welcome, {user?.firstName}!</h1>
                <p className="text-muted-foreground mt-2">
                    This is your personal dashboard.
                </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="bg-white dark:bg-slate-800 rounded-lg shadow p-6">
                    <h2 className="text-xl font-semibold mb-4">Account Information</h2>
                    <div className="space-y-2">
                        <div className="flex justify-between">
                            <span className="text-muted-foreground">Username:</span>
                            <span className="font-medium">{user?.username}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-muted-foreground">Email:</span>
                            <span className="font-medium">{user?.email}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-muted-foreground">Full Name:</span>
                            <span className="font-medium">{user?.firstName} {user?.lastName}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-muted-foreground">Roles:</span>
                            <span className="font-medium">
                {user?.roles.map(role => role.name).join(', ')}
              </span>
                        </div>
                    </div>
                </div>

                <div className="bg-white dark:bg-slate-800 rounded-lg shadow p-6">
                    <h2 className="text-xl font-semibold mb-4">Quick Actions</h2>
                    <div className="space-y-4">
                        <button
                            className="w-full bg-primary hover:bg-primary/90 text-white rounded-md py-2 transition-colors">
                            Edit Profile
                        </button>
                        <button
                            className="w-full bg-secondary hover:bg-secondary/90 text-secondary-foreground rounded-md py-2 transition-colors">
                            Change Password
                        </button>
                        <button
                            className="w-full border border-input hover:bg-accent hover:text-accent-foreground rounded-md py-2 transition-colors">
                            Manage Active Sessions
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default DashboardPage;