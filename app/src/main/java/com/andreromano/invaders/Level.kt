package com.andreromano.invaders

import kotlin.math.roundToInt

enum class Level(
    val gameboard: String,
    val waves: List<Wave>,
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
        waves = (0..20).map { number -> TEST_WAVE(1 + (number / 1f)) },
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
    ),
}

private fun TEST_WAVE(scale: Float) = Wave(
    enemyHealth = (100 * scale).roundToInt(),
    enemySpeed = 3,
    enemyCount = 30,
    enemySpawnDelay = 750,
    enemyMoney = (10 * scale).roundToInt(),
)

data class Wave(
    val enemyHealth: Int,
    val enemySpeed: Int,
    val enemyCount: Int,
    val enemySpawnDelay: Int,
    val enemyMoney: Int,
)