/**
 * LootDrop V3.2.0 - Complete Client Application
 * 1-to-1 Web Edition with 19 Accent Themes & Comprehensive Website Alerts
 */

// ==========================================
// 19 Official Accent Themes (From AppPreferences.kt)
// ==========================================
const ACCENT_THEMES = [
  // Cyber (5)
  { id: 'theme-cyber-cyan', title: 'Cyber Cyan', short: 'Cyan', cat: 'cyber', primary: '#00F0FF', secondary: '#00FF88', desc: 'Vibrant high-contrast cyber blue' },
  { id: 'theme-gog-purple', title: 'Ultraviolet', short: 'Ultra Violet', cat: 'cyber', primary: '#A855F7', secondary: '#D946EF', desc: 'Deep electric violet & magenta' },
  { id: 'theme-razer-lime', title: 'Toxic Lime', short: 'Toxic Lime', cat: 'cyber', primary: '#22C55E', secondary: '#10B981', desc: 'High-energy matrix green' },
  { id: 'theme-inferno-red', title: 'Inferno Red', short: 'Inferno', cat: 'cyber', primary: '#EF4444', secondary: '#F97316', desc: 'Warm battle red & ember' },
  { id: 'theme-cherry-rose', title: 'Cherry Blossom', short: 'Sakura', cat: 'cyber', primary: '#EC4899', secondary: '#F43F5E', desc: 'Vivid neon pink & sakura' },

  // Gaming (6)
  { id: 'theme-steam-blue', title: 'Steam Sky', short: 'Steam', cat: 'gaming', primary: '#38BDF8', secondary: '#818CF8', desc: 'Classic PC gaming blue' },
  { id: 'theme-epic-indigo', title: 'Epic Indigo', short: 'Epic', cat: 'gaming', primary: '#6366F1', secondary: '#8B5CF6', desc: 'Deep launcher indigo' },
  { id: 'theme-playstation-blue', title: 'PlayStation Blue', short: 'PlayStation', cat: 'gaming', primary: '#0070D1', secondary: '#38BDF8', desc: 'Official PlayStation electric cobalt' },
  { id: 'theme-xbox-green', title: 'Xbox Green', short: 'Xbox', cat: 'gaming', primary: '#107C10', secondary: '#22C55E', desc: 'Iconic Xbox emerald neon green' },
  { id: 'theme-nintendo-red', title: 'Nintendo Red', short: 'Nintendo', cat: 'gaming', primary: '#E60012', secondary: '#FF4D5A', desc: 'Iconic Nintendo Switch neon crimson' },
  { id: 'theme-discord-blurple', title: 'Discord Blurple', short: 'Discord', cat: 'gaming', primary: '#5865F2', secondary: '#818CF8', desc: 'Iconic gamer community blurple' },

  // Vault (5)
  { id: 'theme-golden-loot', title: 'Loot Gold', short: 'Loot Gold', cat: 'vault', primary: '#FBBF24', secondary: '#F59E0B', desc: 'Legendary gold drops' },
  { id: 'theme-ocean-teal', title: 'Ocean Teal', short: 'Teal', cat: 'vault', primary: '#14B8A6', secondary: '#06B6D4', desc: 'Deep oceanic crystal teal' },
  { id: 'theme-sunset-coral', title: 'Sunset Coral', short: 'Sunset', cat: 'vault', primary: '#FB923C', secondary: '#F43F5E', desc: 'Warm twilight sunset blaze' },
  { id: 'theme-deep-royale', title: 'Royal Violet', short: 'Royale', cat: 'vault', primary: '#8B5CF6', secondary: '#C084FC', desc: 'Majestic royal imperial purple' },
  { id: 'theme-emerald-mint', title: 'Emerald Vault', short: 'Emerald', cat: 'vault', primary: '#10B981', secondary: '#34D399', desc: 'Lush emerald gemstone green' },

  // Rewards (3)
  { id: 'theme-backlog-slayer', title: 'Backlog Slayer', short: 'Slayer', cat: 'rewards', primary: '#FF0055', secondary: '#FF5500', desc: 'Unlocked via Backlog Challenge! Aggressive neon crimson & blaze' },
  { id: 'theme-vault-cleaner', title: 'Vault Cleaner Gold', short: 'Vault Gold', cat: 'rewards', primary: '#FFD700', secondary: '#00FFCC', desc: 'Unlocked via Backlog Blackout! Pure 24K vault gold & cyber teal' },
  { id: 'theme-nebula-blackout', title: 'Nebula Blackout', short: 'Nebula', cat: 'rewards', primary: '#B026FF', secondary: '#00F0FF', desc: 'Unlocked via Backlog Challenge! Cosmic nebula violet & electric cyan' }
];

// ==========================================
// App State & Local Storage Keys
// ==========================================
const STORAGE_KEYS = {
  VAULT: 'lootdrop_vault_items',
  PROFILE: 'lootdrop_user_profile',
  CHALLENGES: 'lootdrop_challenge_state',
  MILESTONES: 'lootdrop_milestones_claimed',
  THEME: 'lootdrop_accent_theme',
  BG_MODE: 'lootdrop_bg_mode',
  STEAM_ID: 'lootdrop_steam_id',
  NOTIFICATIONS: 'lootdrop_notifications',
  ALERT_SETTINGS: 'lootdrop_alert_settings',
  USE_IN_APP_BROWSER: 'lootdrop_use_in_app_browser'
};

let isUseInAppBrowser = localStorage.getItem(STORAGE_KEYS.USE_IN_APP_BROWSER) !== 'false';

function openDealBrowser(url, title = 'Giveaway Deal') {
  if (!url || url === '#' || url === 'about:blank') return;
  if (!isUseInAppBrowser) {
    window.open(url, '_blank', 'noopener,noreferrer');
    return;
  }
  const modal = document.getElementById('modal-in-app-browser');
  const addressText = document.getElementById('browser-address-text');
  const externalLink = document.getElementById('browser-external-link');
  const iframe = document.getElementById('browser-iframe');
  const fallbackCard = document.getElementById('browser-fallback-card');
  const fallbackBtn = document.getElementById('browser-fallback-btn');

  if (addressText) addressText.textContent = url;
  if (externalLink) externalLink.href = url;
  if (fallbackBtn) fallbackBtn.href = url;

  if (modal) {
    modal.classList.add('open');
  }

  // Check if target storefront blocks iframe embedding
  const isEmbedBlocked = /epicgames\.com|steampowered\.com|gog\.com|xbox\.com|playstation\.com|nintendo\.com/i.test(url);
  if (isEmbedBlocked) {
    if (iframe) iframe.style.display = 'none';
    if (fallbackCard) fallbackCard.style.display = 'flex';
  } else {
    if (fallbackCard) fallbackCard.style.display = 'none';
    if (iframe) {
      iframe.style.display = 'block';
      iframe.src = url;
    }
  }
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

// Catalog of 3x3 Backlog Challenge Blueprints (From BacklogChallengeManager.kt)
const CHALLENGE_BLUEPRINTS = [
  { id: "GOLD_TROPHY", title: "Unlock a Gold Trophy", desc: "Earn any Gold or Mythic tier trophy (<15% unlock rate).", emoji: "🏆" },
  { id: "INDIE_GAME", title: "Play an Indie Game", desc: "Play or advance an indie title in your library for 20+ mins.", emoji: "🕹️" },
  { id: "IMMERSION_SESSION", title: "1-Hour Immersion", desc: "Play an unplayed or classic backlog game for 60+ minutes.", emoji: "⏳" },
  { id: "VAULT_STARTER", title: "Vault Starter", desc: "Start or play a claimed free game from your LootDrop Vault.", emoji: "📦" },
  { id: "BEAT_GAME", title: "Beat a Game", desc: "Finish any backlog game and mark it as Beaten in your Vault!", emoji: "👑" },
  { id: "CONTROLLER_READY", title: "Controller Ready", desc: "Play any game with verified Controller support for 25+ mins.", emoji: "🎮" },
  { id: "TROPHY_COLLECTOR", title: "Trophy Collector", desc: "Unlock 3 total trophies across any games in your collection.", emoji: "💎" },
  { id: "MYTHIC_HUNTER", title: "Mythic Hunter", desc: "Unlock an ultra-rare Mythic trophy (<5% unlock rate).", emoji: "⚡" },
  { id: "TRI_PLAY", title: "Backlog Tri-Play", desc: "Play or log progress in 2 different games this week.", emoji: "🌟" },
  { id: "METACRITIC_80", title: "Critically Acclaimed", desc: "Play a title with Metacritic 80+ for at least 30 minutes.", emoji: "🎖️" },
  { id: "DECK_VERIFIED", title: "Deck Verified Hero", desc: "Play any Steam Deck Verified title from your collection.", emoji: "🔋" },
  { id: "QUICK_SESSION", title: "Bite-Sized Sprint", desc: "Play a quick session friendly game (<30 min loop) for 15+ mins.", emoji: "☕" },
  { id: "UNPLAYED_REVIVAL", title: "Fresh Start", desc: "Boot up an unplayed backlog game and log 30+ minutes.", emoji: "🚀" },
  { id: "FIVE_TROPHIES", title: "Trophy Sprint", desc: "Unlock 5 total trophies across your entire gaming library.", emoji: "🥇" },
  { id: "COMPLETION_JOURNEY", title: "Weekly Finisher", desc: "Have 1 or more backlog games marked as Beaten in your library.", emoji: "🎖️" },
  { id: "COMPACT_STORAGE", title: "Lightweight Gem", desc: "Play any game under 2GB storage footprint for 20+ minutes.", emoji: "💾" },
  { id: "HEAVYWEIGHT_EPIC", title: "Triple-A Immersion", desc: "Play a large 10GB+ title from your library for 45+ minutes.", emoji: "🛡️" },
  { id: "GENRE_HOPPER", title: "Genre Explorer", desc: "Play games across 2 distinct genres this week.", emoji: "🗺️" }
];

// App State
let allGiveaways = [];
let vaultItems = JSON.parse(localStorage.getItem(STORAGE_KEYS.VAULT) || '[]');
let userProfile = JSON.parse(localStorage.getItem(STORAGE_KEYS.PROFILE) || JSON.stringify({
  name: 'Vault Hunter',
  bio: 'Clearing the backlog, one drop at a time.',
  avatar: '🎮',
  title: 'Backlog Slayer'
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

// ==========================================
// Initialization
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
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
});

// ==========================================
// Audio Synthesizer (Gaming Chime)
// ==========================================
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

// ==========================================
// Web Drop Alerts & Notifications System
// ==========================================
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
      showToast('Browser drop alerts enabled!', '🔔');
      playGamerChime();
      document.getElementById('perm-banner').style.display = 'none';
    } else {
      showToast('Browser notifications were declined', 'ℹ️');
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

function initNotificationsList() {
  if (notificationsList.length === 0) {
    notificationsList = [
      { id: 1, title: '🎉 LootDrop V3.2.0 Web Live!', desc: '1-to-1 Web client running directly on GitHub Pages with 19 themes.', timestamp: Date.now(), unread: true },
      { id: 2, title: '🏆 Weekly 3×3 Challenge Ready', desc: 'Complete challenge tiles this week to unlock exclusive themes.', timestamp: Date.now() - 3600000, unread: true },
      { id: 3, title: '❤️ Support on Patreon', desc: 'Help sustain servers and track upcoming F-Droid release.', timestamp: Date.now() - 7200000, unread: false }
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
        <p>No notifications yet. You will receive alerts when new free games drop!</p>
      </div>
    `;
    return;
  }

  container.innerHTML = notificationsList.map(n => `
    <div class="notif-item ${n.unread ? 'unread' : ''}" data-id="${n.id}">
      <span style="font-size: 22px;">🎁</span>
      <div class="notif-text">
        <strong>${escapeHtml(n.title)}</strong>
        <span>${escapeHtml(n.desc)}</span>
      </div>
      ${n.unread ? '<span class="notif-dot"></span>' : ''}
    </div>
  `).join('');

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

// ==========================================
// Theme & Background Mode Management
// ==========================================
function applyThemeAndBg(themeId, bgMode) {
  currentThemeId = themeId;
  currentBgMode = bgMode;

  document.body.className = `${themeId} ${bgMode}`;
  localStorage.setItem(STORAGE_KEYS.THEME, themeId);
  localStorage.setItem(STORAGE_KEYS.BG_MODE, bgMode);

  // Update theme badge in settings
  const foundTheme = ACCENT_THEMES.find(t => t.id === themeId);
  const badge = document.getElementById('current-theme-badge');
  if (badge && foundTheme) {
    badge.textContent = foundTheme.title;
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
    : ACCENT_THEMES.filter(t => t.cat === category);

  container.innerHTML = filtered.map(t => `
    <div class="theme-btn-card ${t.id === currentThemeId ? 'active' : ''}" data-id="${t.id}">
      <div class="theme-preview-dots">
        <span class="theme-dot" style="background: ${t.primary};"></span>
        <span class="theme-dot secondary" style="background: ${t.secondary};"></span>
      </div>
      <div class="theme-info">
        <span class="theme-title">${t.title}</span>
        <span class="theme-cat">${t.cat.toUpperCase()}</span>
      </div>
    </div>
  `).join('');

  container.querySelectorAll('.theme-btn-card').forEach(card => {
    card.addEventListener('click', () => {
      const id = card.dataset.id;
      applyThemeAndBg(id, currentBgMode);
      renderThemeCatalog(category);
      playGamerChime();
      showToast(`Applied ${card.querySelector('.theme-title').textContent} theme!`, '🎨');
    });
  });
}

// ==========================================
// Week & Challenge Calculations
// ==========================================
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

// ==========================================
// Navigation & Tab Switching
// ==========================================
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
}

// ==========================================
// Fetching Live Giveaways (GamerPower API)
// ==========================================
async function fetchGiveaways() {
  try {
    let response;
    try {
      response = await fetch('https://www.gamerpower.com/api/giveaways', {
        headers: { 'Accept': 'application/json' }
      });
    } catch (_) {
      response = await fetch('https://api.allorigins.win/raw?url=' + encodeURIComponent('https://www.gamerpower.com/api/giveaways'));
    }

    if (!response || !response.ok) {
      throw new Error('Network error or rate limited');
    }

    const data = await response.json();
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

// ==========================================
// Render Home Tab
// ==========================================
function renderHome() {
  const featuredContainer = document.getElementById('featured-drops-container');
  const recentContainer = document.getElementById('home-recent-drops-container');
  if (!featuredContainer || !recentContainer) return;

  const featured = allGiveaways.slice(0, 4);
  const recent = allGiveaways.slice(4, 10);

  featuredContainer.innerHTML = featured.map(g => createGiveawayCardHtml(g, true)).join('');
  recentContainer.innerHTML = recent.map(g => createGiveawayCardHtml(g, false)).join('');

  attachCardEvents();
}

// ==========================================
// Render Discover / Radar Tab
// ==========================================
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
    countLabel.textContent = `Showing ${filtered.length} free giveaways found`;
  }

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; padding: 40px; text-align: center; color: var(--color-text-muted);">
        <span style="font-size: 36px; display: block; margin-bottom: 10px;">🔍</span>
        <h3>No Giveaways Matched</h3>
        <p>Try broadening your platform or type filters, or search for a different game.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(g => createGiveawayCardHtml(g, false)).join('');
  attachCardEvents();
}

// ==========================================
// Render Calendar / Expiration Radar Tab
// ==========================================
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
    renderGroup('⚠️ Ending Within 24 Hours', endingToday, '#EF4444') +
    renderGroup('⏳ Ending This Week', endingThisWeek, '#F59E0B') +
    renderGroup('📅 Ending Later This Month', endingLater, '#3B82F6') +
    renderGroup('♾️ Ongoing & Long-Term Freebies', ongoing, '#10B981');

  attachCardEvents();
}

// ==========================================
// Render Vault / Claimed Tab
// ==========================================
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

  const activeStatusChip = document.querySelector('#vault-status-chips .filter-chip.active')?.dataset.status || 'all';
  let displayed = vaultItems;

  if (activeStatusChip !== 'all') {
    displayed = displayed.filter(i => (i.playStatus || 'backlog') === activeStatusChip);
  }

  if (displayed.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; padding: 48px; text-align: center; background: var(--color-surface); border: 1px dashed var(--color-border); border-radius: var(--radius-xl);">
        <span style="font-size: 40px; display: block; margin-bottom: 12px;">💎</span>
        <h3>Your Vault is Ready</h3>
        <p style="color: var(--color-text-muted); font-size: 14px; margin-top: 6px;">
          Bookmark free giveaways from the Radar or sync your Steam 64 ID to track backlog & achievements!
        </p>
      </div>
    `;
    return;
  }

  container.innerHTML = displayed.map(item => `
    <div class="giveaway-card" data-id="${item.id}">
      <div class="giveaway-card-thumb-wrap">
        <img src="${item.thumbnail || item.image || ''}" alt="${escapeHtml(item.title)}" class="giveaway-card-thumb" loading="lazy">
        <div class="card-top-badges">
          <span class="badge-platform">${escapeHtml(item.platforms || 'PC')}</span>
          <span class="badge-type">${(item.playStatus || 'BACKLOG').toUpperCase()}</span>
        </div>
        <div class="badge-price">${escapeHtml(item.worth || 'FREE')}</div>
      </div>
      <div class="giveaway-card-body">
        <h3 class="giveaway-card-title">${escapeHtml(item.title)}</h3>
        <div class="giveaway-card-meta">
          <span>Status:</span>
          <select class="custom-select vault-status-select" data-id="${item.id}" onclick="event.stopPropagation()">
            <option value="backlog" ${item.playStatus === 'backlog' ? 'selected' : ''}>⏳ Backlog</option>
            <option value="playing" ${item.playStatus === 'playing' ? 'selected' : ''}>🎮 Playing</option>
            <option value="beaten" ${item.playStatus === 'beaten' ? 'selected' : ''}>👑 Beaten</option>
            <option value="dropped" ${item.playStatus === 'dropped' ? 'selected' : ''}>⏸️ Paused</option>
          </select>
        </div>
        <div class="card-actions-row">
          <a href="${item.open_giveaway_url || '#'}" target="_blank" rel="noopener" class="btn btn-sm btn-primary claim-deal-btn" data-url="${item.open_giveaway_url || ''}" data-title="${escapeHtml(item.title)}" style="flex:1;" onclick="event.stopPropagation()">
            Launch / Claim ↗
          </a>
          <button class="btn-vault-icon remove-vault-btn" data-id="${item.id}" title="Remove from Vault" onclick="event.stopPropagation()">
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
        showToast(`Updated status to ${newStatus.toUpperCase()}`, '🎮');
        updateStats();
      }
    });
  });

  container.querySelectorAll('.remove-vault-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const id = parseInt(btn.dataset.id, 10);
      vaultItems = vaultItems.filter(v => v.id !== id);
      localStorage.setItem(STORAGE_KEYS.VAULT, JSON.stringify(vaultItems));
      showToast('Removed game from Vault', '🗑️');
      renderVault();
      updateStats();
    });
  });

  attachCardEvents();
}

// ==========================================
// Giveaway Card HTML Generator
// ==========================================
function createGiveawayCardHtml(g, isFeatured = false) {
  const isSaved = vaultItems.some(v => v.id === g.id);
  const platformsStr = g.platforms || 'PC';
  const priceStr = g.worth && g.worth !== 'N/A' ? `${g.worth} FREE` : 'FREE';

  return `
    <div class="giveaway-card ${isFeatured ? 'featured' : ''}" data-id="${g.id}">
      <div class="giveaway-card-thumb-wrap">
        <img src="${g.thumbnail || g.image || ''}" alt="${escapeHtml(g.title)}" class="giveaway-card-thumb" loading="lazy">
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
          <span>👥 ${g.users ? `${g.users} claims` : 'Active'}</span>
        </div>
        <div class="card-actions-row">
          <a href="${g.open_giveaway_url || '#'}" target="_blank" rel="noopener" class="btn btn-sm btn-primary claim-deal-btn" data-url="${g.open_giveaway_url || ''}" data-title="${escapeHtml(g.title)}" style="flex:1;" onclick="event.stopPropagation()">
            Claim Free ↗
          </a>
          <button class="btn-vault-icon ${isSaved ? 'active' : ''}" data-id="${g.id}" title="${isSaved ? 'In Vault' : 'Add to Vault'}" onclick="event.stopPropagation(); toggleVaultItem(${g.id});">
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
      if (isUseInAppBrowser) {
        e.preventDefault();
        e.stopPropagation();
        openDealBrowser(btn.dataset.url, btn.dataset.title);
      }
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
    showToast('Removed from your Vault', '📥');
  } else {
    vaultItems.push({
      ...item,
      playStatus: 'backlog',
      addedAt: Date.now()
    });
    playGamerChime();
    showToast('Added to your Vault!', '💎');
  }

  localStorage.setItem(STORAGE_KEYS.VAULT, JSON.stringify(vaultItems));
  updateStats();
  renderHome();
  renderRadar();
}

// ==========================================
// Giveaway Detail Dialog
// ==========================================
function openGiveawayDetail(g) {
  selectedGiveaway = g;
  const modal = document.getElementById('modal-giveaway-detail');
  if (!modal) return;

  document.getElementById('detail-banner-img').src = g.image || g.thumbnail || '';
  document.getElementById('detail-title').textContent = g.title || '';
  document.getElementById('detail-platform').textContent = g.platforms || 'PC';
  document.getElementById('detail-type').textContent = (g.type || 'GAME').toUpperCase();
  document.getElementById('detail-worth').textContent = g.worth && g.worth !== 'N/A' ? `${g.worth} FREE` : 'FREE';
  document.getElementById('detail-expiration').textContent = formatExpiration(g.end_date);
  document.getElementById('detail-instructions').textContent = g.instructions || 'Claim directly on store page.';
  document.getElementById('detail-description').textContent = g.description || '';
  const claimLink = document.getElementById('detail-claim-link');
  claimLink.href = g.open_giveaway_url || '#';
  claimLink.onclick = (e) => {
    if (isUseInAppBrowser && g.open_giveaway_url) {
      e.preventDefault();
      openDealBrowser(g.open_giveaway_url, g.title);
    }
  };

  const isSaved = vaultItems.some(v => v.id === g.id);
  const vaultBtn = document.getElementById('detail-vault-toggle');
  vaultBtn.textContent = isSaved ? '💎 Remove from Vault' : '📥 Add to Vault';

  modal.classList.add('open');
}

// ==========================================
// 3x3 Backlog Challenge Grid Logic
// ==========================================
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
  const tiles = getWeekBlueprints(weekKey);

  const modalWeekTitle = document.getElementById('modal-week-title');
  const weekDisplayBadge = document.getElementById('week-display-badge');
  const prevBtn = document.getElementById('btn-prev-week');
  const nextBtn = document.getElementById('btn-next-week');

  if (modalWeekTitle) modalWeekTitle.textContent = `Week ${relativeWeekNum} • Anchor 2026-W46`;
  if (weekDisplayBadge) {
    weekDisplayBadge.textContent = weekKey === currentWeekKey ? `Week ${relativeWeekNum} (Active)` : `Week ${relativeWeekNum}`;
  }

  if (prevBtn) prevBtn.disabled = relativeWeekNum <= 1;
  const currentRel = getRelativeWeekNumber(currentWeekKey);
  if (nextBtn) nextBtn.disabled = relativeWeekNum >= (currentRel + MAX_FUTURE_WEEKS_PREVIEW);

  const manualUsed = Object.values(state.manualOverrides || {}).filter(Boolean).length;
  const manualRemaining = Math.max(0, MAX_MANUAL_OVERRIDES - manualUsed);
  const manualText = document.getElementById('manual-unlock-text');
  if (manualText) {
    manualText.innerHTML = `Manual Overrides Remaining: <strong>${manualRemaining} / ${MAX_MANUAL_OVERRIDES}</strong>`;
  }

  const evaluated = tiles.map((tile, idx) => {
    const isManual = !!state.manualOverrides[idx];
    let isAutoDone = false;

    if (tile.id === 'BEAT_GAME' || tile.id === 'COMPLETION_JOURNEY') {
      isAutoDone = vaultItems.some(v => v.playStatus === 'beaten');
    } else if (tile.id === 'VAULT_STARTER' || tile.id === 'UNPLAYED_REVIVAL') {
      isAutoDone = vaultItems.length > 0;
    } else if (tile.id === 'TRI_PLAY') {
      isAutoDone = vaultItems.filter(v => v.playStatus === 'playing').length >= 2;
    }

    const isCompleted = isManual || isAutoDone;
    return { ...tile, index: idx, isCompleted, isManual };
  });

  const gridContainer = document.getElementById('challenge-grid-container');
  if (gridContainer) {
    gridContainer.innerHTML = evaluated.map(t => `
      <div class="challenge-tile ${t.isCompleted ? 'completed' : ''} ${t.isManual ? 'manual' : ''}" data-idx="${t.index}">
        <span class="tile-emoji">${t.emoji}</span>
        <h4 class="tile-title">${t.title}</h4>
        <p class="tile-desc">${t.desc}</p>
        <div class="tile-status-badge ${t.isCompleted ? (t.isManual ? 'manual-done' : 'done') : ''}">
          ${t.isCompleted ? (t.isManual ? '🔑 MANUAL UNLOCK' : '✓ COMPLETED') : 'TAP TO UNLOCK'}
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
    showToast('Recycled manual unlock slot (+1)', '♻️');
  } else {
    const used = Object.values(currentOverrides).filter(Boolean).length;
    if (used >= MAX_MANUAL_OVERRIDES) {
      showToast(`Limit reached: Max ${MAX_MANUAL_OVERRIDES} manual unlocks per week!`, '⚠️');
      return;
    }
    currentOverrides[tileIdx] = true;
    playGamerChime();
    showToast('Tile manually completed! (1 slot used)', '🔑');
  }

  state.manualOverrides = currentOverrides;
  saveChallengeStateForWeek(weekKey, state);
  renderChallengeModal();
}

function checkMilestoneWins(evaluated, weekKey) {
  const completedRows = [0, 1, 2].filter(r => 
    evaluated[r * 3].isCompleted && evaluated[r * 3 + 1].isCompleted && evaluated[r * 3 + 2].isCompleted
  );

  const completedCols = [0, 1, 2].filter(c => 
    evaluated[c].isCompleted && evaluated[c + 3].isCompleted && evaluated[c + 6].isCompleted
  );

  const diag1 = evaluated[0].isCompleted && evaluated[4].isCompleted && evaluated[8].isCompleted;
  const diag2 = evaluated[2].isCompleted && evaluated[4].isCompleted && evaluated[6].isCompleted;
  const totalLines = completedRows.length + completedCols.length + (diag1 ? 1 : 0) + (diag2 ? 1 : 0);
  const isBlackout = evaluated.every(t => t.isCompleted);

  const claimedMilestones = JSON.parse(localStorage.getItem(STORAGE_KEYS.MILESTONES) || '{}');

  const milestones = [
    {
      id: `backlog_slayer_${weekKey}`,
      name: 'Backlog Slayer Trophy & Theme',
      sub: 'Complete any 3-tile line (row, col, or diag)',
      isEligible: totalLines > 0,
      rewardTheme: 'theme-backlog-slayer',
      trophy: '🏆 Backlog Slayer'
    },
    {
      id: `row_raider_${weekKey}`,
      name: 'Row Raider Trophy',
      sub: 'Complete any horizontal row this week',
      isEligible: completedRows.length > 0,
      trophy: '🥇 Row Raider'
    },
    {
      id: `column_conqueror_${weekKey}`,
      name: 'Column Conqueror Trophy',
      sub: 'Complete any vertical column this week',
      isEligible: completedCols.length > 0,
      trophy: '🥈 Column Conqueror'
    },
    {
      id: `blackout_champion_${weekKey}`,
      name: 'Blackout Champion Trophy & Vault Cleaner Theme',
      sub: 'Blackout all 9 tiles on the grid',
      isEligible: isBlackout,
      rewardTheme: 'theme-vault-cleaner',
      trophy: '⚡ Blackout Champion'
    }
  ];

  const milestonesContainer = document.getElementById('milestones-list-container');
  if (milestonesContainer) {
    milestonesContainer.innerHTML = milestones.map(m => {
      const isClaimed = !!claimedMilestones[m.id];
      return `
        <div class="milestone-item ${m.isEligible ? 'unlocked' : ''}">
          <div class="milestone-info">
            <span class="milestone-name">${m.name}</span>
            <span class="milestone-sub">${m.sub}</span>
          </div>
          <button class="btn btn-sm ${isClaimed ? 'btn-outline' : (m.isEligible ? 'btn-primary' : 'btn-secondary')}" 
                  ${!m.isEligible || isClaimed ? 'disabled' : ''} 
                  onclick="claimMilestone('${m.id}', '${m.rewardTheme || ''}', '${m.trophy}')">
            ${isClaimed ? '✓ Claimed' : (m.isEligible ? '🎁 Claim' : 'Locked')}
          </button>
        </div>
      `;
    }).join('');
  }
}

window.claimMilestone = function(id, rewardTheme, trophy) {
  const claimedMilestones = JSON.parse(localStorage.getItem(STORAGE_KEYS.MILESTONES) || '{}');
  claimedMilestones[id] = true;
  localStorage.setItem(STORAGE_KEYS.MILESTONES, JSON.stringify(claimedMilestones));

  triggerConfetti();
  playGamerChime();

  if (rewardTheme) {
    applyThemeAndBg(rewardTheme, currentBgMode);
    renderThemeCatalog('all');
    showToast(`Claimed ${trophy}! Unlocked and applied new theme!`, '🎉');
  } else {
    showToast(`Claimed ${trophy}! Displayed on your profile!`, '🏆');
  }

  renderChallengeModal();
  renderProfile();
};

function renderMiniChallengeWidget() {
  const widget = document.getElementById('home-challenge-widget');
  if (!widget) return;

  const state = getChallengeStateForWeek(currentWeekKey);
  const manualCount = Object.values(state.manualOverrides || {}).filter(Boolean).length;
  const completedCount = manualCount + (vaultItems.length > 0 ? 1 : 0);
  const pct = Math.min(100, Math.round((completedCount / 9) * 100));

  const progressFill = document.getElementById('home-challenge-progress-fill');
  const progressText = document.getElementById('home-challenge-progress-text');
  const weekLabel = document.getElementById('home-challenge-week-label');

  if (progressFill) progressFill.style.width = `${pct}%`;
  if (progressText) progressText.textContent = `${completedCount} / 9 Tiles Completed`;
  if (weekLabel) weekLabel.textContent = `Week ${getRelativeWeekNumber(currentWeekKey)} • Active`;
}

// ==========================================
// Backlog Roulette
// ==========================================
function openRouletteModal() {
  document.getElementById('modal-roulette')?.classList.add('open');
}

function spinRoulette() {
  const candidates = vaultItems.length > 0 ? vaultItems : allGiveaways;
  if (!candidates || candidates.length === 0) {
    showToast('No games available in backlog to spin!', '🎲');
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
    platformEl.textContent = `${randomPick.platforms || 'PC'} • Worth ${randomPick.worth || 'FREE'}`;
    counter++;

    if (counter >= totalSpins) {
      clearInterval(interval);
      spinBtn.disabled = false;
      const finalPick = candidates[Math.floor(Math.random() * candidates.length)];
      titleEl.textContent = finalPick.title;
      platformEl.textContent = `Selected: ${finalPick.platforms || 'PC'}! Enjoy playing!`;
      triggerConfetti();
      playGamerChime();
      showToast(`Fate has spoken: Play ${finalPick.title}!`, '🎰');
    }
  }, speed);
}

// ==========================================
// Profile & Trophies Showcase
// ==========================================
function renderProfile() {
  const nameDisplay = document.getElementById('profile-name-display');
  const bioDisplay = document.getElementById('profile-bio-display');
  const nameInput = document.getElementById('edit-profile-name');
  const bioInput = document.getElementById('edit-profile-bio');
  const trophiesContainer = document.getElementById('profile-trophies-container');

  if (nameDisplay) nameDisplay.textContent = userProfile.name;
  if (bioDisplay) bioDisplay.textContent = userProfile.bio;
  if (nameInput) nameInput.value = userProfile.name;
  if (bioInput) bioInput.value = userProfile.bio;

  const claimedMilestones = JSON.parse(localStorage.getItem(STORAGE_KEYS.MILESTONES) || '{}');
  const allTrophies = [
    { title: 'Backlog Slayer', key: 'backlog_slayer', icon: '🏆', rarity: 'MYTHIC' },
    { title: 'Row Raider', key: 'row_raider', icon: '🥇', rarity: 'GOLD' },
    { title: 'Column Conqueror', key: 'column_conqueror', icon: '🥈', rarity: 'SILVER' },
    { title: 'Blackout Champion', key: 'blackout_champion', icon: '⚡', rarity: 'MYTHIC' }
  ];

  if (trophiesContainer) {
    trophiesContainer.innerHTML = allTrophies.map(t => {
      const isUnlocked = Object.keys(claimedMilestones).some(k => k.startsWith(t.key));
      return `
        <div class="trophy-badge-card ${isUnlocked ? 'unlocked' : ''}">
          <span class="trophy-icon">${t.icon}</span>
          <span class="trophy-title">${t.title}</span>
          <span style="font-size: 9px; color: var(--color-primary); font-weight:800; margin-top:2px;">
            ${isUnlocked ? 'UNLOCKED' : 'LOCKED'}
          </span>
        </div>
      `;
    }).join('');
  }
}

// ==========================================
// Toast Messages & Confetti
// ==========================================
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

// ==========================================
// Stats Calculation & Helpers
// ==========================================
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
  if (!dateStr || dateStr.startsWith('N/A')) return 'Ongoing / Unlimited';
  const end = new Date(dateStr);
  const now = new Date();
  const diffHours = Math.round((end - now) / (1000 * 60 * 60));

  if (diffHours <= 0) return 'Expired';
  if (diffHours < 24) return `Expires in ${diffHours}h`;
  const days = Math.round(diffHours / 24);
  return `Expires in ${days}d`;
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

// ==========================================
// Alert Settings Sheet UI Synchronization
// ==========================================
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

// ==========================================
// Setup Global UI Event Listeners
// ==========================================
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
    showToast('Playing synthesized gaming chime sound', '🎵');
  });

  document.getElementById('btn-test-notification')?.addEventListener('click', () => {
    triggerWebsiteAlert(
      '🚨 New Drop Alert: Warhammer 40K',
      'Warhammer 40,000: Gladius is currently 100% FREE on Steam! Grab it before it ends.',
      'https://store.steampowered.com/',
      '🎮'
    );
  });

  document.getElementById('btn-fire-test-alert')?.addEventListener('click', () => {
    triggerWebsiteAlert(
      '⚡ High-Value Freebie: Cyberpunk OST',
      'The complete digital soundtrack is free on GOG. Claim to keep forever!',
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
    showToast('Alert preferences saved successfully!', '🔔');
  });

  // Notification Inbox Actions
  document.getElementById('btn-mark-all-read')?.addEventListener('click', () => {
    notificationsList.forEach(n => n.unread = false);
    localStorage.setItem(STORAGE_KEYS.NOTIFICATIONS, JSON.stringify(notificationsList));
    renderNotificationsList();
    showToast('Marked all alerts as read', '✓');
  });

  document.getElementById('btn-clear-notifs')?.addEventListener('click', () => {
    notificationsList = [];
    localStorage.removeItem(STORAGE_KEYS.NOTIFICATIONS);
    renderNotificationsList();
    showToast('Cleared notification history', '🗑️');
  });

  // In-App Browser Preference Toggle
  const inAppBrowserToggle = document.getElementById('setting-use-in-app-browser');
  if (inAppBrowserToggle) {
    inAppBrowserToggle.checked = isUseInAppBrowser;
    inAppBrowserToggle.addEventListener('change', (e) => {
      isUseInAppBrowser = e.target.checked;
      localStorage.setItem(STORAGE_KEYS.USE_IN_APP_BROWSER, isUseInAppBrowser ? 'true' : 'false');
      showToast(isUseInAppBrowser ? 'In-App Browser enabled' : 'External browser enabled', '🌐');
    });
  }

  // In-App Browser Controls
  document.getElementById('close-browser-btn')?.addEventListener('click', () => {
    document.getElementById('modal-in-app-browser')?.classList.remove('open');
    const iframe = document.getElementById('browser-iframe');
    if (iframe) iframe.src = 'about:blank';
  });

  document.getElementById('browser-reload-btn')?.addEventListener('click', () => {
    const url = document.getElementById('browser-address-text')?.textContent;
    if (url) openDealBrowser(url);
  });

  document.getElementById('browser-copy-btn')?.addEventListener('click', () => {
    const url = document.getElementById('browser-address-text')?.textContent;
    if (url) {
      navigator.clipboard.writeText(url);
      showToast('Deal URL copied to clipboard!', '📋');
    }
  });

  // Background Mode Buttons
  document.querySelectorAll('.bg-mode-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const mode = btn.dataset.bg;
      applyThemeAndBg(currentThemeId, mode);
      showToast(`Switched to ${btn.textContent.trim()} mode!`, '🌓');
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
      document.getElementById('detail-vault-toggle').textContent = isSaved ? '💎 Remove from Vault' : '📥 Add to Vault';
    }
  });

  // Detail Modal Share Button
  document.getElementById('detail-share-btn')?.addEventListener('click', () => {
    if (!selectedGiveaway) return;
    if (navigator.share) {
      navigator.share({
        title: `LootDrop: Free ${selectedGiveaway.title}`,
        text: `Grab ${selectedGiveaway.title} 100% free on ${selectedGiveaway.platforms || 'PC'} before it expires!`,
        url: selectedGiveaway.open_giveaway_url || window.location.href
      }).catch(() => {});
    } else {
      navigator.clipboard.writeText(selectedGiveaway.open_giveaway_url || window.location.href);
      showToast('Giveaway link copied to clipboard!', '🔗');
    }
  });

  // Profile Save
  document.getElementById('btn-save-profile')?.addEventListener('click', () => {
    const name = document.getElementById('edit-profile-name').value.trim() || 'Vault Hunter';
    const bio = document.getElementById('edit-profile-bio').value.trim() || 'Clearing the backlog, one drop at a time.';
    userProfile.name = name;
    userProfile.bio = bio;
    localStorage.setItem(STORAGE_KEYS.PROFILE, JSON.stringify(userProfile));
    renderProfile();
    showToast('Profile updated!', '👤');
  });

  // Steam 64 ID Sync
  document.getElementById('btn-save-steam-id')?.addEventListener('click', () => {
    const input = document.getElementById('steam-id-input').value.trim();
    if (!input) {
      showToast('Please enter a Steam 64 ID or Vanity URL', '⚠️');
      return;
    }
    localStorage.setItem(STORAGE_KEYS.STEAM_ID, input);
    document.getElementById('steam-status-hint').textContent = `Linked Steam ID: ${input} (Ready to sync)`;
    showToast('Steam 64 ID saved!', '🎮');
  });

  document.getElementById('btn-sync-steam')?.addEventListener('click', () => {
    const savedId = localStorage.getItem(STORAGE_KEYS.STEAM_ID);
    if (!savedId) {
      switchTab('pane-settings');
      showToast('Enter your Steam 64 ID in Settings first!', 'ℹ️');
      return;
    }
    showToast('Synchronizing Steam backlog & achievements...', '🔄');
  });

  // Backup Export
  document.getElementById('btn-export-backup')?.addEventListener('click', () => {
    const backupData = {
      version: '3.2.0',
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
    showToast('Backup JSON exported successfully!', '💾');
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
        showToast('Backup successfully restored!', '📂');
        renderProfile();
        renderVault();
        updateStats();
      } catch (err) {
        showToast('Failed to parse backup JSON file', '❌');
      }
    };
    reader.readAsText(file);
  });

  // Clear Vault
  document.getElementById('btn-clear-vault')?.addEventListener('click', () => {
    if (confirm('Are you sure you want to clear your LootDrop Vault?')) {
      vaultItems = [];
      localStorage.removeItem(STORAGE_KEYS.VAULT);
      renderVault();
      updateStats();
      showToast('Vault cleared', '🗑️');
    }
  });
}
