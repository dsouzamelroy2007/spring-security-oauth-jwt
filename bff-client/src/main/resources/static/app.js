function getCookie(name) {
  const match = document.cookie.match(new RegExp("(?:^|; )" + name + "=([^;]*)"));
  return match ? decodeURIComponent(match[1]) : null;
}

// [FEATURE D2] Every non-GET call echoes the readable XSRF-TOKEN cookie back
// as a header -- the standard fetch-based-SPA CSRF pattern this app's
// SpaCsrfTokenRequestHandler on the server side is built to accept.
function csrfHeaders() {
  const token = getCookie("XSRF-TOKEN");
  return token ? { "X-XSRF-TOKEN": token } : {};
}

async function loadWhoAmI() {
  const response = await fetch("/whoami", { headers: { Accept: "application/json" } });
  if (!response.ok) {
    document.getElementById("anonymous-section").hidden = false;
    document.getElementById("authenticated-section").hidden = true;
    return false;
  }
  const whoami = await response.json();
  document.getElementById("whoami-subject").textContent = whoami.subject;
  document.getElementById("whoami-org").textContent = whoami.orgSlug;
  document.getElementById("anonymous-section").hidden = true;
  document.getElementById("authenticated-section").hidden = false;
  return true;
}

async function loadReports() {
  const response = await fetch("/api/reports", { headers: { Accept: "application/json" } });
  if (!response.ok) {
    return;
  }
  const reports = await response.json();
  const list = document.getElementById("report-list");
  list.innerHTML = "";
  for (const report of reports) {
    const item = document.createElement("li");
    item.textContent = `${report.title} (${report.status}, ${report.totalAmount} ${report.currency}) `;
    const approveButton = document.createElement("button");
    approveButton.textContent = "Approve";
    approveButton.addEventListener("click", () => approveReport(report.id));
    item.appendChild(approveButton);
    list.appendChild(item);
  }
}

async function createReport(event) {
  event.preventDefault();
  const form = event.target;
  await fetch("/api/reports", {
    method: "POST",
    headers: { "Content-Type": "application/json", ...csrfHeaders() },
    body: JSON.stringify({
      title: form.title.value,
      description: form.description.value,
      currency: form.currency.value,
    }),
  });
  form.reset();
  await loadReports();
}

async function approveReport(id) {
  await fetch(`/api/reports/${id}/approve`, { method: "POST", headers: csrfHeaders() });
  await loadReports();
}

async function logout(event) {
  event.preventDefault();
  const response = await fetch("/logout", { method: "POST", headers: csrfHeaders() });
  window.location.href = response.redirected ? response.url : "/";
}

document.getElementById("create-report-form").addEventListener("submit", createReport);
document.getElementById("logout-form").addEventListener("submit", logout);

loadWhoAmI().then((authenticated) => {
  if (authenticated) {
    loadReports();
  }
});
