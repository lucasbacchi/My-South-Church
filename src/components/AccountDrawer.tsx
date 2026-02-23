import { useContext, useEffect, useState } from "react";
import { UserContext } from "../contexts/UserContextDefinition";
import { Link } from "react-router";
import { signOutUser } from "../firebase";
import { getCurrentUser, roles } from "../lib/api";
import { Sheet, SheetClose, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

export default function AccountDrawer(props: { onClose: () => void; isOpen: boolean }) {
    const [user, , cachedPhotoURL] = useContext(UserContext);
    const [userRoles, setUserRoles] = useState<roles[]>([]);

    useEffect(() => {
        if (user?.uid) {
            void getCurrentUser()
                .then((currentUser) => {
                    if (currentUser) {
                        setUserRoles(currentUser.roles);
                    }
                })
                .catch((error) => {
                    console.error("Failed to fetch user roles:", error);
                });
        }
    }, [user?.uid]);

    if (!user) {
        return null;
    }

    return (
        <Sheet open={props.isOpen} onOpenChange={(open) => !open && props.onClose()}>
            <SheetContent className="flex flex-col gap-0 h-full">
                <SheetHeader className="border-b border-border pb-4 flex">
                    <SheetTitle className="text-2xl p-0">Account</SheetTitle>
                    <SheetDescription>Manage your account settings and preferences</SheetDescription>
                </SheetHeader>

                <div className="flex-1 overflow-auto py-8 px-0">
                    {/* Profile Section */}
                    <div className="px-6 mb-8">
                        <Avatar className="mx-auto mb-4 w-20 h-20 ring-2 shadow-md">
                            <AvatarImage
                                key={cachedPhotoURL ?? user.photoURL ?? "fallback"}
                                src={cachedPhotoURL ?? user.photoURL ?? ""}
                                alt={user.displayName ?? ""}
                            />
                            <AvatarFallback className="bg-linear-to-br from-primary-darker to-secondary-darker text-primary-foreground font-semibold text-2xl">
                                {user.displayName?.slice(0, 2).toUpperCase() ?? "U"}
                            </AvatarFallback>
                        </Avatar>
                        <p className="font-semibold text-foreground text-lg text-center">{user.displayName}</p>
                        <p className="text-sm text-muted-foreground break-all text-center">{user.email}</p>
                        <div className="mt-3 flex flex-wrap gap-2 justify-center">
                            {userRoles.includes(roles.SUPER_ADMIN) && (
                                <div className="inline-flex items-center gap-1 px-2.5 py-1 bg-purple-500/15 border border-purple-500/40 rounded-full text-xs text-purple-700 dark:text-purple-400 font-medium">
                                    Super Admin
                                </div>
                            )}
                            {userRoles.includes(roles.ADMIN) && !userRoles.includes(roles.SUPER_ADMIN) && (
                                <div className="inline-flex items-center gap-1 px-2.5 py-1 bg-blue-500/15 border border-blue-500/40 rounded-full text-xs text-blue-700 dark:text-blue-400 font-medium">
                                    Admin
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Navigation Section */}
                    <div className="space-y-1 px-6">
                        <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide px-2 py-2">
                            Navigation
                        </p>
                        <Link
                            to="/account"
                            onClick={props.onClose}
                            className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-primary text-sm text-foreground font-medium transition-colors"
                        >
                            <span className="material-symbols-outlined text-lg">person</span>
                            Account Settings
                        </Link>
                        <Link
                            to="/admin/dashboard"
                            onClick={props.onClose}
                            className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-primary text-sm text-foreground font-medium transition-colors"
                        >
                            <span className="material-symbols-outlined text-lg">admin_panel_settings</span>
                            Admin Dashboard
                        </Link>
                    </div>
                </div>

                {/* Action Buttons */}
                <div className="border-t border-border pt-6 space-y-2 px-6">
                    <Button
                        variant="destructive"
                        className="w-full font-semibold py-2.5 rounded-lg transition-all"
                        onClick={() => {
                            void signOutUser();
                            props.onClose();
                        }}
                    >
                        <span className="material-symbols-outlined mr-2">logout</span>
                        Sign Out
                    </Button>
                    <SheetClose asChild>
                        <Button
                            variant="outline"
                            className="w-full font-semibold py-2.5 rounded-lg transition-all"
                            onClick={props.onClose}
                        >
                            <span className="material-symbols-outlined mr-2">close</span>
                            Close
                        </Button>
                    </SheetClose>
                </div>
            </SheetContent>
        </Sheet>
    );
}
