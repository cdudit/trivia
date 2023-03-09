package com.adaptionsoft.games.uglytrivia

import com.adaptionsoft.games.uglytrivia.console.IConsole
import com.adaptionsoft.games.uglytrivia.console.SystemConsole
import com.adaptionsoft.games.uglytrivia.errors.MinimalGoldRequiredNotEnoughError
import com.adaptionsoft.games.uglytrivia.errors.PlayersNumberError
import java.util.*

class Game(
    private val console: IConsole = SystemConsole(),
    private val shouldReplaceRockByTechno: Boolean = false,
    private val minimalGoldRequired: Int = 6
) {
    private var players = ArrayList<Any>()
    private var places = IntArray(6)
    private var purses = IntArray(6)
    private var inPenaltyBox = BooleanArray(6)

    private var popQuestions = LinkedList<Any>()
    private var scienceQuestions = LinkedList<Any>()
    private var sportsQuestions = LinkedList<Any>()
    private var rockQuestions = LinkedList<Any>()
    private var technoQuestions = LinkedList<Any>()

    private var currentPlayer = 0
    private var isGettingOutOfPenaltyBox: Boolean = false

    enum class CAT {
        POP, SCIENCE, SPORT, ROCK, TECHNO
    }

    init {
        for (i in 0..49) {
            popQuestions.addLast("Pop Question $i")
            scienceQuestions.addLast("Science Question $i")
            sportsQuestions.addLast("Sports Question $i")
            if (shouldReplaceRockByTechno) {
                technoQuestions.addLast("Techno Question $i")
            } else {
                rockQuestions.addLast("Rock Question $i")
            }
        }
    }

    fun add(playerName: String): Boolean {
        try {
            players.add(playerName)
            places[howManyPlayers()] = 0
            purses[howManyPlayers()] = 0
            inPenaltyBox[howManyPlayers()] = false
        } catch (err: IndexOutOfBoundsException) {
            throw PlayersNumberError()
        }

        console.println("$playerName was added")
        console.println("They are player number " + players.size)
        return true
    }

    fun roll(roll: Int) {
        throwIfNotPlayable()
        console.println(players[currentPlayer].toString() + " is the current player")
        console.println("They have rolled a $roll")

        if (inPenaltyBox[currentPlayer]) {
            if (roll % 2 != 0) {
                isGettingOutOfPenaltyBox = true

                inPenaltyBox[currentPlayer] = false
                console.println(players[currentPlayer].toString() + " is getting out of the penalty box")
                places[currentPlayer] = places[currentPlayer] + roll
                if (places[currentPlayer] > 11) places[currentPlayer] = places[currentPlayer] - 12

                console.println(players[currentPlayer].toString()
                        + "'s new location is "
                        + places[currentPlayer])
                console.println("The category is " + currentCategory())
                askQuestion()
            } else {
                console.println(players[currentPlayer].toString() + " is not getting out of the penalty box")
                isGettingOutOfPenaltyBox = false
            }
        } else {
            places[currentPlayer] = places[currentPlayer] + roll
            if (places[currentPlayer] > 11) places[currentPlayer] = places[currentPlayer] - 12

            console.println(players[currentPlayer].toString()
                    + "'s new location is "
                    + places[currentPlayer])
            console.println("The category is " + currentCategory())
            askQuestion()
        }

    }

    fun wasCorrectlyAnswered(): Boolean {
        if (inPenaltyBox[currentPlayer]) {
            if (isGettingOutOfPenaltyBox) {
                console.println("Answer was correct!!!!")
                purses[currentPlayer]++
                console.println(players[currentPlayer].toString()
                        + " now has "
                        + purses[currentPlayer]
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
            purses[currentPlayer]++
            console.println(players[currentPlayer].toString()
                    + " now has "
                    + purses[currentPlayer]
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
        inPenaltyBox[currentPlayer] = true

        currentPlayer++
        if (currentPlayer == players.size) currentPlayer = 0
        return true
    }

    private fun askQuestion() {
        when (currentCategory()) {
            CAT.POP -> console.println(popQuestions.removeFirst().toString())
            CAT.SCIENCE -> console.println(scienceQuestions.removeFirst().toString())
            CAT.SPORT -> console.println(sportsQuestions.removeFirst().toString())
            CAT.ROCK -> console.println(rockQuestions.removeFirst().toString())
            CAT.TECHNO -> console.println(technoQuestions.removeFirst().toString())
        }
    }

    private fun currentCategory(): CAT {
        return when (places[currentPlayer]) {
            0, 4, 8 -> CAT.POP
            1, 5, 9 -> CAT.SCIENCE
            2, 6, 10 -> CAT.SPORT
            else -> if (shouldReplaceRockByTechno) CAT.TECHNO else CAT.ROCK
        }
    }

    private fun howManyPlayers(): Int = players.size

    private fun throwIfNotPlayable() {
        if (players.size !in 2..6) throw PlayersNumberError()
        if (minimalGoldRequired < 6) throw MinimalGoldRequiredNotEnoughError()
    }

    private fun didPlayerNotWin(): Boolean = purses[currentPlayer] < minimalGoldRequired
}