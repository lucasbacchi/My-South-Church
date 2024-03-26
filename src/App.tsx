import { Route, Routes } from "react-router-dom";
import SideNavBar from "./components/SideNavBar";
import HomePage from "./pages/HomePage";
// import AboutPage from "./pages/AboutPage";
// import GroupsPage from "./pages/GroupsPage";
// import DrivePage from "./pages/DrivePage";
import TopBar from "./components/TopBar";
import Footer from "./components/Footer";

function App() {
    return (
        <>
            <TopBar />
            <div className="flex flex-row flex-grow items-stretch">
                <SideNavBar />
                <div className="p-4 flex-grow">
                    <Routes>
                        <Route index path="/" Component={HomePage}></Route>
                        {/* <Route path="/about" Component={AboutPage}></Route>
                        <Route path="/groups" Component={GroupsPage}></Route>
                        <Route path="/drive" Component={DrivePage}></Route> */}
                        <Route path="*" element={<h1>404 Not Found</h1>}></Route>
                    </Routes>
                </div>
            </div>
            <Footer />
        </>
    );
}

export default App;
