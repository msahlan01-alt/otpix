/* ============================================================
   OUTFIX — main.js
   Handles: ticker, fade-up scroll, nav hamburger,
   nav active link, CTA form, contact scroll
   ============================================================ */

document.addEventListener("DOMContentLoaded", () => {
  /* ===================================================
     1. FADE-UP ON SCROLL
     Semua elemen dengan class .fade-up akan muncul
     saat masuk viewport
  =================================================== */
  const fadeEls = document.querySelectorAll(".fade-up");
  if (fadeEls.length) {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("visible");
            observer.unobserve(entry.target); // animasi sekali saja
          }
        });
      },
      { threshold: 0.12 },
    );

    fadeEls.forEach((el) => observer.observe(el));
  }

  /* ===================================================
     2. NAV HAMBURGER (mobile)
     Toggle .open pada .nav-links saat hamburger diklik
  =================================================== */
  const hamburger = document.querySelector(".nav-hamburger");
  const navLinks = document.querySelector(".nav-links");

  if (hamburger && navLinks) {
    hamburger.addEventListener("click", () => {
      navLinks.classList.toggle("open");
      // Animasi hamburger → X
      hamburger.classList.toggle("active");
    });

    // Tutup menu saat link diklik
    navLinks.querySelectorAll(".nav-link").forEach((link) => {
      link.addEventListener("click", () => {
        navLinks.classList.remove("open");
        hamburger.classList.remove("active");
      });
    });

    // Tutup saat klik di luar
    document.addEventListener("click", (e) => {
      if (!hamburger.contains(e.target) && !navLinks.contains(e.target)) {
        navLinks.classList.remove("open");
        hamburger.classList.remove("active");
      }
    });
  }

  /* ===================================================
     3. NAV ACTIVE LINK (highlight berdasarkan scroll)
     Deteksi section mana yang sedang tampil
  =================================================== */
  const sections = document.querySelectorAll("section[id]");
  const allNavLinks = document.querySelectorAll('.nav-link[href^="#"]');

  if (sections.length && allNavLinks.length) {
    const sectionObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const id = entry.target.getAttribute("id");
            allNavLinks.forEach((link) => {
              link.classList.toggle(
                "active",
                link.getAttribute("href") === `#${id}`,
              );
            });
          }
        });
      },
      { threshold: 0.4 },
    );

    sections.forEach((section) => sectionObserver.observe(section));
  }

  /* ===================================================
     4. STICKY NAV — tambah border saat scroll
  =================================================== */
  const nav = document.querySelector(".nav");
  if (nav) {
    window.addEventListener(
      "scroll",
      () => {
        nav.style.boxShadow =
          window.scrollY > 10 ? "0 2px 0 var(--foreground)" : "none";
      },
      { passive: true },
    );
  }

  /* ===================================================
     5. CTA EMAIL FORM (landing page sign up section)
     Validasi dan feedback sederhana
  =================================================== */
  const ctaForm = document.querySelector(".cta-input-row");
  const ctaInput = document.querySelector(".cta-input");
  const ctaBtn = document.querySelector(".cta-btn");

  if (ctaForm && ctaInput && ctaBtn) {
    ctaBtn.addEventListener("click", () => {
      const email = ctaInput.value.trim();
      const isValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

      if (!isValid) {
        ctaInput.style.borderColor = "#d4183d";
        ctaInput.placeholder = "MASUKKAN EMAIL YANG VALID";
        ctaInput.value = "";
        setTimeout(() => {
          ctaInput.style.borderColor = "";
          ctaInput.placeholder = "YOUR EMAIL ADDRESS";
        }, 2000);
        return;
      }

      // Redirect ke register dengan email pre-filled
      window.location.href = `/auth/register?email=${encodeURIComponent(email)}`;
    });

    // Hapus error saat user mulai ketik lagi
    ctaInput.addEventListener("input", () => {
      ctaInput.style.borderColor = "";
    });

    // Enter key submit
    ctaInput.addEventListener("keydown", (e) => {
      if (e.key === "Enter") ctaBtn.click();
    });
  }

  /* ===================================================
     6. SMOOTH SCROLL untuk anchor link (#features, dll)
  =================================================== */
  document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
    anchor.addEventListener("click", (e) => {
      const target = document.querySelector(anchor.getAttribute("href"));
      if (target) {
        e.preventDefault();
        const navHeight = document.querySelector(".nav")?.offsetHeight || 0;
        const top =
          target.getBoundingClientRect().top + window.scrollY - navHeight - 8;
        window.scrollTo({ top, behavior: "smooth" });
      }
    });
  });

  /* ===================================================
     7. CONTACT LINK — scroll ke footer
  =================================================== */
  document
    .querySelectorAll('a[href="#contact"], .nav-link[href="#"]')
    .forEach((link) => {
      if (link.textContent.trim() === "CONTACT") {
        link.addEventListener("click", (e) => {
          e.preventDefault();
          const footer = document.querySelector(".footer");
          if (footer) footer.scrollIntoView({ behavior: "smooth" });
        });
      }
    });

  /* ===================================================
     8. FLASH MESSAGE AUTO-CLOSE
     (untuk login/register flash messages)
  =================================================== */
  document.querySelectorAll(".flash-close").forEach((btn) => {
    btn.addEventListener("click", () => {
      btn.closest(".flash")?.remove();
    });
  });

  // Auto-hide flash setelah 5 detik
  document.querySelectorAll(".flash").forEach((flash) => {
    setTimeout(() => {
      flash.style.transition = "opacity 0.4s";
      flash.style.opacity = "0";
      setTimeout(() => flash.remove(), 400);
    }, 5000);
  });

  /* ===================================================
     9. HAMBURGER ANIMATION CSS (inject style)
  =================================================== */
  const style = document.createElement("style");
  style.textContent = `
    .nav-hamburger.active span:nth-child(1) { transform: translateY(6px) rotate(45deg); }
    .nav-hamburger.active span:nth-child(2) { opacity: 0; transform: scaleX(0); }
    .nav-hamburger.active span:nth-child(3) { transform: translateY(-6px) rotate(-45deg); }
    .nav-hamburger span { transform-origin: center; transition: all 0.2s; }
  `;
  document.head.appendChild(style);
});
