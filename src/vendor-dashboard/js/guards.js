function requireAuth() {
  if (!Auth.isLoggedIn()) {
    window.location.href = "login.html";
  }
}