/**
 * LootDrop Web V4.2.0 - File-Based Localization Loader
 * Dynamically fetches complete language bundles from ./locales/{lang}.json
 * instead of hardcoding English and Spanish strings inline.
 */
(function (global) {
  const LANG_STORAGE_KEY = 'lootdrop_language';
  const LOCALE_CACHE_PREFIX = 'lootdrop_locale_cache_v2_';
  const SUPPORTED_LANGS = ['en', 'es'];

  const localeCache = {};
  const inFlightRequests = {};

  function normalizeLanguage(lang) {
    return lang === 'es' ? 'es' : 'en';
  }

  function getSavedLanguage() {
    try {
      const saved = localStorage.getItem(LANG_STORAGE_KEY);
      if (saved === 'en' || saved === 'es') return saved;
    } catch (_) {}
    const browserLang = (navigator.language || navigator.userLanguage || 'en').toLowerCase();
    return browserLang.startsWith('es') ? 'es' : 'en';
  }

  function hasExplicitLanguageSelection() {
    try {
      const saved = localStorage.getItem(LANG_STORAGE_KEY);
      return saved === 'en' || saved === 'es';
    } catch (_) {
      return true;
    }
  }

  function readCachedLocaleFromStorage(lang) {
    try {
      const raw = localStorage.getItem(LOCALE_CACHE_PREFIX + lang);
      if (!raw) return null;
      const parsed = JSON.parse(raw);
      return parsed && typeof parsed === 'object' && parsed.strings ? parsed : null;
    } catch (_) {
      return null;
    }
  }

  function writeCachedLocaleToStorage(lang, data) {
    try {
      localStorage.setItem(LOCALE_CACHE_PREFIX + lang, JSON.stringify(data));
    } catch (_) {}
  }

  let currentLang = getSavedLanguage();
  // Hydrate synchronously from localStorage cache if previously downloaded
  SUPPORTED_LANGS.forEach(lang => {
    const cached = readCachedLocaleFromStorage(lang);
    if (cached) localeCache[lang] = cached;
  });

  async function loadLocale(lang, forceReload = false) {
    const normalized = normalizeLanguage(lang);
    if (!forceReload && localeCache[normalized]) {
      return localeCache[normalized];
    }
    if (!forceReload && inFlightRequests[normalized]) {
      return inFlightRequests[normalized];
    }

    const request = (async () => {
      try {
        const res = await fetch(`./locales/${normalized}.json?v=4.2.0`, { cache: 'no-cache' });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        if (!data || typeof data !== 'object' || !data.strings) {
          throw new Error(`Invalid locale bundle: ${normalized}`);
        }
        localeCache[normalized] = data;
        writeCachedLocaleToStorage(normalized, data);
        return data;
      } catch (err) {
        const cached = localeCache[normalized] || readCachedLocaleFromStorage(normalized);
        if (cached) {
          localeCache[normalized] = cached;
          return cached;
        }
        console.warn(`Failed to load locale file ./locales/${normalized}.json:`, err);
        return null;
      } finally {
        delete inFlightRequests[normalized];
      }
    })();

    inFlightRequests[normalized] = request;
    return request;
  }

  function getLocaleData(lang = currentLang) {
    return localeCache[normalizeLanguage(lang)] || localeCache.en || null;
  }

  function getMeta() {
    const data = getLocaleData(currentLang);
    if (data && data.meta) return data.meta;
    return {
      code: currentLang,
      dateLocale: currentLang === 'es' ? 'es-ES' : 'en-US',
      steamLanguage: currentLang === 'es' ? 'spanish' : 'english',
      toggleBadge: currentLang === 'es' ? '🌐 ES' : '🌐 EN'
    };
  }

  function t(key, params = {}) {
    const active = getLocaleData(currentLang);
    const fallback = localeCache.en;
    const raw =
      (active && active.strings && active.strings[key] !== undefined ? active.strings[key] : undefined) ??
      (fallback && fallback.strings && fallback.strings[key] !== undefined ? fallback.strings[key] : undefined) ??
      key;

    let str = String(raw);
    if (params && typeof params === 'object') {
      Object.keys(params).forEach(k => {
        str = str.replace(new RegExp(`\\{${k}\\}`, 'g'), String(params[k]));
      });
    }
    return str;
  }

  function getThemeText(themeId) {
    const active = getLocaleData(currentLang);
    const fallback = localeCache.en;
    return (active && active.themes && active.themes[themeId]) ||
      (fallback && fallback.themes && fallback.themes[themeId]) ||
      null;
  }

  function getBlueprintText(blueprintId) {
    const active = getLocaleData(currentLang);
    const fallback = localeCache.en;
    return (active && active.blueprints && active.blueprints[blueprintId]) ||
      (fallback && fallback.blueprints && fallback.blueprints[blueprintId]) ||
      null;
  }

  function getMilestoneText(milestoneKey) {
    const active = getLocaleData(currentLang);
    const fallback = localeCache.en;
    return (active && active.milestones && active.milestones[milestoneKey]) ||
      (fallback && fallback.milestones && fallback.milestones[milestoneKey]) ||
      null;
  }

  function getTrophyText(trophyKey) {
    const active = getLocaleData(currentLang);
    const fallback = localeCache.en;
    return (active && active.trophies && active.trophies[trophyKey]) ||
      (fallback && fallback.trophies && fallback.trophies[trophyKey]) ||
      null;
  }

  function applyStaticDomTranslations() {
    if (typeof document === 'undefined') return;
    document.documentElement.lang = currentLang;

    const titleText = t('doc_title');
    if (titleText && titleText !== 'doc_title') {
      document.title = titleText;
    }

    document.querySelectorAll('[data-i18n]').forEach(el => {
      const key = el.getAttribute('data-i18n');
      if (key) el.textContent = t(key);
    });
    document.querySelectorAll('[data-i18n-html]').forEach(el => {
      const key = el.getAttribute('data-i18n-html');
      if (key) el.innerHTML = t(key);
    });
    document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
      const key = el.getAttribute('data-i18n-placeholder');
      if (key) el.setAttribute('placeholder', t(key));
    });
    document.querySelectorAll('[data-i18n-title]').forEach(el => {
      const key = el.getAttribute('data-i18n-title');
      if (key) el.setAttribute('title', t(key));
    });

    const toggleBtn = document.getElementById('btn-lang-toggle');
    if (toggleBtn) {
      toggleBtn.textContent = getMeta().toggleBadge || (currentLang === 'es' ? '🌐 ES' : '🌐 EN');
    }
    const settingsBtnEn = document.getElementById('btn-set-lang-en');
    const settingsBtnEs = document.getElementById('btn-set-lang-es');
    settingsBtnEn?.classList.toggle('active', currentLang === 'en');
    settingsBtnEs?.classList.toggle('active', currentLang === 'es');
  }

  async function setLanguage(lang, persist = true) {
    const normalized = normalizeLanguage(lang);
    currentLang = normalized;
    if (persist) {
      try {
        localStorage.setItem(LANG_STORAGE_KEY, normalized);
      } catch (_) {}
    }
    await loadLocale(normalized);
    applyStaticDomTranslations();
    return getLocaleData(normalized);
  }

  async function init() {
    await loadLocale(currentLang);
    applyStaticDomTranslations();
    return getLocaleData(currentLang);
  }

  global.LootDropI18n = {
    LANG_STORAGE_KEY,
    init,
    loadLocale,
    getLanguage: () => currentLang,
    isSpanish: () => currentLang === 'es',
    hasExplicitLanguageSelection,
    getLocaleData,
    getMeta,
    setLanguage,
    applyStaticDomTranslations,
    getThemeText,
    getBlueprintText,
    getMilestoneText,
    getTrophyText,
    t
  };
})(typeof window !== 'undefined' ? window : globalThis);
