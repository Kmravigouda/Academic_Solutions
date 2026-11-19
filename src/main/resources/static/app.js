const moduleGrid = document.querySelector("#module-grid");
const workspace = document.querySelector("#workspace");
const recordForm = document.querySelector("#record-form");
const pageError = document.querySelector("#page-error");
let modules = [];
let currentModule = null;
let records = [];

const icons = ["LC", "GH", "CG", "SP", "AI", "MT", "PS", "CA", "CP", "CM"];

async function request(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: { "Content-Type": "application/json", ...options.headers }
  });
  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const body = await response.json();
      message = body.message || body.error || message;
    } catch {
      // Keep the HTTP status message when the server does not return JSON.
    }
    throw new Error(message);
  }
  if (response.status === 204) return null;
  return response.json();
}

function showPageError(message) {
  pageError.textContent = message;
  pageError.classList.remove("hidden");
}

function renderModules() {
  moduleGrid.replaceChildren();
  modules.forEach((module, index) => {
    const card = document.createElement("article");
    card.className = "module-card";
    card.tabIndex = 0;
    card.setAttribute("role", "button");
    card.setAttribute("aria-label", `Open ${module.title}`);

    const top = document.createElement("div");
    top.className = "module-card-top";
    const icon = document.createElement("span");
    icon.className = "module-icon";
    icon.textContent = icons[index] || "AS";
    const number = document.createElement("span");
    number.className = "module-number";
    number.textContent = `MODULE ${String(index + 1).padStart(2, "0")}`;
    top.append(icon, number);

    const title = document.createElement("h3");
    title.textContent = module.title;
    const description = document.createElement("p");
    description.textContent = module.description;
    const bottom = document.createElement("div");
    bottom.className = "module-card-bottom";
    const action = document.createElement("span");
    action.textContent = "OPEN WORKSPACE ↗";
    const count = document.createElement("span");
    count.className = "record-count";
    count.dataset.moduleCount = module.key;
    count.textContent = "0 records";
    bottom.append(action, count);
    card.append(top, title, description, bottom);

    const open = () => openModule(module);
    card.addEventListener("click", open);
    card.addEventListener("keydown", event => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        open();
      }
    });
    moduleGrid.append(card);
  });
  document.querySelector("#module-count").textContent = `${modules.length} MODULES`;
}

function updateStats(stats) {
  document.querySelector("#record-count").textContent = stats.recordCount;
  document.querySelector("#completed-count").textContent = stats.completedCount;
  document.querySelector("#cgpa-value").textContent = stats.cgpa === "" ? "—" : stats.cgpa;
  document.querySelector("#semester-average").textContent =
    stats.semesterAverage === "" ? "—" : `${stats.semesterAverage}%`;
}

async function refreshDashboard() {
  const [stats, allRecords] = await Promise.all([
    request("/api/stats"),
    request("/api/records")
  ]);
  updateStats(stats);
  const counts = new Map();
  allRecords.forEach(record => counts.set(record.moduleKey, (counts.get(record.moduleKey) || 0) + 1));
  document.querySelectorAll("[data-module-count]").forEach(element => {
    const count = counts.get(element.dataset.moduleCount) || 0;
    element.textContent = `${count} ${count === 1 ? "record" : "records"}`;
  });
}

function openModule(module) {
  currentModule = module;
  document.querySelector("#workspace-title").textContent = module.title;
  document.querySelector("#workspace-description").textContent = module.description;
  document.querySelector("#period-label").firstChild.textContent = module.periodLabel;
  document.querySelector("#metric-label").firstChild.textContent = module.metricLabel;
  document.querySelector("#maximum-label").firstChild.textContent = module.maximumMetricLabel;
  document.querySelector("#weight-label").firstChild.textContent = module.weightLabel;
  document.querySelector("#title-label").firstChild.textContent =
    module.key === "academic-score" ? "Course / subject" : "Record title";
  document.querySelector("#form-message").textContent = "";
  document.querySelector("#form-message").classList.remove("error");
  recordForm.reset();
  workspace.classList.remove("hidden");
  loadRecords();
  workspace.scrollIntoView({ behavior: "smooth", block: "start" });
}

function formatMetric(record) {
  if (record.metric == null) return "";
  if (record.moduleKey === "academic-score") {
    return `${record.metric} / ${record.maximumMetric} · ${record.weight} credits`;
  }
  return `${record.metric} / ${record.maximumMetric}`;
}

function renderRecords() {
  const list = document.querySelector("#records-list");
  list.replaceChildren();
  document.querySelector("#records-total").textContent =
    `${records.length} ${records.length === 1 ? "RECORD" : "RECORDS"}`;
  if (records.length === 0) {
    const empty = document.createElement("div");
    empty.className = "empty-state";
    empty.textContent = "No records yet. Add the first one using the form.";
    list.append(empty);
    return;
  }

  records.forEach(record => {
    const item = document.createElement("article");
    item.className = "record-item";
    const main = document.createElement("div");
    main.className = "record-main";
    const title = document.createElement("p");
    title.className = "record-title";
    title.textContent = record.title;
    const meta = document.createElement("p");
    meta.className = "record-meta";
    meta.textContent = [record.studentName, record.studentId, record.period, record.details]
      .filter(Boolean).join(" · ");
    const status = document.createElement("span");
    status.className = "record-status";
    status.textContent = record.status;
    main.append(title, meta, status);
    const side = document.createElement("div");
    const metric = document.createElement("span");
    metric.className = "record-score";
    metric.textContent = formatMetric(record);
    const remove = document.createElement("button");
    remove.type = "button";
    remove.className = "delete-button";
    remove.textContent = "Delete";
    remove.setAttribute("aria-label", `Delete ${record.title}`);
    remove.addEventListener("click", () => deleteRecord(record.id));
    side.append(metric, remove);
    item.append(main, side);
    list.append(item);
  });
}

async function loadRecords() {
  if (!currentModule) return;
  try {
    records = await request(`/api/records?module=${encodeURIComponent(currentModule.key)}`);
    renderRecords();
  } catch (error) {
    showPageError(error.message);
  }
}

async function deleteRecord(id) {
  if (!window.confirm("Delete this record?")) return;
  try {
    await request(`/api/records/${id}`, { method: "DELETE" });
    await Promise.all([loadRecords(), refreshDashboard()]);
  } catch (error) {
    showPageError(error.message);
  }
}

recordForm.addEventListener("submit", async event => {
  event.preventDefault();
  if (!currentModule) return;
  const formMessage = document.querySelector("#form-message");
  const formData = new FormData(recordForm);
  const metric = formData.get("metric");
  const maximumMetric = formData.get("maximumMetric");
  const payload = {
    moduleKey: currentModule.key,
    studentId: formData.get("studentId"),
    studentName: formData.get("studentName"),
    title: formData.get("title"),
    period: formData.get("period"),
    details: formData.get("details"),
    metric: metric === "" ? null : Number(metric),
    maximumMetric: maximumMetric === "" ? null : Number(maximumMetric),
    weight: Number(formData.get("weight")),
    status: formData.get("status")
  };
  const submitButton = recordForm.querySelector("button[type=submit]");
  submitButton.disabled = true;
  formMessage.textContent = "";
  formMessage.classList.remove("error");
  try {
    await request("/api/records", { method: "POST", body: JSON.stringify(payload) });
    recordForm.reset();
    formMessage.textContent = "Record saved.";
    await Promise.all([loadRecords(), refreshDashboard()]);
  } catch (error) {
    formMessage.textContent = error.message;
    formMessage.classList.add("error");
  } finally {
    submitButton.disabled = false;
  }
});

document.querySelector("#close-workspace").addEventListener("click", () => {
  workspace.classList.add("hidden");
  currentModule = null;
});

async function start() {
  try {
    modules = await request("/api/modules");
    renderModules();
    await refreshDashboard();
  } catch (error) {
    showPageError(`Could not load the dashboard: ${error.message}`);
  }
}

start();
