package com.adaptionsoft.games.trivia

import com.adaptionsoft.games.trivia.runner.GameRunner
import com.adaptionsoft.games.uglytrivia.Game
import com.adaptionsoft.games.uglytrivia.console.SpyConsole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

data class Player(val name: String)

class SomeTest {
    // With playGame(console, 5, 5, listOf("players1", "players2"))
    private val firstGoldenMaster = "joueur1 was addedThey are player number 1joueur2 was addedThey are player number 2joueur1 is the current playerThey have rolled a 5joueur1's new location is 5The category is ScienceScience Question 0Answer was corrent!!!!joueur1 now has 1 Gold Coins.joueur2 is the current playerThey have rolled a 5joueur2's new location is 5The category is ScienceScience Question 1Answer was corrent!!!!joueur2 now has 1 Gold Coins.joueur1 is the current playerThey have rolled a 5joueur1's new location is 10The category is SportsSports Question 0Answer was corrent!!!!joueur1 now has 2 Gold Coins.joueur2 is the current playerThey have rolled a 5joueur2's new location is 10The category is SportsSports Question 1Answer was corrent!!!!joueur2 now has 2 Gold Coins.joueur1 is the current playerThey have rolled a 5joueur1's new location is 3The category is RockRock Question 0Answer was corrent!!!!joueur1 now has 3 Gold Coins.joueur2 is the current playerThey have rolled a 5joueur2's new location is 3The category is RockRock Question 1Answer was corrent!!!!joueur2 now has 3 Gold Coins.joueur1 is the current playerThey have rolled a 5joueur1's new location is 8The category is PopPop Question 0Answer was corrent!!!!joueur1 now has 4 Gold Coins.joueur2 is the current playerThey have rolled a 5joueur2's new location is 8The category is PopPop Question 1Answer was corrent!!!!joueur2 now has 4 Gold Coins.joueur1 is the current playerThey have rolled a 5joueur1's new location is 1The category is ScienceScience Question 2Answer was corrent!!!!joueur1 now has 5 Gold Coins.joueur2 is the current playerThey have rolled a 5joueur2's new location is 1The category is ScienceScience Question 3Answer was corrent!!!!joueur2 now has 5 Gold Coins.joueur1 is the current playerThey have rolled a 5joueur1's new location is 6The category is SportsSports Question 2Answer was corrent!!!!joueur1 now has 6 Gold Coins."

    private fun playGame(console: SpyConsole, roll: Int, secondRoll: Int, players: List<String>) {
        val game = Game(console).apply {
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
        val players = listOf(Player("Gatien"), Player("Antoine"))
        val game = Game().apply {
            add(players[0].name)
            add(players[1].name)
        }
        assertEquals(game.isPlayable(), true)
    }

    @Test
    fun `game should not run if player are less than 2`() {
        val players = listOf(Player("Gatien"), Player("Antoine"))
        val game = Game().apply {
            add(players[0].name)
        }
        assertEquals(game.isPlayable(), false)
    }

    @Test
    fun `golden master`() {
        val console = SpyConsole()
        playGame(console, 5, 5, listOf("joueur1", "joueur2"))
        assertEquals(firstGoldenMaster, console.getContent())
    }
}
