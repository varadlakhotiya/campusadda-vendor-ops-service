const Utils = {
  showMessage(message, type = "success") {
    const host = this.getToastHost();
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;

    const text = document.createElement("span");
    text.textContent = String(message ?? "");

    const closeBtn = document.createElement("button");
    closeBtn.type = "button";
    closeBtn.setAttribute("aria-label", "Dismiss notification");
    closeBtn.textContent = "×";

    closeBtn.addEventListener("click", () => {
      toast.remove();
    });

    toast.appendChild(text);
    toast.appendChild(closeBtn);
    host.appendChild(toast);

    window.setTimeout(() => {
      toast.style.opacity = "0";
      toast.style.transform = "translateY(-8px)";
      toast.style.transition = "all 0.2s ease";
      window.setTimeout(() => toast.remove(), 220);
    }, 2800);
  },

  getToastHost() {
    let host = document.getElementById("toastHost");
    if (!host) {
      host = document.createElement("div");
      host.id = "toastHost";
      host.className = "toast-host";
      document.body.appendChild(host);
    }
    return host;
  },

  setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value ?? "-";
  },

  populateSelect(selectId, items, valueKey, labelKey, defaultLabel = "Select") {
    const select = document.getElementById(selectId);
    if (!select) return;

    select.innerHTML = `<option value="">${defaultLabel}</option>`;
    items.forEach((item) => {
      const option = document.createElement("option");
      option.value = item[valueKey];
      option.textContent = item[labelKey];
      select.appendChild(option);
    });
  },

  formatCurrency(value) {
    return `₹${Number(value || 0).toFixed(2)}`;
  },

  formatDateTime(value) {
    if (!value) return "-";
    return new Date(value).toLocaleString();
  },

  setButtonLoading(buttonOrId, isLoading, loadingText = "Please wait...") {
    const button =
      typeof buttonOrId === "string"
        ? document.getElementById(buttonOrId)
        : buttonOrId;

    if (!button) return;

    if (isLoading) {
      if (!button.dataset.originalText) {
        button.dataset.originalText = button.textContent;
      }
      button.disabled = true;
      button.textContent = loadingText;
    } else {
      button.disabled = false;
      if (button.dataset.originalText) {
        button.textContent = button.dataset.originalText;
        delete button.dataset.originalText;
      }
    }
  }
};