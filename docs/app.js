/**
 * LootDrop V4.3.0 - Complete Client Application
 * 1-to-1 Web Edition with 23 Accent Themes & Comprehensive Website Alerts
 */

// 23 Official Accent Themes (Metadata & Colors; Text loaded from /locales/{lang}.json)
const ACCENT_THEMES = [
  // Cyber (5)
  { id: 'theme-cyber-cyan', cat: 'cyber', primary: '#00F0FF', secondary: '#00FF88' },
  { id: 'theme-gog-purple', cat: 'cyber', primary: '#A855F7', secondary: '#D946EF' },
  { id: 'theme-razer-lime', cat: 'cyber', primary: '#22C55E', secondary: '#10B981' },
  { id: 'theme-inferno-red', cat: 'cyber', primary: '#EF4444', secondary: '#F97316' },
  { id: 'theme-cherry-rose', cat: 'cyber', primary: '#EC4899', secondary: '#F43F5E' },

  // Gaming (6)
  { id: 'theme-steam-blue', cat: 'gaming', primary: '#38BDF8', secondary: '#818CF8' },
  { id: 'theme-epic-indigo', cat: 'gaming', primary: '#6366F1', secondary: '#8B5CF6' },
  { id: 'theme-playstation-blue', cat: 'gaming', primary: '#0070D1', secondary: '#38BDF8' },
  { id: 'theme-xbox-green', cat: 'gaming', primary: '#107C10', secondary: '#22C55E' },
  { id: 'theme-nintendo-red', cat: 'gaming', primary: '#E60012', secondary: '#FF4D5A' },
  { id: 'theme-discord-blurple', cat: 'gaming', primary: '#5865F2', secondary: '#818CF8' },

  // Vault (5)
  { id: 'theme-golden-loot', cat: 'vault', primary: '#FBBF24', secondary: '#F59E0B' },
  { id: 'theme-ocean-teal', cat: 'vault', primary: '#14B8A6', secondary: '#06B6D4' },
  { id: 'theme-sunset-coral', cat: 'vault', primary: '#FB923C', secondary: '#F43F5E' },
  { id: 'theme-deep-royale', cat: 'vault', primary: '#8B5CF6', secondary: '#C084FC' },
  { id: 'theme-emerald-mint', cat: 'vault', primary: '#10B981', secondary: '#34D399' },

  // Rewards (7)
  { id: 'theme-backlog-slayer', cat: 'rewards', primary: '#FF0055', secondary: '#FF5500', unlockPrefix: 'backlog_slayer' },
  { id: 'theme-prism-drift', cat: 'rewards', primary: '#FF2E93', secondary: '#00E5FF', unlockPrefix: 'diagonal_drifter' },
  { id: 'theme-solar-nova', cat: 'rewards', primary: '#FF6B00', secondary: '#FFD166', unlockPrefix: 'bingo_architect' },
  { id: 'theme-toxic-overdrive', cat: 'rewards', primary: '#A3E635', secondary: '#C026D3', unlockPrefix: 'overdrive_runner' },
  { id: 'theme-aurora-sovereign', cat: 'rewards', primary: '#00FFA3', secondary: '#3B82F6', unlockPrefix: 'triple_threat' },
  { id: 'theme-vault-cleaner', cat: 'rewards', primary: '#FFD700', secondary: '#00FFCC', unlockPrefix: 'vault_cleaner', legacyPrefix: 'blackout_champion' },
  { id: 'theme-nebula-blackout', cat: 'rewards', primary: '#B026FF', secondary: '#00F0FF', unlockPrefix: 'blackout_champion' }
];

function getLocalizedThemeMeta(themeId) {
  const fromLocale = window.LootDropI18n?.getThemeText?.(themeId);
  if (fromLocale) return fromLocale;
  return { title: themeId, short: themeId, desc: '' };
}

function isThemeUnlocked(themeObj) {
  if (!themeObj || themeObj.cat !== 'rewards') return true;
  const claimed = JSON.parse(localStorage.getItem(STORAGE_KEYS.MILESTONES) || '{}');
  const keys = Object.keys(claimed);
  return keys.some(k =>
    (themeObj.unlockPrefix && k.startsWith(themeObj.unlockPrefix)) ||
    (themeObj.legacyPrefix && k.startsWith(themeObj.legacyPrefix))
  );
}

// App State & Local Storage Keys
const STORAGE_KEYS = {
  VAULT: 'lootdrop_vault_items',
  PROFILE: 'lootdrop_user_profile',
  CHALLENGES: 'lootdrop_challenge_state',
  MILESTONES: 'lootdrop_milestones_claimed',
  THEME: 'lootdrop_accent_theme',
  BG_MODE: 'lootdrop_bg_mode',
  STEAM_ID: 'lootdrop_steam_id',
  STEAM_GAMES: 'lootdrop_steam_games',
  DLC_CACHE: 'lootdrop_steam_dlc_cache',
  DLC_DISMISSED: 'lootdrop_dismissed_dlcs',
  NOTIFICATIONS: 'lootdrop_notifications',
  ALERT_SETTINGS: 'lootdrop_alert_settings',
  SUPA_KEEPALIVE_TS: 'lootdrop_supa_keepalive_ts'
};

const t = (key, params) => (window.LootDropI18n ? window.LootDropI18n.t(key, params) : key);

const DEFAULT_PROFILE_NAMES = new Set(['Vault Hunter', 'Cazador de Bóveda', 'Chasseur de Coffre']);
const DEFAULT_PROFILE_BIOS = new Set([
  'Clearing the backlog, one drop at a time.',
  'Limpiando el backlog, un drop a la vez.',
  'À la conquête du backlog, un drop à la fois.'
]);

function openDealBrowser(url, title = 'Giveaway Deal') {
  if (!url || url === '#' || url === 'about:blank') return;
  let targetUrl = url.trim();
  if (!/^https?:\/\//i.test(targetUrl)) {
    targetUrl = 'https://' + targetUrl;
  }
  window.open(targetUrl, '_blank', 'noopener,noreferrer');
}

// Anchor week for week 1 calculation (Monday Nov 9, 2026)
const ANCHOR_WEEK = { year: 2026, week: 46 };
const MAX_FUTURE_WEEKS_PREVIEW = 2;
const MAX_MANUAL_OVERRIDES = 2;

// Built-in fallback giveaways in case of network restrictions
const FALLBACK_GIVEAWAYS = [
  {
    id: 9001,
    title: "Metro 2033 Redux",
    worth: "$19.99",
    thumbnail: "https://www.gamerpower.com/offers/1/65f42c7bcfa4b.jpg",
    image: "https://www.gamerpower.com/offers/1/65f42c7bcfa4b.jpg",
    description: "Experience the definitive edition of the gripping underground post-apocalyptic classic Metro 2033.",
    instructions: "Click the claim button and add it to your Epic Games or Steam library permanently.",
    open_giveaway_url: "https://store.epicgames.com/",
    published_date: "2026-11-09 10:00:00",
    end_date: "2026-11-16 23:59:59",
    type: "Game",
    platforms: "PC, Steam, Epic Games Store"
  },
  {
    id: 9002,
    title: "Warhammer: Vermintide 2",
    worth: "$29.99",
    thumbnail: "https://www.gamerpower.com/offers/1/65bb0d970e70d.jpg",
    image: "https://www.gamerpower.com/offers/1/65bb0d970e70d.jpg",
    description: "Visceral first-person 4-player co-op melee action game set in the apocalyptic End Times of the Warhammer world.",
    instructions: "Visit Steam store page and click 'Add to Account' before the promotional offer expires.",
    open_giveaway_url: "https://store.steampowered.com/",
    published_date: "2026-11-08 14:00:00",
    end_date: "2026-11-15 18:00:00",
    type: "Game",
    platforms: "PC, Steam"
  },
  {
    id: 9003,
    title: "Fallout 76: Vault 33 Suit DLC",
    worth: "$9.99",
    thumbnail: "https://www.gamerpower.com/offers/1/6619a9f0e0bc3.jpg",
    image: "https://www.gamerpower.com/offers/1/6619a9f0e0bc3.jpg",
    description: "Claim the authentic Vault 33 jumpsuit and cosmetic bundle for Fallout 76 on PC and Xbox.",
    instructions: "Redeem the promotional key inside the Xbox or Microsoft Store app.",
    open_giveaway_url: "https://www.xbox.com/",
    published_date: "2026-11-07 12:00:00",
    end_date: "2026-11-20 23:59:59",
    type: "DLC",
    platforms: "PC, Xbox One, Xbox Series X/S"
  },
  {
    id: 9004,
    title: "The Witcher 3: Wild Hunt - Soundtrack Pack",
    worth: "$14.99",
    thumbnail: "https://www.gamerpower.com/offers/1/65b6f3453883a.jpg",
    image: "https://www.gamerpower.com/offers/1/65b6f3453883a.jpg",
    description: "Full official digital soundtrack with high fidelity FLAC audio from CD PROJEKT RED on GOG.",
    instructions: "Add directly to your GOG.com library via the giveaway banner.",
    open_giveaway_url: "https://www.gog.com/",
    published_date: "2026-11-05 09:00:00",
    end_date: "2026-11-25 23:59:59",
    type: "DLC",
    platforms: "PC, GOG"
  },
  {
    id: 9005,
    title: "Destiny 2: Legacy Collection DLC Pack",
    worth: "$59.99",
    thumbnail: "https://www.gamerpower.com/offers/1/657b4458380d4.jpg",
    image: "https://www.gamerpower.com/offers/1/657b4458380d4.jpg",
    description: "Grab three epic campaigns and multiple subclass masteries completely free on Epic Games Store.",
    instructions: "Log in to Epic Games Store and claim before the Thursday weekly rotation.",
    open_giveaway_url: "https://store.epicgames.com/",
    published_date: "2026-11-04 16:00:00",
    end_date: "2026-11-12 17:00:00",
    type: "DLC",
    platforms: "PC, Epic Games Store"
  },
  {
    id: 9006,
    title: "Amnesia: The Dark Descent",
    worth: "$19.99",
    thumbnail: "https://www.gamerpower.com/offers/1/655c65a498b83.jpg",
    image: "https://www.gamerpower.com/offers/1/655c65a498b83.jpg",
    description: "The critically acclaimed survival horror masterpiece. A heart-stopping experience of terror and desperation.",
    instructions: "Claim DRM-free on GOG.com during the autumn giveaway celebration.",
    open_giveaway_url: "https://www.gog.com/",
    published_date: "2026-11-02 11:00:00",
    end_date: "2026-11-14 15:00:00",
    type: "Game",
    platforms: "PC, GOG"
  }
];

// Catalog of 3x3 Backlog Challenge Blueprints (Text loaded dynamically from /locales/{lang}.json)
const CHALLENGE_BLUEPRINTS = [
  { id: "GOLD_TROPHY", emoji: "🏆" },
  { id: "INDIE_GAME", emoji: "🕹️" },
  { id: "IMMERSION_SESSION", emoji: "⏳" },
  { id: "VAULT_STARTER", emoji: "📦" },
  { id: "BEAT_GAME", emoji: "👑" },
  { id: "CONTROLLER_READY", emoji: "🎮" },
  { id: "TROPHY_COLLECTOR", emoji: "💎" },
  { id: "MYTHIC_HUNTER", emoji: "⚡" },
  { id: "TRI_PLAY", emoji: "🌟" },
  { id: "METACRITIC_80", emoji: "🎖️" },
  { id: "DECK_VERIFIED", emoji: "🔋" },
  { id: "QUICK_SESSION", emoji: "☕" },
  { id: "UNPLAYED_REVIVAL", emoji: "🚀" },
  { id: "FIVE_TROPHIES", emoji: "🥇" },
  { id: "COMPLETION_JOURNEY", emoji: "🎖️" },
  { id: "COMPACT_STORAGE", emoji: "💾" },
  { id: "HEAVYWEIGHT_EPIC", emoji: "🛡️" },
  { id: "GENRE_HOPPER", emoji: "🗺️" }
];

// App State
let allGiveaways = [];
let vaultItems = JSON.parse(localStorage.getItem(STORAGE_KEYS.VAULT) || '[]');
let userProfile = JSON.parse(localStorage.getItem(STORAGE_KEYS.PROFILE) || JSON.stringify({
  name: t('default_profile_name'),
  bio: t('default_profile_bio'),
  avatar: '🎮',
  title: t('default_profile_title')
}));
let alertSettings = JSON.parse(localStorage.getItem(STORAGE_KEYS.ALERT_SETTINGS) || JSON.stringify({
  master: true,
  sound: true,
  desktop: true,
  expiring: true,
  gamesOnly: false,
  highValueOnly: false,
  platforms: { steam: true, epic: true, gog: true, ps: true, xbox: true, nintendo: true }
}));
let notificationsList = JSON.parse(localStorage.getItem(STORAGE_KEYS.NOTIFICATIONS) || '[]');
let currentThemeId = localStorage.getItem(STORAGE_KEYS.THEME) || 'theme-cyber-cyan';
let currentBgMode = localStorage.getItem(STORAGE_KEYS.BG_MODE) || 'bg-dark';

let activePlatformFilter = 'all';
let activeTypeFilter = 'all';
let activeSort = 'date';
let activeSearchQuery = '';
let activeTab = 'pane-home';
let currentWeekKey = '';
let activeChallengeWeekKey = '';
let selectedGiveaway = null;

// Audio Synthesizer Context
let audioCtx = null;

// Initialization
document.addEventListener('DOMContentLoaded', async () => {
  if (window.LootDropI18n?.init) {
    await window.LootDropI18n.init();
  }
  if (!localStorage.getItem(STORAGE_KEYS.PROFILE)) {
    userProfile = {
      name: t('default_profile_name'),
      bio: t('default_profile_bio'),
      avatar: '🎮',
      title: t('default_profile_title')
    };
  }
  initLanguageControls();
  applyThemeAndBg(currentThemeId, currentBgMode);
  initNavigation();
  initWeekKeys();
  renderThemeCatalog('all');
  initAlertSettingsUI();
  initNotificationsList();
  setupEventListeners();
  checkNotificationPermissionState();
  fetchGiveaways();
  renderProfile();
  updateStats();
  fetchPublicReviewsFromR2();
  initReviewsModal();
  initSteamAndDlcFeatures();
  pingSupabaseKeepAlive();
});

function initLanguageControls() {
  if (!window.LootDropI18n) return;
  window.LootDropI18n.applyStaticDomTranslations();
  syncLanguagePickerSelection();

  document.getElementById('btn-lang-toggle')?.addEventListener('click', () => {
    const order = ['en', 'es', 'fr'];
    const cur = window.LootDropI18n.getLanguage();
    const next = order[(order.indexOf(cur) + 1) % order.length];
    switchAppLanguage(next, true);
  });

  document.getElementById('btn-set-lang-en')?.addEventListener('click', () => {
    switchAppLanguage('en', true);
  });
  document.getElementById('btn-set-lang-es')?.addEventListener('click', () => {
    switchAppLanguage('es', true);
  });
  document.getElementById('btn-set-lang-fr')?.addEventListener('click', () => {
    switchAppLanguage('fr', true);
  });

  document.getElementById('btn-pick-lang-en')?.addEventListener('click', async () => {
    await switchAppLanguage('en', true);
    document.getElementById('modal-language-picker')?.classList.remove('open');
  });
  document.getElementById('btn-pick-lang-es')?.addEventListener('click', async () => {
    await switchAppLanguage('es', true);
    document.getElementById('modal-language-picker')?.classList.remove('open');
  });
  document.getElementById('btn-pick-lang-fr')?.addEventListener('click', async () => {
    await switchAppLanguage('fr', true);
    document.getElementById('modal-language-picker')?.classList.remove('open');
  });

  const langModal = document.getElementById('modal-language-picker');
  document.getElementById('close-lang-modal-btn')?.addEventListener('click', () => {
    if (!window.LootDropI18n.hasExplicitLanguageSelection()) {
      switchAppLanguage(window.LootDropI18n.getLanguage(), false);
    }
  });
  langModal?.addEventListener('click', (e) => {
    if (e.target === langModal && !window.LootDropI18n.hasExplicitLanguageSelection()) {
      switchAppLanguage(window.LootDropI18n.getLanguage(), false);
    }
  });

  if (!window.LootDropI18n.hasExplicitLanguageSelection()) {
    langModal?.classList.add('open');
  }
}

function syncLanguagePickerSelection() {
  if (!window.LootDropI18n) return;
  const lang = window.LootDropI18n.getLanguage();
  document.getElementById('btn-pick-lang-en')?.classList.toggle('active', lang === 'en');
  document.getElementById('btn-pick-lang-es')?.classList.toggle('active', lang === 'es');
  document.getElementById('btn-pick-lang-fr')?.classList.toggle('active', lang === 'fr');
}

async function switchAppLanguage(lang, showToastMsg = true) {
  if (!window.LootDropI18n) return;
  await window.LootDropI18n.setLanguage(lang, true);
  syncLanguagePickerSelection();

  applyThemeAndBg(currentThemeId, currentBgMode);
  renderHome();
  renderRadar();
  renderCalendar();
  renderVault();
  renderNotificationsList();
  renderMiniChallengeWidget();
  renderThemeCatalog(document.querySelector('#theme-cat-tabs .theme-cat-tab.active')?.dataset?.cat || 'all');
  updateStats();
  renderProfile();
  updateSettingsAppRatingUI();
  if (typeof refreshSteamStatusHint === 'function') refreshSteamStatusHint();
  if (typeof syncReviewModalLanguage === 'function') syncReviewModalLanguage();

  if (document.getElementById('modal-challenge')?.classList.contains('open')) {
    renderChallengeModal();
  }
  if (document.getElementById('modal-giveaway-detail')?.classList.contains('open') && selectedGiveaway) {
    openGiveawayDetail(selectedGiveaway);
  }
  if (document.getElementById('modal-dlc-hub')?.classList.contains('open')) {
    const gameSelect = document.getElementById('dlc-hub-game-select');
    const firstOpt = gameSelect?.querySelector('option[value="all"]');
    if (firstOpt) firstOpt.textContent = t('dlc_select_all_count', { count: steamGamesList.length });
    updateDlcHubHeader();
    updateDlcHubStatusUI();
    renderDlcHubList();
  }

  if (steamGamesList.length > 0 && !isDlcCacheFresh()) {
    refreshWebSteamDlcRecommendations(false);
  }

  if (showToastMsg) {
    const toastKey = lang === 'es' ? 'toast_lang_es' : (lang === 'fr' ? 'toast_lang_fr' : 'toast_lang_en');
    showToast(t(toastKey), '🌐');
  }
}

// Audio Synthesizer (Gaming Chime)
function playGamerChime() {
  if (!alertSettings.sound) return;
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    if (!audioCtx) audioCtx = new AudioContext();
    if (audioCtx.state === 'suspended') audioCtx.resume();

    // Notes: C5, E5, G5, C6 (classic victory arpeggio)
    const notes = [523.25, 659.25, 783.99, 1046.50];
    const now = audioCtx.currentTime;

    notes.forEach((freq, idx) => {
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();

      osc.type = 'triangle';
      osc.frequency.setValueAtTime(freq, now + idx * 0.08);

      gain.gain.setValueAtTime(0, now + idx * 0.08);
      gain.gain.linearRampToValueAtTime(0.18, now + idx * 0.08 + 0.02);
      gain.gain.exponentialRampToValueAtTime(0.001, now + idx * 0.08 + 0.28);

      osc.connect(gain);
      gain.connect(audioCtx.destination);

      osc.start(now + idx * 0.08);
      osc.stop(now + idx * 0.08 + 0.3);
    });
  } catch (err) {
    console.warn('Audio chime playback failed:', err);
  }
}

// Web Drop Alerts & Notifications System
function checkNotificationPermissionState() {
  const banner = document.getElementById('perm-banner');
  if (!banner) return;

  if ('Notification' in window) {
    if (Notification.permission === 'default' && alertSettings.desktop) {
      banner.style.display = 'flex';
    } else {
      banner.style.display = 'none';
    }
  } else {
    banner.style.display = 'none';
  }
}

async function requestNotificationPermission() {
  if ('Notification' in window) {
    const permission = await Notification.requestPermission();
    if (permission === 'granted') {
      showToast(t('toast_alerts_on'), '🔔');
      playGamerChime();
      document.getElementById('perm-banner').style.display = 'none';
    } else {
      showToast(t('toast_alerts_off'), 'ℹ️');
      document.getElementById('perm-banner').style.display = 'none';
    }
  }
}

function triggerWebsiteAlert(title, message, url = '#', icon = '🎁') {
  // 1. Play synthesized gamer chime
  playGamerChime();

  // 2. Display on-page toast popup
  showToast(message, icon);

  // 3. Trigger native desktop notification if permitted
  if ('Notification' in window && Notification.permission === 'granted' && alertSettings.desktop) {
    try {
      const notif = new Notification(title, {
        body: message,
        icon: 'data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>🎁</text></svg>'
      });
      notif.onclick = () => {
        window.focus();
        if (url && url !== '#') window.open(url, '_blank');
      };
    } catch (_) {}
  }

  // 4. Add to notification history
  notificationsList.unshift({
    id: Date.now(),
    title,
    desc: message,
    url,
    timestamp: Date.now(),
    unread: true
  });
  if (notificationsList.length > 50) notificationsList.pop();
  localStorage.setItem(STORAGE_KEYS.NOTIFICATIONS, JSON.stringify(notificationsList));
  renderNotificationsList();
}

const SEED_NOTIF_KEYS = {
  1: { title: 'notif_seed1_title', desc: 'notif_seed1_desc' },
  2: { title: 'notif_seed2_title', desc: 'notif_seed2_desc' },
  3: { title: 'notif_seed3_title', desc: 'notif_seed3_desc' }
};

function initNotificationsList() {
  if (notificationsList.length === 0) {
    notificationsList = [
      { id: 1, title: t('notif_seed1_title'), desc: t('notif_seed1_desc'), timestamp: Date.now(), unread: true },
      { id: 2, title: t('notif_seed2_title'), desc: t('notif_seed2_desc'), timestamp: Date.now() - 3600000, unread: true },
      { id: 3, title: t('notif_seed3_title'), desc: t('notif_seed3_desc'), timestamp: Date.now() - 7200000, unread: false }
    ];
    localStorage.setItem(STORAGE_KEYS.NOTIFICATIONS, JSON.stringify(notificationsList));
  }
  renderNotificationsList();
}

function renderNotificationsList() {
  const container = document.getElementById('notif-list-container');
  const countBadge = document.getElementById('notif-count');
  if (!container) return;

  const unreadCount = notificationsList.filter(n => n.unread).length;
  if (countBadge) {
    countBadge.textContent = unreadCount;
    countBadge.style.display = unreadCount > 0 ? 'flex' : 'none';
  }

  if (notificationsList.length === 0) {
    container.innerHTML = `
      <div style="padding: 32px; text-align: center; color: var(--color-text-muted);">
        <span style="font-size: 32px; display: block; margin-bottom: 8px;">📭</span>
        <p>${escapeHtml(t('notif_empty'))}</p>
      </div>
    `;
    return;
  }

  container.innerHTML = notificationsList.map(n => {
    const seedKey = SEED_NOTIF_KEYS[n.id];
    const displayTitle = seedKey ? t(seedKey.title) : n.title;
    const displayDesc = seedKey ? t(seedKey.desc) : n.desc;
    return `
      <div class="notif-item ${n.unread ? 'unread' : ''}" data-id="${n.id}">
        <span style="font-size: 22px;">🎁</span>
        <div class="notif-text">
          <strong>${escapeHtml(displayTitle)}</strong>
          <span>${escapeHtml(displayDesc)}</span>
        </div>
        ${n.unread ? '<span class="notif-dot"></span>' : ''}
      </div>
    `;
  }).join('');

  container.querySelectorAll('.notif-item').forEach(item => {
    item.addEventListener('click', () => {
      const id = parseInt(item.dataset.id, 10);
      const target = notificationsList.find(n => n.id === id);
      if (target) {
        target.unread = false;
        localStorage.setItem(STORAGE_KEYS.NOTIFICATIONS, JSON.stringify(notificationsList));
        renderNotificationsList();
        if (target.url && target.url !== '#') window.open(target.url, '_blank');
      }
    });
  });
}

// Theme & Background Mode Management
function applyThemeAndBg(themeId, bgMode) {
  currentThemeId = themeId;
  currentBgMode = bgMode;

  document.body.className = `${themeId} ${bgMode}`;
  localStorage.setItem(STORAGE_KEYS.THEME, themeId);
  localStorage.setItem(STORAGE_KEYS.BG_MODE, bgMode);

  // Update theme badge in settings
  const foundTheme = ACCENT_THEMES.find(t => t.id === themeId);
  const foundMeta = foundTheme ? getLocalizedThemeMeta(foundTheme.id) : null;
  const badge = document.getElementById('current-theme-badge');
  if (badge && foundTheme) {
    badge.textContent = foundMeta.title;
    badge.style.backgroundColor = foundTheme.primary;
    badge.style.color = '#000';
  }

  // Update BG mode buttons
  document.querySelectorAll('.bg-mode-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.bg === bgMode);
  });
}

function renderThemeCatalog(category = 'all') {
  const container = document.getElementById('full-theme-grid');
  if (!container) return;

  const filtered = category === 'all'
    ? ACCENT_THEMES
    : ACCENT_THEMES.filter(th => th.cat === category);

  container.innerHTML = filtered.map(th => {
    const unlocked = isThemeUnlocked(th);
    const meta = getLocalizedThemeMeta(th.id);
    return `
      <div class="theme-btn-card ${th.id === currentThemeId ? 'active' : ''} ${unlocked ? '' : 'locked'}" data-id="${th.id}">
        <div class="theme-preview-dots">
          <span class="theme-dot" style="background: ${th.primary};"></span>
          <span class="theme-dot secondary" style="background: ${th.secondary};"></span>
        </div>
        <div class="theme-info">
          <span class="theme-title">${unlocked ? '' : '🔒 '}${escapeHtml(meta.title)}</span>
          <span class="theme-cat">${unlocked ? th.cat.toUpperCase() : escapeHtml(t('ch_locked_btn').toUpperCase())}</span>
        </div>
      </div>
    `;
  }).join('');

  container.querySelectorAll('.theme-btn-card').forEach(card => {
    card.addEventListener('click', () => {
      const id = card.dataset.id;
      const themeObj = ACCENT_THEMES.find(th => th.id === id);
      const meta = getLocalizedThemeMeta(id);
      if (themeObj && !isThemeUnlocked(themeObj)) {
        showToast(t('toast_theme_locked', { title: meta.title }), '🔒');
        return;
      }
      applyThemeAndBg(id, currentBgMode);
      renderThemeCatalog(category);
      playGamerChime();
      showToast(t('toast_theme_applied', { title: meta.title }), '🎨');
    });
  });
}

// Week & Challenge Calculations
function getISOWeekNumber(d) {
  const date = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
  date.setUTCDate(date.getUTCDate() + 4 - (date.getUTCDay() || 7));
  const yearStart = new Date(Date.UTC(date.getUTCFullYear(), 0, 1));
  const weekNo = Math.ceil((((date - yearStart) / 86400000) + 1) / 7);
  return { year: date.getUTCFullYear(), week: weekNo };
}

function getWeekKey(year, week) {
  return `${year}-W${String(week).padStart(2, '0')}`;
}

function initWeekKeys() {
  const now = new Date();
  const current = getISOWeekNumber(now);
  currentWeekKey = getWeekKey(current.year, current.week);
  activeChallengeWeekKey = currentWeekKey;
}

function getRelativeWeekNumber(weekKey) {
  const parts = weekKey.split('-W');
  const y = parseInt(parts[0], 10);
  const w = parseInt(parts[1], 10);
  const diffWeeks = (y - ANCHOR_WEEK.year) * 52 + (w - ANCHOR_WEEK.week);
  return Math.max(1, diffWeeks + 1);
}

// Navigation & Tab Switching
function initNavigation() {
  const navItems = document.querySelectorAll('.nav-item');
  navItems.forEach(item => {
    item.addEventListener('click', () => {
      const targetTab = item.dataset.tab;
      switchTab(targetTab);
    });
  });

  document.getElementById('brand-home-btn')?.addEventListener('click', () => switchTab('pane-home'));
  document.getElementById('hero-browse-btn')?.addEventListener('click', () => switchTab('pane-discover'));
  document.getElementById('view-all-drops-btn')?.addEventListener('click', () => switchTab('pane-discover'));
}

const LAZY_THUMB_PLACEHOLDER = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7';
let lazyThumbObserver = null;

function createLazyThumbImgHtml(src, alt, className = 'giveaway-card-thumb', fallbackSrc = '') {
  const safeAlt = escapeHtml(alt || '');
  const primary = src || '';
  const fallbackAttr = fallbackSrc && fallbackSrc !== primary ? ` data-fallback-src="${escapeHtml(fallbackSrc)}"` : '';
  return `<img src="${LAZY_THUMB_PLACEHOLDER}" data-src="${escapeHtml(primary)}"${fallbackAttr} alt="${safeAlt}" class="${className} lazy-thumb" loading="lazy" decoding="async" fetchpriority="low">`;
}

function markThumbnailLoaded(img) {
  if (!img) return;
  const wrap = img.closest('.giveaway-card-thumb-wrap, .dlc-thumb-wrap');
  const finish = () => {
    img.classList.add('is-loaded');
    if (wrap) {
      wrap.classList.remove('is-error');
      wrap.classList.add('is-loaded');
    }
  };
  if (typeof img.decode === 'function') {
    img.decode().then(finish, finish);
  } else {
    finish();
  }
}

function loadLazyThumbnail(img) {
  if (!img || img.dataset.lazyState === 'loading' || img.dataset.lazyState === 'done') return;
  const targetSrc = img.dataset.src || '';
  const wrap = img.closest('.giveaway-card-thumb-wrap, .dlc-thumb-wrap');
  delete img.dataset.src;

  if (!targetSrc) {
    img.dataset.lazyState = 'error';
    if (wrap) wrap.classList.add('is-error');
    return;
  }

  img.dataset.lazyState = 'loading';
  const fallbackSrc = img.dataset.fallbackSrc || '';
  let activeUrl = targetSrc;
  let settled = false;

  const BgImage = typeof Image === 'function' ? Image : null;
  if (!BgImage) {
    img.src = activeUrl;
    img.dataset.lazyState = 'done';
    markThumbnailLoaded(img);
    return;
  }

  const bgImg = new BgImage();
  bgImg.decoding = 'async';

  const commitLoaded = () => {
    if (settled) return;
    settled = true;
    bgImg.onload = null;
    bgImg.onerror = null;
    img.src = activeUrl;
    img.dataset.lazyState = 'done';
    markThumbnailLoaded(img);
  };

  bgImg.onload = commitLoaded;

  bgImg.onerror = () => {
    if (settled) return;
    if (fallbackSrc && activeUrl !== fallbackSrc) {
      delete img.dataset.fallbackSrc;
      activeUrl = fallbackSrc;
      bgImg.src = fallbackSrc;
      if (bgImg.complete && bgImg.naturalWidth > 0) commitLoaded();
      return;
    }
    settled = true;
    bgImg.onload = null;
    bgImg.onerror = null;
    img.dataset.lazyState = 'error';
    if (wrap) wrap.classList.add('is-error');
  };

  bgImg.src = activeUrl;
  if (bgImg.complete && bgImg.naturalWidth > 0) {
    commitLoaded();
  }
}

function initLazyThumbnails(root = document) {
  const scope = root || document;
  const images = scope.querySelectorAll('img.lazy-thumb[data-src]');
  if (!images.length) return;

  if (!('IntersectionObserver' in window)) {
    images.forEach(img => loadLazyThumbnail(img));
    return;
  }

  if (!lazyThumbObserver) {
    lazyThumbObserver = new IntersectionObserver((entries, observer) => {
      entries.forEach(entry => {
        if (entry.isIntersecting || entry.intersectionRatio > 0) {
          const img = entry.target;
          observer.unobserve(img);
          loadLazyThumbnail(img);
        }
      });
    }, {
      rootMargin: '240px 0px',
      threshold: 0.01
    });
  }

  images.forEach(img => {
    lazyThumbObserver.unobserve(img);
    lazyThumbObserver.observe(img);
  });
}

function switchTab(tabId) {
  activeTab = tabId;

  document.querySelectorAll('.tab-pane').forEach(pane => {
    pane.classList.toggle('active', pane.id === tabId);
  });

  document.querySelectorAll('.nav-item').forEach(item => {
    item.classList.toggle('active', item.dataset.tab === tabId);
  });

  window.scrollTo({ top: 0, behavior: 'smooth' });

  if (tabId === 'pane-vault') {
    renderVault();
  } else if (tabId === 'pane-calendar') {
    renderCalendar();
  }

  const activePane = document.getElementById(tabId);
  if (activePane) initLazyThumbnails(activePane);
}

// Fetching Live Giveaways (GamerPower API)
async function fetchGiveaways() {
  try {
    const data = await fetchJsonWithCorsFallback('https://www.gamerpower.com/api/giveaways', {
      skipDirect: false,
      timeoutMs: 9000
    });
    if (Array.isArray(data) && data.length > 0) {
      allGiveaways = data;
    } else {
      allGiveaways = FALLBACK_GIVEAWAYS;
    }
  } catch (err) {
    console.warn('Using bundled fallback giveaways:', err.message);
    allGiveaways = FALLBACK_GIVEAWAYS;
  }

  renderHome();
  renderRadar();
  renderCalendar();
  updateStats();
  renderMiniChallengeWidget();
}

// Render Home Tab
function renderHome() {
  const featuredContainer = document.getElementById('featured-drops-container');
  const recentContainer = document.getElementById('home-recent-drops-container');
  if (!featuredContainer || !recentContainer) return;

  const featured = allGiveaways.slice(0, 4);
  const recent = allGiveaways.slice(4, 10);

  featuredContainer.innerHTML = featured.map(g => createGiveawayCardHtml(g, true)).join('');
  recentContainer.innerHTML = recent.map(g => createGiveawayCardHtml(g, false)).join('');

  initLazyThumbnails(featuredContainer);
  initLazyThumbnails(recentContainer);
  attachCardEvents();
  renderHomeDlcSection();
}

// Render Discover / Radar Tab
function renderRadar() {
  const container = document.getElementById('radar-drops-container');
  const countLabel = document.getElementById('radar-count-label');
  if (!container) return;

  let filtered = [...allGiveaways];

  if (activePlatformFilter !== 'all') {
    const requested = activePlatformFilter.toLowerCase().split(',');
    filtered = filtered.filter(g => {
      const p = (g.platforms || '').toLowerCase();
      return requested.some(req => p.includes(req.trim()));
    });
  }

  if (activeTypeFilter !== 'all') {
    filtered = filtered.filter(g => (g.type || '').toLowerCase().includes(activeTypeFilter));
  }

  if (activeSearchQuery.trim()) {
    const q = activeSearchQuery.toLowerCase();
    filtered = filtered.filter(g =>
      (g.title || '').toLowerCase().includes(q) ||
      (g.description || '').toLowerCase().includes(q) ||
      (g.platforms || '').toLowerCase().includes(q)
    );
  }

  if (activeSort === 'value') {
    filtered.sort((a, b) => parsePrice(b.worth) - parsePrice(a.worth));
  } else if (activeSort === 'popularity') {
    filtered.sort((a, b) => (parseInt(b.users, 10) || 0) - (parseInt(a.users, 10) || 0));
  } else if (activeSort === 'alphabetical') {
    filtered.sort((a, b) => (a.title || '').localeCompare(b.title || ''));
  } else {
    filtered.sort((a, b) => new Date(b.published_date || 0) - new Date(a.published_date || 0));
  }

  if (countLabel) {
    countLabel.textContent = t('radar_showing', { count: filtered.length });
  }

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; padding: 40px; text-align: center; color: var(--color-text-muted);">
        <span style="font-size: 36px; display: block; margin-bottom: 10px;">🔍</span>
        <h3>${escapeHtml(t('radar_empty_title'))}</h3>
        <p>${escapeHtml(t('radar_empty_desc'))}</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(g => createGiveawayCardHtml(g, false)).join('');
  initLazyThumbnails(container);
  attachCardEvents();
}

// Render Calendar / Expiration Radar Tab
function renderCalendar() {
  const container = document.getElementById('calendar-groups-container');
  if (!container) return;

  const now = new Date();
  const endingToday = [];
  const endingThisWeek = [];
  const endingLater = [];
  const ongoing = [];

  allGiveaways.forEach(g => {
    if (!g.end_date || g.end_date.startsWith('N/A')) {
      ongoing.push(g);
      return;
    }

    const end = new Date(g.end_date);
    const diffHours = (end - now) / (1000 * 60 * 60);

    if (diffHours <= 24 && diffHours > -24) {
      endingToday.push(g);
    } else if (diffHours > 24 && diffHours <= 168) {
      endingThisWeek.push(g);
    } else if (diffHours > 168) {
      endingLater.push(g);
    } else {
      ongoing.push(g);
    }
  });

  const renderGroup = (title, items, badgeColor) => {
    if (items.length === 0) return '';
    return `
      <div class="calendar-group">
        <div class="calendar-group-header">
          <span style="display:inline-block; width:10px; height:10px; border-radius:50%; background:${badgeColor};"></span>
          <h3 class="calendar-group-title">${title}</h3>
          <span class="calendar-group-count">(${items.length})</span>
        </div>
        <div class="drops-grid">
          ${items.map(g => createGiveawayCardHtml(g, false)).join('')}
        </div>
      </div>
    `;
  };

  container.innerHTML =
    renderGroup(t('cal_ending_24h'), endingToday, '#EF4444') +
    renderGroup(t('cal_ending_week'), endingThisWeek, '#F59E0B') +
    renderGroup(t('cal_ending_month'), endingLater, '#3B82F6') +
    renderGroup(t('cal_ongoing'), ongoing, '#10B981');

  initLazyThumbnails(container);
  attachCardEvents();
}

// Render Vault / Claimed Tab
function renderVault() {
  const container = document.getElementById('vault-items-container');
  const countVal = document.getElementById('vault-total-count');
  const savedVal = document.getElementById('vault-saved-val');
  const beatenVal = document.getElementById('vault-beaten-count');
  if (!container) return;

  let totalWorth = 0;
  let beatenCount = 0;

  vaultItems.forEach(item => {
    totalWorth += parsePrice(item.worth);
    if (item.playStatus === 'beaten') beatenCount++;
  });

  if (countVal) countVal.textContent = vaultItems.length;
  if (savedVal) savedVal.textContent = `$${totalWorth.toFixed(2)}`;
  if (beatenVal) beatenVal.textContent = beatenCount;

  const steamCountEl = document.getElementById('vault-steam-count');
  if (steamCountEl) steamCountEl.textContent = steamGamesList.length;

  if (vaultViewMode === 'steam') {
    if (steamGamesList.length === 0) {
      container.innerHTML = `
        <div style="grid-column: 1 / -1; padding: 48px; text-align: center; background: var(--color-surface); border: 1px dashed var(--color-border); border-radius: var(--radius-xl);">
          <span style="font-size: 40px; display: block; margin-bottom: 12px;">🎮</span>
          <h3>${escapeHtml(t('steam_empty_title'))}</h3>
          <p style="color: var(--color-text-muted); font-size: 14px; margin-top: 6px; margin-bottom: 16px;">
            ${escapeHtml(t('steam_empty_desc'))}
          </p>
          <button class="btn btn-primary" onclick="switchTab('pane-settings')">${escapeHtml(t('steam_empty_btn'))}</button>
        </div>
      `;
      return;
    }

    const sortedSteam = [...steamGamesList].sort((a, b) => (b.playtime2WeeksMinutes || 0) - (a.playtime2WeeksMinutes || 0) || (b.playtimeMinutes || 0) - (a.playtimeMinutes || 0));
    container.innerHTML = sortedSteam.map(sg => {
      const hours = ((sg.playtimeMinutes || 0) / 60).toFixed(1);
      const headerImg = `https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/${sg.appId}/header.jpg`;
      const fallbackImg = sg.headerImageUrl || headerImg;
      const storeUrl = sg.storeUrl || `https://store.steampowered.com/app/${sg.appId}`;
      return `
        <div class="giveaway-card steam-library-card" data-appid="${sg.appId}">
          <div class="giveaway-card-thumb-wrap">
            ${createLazyThumbImgHtml(headerImg, sg.name, 'giveaway-card-thumb', fallbackImg)}
            <div class="card-top-badges">
              <span class="badge-platform">🎮 STEAM</span>
            </div>
            <div class="badge-price">${sg.playtimeMinutes > 0 ? escapeHtml(t('steam_hours_played', { hours })) : escapeHtml(t('steam_unplayed'))}</div>
          </div>
          <div class="giveaway-card-body">
            <h3 class="giveaway-card-title">${escapeHtml(sg.name)}</h3>
            <div class="card-actions-row">
              <button class="btn btn-sm btn-dlc open-game-dlcs-btn" data-appid="${sg.appId}" style="flex:1;">
                ${escapeHtml(t('steam_dlcs_btn'))}
              </button>
              <a href="${storeUrl}" target="_blank" rel="noopener" class="btn btn-sm btn-outline" onclick="event.stopPropagation()">
                ${escapeHtml(t('steam_store_btn'))}
              </a>
            </div>
          </div>
        </div>
      `;
    }).join('');

    container.querySelectorAll('.open-game-dlcs-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const appId = parseInt(btn.dataset.appid, 10);
        if (appId > 0) openDlcHubModal(appId);
      });
    });
    initLazyThumbnails(container);
    return;
  }

  const activeStatusChip = document.querySelector('#vault-status-chips .filter-chip.active')?.dataset.status || 'all';
  let displayed = vaultItems;

  if (activeStatusChip !== 'all') {
    displayed = displayed.filter(i => (i.playStatus || 'backlog') === activeStatusChip);
  }

  if (displayed.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; padding: 48px; text-align: center; background: var(--color-surface); border: 1px dashed var(--color-border); border-radius: var(--radius-xl);">
        <span style="font-size: 40px; display: block; margin-bottom: 12px;">💎</span>
        <h3>${escapeHtml(t('vault_empty_title'))}</h3>
        <p style="color: var(--color-text-muted); font-size: 14px; margin-top: 6px;">
          ${escapeHtml(t('vault_empty_desc'))}
        </p>
      </div>
    `;
    return;
  }

  const statusBadgeLabel = (st) => {
    switch (st) {
      case 'playing': return t('status_badge_playing');
      case 'beaten': return t('status_badge_beaten');
      case 'dropped': return t('status_badge_paused');
      default: return t('status_badge_backlog');
    }
  };

  container.innerHTML = displayed.map(item => `
    <div class="giveaway-card" data-id="${item.id}">
      <div class="giveaway-card-thumb-wrap">
        ${createLazyThumbImgHtml(item.thumbnail || item.image || '', item.title, 'giveaway-card-thumb', item.thumbnail && item.image ? item.image : '')}
        <div class="card-top-badges">
          <span class="badge-platform">${escapeHtml(item.platforms || 'PC')}</span>
          <span class="badge-type">${escapeHtml(statusBadgeLabel(item.playStatus || 'backlog'))}</span>
        </div>
        <div class="badge-price">${escapeHtml(item.worth && item.worth !== 'N/A' ? item.worth : t('card_free'))}</div>
      </div>
      <div class="giveaway-card-body">
        <h3 class="giveaway-card-title">${escapeHtml(item.title)}</h3>
        <div class="giveaway-card-meta">
          <span>${escapeHtml(t('card_status_lbl'))}</span>
          <select class="custom-select vault-status-select" data-id="${item.id}" onclick="event.stopPropagation()">
            <option value="backlog" ${item.playStatus === 'backlog' ? 'selected' : ''}>${escapeHtml(t('vault_status_backlog'))}</option>
            <option value="playing" ${item.playStatus === 'playing' ? 'selected' : ''}>${escapeHtml(t('vault_status_playing'))}</option>
            <option value="beaten" ${item.playStatus === 'beaten' ? 'selected' : ''}>${escapeHtml(t('vault_status_beaten'))}</option>
            <option value="dropped" ${item.playStatus === 'dropped' ? 'selected' : ''}>${escapeHtml(t('vault_status_paused'))}</option>
          </select>
        </div>
        <div class="card-actions-row">
          <a href="${item.open_giveaway_url || '#'}" target="_blank" rel="noopener" class="btn btn-sm btn-primary claim-deal-btn" data-url="${item.open_giveaway_url || ''}" data-title="${escapeHtml(item.title)}" style="flex:1;" onclick="event.stopPropagation()">
            ${escapeHtml(t('card_launch_claim'))}
          </a>
          <button class="btn-vault-icon remove-vault-btn" data-id="${item.id}" title="${escapeHtml(t('card_remove_vault_title'))}" onclick="event.stopPropagation()">
            🗑️
          </button>
        </div>
      </div>
    </div>
  `).join('');

  container.querySelectorAll('.vault-status-select').forEach(sel => {
    sel.addEventListener('change', (e) => {
      const id = parseInt(e.target.dataset.id, 10);
      const newStatus = e.target.value;
      const target = vaultItems.find(v => v.id === id);
      if (target) {
        target.playStatus = newStatus;
        localStorage.setItem(STORAGE_KEYS.VAULT, JSON.stringify(vaultItems));
        playGamerChime();
        const statusLabel = e.target.options[e.target.selectedIndex]?.textContent || statusBadgeLabel(newStatus);
        showToast(t('toast_status_updated', { status: statusLabel }), '🎮');
        renderVault();
        updateStats();
      }
    });
  });

  container.querySelectorAll('.remove-vault-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const id = parseInt(btn.dataset.id, 10);
      vaultItems = vaultItems.filter(v => v.id !== id);
      localStorage.setItem(STORAGE_KEYS.VAULT, JSON.stringify(vaultItems));
      showToast(t('toast_removed_vault'), '🗑️');
      renderVault();
      updateStats();
    });
  });

  initLazyThumbnails(container);
  attachCardEvents();
}

// Giveaway Card HTML Generator
function createGiveawayCardHtml(g, isFeatured = false) {
  const isSaved = vaultItems.some(v => v.id === g.id);
  const platformsStr = g.platforms || 'PC';
  const freeLbl = t('card_free');
  const priceStr = g.worth && g.worth !== 'N/A' ? `${g.worth} ${freeLbl}` : freeLbl;
  const primaryThumb = g.thumbnail || g.image || '';
  const fallbackThumb = g.thumbnail && g.image && g.thumbnail !== g.image ? g.image : '';

  return `
    <div class="giveaway-card ${isFeatured ? 'featured' : ''}" data-id="${g.id}">
      <div class="giveaway-card-thumb-wrap">
        ${createLazyThumbImgHtml(primaryThumb, g.title, 'giveaway-card-thumb', fallbackThumb)}
        <div class="card-top-badges">
          <span class="badge-platform">${escapeHtml(platformsStr)}</span>
          <span class="badge-type">${(g.type || 'GAME').toUpperCase()}</span>
        </div>
        <div class="badge-price">${escapeHtml(priceStr)}</div>
      </div>
      <div class="giveaway-card-body">
        <h3 class="giveaway-card-title">${escapeHtml(g.title)}</h3>
        <div class="giveaway-card-meta">
          <span>${formatExpiration(g.end_date)}</span>
          <span>👥 ${g.users ? `${g.users} ${escapeHtml(t('card_claims_suffix'))}` : escapeHtml(t('card_active'))}</span>
        </div>
        <div class="card-actions-row">
          <a href="${g.open_giveaway_url || '#'}" target="_blank" rel="noopener" class="btn btn-sm btn-primary claim-deal-btn" data-url="${g.open_giveaway_url || ''}" data-title="${escapeHtml(g.title)}" style="flex:1;" onclick="event.stopPropagation()">
            ${escapeHtml(t('card_claim_free'))}
          </a>
          <button class="btn-vault-icon ${isSaved ? 'active' : ''}" data-id="${g.id}" title="${escapeHtml(isSaved ? t('card_in_vault_title') : t('card_add_vault_title'))}" onclick="event.stopPropagation(); toggleVaultItem(${g.id});">
            ${isSaved ? '💎' : '📥'}
          </button>
        </div>
      </div>
    </div>
  `;
}

function attachCardEvents() {
  document.querySelectorAll('.claim-deal-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
    });
  });

  document.querySelectorAll('.giveaway-card').forEach(card => {
    card.addEventListener('click', () => {
      const id = parseInt(card.dataset.id, 10);
      const giveaway = allGiveaways.find(g => g.id === id) || vaultItems.find(v => v.id === id);
      if (giveaway) {
        openGiveawayDetail(giveaway);
      }
    });
  });
}

function toggleVaultItem(id) {
  const item = allGiveaways.find(g => g.id === id);
  if (!item) return;

  const existingIndex = vaultItems.findIndex(v => v.id === id);
  if (existingIndex >= 0) {
    vaultItems.splice(existingIndex, 1);
    showToast(t('toast_removed_vault_short'), '📥');
  } else {
    vaultItems.push({
      ...item,
      playStatus: 'backlog',
      addedAt: Date.now()
    });
    playGamerChime();
    showToast(t('toast_added_vault'), '💎');
  }

  localStorage.setItem(STORAGE_KEYS.VAULT, JSON.stringify(vaultItems));
  updateStats();
  renderHome();
  renderRadar();
}

// Giveaway Detail Dialog
function openGiveawayDetail(g) {
  selectedGiveaway = g;
  const modal = document.getElementById('modal-giveaway-detail');
  if (!modal) return;

  const freeLbl = t('card_free');
  document.getElementById('detail-banner-img').src = g.image || g.thumbnail || '';
  document.getElementById('detail-title').textContent = g.title || '';
  document.getElementById('detail-platform').textContent = g.platforms || 'PC';
  document.getElementById('detail-type').textContent = (g.type || 'GAME').toUpperCase();
  document.getElementById('detail-worth').textContent = g.worth && g.worth !== 'N/A' ? `${g.worth} ${freeLbl}` : freeLbl;
  document.getElementById('detail-expiration').textContent = formatExpiration(g.end_date);
  document.getElementById('detail-instructions').textContent = g.instructions || t('det_default_instr');
  document.getElementById('detail-description').textContent = g.description || '';
  const claimLink = document.getElementById('detail-claim-link');
  claimLink.href = g.open_giveaway_url || '#';
  claimLink.onclick = null;

  const isSaved = vaultItems.some(v => v.id === g.id);
  const vaultBtn = document.getElementById('detail-vault-toggle');
  vaultBtn.textContent = isSaved ? t('det_remove_vault') : t('det_add_vault');

  // Check Vault & Steam Ownership
  const ownershipBanner = document.getElementById('detail-ownership-banner');
  const ownershipIcon = document.getElementById('detail-ownership-icon');
  const ownershipTitle = document.getElementById('detail-ownership-title');
  const ownershipSubtitle = document.getElementById('detail-ownership-subtitle');

  const inVault = isSaved;
  const steamGames = JSON.parse(localStorage.getItem('lootdrop_steam_games') || '[]');
  const cleanTitle = (g.title || '').trim().toLowerCase();

  const matchedSteam = steamGames.find(sg => {
    const sName = (sg.name || '').trim().toLowerCase();
    return sName === cleanTitle || (cleanTitle.length > 5 && sName.includes(cleanTitle)) || (sName.length > 5 && cleanTitle.includes(sName));
  });

  const inSteam = !!matchedSteam;

  if (inVault || inSteam) {
    if (ownershipBanner) {
      ownershipBanner.style.display = 'flex';
    }
    if (ownershipIcon) {
      ownershipIcon.textContent = inSteam ? '🎮' : '💎';
    }
    if (ownershipTitle) {
      ownershipTitle.textContent = t('det_already_own');
    }
    if (ownershipSubtitle) {
      if (inVault && inSteam) {
        ownershipSubtitle.textContent = t('det_own_both');
      } else if (inVault) {
        ownershipSubtitle.textContent = t('det_own_vault');
      } else {
        ownershipSubtitle.textContent = t('det_own_steam', { name: matchedSteam.name });
      }
    }
  } else {
    if (ownershipBanner) {
      ownershipBanner.style.display = 'none';
    }
  }

  loadAndRenderReviewsForGame(g);
  modal.classList.add('open');
}

// 3x3 Backlog Challenge Grid Logic
function getChallengeStateForWeek(weekKey) {
  const allStates = JSON.parse(localStorage.getItem(STORAGE_KEYS.CHALLENGES) || '{}');
  return allStates[weekKey] || {
    manualOverrides: {},
    manualCount: 0
  };
}

function saveChallengeStateForWeek(weekKey, state) {
  const allStates = JSON.parse(localStorage.getItem(STORAGE_KEYS.CHALLENGES) || '{}');
  allStates[weekKey] = state;
  localStorage.setItem(STORAGE_KEYS.CHALLENGES, JSON.stringify(allStates));
}

function getWeekBlueprints(weekKey) {
  let hash = 0;
  for (let i = 0; i < weekKey.length; i++) {
    hash = (hash << 5) - hash + weekKey.charCodeAt(i);
    hash |= 0;
  }
  const rngSeed = Math.abs(hash);

  const shuffled = [...CHALLENGE_BLUEPRINTS];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = (rngSeed + i * 17) % (i + 1);
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled.slice(0, 9);
}

function openChallengeModal(weekKey = currentWeekKey) {
  activeChallengeWeekKey = weekKey;
  renderChallengeModal();
  document.getElementById('modal-challenge')?.classList.add('open');
}

function renderChallengeModal() {
  const weekKey = activeChallengeWeekKey;
  const relativeWeekNum = getRelativeWeekNumber(weekKey);
  const state = getChallengeStateForWeek(weekKey);

  const modalWeekTitle = document.getElementById('modal-week-title');
  const weekDisplayBadge = document.getElementById('week-display-badge');
  const prevBtn = document.getElementById('btn-prev-week');
  const nextBtn = document.getElementById('btn-next-week');

  if (modalWeekTitle) modalWeekTitle.textContent = t('ch_modal_sub', { n: relativeWeekNum });
  if (weekDisplayBadge) {
    weekDisplayBadge.textContent = weekKey === currentWeekKey
      ? t('ch_week_badge_active', { n: relativeWeekNum })
      : t('ch_week_num', { n: relativeWeekNum });
  }

  if (prevBtn) prevBtn.disabled = relativeWeekNum <= 1;
  const currentRel = getRelativeWeekNumber(currentWeekKey);
  if (nextBtn) nextBtn.disabled = relativeWeekNum >= (currentRel + MAX_FUTURE_WEEKS_PREVIEW);

  const manualUsed = Object.values(state.manualOverrides || {}).filter(Boolean).length;
  const manualRemaining = Math.max(0, MAX_MANUAL_OVERRIDES - manualUsed);
  const manualText = document.getElementById('manual-unlock-text');
  if (manualText) {
    manualText.innerHTML = t('ch_manual_rem', { rem: manualRemaining, max: MAX_MANUAL_OVERRIDES });
  }

  const evaluated = getEvaluatedTilesForWeek(weekKey);

  const gridContainer = document.getElementById('challenge-grid-container');
  if (gridContainer) {
    gridContainer.innerHTML = evaluated.map(tl => `
      <div class="challenge-tile ${tl.isCompleted ? 'completed' : ''} ${tl.isManual ? 'manual' : ''}" data-idx="${tl.index}">
        <span class="tile-emoji">${tl.emoji}</span>
        <h4 class="tile-title">${escapeHtml(tl.title)}</h4>
        <p class="tile-desc">${escapeHtml(tl.desc)}</p>
        <div class="tile-status-badge ${tl.isCompleted ? (tl.isManual ? 'manual-done' : 'done') : ''}">
          ${escapeHtml(tl.isCompleted ? (tl.isManual ? t('ch_tile_manual') : t('ch_tile_done')) : t('ch_tile_tap'))}
        </div>
      </div>
    `).join('');

    gridContainer.querySelectorAll('.challenge-tile').forEach(tileEl => {
      tileEl.addEventListener('click', () => {
        const idx = parseInt(tileEl.dataset.idx, 10);
        toggleManualUnlock(idx);
      });
    });
  }

  checkMilestoneWins(evaluated, weekKey);
  renderMiniChallengeWidget();
}

function toggleManualUnlock(tileIdx) {
  const weekKey = activeChallengeWeekKey;
  const state = getChallengeStateForWeek(weekKey);
  const currentOverrides = state.manualOverrides || {};
  const currentlyDone = !!currentOverrides[tileIdx];

  if (currentlyDone) {
    delete currentOverrides[tileIdx];
    showToast(t('toast_recycle_slot'), '♻️');
  } else {
    const used = Object.values(currentOverrides).filter(Boolean).length;
    if (used >= MAX_MANUAL_OVERRIDES) {
      showToast(t('toast_manual_limit', { max: MAX_MANUAL_OVERRIDES }), '⚠️');
      return;
    }
    currentOverrides[tileIdx] = true;
    playGamerChime();
    showToast(t('toast_manual_done'), '🔑');
  }

  state.manualOverrides = currentOverrides;
  saveChallengeStateForWeek(weekKey, state);
  renderChallengeModal();
}

function getEvaluatedTilesForWeek(weekKey) {
  const tiles = getWeekBlueprints(weekKey);
  const state = getChallengeStateForWeek(weekKey);

  return tiles.map((tile, idx) => {
    const isManual = !!(state.manualOverrides && state.manualOverrides[idx]);
    let isAutoDone = false;

    if (tile.id === 'BEAT_GAME' || tile.id === 'COMPLETION_JOURNEY') {
      isAutoDone = vaultItems.some(v => v.playStatus === 'beaten');
    } else if (tile.id === 'VAULT_STARTER' || tile.id === 'UNPLAYED_REVIVAL') {
      isAutoDone = vaultItems.length > 0;
    } else if (tile.id === 'TRI_PLAY') {
      isAutoDone = vaultItems.filter(v => v.playStatus === 'playing').length >= 2;
    }

    const loc = window.LootDropI18n?.getBlueprintText?.(tile.id);
    const isCompleted = isManual || isAutoDone;
    return {
      ...tile,
      title: loc ? loc.title : tile.id,
      desc: loc ? loc.desc : '',
      index: idx,
      isCompleted,
      isManual
    };
  });
}

const CHALLENGE_MILESTONE_PREFIXES = [
  'momentum_spark',
  'backlog_slayer',
  'row_raider',
  'column_conqueror',
  'diagonal_drifter',
  'bingo_architect',
  'overdrive_runner',
  'triple_threat',
  'vault_cleaner',
  'blackout_champion'
];

function checkMilestoneWins(evaluated, weekKey) {
  const completedRows = [0, 1, 2].filter(r =>
    evaluated[r * 3].isCompleted && evaluated[r * 3 + 1].isCompleted && evaluated[r * 3 + 2].isCompleted
  );

  const completedCols = [0, 1, 2].filter(c =>
    evaluated[c].isCompleted && evaluated[c + 3].isCompleted && evaluated[c + 6].isCompleted
  );

  const diag1 = evaluated[0].isCompleted && evaluated[4].isCompleted && evaluated[8].isCompleted;
  const diag2 = evaluated[2].isCompleted && evaluated[4].isCompleted && evaluated[6].isCompleted;
  const completedDiags = (diag1 ? 1 : 0) + (diag2 ? 1 : 0);
  const totalLines = completedRows.length + completedCols.length + completedDiags;
  const completedCount = evaluated.filter(tItem => tItem.isCompleted).length;
  const isBlackout = evaluated.every(tItem => tItem.isCompleted);

  const claimedMilestones = JSON.parse(localStorage.getItem(STORAGE_KEYS.MILESTONES) || '{}');
  const locM = (baseKey) => {
    const loc = window.LootDropI18n?.getMilestoneText?.(baseKey);
    return loc || { name: baseKey, sub: '', trophy: baseKey };
  };

  const milestones = [
    {
      id: `momentum_spark_${weekKey}`,
      ...locM('momentum_spark'),
      isEligible: completedCount >= 2
    },
    {
      id: `backlog_slayer_${weekKey}`,
      ...locM('backlog_slayer'),
      isEligible: totalLines > 0,
      rewardTheme: 'theme-backlog-slayer'
    },
    {
      id: `row_raider_${weekKey}`,
      ...locM('row_raider'),
      isEligible: completedRows.length > 0
    },
    {
      id: `column_conqueror_${weekKey}`,
      ...locM('column_conqueror'),
      isEligible: completedCols.length > 0
    },
    {
      id: `diagonal_drifter_${weekKey}`,
      ...locM('diagonal_drifter'),
      isEligible: completedDiags > 0,
      rewardTheme: 'theme-prism-drift'
    },
    {
      id: `bingo_architect_${weekKey}`,
      ...locM('bingo_architect'),
      isEligible: totalLines >= 2,
      rewardTheme: 'theme-solar-nova'
    },
    {
      id: `overdrive_runner_${weekKey}`,
      ...locM('overdrive_runner'),
      isEligible: completedCount >= 6,
      rewardTheme: 'theme-toxic-overdrive'
    },
    {
      id: `triple_threat_${weekKey}`,
      ...locM('triple_threat'),
      isEligible: totalLines >= 3,
      rewardTheme: 'theme-aurora-sovereign'
    },
    {
      id: `vault_cleaner_${weekKey}`,
      ...locM('vault_cleaner'),
      isEligible: isBlackout,
      rewardTheme: 'theme-vault-cleaner'
    },
    {
      id: `blackout_champion_${weekKey}`,
      ...locM('blackout_champion'),
      isEligible: isBlackout,
      rewardTheme: 'theme-nebula-blackout'
    }
  ];

  const claimedCount = milestones.filter(m => !!claimedMilestones[m.id]).length;
  const badgeEl = document.getElementById('milestones-count-badge');
  if (badgeEl) {
    badgeEl.textContent = t('ch_milestones_progress', { claimed: claimedCount });
  }

  const milestonesContainer = document.getElementById('milestones-list-container');
  if (milestonesContainer) {
    milestonesContainer.innerHTML = milestones.map(m => {
      const isClaimed = !!claimedMilestones[m.id];
      const themeObj = m.rewardTheme ? ACCENT_THEMES.find(th => th.id === m.rewardTheme) : null;
      const themeMeta = themeObj ? getLocalizedThemeMeta(themeObj.id) : null;
      const swatchHtml = themeObj ? `
        <span class="milestone-theme-swatch" title="${escapeHtml(themeMeta.title)}">
          <span class="milestone-swatch-dot" style="background:${themeObj.primary};"></span>
          <span class="milestone-swatch-dot" style="background:${themeObj.secondary};"></span>
        </span>` : '';

      const actionHtml = isClaimed && m.rewardTheme
        ? `<button class="btn btn-sm btn-primary" onclick="equipChallengeTheme('${m.rewardTheme}')">${escapeHtml(t('ch_equip_btn'))}</button>`
        : `<button class="btn btn-sm ${isClaimed ? 'btn-outline' : (m.isEligible ? 'btn-primary' : 'btn-secondary')}"
                  ${!m.isEligible || isClaimed ? 'disabled' : ''}
                  onclick="claimMilestone('${m.id}', '${m.rewardTheme || ''}', '${escapeHtml(m.trophy)}')">
            ${escapeHtml(isClaimed ? t('ch_claimed_btn') : (m.isEligible ? t('ch_claim_btn') : t('ch_locked_btn')))}
          </button>`;

      return `
        <div class="milestone-item ${m.isEligible || isClaimed ? 'unlocked' : ''}">
          <div class="milestone-info">
            <span class="milestone-name">${escapeHtml(m.name)} ${swatchHtml}</span>
            <span class="milestone-sub">${escapeHtml(m.sub)}</span>
          </div>
          <div class="milestone-actions">
            ${actionHtml}
          </div>
        </div>
      `;
    }).join('');
  }
}

window.equipChallengeTheme = function(themeId) {
  const themeObj = ACCENT_THEMES.find(th => th.id === themeId);
  if (!themeObj) return;
  const meta = getLocalizedThemeMeta(themeId);
  applyThemeAndBg(themeId, currentBgMode);
  renderThemeCatalog('all');
  playGamerChime();
  showToast(t('toast_theme_applied', { title: meta.title }), '🎨');
};

window.claimMilestone = function(id, rewardTheme, trophy) {
  const claimedMilestones = JSON.parse(localStorage.getItem(STORAGE_KEYS.MILESTONES) || '{}');
  claimedMilestones[id] = true;
  localStorage.setItem(STORAGE_KEYS.MILESTONES, JSON.stringify(claimedMilestones));

  triggerConfetti();
  playGamerChime();

  if (rewardTheme) {
    applyThemeAndBg(rewardTheme, currentBgMode);
    renderThemeCatalog('all');
    showToast(t('toast_claim_theme', { trophy }), '🎉');
  } else {
    showToast(t('toast_claim_trophy', { trophy }), '🏆');
  }

  renderChallengeModal();
  renderProfile();
};

function renderMiniChallengeWidget() {
  const widget = document.getElementById('home-challenge-widget');
  if (!widget) return;

  const evaluated = getEvaluatedTilesForWeek(currentWeekKey);
  const completedCount = evaluated.filter(tl => tl.isCompleted).length;
  const pct = Math.min(100, Math.round((completedCount / 9) * 100));

  const claimedMilestones = JSON.parse(localStorage.getItem(STORAGE_KEYS.MILESTONES) || '{}');
  const claimedCount = CHALLENGE_MILESTONE_PREFIXES
    .filter(prefix => !!claimedMilestones[`${prefix}_${currentWeekKey}`]).length;

  const progressFill = document.getElementById('home-challenge-progress-fill');
  const progressText = document.getElementById('home-challenge-progress-text');
  const weekLabel = document.getElementById('home-challenge-week-label');
  const milestonesText = document.getElementById('home-challenge-milestones-text');

  if (progressFill) progressFill.style.width = `${pct}%`;
  if (progressText) progressText.textContent = t('ch_tiles_progress', { done: completedCount });
  if (weekLabel) weekLabel.textContent = t('ch_week_active', { n: getRelativeWeekNumber(currentWeekKey) });
  if (milestonesText) milestonesText.textContent = t('ch_milestones_progress', { claimed: claimedCount });
}

// Backlog Roulette
function openRouletteModal() {
  document.getElementById('modal-roulette')?.classList.add('open');
}

function spinRoulette() {
  const candidates = vaultItems.length > 0 ? vaultItems : allGiveaways;
  if (!candidates || candidates.length === 0) {
    showToast(t('toast_roulette_empty'), '🎲');
    return;
  }

  const titleEl = document.getElementById('roulette-game-title');
  const platformEl = document.getElementById('roulette-game-platform');
  const spinBtn = document.getElementById('btn-spin-wheel');

  spinBtn.disabled = true;
  let counter = 0;
  const speed = 70;
  const totalSpins = 25;

  const interval = setInterval(() => {
    const randomPick = candidates[Math.floor(Math.random() * candidates.length)];
    titleEl.textContent = randomPick.title;
    platformEl.textContent = t('rou_worth_fmt', {
      plat: randomPick.platforms || 'PC',
      worth: randomPick.worth && randomPick.worth !== 'N/A' ? randomPick.worth : t('card_free')
    });
    counter++;

    if (counter >= totalSpins) {
      clearInterval(interval);
      spinBtn.disabled = false;
      const finalPick = candidates[Math.floor(Math.random() * candidates.length)];
      titleEl.textContent = finalPick.title;
      platformEl.textContent = t('rou_selected_fmt', { plat: finalPick.platforms || 'PC' });
      triggerConfetti();
      playGamerChime();
      showToast(t('toast_roulette_win', { title: finalPick.title }), '🎰');
    }
  }, speed);
}

// Profile & Trophies Showcase
function renderProfile() {
  const nameDisplay = document.getElementById('profile-name-display');
  const bioDisplay = document.getElementById('profile-bio-display');
  const titleBadge = document.getElementById('profile-title-badge');
  const nameInput = document.getElementById('edit-profile-name');
  const bioInput = document.getElementById('edit-profile-bio');
  const trophiesContainer = document.getElementById('profile-trophies-container');
  const trophyCountEl = document.getElementById('profile-trophy-count');

  const displayName = !userProfile.name || DEFAULT_PROFILE_NAMES.has(userProfile.name)
    ? t('default_profile_name')
    : userProfile.name;
  const displayBio = !userProfile.bio || DEFAULT_PROFILE_BIOS.has(userProfile.bio)
    ? t('default_profile_bio')
    : userProfile.bio;

  if (nameDisplay) nameDisplay.textContent = displayName;
  if (bioDisplay) bioDisplay.textContent = displayBio;
  if (titleBadge) titleBadge.textContent = t('default_profile_title');
  if (nameInput) nameInput.value = displayName;
  if (bioInput) bioInput.value = displayBio;

  const claimedMilestones = JSON.parse(localStorage.getItem(STORAGE_KEYS.MILESTONES) || '{}');
  const allTrophies = [
    { key: 'momentum_spark', icon: '⚡', rarity: 'GOLD' },
    { key: 'backlog_slayer', icon: '🏆', rarity: 'MYTHIC' },
    { key: 'row_raider', icon: '🥇', rarity: 'GOLD' },
    { key: 'column_conqueror', icon: '🥈', rarity: 'MYTHIC' },
    { key: 'diagonal_drifter', icon: '💎', rarity: 'MYTHIC' },
    { key: 'bingo_architect', icon: '☀️', rarity: 'MYTHIC' },
    { key: 'overdrive_runner', icon: '☢️', rarity: 'MYTHIC' },
    { key: 'triple_threat', icon: '🌌', rarity: 'MYTHIC' },
    { key: 'vault_cleaner', icon: '👑', rarity: 'MYTHIC' },
    { key: 'blackout_champion', icon: '🌟', rarity: 'MYTHIC' }
  ];

  let unlockedCount = 0;
  allTrophies.forEach(tr => {
    if (Object.keys(claimedMilestones).some(k => k.startsWith(tr.key))) unlockedCount++;
  });
  if (trophyCountEl) {
    trophyCountEl.textContent = t('prof_unlocked_count', { count: unlockedCount });
  }

  if (trophiesContainer) {
    trophiesContainer.innerHTML = allTrophies.map(tr => {
      const isUnlocked = Object.keys(claimedMilestones).some(k => k.startsWith(tr.key));
      const trophyTitle = window.LootDropI18n?.getTrophyText?.(tr.key) || tr.key;
      return `
        <div class="trophy-badge-card ${isUnlocked ? 'unlocked' : ''}">
          <span class="trophy-icon">${tr.icon}</span>
          <span class="trophy-title">${escapeHtml(trophyTitle)}</span>
          <span style="font-size: 9px; color: var(--color-primary); font-weight:800; margin-top:2px;">
            ${escapeHtml(isUnlocked ? t('prof_unlocked_badge') : t('prof_locked_badge'))}
          </span>
        </div>
      `;
    }).join('');
  }
}

// Toast Messages & Confetti
function showToast(message, icon = '✨') {
  const toast = document.getElementById('toast-popup');
  const iconEl = document.getElementById('toast-icon');
  const msgEl = document.getElementById('toast-message');
  if (!toast) return;

  iconEl.textContent = icon;
  msgEl.textContent = message;
  toast.classList.add('show');

  setTimeout(() => {
    toast.classList.remove('show');
  }, 3200);
}

function triggerConfetti() {
  const canvas = document.getElementById('confetti-canvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;

  const particles = [];
  const colors = ['#00F0FF', '#00FF88', '#A855F7', '#FBBF24', '#FF0055'];

  for (let i = 0; i < 90; i++) {
    particles.push({
      x: canvas.width / 2,
      y: canvas.height / 2,
      vx: (Math.random() - 0.5) * 14,
      vy: (Math.random() - 0.5) * 14 - 3,
      size: Math.random() * 8 + 4,
      color: colors[Math.floor(Math.random() * colors.length)],
      alpha: 1
    });
  }

  function frame() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    let alive = false;
    particles.forEach(p => {
      p.x += p.vx;
      p.y += p.vy;
      p.vy += 0.25;
      p.alpha -= 0.015;

      if (p.alpha > 0) {
        alive = true;
        ctx.globalAlpha = p.alpha;
        ctx.fillStyle = p.color;
        ctx.fillRect(p.x, p.y, p.size, p.size);
      }
    });

    if (alive) {
      requestAnimationFrame(frame);
    } else {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
    }
  }
  requestAnimationFrame(frame);
}

// Stats Calculation & Helpers
function updateStats() {
  let activeValue = 0;
  let expiringSoonCount = 0;
  const now = new Date();

  allGiveaways.forEach(g => {
    activeValue += parsePrice(g.worth);
    if (g.end_date && !g.end_date.startsWith('N/A')) {
      const end = new Date(g.end_date);
      const diffHours = (end - now) / (1000 * 60 * 60);
      if (diffHours > 0 && diffHours <= 168) {
        expiringSoonCount++;
      }
    }
  });

  let vaultValue = 0;
  vaultItems.forEach(v => {
    vaultValue += parsePrice(v.worth);
  });

  document.getElementById('stat-active-count').textContent = allGiveaways.length;
  document.getElementById('stat-total-value').textContent = `$${activeValue.toFixed(2)}`;
  document.getElementById('stat-vault-saved').textContent = `$${vaultValue.toFixed(2)}`;
  document.getElementById('stat-vault-count').textContent = vaultItems.length;
  document.getElementById('stat-expiring-soon').textContent = expiringSoonCount;
}

function parsePrice(str) {
  if (!str) return 0;
  const cleaned = str.replace(/[^0-9.]/g, '');
  return parseFloat(cleaned) || 0;
}

function formatExpiration(dateStr) {
  if (!dateStr || dateStr.startsWith('N/A')) return t('exp_ongoing');
  const end = new Date(dateStr);
  const now = new Date();
  const diffHours = Math.round((end - now) / (1000 * 60 * 60));

  if (diffHours <= 0) return t('exp_expired');
  if (diffHours < 24) return t('exp_hours', { n: diffHours });
  const days = Math.round(diffHours / 24);
  return t('exp_days', { n: days });
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/[&<>'"]/g, tag => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    "'": '&#39;',
    '"': '&quot;'
  }[tag] || tag));
}

// Alert Settings Sheet UI Synchronization
function initAlertSettingsUI() {
  const masterToggle = document.getElementById('master-alert-toggle');
  const soundToggle = document.getElementById('alert-filter-sound');
  const expiringToggle = document.getElementById('alert-filter-expiring');
  const gamesOnlyToggle = document.getElementById('alert-filter-games-only');
  const highValueToggle = document.getElementById('alert-filter-high-value');

  if (masterToggle) masterToggle.checked = alertSettings.master;
  if (soundToggle) soundToggle.checked = alertSettings.sound;
  if (expiringToggle) expiringToggle.checked = alertSettings.expiring;
  if (gamesOnlyToggle) gamesOnlyToggle.checked = alertSettings.gamesOnly;
  if (highValueToggle) highValueToggle.checked = alertSettings.highValueOnly;

  // Quick toggles in Settings
  const quickChime = document.getElementById('toggle-audio-chime');
  const quickDesktop = document.getElementById('toggle-desktop-alerts');
  const quickExpiring = document.getElementById('toggle-ending-soon-alerts');

  if (quickChime) quickChime.checked = alertSettings.sound;
  if (quickDesktop) quickDesktop.checked = alertSettings.desktop;
  if (quickExpiring) quickExpiring.checked = alertSettings.expiring;

  quickChime?.addEventListener('change', (e) => {
    alertSettings.sound = e.target.checked;
    localStorage.setItem(STORAGE_KEYS.ALERT_SETTINGS, JSON.stringify(alertSettings));
    if (alertSettings.sound) playGamerChime();
  });

  quickDesktop?.addEventListener('change', (e) => {
    alertSettings.desktop = e.target.checked;
    localStorage.setItem(STORAGE_KEYS.ALERT_SETTINGS, JSON.stringify(alertSettings));
    if (alertSettings.desktop && 'Notification' in window && Notification.permission === 'default') {
      requestNotificationPermission();
    }
  });

  quickExpiring?.addEventListener('change', (e) => {
    alertSettings.expiring = e.target.checked;
    localStorage.setItem(STORAGE_KEYS.ALERT_SETTINGS, JSON.stringify(alertSettings));
  });
}

// Setup Global UI Event Listeners
function setupEventListeners() {
  // Modal Closes
  document.querySelectorAll('.close-modal-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('open'));
    });
  });

  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) overlay.classList.remove('open');
    });
  });

  // Permission Banner
  document.getElementById('btn-enable-web-alerts')?.addEventListener('click', () => requestNotificationPermission());
  document.getElementById('btn-dismiss-perm')?.addEventListener('click', () => {
    document.getElementById('perm-banner').style.display = 'none';
  });

  // Top Action Buttons
  document.getElementById('btn-open-challenge')?.addEventListener('click', () => openChallengeModal());
  document.getElementById('hero-challenge-btn')?.addEventListener('click', () => openChallengeModal());
  document.getElementById('home-challenge-open-btn')?.addEventListener('click', () => openChallengeModal());
  document.getElementById('btn-open-roulette')?.addEventListener('click', () => openRouletteModal());
  document.getElementById('btn-spin-roulette')?.addEventListener('click', () => openRouletteModal());
  document.getElementById('btn-spin-wheel')?.addEventListener('click', () => spinRoulette());
  document.getElementById('btn-open-profile')?.addEventListener('click', () => {
    document.getElementById('modal-profile')?.classList.add('open');
  });
  document.getElementById('btn-open-notifications')?.addEventListener('click', () => {
    document.getElementById('modal-notifications')?.classList.add('open');
  });
  document.getElementById('btn-settings-notif')?.addEventListener('click', () => {
    document.getElementById('modal-notifications')?.classList.remove('open');
    document.getElementById('modal-alert-settings')?.classList.add('open');
  });
  document.getElementById('btn-open-alerts-sheet')?.addEventListener('click', () => {
    document.getElementById('modal-alert-settings')?.classList.add('open');
  });

  // Test Chime & Test Alert Triggers
  document.getElementById('btn-test-chime')?.addEventListener('click', () => {
    playGamerChime();
    showToast(t('toast_chime_test'), '🎵');
  });

  document.getElementById('btn-test-notification')?.addEventListener('click', () => {
    triggerWebsiteAlert(
      t('test_alert_drop_title'),
      t('test_alert_drop_body'),
      'https://store.steampowered.com/',
      '🎮'
    );
  });

  document.getElementById('btn-fire-test-alert')?.addEventListener('click', () => {
    triggerWebsiteAlert(
      t('test_alert_high_title'),
      t('test_alert_high_body'),
      'https://www.gog.com/',
      '⚡'
    );
  });

  // Save Alert Settings Sheet
  document.getElementById('btn-save-alert-settings')?.addEventListener('click', () => {
    alertSettings.master = document.getElementById('master-alert-toggle').checked;
    alertSettings.sound = document.getElementById('alert-filter-sound').checked;
    alertSettings.expiring = document.getElementById('alert-filter-expiring').checked;
    alertSettings.gamesOnly = document.getElementById('alert-filter-games-only').checked;
    alertSettings.highValueOnly = document.getElementById('alert-filter-high-value').checked;

    localStorage.setItem(STORAGE_KEYS.ALERT_SETTINGS, JSON.stringify(alertSettings));
    initAlertSettingsUI();
    document.getElementById('modal-alert-settings')?.classList.remove('open');
    playGamerChime();
    showToast(t('toast_alerts_saved'), '🔔');
  });

  // Notification Inbox Actions
  document.getElementById('btn-mark-all-read')?.addEventListener('click', () => {
    notificationsList.forEach(n => n.unread = false);
    localStorage.setItem(STORAGE_KEYS.NOTIFICATIONS, JSON.stringify(notificationsList));
    renderNotificationsList();
    showToast(t('toast_mark_read'), '✓');
  });

  document.getElementById('btn-clear-notifs')?.addEventListener('click', () => {
    notificationsList = [];
    localStorage.removeItem(STORAGE_KEYS.NOTIFICATIONS);
    renderNotificationsList();
    showToast(t('toast_clear_notifs'), '🗑️');
  });

  // Background Mode Buttons
  document.querySelectorAll('.bg-mode-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const mode = btn.dataset.bg;
      applyThemeAndBg(currentThemeId, mode);
      showToast(t('toast_bg_switched', { mode: btn.textContent.trim() }), '🌓');
    });
  });

  // Theme Category Tabs
  document.querySelectorAll('.theme-cat-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('.theme-cat-tab').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      renderThemeCatalog(tab.dataset.cat);
    });
  });

  // Platform Filter Chips
  document.querySelectorAll('#platform-chips .filter-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('#platform-chips .filter-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      activePlatformFilter = chip.dataset.platform;
      renderRadar();
    });
  });

  // Type & Sort Dropdowns
  document.getElementById('filter-type')?.addEventListener('change', (e) => {
    activeTypeFilter = e.target.value;
    renderRadar();
  });

  document.getElementById('sort-by')?.addEventListener('change', (e) => {
    activeSort = e.target.value;
    renderRadar();
  });

  // Search Input
  const searchInput = document.getElementById('radar-search-input');
  const clearBtn = document.getElementById('clear-search-btn');

  searchInput?.addEventListener('input', (e) => {
    activeSearchQuery = e.target.value;
    clearBtn.style.display = activeSearchQuery ? 'block' : 'none';
    renderRadar();
  });

  clearBtn?.addEventListener('click', () => {
    searchInput.value = '';
    activeSearchQuery = '';
    clearBtn.style.display = 'none';
    renderRadar();
  });

  // View Mode Toggle
  document.getElementById('btn-view-grid')?.addEventListener('click', () => {
    document.getElementById('radar-drops-container')?.classList.remove('list-view');
    document.getElementById('btn-view-grid')?.classList.add('active');
    document.getElementById('btn-view-list')?.classList.remove('active');
  });

  document.getElementById('btn-view-list')?.addEventListener('click', () => {
    document.getElementById('radar-drops-container')?.classList.add('list-view');
    document.getElementById('btn-view-list')?.classList.add('active');
    document.getElementById('btn-view-grid')?.classList.remove('active');
  });

  // Vault Status Chips
  document.querySelectorAll('#vault-status-chips .filter-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('#vault-status-chips .filter-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      renderVault();
    });
  });

  // Week Navigation inside 3x3 Modal
  document.getElementById('btn-prev-week')?.addEventListener('click', () => {
    const parts = activeChallengeWeekKey.split('-W');
    let y = parseInt(parts[0], 10);
    let w = parseInt(parts[1], 10) - 1;
    if (w < 1) { y -= 1; w = 52; }
    openChallengeModal(getWeekKey(y, w));
  });

  document.getElementById('btn-next-week')?.addEventListener('click', () => {
    const parts = activeChallengeWeekKey.split('-W');
    let y = parseInt(parts[0], 10);
    let w = parseInt(parts[1], 10) + 1;
    if (w > 52) { y += 1; w = 1; }
    openChallengeModal(getWeekKey(y, w));
  });

  // Detail Modal Vault Button
  document.getElementById('detail-vault-toggle')?.addEventListener('click', () => {
    if (selectedGiveaway) {
      toggleVaultItem(selectedGiveaway.id);
      const isSaved = vaultItems.some(v => v.id === selectedGiveaway.id);
      document.getElementById('detail-vault-toggle').textContent = isSaved ? t('det_remove_vault') : t('det_add_vault');
    }
  });

  // Detail Modal Share Button
  document.getElementById('detail-share-btn')?.addEventListener('click', () => {
    if (!selectedGiveaway) return;
    if (navigator.share) {
      navigator.share({
        title: t('share_deal_title', { title: selectedGiveaway.title }),
        text: t('share_deal_text', {
          title: selectedGiveaway.title,
          plat: selectedGiveaway.platforms || 'PC'
        }),
        url: selectedGiveaway.open_giveaway_url || window.location.href
      }).catch(() => {});
    } else {
      navigator.clipboard.writeText(selectedGiveaway.open_giveaway_url || window.location.href);
      showToast(t('toast_link_copied'), '🔗');
    }
  });

  // Profile Save
  document.getElementById('btn-save-profile')?.addEventListener('click', () => {
    const name = document.getElementById('edit-profile-name').value.trim() || t('default_profile_name');
    const bio = document.getElementById('edit-profile-bio').value.trim() || t('default_profile_bio');
    userProfile.name = name;
    userProfile.bio = bio;
    localStorage.setItem(STORAGE_KEYS.PROFILE, JSON.stringify(userProfile));
    renderProfile();
    showToast(t('toast_profile_saved'), '👤');
  });

  // Steam 64 ID Sync
  document.getElementById('btn-save-steam-id')?.addEventListener('click', () => {
    const input = document.getElementById('steam-id-input').value.trim();
    if (!input) {
      showToast(t('toast_steam_enter'), '⚠️');
      return;
    }
    syncWebSteamLibrary(input);
  });

  document.getElementById('btn-sync-steam')?.addEventListener('click', () => {
    const savedId = localStorage.getItem(STORAGE_KEYS.STEAM_ID);
    if (!savedId) {
      switchTab('pane-settings');
      showToast(t('toast_steam_need'), 'ℹ️');
      return;
    }
    syncWebSteamLibrary(savedId);
  });

  // Backup Export
  document.getElementById('btn-export-backup')?.addEventListener('click', () => {
    const backupData = {
      version: '4.3.0',
      timestamp: Date.now(),
      vault: vaultItems,
      profile: userProfile,
      challenges: JSON.parse(localStorage.getItem(STORAGE_KEYS.CHALLENGES) || '{}'),
      milestones: JSON.parse(localStorage.getItem(STORAGE_KEYS.MILESTONES) || '{}')
    };

    const blob = new Blob([JSON.stringify(backupData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `lootdrop_backup_${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
    showToast(t('toast_backup_exported'), '💾');
  });

  // Backup Import
  document.getElementById('backup-file-input')?.addEventListener('change', (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const data = JSON.parse(event.target.result);
        if (data.vault) {
          vaultItems = data.vault;
          localStorage.setItem(STORAGE_KEYS.VAULT, JSON.stringify(vaultItems));
        }
        if (data.profile) {
          userProfile = data.profile;
          localStorage.setItem(STORAGE_KEYS.PROFILE, JSON.stringify(userProfile));
        }
        if (data.challenges) {
          localStorage.setItem(STORAGE_KEYS.CHALLENGES, JSON.stringify(data.challenges));
        }
        if (data.milestones) {
          localStorage.setItem(STORAGE_KEYS.MILESTONES, JSON.stringify(data.milestones));
        }
        showToast(t('toast_backup_restored'), '📂');
        renderProfile();
        renderVault();
        updateStats();
      } catch (err) {
        showToast(t('toast_backup_failed'), '❌');
      }
    };
    reader.readAsText(file);
  });

  // Clear Vault
  document.getElementById('btn-clear-vault')?.addEventListener('click', () => {
    if (confirm(t('confirm_clear_vault'))) {
      vaultItems = [];
      localStorage.removeItem(STORAGE_KEYS.VAULT);
      renderVault();
      updateStats();
      showToast(t('toast_vault_cleared'), '🗑️');
    }
  });
}

// Cloudflare R2 Public Reviews System
const R2_REVIEWS_URL = 'https://lootdrop-storage-proxy.hardingbentley67.workers.dev?accountId=public_reviews&filename=lootdrop_reviews.json';
const R2_AUTH_KEY = 'sb_publishable_htD9aXXk0QHjhG_ifS3nYg_kjUp7osD';
const SUPABASE_PROJECT_URL = 'https://imcwutouksxeevmofcmn.supabase.co';
const SUPABASE_KEEPALIVE_INTERVAL_MS = 12 * 60 * 60 * 1000;
let publicReviewsList = [];
let currentReviewRating = 5;

async function pingSupabaseKeepAlive(force = false) {
  try {
    const now = Date.now();
    const lastTs = parseInt(localStorage.getItem(STORAGE_KEYS.SUPA_KEEPALIVE_TS) || '0', 10);
    if (!force && lastTs > 0 && (now - lastTs) < SUPABASE_KEEPALIVE_INTERVAL_MS) {
      return;
    }
    const headers = {
      'apikey': R2_AUTH_KEY,
      'Accept': 'application/json',
      'Prefer': 'count=exact'
    };
    if (!R2_AUTH_KEY.startsWith('sb_')) {
      headers['Authorization'] = `Bearer ${R2_AUTH_KEY}`;
    }
    const endpoints = [
      `${SUPABASE_PROJECT_URL}/rest/v1/lootdrop_backups?select=account_id&limit=1`,
      `${SUPABASE_PROJECT_URL}/rest/v1/keep_alive?select=id&limit=1`,
      `${SUPABASE_PROJECT_URL}/rest/v1/`
    ];
    for (const url of endpoints) {
      try {
        const resp = await fetch(url, { method: 'GET', headers });
        if (resp.status >= 200 && resp.status < 300) {
          localStorage.setItem(STORAGE_KEYS.SUPA_KEEPALIVE_TS, String(now));
          return;
        }
        if (resp.status !== 404 && resp.status < 500) {
          localStorage.setItem(STORAGE_KEYS.SUPA_KEEPALIVE_TS, String(now));
        }
      } catch (_) {}
    }
  } catch (_) {}
}

const REVIEW_BLACKLIST_TERMS = [
  'fuck(?:er|ers|ed|ing|ings|in|wit|wits|head|heads|face|faces|tard|tards|boy|boys|s)?',
  '(?:mother|motha|mutha)fuck(?:er|ers|ed|ing|a|as|s)?',
  'clusterfuck(?:ed|s)?',
  'fvck(?:er|ers|ed|ing|s)?',
  'fuk(?:c|k)?(?:er|ers|ed|ing|s)?',
  'phuck(?:er|ers|ed|ing|s)?',
  'phuk(?:er|ers|ed|ing|s)?',
  'f[*@#_]ck(?:er|ers|ed|ing|s)?',
  'f[*_]{2}k(?:er|ers|ed|ing|s)?',
  'shit(?:s|ty|tier|tiest|ted|ting|head|heads|face|faces|hole|holes|load|loads|show|shows|storm|storms|post|posts)?',
  'sh[*!_#]t(?:s|ty|ted|ting|head|heads)?',
  'bullshit(?:ter|ters|ted|ting|s)?',
  'horseshit',
  'dipshit(?:s)?',
  'batshit',
  'apeshit',
  'bitch(?:es|ed|ing|y|ass)?',
  'b[*!_#]tch(?:es|ed|ing|y)?',
  'son\\s+of\\s+a\\s+bitch',
  'bastard(?:s|ized)?',
  'cunt(?:s|y)?',
  'c[*_#]nt(?:s|y)?',
  'ass',
  'ass?hole(?:s)?',
  'arsehole(?:s)?',
  'jackass(?:es)?',
  'dumbass(?:es)?',
  'fatass(?:es)?',
  'smartass(?:es)?',
  'asswipe(?:s)?',
  'asshat(?:s)?',
  'assclown(?:s)?',
  'a[*_]{2}hole(?:s)?',
  'cock(?:s|sucker|suckers|sucking|head|heads|block|blocker|blockers)?',
  'c[*_#]ck(?:s|sucker|suckers|head|heads)?',
  'dick(?:s|head|heads|wad|wads|face|faces|weed|less)?',
  'd[*!_#]ck(?:s|head|heads|wad|wads|face|faces)?',
  'pussy',
  'pussies',
  'p[*_#]ss(?:y|ies)',
  'whore(?:s|d|ing|house|houses)?',
  'wh[*_#]re(?:s|d|ing)?',
  'slut(?:s|ty|tier|tiest)?',
  'sl[*_#]t(?:s|ty)?',
  'twat(?:s|waffle|waffles)?',
  'wank(?:er|ers|ed|ing|s)?',
  'prick(?:s)?',
  'douche(?:bag|bags|canoe|canoes|y|s)?',
  'piss(?:ed|ing|es)?',
  'nigg+(?:a|as|ah|ahs|er|ers|uh|uhs|let|lets)',
  'n[*!_#]gg(?:a|as|er|ers)',
  'nigr(?:a|o|os)',
  'negro(?:es|s)?',
  'sandnigg+(?:a|er|ers|as)',
  'fag+(?:ot|ots|otry|y|s)?',
  'f[*@_#]g(?:got|gots|s)?',
  'dyke(?:s)?',
  'trann(?:y|ies)',
  'shemale(?:s)?',
  'kike(?:s)?',
  'kyke(?:s)?',
  'spic(?:s|k|ks)?',
  'wetback(?:s)?',
  'beaner(?:s)?',
  'chink(?:s)?',
  'gook(?:s)?',
  'coon(?:s)?',
  'jigaboo(?:s)?',
  'sambo(?:s)?',
  'raghead(?:s)?',
  'towelhead(?:s)?',
  'paki(?:s)?',
  'retard(?:s|ed|ation)?',
  'r[*e]tard(?:s|ed)?',
  'libtard(?:s)?',
  'fucktard(?:s)?',
  'spastic(?:s)?',
  'mongoloid(?:s)?',
  'zipperhead(?:s)?',
  'porchmonkey(?:s)?',
  'junglebunny',
  'junglebunnies',
  'cameljockey(?:s)?',
  'redskin(?:s)?',
  'squaw(?:s)?',
  'injun(?:s)?',
  'wop(?:s)?',
  'dago(?:s|es)?',
  'polack(?:s)?',
  'kraut(?:s)?',
  'heeb(?:s)?',
  'cash[\\s._\\-*]+is[\\s._\\-*]+[ck]lay',
  'cashis[ck]lay',
  'nuh+',
  'cuh+(?:z|zz)?',
  'nut(?:s|t|ted|ting|z)?',
  'nut[\\s._\\-*]*sack(?:s)?',
  'deez[\\s._\\-*]*nut(?:s|z)?',
  'deeznut(?:s|z)?',
  'c[u*]m+(?:ming|med|mer|mers|shot|shots|slut|sluts|dumpster|dumpsters|stain|stains|rag|rags|s)?',
  'jizz+(?:ed|ing|er|s)?',
  'splooge(?:d|ing|s)?',
  'skeet(?:ed|ing|s)?',
  'semen',
  'sperm',
  'ejaculat(?:e|ed|ing|ion|ions)',
  'masturbat(?:e|ed|ing|ion)',
  'fap+(?:ed|ing|s)?',
  'orgasm(?:s)?',
  'boner(?:s)?',
  'horny',
  'porn(?:o|ography)?',
  'hentai',
  'dildo(?:s)?',
  'penis(?:es)?',
  'vagina(?:s)?',
  'clit(?:s|oris)?',
  'boob(?:s|ies|y)?',
  'tit(?:s|ty|ties)',
  'handjob(?:s)?',
  'blowjob(?:s)?',
  'rimjob(?:s)?',
  'footjob(?:s)?',
  'anal',
  'anus',
  'scrotum',
  'testicle(?:s)?',
  'ballsack(?:s)?',
  'buss(?:y|ies)',
  'gyat+',
  'goon(?:er|ers|ing)',
  'skibidi',
  'thot(?:s)?',
  'skank(?:s|y)?',
  'milf(?:s)?',
  'dilf(?:s)?',
  'stfu',
  'gtfo',
  'fuq',
  'fck',
  'shyt+',
  'shite',
  'bich(?:es)?',
  'biatch(?:es)?',
  'bytch(?:es)?',
  'azz+',
  'ashole(?:s)?',
  'cnt',
  'dik',
  'kok',
  'pusy'
];

const REVIEW_DIRECT_WORD_REGEX = new RegExp(`\\b(?:${REVIEW_BLACKLIST_TERMS.join('|')})\\b`, 'gi');
const REVIEW_SEPARATED_LETTERS_REGEX = /(?<![\p{L}\p{N}])([\p{L}\p{N}@$!*])(?:[\s._\-*]+([\p{L}\p{N}@$!*])){2,}(?![\p{L}\p{N}])/gu;
const REVIEW_INLINE_SEP_TOKEN_REGEX = /(?<!\S)[\p{L}\p{N}@$!*]+(?:[._\-*]+[\p{L}\p{N}@$!*]+)+(?!\S)/gu;
const REVIEW_PHONE_REGEX = /(?:(?:\+?1[-.\s]*)?(?:\(\d{3}\)|\d{3})[-.\s]*\d{3}[-.\s]*\d{4}|\+\d{1,3}(?:[-.\s()]*\d){7,13}|\b(?:call|text|phone|tel|dial|sms|ring|whatsapp|facetime)\b[^\n\d]{0,20}\d{3}[-.\s]\d{4}|\b\d{3}[-.]\d{4})\b(?!\s*(?:fps|hz|hours?|hrs?|players?|gamers?|mb|gb|tb|xp|gold|coins?|points?|pts)\b)/gi;
const REVIEW_STREET_ADDRESS_REGEX = /\b\d{1,6}[a-z]?\s+(?:(?:north|south|east|west|n\.?|s\.?|e\.?|w\.?|ne|nw|se|sw)\s+)?(?!(?:hours?|hrs?|mins?|minutes?|secs?|seconds?|days?|weeks?|months?|years?|fps|hz|gb|tb|mb|kb|pb|players?|gamers?|fans?|users?|times?|ways?|stars?|bucks|dollars?|coins?|points?|levels?|miles?|percent)\b)(?:[a-z0-9'.\-]+\s+){1,3}(?:street|st|avenue|ave|boulevard|blvd|road|rd|drive|dr|lane|ln|court|ct|terrace|ter|trail|trl|parkway|pkwy|highway|hwy|circle|cir|crescent|cres|plaza|plz|alley)\b\.?(?:\s+(?:n|s|e|w|ne|nw|se|sw)\b)?(?:[,\s]+(?:apt|apartment|suite|ste|unit|#)\s*[a-z0-9-]+)?/gi;
const REVIEW_SAFE_ADDRESS_FALSE_POSITIVE_REGEX = /\b(?:hard\s+drive|solid\s+state\s+drive|ssd\s+drive|hdd\s+drive|nvme\s+drive|usb\s+drive|flash\s+drive|external\s+drive|internal\s+drive|disk\s+drive|disc\s+drive|optical\s+drive|thumb\s+drive|test\s+drive|warp\s+drive|hyper\s+drive|on\s+the\s+road|down\s+the\s+road)\b/i;
const REVIEW_PO_BOX_REGEX = /\bP\.?\s*O\.?\s*Box\s+#?[a-z0-9-]{1,10}\b/gi;
const REVIEW_CITY_STATE_ZIP_REGEX = /\b[a-z][a-z\s.'-]{1,25},\s*(?:AL|AK|AZ|AR|CA|CO|CT|DE|FL|GA|HI|ID|IL|IN|IA|KS|KY|LA|ME|MD|MA|MI|MN|MS|MO|MT|NE|NV|NH|NJ|NM|NY|NC|ND|OH|OK|OR|PA|RI|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI|WY|DC)\s+\d{5}(?:-\d{4})?\b/gi;
const REVIEW_EMAIL_REGEX = /\b[a-z0-9._%+\-]+@[a-z0-9.\-]+\.[a-z]{2,}\b/gi;

function normalizeReviewLeet(text) {
  return text
    .replace(/0/g, 'o')
    .replace(/[1!]/g, 'i')
    .replace(/3/g, 'e')
    .replace(/[4@]/g, 'a')
    .replace(/[5$]/g, 's')
    .replace(/[7+]/g, 't')
    .replace(/8/g, 'b')
    .replace(/#/g, 'h');
}

function buildReviewModerationVariants(text) {
  const cleaned = String(text || '')
    .replace(/[\u200B-\u200D\uFEFF]/g, '')
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '');
  const base = cleaned.toLowerCase();
  const camelBase = cleaned.replace(/([\p{Ll}\p{N}@$!*#])([\p{Lu}])/gu, '$1 $2').toLowerCase();
  const leet = normalizeReviewLeet(base);
  const camelLeet = normalizeReviewLeet(camelBase);
  const inlineBase = base.replace(REVIEW_INLINE_SEP_TOKEN_REGEX, m => m.replace(/[._\-*]+/g, ''));
  const inlineLeet = normalizeReviewLeet(inlineBase);
  const spacedBase = base.replace(REVIEW_SEPARATED_LETTERS_REGEX, m => m.replace(/[\s._\-*]+/g, ''));
  const spacedLeet = normalizeReviewLeet(spacedBase);

  return Array.from(new Set([
    base,
    camelBase,
    leet,
    camelLeet,
    base.replace(/(.)\1{2,}/g, '$1'),
    leet.replace(/(.)\1{2,}/g, '$1'),
    camelLeet.replace(/(.)\1{2,}/g, '$1'),
    base.replace(/(.)\1{2,}/g, '$1$1'),
    leet.replace(/(.)\1{2,}/g, '$1$1'),
    base.replace(/(.)\1+/g, '$1'),
    leet.replace(/(.)\1+/g, '$1'),
    camelLeet.replace(/(.)\1+/g, '$1'),
    inlineBase,
    inlineLeet,
    inlineLeet.replace(/(.)\1+/g, '$1'),
    spacedBase,
    spacedLeet,
    spacedLeet.replace(/(.)\1+/g, '$1')
  ]));
}

function containsReviewPhoneNumber(text) {
  if (!text || !String(text).trim()) return false;
  const cleaned = String(text).replace(/[\u200B-\u200D\uFEFF]/g, '');
  REVIEW_PHONE_REGEX.lastIndex = 0;
  return REVIEW_PHONE_REGEX.test(cleaned);
}

function containsReviewAddress(text) {
  if (!text || !String(text).trim()) return false;
  const cleaned = String(text).replace(/[\u200B-\u200D\uFEFF]/g, '');
  REVIEW_PO_BOX_REGEX.lastIndex = 0;
  REVIEW_CITY_STATE_ZIP_REGEX.lastIndex = 0;
  REVIEW_EMAIL_REGEX.lastIndex = 0;
  if (REVIEW_PO_BOX_REGEX.test(cleaned) || REVIEW_CITY_STATE_ZIP_REGEX.test(cleaned) || REVIEW_EMAIL_REGEX.test(cleaned)) {
    return true;
  }
  REVIEW_STREET_ADDRESS_REGEX.lastIndex = 0;
  const matches = cleaned.match(REVIEW_STREET_ADDRESS_REGEX);
  if (!matches) return false;
  return matches.some(m => !REVIEW_SAFE_ADDRESS_FALSE_POSITIVE_REGEX.test(m));
}

function containsProfanityOrSlurWords(text) {
  if (!text || !String(text).trim()) return false;
  return buildReviewModerationVariants(text).some(variant => {
    REVIEW_DIRECT_WORD_REGEX.lastIndex = 0;
    return REVIEW_DIRECT_WORD_REGEX.test(variant);
  });
}

function containsBlacklistedReviewWords(text) {
  if (!text || !String(text).trim()) return false;
  return containsProfanityOrSlurWords(text) || containsReviewPhoneNumber(text) || containsReviewAddress(text);
}

function validateReviewModeration(title, comment, authorName) {
  const flagged = [];
  const titleHasPii = containsReviewAddress(title) || containsReviewPhoneNumber(title);
  const commentHasPii = containsReviewAddress(comment) || containsReviewPhoneNumber(comment);
  const authorHasPii = containsReviewAddress(authorName) || containsReviewPhoneNumber(authorName);

  if (titleHasPii || containsProfanityOrSlurWords(title)) flagged.push(t('mod_field_headline'));
  if (commentHasPii || containsProfanityOrSlurWords(comment)) flagged.push(t('mod_field_comment'));
  if (authorHasPii || containsProfanityOrSlurWords(authorName)) flagged.push(t('mod_field_tag'));

  if (flagged.length > 0) {
    const joined = flagged.length === 1
      ? flagged[0]
      : flagged.length === 2
        ? t('mod_join_two', { a: flagged[0], b: flagged[1] })
        : t('mod_join_three', { a: flagged[0], b: flagged[1], c: flagged[2] });
    const hasPii = titleHasPii || commentHasPii || authorHasPii;
    const errorMessage = t(hasPii ? 'mod_err_pii' : 'mod_err_words', { fields: joined });
    return {
      isAllowed: false,
      errorMessage
    };
  }
  return { isAllowed: true, errorMessage: null };
}

function sanitizeReviewObject(review) {
  if (!review || typeof review !== 'object') return review;
  return {
    ...review,
    authorName: sanitizeReviewText(review.authorName || ''),
    title: sanitizeReviewText(review.title || ''),
    comment: sanitizeReviewText(review.comment || '')
  };
}

function maskBlacklistedToken(token) {
  const trimmed = token.trim();
  if (trimmed.length <= 2) return '*'.repeat(Math.max(2, trimmed.length));
  return trimmed.charAt(0) + '*'.repeat(trimmed.length - 1);
}

function sanitizeReviewText(text) {
  if (!containsBlacklistedReviewWords(text)) return String(text || '');
  let result = String(text || '')
    .replace(REVIEW_PO_BOX_REGEX, '[redacted]')
    .replace(REVIEW_CITY_STATE_ZIP_REGEX, '[redacted]')
    .replace(REVIEW_EMAIL_REGEX, '[redacted]')
    .replace(REVIEW_STREET_ADDRESS_REGEX, m =>
      REVIEW_SAFE_ADDRESS_FALSE_POSITIVE_REGEX.test(m) ? m : '[redacted]'
    )
    .replace(REVIEW_PHONE_REGEX, '[redacted]');

  if (containsProfanityOrSlurWords(result)) {
    result = result.replace(REVIEW_DIRECT_WORD_REGEX, m => maskBlacklistedToken(m));
  }
  if (containsProfanityOrSlurWords(result)) {
    result = result.replace(REVIEW_INLINE_SEP_TOKEN_REGEX, m =>
      containsProfanityOrSlurWords(m) ? maskBlacklistedToken(m) : m
    );
  }
  if (containsProfanityOrSlurWords(result)) {
    result = result.replace(REVIEW_SEPARATED_LETTERS_REGEX, m =>
      containsProfanityOrSlurWords(m) ? '****' : m
    );
  }
  if (containsProfanityOrSlurWords(result)) {
    result = result.split(/(\s+)/).map(tok =>
      tok.trim() && containsProfanityOrSlurWords(tok) ? maskBlacklistedToken(tok) : tok
    ).join('');
  }
  return result;
}

const INITIAL_SEED_REVIEWS = [
  {
    id: "rev_seed_1",
    giveawayId: 0,
    gameTitle: "Death Stranding",
    authorName: "SamPorter99",
    authorTag: "Verified Claimer",
    rating: 5,
    title: "Atmospheric masterpiece! Grab it while it's free",
    comment: "One of the most unique experiences in modern gaming. The soundtrack is incredible and the traversal mechanics are surprisingly engaging. Runs great on Steam Deck!",
    platform: "Epic Games",
    timestamp: Date.now() - 86400000 * 2,
    helpfulCount: 28,
    isVerifiedClaimer: true
  },
  {
    id: "rev_seed_2",
    giveawayId: 0,
    gameTitle: "Fallout: New Vegas",
    authorName: "CourierSix",
    authorTag: "RPG Veteran",
    rating: 5,
    title: "The pinnacle of modern Fallout",
    comment: "Best writing, best factions, and unmatched quest variety. If you've never played New Vegas, this free giveaway is an absolute must-claim.",
    platform: "GOG",
    timestamp: Date.now() - 86400000 * 5,
    helpfulCount: 45,
    isVerifiedClaimer: true
  },
  {
    id: "rev_seed_3",
    giveawayId: 0,
    gameTitle: "Tomb Raider Definitive Edition",
    authorName: "LaraExplorer",
    authorTag: "Verified Claimer",
    rating: 4,
    title: "Action packed reboot with thrilling set-pieces",
    comment: "Terrific reboot of the classic franchise. Fluid combat, fun traversal puzzles, and great visuals. Definitely worth claiming and playing through over a weekend.",
    platform: "Steam",
    timestamp: Date.now() - 86400000 * 7,
    helpfulCount: 19,
    isVerifiedClaimer: true
  },
  {
    id: "rev_seed_4",
    giveawayId: 0,
    gameTitle: "Control Standard Edition",
    authorName: "RemedyFanatic",
    authorTag: "Remedy Fan",
    rating: 5,
    title: "Mind-bending lore and telekinetic powers",
    comment: "The atmosphere of the Oldest House is unmatched. Throwing forklifts at Hiss enemies with telekinesis never gets old. Claim it without hesitation!",
    platform: "Epic Games",
    timestamp: Date.now() - 86400000 * 9,
    helpfulCount: 33,
    isVerifiedClaimer: true
  },
  {
    id: "rev_app_seed_1",
    giveawayId: -1,
    gameTitle: "LootDrop App",
    isAppReview: true,
    authorName: "CyberHunterX",
    authorTag: "Vault Master",
    rating: 5,
    title: "Best free games tracker hands down!",
    comment: "Never miss an Epic, Steam, or GOG freebie again. The 3x3 challenge grid and Steam library sync make clearing my backlog actually fun. 10/10!",
    platform: "LootDrop V4.3.0",
    timestamp: Date.now() - 86400000 * 3,
    helpfulCount: 52,
    isVerifiedClaimer: true
  },
  {
    id: "rev_app_seed_2",
    giveawayId: -1,
    gameTitle: "LootDrop App",
    isAppReview: true,
    authorName: "PixelKnight",
    authorTag: "Verified Claimer",
    rating: 5,
    title: "Essential for PC and Console gamers",
    comment: "Saved me over $300 in games this year alone. Fast notifications, clean dark cyberpunk UI, zero ads, and instant R2 cloud sync. Absolute perfection!",
    platform: "LootDrop V4.3.0",
    timestamp: Date.now() - 86400000 * 6,
    helpfulCount: 41,
    isVerifiedClaimer: true
  }
];

async function fetchPublicReviewsFromR2() {
  const cached = localStorage.getItem('lootdrop_public_reviews');
  if (cached) {
    try {
      publicReviewsList = JSON.parse(cached).map(sanitizeReviewObject);
    } catch (_) {}
  }

  try {
    const resp = await fetch(R2_REVIEWS_URL, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${R2_AUTH_KEY}`,
        'User-Agent': 'LootDrop-Web/4.3.0'
      }
    });
    if (resp.ok) {
      const data = await resp.json();
      if (Array.isArray(data) && data.length > 0) {
        const seedIds = new Set(data.map(d => d.id));
        const merged = [...data.map(sanitizeReviewObject)];
        INITIAL_SEED_REVIEWS.forEach(s => {
          if (!seedIds.has(s.id)) merged.push(s);
        });
        publicReviewsList = merged;
        localStorage.setItem('lootdrop_public_reviews', JSON.stringify(merged));
      }
    }
  } catch (err) {
    console.warn('Could not fetch R2 reviews, using cache or seed reviews', err);
  }

  if (publicReviewsList.length === 0) {
    publicReviewsList = [...INITIAL_SEED_REVIEWS];
  }

  updateSettingsAppRatingUI();
}

function getAppReviews() {
  return publicReviewsList.filter(r => r.isAppReview || r.giveawayId === -1 || r.gameTitle === 'LootDrop App');
}

function updateViewAppReviewsButton() {
  const viewAppReviewsBtn = document.getElementById('btn-view-app-reviews');
  const appPreviewList = document.getElementById('settings-app-reviews-preview');
  if (!viewAppReviewsBtn) return;
  const isOpen = appPreviewList && appPreviewList.style.display !== 'none';
  if (isOpen) {
    viewAppReviewsBtn.textContent = t('btn_hide_reviews');
  } else {
    const count = getAppReviews().length;
    viewAppReviewsBtn.innerHTML = `<span data-i18n="btn_view_reviews">${escapeHtml(t('btn_view_reviews'))}</span> (<span id="settings-app-reviews-count">${count}</span>)`;
  }
}

function updateSettingsAppRatingUI() {
  const appReviews = getAppReviews();
  const scoreEl = document.getElementById('settings-app-score');
  const starsEl = document.getElementById('settings-app-stars');
  const countEl = document.getElementById('settings-app-reviews-count');
  const countLabelEl = document.getElementById('settings-app-count-label');
  const previewEl = document.getElementById('settings-app-reviews-preview');

  if (!scoreEl) return;

  const count = appReviews.length;
  if (countEl) countEl.textContent = count;

  if (count === 0) {
    scoreEl.textContent = '5.0';
    if (starsEl) starsEl.textContent = '★★★★★';
    if (countLabelEl) countLabelEl.textContent = t('rev_first_app');
  } else {
    const avg = appReviews.reduce((sum, r) => sum + (r.rating || 5), 0) / count;
    const rounded = Math.round(avg);
    scoreEl.textContent = avg.toFixed(1);
    if (starsEl) starsEl.textContent = '★'.repeat(rounded) + '☆'.repeat(5 - rounded);
    if (countLabelEl) {
      countLabelEl.textContent = t('rev_based_app', {
        count,
        noun: count === 1 ? t('rev_noun_one') : t('rev_noun_many')
      });
    }
  }

  updateViewAppReviewsButton();

  if (previewEl && previewEl.style.display !== 'none') {
    renderAppReviewsPreview();
  }
}

function renderAppReviewsPreview() {
  const previewEl = document.getElementById('settings-app-reviews-preview');
  if (!previewEl) return;
  const appReviews = getAppReviews();
  const votedIds = JSON.parse(localStorage.getItem('lootdrop_voted_reviews') || '[]');

  if (appReviews.length === 0) {
    previewEl.innerHTML = `<p class="input-hint">${escapeHtml(t('rev_none_app'))}</p>`;
    return;
  }

  previewEl.innerHTML = appReviews.map(r => {
    const cleanAuthor = sanitizeReviewText(r.authorName || 'Gamer');
    const cleanTitle = sanitizeReviewText(r.title || '');
    const cleanComment = sanitizeReviewText(r.comment || '');
    const starStr = '★'.repeat(r.rating || 5) + '☆'.repeat(5 - (r.rating || 5));
    const initial = (cleanAuthor || 'G').trim().charAt(0).toUpperCase();
    const isVoted = votedIds.includes(r.id);
    const dateLocale = window.LootDropI18n?.getMeta?.()?.dateLocale || 'en-US';
    const dateFormatted = new Date(r.timestamp || Date.now()).toLocaleDateString(dateLocale, { month: 'short', day: 'numeric' });

    return `
      <div class="review-item-card" data-review-id="${r.id}" style="margin-bottom: 10px;">
        <div class="review-card-header">
          <div class="review-author-box">
            <div class="review-author-avatar">${initial}</div>
            <div class="review-author-meta">
              <span class="review-author-name">
                ${escapeHtml(cleanAuthor)}
                <span class="review-verified-badge">${escapeHtml(t('rev_verified_user'))}</span>
              </span>
              <span class="review-time-platform">${escapeHtml(r.platform || 'LootDrop')} • ${dateFormatted}</span>
            </div>
          </div>
          <div class="review-stars-badge">${starStr}</div>
        </div>
        ${cleanTitle ? `<div class="review-card-title">${escapeHtml(cleanTitle)}</div>` : ''}
        ${cleanComment ? `<div class="review-card-body">${escapeHtml(cleanComment)}</div>` : ''}
        <div class="review-card-footer">
          <button class="review-helpful-btn ${isVoted ? 'voted' : ''}" data-review-id="${r.id}">
            ${escapeHtml(t('rev_helpful', { count: r.helpfulCount || 0 }))}
          </button>
          <span style="font-size: 10px; color: var(--color-text-dim);">${escapeHtml(t('rev_app_tag'))}</span>
        </div>
      </div>
    `;
  }).join('');

  previewEl.querySelectorAll('.review-helpful-btn').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      e.stopPropagation();
      const revId = btn.getAttribute('data-review-id');
      await voteReviewHelpful(revId, null);
      renderAppReviewsPreview();
    });
  });
}

function getReviewsForGame(game) {
  if (!game) return [];
  const cleanTitle = (game.title || '').trim().toLowerCase();
  return publicReviewsList.filter(r => {
    if (game.id && r.giveawayId === game.id) return true;
    const rTitle = (r.gameTitle || '').trim().toLowerCase();
    if (cleanTitle && rTitle === cleanTitle) return true;
    if (cleanTitle.length >= 4 && (rTitle.includes(cleanTitle) || cleanTitle.includes(rTitle))) return true;
    return false;
  });
}

function loadAndRenderReviewsForGame(game) {
  const reviews = getReviewsForGame(game);
  const listEl = document.getElementById('detail-reviews-list');
  const avgScoreEl = document.getElementById('detail-reviews-avg-score');
  const starsEl = document.getElementById('detail-reviews-stars');
  const countLabelEl = document.getElementById('detail-reviews-count-label');

  if (!listEl) return;

  if (reviews.length === 0) {
    avgScoreEl.textContent = '5.0';
    starsEl.textContent = '★★★★★';
    countLabelEl.textContent = t('rev_first_game');
    listEl.innerHTML = `
      <div class="review-empty-state">
        <p>🎮 ${t('rev_empty_game', { title: escapeHtml(game.title || t('rev_this_game')) })}</p>
        <p style="font-size: 11px; margin-top: 4px;">${t('rev_empty_hint')}</p>
      </div>
    `;
    return;
  }

  const avg = reviews.reduce((sum, r) => sum + (r.rating || 5), 0) / reviews.length;
  const rounded = Math.round(avg);
  avgScoreEl.textContent = avg.toFixed(1);
  starsEl.textContent = '★'.repeat(rounded) + '☆'.repeat(5 - rounded);
  countLabelEl.textContent = t('rev_based_on', {
    count: reviews.length,
    noun: reviews.length === 1 ? t('rev_noun_one') : t('rev_noun_many')
  });

  const votedIds = JSON.parse(localStorage.getItem('lootdrop_voted_reviews') || '[]');
  const dateLocale = window.LootDropI18n?.getMeta?.()?.dateLocale || 'en-US';

  listEl.innerHTML = reviews.map(r => {
    const cleanAuthor = sanitizeReviewText(r.authorName || 'Gamer');
    const cleanTitle = sanitizeReviewText(r.title || '');
    const cleanComment = sanitizeReviewText(r.comment || '');
    const starStr = '★'.repeat(r.rating || 5) + '☆'.repeat(5 - (r.rating || 5));
    const initial = (cleanAuthor || 'G').trim().charAt(0).toUpperCase();
    const isVoted = votedIds.includes(r.id);
    const dateFormatted = new Date(r.timestamp || Date.now()).toLocaleDateString(dateLocale, { month: 'short', day: 'numeric' });

    return `
      <div class="review-item-card" data-review-id="${r.id}">
        <div class="review-card-header">
          <div class="review-author-box">
            <div class="review-author-avatar">${initial}</div>
            <div class="review-author-meta">
              <span class="review-author-name">
                ${escapeHtml(cleanAuthor)}
                ${r.isVerifiedClaimer ? `<span class="review-verified-badge">${escapeHtml(t('rev_verified'))}</span>` : ''}
              </span>
              <span class="review-time-platform">${escapeHtml(r.platform || 'PC')} • ${dateFormatted}</span>
            </div>
          </div>
          <div class="review-stars-badge">${starStr}</div>
        </div>
        ${cleanTitle ? `<div class="review-card-title">${escapeHtml(cleanTitle)}</div>` : ''}
        ${cleanComment ? `<div class="review-card-body">${escapeHtml(cleanComment)}</div>` : ''}
        <div class="review-card-footer">
          <button class="review-helpful-btn ${isVoted ? 'voted' : ''}" data-review-id="${r.id}">
            ${escapeHtml(t('rev_helpful', { count: r.helpfulCount || 0 }))}
          </button>
          ${r.authorTag ? `<span style="font-size: 10px; color: var(--color-text-dim);">🏷️ ${escapeHtml(r.authorTag)}</span>` : ''}
        </div>
      </div>
    `;
  }).join('');

  // Attach helpful vote listeners
  listEl.querySelectorAll('.review-helpful-btn').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      e.stopPropagation();
      const revId = btn.getAttribute('data-review-id');
      await voteReviewHelpful(revId, game);
    });
  });
}

async function voteReviewHelpful(reviewId, game) {
  const votedIds = JSON.parse(localStorage.getItem('lootdrop_voted_reviews') || '[]');
  if (votedIds.includes(reviewId)) {
    showToast(t('toast_already_voted'), 'ℹ️');
    return;
  }

  const review = publicReviewsList.find(r => r.id === reviewId);
  if (!review) return;

  review.helpfulCount = (review.helpfulCount || 0) + 1;
  votedIds.push(reviewId);
  localStorage.setItem('lootdrop_voted_reviews', JSON.stringify(votedIds));
  localStorage.setItem('lootdrop_public_reviews', JSON.stringify(publicReviewsList));

  if (game) {
    loadAndRenderReviewsForGame(game);
  }
  updateSettingsAppRatingUI();
  showToast(t('toast_voted_helpful'), '👍');

  // Push updated list to R2
  try {
    await fetch(R2_REVIEWS_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${R2_AUTH_KEY}`,
        'X-Account-Id': 'public_reviews',
        'User-Agent': 'LootDrop-Web/4.3.0'
      },
      body: JSON.stringify(publicReviewsList)
    });
  } catch (_) {}
}

async function postPublicReviewToR2(newReview) {
  publicReviewsList.unshift(newReview);
  localStorage.setItem('lootdrop_public_reviews', JSON.stringify(publicReviewsList));

  try {
    const resp = await fetch(R2_REVIEWS_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${R2_AUTH_KEY}`,
        'X-Account-Id': 'public_reviews',
        'User-Agent': 'LootDrop-Web/4.3.0'
      },
      body: JSON.stringify(publicReviewsList.slice(0, 300))
    });
    return resp.ok;
  } catch (err) {
    console.warn('Failed to sync review to Cloudflare R2', err);
    return false;
  }
}

let syncReviewModalLanguage = () => {};

function initReviewsModal() {
  const modal = document.getElementById('modal-write-review');
  const openBtn = document.getElementById('btn-open-write-review');
  const closeBtn = document.getElementById('close-write-review-btn');
  const submitBtn = document.getElementById('btn-submit-review');
  const starsContainer = document.getElementById('write-review-star-selector');
  const ratingLabel = document.getElementById('write-review-rating-label');
  const targetGameBtn = document.getElementById('btn-target-game');
  const targetAppBtn = document.getElementById('btn-target-app');
  const rateAppSettingBtn = document.getElementById('btn-rate-lootdrop-app');
  const viewAppReviewsBtn = document.getElementById('btn-view-app-reviews');
  const appPreviewList = document.getElementById('settings-app-reviews-preview');

  let isWritingAppReview = false;

  function setReviewTarget(isApp) {
    isWritingAppReview = isApp;
    const titleSub = document.getElementById('write-review-game-title');
    if (isApp) {
      targetAppBtn?.classList.add('active');
      targetGameBtn?.classList.remove('active');
      if (titleSub) titleSub.textContent = '⭐ LootDrop App (V4.3.0)';
    } else {
      targetGameBtn?.classList.add('active');
      targetAppBtn?.classList.remove('active');
      if (titleSub) titleSub.textContent = selectedGiveaway?.title || t('rev_giveaway_fallback');
    }
  }

  targetGameBtn?.addEventListener('click', () => setReviewTarget(false));
  targetAppBtn?.addEventListener('click', () => setReviewTarget(true));

  const moderationErrEl = document.getElementById('review-moderation-error');
  const clearModerationError = () => {
    if (moderationErrEl) {
      moderationErrEl.style.display = 'none';
      moderationErrEl.textContent = '';
    }
  };
  ['review-input-title', 'review-input-comment', 'review-input-author'].forEach(id => {
    document.getElementById(id)?.addEventListener('input', clearModerationError);
  });

  // Open from game detail dialog
  openBtn?.addEventListener('click', () => {
    if (!selectedGiveaway) return;
    setReviewTarget(false);
    clearModerationError();
    document.getElementById('review-input-author').value = userProfile.name || t('default_profile_name');
    document.getElementById('review-input-title').value = '';
    document.getElementById('review-input-comment').value = '';
    currentReviewRating = 5;
    updateStarUI();
    modal?.classList.add('open');
  });

  // Open directly from Settings
  rateAppSettingBtn?.addEventListener('click', () => {
    setReviewTarget(true);
    clearModerationError();
    document.getElementById('review-input-author').value = userProfile.name || t('default_profile_name');
    document.getElementById('review-input-title').value = '';
    document.getElementById('review-input-comment').value = '';
    currentReviewRating = 5;
    updateStarUI();
    modal?.classList.add('open');
  });

  // Toggle app reviews list in Settings
  viewAppReviewsBtn?.addEventListener('click', () => {
    if (!appPreviewList) return;
    const isHidden = appPreviewList.style.display === 'none';
    appPreviewList.style.display = isHidden ? 'block' : 'none';
    updateViewAppReviewsButton();
    if (isHidden) {
      renderAppReviewsPreview();
    }
  });

  closeBtn?.addEventListener('click', () => {
    clearModerationError();
    modal?.classList.remove('open');
  });

  function updateStarUI() {
    starsContainer?.querySelectorAll('.star-btn').forEach(btn => {
      const starVal = parseInt(btn.getAttribute('data-star'), 10);
      if (starVal <= currentReviewRating) {
        btn.classList.add('active');
      } else {
        btn.classList.remove('active');
      }
    });
    if (ratingLabel) {
      const key = `wr_rating_${currentReviewRating}`;
      const translated = t(key);
      ratingLabel.textContent = translated !== key ? translated : t('wr_stars_suffix', { n: currentReviewRating });
    }
  }

  syncReviewModalLanguage = () => {
    updateStarUI();
    setReviewTarget(isWritingAppReview);
    if (submitBtn && !submitBtn.disabled) {
      submitBtn.textContent = t('wr_submit');
    }
  };

  starsContainer?.querySelectorAll('.star-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      currentReviewRating = parseInt(btn.getAttribute('data-star'), 10) || 5;
      updateStarUI();
    });
  });

  submitBtn?.addEventListener('click', async () => {
    const title = document.getElementById('review-input-title').value.trim();
    const comment = document.getElementById('review-input-comment').value.trim();
    const author = document.getElementById('review-input-author').value.trim() || userProfile.name || 'Anonymous Gamer';

    if (!title && !comment) {
      showToast(t('toast_enter_review'), '⚠️');
      return;
    }

    const moderation = validateReviewModeration(title, comment, author);
    if (!moderation.isAllowed) {
      if (moderationErrEl) {
        moderationErrEl.textContent = `⚠️ ${moderation.errorMessage}`;
        moderationErrEl.style.display = 'block';
      }
      showToast(t('toast_moderation_blocked'), '🚫');
      return;
    }
    clearModerationError();

    submitBtn.disabled = true;
    submitBtn.textContent = t('wr_submitting');

    if (isWritingAppReview) {
      const newReview = {
        id: `rev_app_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`,
        giveawayId: -1,
        gameTitle: 'LootDrop App',
        isAppReview: true,
        authorName: author,
        authorTag: 'Verified Claimer',
        rating: currentReviewRating,
        title: title,
        comment: comment,
        platform: 'Web Edition V4.3.0',
        timestamp: Date.now(),
        helpfulCount: 0,
        isVerifiedClaimer: true
      };

      await postPublicReviewToR2(newReview);

      submitBtn.disabled = false;
      submitBtn.textContent = t('wr_submit');
      modal?.classList.remove('open');

      showToast(t('toast_review_app_ok'), '⭐');
      updateSettingsAppRatingUI();
    } else {
      if (!selectedGiveaway) {
        submitBtn.disabled = false;
        submitBtn.textContent = t('wr_submit');
        return;
      }
      const isClaimed = vaultItems.some(v => v.id === selectedGiveaway.id);

      const newReview = {
        id: `rev_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`,
        giveawayId: selectedGiveaway.id || 0,
        gameTitle: selectedGiveaway.title || '',
        authorName: author,
        authorTag: isClaimed ? 'Verified Claimer' : 'Community Member',
        rating: currentReviewRating,
        title: title,
        comment: comment,
        platform: 'Web Edition',
        timestamp: Date.now(),
        helpfulCount: 0,
        isVerifiedClaimer: isClaimed
      };

      await postPublicReviewToR2(newReview);

      submitBtn.disabled = false;
      submitBtn.textContent = t('wr_submit');
      modal?.classList.remove('open');

      showToast(t('toast_review_game_ok'), '⭐');
      loadAndRenderReviewsForGame(selectedGiveaway);
      updateSettingsAppRatingUI();
    }
  });

  updateStarUI();
  updateSettingsAppRatingUI();
}

// Steam Library Sync & DLC Recommender Engine (Web)
const DLC_CACHE_TTL_MS = 12 * 60 * 60 * 1000;
const COSMETIC_OR_OST_REGEX = /\b(soundtrack|ost|original score|art\s*book|digital art|wallpaper|music pack|digital extras|comic book|ringtones)\b/i;
const DLC_GIVEAWAY_REGEX = /\b(dlc|expansion|season pass|starter pack|booster pack|add-on|addon|bundle|chapter|pass|cosmetic|skin|pack)\b/i;

let steamGamesList = [];
let storeDlcRecommendations = [];
let dismissedDlcIds = new Set();
let isScanningDlcs = false;
let dlcScanStatus = '';
let vaultViewMode = 'vault';
let activeDlcHubFilter = 'all';
let selectedDlcParentAppId = 'all';
let selectedGameDlcsList = [];
let isLoadingGameDlcs = false;

function refreshSteamStatusHint() {
  const savedSteamId = localStorage.getItem(STORAGE_KEYS.STEAM_ID) || '';
  const steamHint = document.getElementById('steam-status-hint');
  if (!steamHint) return;
  if (steamGamesList.length > 0) {
    steamHint.textContent = t('steam_status_synced', {
      count: steamGamesList.length,
      id: savedSteamId ? ` (${savedSteamId})` : ''
    });
  } else if (savedSteamId) {
    steamHint.textContent = t('steam_status_linked', { id: savedSteamId });
  } else {
    steamHint.textContent = t('steam_status_not_linked');
  }
}

function initSteamAndDlcFeatures() {
  try {
    steamGamesList = JSON.parse(localStorage.getItem(STORAGE_KEYS.STEAM_GAMES) || '[]');
  } catch (_) {
    steamGamesList = [];
  }
  try {
    const cache = JSON.parse(localStorage.getItem(STORAGE_KEYS.DLC_CACHE) || '{}');
    storeDlcRecommendations = Array.isArray(cache.items) ? cache.items : [];
  } catch (_) {
    storeDlcRecommendations = [];
  }
  try {
    const dismissed = JSON.parse(localStorage.getItem(STORAGE_KEYS.DLC_DISMISSED) || '[]');
    dismissedDlcIds = new Set(Array.isArray(dismissed) ? dismissed.map(Number) : []);
  } catch (_) {
    dismissedDlcIds = new Set();
  }

  const savedSteamId = localStorage.getItem(STORAGE_KEYS.STEAM_ID) || '';
  const steamInput = document.getElementById('steam-id-input');
  if (steamInput && savedSteamId) steamInput.value = savedSteamId;
  refreshSteamStatusHint();

  // Vault Mode Tabs (Vault vs Steam Library)
  document.querySelectorAll('#vault-mode-tabs .vault-mode-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('#vault-mode-tabs .vault-mode-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      vaultViewMode = btn.dataset.mode || 'vault';
      const statusChips = document.getElementById('vault-status-chips');
      if (statusChips) statusChips.style.display = vaultViewMode === 'vault' ? 'flex' : 'none';
      renderVault();
    });
  });

  // Home & Vault DLC Buttons
  document.getElementById('btn-home-dlc-scan')?.addEventListener('click', () => {
    refreshWebSteamDlcRecommendations(true);
  });
  document.getElementById('btn-home-dlc-see-all')?.addEventListener('click', () => {
    openDlcHubModal('all');
  });
  document.getElementById('btn-open-dlc-hub')?.addEventListener('click', () => {
    openDlcHubModal('all');
  });
  document.getElementById('btn-dlc-hub-refresh')?.addEventListener('click', () => {
    if (selectedDlcParentAppId !== 'all') {
      fetchDlcsForSingleGameWeb(parseInt(selectedDlcParentAppId, 10));
    } else {
      refreshWebSteamDlcRecommendations(true);
    }
  });
  document.getElementById('close-dlc-hub-btn')?.addEventListener('click', () => {
    document.getElementById('modal-dlc-hub')?.classList.remove('open');
  });
  document.getElementById('modal-dlc-hub')?.addEventListener('click', (e) => {
    if (e.target && e.target.id === 'modal-dlc-hub') {
      document.getElementById('modal-dlc-hub')?.classList.remove('open');
    }
  });

  // DLC Hub filter chips
  document.querySelectorAll('#dlc-hub-filter-chips .filter-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('#dlc-hub-filter-chips .filter-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      activeDlcHubFilter = chip.dataset.dlcFilter || 'all';
      renderDlcHubList();
    });
  });

  // DLC Hub per-game selector
  document.getElementById('dlc-hub-game-select')?.addEventListener('change', (e) => {
    const val = e.target.value;
    selectedDlcParentAppId = val;
    if (val === 'all') {
      selectedGameDlcsList = [];
      updateDlcHubHeader();
      renderDlcHubList();
    } else {
      fetchDlcsForSingleGameWeb(parseInt(val, 10));
    }
  });

  updateDlcBadges();
  renderHomeDlcSection();

  if (steamGamesList.length > 0 && !isDlcCacheFresh()) {
    refreshWebSteamDlcRecommendations(false);
  }
}

const STEAM_WEB_API_KEY = '22F46E31080D84AE934E0D1A142E6209';

function unwrapCorsProxyPayload(rawText, mode) {
  let text = String(rawText || '').trim();
  if (mode === 'allorigins_get') {
    const wrapper = JSON.parse(text);
    if (!wrapper || typeof wrapper.contents !== 'string') {
      throw new Error('Empty proxy wrapper response');
    }
    text = wrapper.contents.trim();
  } else if (mode === 'jina') {
    const marker = 'Markdown Content:';
    const markerIdx = text.indexOf(marker);
    if (markerIdx >= 0) {
      text = text.slice(markerIdx + marker.length).trim();
    }
    if (text.startsWith('```')) {
      text = text.replace(/^```(?:json|xml)?\s*/i, '').replace(/```\s*$/, '').trim();
    }
  }
  return text;
}

function extractJsonFromProxyText(rawText) {
  const trimmed = String(rawText || '').trim();
  try {
    return JSON.parse(trimmed);
  } catch (_) {
    const start = trimmed.search(/[\{\[]/);
    if (start >= 0) {
      return JSON.parse(trimmed.slice(start));
    }
    throw new Error('Response was not valid JSON');
  }
}

async function fetchWithTimeout(url, timeoutMs = 9500) {
  const ctrl = typeof AbortController !== 'undefined' ? new AbortController() : null;
  const timer = ctrl ? setTimeout(() => ctrl.abort(), timeoutMs) : null;
  try {
    return await fetch(url, ctrl ? { signal: ctrl.signal } : undefined);
  } finally {
    if (timer) clearTimeout(timer);
  }
}

function buildCorsCandidates(targetUrl, skipDirect = true) {
  const candidates = [];
  if (!skipDirect) {
    candidates.push({ url: targetUrl, mode: 'raw' });
  }
  candidates.push(
    { url: `https://r.jina.ai/${targetUrl}`, mode: 'jina' },
    { url: `https://api.allorigins.win/get?url=${encodeURIComponent(targetUrl)}`, mode: 'allorigins_get' },
    { url: `https://api.allorigins.win/raw?url=${encodeURIComponent(targetUrl)}`, mode: 'raw' },
    { url: `https://api.codetabs.com/v1/proxy?quest=${encodeURIComponent(targetUrl)}`, mode: 'raw' },
    { url: `https://api.cors.lol/?url=${encodeURIComponent(targetUrl)}`, mode: 'raw' }
  );
  return candidates;
}

async function fetchTextWithCorsFallback(targetUrl, { skipDirect = true, timeoutMs = 9500 } = {}) {
  const candidates = buildCorsCandidates(targetUrl, skipDirect);
  let lastErr = null;
  for (const candidate of candidates) {
    try {
      const resp = await fetchWithTimeout(candidate.url, timeoutMs);
      if (!resp || !resp.ok) {
        lastErr = new Error(`HTTP ${resp ? resp.status : 'error'}`);
        continue;
      }
      const raw = await resp.text();
      const text = unwrapCorsProxyPayload(raw, candidate.mode);
      if (text) return text;
      lastErr = new Error('Empty proxy response');
    } catch (err) {
      lastErr = err;
    }
  }
  throw lastErr || new Error('Request blocked or network unavailable');
}

async function fetchJsonWithCorsFallback(targetUrl, { skipDirect = true, timeoutMs = 9500 } = {}) {
  const candidates = buildCorsCandidates(targetUrl, skipDirect);
  let lastErr = null;
  for (const candidate of candidates) {
    try {
      const resp = await fetchWithTimeout(candidate.url, timeoutMs);
      if (!resp || !resp.ok) {
        lastErr = new Error(`HTTP ${resp ? resp.status : 'error'}`);
        continue;
      }
      const raw = await resp.text();
      const text = unwrapCorsProxyPayload(raw, candidate.mode);
      return extractJsonFromProxyText(text);
    } catch (err) {
      lastErr = err;
    }
  }
  throw lastErr || new Error('Request blocked or network unavailable');
}

async function fetchWithCorsFallback(targetUrl) {
  const text = await fetchTextWithCorsFallback(targetUrl, { skipDirect: true });
  return new Response(text, { status: 200, headers: { 'Content-Type': 'text/plain; charset=utf-8' } });
}

function parseSteamIdentifier(rawInput) {
  let clean = (rawInput || '').trim().split('?')[0].split('#')[0];
  const idMatch = clean.match(/\b(7656119\d{10}|\d{17})\b/);
  if (idMatch) return { type: 'steam64', value: idMatch[1] };

  if (clean.includes('/id/')) {
    clean = clean.split('/id/')[1].split('/')[0].trim();
  } else if (clean.includes('/profiles/')) {
    clean = clean.split('/profiles/')[1].split('/')[0].trim();
  } else {
    clean = clean.replace(/^https?:\/\/(www\.)?steamcommunity\.com\//i, '').replace(/^\/+|\/+$/g, '');
  }
  if (/^\d{17}$/.test(clean)) return { type: 'steam64', value: clean };
  return { type: 'vanity', value: clean };
}

async function resolveWebSteam64Id(parsed) {
  if (parsed.type === 'steam64') return parsed.value;

  const vanity = encodeURIComponent(parsed.value);
  const apiUrl = `https://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=${STEAM_WEB_API_KEY}&vanityurl=${vanity}&format=json`;
  try {
    const json = await fetchJsonWithCorsFallback(apiUrl, { skipDirect: true });
    if (json?.response?.success === 1 && json.response.steamid) {
      return String(json.response.steamid);
    }
  } catch (_) {}

  for (const xmlUrl of [
    `https://steamcommunity.com/id/${vanity}/?xml=1`,
    `https://steamcommunity.com/profiles/${vanity}/?xml=1`
  ]) {
    try {
      const xmlText = await fetchTextWithCorsFallback(xmlUrl, { skipDirect: true });
      const match = xmlText.match(/<steamID64>(\d{17})<\/steamID64>/);
      if (match && match[1]) return match[1];
    } catch (_) {}
  }

  throw new Error(t('steam_err_private'));
}

function parseLegacySteamXmlGames(xmlText) {
  const doc = new DOMParser().parseFromString(xmlText, 'text/xml');
  const errorNode = doc.querySelector('response > error');
  if (errorNode) {
    throw new Error(errorNode.textContent || t('steam_err_private'));
  }
  const gameNodes = Array.from(doc.querySelectorAll('games > game'));
  return gameNodes.map(node => {
    const appId = parseInt(node.querySelector('appID')?.textContent || '0', 10);
    const name = (node.querySelector('name')?.textContent || '').trim();
    const hoursOnRecord = parseFloat((node.querySelector('hoursOnRecord')?.textContent || '0').replace(/,/g, '')) || 0;
    const hoursLast2Weeks = parseFloat((node.querySelector('hoursLast2Weeks')?.textContent || '0').replace(/,/g, '')) || 0;
    const logo = (node.querySelector('logo')?.textContent || '').trim();
    if (!appId || !name) return null;
    return {
      appId,
      name,
      playtimeMinutes: Math.round(hoursOnRecord * 60),
      playtime2WeeksMinutes: Math.round(hoursLast2Weeks * 60),
      headerImageUrl: logo || `https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/${appId}/header.jpg`,
      storeUrl: `https://store.steampowered.com/app/${appId}`
    };
  }).filter(Boolean);
}

async function syncWebSteamLibrary(rawInput) {
  const parsed = parseSteamIdentifier(rawInput);
  if (!parsed.value) {
    showToast(t('toast_steam_invalid'), '⚠️');
    return;
  }

  localStorage.setItem(STORAGE_KEYS.STEAM_ID, rawInput.trim());
  const hint = document.getElementById('steam-status-hint');
  if (hint) {
    hint.textContent = t('steam_syncing_for', { id: parsed.value });
  }
  showToast(t('toast_steam_connecting'), '🔄');

  try {
    const steam64 = await resolveWebSteam64Id(parsed);
    const ownedApiUrl = `https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=${STEAM_WEB_API_KEY}&steamid=${encodeURIComponent(steam64)}&include_appinfo=1&include_played_free_games=1&format=json`;

    let parsedGames = null;
    let apiErr = null;
    try {
      const json = await fetchJsonWithCorsFallback(ownedApiUrl, { skipDirect: true, timeoutMs: 12000 });
      const resp = json?.response;
      if (!resp || (!Object.prototype.hasOwnProperty.call(resp, 'games') && !Object.prototype.hasOwnProperty.call(resp, 'game_count'))) {
        throw new Error(t('steam_err_no_games'));
      }
      const rawGames = Array.isArray(resp.games) ? resp.games : [];
      parsedGames = rawGames.map(g => {
        const appId = parseInt(g.appid || '0', 10);
        const name = String(g.name || '').trim();
        if (!appId || !name) return null;
        return {
          appId,
          name,
          playtimeMinutes: Number(g.playtime_forever) || 0,
          playtime2WeeksMinutes: Number(g.playtime_2weeks) || 0,
          headerImageUrl: `https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/${appId}/header.jpg`,
          storeUrl: `https://store.steampowered.com/app/${appId}`
        };
      }).filter(Boolean);
    } catch (err) {
      apiErr = err;
    }

    if (parsedGames === null) {
      // Fallback to legacy XML only if API request itself failed (not for explicit privacy errors)
      if (apiErr && apiErr.message === t('steam_err_no_games')) {
        throw apiErr;
      }
      const xmlUrl = `https://steamcommunity.com/profiles/${encodeURIComponent(steam64)}/games?tab=all&xml=1`;
      try {
        const xmlText = await fetchTextWithCorsFallback(xmlUrl, { skipDirect: true });
        parsedGames = parseLegacySteamXmlGames(xmlText);
      } catch (_) {
        throw apiErr || new Error(t('steam_err_private'));
      }
    }

    if (!parsedGames || parsedGames.length === 0) {
      throw new Error(t('steam_err_no_games'));
    }

    steamGamesList = parsedGames;
    localStorage.setItem(STORAGE_KEYS.STEAM_GAMES, JSON.stringify(steamGamesList));

    if (hint) {
      hint.textContent = t('toast_steam_synced', { count: steamGamesList.length });
    }
    showToast(t('toast_steam_synced', { count: steamGamesList.length }), '🎮');

    updateDlcBadges();
    renderVault();
    renderHomeDlcSection();
    await refreshWebSteamDlcRecommendations(true);

    if (hint) {
      hint.textContent = t('steam_status_ready', {
        count: steamGamesList.length,
        dlc: getCombinedDlcRecommendations().length
      });
    }
  } catch (err) {
    console.warn('Steam sync error:', err);
    if (hint) hint.textContent = t('toast_steam_failed', { err: err.message });
    showToast(t('toast_steam_failed', { err: err.message }), '⚠️');
  }
}

function isDlcCacheFresh() {
  try {
    const cache = JSON.parse(localStorage.getItem(STORAGE_KEYS.DLC_CACHE) || '{}');
    const langNow = window.LootDropI18n?.getLanguage() || 'en';
    return cache.timestamp &&
      (!cache.lang || cache.lang === langNow) &&
      (Date.now() - cache.timestamp) < DLC_CACHE_TTL_MS &&
      Array.isArray(cache.items) &&
      cache.items.length > 0;
  } catch (_) {
    return false;
  }
}

function saveDlcCache(items) {
  const langNow = window.LootDropI18n?.getLanguage() || 'en';
  localStorage.setItem(STORAGE_KEYS.DLC_CACHE, JSON.stringify({
    timestamp: Date.now(),
    lang: langNow,
    items
  }));
}

function normalizeGameMatchTitle(title) {
  return (title || '')
    .replace(/[™®©:]/g, ' ')
    .replace(/\([^)]*\)/g, ' ')
    .replace(/\[[^\]]*\]/g, ' ')
    .replace(/[^a-zA-Z0-9 ]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .toLowerCase();
}

function escapeRegExp(str) {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function findFreeDlcGiveawaysForSteamLibraryWeb(giveaways, steamGames) {
  if (!Array.isArray(giveaways) || !Array.isArray(steamGames) || steamGames.length === 0) return [];
  const indexedOwned = steamGames
    .map(g => ({ clean: normalizeGameMatchTitle(g.name), game: g }))
    .filter(x => x.clean.length >= 4)
    .sort((a, b) => b.clean.length - a.clean.length);

  if (indexedOwned.length === 0) return [];

  const matches = [];
  for (const g of giveaways) {
    if (g.status && g.status.toLowerCase() !== 'active') continue;
    const typeLower = (g.type || '').toLowerCase();
    const isDlc = typeLower.includes('dlc') || typeLower.includes('loot') || DLC_GIVEAWAY_REGEX.test(g.title || '');
    if (!isDlc) continue;

    const gNorm = normalizeGameMatchTitle(g.title);
    if (!gNorm) continue;

    const matched = indexedOwned.find(({ clean }) => {
      if (gNorm === clean) return false;
      const rx = new RegExp(`(?:^|\\s)${escapeRegExp(clean)}(?:\\s|$)`);
      return rx.test(gNorm);
    });
    if (!matched) continue;

    const parent = matched.game;
    const steamAppMatch = (g.open_giveaway_url || '').match(/store\.steampowered\.com\/app\/(\d+)/i);
    const dlcAppId = steamAppMatch ? parseInt(steamAppMatch[1], 10) : (9000000 + (g.id || 0));
    const isClaimed = vaultItems.some(v => v.id === g.id);
    const playtimeHrs = ((parent.playtimeMinutes || 0) / 60).toFixed(1);
    const infoStr = parent.playtimeMinutes > 0
      ? t('steam_hours_played', { hours: playtimeHrs })
      : t('dlc_owned_steam');

    matches.push({
      dlcAppId,
      name: g.title,
      parentAppId: parent.appId,
      parentGameName: parent.name,
      parentPlaytimeMinutes: parent.playtimeMinutes || 0,
      headerImageUrl: g.image || g.thumbnail || parent.headerImageUrl,
      shortDescription: g.description || '',
      isFree: true,
      discountPercent: 100,
      priceFormatted: t('card_free'),
      initialPriceFormatted: g.worth && g.worth !== 'N/A' ? g.worth : '',
      recommendationsCount: parseInt(g.users || '0', 10) || 0,
      releaseDate: g.published_date || '',
      reasonBadge: isClaimed ? t('dlc_badge_claimed_free') : t('dlc_badge_free_drop'),
      recommendationReason: t('dlc_reason_free_giveaway', { name: parent.name, info: infoStr }),
      storeUrl: g.open_giveaway_url || `https://store.steampowered.com/app/${parent.appId}`,
      matchedGiveawayId: g.id,
      isSoundtrackOrCosmetic: false,
      relevanceScore: 1500 + Math.min(Math.floor((parent.playtimeMinutes || 0) / 10), 300) + (isClaimed ? 0 : 300)
    });
  }
  return matches.sort((a, b) => b.relevanceScore - a.relevanceScore);
}

function selectCandidateGamesForDlcScanWeb(steamGames, maxGames = 6) {
  return [...steamGames]
    .filter(g => g.appId > 0 && g.name)
    .sort((a, b) => {
      const scoreA = (a.playtime2WeeksMinutes > 0 ? 10000 : 0) + Math.min(a.playtimeMinutes || 0, 10000);
      const scoreB = (b.playtime2WeeksMinutes > 0 ? 10000 : 0) + Math.min(b.playtimeMinutes || 0, 10000);
      return scoreB - scoreA;
    })
    .slice(0, maxGames);
}

async function fetchSteamStoreAppDetailsWeb(appId) {
  const steamLang = window.LootDropI18n?.getMeta?.()?.steamLanguage || 'english';
  const url = `https://store.steampowered.com/api/appdetails?appids=${appId}&l=${steamLang}&cc=us`;
  const json = await fetchJsonWithCorsFallback(url, { skipDirect: true });
  const entry = json?.[String(appId)];
  if (!entry || !entry.success || !entry.data) return null;
  return entry.data;
}

function buildDlcRecommendationFromStoreData(dlcAppId, data, parentGame) {
  if (!data || !data.name || data.release_date?.coming_soon) return null;
  const isCosmeticOrOst = COSMETIC_OR_OST_REGEX.test(data.name);
  const priceObj = data.price_overview || null;
  const isFree = Boolean(data.is_free) || (priceObj && priceObj.final === 0);
  const discountPercent = priceObj?.discount_percent || 0;
  const priceFormatted = isFree ? t('dlc_price_free') : (priceObj?.final_formatted || t('dlc_view_price'));
  const initialPriceFormatted = priceObj?.initial_formatted || '';
  const recsCount = data.recommendations?.total || 0;
  const metacriticScore = data.metacritic?.score || null;

  let score = 0;
  if (isFree) score += 600;
  if (discountPercent > 0) score += discountPercent * 5;
  score += Math.min(Math.floor(recsCount / 15), 500);
  score += Math.min(Math.floor((parentGame.playtimeMinutes || 0) / 15), 300);
  if ((parentGame.playtime2WeeksMinutes || 0) > 0) score += 220;
  if (metacriticScore && metacriticScore > 65) score += (metacriticScore - 65) * 4;
  if (isCosmeticOrOst) score -= 450;

  const hours = ((parentGame.playtimeMinutes || 0) / 60).toFixed(1);
  const playtimeText = (parentGame.playtimeMinutes || 0) > 0
    ? t('steam_hours_played', { hours })
    : t('dlc_in_library');

  let reasonBadge = t('dlc_badge_exp');
  if (isFree) reasonBadge = t('dlc_badge_free');
  else if (discountPercent >= 50) reasonBadge = t('dlc_badge_deal', { pct: discountPercent });
  else if (discountPercent > 0) reasonBadge = t('dlc_badge_off', { pct: discountPercent });
  else if (isCosmeticOrOst) reasonBadge = t('dlc_badge_ost');
  else if (recsCount >= 5000) reasonBadge = t('dlc_badge_fan');
  else if ((parentGame.playtime2WeeksMinutes || 0) > 0) reasonBadge = t('dlc_badge_recent');
  else if ((parentGame.playtimeMinutes || 0) >= 1200) reasonBadge = t('dlc_badge_top');

  let recommendationReason = t('dlc_reason_official', { name: parentGame.name, info: playtimeText });
  if (isFree) recommendationReason = t('dlc_reason_free_addon', { name: parentGame.name, info: playtimeText });
  else if (discountPercent > 0) recommendationReason = t('dlc_reason_sale', { pct: discountPercent, name: parentGame.name, info: playtimeText });
  else if (recsCount >= 1000) recommendationReason = t('dlc_reason_popular', { name: parentGame.name, info: playtimeText });
  else if ((parentGame.playtimeMinutes || 0) > 0) recommendationReason = t('dlc_reason_played', { info: playtimeText, name: parentGame.name });

  const cleanDesc = (data.short_description || '')
    .replace(/<[^>]*>/g, ' ')
    .replace(/&amp;/g, '&')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/\s+/g, ' ')
    .trim();

  return {
    dlcAppId,
    name: data.name.trim(),
    parentAppId: parentGame.appId,
    parentGameName: parentGame.name,
    parentPlaytimeMinutes: parentGame.playtimeMinutes || 0,
    headerImageUrl: data.header_image || `https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/${dlcAppId}/header.jpg`,
    shortDescription: cleanDesc,
    isFree,
    discountPercent,
    priceFormatted,
    initialPriceFormatted,
    recommendationsCount: recsCount,
    releaseDate: data.release_date?.date || '',
    metacriticScore,
    reasonBadge,
    recommendationReason,
    storeUrl: `https://store.steampowered.com/app/${dlcAppId}`,
    matchedGiveawayId: null,
    isSoundtrackOrCosmetic: isCosmeticOrOst,
    relevanceScore: score
  };
}

function getCombinedDlcRecommendations() {
  const ownedIds = new Set(steamGamesList.map(g => g.appId));
  const freeMatches = findFreeDlcGiveawaysForSteamLibraryWeb(allGiveaways, steamGamesList)
    .filter(d => !dismissedDlcIds.has(d.dlcAppId));

  const freeIds = new Set(freeMatches.map(d => d.dlcAppId));
  const filteredStore = storeDlcRecommendations.filter(d =>
    !ownedIds.has(d.dlcAppId) &&
    !dismissedDlcIds.has(d.dlcAppId) &&
    ownedIds.has(d.parentAppId) &&
    !freeIds.has(d.dlcAppId)
  );

  return [...freeMatches, ...filteredStore].sort((a, b) => {
    const aFreeG = a.matchedGiveawayId != null ? 1 : 0;
    const bFreeG = b.matchedGiveawayId != null ? 1 : 0;
    if (bFreeG !== aFreeG) return bFreeG - aFreeG;
    if (a.isSoundtrackOrCosmetic !== b.isSoundtrackOrCosmetic) return a.isSoundtrackOrCosmetic ? 1 : -1;
    return b.relevanceScore - a.relevanceScore;
  });
}

async function refreshWebSteamDlcRecommendations(force = true) {
  if (isScanningDlcs) return;
  if (steamGamesList.length === 0) {
    renderHomeDlcSection();
    return;
  }
  if (!force && isDlcCacheFresh()) return;

  isScanningDlcs = true;
  dlcScanStatus = t('dlc_scanning_store');
  renderHomeDlcSection();
  updateDlcHubStatusUI();

  try {
    const candidates = selectCandidateGamesForDlcScanWeb(steamGamesList, 6);
    const ownedIds = new Set(steamGamesList.map(g => g.appId));
    const seenDlcIds = new Set();
    const collected = [];

    for (const game of candidates) {
      dlcScanStatus = t('dlc_scan_checking', { name: game.name });
      updateDlcHubStatusUI();

      try {
        const gameData = await fetchSteamStoreAppDetailsWeb(game.appId);
        const dlcIds = Array.isArray(gameData?.dlc)
          ? gameData.dlc.filter(id => id > 0 && !ownedIds.has(id) && !dismissedDlcIds.has(id) && !seenDlcIds.has(id) && seenDlcIds.add(id))
          : [];
        if (dlcIds.length === 0) continue;

        const sampled = [
          ...dlcIds.slice(-4).reverse(),
          ...(dlcIds.length > 4 ? [dlcIds[0]] : [])
        ].slice(0, 5);

        for (const dlcId of sampled) {
          try {
            const dlcData = await fetchSteamStoreAppDetailsWeb(dlcId);
            const rec = buildDlcRecommendationFromStoreData(dlcId, dlcData, game);
            if (rec) collected.push(rec);
          } catch (_) {
            // Skip single DLC failure gracefully
          }
        }
      } catch (_) {
        // Skip single parent game failure gracefully
      }
    }

    storeDlcRecommendations = collected.sort((a, b) => {
      if (a.isSoundtrackOrCosmetic !== b.isSoundtrackOrCosmetic) return a.isSoundtrackOrCosmetic ? 1 : -1;
      return b.relevanceScore - a.relevanceScore;
    }).slice(0, 30);

    saveDlcCache(storeDlcRecommendations);
    dlcScanStatus = collected.length > 0
      ? t('dlc_scan_found', { count: collected.length })
      : t('dlc_scan_none');
  } catch (err) {
    dlcScanStatus = t('dlc_scan_err', { err: err.message });
  } finally {
    isScanningDlcs = false;
    updateDlcBadges();
    renderHomeDlcSection();
    updateDlcHubStatusUI();
    renderDlcHubList();
  }
}

async function fetchDlcsForSingleGameWeb(appId) {
  const parentGame = steamGamesList.find(g => g.appId === appId);
  if (!parentGame) return;

  selectedDlcParentAppId = String(appId);
  isLoadingGameDlcs = true;
  dlcScanStatus = t('dlc_scan_single', { name: parentGame.name });
  selectedGameDlcsList = getCombinedDlcRecommendations().filter(d => d.parentAppId === appId);
  updateDlcHubHeader();
  updateDlcHubStatusUI();
  renderDlcHubList();

  try {
    const ownedIds = new Set(steamGamesList.map(g => g.appId));
    const gameData = await fetchSteamStoreAppDetailsWeb(appId);
    const dlcIds = Array.isArray(gameData?.dlc)
      ? gameData.dlc.filter(id => id > 0 && !ownedIds.has(id) && !dismissedDlcIds.has(id))
      : [];

    const targets = Array.from(new Set([
      ...dlcIds.slice(-6).reverse(),
      ...dlcIds.slice(0, 4)
    ])).slice(0, 10);

    const fetchedStore = [];
    for (const dlcId of targets) {
      try {
        const info = await fetchSteamStoreAppDetailsWeb(dlcId);
        const rec = buildDlcRecommendationFromStoreData(dlcId, info, parentGame);
        if (rec) fetchedStore.push(rec);
      } catch (_) {}
    }

    if (fetchedStore.length > 0) {
      const mergedMap = new Map();
      [...storeDlcRecommendations, ...fetchedStore].forEach(d => mergedMap.set(d.dlcAppId, d));
      storeDlcRecommendations = Array.from(mergedMap.values());
      saveDlcCache(storeDlcRecommendations);
    }

    const freeMatches = findFreeDlcGiveawaysForSteamLibraryWeb(allGiveaways, [parentGame])
      .filter(d => !dismissedDlcIds.has(d.dlcAppId));
    const freeIds = new Set(freeMatches.map(d => d.dlcAppId));
    selectedGameDlcsList = [
      ...freeMatches,
      ...fetchedStore.filter(d => !freeIds.has(d.dlcAppId))
    ].sort((a, b) => {
      if (a.isSoundtrackOrCosmetic !== b.isSoundtrackOrCosmetic) return a.isSoundtrackOrCosmetic ? 1 : -1;
      return b.relevanceScore - a.relevanceScore;
    });

    dlcScanStatus = '';
  } catch (err) {
    dlcScanStatus = t('dlc_scan_single_err', { name: parentGame.name, err: err.message });
  } finally {
    isLoadingGameDlcs = false;
    updateDlcBadges();
    updateDlcHubStatusUI();
    renderDlcHubList();
  }
}

function updateDlcBadges() {
  const recs = getCombinedDlcRecommendations();
  const countPill = document.getElementById('vault-dlc-count');
  if (countPill) {
    countPill.textContent = recs.length;
    countPill.style.display = recs.length > 0 ? 'inline-block' : 'none';
  }
  const steamCountEl = document.getElementById('vault-steam-count');
  if (steamCountEl) steamCountEl.textContent = steamGamesList.length;
}

function renderHomeDlcSection() {
  updateDlcBadges();
  const container = document.getElementById('home-dlc-container');
  const scanBtn = document.getElementById('btn-home-dlc-scan');
  const seeAllBtn = document.getElementById('btn-home-dlc-see-all');
  if (!container) return;

  const recs = getCombinedDlcRecommendations();
  const hasSteam = steamGamesList.length > 0;

  if (scanBtn) scanBtn.style.display = hasSteam ? 'inline-flex' : 'none';
  if (seeAllBtn) seeAllBtn.style.display = (hasSteam && recs.length > 0) ? 'inline-flex' : 'none';

  if (!hasSteam) {
    container.innerHTML = `
      <div class="dlc-link-steam-card">
        <div class="dlc-link-steam-top">
          <div class="dlc-link-icon">🧩</div>
          <div class="dlc-link-copy">
            <h4>${escapeHtml(t('dlc_home_cta_title'))}</h4>
            <p>${escapeHtml(t('dlc_home_cta_desc'))}</p>
          </div>
        </div>
        <div>
          <button class="btn btn-sm btn-dlc" id="btn-home-dlc-link-steam">${escapeHtml(t('dlc_home_cta_btn'))}</button>
        </div>
      </div>
    `;
    document.getElementById('btn-home-dlc-link-steam')?.addEventListener('click', () => switchTab('pane-settings'));
    return;
  }

  if (isScanningDlcs && recs.length === 0) {
    container.innerHTML = `
      <div class="dlc-scan-status-bar">
        <div class="spinner dlc-mini-spinner"></div>
        <span>${escapeHtml(dlcScanStatus || t('dlc_scanning_store'))}</span>
      </div>
    `;
    return;
  }

  if (recs.length === 0) {
    const emptyDescFallback = t('dlc_home_empty_desc');
    const syncedTitle = t('dlc_home_synced_title', { count: steamGamesList.length });
    container.innerHTML = `
      <div class="dlc-link-steam-card">
        <div class="dlc-link-steam-top">
          <div class="dlc-link-icon">✨</div>
          <div class="dlc-link-copy">
            <h4>${escapeHtml(syncedTitle)}</h4>
            <p>${escapeHtml(dlcScanStatus || emptyDescFallback)}</p>
          </div>
        </div>
        <div>
          <button class="btn btn-sm btn-dlc" id="btn-home-dlc-empty-scan">${escapeHtml(t('dlc_scan_now_btn'))}</button>
        </div>
      </div>
    `;
    document.getElementById('btn-home-dlc-empty-scan')?.addEventListener('click', () => refreshWebSteamDlcRecommendations(true));
    return;
  }

  const freeLbl = t('card_free');
  const forPrefix = t('dlc_for_prefix');
  container.innerHTML = `
    <div class="dlc-carousel">
      ${recs.slice(0, 10).map(dlc => {
        const badgeCls = dlc.isFree ? 'free' : (dlc.discountPercent > 0 ? 'sale' : '');
        const cardCls = dlc.isFree ? 'is-free' : (dlc.discountPercent > 0 ? 'is-sale' : '');
        const hours = ((dlc.parentPlaytimeMinutes || 0) / 60).toFixed(1);
        const parentLabel = dlc.parentPlaytimeMinutes > 0 ? `${dlc.parentGameName} • ${hours}h` : dlc.parentGameName;
        return `
          <div class="dlc-carousel-card ${cardCls}" data-dlcid="${dlc.dlcAppId}">
            <div class="dlc-thumb-wrap">
              ${createLazyThumbImgHtml(dlc.headerImageUrl, dlc.name, 'dlc-thumb-img', `https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/${dlc.parentAppId}/header.jpg`)}
              <div class="dlc-thumb-scrim"></div>
              <span class="dlc-reason-badge ${badgeCls}">${escapeHtml(dlc.reasonBadge)}</span>
              <button class="dlc-dismiss-btn" data-dismiss-dlcid="${dlc.dlcAppId}" title="${escapeHtml(t('dlc_hide_btn'))}">✕</button>
              <div class="dlc-parent-pill">${forPrefix} ${escapeHtml(parentLabel)}</div>
            </div>
            <div class="dlc-card-body">
              <div class="dlc-card-title">${escapeHtml(dlc.name)}</div>
              <div class="dlc-card-reason">${escapeHtml(dlc.recommendationReason)}</div>
              <div class="dlc-card-footer">
                <div class="dlc-price-group">
                  ${dlc.initialPriceFormatted && dlc.initialPriceFormatted !== dlc.priceFormatted ? `<span class="dlc-price-strike">${escapeHtml(dlc.initialPriceFormatted)}</span>` : ''}
                  <span class="dlc-price-pill ${badgeCls}">${dlc.isFree ? escapeHtml(freeLbl) : escapeHtml(dlc.priceFormatted)}</span>
                </div>
                <button class="btn btn-sm ${dlc.isFree ? 'btn-primary' : 'btn-dlc'} open-dlc-action-btn" data-dlcid="${dlc.dlcAppId}">
                  ${escapeHtml(dlc.matchedGiveawayId != null ? t('dlc_claim_free_short') : t('dlc_view_btn'))}
                </button>
              </div>
            </div>
          </div>
        `;
      }).join('')}
    </div>
  `;

  container.querySelectorAll('.dlc-carousel-card').forEach(card => {
    card.addEventListener('click', (e) => {
      if (e.target.closest('.dlc-dismiss-btn')) return;
      const dlcId = parseInt(card.dataset.dlcid, 10);
      const dlc = getCombinedDlcRecommendations().find(d => d.dlcAppId === dlcId);
      if (dlc) openDlcRecommendationWeb(dlc);
    });
  });

  container.querySelectorAll('.dlc-dismiss-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      dismissDlcWeb(parseInt(btn.dataset.dismissDlcid, 10));
    });
  });

  initLazyThumbnails(container);
}

function openDlcHubModal(gameAppId = 'all') {
  const modal = document.getElementById('modal-dlc-hub');
  if (!modal) return;

  const gameSelect = document.getElementById('dlc-hub-game-select');
  if (gameSelect) {
    const sorted = [...steamGamesList].sort((a, b) => (b.playtimeMinutes || 0) - (a.playtimeMinutes || 0));
    gameSelect.innerHTML = `<option value="all">${escapeHtml(t('dlc_select_all_count', { count: steamGamesList.length }))}</option>` +
      sorted.map(g => `<option value="${g.appId}">${escapeHtml(g.name)} (${((g.playtimeMinutes || 0) / 60).toFixed(1)}h)</option>`).join('');
    gameSelect.value = String(gameAppId);
  }

  selectedDlcParentAppId = String(gameAppId);
  modal.classList.add('open');

  if (gameAppId !== 'all') {
    fetchDlcsForSingleGameWeb(parseInt(gameAppId, 10));
  } else {
    selectedGameDlcsList = [];
    updateDlcHubHeader();
    updateDlcHubStatusUI();
    renderDlcHubList();
  }
}

function updateDlcHubHeader() {
  const titleEl = document.getElementById('dlc-hub-title');
  const subEl = document.getElementById('dlc-hub-subtitle');
  if (!titleEl || !subEl) return;

  if (selectedDlcParentAppId === 'all') {
    titleEl.textContent = t('dlc_title');
    subEl.textContent = t('dlc_sub');
  } else {
    const game = steamGamesList.find(g => String(g.appId) === String(selectedDlcParentAppId));
    titleEl.textContent = game ? t('dlc_for_game', { name: game.name }) : t('dlc_title');
    subEl.textContent = game
      ? t('dlc_played_steam', { hours: ((game.playtimeMinutes || 0) / 60).toFixed(1) })
      : t('dlc_official_exp');
  }
}

function updateDlcHubStatusUI() {
  const bar = document.getElementById('dlc-hub-status-bar');
  const textEl = document.getElementById('dlc-hub-status-text');
  const spinnerEl = document.getElementById('dlc-hub-spinner');
  if (!bar || !textEl) return;

  const busy = isScanningDlcs || isLoadingGameDlcs;
  if (busy) {
    bar.style.display = 'flex';
    if (spinnerEl) spinnerEl.style.display = 'block';
    textEl.textContent = dlcScanStatus || t('dlc_fetching_meta');
  } else {
    bar.style.display = 'none';
  }
}

function renderDlcHubList() {
  const listEl = document.getElementById('dlc-hub-list');
  if (!listEl) return;

  const source = selectedDlcParentAppId === 'all' ? getCombinedDlcRecommendations() : selectedGameDlcsList;
  let filtered = [...source];
  if (activeDlcHubFilter === 'free') {
    filtered = filtered.filter(d => d.isFree);
  } else if (activeDlcHubFilter === 'sale') {
    filtered = filtered.filter(d => d.discountPercent > 0 || d.isFree);
  } else if (activeDlcHubFilter === 'top') {
    filtered = filtered.filter(d => !d.isSoundtrackOrCosmetic).sort((a, b) => (b.recommendationsCount || 0) - (a.recommendationsCount || 0));
  }

  if (filtered.length === 0) {
    if (isScanningDlcs || isLoadingGameDlcs) {
      listEl.innerHTML = '';
      return;
    }
    listEl.innerHTML = `
      <div style="text-align:center; padding: 36px 20px; color: var(--color-text-muted);">
        <span style="font-size: 38px; display:block; margin-bottom:10px;">🧩</span>
        <h4>${escapeHtml(steamGamesList.length === 0 ? t('dlc_empty_no_steam_title') : t('dlc_empty_none_title'))}</h4>
        <p style="font-size:12px; margin-top:6px; margin-bottom:14px;">
          ${escapeHtml(steamGamesList.length === 0 ? t('dlc_empty_no_steam_desc') : t('dlc_empty_rescan_desc'))}
        </p>
        ${steamGamesList.length === 0
          ? `<button class="btn btn-sm btn-primary" onclick="document.getElementById('modal-dlc-hub').classList.remove('open'); switchTab('pane-settings');">${escapeHtml(t('dlc_link_account_btn'))}</button>`
          : `<button class="btn btn-sm btn-dlc" onclick="document.getElementById('btn-dlc-hub-refresh').click();">${escapeHtml(t('dlc_scan_now_btn'))}</button>`
        }
      </div>
    `;
    return;
  }

  const freeLbl = t('card_free');
  const reviewsWord = t('dlc_reviews_word');
  listEl.innerHTML = filtered.map(dlc => {
    const badgeCls = dlc.isFree ? 'free' : (dlc.discountPercent > 0 ? 'sale' : '');
    const cardCls = dlc.isFree ? 'is-free' : (dlc.discountPercent > 0 ? 'is-sale' : '');
    const hours = ((dlc.parentPlaytimeMinutes || 0) / 60).toFixed(1);
    const recsText = dlc.recommendationsCount > 0 ? `${dlc.recommendationsCount.toLocaleString()} ${reviewsWord}` : '';

    return `
      <div class="dlc-detail-card ${cardCls}">
        <div class="dlc-thumb-wrap">
          ${createLazyThumbImgHtml(dlc.headerImageUrl, dlc.name, 'dlc-thumb-img', `https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/${dlc.parentAppId}/header.jpg`)}
          <div class="dlc-thumb-scrim"></div>
          <span class="dlc-reason-badge ${badgeCls}">${escapeHtml(dlc.reasonBadge)}</span>
          <div class="dlc-parent-pill">🎮 ${escapeHtml(dlc.parentGameName)}${dlc.parentPlaytimeMinutes > 0 ? ` • ${escapeHtml(t('dlc_played_suffix', { hours }))}` : ''}</div>
        </div>
        <div class="dlc-card-body">
          <div class="dlc-card-footer" style="margin-top:0; padding-top:0;">
            <strong style="font-size:15px; color:var(--color-text-main);">${escapeHtml(dlc.name)}</strong>
            <div class="dlc-price-group">
              ${dlc.initialPriceFormatted && dlc.initialPriceFormatted !== dlc.priceFormatted ? `<span class="dlc-price-strike">${escapeHtml(dlc.initialPriceFormatted)}</span>` : ''}
              <span class="dlc-price-pill ${badgeCls}">${dlc.isFree ? escapeHtml(freeLbl) : escapeHtml(dlc.priceFormatted)}</span>
            </div>
          </div>
          <div style="font-size:12px; font-weight:700; color:#D8B4FE;">${escapeHtml(dlc.recommendationReason)}</div>
          ${dlc.shortDescription ? `<p class="dlc-detail-desc">${escapeHtml(dlc.shortDescription)}</p>` : ''}
          <div class="dlc-meta-chips">
            ${recsText ? `<span class="dlc-meta-pill">👍 ${escapeHtml(recsText)}</span>` : ''}
            ${dlc.metacriticScore ? `<span class="dlc-meta-pill" style="background:rgba(16,185,129,0.16); color:#34D399;">MC ${dlc.metacriticScore}</span>` : ''}
            ${dlc.releaseDate ? `<span>${escapeHtml(dlc.releaseDate)}</span>` : ''}
          </div>
          <div class="dlc-detail-actions">
            <button class="btn btn-sm btn-outline hub-dismiss-dlc-btn" data-dlcid="${dlc.dlcAppId}">${escapeHtml(t('dlc_hide_btn'))}</button>
            <button class="btn btn-sm ${dlc.isFree ? 'btn-primary' : 'btn-dlc'} hub-open-dlc-btn" data-dlcid="${dlc.dlcAppId}" style="flex:1;">
              ${escapeHtml(dlc.matchedGiveawayId != null ? t('dlc_claim_free_btn') : t('dlc_open_store_btn'))}
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');

  listEl.querySelectorAll('.hub-dismiss-dlc-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      dismissDlcWeb(parseInt(btn.dataset.dlcid, 10));
    });
  });

  listEl.querySelectorAll('.hub-open-dlc-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const dlcId = parseInt(btn.dataset.dlcid, 10);
      const dlc = source.find(d => d.dlcAppId === dlcId);
      if (dlc) openDlcRecommendationWeb(dlc);
    });
  });

  initLazyThumbnails(listEl);
}

function dismissDlcWeb(dlcAppId) {
  dismissedDlcIds.add(dlcAppId);
  localStorage.setItem(STORAGE_KEYS.DLC_DISMISSED, JSON.stringify(Array.from(dismissedDlcIds)));
  selectedGameDlcsList = selectedGameDlcsList.filter(d => d.dlcAppId !== dlcAppId);
  updateDlcBadges();
  renderHomeDlcSection();
  renderDlcHubList();
  showToast(t('toast_dlc_hidden'), '👁️');
}

function openDlcRecommendationWeb(dlc) {
  if (dlc.matchedGiveawayId != null) {
    const giveaway = allGiveaways.find(g => g.id === dlc.matchedGiveawayId);
    if (giveaway) {
      document.getElementById('modal-dlc-hub')?.classList.remove('open');
      openGiveawayDetail(giveaway);
      return;
    }
  }
  const target = dlc.storeUrl || `https://store.steampowered.com/app/${dlc.dlcAppId}`;
  openDealBrowser(target, dlc.name);
}
