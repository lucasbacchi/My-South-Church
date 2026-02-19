import { apiRequest, getAuthHeaders } from "./apiClient";

export interface GroupSummary {
    id?: string | null;
    email?: string | null;
    name?: string | null;
    description?: string | null;
}

export async function getGroupsForMemberEmail(memberEmail: string): Promise<GroupSummary[]> {
    const headers = await getAuthHeaders();
    const encodedEmail = encodeURIComponent(memberEmail);
    return apiRequest<GroupSummary[]>(`/members/${encodedEmail}/groups`, { method: "GET", headers });
}
