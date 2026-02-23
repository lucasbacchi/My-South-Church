import { useEffect, useRef, useState } from "react";
import PrimaryNav from "./PrimaryNav";

export default function SideNavBar() {
    const [showTopShadow, setShowTopShadow] = useState(false);
    const [showBottomShadow, setShowBottomShadow] = useState(false);
    const scrollContainerRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const element = scrollContainerRef.current;
        if (!element) return;

        const handleScroll = () => {
            const isScrolled = element.scrollTop > 0;
            const isAtBottom = element.scrollHeight - element.clientHeight <= element.scrollTop + 1;
            const hasScroll = element.scrollHeight > element.clientHeight;

            setShowTopShadow(isScrolled && hasScroll);
            setShowBottomShadow(!isAtBottom && hasScroll);
        };

        element.addEventListener("scroll", handleScroll);
        handleScroll();

        return () => element.removeEventListener("scroll", handleScroll);
    }, []);

    const boxShadowParts: string[] = [];
    if (showTopShadow) boxShadowParts.push("inset 0 12px 10px 0px rgba(0, 0, 0, 0.3)");
    if (showBottomShadow) boxShadowParts.push("inset 0 -12px 16px 0px rgba(0, 0, 0, 0.3)");

    return (
        <aside
            ref={scrollContainerRef}
            className="hidden bg-card/95 md:flex flex-col fixed top-16 sm:top-20 left-0 bottom-0 w-48 md:w-56 lg:w-60 border-r border-border py-4 px-3 overflow-y-auto z-30 scrollbar-thumb-border scrollbar-track-transparent scroll-smooth"
            style={
                {
                    boxShadow: boxShadowParts.join(", ") || "none",
                    transition: "box-shadow 0.15s ease",
                } as React.CSSProperties
            }
        >
            <PrimaryNav variant="side" />
        </aside>
    );
}
