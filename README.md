# WordGeek - Wordle Clone Android App

## Description

Wordle Clone is an Android app that replicates the popular word-guessing game called Wordle. Users can guess words of various lengths (4, 5, or 6 letters) by selecting letters from an on-screen keyboard. The app provides feedback on the correctness of the user's guesses based on the number of correct letters and positions. It also includes features like dictionary integration, challenge functionality, and voice input for a dynamic and engaging word-guessing experience.


## Features

- Simultaneous Word Guessing: Implemented the feature to guess multiple words simultaneously, with dynamic word size (4, 5, or 6 letters). Users can challenge themselves with multiple words and of different lengths.
- Voice Input Functionality: Integrated voice input functionality, enabling users to guess words using their voice. Users can speak the letters instead of typing them, adding an interactive touch to the game.
- Word Meaning Lookup: Utilized Retrofit to make API calls and fetch word meanings, providing users with word definitions for an enhanced gaming experience. Users can learn the meanings of words they encounter during the game.
- Online Multiplayer: Implemented online multiplayer feature using Firebase Realtime Database, allowing users to play simultaneously with their friends. Users can compete and enjoy the game together in real-time.
- Challenge Friends: Generated shareable links with custom words, allowing users to challenge their friends to guess a specific word. Users can share the link and compete with their friends using their chosen words.


## Tech Stack

- Kotlin: Programming language used for Android app development.
- Android Studio: Integrated Development Environment (IDE) for Android app development.
- MVVM Architecture: Utilizes Model-View-ViewModel architectural pattern to separate concerns and improve code organization.
- Firebase Realtime Database: Cloud-hosted NoSQL database used to store and sync word data.
- Retrofit: Networking library used for making API calls to fetch word definitions.
- Coroutines: Kotlin coroutine framework for handling asynchronous tasks.
- LiveData: Lifecycle-aware observable data holder class used for communication between ViewModel and UI.
- ViewModel: Android Architecture Component used to manage UI-related data and handle user interactions.
- ViewBinding: Feature that allows for type-safe access to UI components in XML layout files.

##Demo


https://github.com/SaurabhYadav388/WordGeek/assets/128916471/b0c13c1c-d3f6-41e5-a75e-3e23f6874247

