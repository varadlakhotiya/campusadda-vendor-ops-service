const Auth = {
  saveSession(authResponse) {
    localStorage.setItem(APP_CONFIG.TOKEN_KEY, authResponse.accessToken);
    localStorage.setItem(APP_CONFIG.USER_KEY, JSON.stringify(authResponse.user));
  },

  getToken() {
    return localStorage.getItem(APP_CONFIG.TOKEN_KEY);
  },

  getCurrentUser() {
    const raw = localStorage.getItem(APP_CONFIG.USER_KEY);
    return raw ? JSON.parse(raw) : null;
  },

  clearSession() {
    localStorage.removeItem(APP_CONFIG.TOKEN_KEY);
    localStorage.removeItem(APP_CONFIG.USER_KEY);
  },

  isLoggedIn() {
    return !!this.getToken();
  },

  logout() {
    this.clearSession();
    window.location.href = "login.html";
  }
};