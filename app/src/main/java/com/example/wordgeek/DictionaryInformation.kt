package com.example.wordgeek

data class DictionaryEntry(
    val word: String,
    val phonetic: String,
    val phonetics: List<Phonetic>,
    val meanings: List<Meaning>
)

data class Phonetic(
    val text: String,
    val audio: String? // Optional field
)

data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>
)

data class Definition(
    val definition: String,
    val example: String,
    val synonyms: List<String>,
    val antonyms: List<String>
)
