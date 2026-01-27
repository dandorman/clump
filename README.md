# Clump

A card game where players find valid combinations of three cards based on matching rules. Similar to the classic "Set" game.

## Overview

Clump is a single-page web application built with ClojureScript and React (via Reagent). Players are presented with 12 cards and must find "clumps" - valid combinations of 3 cards where each trait (shape, color, fill, number) is either all the same or all different across the cards.

## Setup

Prerequisites:
- Node.js (v20+)
- Java (v21+)

Install dependencies:

```bash
npm ci
```

## Development

Start the development server with hot reload:

```bash
npm run dev
```

This starts a dev server at http://localhost:8080 with automatic recompilation on file changes.

Start a ClojureScript REPL (while dev server is running):

```bash
npm run repl
```

## Production Build

Create an optimized production build:

```bash
npm run release
```

Output is written to `public/js/main.js`.

## Deployment

The app automatically deploys to GitHub Pages when changes are pushed to the `main` branch via GitHub Actions.

## Game Rules

Each card has 4 traits with 3 possible values:
- **Shape**: circle, square, triangle
- **Color**: red, green, blue
- **Fill**: empty, striped, solid
- **Number**: 1, 2, or 3 shapes

A valid **clump** is 3 cards where, for each trait, the values are either:
- All the same (e.g., all red), OR
- All different (e.g., red, green, blue)

## License

Copyright (c) 2026 Dan Dorman. Released under the [MIT License](LICENSE).
