# Material 3 Consolidated Filter & Sort Menu

Consolidate all launcher filters, giveaway types, and sort options into a unified Material 3 filter menu accessible from the top bar icon, featuring active filter count indicators and a 1-tap quick clear action.

## User Review & Critical Decisions

> [!IMPORTANT]
> Based on your confirmed preferences:
> - **Top Bar Access**: The top bar filter icon (3 lines) opens a consolidated Material 3 menu containing all filter and sort options, removing the clutter of dual horizontal chip rows from the main feed.
> - **Header Controls**: The top of the filter menu includes an active filter count badge and a "Clear All" button for instant reset.
> - **Material 3 Design System**: All filter sections use Material 3 components (M3 `FilterChip` with leading icons, M3 surface tonal elevations, theme-responsive colors, and rounded corners).

- **Confirmed Decision 1**: Filter menu opened directly from the top bar action button.
- **Confirmed Decision 2**: Launcher, giveaway type, and sorting options all grouped neatly into distinct M3 sections within the menu.
- **Confirmed Decision 3**: Active filter badge displayed on both the top bar button and inside the menu header with a one-tap "Clear" reset.

---

## 1. Overview & Core Concept

Currently, the Discover screen displays two separate horizontal scrolling rows (Launchers and Giveaway Types) plus a separate dropdown menu for Sorting. While functional, they consume significant vertical screen space and fragment the filtering experience.

This update introduces a **Unified Material 3 Filter & Sort Menu**:
- Tapping the filter icon in the top bar opens an elegant M3 bottom sheet / dialog menu.
- The menu consolidates **Sort By** (Newest, Highest Value, Most Claimed, Ending Soon), **Store Launchers** (All, Steam, Epic, GOG, Ubisoft, itch.io, Prime, EA), and **Giveaway Types** (All Loot, Full Games, DLC, Beta).
- At the top of the menu, users see the exact count of active filters and a **"Clear All"** button.
- The top bar filter button features a dynamic badge showing active filter count so users always know when a filter is applied.
- The main Discover feed becomes clean and expansive, focusing directly on the giveaways.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Viewing Feed**: The main feed now has a clean layout showing the App Header, Stats Banner (Discover) or Savings Banner (In Library), and the game cards.
2. **Opening Filters**: User taps the filter icon in the top bar. If any non-default filters are active, a glowing accent badge displays the number of active filters.
3. **Interacting with Menu**:
   - **Header**: Displays "Filter & Sort", an active filter count pill (e.g. `2 Active`), and a `Clear All` button (disabled if all defaults).
   - **Sort Section**: M3 single-select chips/pills for `Newest First`, `Highest Value ($)`, `Most Claimed`, and `Ending Soon` with checkmark indicators.
   - **Launchers Section**: M3 FilterChips with launcher color indicators and live giveaway counters (e.g. `Steam (8)`, `Epic Games (3)`).
   - **Giveaway Type Section**: M3 FilterChips for `All Loot`, `Full Games Only`, `DLC & Loot`, `Beta / Early Access`.
   - **Bottom CTA**: Full-width button: `Apply & View (X Freebies)` that closes the sheet and applies selections instantly.
4. **Quick Reset**: Tapping `Clear All` resets launcher to All, type to All, and sort to Newest.

### Visual Identity & Theme
- Built strictly with Material 3 tokens referencing `MaterialTheme.colorScheme` and `MaterialTheme.typography`.
- Menu container uses `MaterialTheme.colorScheme.surface` with subtle outline border `MaterialTheme.colorScheme.outline`.
- Filter chips use M3 `FilterChipDefaults` with selected colors matching the active `accentTheme` (Cyber Cyan, Steam Blue, Ultraviolet, Toxic Lime, etc.).

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Bottom Sheet vs Dropdown Menu**
  - *Chosen Approach*: Material 3 `ModalBottomSheet` for phone ergonomics.
  - *Why*: A bottom sheet provides spacious touch targets (min 48dp), allows organizing 8+ launchers and multiple filter groups cleanly without cramped dropdown clipping, and is easy to reach with one hand.
  - *Alternatives Considered*: Traditional `DropdownMenu` is too narrow for three multi-item filter categories.

- **Decision 2: Active Filter Visibility on Main Screen**
  - *Chosen Approach*: When filters are active, display a compact chip below the stats banner with "Active Filters: [Summary] • Clear", plus a badge on the top bar icon.
  - *Why*: Prevents user confusion where they might forget a filter was active and wonder why certain games aren't appearing.

---

## 4. Technical Architecture & Data Strategy

### System Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                       Top App Bar                           │
│  [Logo LootDrop]                  [Search] [Filter Badge ≡] │
└──────────────────────────────────────┬──────────────────────┘
                                       │ Opens
                                       ▼
┌─────────────────────────────────────────────────────────────┐
│           Material 3 Filter & Sort Bottom Sheet             │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Header: "Filter & Sort"   [ 2 Active ]   [ Clear All ]  │ │
│ └─────────────────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Section 1: Sort By                                      │ │
│ │ [✓ Newest First]  [Highest Value]  [Most Claimed] ...   │ │
│ └─────────────────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Section 2: Store Launcher                               │ │
│ │ [✓ All (24)]  [Steam (8)]  [Epic (3)]  [GOG (2)] ...    │ │
│ └─────────────────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Section 3: Giveaway Type                                │ │
│ │ [✓ All Loot]  [Full Games Only]  [DLC & Loot] ...       │ │
│ └─────────────────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ [ Apply & View 24 Giveaways ]                           │ │
│ └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow Update
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    GiveawaysViewModel                       │
│  • selectedLauncher / selectedTypeFilter / selectedSort     │
│  • activeFilterCount: StateFlow<Int>                        │
│  • resetFilters()                                           │
└──────────────────────────────┬──────────────────────────────┘
                               │ Updates
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                     Giveaways List Feed                     │
└─────────────────────────────────────────────────────────────┘
```

### Component & State Mapping

1. `GiveawaysViewModel`:
   - Compute `activeFilterCount`: count of non-default filters (e.g. `selectedLauncher != LauncherType.ALL`, `selectedTypeFilter != TypeFilter.ALL`, `selectedSort != SortOption.NEWEST`).
   - Add `resetFilters()`: resets launcher to `ALL`, type to `ALL`, and sort to `NEWEST`.
   - Expose `isFilterSheetOpen: MutableStateFlow<Boolean>`.
2. `ui/components/FilterSortBottomSheet.kt`:
   - New dedicated M3 composable component containing the header, active count pill, clear button, and M3 filter chips for sorting, launchers, and types.
3. `ui/HomeScreen.kt`:
   - Replace top bar sort dropdown and separate horizontal filter chip rows with the new M3 filter sheet trigger and active filter badge.
