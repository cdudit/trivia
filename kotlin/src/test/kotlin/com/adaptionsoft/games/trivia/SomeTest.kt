package com.adaptionsoft.games.trivia

import com.adaptionsoft.games.trivia.runner.GameRunner
import com.adaptionsoft.games.uglytrivia.Game
import com.adaptionsoft.games.uglytrivia.errors.PlayersNumberError
import com.adaptionsoft.games.uglytrivia.console.SpyConsole
import com.adaptionsoft.games.uglytrivia.errors.MinimalGoldRequiredNotEnoughError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class SomeTest {
    // With playGame(console, 5, 5, false, listOf("players1", "players2"))
    private val firstGoldenMaster = "Gatien was addedThey are player number 1Gatien was addedThey are player number 2Gatien is the current playerThey have rolled a 5Gatien's new location is 5The category is SCIENCEScience Question 0Answer was corrent!!!!Gatien now has 1 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 5The category is SCIENCEScience Question 1Answer was corrent!!!!Gatien now has 1 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 10The category is SPORTSports Question 0Answer was corrent!!!!Gatien now has 2 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 10The category is SPORTSports Question 1Answer was corrent!!!!Gatien now has 2 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 3The category is ROCKRock Question 0Answer was corrent!!!!Gatien now has 3 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 3The category is ROCKRock Question 1Answer was corrent!!!!Gatien now has 3 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 8The category is POPPop Question 0Answer was corrent!!!!Gatien now has 4 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 8The category is POPPop Question 1Answer was corrent!!!!Gatien now has 4 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 1The category is SCIENCEScience Question 2Answer was corrent!!!!Gatien now has 5 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 1The category is SCIENCEScience Question 3Answer was corrent!!!!Gatien now has 5 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 6The category is SPORTSports Question 2Answer was corrent!!!!Gatien now has 6 Gold Coins."

    //region GOLDEN MASTER
    @Test
    fun `golden master`() {
        val console = SpyConsole()
        val players = listOf("Gatien", "Gatien")
        val game = Game(console).apply {
            players.forEach { add(it) }
        }
        GameRunner.runGame(game, false, 5, 5)
        assertEquals(firstGoldenMaster, console.getContent())
    }
    //endregion

    //region PLAYERS NUMBER
    @Test
    fun `game should run if player are 2 or more and 6 or less`() {
        val players = listOf("Gatien", "Gatien")
        val game = Game().apply {
            players.forEach { add(it) }
        }
        assertDoesNotThrow {
            GameRunner.runGame(game, shouldUseRandom = false, firstRoll = 5, secondRoll = 5)
        }
    }

    @Test
    fun `game should not run if player are less than 2`() {
        val players = listOf("Gatien")
        assertThrows<PlayersNumberError> {
            val game = Game().apply {
                players.forEach { add(it) }
            }
            GameRunner.runGame(game, shouldUseRandom = false, firstRoll = 5, secondRoll = 5)
        }
    }

    @Test
    fun `game should not run if player are more than 6`() {
        val players = listOf("Gatien", "Gatien", "Gatien", "Gatien", "Gatien", "Gatien", "Gatien")
        assertThrows<PlayersNumberError> {
            val game = Game().apply {
                players.forEach { add(it) }
            }
            GameRunner.runGame(game, shouldUseRandom = false, firstRoll = 5, secondRoll = 5)
        }
    }
    //endregion

    //region FEATURE REPLACE ROCK BY TECHNO
    @Test
    fun `should replace rock by techno`() {
        val console = SpyConsole()
        val players = listOf("Gatien", "Gatien")
        val game = Game(console, shouldReplaceRockByTechno = true).apply {
            players.forEach { add(it) }
        }
        GameRunner.runGame(game, shouldUseRandom = false, firstRoll = 5, secondRoll = 5)
        assert(console.getContent().contains("The category is TECHNO"))
        assert(!console.getContent().contains("The category is ROCK"))
    }
    //endregion

    //region MINIMAL GOLD REQUIRED
    @Test
    fun `game with less than 6 gold should throw`() {
        val players = listOf("Gatien", "Gatien")
        val game = Game(minimalGoldRequired = 3).apply {
            players.forEach { add(it) }
        }
        assertThrows<MinimalGoldRequiredNotEnoughError> {
            GameRunner.runGame(game)
        }
    }

    @Test
    fun `game with 6 gold should not throw`() {
        val players = listOf("Gatien", "Gatien")
        val game = Game(minimalGoldRequired = 6).apply {
            players.forEach { add(it) }
        }
        assertDoesNotThrow {
            GameRunner.runGame(game)
        }
    }

    @Test
    fun `game more than 6 gold should not throw`() {
        val players = listOf("Gatien", "Gatien")
        val game = Game(minimalGoldRequired = 40).apply {
            players.forEach { add(it) }
        }
        assertDoesNotThrow {
            GameRunner.runGame(game)
        }
    }
    //endregion

    //region BUG STAY IN PRISON
    @Test
    fun `should stay in prison`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            listOf("Arthur", "Gatien").forEach { add(it) }
        }
        GameRunner.runGame(game, shouldUseRandom = false, firstRoll = 2, secondRoll = 7)
        assertEquals(false, console.getContent().contains("Arthur is getting out of the penalty box"))
    }


    @Test
    fun `should quit prison`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            listOf("Arthur", "Gatien").forEach { add(it) }
        }
        GameRunner.runGame(game, shouldUseRandom = false, firstRoll = 7, secondRoll = 7)
        assert(console.getContent().contains("Arthur is getting out of the penalty box"))
    }

    // WIP
    /*@Test
    fun `should quit prison and win game`() {
        val console = SpyConsole()
        playGame(console, 2, 7, false, listOf("Arthur", "Gatien"))
        assertFalse(GameRunner.notAWinner)
    }*/
    //endregion
}
