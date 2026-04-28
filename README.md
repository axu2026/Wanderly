# CS501 E1 - Wanderly

Wanderly is a travel application that finds your location and provides information and suggestions on places to visit nearby, using user preferences and other information to recommend locations accordingly.

Core features implemented as of now include:
- GPS and Location Service to find user location
- Functional map
- Weather information
- General location information
- Recommended locations
- Location searching

Additional features to implement include:
- Itinerary and planning
- AI integration
- Travel log book
- Map refinements and full integration

Stretch goals that are descoped include:
- User accounts
- Social media aspect

# Architecture

Wanderly is set up with MVVM architecture, where UI composables retrieve state from ViewModels to update automatically via FlowStates. ViewModels separate logic and state from UI and handle communication to data stores via a Repository, which abstracts how data is retrieved. Repositories either get this by an external API call from a Retrofit instance or from local storage via a Room when values are within the cache.

# Debugging and Testing

A great portion of debugging and testing is done via the use of an Android emulator, which outputs logs via LogCat. Any errors within the code will be dispalyed there and can be traced. Agentic tools like Gemini can also assist in debugging by reading files and LogCat.

# AI Disclosure

Agentic tools such as Gemini were used to assist in development of code, and other LLMs like ChatGPT were used for API recommendations and explanations. Gemini was used in debugging as well, as it is able to view Android SDK files and view LogCat to provide bug solutions with greater context. AI saves a great deal of time dealing with menial tasks, such as formatting, rendering logic, and so on. AI is also helpful when UI development skills are not strong, allowing one to produce thoughtful and refined UI easily. AI may give ideas that are innaccurate or out-of-scope, so all AI suggestions are reviewed before being accepted for both codebase changes and ideas.

AI has been used to help develop and debug certains parts of code, such as for:
- Setting up API calls and handling responses
- API recommendations
- UI Design
- ViewModel logic
- Persistent Room storage
- Managing dependencies

## An example of using ChatGPT for an explanation:

Prompt: "i am relying on android Address object for user location. does this object provide enough info to get specific user location info, like a city hall or a chinatown?"

Response: "no—Address alone isn’t enough for what you’re trying to do. The Android Address object (from reverse geocoding) is designed to describe a mailing/location context, not to discover nearby points of interest (POIs) like city halls or neighborhoods such as Chinatown ... To get things like city hall or Chinatown, you need a Places/POI layer: Google Places API orFoursquare Places API ..."

## An example of using Gemini for development (and subsequent review):

Prompt: "i would like the places in the PlacesCards composable to be sorted in order of distance from the user. please implement this"

Response: "I have updated PlacesCards.kt to sort the list of places by distance from the user before rendering..."

Prompt: "we are now calculating distance twice for this, once when sorting and once for rendering cards, right? is this not inefficient?"

Response: "You're absolutely right. Calculating the distance twice (once for sorting and once inside each card) is redundant and slightly inefficient, especially as the list of places grows.
To optimize this, I've updated PlacesCards.kt..."
