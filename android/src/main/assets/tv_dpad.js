/* ============================================================================
 * FILE: app/src/main/assets/tv_dpad.js
 * ============================================================================
 * This file is injected into EVERY page loaded in the WebView by
 * WebViewActivity.injectTvDpad().
 *
 * What it does
 *  1. Injects a stylesheet that paints the focused link with a bright red
 *     outline  ->  outline: 3px solid #FF0000
 *  2. Builds a list of every visible <a> on the page and provides geometric
 *     (up / down / left / right) navigation between them, so a TV remote can
 *     drive a plain HTML directory listing.
 *  3. Clicks the focused link when OK/Enter is pressed.
 *  4. Re-scans the page when the directory listing changes (AJAX listings).
 *
 * Two-way contract with Java
 *  - Java sets  window.__FTVTV_JAVA_DPAD__ = true  right after injecting, which
 *    tells this script that Android is consuming the D-Pad keys itself. In that
 *    case the internal keydown listener stays disabled so a single press never
 *    moves the focus twice.
 *  - Java calls window.FTVTV.navigate('up'|'down'|'left'|'right'),
 *    window.FTVTV.activate() and window.FTVTV.focusFirst() through
 *    evaluateJavascript().
 * ==========================================================================*/

(function () {
    'use strict';

    if (window.__FTVTV_DPAD_READY__) {
        return; // already injected into this document
    }
    window.__FTVTV_DPAD_READY__ = true;

    var FOCUS_CLASS = 'ftvtv-focused';
    var STYLE_ID = 'ftvtv-dpad-style';
    var TV_STYLE_ID = 'ftvtv-tv-style';

    var links = [];       // currently known focusable anchors
    var current = -1;     // index of the focused anchor inside links[]
    var tvModeOn = false;

    /* ------------------------------------------------------------------
     * 1. STYLES
     * ----------------------------------------------------------------*/

    function ensureStyle(id, css) {
        if (document.getElementById(id)) {
            return;
        }
        var style = document.createElement('style');
        style.id = id;
        style.type = 'text/css';
        style.appendChild(document.createTextNode(css));
        var head = document.head || document.documentElement;
        if (head) {
            head.appendChild(style);
        }
    }

    function injectBaseStyle() {
        // The red outline is the core requirement: 3px solid #FF0000.
        ensureStyle(STYLE_ID,
            'html{scroll-behavior:smooth;}' +
            'body{-webkit-user-select:none;user-select:none;}' +
            'a:focus, a.' + FOCUS_CLASS + ',' +
            'input:focus, select:focus, button:focus, [tabindex]:focus{' +
            '  outline:3px solid #FF0000 !important;' +
            '  outline-offset:2px !important;' +
            '  background-color:rgba(255,0,0,0.20) !important;' +
            '  color:#ffffff !important;' +
            '  border-radius:4px;' +
            '  box-shadow:0 0 14px rgba(255,0,0,0.85) !important;' +
            '}' +
            'a{-webkit-tap-highlight-color:rgba(255,0,0,0.45);}' +
            'a:focus{text-decoration:none;}'
        );
    }

    /** Optional "TV readability" pass: bigger text and taller link hit areas. */
    function setTvMode(enabled) {
        tvModeOn = !!enabled;
        var existing = document.getElementById(TV_STYLE_ID);
        if (!tvModeOn) {
            if (existing && existing.parentNode) {
                existing.parentNode.removeChild(existing);
            }
            return;
        }
        ensureStyle(TV_STYLE_ID,
            'body{font-size:18px !important;line-height:1.85 !important;}' +
            'a{display:inline-block;padding:2px 6px;margin:1px 0;}' +
            'pre,code,td{font-size:17px !important;line-height:1.8 !important;}' +
            'table{border-collapse:separate;border-spacing:0 4px;}'
        );
    }

    /* ------------------------------------------------------------------
     * 2. LINK COLLECTION
     * ----------------------------------------------------------------*/

    function isVisible(el) {
        if (!el) {
            return false;
        }
        var r = el.getBoundingClientRect();
        if (r.width <= 1 || r.height <= 1) {
            return false;
        }
        var cs = window.getComputedStyle(el);
        if (!cs) {
            return true;
        }
        return cs.display !== 'none' && cs.visibility !== 'hidden' && cs.opacity !== '0';
    }

    /** Rebuilds the list of navigable links, in document order. */
    function collectLinks() {
        var nodes = document.querySelectorAll('a[href]');
        var out = [];
        for (var i = 0; i < nodes.length; i++) {
            var a = nodes[i];
            var href = a.getAttribute('href') || '';
            if (href.length === 0 || href.charAt(0) === '#') {
                continue; // pure in-page anchors are not useful in a file listing
            }
            if (href.indexOf('javascript:') === 0) {
                continue;
            }
            if (!isVisible(a)) {
                continue;
            }
            out.push(a);
        }
        links = out;
        if (current >= links.length) {
            current = links.length - 1;
        }
    }

    /* ------------------------------------------------------------------
     * 3. GEOMETRIC NAVIGATION
     * ----------------------------------------------------------------*/

    function horizontalOverlap(a, b) {
        var left = Math.max(a.left, b.left);
        var right = Math.min(a.right, b.right);
        return Math.max(0, right - left);
    }

    function verticalOverlap(a, b) {
        var top = Math.max(a.top, b.top);
        var bottom = Math.min(a.bottom, b.bottom);
        return Math.max(0, bottom - top);
    }

    /**
     * Moves the highlight one step in the given direction.
     * @param {string} dir 'up' | 'down' | 'left' | 'right'
     * @return {boolean} true when the focus moved
     */
    function navigate(dir) {
        collectLinks();
        if (links.length === 0) {
            return false;
        }
        if (current < 0 || current >= links.length) {
            focusIndex(0);
            return true;
        }

        var from = links[current];
        var fr = from.getBoundingClientRect();
        var bestIndex = -1;
        var bestScore = Number.MAX_VALUE;

        for (var i = 0; i < links.length; i++) {
            if (i === current) {
                continue;
            }
            var r = links[i].getBoundingClientRect();
            if (r.width <= 1 || r.height <= 1) {
                continue;
            }

            var primary, secondary, overlap;

            if (dir === 'up') {
                primary = fr.top - r.bottom;
                if (primary < -1) { continue; }
                overlap = horizontalOverlap(fr, r);
                secondary = Math.abs((r.left + r.width / 2) - (fr.left + fr.width / 2));
            } else if (dir === 'down') {
                primary = r.top - fr.bottom;
                if (primary < -1) { continue; }
                overlap = horizontalOverlap(fr, r);
                secondary = Math.abs((r.left + r.width / 2) - (fr.left + fr.width / 2));
            } else if (dir === 'left') {
                primary = fr.left - r.right;
                if (primary < -1) { continue; }
                overlap = verticalOverlap(fr, r);
                secondary = Math.abs((r.top + r.height / 2) - (fr.top + fr.height / 2));
            } else {
                primary = r.left - fr.right;
                if (primary < -1) { continue; }
                overlap = verticalOverlap(fr, r);
                secondary = Math.abs((r.top + r.height / 2) - (fr.top + fr.height / 2));
            }

            // Prefer: close by, well aligned with the current element.
            var score = primary + secondary * 2.5 - overlap * 3;
            if (overlap > 0) {
                score -= 250; // strong bonus for items sharing the same row/column
            }

            if (score < bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        if (bestIndex >= 0) {
            focusIndex(bestIndex);
            return true;
        }
        return false; // nothing in that direction -> let the page scroll
    }

    /** Applies the highlight + red outline to links[index] and scrolls to it. */
    function focusIndex(index) {
        if (index < 0 || index >= links.length) {
            return;
        }
        if (current >= 0 && links[current]) {
            links[current].classList.remove(FOCUS_CLASS);
        }
        current = index;
        var el = links[index];
        el.classList.add(FOCUS_CLASS);

        try {
            el.focus({ preventScroll: true });
        } catch (e) {
            try { el.focus(); } catch (e2) { /* ignore */ }
        }

        scrollIntoViewCentered(el);
    }

    function scrollIntoViewCentered(el) {
        var r = el.getBoundingClientRect();
        var vh = window.innerHeight || document.documentElement.clientHeight || 720;
        var margin = 70;
        if (r.top < margin || r.bottom > vh - margin) {
            var delta = (r.top + r.height / 2) - vh / 2;
            try {
                window.scrollBy({ top: delta, left: 0, behavior: 'smooth' });
            } catch (e) {
                window.scrollBy(0, delta);
            }
        }
    }

    /** Clicks the currently highlighted link. */
    function activate() {
        if (current < 0 || current >= links.length) {
            if (links.length > 0) {
                focusIndex(0);
                return true;
            }
            return false;
        }
        var el = links[current];
        try {
            el.click();
        } catch (e) {
            // Fallback for pages that only respond to real navigation.
            var href = el.getAttribute('href');
            if (href) {
                window.location.href = href;
            }
        }
        return true;
    }

    function focusFirst() {
        collectLinks();
        if (links.length === 0) {
            return false;
        }
        focusIndex(0);
        return true;
    }

    function focusLast() {
        collectLinks();
        if (links.length === 0) {
            return false;
        }
        focusIndex(links.length - 1);
        return true;
    }

    function blurAll() {
        if (current >= 0 && links[current]) {
            links[current].classList.remove(FOCUS_CLASS);
        }
        current = -1;
    }

    /* ------------------------------------------------------------------
     * 4. PAGE SCROLLING HELPERS (used when no link is in that direction)
     * ----------------------------------------------------------------*/

    function pageScroll(dir) {
        var step = Math.round((window.innerHeight || 720) * 0.6);
        var x = 0;
        var y = 0;
        if (dir === 'down') { y = step; }
        else if (dir === 'up') { y = -step; }
        else if (dir === 'right') { x = step; }
        else if (dir === 'left') { x = -step; }
        try {
            window.scrollBy({ top: y, left: x, behavior: 'smooth' });
        } catch (e) {
            window.scrollBy(x, y);
        }
        return true;
    }

    /**
     * Used by Java when no focusable link is available: scroll the page, and if
     * the page cannot scroll any further report false so Android can move on.
     */
    function scrollOrReport(dir) {
        if (navigate(dir)) {
            return true;
        }
        return pageScroll(dir);
    }

    /* ------------------------------------------------------------------
     * 5. INTERNAL KEY HANDLING (only when Java is NOT consuming keys)
     * ----------------------------------------------------------------*/

    function dirFromKey(k) {
        if (k === 38) { return 'up'; }
        if (k === 40) { return 'down'; }
        if (k === 37) { return 'left'; }
        if (k === 39) { return 'right'; }
        return null;
    }

    document.addEventListener('keydown', function (e) {
        // Java owns the D-Pad when it has flagged itself; skip to avoid doubles.
        if (window.__FTVTV_JAVA_DPAD__) {
            return;
        }
        var dir = dirFromKey(e.keyCode);
        if (dir) {
            if (navigate(dir)) {
                e.preventDefault();
                e.stopPropagation();
            }
            return;
        }
        if (e.keyCode === 13 || e.keyCode === 10) {
            if (activate()) {
                e.preventDefault();
                e.stopPropagation();
            }
        }
    }, true);

    /* ------------------------------------------------------------------
     * 6. LIFECYCLE / OBSERVERS
     * ----------------------------------------------------------------*/

    function boot() {
        injectBaseStyle();
        collectLinks();
        // Do not steal focus if the page already put it somewhere useful.
        var active = document.activeElement;
        if (!active || active === document.body || active === document.documentElement) {
            if (links.length > 0) {
                focusIndex(0);
            }
        } else {
            for (var i = 0; i < links.length; i++) {
                if (links[i] === active) { current = i; break; }
            }
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', boot);
    } else {
        boot();
    }
    window.addEventListener('load', function () {
        injectBaseStyle();
        collectLinks();
    });

    // AJAX-driven directory listings replace the DOM without a page load.
    try {
        var observer = new MutationObserver(function () {
            // Cheap debounce: only re-scan on the next frame.
            if (observer.__pending) { return; }
            observer.__pending = true;
            window.requestAnimationFrame(function () {
                observer.__pending = false;
                var before = current >= 0 && links[current] ? links[current] : null;
                collectLinks();
                current = -1;
                if (before) {
                    for (var i = 0; i < links.length; i++) {
                        if (links[i] === before) { current = i; break; }
                    }
                }
            });
        });
        observer.observe(document.documentElement || document.body, {
            childList: true,
            subtree: true
        });
    } catch (e) { /* MutationObserver unavailable on very old WebViews */ }

    /* ------------------------------------------------------------------
     * 7. PUBLIC API USED BY JAVA
     * ----------------------------------------------------------------*/

    window.FTVTV = {
        version: 3,
        navigate: navigate,
        scrollOrReport: scrollOrReport,
        activate: activate,
        focusFirst: focusFirst,
        focusLast: focusLast,
        blur: blurAll,
        rebuild: collectLinks,
        setTvMode: setTvMode,
        count: function () { return links.length; },
        currentHref: function () {
            if (current >= 0 && links[current]) {
                return links[current].href || '';
            }
            return '';
        },
        currentText: function () {
            if (current >= 0 && links[current]) {
                return (links[current].textContent || '').trim();
            }
            return '';
        }
    };
})();
