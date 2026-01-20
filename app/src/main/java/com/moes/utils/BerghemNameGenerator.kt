package com.moes.utils

import kotlin.random.Random

object BerghemNameGenerator {

    private val names = listOf(
        "Bepi",
        "Bortol",
        "Giopì",
        "Bertoldo",
        "Giacomì",
        "Tone",
        "Cecco",
        "Margì",
        "Marièt",
        "Liseta",
        "Teresina",
        "Gina"
    )

    private val surnames = listOf(
        "Pota",
        "MolaMia",
        "Desfès",
        "Casoncello",
        "Polenta",
        "Scarpinocc",
        "Strinù",
        "del Brembo",
        "del Sère",
        "de la Al"
    )

    fun generate(): Pair<String, String> {
        val randomName = names[Random.nextInt(names.size)]
        val randomSurname = surnames[Random.nextInt(surnames.size)]
        return randomName to randomSurname
    }
}