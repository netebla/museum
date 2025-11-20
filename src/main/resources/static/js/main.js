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

  function getQueryParam(name) {
    const params = new URLSearchParams(window.location.search);
    return params.get(name);
  }

  function formatDateRange(start, end) {
    if (!start && !end) return "";
    if (start && !end) return start;
    if (!start && end) return end;
    if (start === end) return start;
    return `${start} — ${end}`;
  }

  function isPastEvent(e) {
    if (!e.endDate) return false;
    const today = new Date().toISOString().slice(0, 10);
    return e.endDate < today;
  }

  function isUpcomingEvent(e) {
    if (!e.startDate) return true;
    const today = new Date().toISOString().slice(0, 10);
    return e.endDate >= today;
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

     if (e.imageUrl) {
       const imgWrap = document.createElement("div");
       imgWrap.className = "event-card-image";
       const img = document.createElement("img");
       img.src = e.imageUrl;
       img.alt = e.title || "Мероприятие";
       imgWrap.appendChild(img);
       card.appendChild(imgWrap);
     }

    const header = document.createElement("div");
    header.className = "event-card-header";
    const meta = document.createElement("div");
    meta.className = "event-meta";
    meta.textContent = formatDateRange(e.startDate, e.endDate);
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
    } else {
      price.textContent = "Вход свободный / по регистрации";
    }
    meta2.appendChild(hall);
    meta2.appendChild(price);

    body.appendChild(desc);
    body.appendChild(meta2);

    card.appendChild(header);
    const footer = document.createElement("div");
    footer.className = "d-flex justify-content-between align-items-center px-3 pb-3";
    const link = document.createElement("a");
    link.href = `/event.html?id=${e.id}`;
    link.className = "small text-decoration-none";
    link.textContent = "Подробнее";
    const btn = document.createElement("a");
    btn.href = `/tickets.html?eventId=${e.id}`;
    btn.className = "btn btn-sm btn-outline-dark";
    btn.textContent = "Купить билет";
    footer.appendChild(link);
    footer.appendChild(btn);

    card.appendChild(body);
    card.appendChild(footer);
    col.appendChild(card);
    return col;
  }

  function renderExhibitionCard(x) {
    const col = createCol();
    const card = document.createElement("article");
    card.className = "exhibition-card";

    if (x.imageUrl) {
      const imgWrap = document.createElement("div");
      imgWrap.className = "exhibition-card-image";
      const img = document.createElement("img");
      img.src = x.imageUrl;
      img.alt = x.title || "Экспозиция";
      imgWrap.appendChild(img);
      card.appendChild(imgWrap);
    }

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

    const footer = document.createElement("div");
    footer.className = "d-flex justify-content-between align-items-center px-3 pb-3";
    const link = document.createElement("a");
    link.href = `/exhibition.html?id=${x.id}`;
    link.className = "small text-decoration-none";
    link.textContent = "Подробнее";
    const btn = document.createElement("a");
    btn.href = "/tickets.html";
    btn.className = "btn btn-sm btn-outline-dark";
    btn.textContent = "Купить билет";
    footer.appendChild(link);
    footer.appendChild(btn);

    card.appendChild(header);
    card.appendChild(body);
    card.appendChild(footer);
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

  async function initHeroCarousel(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = "";
    try {
      const data = await fetchJson("/api/events");
      const list = data.slice(0, 3);
      if (!list.length) {
        container.innerHTML =
          '<div class="carousel-item active"><div class="hero-slide-inner"><div class="hero-slide-meta">Афиша</div><div class="hero-slide-title">Скоро здесь появятся события музея</div><p class="hero-slide-text">Добавьте мероприятия через админ‑панель, чтобы показать их на главной.</p></div></div>';
        return;
      }
      list.forEach((e, index) => {
        const item = document.createElement("div");
        item.className = "carousel-item" + (index === 0 ? " active" : "");
        const inner = document.createElement("div");
        inner.className = "hero-slide-inner";
        const meta = document.createElement("div");
        meta.className = "hero-slide-meta";
        meta.textContent = formatDateRange(e.startDate, e.endDate);
        const title = document.createElement("div");
        title.className = "hero-slide-title";
        title.textContent = e.title || "Мероприятие";
        const text = document.createElement("p");
        text.className = "hero-slide-text";
        text.textContent = truncate(e.description || "", 100);
        const link = document.createElement("a");
        link.href = `/event.html?id=${e.id}`;
        link.className = "btn btn-sm btn-light mt-2";
        link.textContent = "Подробнее";
        inner.appendChild(meta);
        inner.appendChild(title);
        inner.appendChild(text);
        inner.appendChild(link);
        item.appendChild(inner);
        container.appendChild(item);
      });
    } catch (e) {
      console.error(e);
    }
  }

  async function initEventsPage(listId, dateFilterId, typeFilterId, resetId) {
    const container = document.getElementById(listId);
    const dateFilter = document.getElementById(dateFilterId);
    const typeFilter = document.getElementById(typeFilterId);
    const resetBtn = document.getElementById(resetId);
    if (!container || !dateFilter || !typeFilter || !resetBtn) return;
    const searchInput = document.getElementById("events-search");
    const sortSelect = document.getElementById("events-sort");

    let events = [];

    function render() {
      container.innerHTML = "";
      if (!events.length) {
        container.innerHTML = '<p class="text-muted">Список мероприятий пока пуст.</p>';
        return;
      }
      const dateValue = dateFilter.value;
      const typeValue = typeFilter.value;
      const query = searchInput ? (searchInput.value || "").toLowerCase() : "";
      const sortValue = sortSelect ? sortSelect.value : "date";
      let list = events.slice();
      if (dateValue === "upcoming") {
        list = list.filter(isUpcomingEvent);
      } else if (dateValue === "past") {
        list = list.filter(isPastEvent);
      }
      // Тип события пока заглушка — можно фильтровать по ключевым словам в названии
      if (typeValue !== "all") {
        const keywords = {
          lecture: ["лекция", "talk"],
          screening: ["сеанс", "film", "показ"],
          tour: ["экскурсия", "тур"]
        }[typeValue] || [];
        list = list.filter(e =>
          keywords.some(k => (e.title || "").toLowerCase().includes(k))
        );
      }
      if (query) {
        list = list.filter(e => {
          const t = (e.title || "").toLowerCase();
          const d = (e.description || "").toLowerCase();
          return t.includes(query) || d.includes(query);
        });
      }
      list.sort((a, b) => {
        if (sortValue === "title") {
          return (a.title || "").localeCompare(b.title || "", "ru");
        }
        if (sortValue === "price_asc" || sortValue === "price_desc") {
          const pa = a.ticketPrice != null ? Number(a.ticketPrice) : Infinity;
          const pb = b.ticketPrice != null ? Number(b.ticketPrice) : Infinity;
          const base = pa - pb;
          return sortValue === "price_asc" ? base : -base;
        }
        // default: по дате начала
        const da = a.startDate || "";
        const db = b.startDate || "";
        return da.localeCompare(db);
      });
      if (!list.length) {
        container.innerHTML = '<p class="text-muted">По выбранным фильтрам мероприятий не найдено.</p>';
        return;
      }
      list.forEach(e => container.appendChild(renderEventCard(e)));
    }

    try {
      events = await fetchJson("/api/events");
      render();
    } catch (e) {
      console.error(e);
      container.innerHTML = '<p class="text-muted">Не удалось загрузить мероприятия.</p>';
    }

    dateFilter.addEventListener("change", render);
    typeFilter.addEventListener("change", render);
    if (searchInput) {
      searchInput.addEventListener("input", render);
    }
    if (sortSelect) {
      sortSelect.addEventListener("change", render);
    }
    resetBtn.addEventListener("click", function () {
      dateFilter.value = "all";
      typeFilter.value = "all";
      if (searchInput) searchInput.value = "";
      if (sortSelect) sortSelect.value = "date";
      render();
    });
  }

  async function initExhibitionsPage(listId, hallFilterId, statusFilterId, resetId) {
    const container = document.getElementById(listId);
    const hallFilter = document.getElementById(hallFilterId);
    const statusFilter = document.getElementById(statusFilterId);
    const resetBtn = document.getElementById(resetId);
    if (!container || !hallFilter || !statusFilter || !resetBtn) return;
    const searchInput = document.getElementById("exh-search");
    const sortSelect = document.getElementById("exh-sort");

    let exhibitions = [];

    function render() {
      container.innerHTML = "";
      if (!exhibitions.length) {
        container.innerHTML = '<p class="text-muted">Список экспозиций пока пуст.</p>';
        return;
      }
      const hallValue = hallFilter.value;
      const statusValue = statusFilter.value;
      const query = searchInput ? (searchInput.value || "").toLowerCase() : "";
      const sortValue = sortSelect ? sortSelect.value : "title";
      let list = exhibitions.slice();
      if (hallValue !== "all") {
        list = list.filter(x => (x.hall || "") === hallValue);
      }
      if (statusValue !== "all") {
        list = list.filter(x => (x.status || "") === statusValue);
      }
      if (query) {
        list = list.filter(x => {
          const t = (x.title || "").toLowerCase();
          const d = (x.description || "").toLowerCase();
          return t.includes(query) || d.includes(query);
        });
      }
      list.sort((a, b) => {
        if (sortValue === "hall") {
          return (a.hall || "").localeCompare(b.hall || "", "ru");
        }
        // default: по названию
        return (a.title || "").localeCompare(b.title || "", "ru");
      });
      if (!list.length) {
        container.innerHTML = '<p class="text-muted">По выбранным фильтрам экспозиции не найдены.</p>';
        return;
      }
      list.forEach(x => container.appendChild(renderExhibitionCard(x)));
    }

    try {
      exhibitions = await fetchJson("/api/exhibitions");
      const halls = Array.from(
        new Set(
          exhibitions
            .map(x => x.hall)
            .filter(Boolean)
        )
      );
      halls.forEach(h => {
        const opt = document.createElement("option");
        opt.value = h;
        opt.textContent = h;
        hallFilter.appendChild(opt);
      });
      render();
    } catch (e) {
      console.error(e);
      container.innerHTML = '<p class="text-muted">Не удалось загрузить экспозиции.</p>';
    }

    hallFilter.addEventListener("change", render);
    statusFilter.addEventListener("change", render);
    if (searchInput) {
      searchInput.addEventListener("input", render);
    }
    if (sortSelect) {
      sortSelect.addEventListener("change", render);
    }
    resetBtn.addEventListener("click", function () {
      hallFilter.value = "all";
      statusFilter.value = "all";
      if (searchInput) searchInput.value = "";
      if (sortSelect) sortSelect.value = "title";
      render();
    });
  }

  async function initEventDetail(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    const id = getQueryParam("id");
    if (!id) {
      container.innerHTML = '<p class="text-muted">Не указан идентификатор мероприятия.</p>';
      return;
    }
    try {
      let e;
      try {
        e = await fetchJson(`/api/events/${id}`);
      } catch (err) {
        // fallback: пробуем получить из общего списка
        try {
          const list = await fetchJson("/api/events");
          e = list.find(ev => String(ev.id) === String(id));
        } catch (innerErr) {
          console.error(innerErr);
        }
      }
      if (!e) {
        container.innerHTML = '<p class="text-muted">Не удалось загрузить данные мероприятия.</p>';
        return;
      }
      const title = e.title || "Мероприятие";
      const dates = formatDateRange(e.startDate, e.endDate);
      const hall = e.hall || "";
      const price = e.ticketPrice != null ? `${e.ticketPrice} ₽` : "Вход свободный / по регистрации";
      const image = e.imageUrl || "";

      const imageBlock = image
        ? `<div class="mb-3">
             <img src="${image}" alt="${title}" class="img-fluid rounded-3 w-100" style="object-fit: cover; max-height: 320px;">
           </div>`
        : "";

      container.innerHTML = `
        <div class="row g-4">
          <div class="col-lg-7">
            <p class="small text-uppercase text-muted mb-1">Мероприятие</p>
            <h1 class="section-title mb-2">${title}</h1>
            <p class="small mb-1">${dates}</p>
            <p class="small text-muted mb-1">${hall}</p>
            <p class="small mb-3"><strong>${price}</strong></p>
            ${imageBlock}
            <p>${(e.description || "").replace(/\n/g, "<br>")}</p>
          </div>
          <div class="col-lg-5">
            <div class="card border-0 shadow-sm mb-3">
              <div class="card-body small">
                <h2 class="h6 text-uppercase text-muted mb-2">Практическая информация</h2>
                <p class="mb-1"><strong>Место:</strong> ${hall || "Будет уточнено"}</p>
                <p class="mb-1"><strong>Формат:</strong> лекция / показ / экскурсия (пример)</p>
                <p class="mb-0 text-muted">Точные детали формата и расписания можно добавить в описание.</p>
              </div>
            </div>
            <a href="/tickets.html?eventId=${e.id}" class="btn btn-dark mb-3 w-100">Купить билет</a>
            <a href="/events.html" class="small text-decoration-none d-block">← Назад к афише</a>
          </div>
        </div>
      `;
    } catch (e) {
      console.error(e);
      container.innerHTML = '<p class="text-muted">Не удалось загрузить данные мероприятия.</p>';
    }
  }

  async function initExhibitionDetail(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    const id = getQueryParam("id");
    if (!id) {
      container.innerHTML = '<p class="text-muted">Не указан идентификатор экспозиции.</p>';
      return;
    }
    try {
      let x;
      try {
        x = await fetchJson(`/api/exhibitions/${id}`);
      } catch (err) {
        // fallback: пробуем получить из общего списка
        try {
          const list = await fetchJson("/api/exhibitions");
          x = list.find(ex => String(ex.id) === String(id));
        } catch (innerErr) {
          console.error(innerErr);
        }
      }
      if (!x) {
        container.innerHTML = '<p class="text-muted">Не удалось загрузить данные экспозиции.</p>';
        return;
      }
      const title = x.title || "Экспозиция";
      const hall = x.hall || "";
      const status = x.status || "";
      const statusText = status === "PERMANENT" ? "Постоянная экспозиция" : status === "TEMPORARY" ? "Временная выставка" : "";
      const image = x.imageUrl || "";

      const imageBlock = image
        ? `<div class="mb-3">
             <img src="${image}" alt="${title}" class="img-fluid rounded-3 w-100" style="object-fit: cover; max-height: 320px;">
           </div>`
        : "";

      container.innerHTML = `
        <div class="row g-4">
          <div class="col-lg-7">
            <p class="small text-uppercase text-muted mb-1">Экспозиция</p>
            <h1 class="section-title mb-2">${title}</h1>
            <p class="small mb-1">${statusText}</p>
            <p class="small text-muted mb-3">${hall}</p>
            ${imageBlock}
            <p>${(x.description || "").replace(/\n/g, "<br>")}</p>
          </div>
          <div class="col-lg-5">
            <div class="card border-0 shadow-sm mb-3">
              <div class="card-body small">
                <h2 class="h6 text-uppercase text-muted mb-2">Факты об экспозиции</h2>
                <p class="mb-1"><strong>Тип:</strong> ${statusText || "Будет уточнено"}</p>
                <p class="mb-1"><strong>Зал:</strong> ${hall || "Будет уточнён"}</p>
                <p class="mb-0 text-muted">Даты работы, кураторы и партнёры могут быть добавлены в описании.</p>
              </div>
            </div>
            <a href="/tickets.html" class="btn btn-dark mb-3 w-100">Купить билет</a>
            <a href="/exhibitions.html" class="small text-decoration-none d-block">← Назад к экспозициям</a>
          </div>
        </div>
      `;
    } catch (e) {
      console.error(e);
      container.innerHTML = '<p class="text-muted">Не удалось загрузить данные экспозиции.</p>';
    }
  }

  async function initTicketsPage(ids) {
    const eventSelect = document.getElementById(ids.eventSelectId);
    const qtyInput = document.getElementById(ids.qtyInputId);
    const totalInput = document.getElementById(ids.totalInputId);
    const nameInput = document.getElementById(ids.nameInputId);
    const emailInput = document.getElementById(ids.emailInputId);
    const form = document.getElementById(ids.formId);
    const message = document.getElementById(ids.messageId);
    const summaryTitle = document.getElementById(ids.summaryTitleId);
    const summaryDate = document.getElementById(ids.summaryDateId);
    const summaryHall = document.getElementById(ids.summaryHallId);
    const summaryQty = document.getElementById(ids.summaryQtyId);
    const summaryTotal = document.getElementById(ids.summaryTotalId);
    if (!eventSelect || !qtyInput || !totalInput || !nameInput || !emailInput || !form || !message) return;

    let events = [];
    let selectedEvent = null;

    function updateSummary() {
      const qty = parseInt(qtyInput.value || "1", 10) || 1;
      summaryQty.textContent = String(qty);
      if (selectedEvent) {
        summaryTitle.textContent = selectedEvent.title || "Мероприятие";
        summaryDate.textContent = formatDateRange(selectedEvent.startDate, selectedEvent.endDate);
        summaryHall.textContent = selectedEvent.hall || "";
        if (selectedEvent.ticketPrice != null) {
          const total = Number(selectedEvent.ticketPrice) * qty;
          summaryTotal.textContent = `${total} ₽`;
          totalInput.value = `${total} ₽`;
        } else {
          summaryTotal.textContent = "Вход свободный / по регистрации";
          totalInput.value = "—";
        }
      } else {
        summaryTitle.textContent = "Мероприятие не выбрано";
        summaryDate.textContent = "";
        summaryHall.textContent = "";
        summaryTotal.textContent = "—";
        totalInput.value = "";
      }
    }

    function onEventChange() {
      const id = eventSelect.value;
      selectedEvent = events.find(e => String(e.id) === id) || null;
      updateSummary();
    }

    qtyInput.addEventListener("input", updateSummary);

    try {
      events = await fetchJson("/api/events");
      if (!events.length) {
        const opt = document.createElement("option");
        opt.disabled = true;
        opt.selected = true;
        opt.textContent = "Нет активных мероприятий";
        eventSelect.appendChild(opt);
        eventSelect.disabled = true;
      } else {
        const preselectedId = getQueryParam("eventId");
        events.forEach(e => {
          const opt = document.createElement("option");
          opt.value = e.id;
          opt.textContent = e.title || `Мероприятие #${e.id}`;
          if (preselectedId && String(e.id) === preselectedId) {
            opt.selected = true;
            selectedEvent = e;
          }
          eventSelect.appendChild(opt);
        });
        if (!selectedEvent) {
          selectedEvent = events[0];
        }
        onEventChange();
      }
    } catch (e) {
      console.error(e);
      const opt = document.createElement("option");
      opt.disabled = true;
      opt.selected = true;
      opt.textContent = "Ошибка загрузки списка";
      eventSelect.appendChild(opt);
      eventSelect.disabled = true;
    }

    eventSelect.addEventListener("change", onEventChange);

    form.addEventListener("submit", async function (ev) {
      ev.preventDefault();
      message.textContent = "";
      message.className = "small mt-2";
      if (!selectedEvent || eventSelect.disabled) return;

      const payload = {
        buyerName: nameInput.value,
        buyerEmail: emailInput.value
      };

      try {
        const res = await fetch(`/api/events/${selectedEvent.id}/tickets/purchase`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        if (!res.ok) {
          message.textContent = "Не удалось оформить покупку. Проверьте данные.";
          message.classList.add("text-danger");
          return;
        }
        message.textContent = "Покупка оформлена. Детали заказа отправлены на e‑mail (пример).";
        message.classList.add("text-success");
      } catch (err) {
        console.error(err);
        message.textContent = "Произошла ошибка при оформлении покупки.";
        message.classList.add("text-danger");
      }
    });
  }

  return {
    initHeroCarousel,
    initEventsPage,
    initExhibitionsPage,
    initEventDetail,
    initExhibitionDetail,
    initTicketsPage,
    loadEventsPreview,
    loadEventsGrid,
    loadExhibitionsPreview,
    loadExhibitionsGrid,
    populateTicketEvents,
    initTicketForm,
    loadFaq
  };
})();
