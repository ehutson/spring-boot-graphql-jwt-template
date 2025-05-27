import React, {useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {useAuth} from '@/features/auth/hooks/useAuth.ts';
import {Button} from '@/shared/components/ui/button.tsx';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu.tsx';
import {LogOut, Menu, Settings, Shield, User, X} from 'lucide-react';
import {Avatar, AvatarFallback} from '@/shared/components/ui/avatar.tsx';

const Navbar: React.FC = () => {
    const {user, isAuthenticated, isAdmin, logout} = useAuth();
    const navigate = useNavigate();
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

    const handleLogout = async () => {
        logout();
        navigate('/login');
    };

    const getInitials = (name: string) => {
        if (!name) return 'U';
        return name
            .split(' ')
            .map((n) => n[0])
            .join('')
            .toUpperCase()
            .substring(0, 2);
    };

    return (
        <nav className="bg-white dark:bg-slate-900 shadow-sm border-b border-border">
            <div className="mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">
                    <div className="flex items-center">
                        <Link to="/" className="flex-shrink-0">
                            <h1 className="text-xl font-bold text-primary">GraphQL App</h1>
                        </Link>

                        {/* Desktop Navigation */}
                        <div className="hidden md:block ml-10">
                            <div className="flex items-center space-x-4">
                                <Link to="/"
                                      className="px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-100 dark:hover:bg-slate-800">
                                    Home
                                </Link>

                                {isAuthenticated && (
                                    <Link to="/dashboard"
                                          className="px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-100 dark:hover:bg-slate-800">
                                        Dashboard
                                    </Link>
                                )}

                                {isAdmin && (
                                    <Link to="/admin"
                                          className="px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-100 dark:hover:bg-slate-800">
                                        Admin
                                    </Link>
                                )}
                            </div>
                        </div>
                    </div>

                    <div className="hidden md:block">
                        <div className="flex items-center">
                            {isAuthenticated ? (
                                <DropdownMenu>
                                    <DropdownMenuTrigger asChild>
                                        <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                                            <Avatar>
                                                <AvatarFallback>
                                                    {getInitials(`${user?.firstName} ${user?.lastName}`)}
                                                </AvatarFallback>
                                            </Avatar>
                                        </Button>
                                    </DropdownMenuTrigger>
                                    <DropdownMenuContent className="w-56" align="end">
                                        <DropdownMenuLabel className="font-normal">
                                            <div className="flex flex-col space-y-1">
                                                <p className="text-sm font-medium leading-none">{user?.firstName} {user?.lastName}</p>
                                                <p className="text-xs leading-none text-muted-foreground">{user?.email}</p>
                                            </div>
                                        </DropdownMenuLabel>
                                        <DropdownMenuSeparator/>
                                        <DropdownMenuItem asChild>
                                            <Link to="/profile" className="flex items-center cursor-pointer">
                                                <User className="mr-2 h-4 w-4"/>
                                                <span>Profile</span>
                                            </Link>
                                        </DropdownMenuItem>
                                        {isAdmin && (
                                            <DropdownMenuItem asChild>
                                                <Link to="/admin" className="flex items-center cursor-pointer">
                                                    <Shield className="mr-2 h-4 w-4"/>
                                                    <span>Admin Dashboard</span>
                                                </Link>
                                            </DropdownMenuItem>
                                        )}
                                        <DropdownMenuItem asChild>
                                            <Link to="/settings" className="flex items-center cursor-pointer">
                                                <Settings className="mr-2 h-4 w-4"/>
                                                <span>Settings</span>
                                            </Link>
                                        </DropdownMenuItem>
                                        <DropdownMenuSeparator/>
                                        <DropdownMenuItem onClick={handleLogout} className="cursor-pointer">
                                            <LogOut className="mr-2 h-4 w-4"/>
                                            <span>Log out</span>
                                        </DropdownMenuItem>
                                    </DropdownMenuContent>
                                </DropdownMenu>
                            ) : (
                                <div className="flex space-x-4">
                                    <Button asChild variant="ghost">
                                        <Link to="/login">Log in</Link>
                                    </Button>
                                    <Button asChild>
                                        <Link to="/register">Sign up</Link>
                                    </Button>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Mobile menu button */}
                    <div className="flex md:hidden">
                        <button
                            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                            className="inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-primary hover:bg-gray-100 dark:hover:bg-gray-800 focus:outline-none"
                        >
                            <span className="sr-only">Open main menu</span>
                            {mobileMenuOpen ? (
                                <X className="block h-6 w-6"/>
                            ) : (
                                <Menu className="block h-6 w-6"/>
                            )}
                        </button>
                    </div>
                </div>
            </div>

            {/* Mobile menu */}
            {mobileMenuOpen && (
                <div className="md:hidden">
                    <div className="px-2 pt-2 pb-3 space-y-1 border-t border-gray-200 dark:border-gray-700">
                        <Link
                            to="/"
                            className="block px-3 py-2 rounded-md text-base font-medium hover:bg-gray-100 dark:hover:bg-gray-800"
                            onClick={() => setMobileMenuOpen(false)}
                        >
                            Home
                        </Link>

                        {isAuthenticated && (
                            <Link
                                to="/dashboard"
                                className="block px-3 py-2 rounded-md text-base font-medium hover:bg-gray-100 dark:hover:bg-gray-800"
                                onClick={() => setMobileMenuOpen(false)}
                            >
                                Dashboard
                            </Link>
                        )}

                        {isAdmin && (
                            <Link
                                to="/admin"
                                className="block px-3 py-2 rounded-md text-base font-medium hover:bg-gray-100 dark:hover:bg-gray-800"
                                onClick={() => setMobileMenuOpen(false)}
                            >
                                Admin
                            </Link>
                        )}

                        {isAuthenticated ? (
                            <>
                                <Link
                                    to="/profile"
                                    className="block px-3 py-2 rounded-md text-base font-medium hover:bg-gray-100 dark:hover:bg-gray-800"
                                    onClick={() => setMobileMenuOpen(false)}
                                >
                                    Profile
                                </Link>
                                <button
                                    onClick={() => {
                                        handleLogout();
                                        setMobileMenuOpen(false);
                                    }}
                                    className="w-full text-left block px-3 py-2 rounded-md text-base font-medium hover:bg-gray-100 dark:hover:bg-gray-800"
                                >
                                    Log out
                                </button>
                            </>
                        ) : (
                            <>
                                <Link
                                    to="/login"
                                    className="block px-3 py-2 rounded-md text-base font-medium hover:bg-gray-100 dark:hover:bg-gray-800"
                                    onClick={() => setMobileMenuOpen(false)}
                                >
                                    Log in
                                </Link>
                                <Link
                                    to="/register"
                                    className="block px-3 py-2 rounded-md text-base font-medium hover:bg-gray-100 dark:hover:bg-gray-800"
                                    onClick={() => setMobileMenuOpen(false)}
                                >
                                    Sign up
                                </Link>
                            </>
                        )}
                    </div>
                </div>
            )}
        </nav>
    );
};

export default Navbar;