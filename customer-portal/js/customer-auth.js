const CustomerAuth = {
  ACCESS_TOKEN_KEY: "campusadda_customer_access_token",
  REFRESH_TOKEN_KEY: "campusadda_customer_refresh_token",
  USER_KEY: "campusadda_customer_user",

  getAccessToken() {
    return window.localStorage.getItem(this.ACCESS_TOKEN_KEY) || "";
  },

  getRefreshToken() {
    return window.localStorage.getItem(this.REFRESH_TOKEN_KEY) || "";
  },

  getStoredUser() {
    try {
      const raw = window.localStorage.getItem(this.USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch (_) {
      return null;
    }
  },

  hasSession() {
    return !!this.getAccessToken();
  },

  setSession(authData) {
    if (!authData) return;
    window.localStorage.setItem(this.ACCESS_TOKEN_KEY, authData.accessToken || "");
    window.localStorage.setItem(this.REFRESH_TOKEN_KEY, authData.refreshToken || "");
    window.localStorage.setItem(this.USER_KEY, JSON.stringify(authData.user || null));
  },

  clearSession() {
    window.localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    window.localStorage.removeItem(this.USER_KEY);
  },

  async login(email, password) {
    const response = await CustomerApi.post(
      "/login",
      { email, password },
      { baseUrl: window.CUSTOMER_APP_CONFIG.AUTH_BASE_URL }
    );
    this.setSession(response.data);
    return response.data;
  },

  async signup(payload) {
    const response = await CustomerApi.post(
      "/signup/customer",
      payload,
      { baseUrl: window.CUSTOMER_APP_CONFIG.AUTH_BASE_URL }
    );
    this.setSession(response.data);
    return response.data;
  },

  async getCurrentUser() {
    const response = await CustomerApi.get(
      "/me",
      { baseUrl: window.CUSTOMER_APP_CONFIG.AUTH_BASE_URL }
    );
    return response.data;
  },

  async logout() {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      try {
        await CustomerApi.post(
          "/logout",
          { refreshToken },
          { baseUrl: window.CUSTOMER_APP_CONFIG.AUTH_BASE_URL }
        );
      } catch (_) {
        // ignore logout API failures
      }
    }
    this.clearSession();
  }
};