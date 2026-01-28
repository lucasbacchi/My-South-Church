import { Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";

interface SpinnerProps extends React.HTMLAttributes<SVGElement> {
    size?: number;
}

export const Spinner = ({ className, size = 24, ...props }: SpinnerProps) => {
    return <Loader2 className={cn("animate-spin text-muted-foreground", className)} size={size} {...props} />;
};
