package com.andreromano.invaders

import kotlin.math.roundToInt

enum class Level(
    val gameboard: String,
    val waves: List<Wave>,
    val startingMoney: Int,
) {

    ONE(
        gameboard = """
            ###########
            #o        #
            #□□□□□□#□ #
            #         #
            # □□#□□□□##
            #         #
            #□□□□□□## #
            #         #
            # □□#□□□□##
            #        x#
            ###########
        """.trimIndent(),
        waves = (0..200).map { number -> TEST_WAVE(1 + (number / 1f)) },
        startingMoney = 200,
    ),

    TWO(
        gameboard = """
            ###########
            #o        #
            #□□□□□□## #
            #   □   □ #
            # □ # □ □ #
            # □ □ □ □ #
            # □ □ □ # #
            # □   □   #
            # □□#□□□□##
            #        x#
            ###########
        """.trimIndent(),
        waves = (0..2).map { number -> TEST_WAVE(1 + (number / 1f)) },
        startingMoney = 200,
    ),
}

private fun TEST_WAVE(scale: Float) = Wave(
    enemyHealth = (100 * scale).roundToInt(),
    enemySpeed = 3f,
    enemyCount = 50,
    enemySpawnDelay = 150,
    enemyMoney = (10 * scale).roundToInt(),
)

data class Wave(
    val enemyHealth: Int,
    val enemySpeed: Float,
    val enemyCount: Int,
    val enemySpawnDelay: Int,
    val enemyMoney: Int,
)