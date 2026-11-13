/**
 * LootDrop V3.3.0 - Complete Client Application
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
  STEAM_GAMES: 'lootdrop_steam_games',
  DLC_CACHE: 'lootdrop_steam_dlc_cache',
  DLC_DISMISSED: 'lootdrop_dismissed_dlcs',
  NOTIFICATIONS: 'lootdrop_notifications',
  ALERT_SETTINGS: 'lootdrop_alert_settings'
};

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
  fetchPublicReviewsFromR2();
  initReviewsModal();
  initSteamAndDlcFeatures();
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
      { id: 1, title: '🎉 LootDrop V3.3.0 Web Live!', desc: '1-to-1 Web client running directly on GitHub Pages with 19 themes.', timestamp: Date.now(), unread: true },
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
  renderHomeDlcSection();
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

  const steamCountEl = document.getElementById('vault-steam-count');
  if (steamCountEl) steamCountEl.textContent = steamGamesList.length;

  if (vaultViewMode === 'steam') {
    if (steamGamesList.length === 0) {
      container.innerHTML = `
        <div style="grid-column: 1 / -1; padding: 48px; text-align: center; background: var(--color-surface); border: 1px dashed var(--color-border); border-radius: var(--radius-xl);">
          <span style="font-size: 40px; display: block; margin-bottom: 12px;">🎮</span>
          <h3>No Synced Steam Games Yet</h3>
          <p style="color: var(--color-text-muted); font-size: 14px; margin-top: 6px; margin-bottom: 16px;">
            Link your public Steam 64 ID or Vanity URL in Settings to browse your Steam games and find matching DLCs!
          </p>
          <button class="btn btn-primary" onclick="switchTab('pane-settings')">Link Steam Account ➔</button>
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
            <img src="${headerImg}" onerror="this.onerror=null;this.src='${fallbackImg}';" alt="${escapeHtml(sg.name)}" class="giveaway-card-thumb" loading="lazy">
            <div class="card-top-badges">
              <span class="badge-platform">🎮 STEAM</span>
            </div>
            <div class="badge-price">${sg.playtimeMinutes > 0 ? `${hours}h played` : 'Unplayed'}</div>
          </div>
          <div class="giveaway-card-body">
            <h3 class="giveaway-card-title">${escapeHtml(sg.name)}</h3>
            <div class="card-actions-row">
              <button class="btn btn-sm btn-dlc open-game-dlcs-btn" data-appid="${sg.appId}" style="flex:1;">
                🧩 DLCs
              </button>
              <a href="${storeUrl}" target="_blank" rel="noopener" class="btn btn-sm btn-outline" onclick="event.stopPropagation()">
                Store ↗
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
        <h3>Your Vault is Ready</h3>
        <p style="color: var(--color-text-muted); font-size: 14px; margin-top: 6px;">
          Bookmark free giveaways from the Radar or sync your Steam 64 ID to track backlog & discover DLCs!
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
  claimLink.onclick = null;

  const isSaved = vaultItems.some(v => v.id === g.id);
  const vaultBtn = document.getElementById('detail-vault-toggle');
  vaultBtn.textContent = isSaved ? '💎 Remove from Vault' : '📥 Add to Vault';

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
      ownershipTitle.textContent = 'YOU ALREADY HAVE THIS';
    }
    if (ownershipSubtitle) {
      if (inVault && inSteam) {
        ownershipSubtitle.textContent = 'Saved in your LootDrop Vault and found in your Steam Library!';
      } else if (inVault) {
        ownershipSubtitle.textContent = 'Already saved in your LootDrop Vault.';
      } else {
        ownershipSubtitle.textContent = `Found in your linked Steam library as "${matchedSteam.name}".`;
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
    syncWebSteamLibrary(input);
  });

  document.getElementById('btn-sync-steam')?.addEventListener('click', () => {
    const savedId = localStorage.getItem(STORAGE_KEYS.STEAM_ID);
    if (!savedId) {
      switchTab('pane-settings');
      showToast('Enter your Steam 64 ID in Settings first!', 'ℹ️');
      return;
    }
    syncWebSteamLibrary(savedId);
  });

  // Backup Export
  document.getElementById('btn-export-backup')?.addEventListener('click', () => {
    const backupData = {
      version: '3.3.0',
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

// ==========================================
// Cloudflare R2 Public Reviews System
// ==========================================
const R2_REVIEWS_URL = 'https://lootdrop-storage-proxy.hardingbentley67.workers.dev?accountId=public_reviews&filename=lootdrop_reviews.json';
const R2_AUTH_KEY = 'sb_publishable_htD9aXXk0QHjhG_ifS3nYg_kjUp7osD';
let publicReviewsList = [];
let currentReviewRating = 5;

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
    platform: "LootDrop V3.3.0",
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
    platform: "LootDrop V3.3.0",
    timestamp: Date.now() - 86400000 * 6,
    helpfulCount: 41,
    isVerifiedClaimer: true
  }
];

async function fetchPublicReviewsFromR2() {
  const cached = localStorage.getItem('lootdrop_public_reviews');
  if (cached) {
    try {
      publicReviewsList = JSON.parse(cached);
    } catch (_) {}
  }

  try {
    const resp = await fetch(R2_REVIEWS_URL, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${R2_AUTH_KEY}`,
        'User-Agent': 'LootDrop-Web/3.3.0'
      }
    });
    if (resp.ok) {
      const data = await resp.json();
      if (Array.isArray(data) && data.length > 0) {
        const seedIds = new Set(data.map(d => d.id));
        const merged = [...data];
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
    if (countLabelEl) countLabelEl.textContent = 'Be the first to rate LootDrop App!';
  } else {
    const avg = appReviews.reduce((sum, r) => sum + (r.rating || 5), 0) / count;
    const rounded = Math.round(avg);
    scoreEl.textContent = avg.toFixed(1);
    if (starsEl) starsEl.textContent = '★'.repeat(rounded) + '☆'.repeat(5 - rounded);
    if (countLabelEl) countLabelEl.textContent = `Based on ${count} community ${count === 1 ? 'review' : 'reviews'} across Android & Web.`;
  }

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
    previewEl.innerHTML = '<p class="input-hint">No app reviews posted yet.</p>';
    return;
  }

  previewEl.innerHTML = appReviews.map(r => {
    const starStr = '★'.repeat(r.rating || 5) + '☆'.repeat(5 - (r.rating || 5));
    const initial = (r.authorName || 'G').trim().charAt(0).toUpperCase();
    const isVoted = votedIds.includes(r.id);
    const dateFormatted = new Date(r.timestamp || Date.now()).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });

    return `
      <div class="review-item-card" data-review-id="${r.id}" style="margin-bottom: 10px;">
        <div class="review-card-header">
          <div class="review-author-box">
            <div class="review-author-avatar">${initial}</div>
            <div class="review-author-meta">
              <span class="review-author-name">
                ${escapeHtml(r.authorName || 'Gamer')}
                <span class="review-verified-badge">✓ Verified User</span>
              </span>
              <span class="review-time-platform">${escapeHtml(r.platform || 'LootDrop')} • ${dateFormatted}</span>
            </div>
          </div>
          <div class="review-stars-badge">${starStr}</div>
        </div>
        ${r.title ? `<div class="review-card-title">${escapeHtml(r.title)}</div>` : ''}
        ${r.comment ? `<div class="review-card-body">${escapeHtml(r.comment)}</div>` : ''}
        <div class="review-card-footer">
          <button class="review-helpful-btn ${isVoted ? 'voted' : ''}" data-review-id="${r.id}">
            👍 Helpful (${r.helpfulCount || 0})
          </button>
          <span style="font-size: 10px; color: var(--color-text-dim);">⭐ LootDrop App Review</span>
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
    countLabelEl.textContent = 'Be the first to review this game!';
    listEl.innerHTML = `
      <div class="review-empty-state">
        <p>🎮 No community reviews yet for <strong>${escapeHtml(game.title || 'this game')}</strong>.</p>
        <p style="font-size: 11px; margin-top: 4px;">Click <strong>"Leave Review"</strong> to share your rating with everyone!</p>
      </div>
    `;
    return;
  }

  const avg = reviews.reduce((sum, r) => sum + (r.rating || 5), 0) / reviews.length;
  const rounded = Math.round(avg);
  avgScoreEl.textContent = avg.toFixed(1);
  starsEl.textContent = '★'.repeat(rounded) + '☆'.repeat(5 - rounded);
  countLabelEl.textContent = `Based on ${reviews.length} community ${reviews.length === 1 ? 'review' : 'reviews'}`;

  const votedIds = JSON.parse(localStorage.getItem('lootdrop_voted_reviews') || '[]');

  listEl.innerHTML = reviews.map(r => {
    const starStr = '★'.repeat(r.rating || 5) + '☆'.repeat(5 - (r.rating || 5));
    const initial = (r.authorName || 'G').trim().charAt(0).toUpperCase();
    const isVoted = votedIds.includes(r.id);
    const dateFormatted = new Date(r.timestamp || Date.now()).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });

    return `
      <div class="review-item-card" data-review-id="${r.id}">
        <div class="review-card-header">
          <div class="review-author-box">
            <div class="review-author-avatar">${initial}</div>
            <div class="review-author-meta">
              <span class="review-author-name">
                ${escapeHtml(r.authorName || 'Gamer')}
                ${r.isVerifiedClaimer ? '<span class="review-verified-badge">✓ Verified</span>' : ''}
              </span>
              <span class="review-time-platform">${escapeHtml(r.platform || 'PC')} • ${dateFormatted}</span>
            </div>
          </div>
          <div class="review-stars-badge">${starStr}</div>
        </div>
        ${r.title ? `<div class="review-card-title">${escapeHtml(r.title)}</div>` : ''}
        ${r.comment ? `<div class="review-card-body">${escapeHtml(r.comment)}</div>` : ''}
        <div class="review-card-footer">
          <button class="review-helpful-btn ${isVoted ? 'voted' : ''}" data-review-id="${r.id}">
            👍 Helpful (${r.helpfulCount || 0})
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
    showToast('You already voted helpful on this review!', 'ℹ️');
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
  showToast('Marked as helpful!', '👍');

  // Push updated list to R2
  try {
    await fetch(R2_REVIEWS_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${R2_AUTH_KEY}`,
        'X-Account-Id': 'public_reviews',
        'User-Agent': 'LootDrop-Web/3.3.0'
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
        'User-Agent': 'LootDrop-Web/3.3.0'
      },
      body: JSON.stringify(publicReviewsList.slice(0, 300))
    });
    return resp.ok;
  } catch (err) {
    console.warn('Failed to sync review to Cloudflare R2', err);
    return false;
  }
}

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

  const RATING_TEXTS = {
    1: '1 - Skip / Unplayable',
    2: '2 - Mediocre / Flawed',
    3: '3 - Average / Fun with Friends',
    4: '4 - Great / Highly Recommended',
    5: '5 - Pure Masterpiece! Must Play!'
  };

  function setReviewTarget(isApp) {
    isWritingAppReview = isApp;
    const titleSub = document.getElementById('write-review-game-title');
    if (isApp) {
      targetAppBtn?.classList.add('active');
      targetGameBtn?.classList.remove('active');
      if (titleSub) titleSub.textContent = '⭐ LootDrop App (V3.3.0)';
    } else {
      targetGameBtn?.classList.add('active');
      targetAppBtn?.classList.remove('active');
      if (titleSub) titleSub.textContent = selectedGiveaway?.title || 'Giveaway';
    }
  }

  targetGameBtn?.addEventListener('click', () => setReviewTarget(false));
  targetAppBtn?.addEventListener('click', () => setReviewTarget(true));

  // Open from game detail dialog
  openBtn?.addEventListener('click', () => {
    if (!selectedGiveaway) return;
    setReviewTarget(false);
    document.getElementById('review-input-author').value = userProfile.name || 'Gamer';
    document.getElementById('review-input-title').value = '';
    document.getElementById('review-input-comment').value = '';
    currentReviewRating = 5;
    updateStarUI();
    modal?.classList.add('open');
  });

  // Open directly from Settings
  rateAppSettingBtn?.addEventListener('click', () => {
    setReviewTarget(true);
    document.getElementById('review-input-author').value = userProfile.name || 'Gamer';
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
    viewAppReviewsBtn.textContent = isHidden ? '▲ Hide Reviews' : `💬 View All Reviews (${getAppReviews().length})`;
    if (isHidden) {
      renderAppReviewsPreview();
    }
  });

  closeBtn?.addEventListener('click', () => {
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
      ratingLabel.textContent = RATING_TEXTS[currentReviewRating] || `${currentReviewRating} Stars`;
    }
  }

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
      showToast('Please enter a review headline or comment!', '⚠️');
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Uploading to Cloudflare R2...';

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
        platform: 'Web Edition V3.3.0',
        timestamp: Date.now(),
        helpfulCount: 0,
        isVerifiedClaimer: true
      };

      await postPublicReviewToR2(newReview);

      submitBtn.disabled = false;
      submitBtn.textContent = '🚀 Post Review to Community';
      modal?.classList.remove('open');

      showToast('Your rating for LootDrop App has been published to R2!', '⭐');
      updateSettingsAppRatingUI();
    } else {
      if (!selectedGiveaway) {
        submitBtn.disabled = false;
        submitBtn.textContent = '🚀 Post Review to Community';
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
      submitBtn.textContent = '🚀 Post Review to Community';
      modal?.classList.remove('open');

      showToast('Your review has been published publicly to R2!', '⭐');
      loadAndRenderReviewsForGame(selectedGiveaway);
      updateSettingsAppRatingUI();
    }
  });

  updateSettingsAppRatingUI();
}

// ==========================================
// Steam Library Sync & DLC Recommender Engine (Web)
// ==========================================
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
  const steamHint = document.getElementById('steam-status-hint');
  if (steamInput && savedSteamId) steamInput.value = savedSteamId;
  if (steamHint) {
    if (steamGamesList.length > 0) {
      steamHint.textContent = `Synced ${steamGamesList.length} Steam games${savedSteamId ? ` (${savedSteamId})` : ''}`;
    } else if (savedSteamId) {
      steamHint.textContent = `Linked Steam ID: ${savedSteamId} (Click Save & Sync to load games)`;
    }
  }

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

async function fetchWithCorsFallback(targetUrl) {
  const candidates = [
    targetUrl,
    `https://api.allorigins.win/raw?url=${encodeURIComponent(targetUrl)}`,
    `https://corsproxy.io/?${encodeURIComponent(targetUrl)}`
  ];
  let lastErr = null;
  for (const url of candidates) {
    try {
      const resp = await fetch(url);
      if (resp && resp.ok) {
        return resp;
      }
    } catch (err) {
      lastErr = err;
    }
  }
  throw lastErr || new Error('Request blocked or network unavailable');
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

async function syncWebSteamLibrary(rawInput) {
  const parsed = parseSteamIdentifier(rawInput);
  if (!parsed.value) {
    showToast('Please enter a valid Steam 64 ID or Custom URL', '⚠️');
    return;
  }

  localStorage.setItem(STORAGE_KEYS.STEAM_ID, rawInput.trim());
  const hint = document.getElementById('steam-status-hint');
  if (hint) hint.textContent = `Syncing public Steam library for "${parsed.value}"...`;
  showToast('Connecting to Steam profile...', '🔄');

  const xmlUrl = parsed.type === 'steam64'
    ? `https://steamcommunity.com/profiles/${encodeURIComponent(parsed.value)}/games?tab=all&xml=1`
    : `https://steamcommunity.com/id/${encodeURIComponent(parsed.value)}/games?tab=all&xml=1`;

  try {
    const resp = await fetchWithCorsFallback(xmlUrl);
    const xmlText = await resp.text();
    const doc = new DOMParser().parseFromString(xmlText, 'text/xml');
    const errorNode = doc.querySelector('response > error');
    if (errorNode) {
      throw new Error(errorNode.textContent || 'Steam profile private or not found');
    }

    const gameNodes = Array.from(doc.querySelectorAll('games > game'));
    if (gameNodes.length === 0) {
      throw new Error('No games returned. Ensure Steam Profile & Game Details are Public.');
    }

    const parsedGames = gameNodes.map(node => {
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

    steamGamesList = parsedGames;
    localStorage.setItem(STORAGE_KEYS.STEAM_GAMES, JSON.stringify(steamGamesList));

    if (hint) {
      hint.textContent = `Synced ${steamGamesList.length} Steam games! Scanning DLCs next...`;
    }
    showToast(`Synced ${steamGamesList.length} Steam games! Scanning DLCs...`, '🎮');

    updateDlcBadges();
    renderVault();
    renderHomeDlcSection();
    await refreshWebSteamDlcRecommendations(true);

    if (hint) {
      hint.textContent = `Synced ${steamGamesList.length} Steam games • ${getCombinedDlcRecommendations().length} DLC picks ready`;
    }
  } catch (err) {
    console.warn('Steam sync error:', err);
    if (hint) hint.textContent = `Sync failed: ${err.message}`;
    showToast(`Steam sync failed: ${err.message}`, '⚠️');
  }
}

function isDlcCacheFresh() {
  try {
    const cache = JSON.parse(localStorage.getItem(STORAGE_KEYS.DLC_CACHE) || '{}');
    return cache.timestamp && (Date.now() - cache.timestamp) < DLC_CACHE_TTL_MS && Array.isArray(cache.items) && cache.items.length > 0;
  } catch (_) {
    return false;
  }
}

function saveDlcCache(items) {
  localStorage.setItem(STORAGE_KEYS.DLC_CACHE, JSON.stringify({
    timestamp: Date.now(),
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
      priceFormatted: 'FREE',
      initialPriceFormatted: g.worth && g.worth !== 'N/A' ? g.worth : '',
      recommendationsCount: parseInt(g.users || '0', 10) || 0,
      releaseDate: g.published_date || '',
      reasonBadge: isClaimed ? '✓ Claimed Free DLC' : '🎁 Free DLC Drop',
      recommendationReason: `100% Free giveaway for ${parent.name} (${parent.playtimeMinutes > 0 ? `${playtimeHrs}h played` : 'Owned in Steam'})`,
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
  const url = `https://store.steampowered.com/api/appdetails?appids=${appId}&l=english&cc=us`;
  const resp = await fetchWithCorsFallback(url);
  const json = await resp.json();
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
  const priceFormatted = isFree ? 'Free' : (priceObj?.final_formatted || 'View Price');
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
  const playtimeText = (parentGame.playtimeMinutes || 0) > 0 ? `${hours}h played` : 'In Steam Library';

  let reasonBadge = '🧩 DLC Expansion';
  if (isFree) reasonBadge = '🎁 Free DLC';
  else if (discountPercent >= 50) reasonBadge = `🔥 -${discountPercent}% Deal`;
  else if (discountPercent > 0) reasonBadge = `🏷️ -${discountPercent}% Off`;
  else if (isCosmeticOrOst) reasonBadge = '🎵 Soundtrack / Extra';
  else if (recsCount >= 5000) reasonBadge = '⭐ Fan Favorite';
  else if ((parentGame.playtime2WeeksMinutes || 0) > 0) reasonBadge = '🎮 Recently Played';
  else if ((parentGame.playtimeMinutes || 0) >= 1200) reasonBadge = '🏆 Top Game Pick';

  let recommendationReason = `Official expansion for ${parentGame.name} (${playtimeText})`;
  if (isFree) recommendationReason = `Free add-on for ${parentGame.name} (${playtimeText})`;
  else if (discountPercent > 0) recommendationReason = `On sale (-${discountPercent}%) for ${parentGame.name} • ${playtimeText}`;
  else if (recsCount >= 1000) recommendationReason = `Popular expansion for ${parentGame.name} (${playtimeText})`;
  else if ((parentGame.playtimeMinutes || 0) > 0) recommendationReason = `Recommended from your ${playtimeText} in ${parentGame.name}`;

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
  dlcScanStatus = 'Scanning Steam Storefront for DLCs matching your library...';
  renderHomeDlcSection();
  updateDlcHubStatusUI();

  try {
    const candidates = selectCandidateGamesForDlcScanWeb(steamGamesList, 6);
    const ownedIds = new Set(steamGamesList.map(g => g.appId));
    const seenDlcIds = new Set();
    const collected = [];

    for (const game of candidates) {
      dlcScanStatus = `Checking DLCs for ${game.name}...`;
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
      ? `Found ${collected.length} Steam DLC recommendations!`
      : 'Scan complete — no additional store DLCs returned.';
  } catch (err) {
    dlcScanStatus = `Could not finish Steam DLC scan: ${err.message}`;
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
  dlcScanStatus = `Fetching official Steam DLCs for ${parentGame.name}...`;
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
    dlcScanStatus = `Could not fetch DLCs for ${parentGame.name}: ${err.message}`;
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
            <h4>Discover DLCs for Games You Already Own</h4>
            <p>Link your public Steam ID in Settings to automatically spot free DLC drops, expansion sales, and top-rated add-ons for your Steam library.</p>
          </div>
        </div>
        <div>
          <button class="btn btn-sm btn-dlc" id="btn-home-dlc-link-steam">🎮 Link Steam Library ➔</button>
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
        <span>${escapeHtml(dlcScanStatus || 'Scanning Steam Storefront for DLCs matching your library...')}</span>
      </div>
    `;
    return;
  }

  if (recs.length === 0) {
    container.innerHTML = `
      <div class="dlc-link-steam-card">
        <div class="dlc-link-steam-top">
          <div class="dlc-link-icon">✨</div>
          <div class="dlc-link-copy">
            <h4>${steamGamesList.length} Steam Games Synced</h4>
            <p>${escapeHtml(dlcScanStatus || 'Click Scan Now to check the Steam Store for expansions, deals, and free DLC drops for games you own.')}</p>
          </div>
        </div>
        <div>
          <button class="btn btn-sm btn-dlc" id="btn-home-dlc-empty-scan">🔄 Scan Library for DLCs</button>
        </div>
      </div>
    `;
    document.getElementById('btn-home-dlc-empty-scan')?.addEventListener('click', () => refreshWebSteamDlcRecommendations(true));
    return;
  }

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
              <img src="${dlc.headerImageUrl}" onerror="this.onerror=null;this.src='https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/${dlc.parentAppId}/header.jpg';" alt="${escapeHtml(dlc.name)}" class="dlc-thumb-img" loading="lazy">
              <div class="dlc-thumb-scrim"></div>
              <span class="dlc-reason-badge ${badgeCls}">${escapeHtml(dlc.reasonBadge)}</span>
              <button class="dlc-dismiss-btn" data-dismiss-dlcid="${dlc.dlcAppId}" title="Hide recommendation">✕</button>
              <div class="dlc-parent-pill">For: ${escapeHtml(parentLabel)}</div>
            </div>
            <div class="dlc-card-body">
              <div class="dlc-card-title">${escapeHtml(dlc.name)}</div>
              <div class="dlc-card-reason">${escapeHtml(dlc.recommendationReason)}</div>
              <div class="dlc-card-footer">
                <div class="dlc-price-group">
                  ${dlc.initialPriceFormatted && dlc.initialPriceFormatted !== dlc.priceFormatted ? `<span class="dlc-price-strike">${escapeHtml(dlc.initialPriceFormatted)}</span>` : ''}
                  <span class="dlc-price-pill ${badgeCls}">${dlc.isFree ? 'FREE' : escapeHtml(dlc.priceFormatted)}</span>
                </div>
                <button class="btn btn-sm ${dlc.isFree ? 'btn-primary' : 'btn-dlc'} open-dlc-action-btn" data-dlcid="${dlc.dlcAppId}">
                  ${dlc.matchedGiveawayId != null ? 'Claim Free' : 'View DLC'}
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
}

function openDlcHubModal(gameAppId = 'all') {
  const modal = document.getElementById('modal-dlc-hub');
  if (!modal) return;

  const gameSelect = document.getElementById('dlc-hub-game-select');
  if (gameSelect) {
    const sorted = [...steamGamesList].sort((a, b) => (b.playtimeMinutes || 0) - (a.playtimeMinutes || 0));
    gameSelect.innerHTML = `<option value="all">All Owned Steam Games (${steamGamesList.length})</option>` +
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
    titleEl.textContent = '🧩 Steam DLC Recommender';
    subEl.textContent = 'Matched to games you already own in Steam';
  } else {
    const game = steamGamesList.find(g => String(g.appId) === String(selectedDlcParentAppId));
    titleEl.textContent = game ? `🧩 DLCs for ${game.name}` : '🧩 Game DLC Explorer';
    subEl.textContent = game ? `${((game.playtimeMinutes || 0) / 60).toFixed(1)}h played on Steam` : 'Official Steam Storefront expansions';
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
    textEl.textContent = dlcScanStatus || 'Fetching DLC metadata from Steam Storefront...';
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
        <h4>${steamGamesList.length === 0 ? 'No Steam Library Synced Yet' : 'No Matching DLC Recommendations'}</h4>
        <p style="font-size:12px; margin-top:6px; margin-bottom:14px;">
          ${steamGamesList.length === 0
            ? 'Link your Steam 64 ID in Settings so LootDrop can recommend DLCs for games you own.'
            : 'Try switching filters or clicking Scan to fetch fresh Steam Storefront DLC deals.'}
        </p>
        ${steamGamesList.length === 0
          ? `<button class="btn btn-sm btn-primary" onclick="document.getElementById('modal-dlc-hub').classList.remove('open'); switchTab('pane-settings');">Link Steam Account</button>`
          : `<button class="btn btn-sm btn-dlc" onclick="document.getElementById('btn-dlc-hub-refresh').click();">🔄 Scan Library Now</button>`
        }
      </div>
    `;
    return;
  }

  listEl.innerHTML = filtered.map(dlc => {
    const badgeCls = dlc.isFree ? 'free' : (dlc.discountPercent > 0 ? 'sale' : '');
    const cardCls = dlc.isFree ? 'is-free' : (dlc.discountPercent > 0 ? 'is-sale' : '');
    const hours = ((dlc.parentPlaytimeMinutes || 0) / 60).toFixed(1);
    const recsText = dlc.recommendationsCount > 0 ? `${dlc.recommendationsCount.toLocaleString()} reviews` : '';

    return `
      <div class="dlc-detail-card ${cardCls}">
        <div class="dlc-thumb-wrap">
          <img src="${dlc.headerImageUrl}" onerror="this.onerror=null;this.src='https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/${dlc.parentAppId}/header.jpg';" alt="${escapeHtml(dlc.name)}" class="dlc-thumb-img" loading="lazy">
          <div class="dlc-thumb-scrim"></div>
          <span class="dlc-reason-badge ${badgeCls}">${escapeHtml(dlc.reasonBadge)}</span>
          <div class="dlc-parent-pill">🎮 ${escapeHtml(dlc.parentGameName)}${dlc.parentPlaytimeMinutes > 0 ? ` • ${hours}h played` : ''}</div>
        </div>
        <div class="dlc-card-body">
          <div class="dlc-card-footer" style="margin-top:0; padding-top:0;">
            <strong style="font-size:15px; color:var(--color-text-main);">${escapeHtml(dlc.name)}</strong>
            <div class="dlc-price-group">
              ${dlc.initialPriceFormatted && dlc.initialPriceFormatted !== dlc.priceFormatted ? `<span class="dlc-price-strike">${escapeHtml(dlc.initialPriceFormatted)}</span>` : ''}
              <span class="dlc-price-pill ${badgeCls}">${dlc.isFree ? 'FREE' : escapeHtml(dlc.priceFormatted)}</span>
            </div>
          </div>
          <div style="font-size:12px; font-weight:700; color:#D8B4FE;">${escapeHtml(dlc.recommendationReason)}</div>
          ${dlc.shortDescription ? `<p class="dlc-detail-desc">${escapeHtml(dlc.shortDescription)}</p>` : ''}
          <div class="dlc-meta-chips">
            ${recsText ? `<span class="dlc-meta-pill">👍 ${recsText}</span>` : ''}
            ${dlc.metacriticScore ? `<span class="dlc-meta-pill" style="background:rgba(16,185,129,0.16); color:#34D399;">MC ${dlc.metacriticScore}</span>` : ''}
            ${dlc.releaseDate ? `<span>${escapeHtml(dlc.releaseDate)}</span>` : ''}
          </div>
          <div class="dlc-detail-actions">
            <button class="btn btn-sm btn-outline hub-dismiss-dlc-btn" data-dlcid="${dlc.dlcAppId}">Hide</button>
            <button class="btn btn-sm ${dlc.isFree ? 'btn-primary' : 'btn-dlc'} hub-open-dlc-btn" data-dlcid="${dlc.dlcAppId}" style="flex:1;">
              ${dlc.matchedGiveawayId != null ? '🎁 Claim Free DLC' : 'Open Steam Store ↗'}
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
}

function dismissDlcWeb(dlcAppId) {
  dismissedDlcIds.add(dlcAppId);
  localStorage.setItem(STORAGE_KEYS.DLC_DISMISSED, JSON.stringify(Array.from(dismissedDlcIds)));
  selectedGameDlcsList = selectedGameDlcsList.filter(d => d.dlcAppId !== dlcAppId);
  updateDlcBadges();
  renderHomeDlcSection();
  renderDlcHubList();
  showToast('DLC recommendation hidden', '👁️');
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

