window.MuseumFront = (function () {
  async function fetchJson(url) {
    const res = await fetch(url);
    if (!res.ok) {
      throw new Error("Request failed: " + res.status);
    }
    return res.json();
  }

  function truncate(text, max) {
    if (!text) return "";
    if (text.length <= max) return text;
    return text.slice(0, max - 1).trimEnd() + "…";
  }

  function createCol() {
    const col = document.createElement("div");
    col.className = "col-md-4";
    return col;
  }

  function renderEventCard(e) {
    const col = createCol();
    const card = document.createElement("article");
    card.className = "event-card";

    const header = document.createElement("div");
    header.className = "event-card-header";
    const meta = document.createElement("div");
    meta.className = "event-meta";
    meta.textContent = `${e.startDate || ""} — ${e.endDate || ""}`;
    const title = document.createElement("h3");
    title.className = "event-title";
    title.textContent = e.title || "Мероприятие";
    header.appendChild(meta);
    header.appendChild(title);

    const body = document.createElement("div");
    body.className = "event-card-body";
    const desc = document.createElement("p");
    desc.textContent = truncate(e.description || "", 160);
    const meta2 = document.createElement("div");
    meta2.className = "d-flex justify-content-between align-items-center mt-2";
    const hall = document.createElement("span");
    hall.className = "text-muted small";
    hall.textContent = e.hall || "";
    const price = document.createElement("span");
    price.className = "event-price small";
    if (e.ticketPrice != null) {
      price.textContent = `${e.ticketPrice} ₽`;
    }
    meta2.appendChild(hall);
    meta2.appendChild(price);

    body.appendChild(desc);
    body.appendChild(meta2);

    card.appendChild(header);
    card.appendChild(body);
    col.appendChild(card);
    return col;
  }

  function renderExhibitionCard(x) {
    const col = createCol();
    const card = document.createElement("article");
    card.className = "exhibition-card";

    const header = document.createElement("div");
    header.className = "event-card-header";
    const meta = document.createElement("div");
    meta.className = "event-meta";
    meta.textContent = x.hall || "";
    const title = document.createElement("h3");
    title.className = "event-title";
    title.textContent = x.title || "Экспозиция";
    header.appendChild(meta);
    header.appendChild(title);

    const body = document.createElement("div");
    body.className = "event-card-body";
    const desc = document.createElement("p");
    desc.textContent = truncate(x.description || "", 160);
    const meta2 = document.createElement("div");
    meta2.className = "d-flex justify-content-between align-items-center mt-2";
    const status = document.createElement("span");
    status.className = "badge-status";
    status.textContent = x.status || "";
    meta2.appendChild(status);

    body.appendChild(desc);
    body.appendChild(meta2);

    card.appendChild(header);
    card.appendChild(body);
    col.appendChild(card);
    return col;
  }

  async function loadEventsPreview(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = "";
    try {
      const data = await fetchJson("/api/events");
      const list = data.slice(0, 3);
      if (!list.length) {
        container.innerHTML = '<p class="text-muted">Список мероприятий пока пуст.</p>';
        return;
      }
      list.forEach(e => container.appendChild(renderEventCard(e)));
    } catch (e) {
      console.error(e);
      container.innerHTML = '<p class="text-muted">Не удалось загрузить мероприятия.</p>';
    }
  }

  async function loadEventsGrid(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = "";
    try {
      const data = await fetchJson("/api/events");
      if (!data.length) {
        container.innerHTML = '<p class="text-muted">Список мероприятий пока пуст.</p>';
        return;
      }
      data.forEach(e => container.appendChild(renderEventCard(e)));
    } catch (e) {
      console.error(e);
      container.innerHTML = '<p class="text-muted">Не удалось загрузить мероприятия.</p>';
    }
  }

  async function loadExhibitionsPreview(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = "";
    try {
      const data = await fetchJson("/api/exhibitions");
      const list = data.slice(0, 3);
      if (!list.length) {
        container.innerHTML = '<p class="text-muted">Список экспозиций пока пуст.</p>';
        return;
      }
      list.forEach(x => container.appendChild(renderExhibitionCard(x)));
    } catch (e) {
      console.error(e);
      container.innerHTML = '<p class="text-muted">Не удалось загрузить экспозиции.</p>';
    }
  }

  async function loadExhibitionsGrid(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = "";
    try {
      const data = await fetchJson("/api/exhibitions");
      if (!data.length) {
        container.innerHTML = '<p class="text-muted">Список экспозиций пока пуст.</p>';
        return;
      }
      data.forEach(x => container.appendChild(renderExhibitionCard(x)));
    } catch (e) {
      console.error(e);
      container.innerHTML = '<p class="text-muted">Не удалось загрузить экспозиции.</p>';
    }
  }

  async function populateTicketEvents(selectId) {
    const select = document.getElementById(selectId);
    if (!select) return;
    select.innerHTML = "";
    try {
      const data = await fetchJson("/api/events");
      if (!data.length) {
        const opt = document.createElement("option");
        opt.disabled = true;
        opt.selected = true;
        opt.textContent = "Нет активных мероприятий";
        select.appendChild(opt);
        select.disabled = true;
        return;
      }
      data.forEach(e => {
        const opt = document.createElement("option");
        opt.value = e.id;
        opt.textContent = e.title || `Мероприятие #${e.id}`;
        select.appendChild(opt);
      });
    } catch (e) {
      console.error(e);
      const opt = document.createElement("option");
      opt.disabled = true;
      opt.selected = true;
      opt.textContent = "Ошибка загрузки списка";
      select.appendChild(opt);
      select.disabled = true;
    }
  }

  function initTicketForm(formId, selectId, nameId, emailId, msgId) {
    const form = document.getElementById(formId);
    const select = document.getElementById(selectId);
    const nameInput = document.getElementById(nameId);
    const emailInput = document.getElementById(emailId);
    const msg = document.getElementById(msgId);
    if (!form || !select || !nameInput || !emailInput || !msg) return;

    form.addEventListener("submit", async function (e) {
      e.preventDefault();
      msg.textContent = "";
      msg.className = "small mt-2";
      if (select.disabled) return;

      const eventId = select.value;
      const payload = {
        buyerName: nameInput.value,
        buyerEmail: emailInput.value
      };

      try {
        const res = await fetch(`/api/events/${eventId}/tickets/purchase`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        if (!res.ok) {
          msg.textContent = "Не удалось оформить заявку. Проверьте данные.";
          msg.classList.add("text-danger");
          return;
        }
        msg.textContent = "Заявка отправлена. Проверьте e‑mail.";
        msg.classList.add("text-success");
        form.reset();
      } catch (err) {
        console.error(err);
        msg.textContent = "Произошла ошибка при оформлении заявки.";
        msg.classList.add("text-danger");
      }
    });
  }

  async function loadFaq(accordionId) {
    const root = document.getElementById(accordionId);
    if (!root) return;
    root.innerHTML = "";
    try {
      const data = await fetchJson("/api/faq");
      if (!data.length) {
        root.innerHTML = '<p class="text-muted">Раздел FAQ пока пуст. Добавьте вопросы в админ‑панели.</p>';
        return;
      }
      data.forEach((f, index) => {
        const itemId = `faq-item-${index}`;
        const headerId = `faq-header-${index}`;
        const collapseId = `faq-collapse-${index}`;

        const item = document.createElement("div");
        item.className = "accordion-item";

        const h2 = document.createElement("h2");
        h2.className = "accordion-header";
        h2.id = headerId;

        const button = document.createElement("button");
        button.className = "accordion-button collapsed";
        button.type = "button";
        button.setAttribute("data-bs-toggle", "collapse");
        button.setAttribute("data-bs-target", "#" + collapseId);
        button.setAttribute("aria-expanded", "false");
        button.setAttribute("aria-controls", collapseId);
        button.textContent = f.question || "Вопрос";

        const collapse = document.createElement("div");
        collapse.id = collapseId;
        collapse.className = "accordion-collapse collapse";
        collapse.setAttribute("aria-labelledby", headerId);
        collapse.setAttribute("data-bs-parent", "#" + accordionId);

        const body = document.createElement("div");
        body.className = "accordion-body";
        body.innerHTML = (f.answer || "").replace(/\n/g, "<br>");

        h2.appendChild(button);
        collapse.appendChild(body);
        item.appendChild(h2);
        item.appendChild(collapse);
        root.appendChild(item);
      });
    } catch (e) {
      console.error(e);
      root.innerHTML = '<p class="text-muted">Не удалось загрузить раздел FAQ.</p>';
    }
  }

  return {
    loadEventsPreview,
    loadEventsGrid,
    loadExhibitionsPreview,
    loadExhibitionsGrid,
    populateTicketEvents,
    initTicketForm,
    loadFaq
  };
})();

