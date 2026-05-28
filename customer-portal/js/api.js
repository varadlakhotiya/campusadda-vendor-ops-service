const CustomerApi = {
  async request(path, options = {}) {
    const baseUrl = String(options.baseUrl || window.CUSTOMER_APP_CONFIG?.API_BASE_URL || "").replace(/\/+$/, "");
    const cleanPath = String(path || "").startsWith("/") ? path : `/${path}`;

    const headers = {
      Accept: "application/json",
      ...(options.headers || {})
    };

    const accessToken =
      typeof CustomerAuth !== "undefined" && typeof CustomerAuth.getAccessToken === "function"
        ? CustomerAuth.getAccessToken()
        : null;

    if (accessToken && !headers.Authorization) {
      headers.Authorization = `Bearer ${accessToken}`;
    }

    let response;
    try {
      response = await fetch(`${baseUrl}${cleanPath}`, {
        ...options,
        headers
      });
    } catch (error) {
      throw new Error("Unable to connect to the backend. Check whether your API server is running.");
    }

    let data = null;
    try {
      data = await response.json();
    } catch (_) {
      data = null;
    }

    if (!response.ok || data?.success === false) {
      throw new Error(data?.message || `Request failed with status ${response.status}`);
    }

    return data ?? { success: true };
  },

  get(path, options = {}) {
    return this.request(path, {
      ...options,
      method: "GET"
    });
  },

  post(path, body, options = {}) {
    return this.request(path, {
      ...options,
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(options.headers || {})
      },
      body: JSON.stringify(body)
    });
  },

  patch(path, body, options = {}) {
    return this.request(path, {
      ...options,
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        ...(options.headers || {})
      },
      body: JSON.stringify(body)
    });
  }
};