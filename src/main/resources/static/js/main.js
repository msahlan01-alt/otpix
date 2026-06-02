(function () {
  "use strict";

  const $ = (selector, scope = document) => scope.querySelector(selector);
  const $$ = (selector, scope = document) =>
    Array.from(scope.querySelectorAll(selector));

  function createFlash(message, type) {
    const flash = document.createElement("div");
    flash.className = `flash flash-${type || "error"} flash-client`;
    flash.innerHTML = `
            <span>${message}</span>
            <button type="button" class="flash-close" aria-label="Close">&times;</button>
        `;

    const close = $(".flash-close", flash);
    close.addEventListener("click", () => flash.remove());

    return flash;
  }

  function showFormMessage(form, message, type) {
    const oldFlash = $(".flash-client", form.parentElement);
    if (oldFlash) {
      oldFlash.remove();
    }

    form.parentElement.insertBefore(createFlash(message, type), form);
  }

  function enhanceLoginForm() {
    const form = $("[data-login-form]");
    if (!form) {
      return;
    }

    const username = $("#username", form);
    const password = $("#password", form);
    const submit = $("button[type='submit']", form);

    form.addEventListener("submit", (event) => {
      if (!username.value.trim() || !password.value.trim()) {
        event.preventDefault();
        showFormMessage(form, "Username dan password wajib diisi.", "error");
        return;
      }

      if (submit) {
        submit.disabled = true;
        submit.textContent = "LOGIN...";
      }
    });
  }

  function enhanceRegisterForm() {
    const form = $("[data-register-form]");
    if (!form) {
      return;
    }

    const password = $("#password", form);
    const confirmPassword = $("#confirmPassword", form);
    const submit = $("button[type='submit']", form);

    form.addEventListener("submit", (event) => {
      if (password.value.length < 8) {
        event.preventDefault();
        showFormMessage(form, "Password minimal 8 karakter.", "error");
        return;
      }

      if (password.value !== confirmPassword.value) {
        event.preventDefault();
        showFormMessage(
          form,
          "Password dan konfirmasi password tidak sama.",
          "error",
        );
        return;
      }

      if (submit) {
        submit.disabled = true;
        submit.textContent = "MENDAFTAR...";
      }
    });
  }

  function enhanceRequiredForms() {
    $$("form").forEach((form) => {
      if (
        form.hasAttribute("data-login-form") ||
        form.hasAttribute("data-register-form")
      ) {
        return;
      }

      form.addEventListener("submit", (event) => {
        const requiredFields = $$("[required]", form);

        for (const field of requiredFields) {
          if (!field.value.trim()) {
            event.preventDefault();
            showFormMessage(form, "Lengkapi field yang wajib diisi.", "error");
            field.focus();
            return;
          }
        }

        const submit = $("button[type='submit']", form);
        if (submit) {
          submit.disabled = true;
          submit.textContent = "PROCESSING...";
        }
      });
    });
  }

  function autoCloseServerFlash() {
    $$(".flash").forEach((flash) => {
      const close = $(".flash-close", flash);
      if (close) {
        close.addEventListener("click", () => flash.remove());
      }

      window.setTimeout(() => {
        if (flash.classList.contains("flash-success")) {
          flash.remove();
        }
      }, 4500);
    });
  }

  function setActiveSidebarByPath() {
    const path = window.location.pathname;

    $$(".sidebar-link").forEach((link) => {
      const href = link.getAttribute("href");

      if (!href) {
        return;
      }

      if (path === href || path.startsWith(href + "/")) {
        link.classList.add("active");
      }
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    enhanceLoginForm();
    enhanceRegisterForm();
    enhanceRequiredForms();
    autoCloseServerFlash();
    setActiveSidebarByPath();
  });
})();
