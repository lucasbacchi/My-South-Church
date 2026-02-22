/**
 * Global backend health monitoring
 * Checks backend availability and notifies listeners of status changes
 */

type HealthStatus = "checking" | "up" | "down";
type HealthListener = (status: HealthStatus) => void;

// Get the API base URL (same logic as apiClient.ts)
const API_BASE_URL = (() => {
    if (typeof window === "undefined") {
        return "https://api.my.southchurch.com";
    }

    const { hostname } = window.location;
    return hostname === "localhost" || hostname === "127.0.0.1"
        ? "http://localhost:8080"
        : "https://api.my.southchurch.com";
})();

let currentStatus: HealthStatus = "checking";
const listeners = new Set<HealthListener>();
let checkIntervalId: number | undefined;
let consecutiveFailures = 0;
const MAX_CONSECUTIVE_FAILURES = 2; // Declare down after 2 failures
const CHECK_INTERVAL = 30000; // Check every 30 seconds when monitoring

/**
 * Subscribe to health status changes
 */
export function subscribeToHealthStatus(listener: HealthListener): () => void {
    listeners.add(listener);
    // Immediately notify of current status
    listener(currentStatus);

    return () => {
        listeners.delete(listener);
    };
}

/**
 * Notify all listeners of status change
 */
function notifyListeners(status: HealthStatus) {
    currentStatus = status;
    listeners.forEach((listener) => listener(status));
}

/**
 * Get current health status
 */
export function getHealthStatus(): HealthStatus {
    return currentStatus;
}

/**
 * Check backend health with configurable timeout
 */
export async function checkBackendHealth(timeoutMs = 5000): Promise<boolean> {
    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), timeoutMs);

        const response = await fetch(`${API_BASE_URL}/system/health`, {
            cache: "no-store",
            signal: controller.signal,
        });

        clearTimeout(timeoutId);

        if (response.ok) {
            consecutiveFailures = 0;
            // console.log("[Health] Backend is healthy");
            return true;
        }

        consecutiveFailures++;
        console.warn(
            `[Health] Backend returned status ${response.status}, consecutive failures: ${consecutiveFailures}`
        );
        return false;
    } catch (error) {
        consecutiveFailures++;
        console.error(`[Health] Backend check failed, consecutive failures: ${consecutiveFailures}`, error);
        return false;
    }
}

/**
 * Perform initial health check on app startup
 */
export async function performInitialHealthCheck(): Promise<void> {
    // console.log("[Health] Starting initial health check...");
    notifyListeners("checking");

    // Use shorter timeout for initial check (2 seconds)
    const isHealthy = await checkBackendHealth(2000);

    if (isHealthy) {
        // console.log("[Health] Initial check passed - backend is up");
        notifyListeners("up");
    } else {
        // Do one retry with normal timeout before declaring down
        console.log("[Health] Initial check failed, retrying...");
        const retryHealthy = await checkBackendHealth(5000);
        const finalStatus = retryHealthy ? "up" : "down";
        console.log(`[Health] Final status after retry: ${finalStatus}`);
        notifyListeners(finalStatus);
    }
}

async function runHealthCheckInterval(): Promise<void> {
    const isHealthy = await checkBackendHealth();

    if (!isHealthy && consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
        notifyListeners("down");
    } else if (isHealthy && currentStatus === "down") {
        notifyListeners("up");
    }
}

/**
 * Start periodic health monitoring
 * This runs in the background and updates status if backend goes down
 */
export function startHealthMonitoring(): void {
    if (checkIntervalId !== undefined) {
        return; // Already monitoring
    }

    checkIntervalId = window.setInterval(() => {
        void runHealthCheckInterval();
    }, CHECK_INTERVAL);
}

/**
 * Stop periodic health monitoring
 */
export function stopHealthMonitoring(): void {
    if (checkIntervalId !== undefined) {
        clearInterval(checkIntervalId);
        checkIntervalId = undefined;
    }
}

/**
 * Force a health check immediately
 */
export async function forceHealthCheck(): Promise<void> {
    notifyListeners("checking");
    const isHealthy = await checkBackendHealth();
    notifyListeners(isHealthy ? "up" : "down");
}
