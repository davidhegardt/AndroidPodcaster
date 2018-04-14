# AndroidPodcaster

Fully working Podcast application for Android. SQLite database with full CRUD functionality.
App is fully translated to Swedish, users are ablo to :
- Listen to Top Swe and Eng podcasts which are pre-loaded into the app
- Search podcasts and add to a personal lists
- Stream or download episodes from any podcasts
- App works offline for downloaded episodes
- Tracking progress, app remembers which episodes have been opened and tracks progress
- Parses the latest episodes (10 days, can be changed in app) of all added podcasts
- Browse categories from all countries and add any podcast
- Streaming radioplayer built in
- Uses background AudioService - users can close app and continue to listen
- Custom built notification Icon to see current playing episodes and users can easily get back to app from the menu
- No registration and no personal information is needed, SD card required to download episodes
- Touch sensor support, users can swipe from the Episodes menu to easily navigate in the app

Technology :
Custom built RSS and XML parsers, can handle any inconsitency in RSS-feeds
AudioPlayer is built as a background service, uses can close app and have audio running in the background.
Interruptions for an incoming call pauses audio, if headphones are disconnected the audio is automatically paused.
Uses fragments for navigation and RecyclerView for lists, to handle dynamic loading of content in lists.
Uses background threads for all heavy operations (downloading, parsing etc)
