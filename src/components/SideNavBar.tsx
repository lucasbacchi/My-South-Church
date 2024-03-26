import { useState } from "react";
import { NavLink, useLocation } from "react-router-dom";

const sideNavBarPages: string[][] = [
    ["Home", "home", "/"]
    // ["About", "info", "/about"],
    // ["Google Groups (Email)", "email", "/groups"],
    // ["Google Drive", "folder", "/drive"]
];

const SideNavBar = () => {
    const location = useLocation();
    const [currentPage, setCurrentPage] = useState(location.pathname);
    const pageName = sideNavBarPages.find((page) => page[2] == location.pathname)?.[0] || "";
    document.title = pageName + " || My South Church";

    // useEffect(() => {
    //     setCurrentPage(location.pathname);
    // }, [location]);

    return (
        <div className="flex flex-col grow top-0 left-0 max-w-40 bg-zinc-700">
            {sideNavBarPages.map((page) => (
                <NavLink
                    key={page[1]}
                    to={page[2]}
                    onClick={() => setCurrentPage(page[2])}
                    className={(page[2] == currentPage ? "active " : "") + "text-white hover:text-green-200"}
                >
                    <div className="side-nav-bar-item p-3 m-1">
                        <span className="material-symbols-outlined block p-2">{page[1]}</span>
                        <span>{page[0]}</span>
                    </div>
                </NavLink>
            ))}
        </div>
    );
};

export default SideNavBar;
