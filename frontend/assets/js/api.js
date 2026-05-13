/**
 * API Service for CuuTruyen Platform
 * Handles all network requests to the Backend API
 */
const API_CONFIG = {
    BASE_URL: "https://demo-nmcnpm.onrender.com/", // Default Spring Boot port
    TIMEOUT: 5000
};

// Helper function to construct image URLs
window.getImageUrl = (coverUrl) => {
    if (!coverUrl) return null;

    // Explicitly block placeholder/mock URLs
    if (coverUrl.includes('placehold.co') || coverUrl.includes('placeholder')) {
        return null;
    }

    // If it's already a full URL, return as-is
    if (coverUrl.startsWith('http')) return coverUrl;

    const serverUrl = API_CONFIG.BASE_URL.replace('/api', '');
    // If it's a relative path starting with /, prepend server URL
    if (coverUrl.startsWith('/')) {
        return serverUrl + coverUrl;
    }
    // Otherwise it might be "uploads/...", prepend server URL with slash
    return serverUrl + '/' + coverUrl;
};

const getImageUrl = window.getImageUrl;

const ApiService = {
    async fetchWithTimeout(resource, options = {}) {
        const { timeout = API_CONFIG.TIMEOUT } = options;
        const controller = new AbortController();
        const id = setTimeout(() => controller.abort(), timeout);

        try {
            const token = localStorage.getItem('token');
            const headers = { ...options.headers };
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }

            const response = await fetch(API_CONFIG.BASE_URL + resource, {
                ...options,
                headers,
                signal: controller.signal
            });
            clearTimeout(id);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const text = await response.text();
            return text ? JSON.parse(text) : null;
        } catch (error) {
            clearTimeout(id);
            console.error("API Call Failed:", error);
            throw error;
        }
    },

    // MANGA APIs
    async getMangas() {
        const response = await this.fetchWithTimeout("/manga");
        return response.content !== undefined ? response.content : response;
    },

    async getAllMangas(page = 0, size = 20) {
        return this.fetchWithTimeout(`/manga?page=${page}&size=${size}`);
    },

    async getMangasForAdmin(page = 0, size = 50) {
        return this.fetchWithTimeout(`/manga/admin?page=${page}&size=${size}`);
    },

    async getMangasByGroup(groupId) {
        return this.fetchWithTimeout(`/manga/group/${groupId}`);
    },

    async deleteManga(seriesId) {
        return this.fetchWithTimeout(`/manga/${seriesId}`, { method: "DELETE" });
    },

    async getMangaDetail(seriesId) {
        return this.fetchWithTimeout(`/manga/${seriesId}`);
    },

    async getChaptersBySeries(seriesId) {
        return this.fetchWithTimeout(`/chapter/series/${seriesId}`);
    },

    async toggleFavorite(seriesId, isFollowing) {
        return this.fetchWithTimeout(`/manga/${seriesId}/favorite?isFollowing=${isFollowing}`, {
            method: "POST"
        });
    },

    async rateManga(seriesId, rating) {
        return this.fetchWithTimeout(`/manga/${seriesId}/rate?rating=${rating}`, {
            method: "POST"
        });
    },

    async login(username, password) {
        return this.fetchWithTimeout("/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });
    },

    async register(userData) {
        return this.fetchWithTimeout("/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(userData)
        });
    },

    async createManga(mangaData, coverFile) {
        const formData = new FormData();
        formData.append("title", mangaData.title);
        formData.append("alternativeTitle", mangaData.alternativeTitle);
        formData.append("description", mangaData.description);
        formData.append("seriesType", mangaData.seriesType);
        if (coverFile) {
            formData.append("cover", coverFile);
        }

        const token = localStorage.getItem('token');
        const response = await fetch(API_CONFIG.BASE_URL + "/manga", {
            method: "POST",
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    },

    async updateManga(seriesId, mangaData, coverFile) {
        const formData = new FormData();
        formData.append("title", mangaData.title);
        formData.append("alternativeTitle", mangaData.alternativeTitle);
        formData.append("description", mangaData.description);
        formData.append("seriesType", mangaData.seriesType);
        formData.append("status", mangaData.status);
        if (coverFile) {
            formData.append("cover", coverFile);
        }

        const token = localStorage.getItem('token');
        const response = await fetch(API_CONFIG.BASE_URL + "/manga/" + seriesId, {
            method: "PUT",
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    },

    async getMangaDetail(seriesId) {
        return this.fetchWithTimeout(`/manga/${seriesId}`);
    },

    async getChapter(chapterId) {
        return this.fetchWithTimeout(`/chapter/${chapterId}`);
    },

    async createChapter(chapterData) {
        return this.fetchWithTimeout("/chapter", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(chapterData)
        });
    },

    async uploadChapterPages(chapterId, files) {
        const formData = new FormData();
        Array.from(files).forEach(file => {
            formData.append("files", file);
        });

        // Use standard fetch here because fetchWithTimeout handles content-type automatically for JSON
        // but for FormData we want the browser to set it with the boundary
        const token = localStorage.getItem('token');
        const response = await fetch(API_CONFIG.BASE_URL + `/chapter/${chapterId}/upload`, {
            method: "POST",
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        if (!response.ok) throw new Error(`Upload failed! status: ${response.status}`);
        return true;
    },

    async getChapterPages(chapterId) {
        return this.fetchWithTimeout(`/chapter/${chapterId}/pages`);
    },

    async getComments(chapterId) {
        return this.fetchWithTimeout(`/comment/chapter/${chapterId}`);
    },

    async addComment(commentData) {
        return this.fetchWithTimeout("/comment", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(commentData)
        });
    },

    // GROUP APIs
    async getAllGroups() {
        return this.fetchWithTimeout("/groups");
    },

    async getPendingGroups() {
        return this.fetchWithTimeout("/groups/pending");
    },

    async requestGroup(name) {
        return this.fetchWithTimeout("/groups/request", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name })
        });
    },

    async acceptGroup(groupId) {
        return this.fetchWithTimeout(`/groups/${groupId}/accept`, {
            method: "PUT"
        });
    },

    async rejectGroup(groupId) {
        return this.fetchWithTimeout(`/groups/${groupId}/reject`, {
            method: "PUT"
        });
    },

    async deleteGroup(groupId) {
        return this.fetchWithTimeout(`/groups/${groupId}`, {
            method: "DELETE"
        });
    },

    // USER & AUTH APIs
    async login(username, password) {
        return this.fetchWithTimeout("/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });
    },

    async register(userData) {
        return this.fetchWithTimeout("/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(userData)
        });
    },

    async getProfile() {
        return this.fetchWithTimeout("/user/profile");
    },

    async updateUserRole(userId, role) {
        return this.fetchWithTimeout(`/users/${userId}/role`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ role: role })
        });
    },

    // ME APIs (Personal Profile)
    async getMeFavorites() {
        return this.fetchWithTimeout("/me/favorites");
    },

    async toggleFavorite(seriesId) {
        return this.fetchWithTimeout(`/me/favorites/${seriesId}`, {
            method: "POST"
        });
    },

    async getMeHistory() {
        return this.fetchWithTimeout("/me/history");
    },

    async getMeWallet() {
        return this.fetchWithTimeout("/users/me/wallet");
    },

    async getMyWallet() {
        return this.fetchWithTimeout("/users/me/wallet");
    },

    // GROUP APIs
    async getGroupInfo(groupId) {
        return this.fetchWithTimeout(`/groups/${groupId}`);
    },

    async getMyGroup() {
        return this.fetchWithTimeout("/groups/my-group");
    },

    // TRANSACTION APIs
    async getAllTransactions() {
        return this.fetchWithTimeout("/transactions");
    },

    async getPendingWithdrawals() {
        return this.fetchWithTimeout("/transactions/pending-withdrawals");
    },

    async requestWithdrawal(amount, note) {
        return this.fetchWithTimeout("/transactions/withdraw", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ amount, note })
        });
    },

    async approveWithdrawal(txId) {
        return this.fetchWithTimeout(`/transactions/${txId}/approve`, { method: "PUT" });
    },

    async rejectWithdrawal(txId) {
        return this.fetchWithTimeout(`/transactions/${txId}/reject`, { method: "PUT" });
    },

    // MANGA APPROVAL APIs
    async getPendingManga() {
        return this.fetchWithTimeout("/manga/pending");
    },

    async getPendingMangasByGroup(groupId) {
        return this.fetchWithTimeout(`/manga/group/${groupId}/pending`);
    },

    async approveManga(seriesId) {
        return this.fetchWithTimeout(`/manga/${seriesId}/approve`, { method: "PUT" });
    },

    async rejectManga(seriesId) {
        return this.fetchWithTimeout(`/manga/${seriesId}/reject-manga`, { method: "PUT" });
    },

    // CHAPTER UNLOCK APIs
    async checkChapterUnlock(chapterId) {
        return this.fetchWithTimeout(`/chapter/${chapterId}/check-unlock`);
    },

    async unlockChapter(chapterId) {
        return this.fetchWithTimeout(`/chapter/${chapterId}/unlock`, { method: "POST" });
    }
};

window.ApiService = ApiService;
