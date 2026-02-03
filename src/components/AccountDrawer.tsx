import { useContext } from "react";
import { UserContext } from "../contexts/UserContextDefinition";
import { Link } from "react-router";
import { signOutUser } from "../firebase";
import { Sheet, SheetClose, SheetContent, SheetFooter, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

export default function AccountDrawer(props: { onClose: () => void; isOpen: boolean }) {
    const [user, , cachedPhotoURL] = useContext(UserContext);

    if (!user) {
        return null;
    }

    return (
        <Sheet open={props.isOpen} onOpenChange={(open) => !open && props.onClose()}>
            <SheetContent>
                <SheetHeader>
                    <SheetTitle>Your Account</SheetTitle>
                </SheetHeader>
                <div className="flex flex-col gap-4 items-center my-6">
                    <Avatar className="w-24 h-24">
                        <AvatarImage
                            key={cachedPhotoURL ?? user.photoURL ?? "fallback"}
                            src={cachedPhotoURL ?? user.photoURL ?? ""}
                            alt={user.displayName ?? ""}
                        />
                        <AvatarFallback>{user.displayName?.slice(0, 2).toUpperCase() ?? "U"}</AvatarFallback>
                    </Avatar>
                    <div className="text-center">
                        <p className="font-medium">{user.displayName}</p>
                        <p className="text-sm text-muted-foreground">{user.email}</p>
                    </div>
                </div>
                <SheetFooter>
                    <div className="flex flex-col gap-2 w-full">
                        <Button
                            asChild
                            className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
                            onClick={props.onClose}
                        >
                            <Link to="/account">Account Settings</Link>
                        </Button>
                        <Button
                            asChild
                            className="w-full bg-secondary hover:bg-secondary/90 text-secondary-foreground"
                            onClick={props.onClose}
                        >
                            <Link to="/admin/dashboard">Admin Settings</Link>
                        </Button>
                        <Button variant="destructive" className="w-full" onClick={() => void signOutUser()}>
                            Logout
                        </Button>
                        <SheetClose asChild>
                            <Button variant="outline" className="w-full" onClick={props.onClose}>
                                Close
                            </Button>
                        </SheetClose>
                    </div>
                </SheetFooter>
            </SheetContent>
        </Sheet>
    );
}
