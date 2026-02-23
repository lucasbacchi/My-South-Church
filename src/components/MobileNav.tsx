import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import PrimaryNav from "./PrimaryNav";

export default function MobileNav() {
    const [isOpen, setIsOpen] = useState(false);
    const closeMenu = () => setIsOpen(false);

    return (
        <div className="md:hidden">
            <Sheet open={isOpen} onOpenChange={setIsOpen}>
                <SheetTrigger asChild>
                    <Button
                        variant="ghost"
                        size="icon"
                        className="w-12 h-12 sm:w-14 sm:h-14 self-center text-foreground bg-transparent hover:bg-transparent active:bg-transparent shadow-none"
                        aria-label="Toggle menu"
                    >
                        <span className="material-symbols-outlined text-xl sm:text-2xl">
                            {isOpen ? "close" : "menu"}
                        </span>
                    </Button>
                </SheetTrigger>
                <SheetContent side="left" className="w-72 p-0 flex flex-col gap-0">
                    <SheetHeader className="border-b border-border px-4 py-4 shrink-0">
                        <SheetTitle className="text-base">Navigation</SheetTitle>
                        <SheetDescription>Browse the site and your pages.</SheetDescription>
                    </SheetHeader>
                    <div className="p-4 overflow-y-auto">
                        <PrimaryNav variant="mobile" onNavigate={closeMenu} />
                    </div>
                </SheetContent>
            </Sheet>
        </div>
    );
}
