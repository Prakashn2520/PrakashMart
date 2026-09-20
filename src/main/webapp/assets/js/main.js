/**
 * ==========================================================================
 * PrakashMart - Classical & Lightweight Client-side Framework
 * Vanilla JavaScript: Toast, Modal, Loader, Cart Sync, Accessible Nav, Debounced Search
 * ==========================================================================
 */

// Global App State & Context
const PrakashMart = {
    contextPath: window.location.pathname.substring(0, window.location.pathname.indexOf("/", 1)) || "",
    
    init: function () {
        this.initMobileNav();
        this.initAutoDismissAlerts();
        this.initQuickAddToCart();
        this.initQuantitySelectors();
        this.initWishlistToggles();
        this.initCartActions();
        this.initDebouncedSearch();
        this.initProductGallery();
        this.refreshCartBadge();
    },

    // ----------------------------------------------------------------------
    // CSRF Token Helper
    // ----------------------------------------------------------------------
    getCsrfToken: function () {
        const metaTag = document.querySelector('meta[name="_csrf"]');
        if (metaTag && metaTag.content) {
            return metaTag.content;
        }
        const hiddenInput = document.querySelector('input[name="_csrf"]') || document.querySelector('input[name="csrfToken"]');
        if (hiddenInput && hiddenInput.value) {
            return hiddenInput.value;
        }
        return "";
    },

    // ----------------------------------------------------------------------
    // Mobile Navigation & Keyboard Accessibility
    // ----------------------------------------------------------------------
    initMobileNav: function () {
        const toggleBtn = document.querySelector(".mobile-toggle");
        const navMenu = document.querySelector(".nav-menu");

        if (toggleBtn && navMenu) {
            toggleBtn.addEventListener("click", function () {
                const isExpanded = toggleBtn.getAttribute("aria-expanded") === "true";
                toggleBtn.setAttribute("aria-expanded", !isExpanded);
                navMenu.classList.toggle("active");
            });

            // Close on Escape key
            document.addEventListener("keydown", function (e) {
                if (e.key === "Escape" && navMenu.classList.contains("active")) {
                    navMenu.classList.remove("active");
                    toggleBtn.setAttribute("aria-expanded", "false");
                    toggleBtn.focus();
                }
            });
        }
    },

    // ----------------------------------------------------------------------
    // Auto-dismiss Alerts
    // ----------------------------------------------------------------------
    initAutoDismissAlerts: function () {
        const alerts = document.querySelectorAll(".alert-dismissible");
        alerts.forEach(function (alert) {
            setTimeout(function () {
                alert.style.transition = "opacity 0.4s ease";
                alert.style.opacity = "0";
                setTimeout(function () {
                    if (alert.parentNode) alert.parentNode.removeChild(alert);
                }, 400);
            }, 4000);
        });
    },

    // ----------------------------------------------------------------------
    // Live Cart Badge Sync
    // ----------------------------------------------------------------------
    refreshCartBadge: async function () {
        try {
            const res = await fetch(this.contextPath + "/api/cart", {
                headers: { "Accept": "application/json" }
            });
            if (res.ok) {
                const json = await res.json();
                if (json.success && json.data) {
                    const badge = document.querySelector(".cart-count");
                    if (badge) {
                        const items = json.data.items || [];
                        const count = items.length;
                        badge.textContent = count;
                        badge.style.display = count > 0 ? "inline-flex" : "none";
                    }
                }
            }
        } catch (e) {
            // Silently ignore if guest or unauthenticated
        }
    },

    // ----------------------------------------------------------------------
    // Quick Add-to-Cart (Async with Fallback)
    // ----------------------------------------------------------------------
    initQuickAddToCart: function () {
        document.addEventListener("submit", async function (e) {
            const form = e.target.closest(".ajax-add-cart-form");
            if (!form) return;

            e.preventDefault();
            const btn = form.querySelector("button[type='submit']");
            const originalText = btn ? btn.innerHTML : "";
            if (btn) {
                btn.disabled = true;
                btn.innerHTML = '<span class="spinner" style="width:14px;height:14px;border-width:2px;"></span>';
            }

            const productId = form.querySelector("input[name='productId']")?.value;
            const quantity = form.querySelector("input[name='quantity']")?.value || 1;
            const csrf = PrakashMart.getCsrfToken();

            try {
                const headers = {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                };
                if (csrf) headers["X-CSRF-Token"] = csrf;

                const response = await fetch(PrakashMart.contextPath + "/api/cart/add", {
                    method: "POST",
                    headers: headers,
                    body: JSON.stringify({ productId: parseInt(productId), quantity: parseInt(quantity) })
                });

                const data = await response.json();
                if (response.ok && data.success) {
                    Toast.success(data.message || "Product added to cart");
                    PrakashMart.refreshCartBadge();
                } else if (response.status === 401) {
                    Toast.info("Please sign in to manage your shopping cart");
                    setTimeout(() => {
                        window.location.href = PrakashMart.contextPath + "/login?redirect=" + encodeURIComponent(window.location.pathname + window.location.search);
                    }, 1200);
                } else {
                    Toast.error(data.message || "Unable to add product to cart");
                }
            } catch (err) {
                Toast.error("Network error while updating cart");
            } finally {
                if (btn) {
                    btn.disabled = false;
                    btn.innerHTML = originalText;
                }
            }
        });
    },

    // ----------------------------------------------------------------------
    // Quantity Selectors (+ / -)
    // ----------------------------------------------------------------------
    initQuantitySelectors: function () {
        document.addEventListener("click", function (e) {
            const decBtn = e.target.closest(".qty-dec");
            const incBtn = e.target.closest(".qty-inc");
            if (!decBtn && !incBtn) return;

            const container = (decBtn || incBtn).closest(".qty-control");
            if (!container) return;
            const input = container.querySelector(".qty-input");
            if (!input) return;

            let val = parseInt(input.value) || 1;
            const max = parseInt(input.getAttribute("max")) || 999;
            const min = parseInt(input.getAttribute("min")) || 1;

            if (decBtn && val > min) {
                input.value = val - 1;
                input.dispatchEvent(new Event("change", { bubbles: true }));
            } else if (incBtn && val < max) {
                input.value = val + 1;
                input.dispatchEvent(new Event("change", { bubbles: true }));
            }
        });
    },

    // ----------------------------------------------------------------------
    // Wishlist Visual Toggle (UI only)
    // ----------------------------------------------------------------------
    initWishlistToggles: function () {
        document.addEventListener("click", function (e) {
            const btn = e.target.closest(".btn-wishlist");
            if (!btn) return;
            e.preventDefault();
            btn.classList.toggle("active");
            if (btn.classList.contains("active")) {
                btn.style.color = "#e11d48";
                Toast.info("Saved to your wishlist");
            } else {
                btn.style.color = "";
                Toast.info("Removed from your wishlist");
            }
        });
    },

    // ----------------------------------------------------------------------
    // Cart Page Actions (Update quantity, remove)
    // ----------------------------------------------------------------------
    initCartActions: function () {
        // Quantity change in cart table
        document.addEventListener("change", async function (e) {
            const input = e.target.closest(".cart-item-qty-input");
            if (!input) return;

            const cartItemId = input.dataset.cartItemId;
            const quantity = parseInt(input.value) || 1;
            const csrf = PrakashMart.getCsrfToken();

            try {
                Loader.show();
                const headers = {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                };
                if (csrf) headers["X-CSRF-Token"] = csrf;

                const res = await fetch(PrakashMart.contextPath + "/api/cart/update", {
                    method: "POST",
                    headers: headers,
                    body: JSON.stringify({ cartItemId: parseInt(cartItemId), quantity: quantity })
                });
                const data = await res.json();
                if (res.ok && data.success) {
                    Toast.success("Cart updated");
                    window.location.reload();
                } else {
                    Toast.error(data.message || "Failed to update item quantity");
                    window.location.reload();
                }
            } catch (err) {
                Toast.error("Network error while updating cart");
            } finally {
                Loader.hide();
            }
        });

        // Remove item button
        document.addEventListener("click", function (e) {
            const removeBtn = e.target.closest(".cart-remove-btn");
            if (!removeBtn) return;
            e.preventDefault();

            const cartItemId = removeBtn.dataset.cartItemId;
            const productName = removeBtn.dataset.productName || "this item";
            const csrf = PrakashMart.getCsrfToken();

            Modal.confirm({
                title: "Remove from Cart",
                message: "Are you sure you want to remove " + productName + " from your cart?",
                confirmText: "Remove",
                onConfirm: async function () {
                    try {
                        Loader.show();
                        const headers = {
                            "Content-Type": "application/json",
                            "Accept": "application/json"
                        };
                        if (csrf) headers["X-CSRF-Token"] = csrf;

                        const res = await fetch(PrakashMart.contextPath + "/api/cart/remove", {
                            method: "POST",
                            headers: headers,
                            body: JSON.stringify({ cartItemId: parseInt(cartItemId) })
                        });
                        const data = await res.json();
                        if (res.ok && data.success) {
                            Toast.success("Item removed");
                            window.location.reload();
                        } else {
                            Toast.error(data.message || "Failed to remove item");
                        }
                    } catch (err) {
                        Toast.error("Network error removing item");
                    } finally {
                        Loader.hide();
                    }
                }
            });
        });
    },

    // ----------------------------------------------------------------------
    // Debounced Search Utility
    // ----------------------------------------------------------------------
    initDebouncedSearch: function () {
        const searchInput = document.querySelector(".header-search .search-input");
        if (!searchInput) return;

        let debounceTimer;
        searchInput.addEventListener("input", function () {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                // Subtle feedback on typing if needed
            }, 300);
        });
    },

    // ----------------------------------------------------------------------
    // Password Visibility Toggle
    // ----------------------------------------------------------------------
    togglePassword: function (inputId, btn) {
        const input = document.getElementById(inputId);
        if (!input) return;
        if (input.type === "password") {
            input.type = "text";
            if (btn) btn.textContent = "Hide";
        } else {
            input.type = "password";
            if (btn) btn.textContent = "Show";
        }
    },

    // ----------------------------------------------------------------------
    // Form Submission Feedback & Spinner State
    // ----------------------------------------------------------------------
    showFormLoading: function (form) {
        const btn = form.querySelector(".submit-btn") || form.querySelector("button[type='submit']");
        if (btn) {
            btn.disabled = true;
            const orig = btn.innerHTML;
            btn.innerHTML = '<span class="spinner" style="width:14px;height:14px;border-width:2px;margin-right:6px;"></span> Processing...';
            setTimeout(() => {
                btn.disabled = false;
                btn.innerHTML = orig;
            }, 8000);
        }
    },

    // ----------------------------------------------------------------------
    // Accessible Modal Open/Close Controls
    // ----------------------------------------------------------------------
    openModal: function (modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add("active");
            const firstInput = modal.querySelector("input, select, textarea, button");
            if (firstInput) firstInput.focus();
        }
    },

    closeModal: function (modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove("active");
        }
    }
};

// --------------------------------------------------------------------------
// Toast Notification Engine
// --------------------------------------------------------------------------
const Toast = {
    show: function (message, type = "info", duration = 3200) {
        let container = document.getElementById("toast-container");
        if (!container) {
            container = document.createElement("div");
            container.id = "toast-container";
            document.body.appendChild(container);
        }

        const toast = document.createElement("div");
        toast.className = `toast toast-${type}`;
        toast.setAttribute("role", "status");
        toast.textContent = message;

        container.appendChild(toast);
        // Force reflow
        void toast.offsetWidth;
        toast.classList.add("show");

        setTimeout(function () {
            toast.classList.remove("show");
            setTimeout(function () {
                if (toast.parentNode) toast.parentNode.removeChild(toast);
            }, 300);
        }, duration);
    },

    success: function (message, duration) {
        this.show(message, "success", duration);
    },

    error: function (message, duration) {
        this.show(message, "error", duration || 4000);
    },

    info: function (message, duration) {
        this.show(message, "info", duration);
    }
};

// --------------------------------------------------------------------------
// Modal Dialog Engine
// --------------------------------------------------------------------------
const Modal = {
    confirm: function ({ title, message, confirmText = "Confirm", cancelText = "Cancel", onConfirm }) {
        let backdrop = document.getElementById("confirm-modal");
        if (!backdrop) {
            backdrop = document.createElement("div");
            backdrop.id = "confirm-modal";
            backdrop.className = "modal-backdrop";
            backdrop.innerHTML = `
                <div class="modal-dialog" role="dialog" aria-modal="true">
                    <div class="modal-header">
                        <h3 class="modal-title" id="confirm-modal-title"></h3>
                        <button type="button" class="modal-close" aria-label="Close">&times;</button>
                    </div>
                    <div class="modal-body" id="confirm-modal-body"></div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary modal-cancel"></button>
                        <button type="button" class="btn btn-primary modal-confirm"></button>
                    </div>
                </div>
            `;
            document.body.appendChild(backdrop);
        }

        backdrop.querySelector("#confirm-modal-title").textContent = title;
        backdrop.querySelector("#confirm-modal-body").textContent = message;
        
        const confirmBtn = backdrop.querySelector(".modal-confirm");
        const cancelBtn = backdrop.querySelector(".modal-cancel");
        const closeBtn = backdrop.querySelector(".modal-close");

        confirmBtn.textContent = confirmText;
        cancelBtn.textContent = cancelText;

        const closeModal = function () {
            backdrop.classList.remove("active");
        };

        confirmBtn.onclick = function () {
            closeModal();
            if (typeof onConfirm === "function") onConfirm();
        };

        cancelBtn.onclick = closeModal;
        closeBtn.onclick = closeModal;
        backdrop.onclick = function (e) {
            if (e.target === backdrop) closeModal();
        };

        backdrop.classList.add("active");
        confirmBtn.focus();
    }
};

// --------------------------------------------------------------------------
// Global Loader
// --------------------------------------------------------------------------
const Loader = {
    show: function () {
        let loader = document.getElementById("global-loader");
        if (!loader) {
            loader = document.createElement("div");
            loader.id = "global-loader";
            loader.innerHTML = '<div class="spinner" style="width:36px;height:36px;border-width:3px;"></div>';
            document.body.appendChild(loader);
        }
        loader.classList.add("active");
    },
    hide: function () {
        const loader = document.getElementById("global-loader");
        if (loader) loader.classList.remove("active");
    }
};

// Auto-initialize on DOM ready
document.addEventListener("DOMContentLoaded", function () {
    PrakashMart.init();
});
