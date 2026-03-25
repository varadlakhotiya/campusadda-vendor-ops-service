const Utils = {
  showMessage(message, type = "success") {
    alert(`${type.toUpperCase()}: ${message}`);
  },

  setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value ?? "-";
  },

  populateSelect(selectId, items, valueKey, labelKey, defaultLabel = "Select") {
    const select = document.getElementById(selectId);
    if (!select) return;

    select.innerHTML = `<option value="">${defaultLabel}</option>`;
    items.forEach(item => {
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
  }
};