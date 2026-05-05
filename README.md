# CS501 E1 — Wanderly

Wanderly is a personal travel assistant for Android. It uses your live location to surface highly-rated places nearby, builds a balanced day-by-day itinerary tailored to your interests and budget, and gets you there with on-screen routes and a one-tap handoff to Google Maps. Trips, accounts, and preferences are all stored on-device.

## Core Features

- **First-launch onboarding** — illustrated 3-page intro that introduces the app's main features
- **Local accounts** — sign up and log in with username + password; sessions persist across launches
- **GPS & location** via FusedLocationProvider, with permission handling and graceful "Locating…" fallbacks
- **Functional Google Map** with stylized user marker, in-app route polylines, place markers, search-on-map, and an "Open in Google Maps" handoff for turn-by-turn nav
- **Home tab** — interesting places near you with photos, ratings, weather, and Wikipedia summaries
- **Itinerary planner** — pick a city, days, budget, transport, and from 10 interest chips (Food, Culture, History, Nightlife, Nature, Adventure, Shopping, Wellness, Family, Coffee). The planner generates a full day-by-day plan with meals and activities, costs, and timings
- **Saved trips** — persisted in Room, **scoped per user** so different accounts on the same device see only their own trips
- **Profile** — real account info, member-since date, current location, saved-trip count, and sign-out flow

## Setup

Wanderly needs two API keys to run with full functionality:

- **Google Maps key** — with **Maps SDK for Android**, **Directions API**, **Places API (New)**, and the legacy **Places API** all enabled in the Cloud project. Same key for all four.
- **OpenWeatherMap key** — newly created keys take up to 2 hours to activate.

Both are read from `local.properties` at the project root. That file is gitignored, so each developer keeps their own keys locally.

To set up:

1. Copy `local.properties.example` to `local.properties` (or, if Android Studio already generated `local.properties` with the SDK path, just append the two API key lines from the example).
2. Paste your keys into the empty values.
3. **File → Sync Project with Gradle Files**, then **Run**.

If a key is missing, the app degrades gracefully — the Map tab won't render, weather will show "Unavailable", and the itinerary will fall back to a curated NYC pool with a banner.

## Architecture

Wanderly follows MVVM with a clean repository layer:

- UI is composed of stateless Compose screens that read state via `StateFlow`
- ViewModels own all business logic and state. They never touch Retrofit or Room directly
- Repositories abstract data sources — same call site whether data comes from cache (Room) or network (Retrofit)
- Coroutines are scoped to `viewModelScope`; failures bubble up as sealed result types or `null` so the UI can render fallbacks instead of crashing

A representative slice:

```
WeatherCard ── collects ─► WeatherViewModel ── reads ─► WeatherRepository
                                                          │
                                                          ├── Retrofit (OpenWeather)
                                                          └── Room (PlaceEntity cache)
```

The auth flow follows the same pattern: `LoginScreen` ⇆ `AuthViewModel` ⇆ `AuthRepository` ⇆ `UserDao`.

## Database & Schema

Room is the single source of truth for on-device data. Three tables, schema version 5:

### `places` (PlaceEntity)
- `key` — composite of LatLng + query type (PRIMARY KEY)
- `json` — serialized Place response from Google's API
- `timestamp` — used to expire cached entries older than 24 h

### `saved_trips` (SavedTripEntity)
- `id` — auto-increment primary key
- `userId` — owning account; **indexed**, used as a filter in every read/write
- `city`, `days`, `stopCount`, `totalCost`, `transportMode`, `savedAt` — display summary
- `payloadJson` — full serialized itinerary (setup + day-by-day plan)

### `users` (UserEntity)
- `id` — auto-increment primary key
- `username` — **unique index**, lowercased on write
- `displayName`
- `passwordHash` + `passwordSalt` — base64-encoded; see *Authentication & Privacy*
- `createdAt`

Schema migrations are real (not destructive): `MIGRATION_3_4` adds the users table; `MIGRATION_4_5` adds `userId` to `saved_trips` and back-fills any pre-auth rows with the first registered user, so legacy data isn't lost on upgrade.

## APIs

| API | Purpose |
|-----|---------|
| **OpenWeather** | Current weather at the user's location |
| **Google Maps SDK for Android** | Embedded map, markers, polylines |
| **Google Places API (legacy)** | Home recommendations + photos (the photo endpoint is legacy-only) |
| **Google Places API (New)** | Itinerary generation — uses fine-grained `includedTypes` filters that aren't available on the legacy API |
| **Google Directions API** | Encoded polyline that we decode and draw on the map |
| **Wikipedia (Geosearch + Summary)** | Background information for tapped places |

## Sensors

- **GPS / Location services** via `FusedLocationProviderClient`. Updates are coalesced to ~2-5 second intervals and cached so background tab switches don't burn battery. Both `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` are requested at runtime, with a degraded-state path if the user denies permission.

## Authentication & Privacy

Accounts live entirely on-device. There is no backend.

- **Hashing.** Passwords are hashed with `PBKDF2WithHmacSHA256`, 120 000 iterations, 256-bit derived key, with a fresh 16-byte `SecureRandom` salt per user. Verification uses `MessageDigest.isEqual` for constant-time comparison so login does not leak timing information.
- **Storage.** Only the Base64-encoded hash and salt are persisted; raw passwords are wiped from the `PBEKeySpec` immediately after hashing.
- **Sessions.** A small `SharedPreferences` file (`wanderly_session`) stores the current `userId` and an onboarding-seen flag. Nothing sensitive is stored there.
- **Per-user data isolation.** Every read and write to `saved_trips` is filtered by `userId`. `get(id, userId)` and `delete(id, userId)` both require a user ID match, so even a buggy load-by-id can't reach across accounts.

## Team Responsibilities & Contributions

Work was divided by feature area, with explicit handoff seams (e.g. the Map tab takes three independent focus states, one per feature owner).

### Aidan Xu
- Home screen + map screen architecture
- Wikipedia, Places (legacy), Directions, OpenWeather wiring
- Room scaffolding, MVVM patterns, and `secrets-gradle-plugin` for API key injection
- Map tab integration: in-app route polylines, search-on-map, place detail sheet, stylized user marker
- Geocoding helpers and home-screen recommendations

### Shrey Devkar
- Itinerary planner (TripSetup → ItineraryScreen, day-by-day rendering, multi-stop deeplinks) and User Profile
- Places API (New) integration with granular type filters
- `SavedTrip` Room entity + repository + ViewModel
- Local authentication: PBKDF2 password hashing, `AuthRepository` with sealed `AuthResult`, `AuthViewModel`, Login/Signup screens
- First-launch onboarding pager with Material 3 illustrated panels
- Per-user data isolation (Room v4 → v5 migration with orphan-claim) and Profile redesign

## Debugging & Testing

The bulk of testing was on the Android Emulator with LogCat as the primary signal. We exercised:

- Permission flows (deny → re-prompt → grant)
- Configuration changes (rotation, dark/light theme, font scale) to verify state survives via `rememberSaveable` and StateFlow
- Network failure modes (key missing, API quota, no network) to confirm the graceful-fallback paths actually render
- DB upgrade paths (each migration was tested by installing the prior schema, then upgrading, and asserting no data loss)

LLM tools (Gemini in the IDE, Claude, and ChatGPT) helped trace LogCat output, propose hypotheses, and rewrite specific composables. Every AI suggestion was reviewed against the existing patterns before merging.

## AI Disclosure & Reflection

Across the semester we used Gemini (in the Android Studio companion), Claude, and ChatGPT to accelerate development. Every AI suggestion was reviewed before being accepted, and several were explicitly rejected (see below). The work and architectural decisions are ours; AI was a productivity multiplier, not a designer.

### Where AI was used

- **Architecture.** AI helped articulate the MVVM + Repository + StateFlow split early, especially the rule that ViewModels don't import Retrofit or Room directly. That rule held up across the whole codebase.
- **Code.** Boilerplate-heavy work — Compose layouts, Retrofit interfaces, Room DAO methods, ViewModelScope coroutine launches — was AI-assisted. Specific examples:
  - The `AuthScaffold` shared composable was iterated with AI to keep Login and Signup visually identical without copy-paste
  - The `flatMapLatest` pattern in `SavedTripsViewModel` (for re-subscribing to Room on user change) was AI-suggested when we ran into a race condition between auth state and trip queries
  - The PBKDF2 `PBEKeySpec` + `SecretKeyFactory` snippet came from AI; we audited it against Android docs before merging
- **Testing.** AI helped reproduce edge cases (e.g. "what does the app do if the user denies location permission and immediately rotates?") and write the migration test cases.
- **UX.** Material 3 specifics — gradient brushes, surface alpha layering, animated indicator widths — were largely AI-driven first drafts that we then trimmed for taste.

### What AI accelerated

- Material 3 UI scaffolding (cards, lists, chips, dialogs) — easily a 3-4× speedup on screens like Profile and the auth/onboarding flows
- API result data classes with Gson deserialization — tedious by hand, near-instant via AI
- Migration SQL boilerplate — `ALTER TABLE`, index naming conventions, and the `COALESCE` trick for orphan claiming
- Cross-referencing deprecation warnings (e.g. `Icons.Outlined.Login` → `Icons.AutoMirrored.Outlined.Login`) and applying fixes in bulk

### Suggestions we rejected and why

- **FourSquare Places API.** ChatGPT recommended FourSquare for richer POI data. Rejected — restrictive free tier, more involved auth, and no real advantage over Google Places (New) for our use cases.
- **SHA-256 for password hashing.** Initially proposed by AI as a simple option. Rejected — SHA-256 is too fast and GPU-friendly for password storage. We switched to PBKDF2 with 120k iterations to make brute-force prohibitively slow without adding a third-party dependency.
- **Firebase Authentication.** Suggested for "real" login. Rejected — adds a backend dependency, OAuth setup, and extra build configuration that didn't match our scope (a personal travel assistant with on-device data). Local Room-backed auth fit the existing architecture.
- **Hardcoded demo seed credentials.** Suggested as a way to skip the signup screen during the demo. Rejected — embedding credentials in source is a code-quality smell, and the signup flow itself is a strong demo beat.
- **Mock the database in unit tests.** Rejected on the same principle as our integration tests on real Room: mock divergence has burned us before, and Room's in-memory builder is fast enough to use for free.
- **Excessive comments.** AI tends to over-comment generated code. We removed almost all explanatory comments — well-named identifiers and small functions document themselves; comments should only justify non-obvious decisions (see e.g. the `MIGRATION_4_5` comment).

### Example prompt + review

> Prompt: *"i would like the places in the PlacesCards composable to be sorted in order of distance from the user. please implement this"*
>
> Response: "I have updated PlacesCards.kt to sort the list of places by distance from the user before rendering…"
>
> Follow-up: *"we are now calculating distance twice for this, once when sorting and once for rendering cards, right? is this not inefficient?"*
>
> Response: "You're absolutely right. Calculating the distance twice (once for sorting and once inside each card) is redundant…"

The follow-up is the lesson: AI's first answer is usually fine functionally and often careless about performance. Reviewing as a critical reader (not a typist) is what makes the collaboration useful.
