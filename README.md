# CS501 E1 - Wanderly

Wanderly is a travel application that finds your location and provides information and suggestions on places to visit nearby, using user preferences and other information to recommend locations accordingly.

Core features implemented as of now include:
- GPS & Location Service to find user location
- Functional map & Location searching
- Weather information
- Location Summaries & Info
- Recommended locations
- Itinerary & planning

Stretch goals that were descoped:
- User accounts
- Social media aspect
- AI integration
- Travel log book

# Setup

Wanderly needs two API keys to run with full functionality:

- **Google Maps key** — with **Maps SDK for Android**, **Directions API**, **Places API (New)**, and the legacy **Places API** all enabled in the Cloud project. Same key for all four.
- **OpenWeatherMap key** — newly created keys take up to 2 hours to activate.

Both are read from `local.properties` at the project root. That file is gitignored, so each developer keeps their own keys locally.

To set up:

1. Copy `local.properties.example` to `local.properties` (or, if Android Studio already generated `local.properties` with the SDK path, just append the two API key lines from the example).
2. Paste your keys into the empty values.
3. **File → Sync Project with Gradle Files**, then **Run**.

If a key is missing, the app degrades gracefully — the Map tab won't render, weather will show "Unavailable", and the itinerary will fall back to a curated NYC pool with a banner.

# Architecture

Wanderly is set up with MVVM architecture:
- UI is given state via FlowState
- FlowState comes from ViewModels, abstracting business logic from UI
- ViewModels obtain data from an intermediate Repository
- The Repository abstracts where data comes from, either externally (API) or internally (Room)
- External API calls are made by a Retrofit instance, called within the Repository
- If data already exists within the cache, we retrieve it from local Room database

# Database & Schema

Room is used to store data locally on the device, particularly for place information and saved trips. For place information, storing via a Room cache saves API calls and generally makes performance better. Here are the schemas:

## Place
- key: String
- json: String
- timestamp: Long

## SavedTrip
- city: String
- days: Int
- stopCount: Int
- totalCost: Double
- transportMode: String
- savedAt: Long
- payloadJson: String

# APIs & Sensor Usage

APIs used in this app include: 
- OpenWeather API (weather information)
- Google Maps SDK for Android (built-in map)
- Places API/Places API (New) (place recommendations, search, and itinerary)
- Directions API (polyline route for locations)
- Wikipedia API (location background information)

Wanderly leverages GPS and Location Services on the Android device, giving the app the user's location in 2-5 second intervals. This is done with a FusedLocationProviderClient, which gets permission from the user to get their location.

# Team Responsibilities & Contributions

Work was divided by different features, with Aidan focusing on home and map screen features, and Shrey focusing on the itinerary features.

## Aidan Xu
- Home screen and map screen
- Setting up Wikipedia API, Places API, Directions API, and OpenWeather API
- Setting up Room and MVVM architecture
- Location search, recommendations, and information
- Geocoding

## Shrey Devkar
- Itinerary screen and profile screen
- Setting up Places API (New)
- Implemented SaveTrip entity in Room
- Itinerary creation process
- Saving itineraries

# Debugging and Testing

A great portion of debugging and testing is done via the use of an Android emulator, which outputs logs via LogCat. Any errors within the code will be displayed there and can be traced. LogCat also helps trace code execution using log debugging statements. Agentic tools like Gemini can also assist in debugging by reading files and LogCat.

# AI Disclosure

Agentic tools such as Gemini & Claude were used to assist in development of code, and other LLMs like ChatGPT were used for API recommendations and explanations. Gemini was used in debugging as well, as it is able to view Android SDK files and view LogCat to provide bug solutions with greater context. AI saves a great deal of time dealing with menial tasks, such as formatting, rendering logic, and so on. AI is also helpful when UI development skills are not strong, allowing one to produce thoughtful and refined UI easily. Generally, AI accelerated most of the time consuming aspects of UI development and helped debug code much quicker. AI may give ideas that are innaccurate, out-of-scope, or unhelpful, so all AI suggestions are reviewed before being accepted for both codebase changes and ideas. For instance, FourSquare API was recommended by ChatGPT but rejected due to being difficult to set up, having a restrictive plan, and is generally inferior to Places API.

AI has been used to help develop and debug certains parts of code, such as for:
- Setting up API calls and handling responses
- API recommendations
- UI design & styling
- ViewModel logic
- Setting up persistent Room storage
- Managing dependencies

## An example of using ChatGPT for an explanation:

Prompt: "i am relying on android Address object for user location. does this object provide enough info to get specific user location info, like a city hall or a chinatown?"

Response: "no—Address alone isn’t enough for what you’re trying to do. The Android Address object (from reverse geocoding) is designed to describe a mailing/location context, not to discover nearby points of interest (POIs) like city halls or neighborhoods such as Chinatown ... To get things like city hall or Chinatown, you need a Places/POI layer: Google Places API or Foursquare Places API ..."

## An example of using Gemini for development (and subsequent review):

Prompt: "i would like the places in the PlacesCards composable to be sorted in order of distance from the user. please implement this"

Response: "I have updated PlacesCards.kt to sort the list of places by distance from the user before rendering..."

Prompt: "we are now calculating distance twice for this, once when sorting and once for rendering cards, right? is this not inefficient?"

Response: "You're absolutely right. Calculating the distance twice (once for sorting and once inside each card) is redundant and slightly inefficient, especially as the list of places grows. To optimize this, I've updated PlacesCards.kt..."
