package com.adaptionsoft.games.trivia

import com.adaptionsoft.games.trivia.runner.GameRunner
import com.adaptionsoft.games.uglytrivia.Game
import com.adaptionsoft.games.uglytrivia.PlayersNumberError
import com.adaptionsoft.games.uglytrivia.console.SpyConsole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class SomeTest {
    // With playGame(console, 5, 5, false, listOf("players1", "players2"))
    private val firstGoldenMaster = "Gatien was addedThey are player number 1Gatien was addedThey are player number 2Gatien is the current playerThey have rolled a 5Gatien's new location is 5The category is SCIENCEScience Question 0Answer was corrent!!!!Gatien now has 1 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 5The category is SCIENCEScience Question 1Answer was corrent!!!!Gatien now has 1 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 10The category is SPORTSports Question 0Answer was corrent!!!!Gatien now has 2 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 10The category is SPORTSports Question 1Answer was corrent!!!!Gatien now has 2 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 3The category is ROCKRock Question 0Answer was corrent!!!!Gatien now has 3 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 3The category is ROCKRock Question 1Answer was corrent!!!!Gatien now has 3 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 8The category is POPPop Question 0Answer was corrent!!!!Gatien now has 4 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 8The category is POPPop Question 1Answer was corrent!!!!Gatien now has 4 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 1The category is SCIENCEScience Question 2Answer was corrent!!!!Gatien now has 5 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 1The category is SCIENCEScience Question 3Answer was corrent!!!!Gatien now has 5 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 6The category is SPORTSports Question 2Answer was corrent!!!!Gatien now has 6 Gold Coins."

    private val goldenMasterRockByTechno = "Gatien was addedThey are player number 1Gatien was addedThey are player number 2Gatien is the current playerThey have rolled a 5Gatien's new location is 5The category is SCIENCEScience Question 0Answer was corrent!!!!Gatien now has 1 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 5The category is SCIENCEScience Question 1Answer was corrent!!!!Gatien now has 1 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 10The category is SPORTSports Question 0Answer was corrent!!!!Gatien now has 2 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 10The category is SPORTSports Question 1Answer was corrent!!!!Gatien now has 2 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 3The category is TECHNOTechno Question 0Answer was corrent!!!!Gatien now has 3 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 3The category is TECHNOTechno Question 1Answer was corrent!!!!Gatien now has 3 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 8The category is POPPop Question 0Answer was corrent!!!!Gatien now has 4 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 8The category is POPPop Question 1Answer was corrent!!!!Gatien now has 4 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 1The category is SCIENCEScience Question 2Answer was corrent!!!!Gatien now has 5 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 1The category is SCIENCEScience Question 3Answer was corrent!!!!Gatien now has 5 Gold Coins.Gatien is the current playerThey have rolled a 5Gatien's new location is 6The category is SPORTSports Question 2Answer was corrent!!!!Gatien now has 6 Gold Coins."

    private fun playGame(console: SpyConsole, roll: Int, secondRoll: Int, shouldReplaceRockByTechno: Boolean, players: List<String>) {
        val game = Game(console, shouldReplaceRockByTechno).apply {
            players.forEach { add(it) }
        }
        do {
            game.roll(roll)
            if (secondRoll == 7) {
                GameRunner.notAWinner = game.wrongAnswer()
            } else {
                GameRunner.notAWinner = game.wasCorrectlyAnswered()
            }
        } while (GameRunner.notAWinner)
    }

    @Test
    fun `game should run if player are 2 or more and 6 or less`() {
        val players = listOf("Gatien", "Gatien", "Gatien")
        assertDoesNotThrow {
            playGame(SpyConsole(), 5, 5, false, players)
        }
    }

    @Test
    fun `game should not run if player are less than 2`() {
        val players = listOf("Gatien")
        assertThrows<PlayersNumberError> {
            playGame(SpyConsole(), 5, 5, false, players)
        }
    }

    @Test
    fun `game should not run if player are more than 6`() {
        val players = listOf("Gatien", "Gatien", "Gatien", "Gatien", "Gatien", "Gatien", "Gatien")
        assertThrows<PlayersNumberError> {
            playGame(SpyConsole(), 5, 5, false, players)
        }
    }

    @Test
    fun `golden master should replace rock by techno`() {
        val console = SpyConsole()
        playGame(console, 5, 5, true, listOf("Gatien", "Gatien"))
        assertEquals(goldenMasterRockByTechno, console.getContent())
    }

    @Test
    fun `golden master`() {
        val console = SpyConsole()
        playGame(console, 5, 5, false, listOf("Gatien", "Gatien"))
        assertEquals(firstGoldenMaster, console.getContent())
    }
}
