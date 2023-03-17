package com.adaptionsoft.games.uglytrivia

import com.adaptionsoft.games.uglytrivia.console.IConsole
import com.adaptionsoft.games.uglytrivia.console.SystemConsole
import com.adaptionsoft.games.uglytrivia.errors.MinimalGoldRequiredNotEnoughError
import com.adaptionsoft.games.uglytrivia.errors.PlayersNumberError
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList

data class Player(
    val name: String,
    var purses: Int = 0,
    var place: Int = 0,
    var isInPenaltyBox: Boolean = false,
    var timesGotInPrison: Int = 0
)

class Game(
    private val console: IConsole = SystemConsole(),
    private val shouldReplaceRockByTechno: Boolean = false,
    private val minimalGoldRequired: Int = 6
) {
    private var players = arrayListOf<Player>()

    private var popQuestions = LinkedList<Any>()
    private var scienceQuestions = LinkedList<Any>()
    private var sportsQuestions = LinkedList<Any>()
    private var rockQuestions = LinkedList<Any>()
    private var technoQuestions = LinkedList<Any>()

    private var currentPlayer = 0
    private var isGettingOutOfPenaltyBox: Boolean = false

    private var numberOfQuestions = 0

    enum class CAT {
        POP, SCIENCE, SPORT, ROCK, TECHNO
    }

    init {
        fillQuestions()
    }

    fun add(newPlayers: ArrayList<Player>) {
        players = newPlayers
        players.forEachIndexed { index, player ->
            console.println("${player.name} was added")
            console.println("They are player number ${index + 1}")
        }
        throwIfNotPlayable()
    }

    fun roll(roll: Int) {
        throwIfNotPlayable()
        console.println("${players[currentPlayer].name} is the current player")
        console.println("They have rolled a $roll")

        if (players[currentPlayer].isInPenaltyBox) {
            if (roll % 2 != 0) {
                isGettingOutOfPenaltyBox = true

                players[currentPlayer].isInPenaltyBox = false
                console.println("${players[currentPlayer].name} is getting out of the penalty box")
                players[currentPlayer].place = players[currentPlayer].place + roll
                if (players[currentPlayer].place > 11) players[currentPlayer].place = players[currentPlayer].place - 12

                console.println(players[currentPlayer].name
                        + "'s new location is "
                        + players[currentPlayer].place)
                console.println("The category is " + currentCategory())
                askQuestion()
            } else {
                console.println("${players[currentPlayer].name} is not getting out of the penalty box")
                isGettingOutOfPenaltyBox = false
            }
        } else {
            players[currentPlayer].place = players[currentPlayer].place + roll
            if (players[currentPlayer].place > 11) players[currentPlayer].place = players[currentPlayer].place - 12

            console.println(players[currentPlayer].name
                    + "'s new location is "
                    + players[currentPlayer].place)
            console.println("The category is " + currentCategory())
            askQuestion()
        }

    }

    fun wasCorrectlyAnswered(): Boolean {
        if (players[currentPlayer].isInPenaltyBox) {
            if (isGettingOutOfPenaltyBox) {
                console.println("Answer was correct!!!!")
                players[currentPlayer].purses++
                console.println(players[currentPlayer].name
                        + " now has "
                        + players[currentPlayer].purses
                        + " Gold Coins.")

                val winner = didPlayerNotWin()
                currentPlayer++
                if (currentPlayer == players.size) currentPlayer = 0

                return winner
            } else {
                currentPlayer++
                if (currentPlayer == players.size) currentPlayer = 0
                return true
            }
        } else {
            console.println("Answer was corrent!!!!")
            players[currentPlayer].purses++
            console.println(players[currentPlayer].name
                    + " now has "
                    + players[currentPlayer].purses
                    + " Gold Coins.")

            val winner = didPlayerNotWin()
            currentPlayer++
            if (currentPlayer == players.size) currentPlayer = 0

            return winner
        }
    }

    fun wrongAnswer(): Boolean {
        console.println("Question was incorrectly answered")
        console.println(players[currentPlayer].toString() + " was sent to the penalty box")
        players[currentPlayer].isInPenaltyBox = true
        players[currentPlayer].timesGotInPrison++

        currentPlayer++
        if (currentPlayer == players.size) currentPlayer = 0
        return true
    }

    private fun askQuestion() {
        try {
            when (currentCategory()) {
                CAT.POP -> console.println(popQuestions.removeFirst().toString())
                CAT.SCIENCE -> console.println(scienceQuestions.removeFirst().toString())
                CAT.SPORT -> console.println(sportsQuestions.removeFirst().toString())
                CAT.ROCK -> console.println(rockQuestions.removeFirst().toString())
                CAT.TECHNO -> console.println(technoQuestions.removeFirst().toString())
            }
        } catch (exception: NoSuchElementException) {
            fillQuestions()
            askQuestion()
        }
    }

    private fun currentCategory(): CAT {
        return when (players[currentPlayer].place) {
            0, 4, 8 -> CAT.POP
            1, 5, 9 -> CAT.SCIENCE
            2, 6, 10 -> CAT.SPORT
            else -> if (shouldReplaceRockByTechno) CAT.TECHNO else CAT.ROCK
        }
    }

    private fun throwIfNotPlayable() {
        if (players.size !in 2..6) throw PlayersNumberError()
        if (minimalGoldRequired < 6) throw MinimalGoldRequiredNotEnoughError()
    }

    private fun didPlayerNotWin(): Boolean = players[currentPlayer].purses < minimalGoldRequired

    private fun fillQuestions() {
        for (i in numberOfQuestions..numberOfQuestions + 49) {
            popQuestions.addLast("Pop Question $i")
            scienceQuestions.addLast("Science Question $i")
            sportsQuestions.addLast("Sports Question $i")
            if (shouldReplaceRockByTechno) {
                technoQuestions.addLast("Techno Question $i")
            } else {
                rockQuestions.addLast("Rock Question $i")
            }
        }
        numberOfQuestions += 49
    }
}