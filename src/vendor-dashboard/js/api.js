const Api = {
  async request(endpoint, options = {}) {
    const token = Auth.getToken();

    const headers = {
      "Content-Type": "application/json",
      ...(options.headers || {})
    };

    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(`${APP_CONFIG.API_BASE_URL}${endpoint}`, {
      ...options,
      headers
    });

    const contentType = response.headers.get("content-type");
    const data = contentType && contentType.includes("application/json")
      ? await response.json()
      : null;

    if (!response.ok) {
      const message = data?.message || "Request failed";
      throw new Error(message);
    }

    return data;
  },

  get(endpoint) {
    return this.request(endpoint, { method: "GET" });
  },

  post(endpoint, body) {
    return this.request(endpoint, {
      method: "POST",
      body: JSON.stringify(body)
    });
  },

  put(endpoint, body) {
    return this.request(endpoint, {
      method: "PUT",
      body: JSON.stringify(body)
    });
  },

  patch(endpoint, body) {
    return this.request(endpoint, {
      method: "PATCH",
      body: JSON.stringify(body)
    });
  },

  delete(endpoint) {
    return this.request(endpoint, { method: "DELETE" });
  }
};