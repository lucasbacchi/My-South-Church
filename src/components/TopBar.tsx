const TopBar = () => {
    return (
        <div className="flex sticky top-0 left-0 h-16 flex-row bg-zinc-600">
            <h1 className="text-white text-2xl p-4">My South Church</h1>
            <div className="flex flex-grow"></div>
            <div className="flex items-center justify-center w-16 h-16 bg-zinc-600">
                <span className="material-symbols-outlined text-white text-2xl">account_circle</span>
            </div>
            {/* <div className="flex items-center justify-center w-16 h-16 bg-zinc-600">
                <span className="material-symbols-outlined text-white text-2xl">search</span>
            </div>
            <div className="flex items-center justify-center w-16 h-16 bg-zinc-600">
                <span className="material-symbols-outlined text-white text-2xl">notifications</span>
            </div> */}
        </div>
    );
};

export default TopBar;
