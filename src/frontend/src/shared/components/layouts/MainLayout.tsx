import React from 'react';
import {Outlet} from 'react-router-dom';
import Navbar from '@/shared/components/navigation/Navbar.tsx';
import Footer from '@/shared/components/navigation/Footer.tsx';
import {Toaster} from '@/shared/components/ui/sonner.tsx';


const MainLayout: React.FC = () => {
    return (
        <div className="flex flex-col min-h-screen">
            <Navbar/>
            <main className="flex-grow">
                <Outlet/>
            </main>
            <Footer/>
            <Toaster position="top-right"/>
        </div>
    );
};

export default MainLayout;