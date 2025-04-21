import React from 'react';
import {Outlet} from 'react-router-dom';
import Navbar from '@/components/navigation/Navbar.tsx';
import Footer from '@/components/navigation/Footer.tsx';
import {Toaster} from '@/components/ui/sonner';


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