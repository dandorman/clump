# CLAUDE.md

This document provides guidance for AI assistants working with the Clump codebase.

## Project Overview

Clump is a card game web application built with ClojureScript and React. It's a "Set"-style game where players find valid combinations ("clumps") of three cards based on matching rules. The app is a static single-page application (SPA) deployed to GitHub Pages.

## Technology Stack

- **Language**: ClojureScript (Clojure 1.12.4)
- **Build Tool**: Shadow CLJS 3.3.5
- **UI Framework**: React 18.2.0 via Reagent 2.0.1
- **Runtime**: Browser (pure frontend, no backend)
- **Deployment**: GitHub Pages via GitHub Actions

## Project Structure

```
src/clump/
├── core.cljs        # Main entry point, React UI components, game state atoms
├── game.cljs        # Game state management, deck generation, card selection
├── rules.cljs       # Card combination validation (clump? predicate)
├── utils.cljs       # Combinatorics utilities (combinations, map-combinations)
└── ui/utils.cljs    # UI helpers (alert, class-names)

public/
├── index.html       # HTML entry point with #app mount
├── styles.css       # CSS with dark theme, 3D animations, responsive layout
└── js/              # Compiled output (gitignored)

.github/workflows/
└── deploy.yml       # CI/CD: build and deploy to GitHub Pages on push to main
```

## Development Commands

```bash
# Install dependencies
npm ci

# Start development server with hot reload (http://localhost:8080)
npm run dev

# Start ClojureScript REPL
npm run repl

# Build optimized production release
npm run release
```

## Build System Details

- **Dev server**: Port 8080, serves `public/` directory
- **nREPL**: Port 9000 for REPL integration
- **Hot reload**: Enabled with `clump.core/init` as after-load hook
- **Output**: Compiled to `public/js/main.js`

## Code Architecture

### Game Logic Flow

1. **Card Model** (`game.cljs`): Cards have 4 traits (shape, color, fill, number), each with 3 values = 81 unique cards
2. **Rule Engine** (`rules.cljs`): A valid "clump" requires each trait to be either all-same or all-different across 3 cards
3. **State Management** (`game.cljs`): Pure functions transform game state (deck, board, selected, score)
4. **UI Layer** (`core.cljs`): Reagent components render state, dispatch actions via atoms

### Key Functions

- `clump.rules/clump?` - Validates if 3 cards form a valid combination
- `clump.game/new-game` - Creates initial game state with 12 cards
- `clump.game/select` - Handles card selection and clump validation
- `clump.game/hint` - Finds and highlights a valid clump
- `clump.core/init` - Mounts React app to DOM

### State Atoms (core.cljs)

- `game-state` - Current game (board, deck, selected, score, hint)
- `history` - Stack of previous states for undo functionality

## Game Rules Reference

Cards have these traits:
- **Shape**: circle, square, triangle
- **Color**: red, green, blue
- **Fill**: empty, striped, solid
- **Number**: 1, 2, or 3 shapes

A valid clump: For EACH trait, the 3 cards must be ALL SAME or ALL DIFFERENT.

## Styling Conventions

- Dark theme with CSS variables
- Colors: `--clump-red`, `--clump-green`, `--clump-blue`
- 3D card flip animation (rotateY transform)
- Responsive using viewport units (vw/vh)
- Golden ratio proportions for card sizing

## Testing

No test framework is currently configured. Game logic in `rules.cljs` and `game.cljs` is pure and deterministic, suitable for unit testing if added.

## CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/deploy.yml`):
1. Triggers on push to `main` branch
2. Sets up Java 21, Clojure CLI, Node.js 20
3. Runs `npm ci` and `npm run release`
4. Deploys `public/` to GitHub Pages

## Common Tasks

### Adding a new card trait
1. Add trait values in `game.cljs` trait definitions
2. Update `rules.cljs` to include trait in validation
3. Update SVG rendering in `core.cljs`
4. Add CSS styles in `styles.css`

### Modifying game rules
- Edit `clump.rules/clump?` predicate
- Rules are defined per-trait with `same?` or `all-diff?` checks

### UI changes
- Components are in `core.cljs`
- Use Reagent hiccup syntax: `[:div {:class "name"} content]`
- State changes via `swap!` on atoms

## Notes for AI Assistants

- The README.md is outdated (references old lein workflow) - shadow-cljs is the current build system
- All game logic is pure functional - no side effects in `game.cljs` or `rules.cljs`
- The `init` function is called both on page load and after hot reload
- SVG shapes are rendered inline with dynamic fill opacity for striped/empty/solid variants
- No backend/API - all state is client-side
