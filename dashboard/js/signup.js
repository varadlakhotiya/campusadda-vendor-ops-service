document.getElementById("signupForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  try {
    const payload = {
      restaurantName: document.getElementById("restaurantName").value,
      contactPersonName: document.getElementById("contactPersonName").value,
      contactEmail: document.getElementById("contactEmail").value,
      contactPhone: document.getElementById("contactPhone").value,
      campusArea: document.getElementById("campusArea").value,
      locationLabel: document.getElementById("locationLabel").value,
      password: document.getElementById("password").value,
      notes: document.getElementById("notes").value
    };

    const response = await fetch(`${APP_CONFIG.API_BASE_URL}/public/vendor-signup-requests`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Signup request failed");
    }

    alert("Signup request submitted successfully. CampusAdda admin will review it.");
    window.location.href = "login.html";
  } catch (err) {
    alert(err.message);
  }
});