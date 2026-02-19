import { apiRequest, getAuthHeaders } from "./apiClient";

export interface GroupSummary {
    id?: string | null;
    email?: string | null;
    name?: string | null;
    description?: string | null;
}

export interface Group extends GroupSummary {
    id: string;
    email: string;
    name: string;
    description?: string;
}

export interface GroupMember {
    email: string;
    role: "MANAGER" | "MEMBER" | "OWNER";
    status?: string;
}

export interface GroupSettings {
    groupUniqueId?: string;
    email?: string;
    name?: string;
    description?: string;
    allowExternalMembers?: boolean;
    allowWebPosting?: boolean;
    enableCollaborativeInbox?: boolean;
    includeInGlobalAddressList?: boolean;
    isArchived?: boolean;
    membersCanPostAsTheGroup?: boolean;
    messageModerationLevel?: string;
    replyTo?: string;
    sendMessageDenyNotification?: boolean;
    showInGroupDirectory?: boolean;
    spamModerationLevel?: string;
    whoCanAdd?: string;
    whoCanApproveMembers?: string;
    whoCanApproveMessages?: string;
    whoCanContactOwner?: string;
    whoCanDeleteAnyPost?: string;
    whoCanDiscoverGroup?: string;
    whoCanInvite?: string;
    whoCanJoin?: string;
    whoCanLeaveGroup?: string;
    whoCanModerateMembers?: string;
    whoCanModerateContent?: string;
    whoCanModifyMembers?: string;
    whoCanPostAnnouncements?: string;
    whoCanPostMessage?: string;
    whoCanViewGroup?: string;
    whoCanViewMembership?: string;
    customReplyTo?: string;
    defaultMessageDenyNotificationText?: string;
    defaultSender?: string;
    displayName?: string;
    maxMessageBytes?: number;
    messageDisplayFont?: string;
    primaryLanguage?: string;
    archiveOnly?: boolean;
}

const GROUPS_CACHE_TTL_MS = 60_000;

let groupsCache: Group[] | null = null;
let groupsCacheTime = 0;
let groupsInflight: Promise<Group[]> | null = null;

export function peekGroupsCache() {
    return groupsCache;
}

export function clearGroupsCache() {
    groupsCache = null;
    groupsCacheTime = 0;
}

export async function getGroups(options?: { force?: boolean }): Promise<Group[]> {
    const now = Date.now();
    const shouldUseCache = !options?.force && groupsCache !== null && now - groupsCacheTime < GROUPS_CACHE_TTL_MS;

    if (shouldUseCache && groupsCache !== null) {
        return groupsCache;
    }

    if (groupsInflight) {
        return groupsInflight;
    }

    groupsInflight = (async () => {
        const headers = await getAuthHeaders();
        const data = await apiRequest<Group[]>("/groups", { method: "GET", headers });
        groupsCache = data;
        groupsCacheTime = Date.now();
        return data;
    })();

    try {
        return await groupsInflight;
    } finally {
        groupsInflight = null;
    }
}

export async function getGroup(groupId: string): Promise<Group> {
    const headers = await getAuthHeaders();
    return apiRequest<Group>(`/groups/${groupId}`, { method: "GET", headers });
}

export async function getGroupMembers(groupId: string): Promise<GroupMember[]> {
    const headers = await getAuthHeaders();
    return apiRequest<GroupMember[]>(`/groups/${groupId}/members`, { method: "GET", headers });
}

export async function getGroupSettings(groupId: string): Promise<GroupSettings> {
    const headers = await getAuthHeaders();
    return apiRequest<GroupSettings>(`/groups/${groupId}/settings`, { method: "GET", headers });
}

export async function getGroupsForMemberEmail(memberEmail: string): Promise<Group[]> {
    const headers = await getAuthHeaders();
    const encodedEmail = encodeURIComponent(memberEmail);
    return apiRequest<Group[]>(`/members/${encodedEmail}/groups`, { method: "GET", headers });
}
