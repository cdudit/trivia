package com.adaptionsoft.games.uglytrivia

import com.adaptionsoft.games.uglytrivia.console.IConsole
import com.adaptionsoft.games.uglytrivia.console.SystemConsole
import com.adaptionsoft.games.uglytrivia.errors.MinimalGoldRequiredNotEnoughError
import com.adaptionsoft.games.uglytrivia.errors.PlayersNumberError
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList

class Game(
    private val console: IConsole = SystemConsole(),
    private val shouldReplaceRockByTechno: Boolean = false,
    private val minimalGoldRequired: Int = 6,
    private val shouldUseExpansionPack: Boolean = false
) {
    private var players = arrayListOf<Player>()

    private var leaderboard = Leaderboard()

    private var popQuestions = LinkedList<String>()
    private var scienceQuestions = LinkedList<String>()
    private var sportsQuestions = LinkedList<String>()
    private var rockQuestions = LinkedList<String>()
    private var technoQuestions = LinkedList<String>()
    private var rapQuestions = LinkedList<String>()
    private var philoQuestions = LinkedList<String>()
    private var litteratureQuestions = LinkedList<String>()
    private var geoQuestions = LinkedList<String>()

    private var currentPlayerIndex = 0
    private var isGettingOutOfPenaltyBox: Boolean = false

    private var numberOfQuestions = 0

    enum class CAT {
        POP, SCIENCE, SPORT, ROCK, TECHNO, RAP, PHILO, LITTERATURE, GEO
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
        if (players.size == 2) {
            leaderboard.hasThirdPlayer = true
            leaderboard.hasSecondPlayer = true
        } else if (players.size == 3) {
            leaderboard.hasThirdPlayer = true
        }
    }

    fun roll(roll: Int) {
        console.println("${players[currentPlayerIndex].name} is the current player")
        console.println("They have rolled a $roll")

        if (players[currentPlayerIndex].isInPenaltyBox) {
            if (roll % 2 != 0) {
                isGettingOutOfPenaltyBox = true

                players[currentPlayerIndex].isInPenaltyBox = false
                console.println("${players[currentPlayerIndex].name} is getting out of the penalty box")
                players[currentPlayerIndex].place = players[currentPlayerIndex].place + roll
                if (players[currentPlayerIndex].place > 11) players[currentPlayerIndex].place = players[currentPlayerIndex].place - 12

                console.println(players[currentPlayerIndex].name
                        + "'s new location is "
                        + players[currentPlayerIndex].place)
                console.println("The category is " + currentCategory())
                askQuestion()
            } else {
                console.println("${players[currentPlayerIndex].name} is not getting out of the penalty box")
                isGettingOutOfPenaltyBox = false
            }
        } else {
            players[currentPlayerIndex].place = players[currentPlayerIndex].place + roll
            if (players[currentPlayerIndex].place > 11) players[currentPlayerIndex].place = players[currentPlayerIndex].place - 12

            console.println(players[currentPlayerIndex].name
                    + "'s new location is "
                    + players[currentPlayerIndex].place)
            console.println("The category is " + currentCategory())
            askQuestion()
        }

    }

    fun wasCorrectlyAnswered(): Boolean {
        try {
            // Check if player has been removed because doesn't want to answer
            players[currentPlayerIndex]
        } catch (error: IndexOutOfBoundsException) {
            incrementCurrentPlayerIndex()
            return true
        }
        if (players[currentPlayerIndex].wantToUseJoker) {
            incrementCurrentPlayerIndex()
            return true
        }
        if (players[currentPlayerIndex].isInPenaltyBox) {
            if (isGettingOutOfPenaltyBox) {
                console.println("Answer was correct!!!!")
                players[currentPlayerIndex].purses++
                console.println(players[currentPlayerIndex].name
                        + " now has "
                        + players[currentPlayerIndex].purses
                        + " Gold Coins.")

                val winner = didPlayerNotWin()
                incrementCurrentPlayerIndex()

                return winner
            } else {
                incrementCurrentPlayerIndex()
                return true
            }
        } else {
            console.println("Answer was corrent!!!!")
            players[currentPlayerIndex].purses++
            console.println(players[currentPlayerIndex].name
                    + " now has "
                    + players[currentPlayerIndex].purses
                    + " Gold Coins.")

            val winner = didPlayerNotWin()
            currentPlayerIndex++
            if (currentPlayerIndex == players.size) currentPlayerIndex = 0

            return winner
        }
    }

    fun wrongAnswer(): Boolean {
        try {
            // Check if player has been removed because doesn't want to answer
            players[currentPlayerIndex]
        } catch (error: IndexOutOfBoundsException) {
            incrementCurrentPlayerIndex()
            return true
        }
        if (players[currentPlayerIndex].wantToUseJoker) {
            incrementCurrentPlayerIndex()
            return true
        }
        console.println("Question was incorrectly answered")
        console.println(players[currentPlayerIndex].name + " was sent to the penalty box")
        players[currentPlayerIndex].isInPenaltyBox = true
        players[currentPlayerIndex].timesGotInPrison++

        incrementCurrentPlayerIndex()
        return true
    }

    private fun askQuestion() {
        if (players[currentPlayerIndex].wantToUseJoker) {
            console.println("${players[currentPlayerIndex].name} uses his joker")
        } else if (players[currentPlayerIndex].doesNotWantToAnswer) {
            console.println("${players[currentPlayerIndex].name} doesn't want to answer and left the game")
            players.removeAt(currentPlayerIndex)
        } else {
            try {
                when (currentCategory()) {
                    CAT.POP -> console.println(popQuestions.removeFirst().toString())
                    CAT.SCIENCE -> console.println(scienceQuestions.removeFirst().toString())
                    CAT.SPORT -> console.println(sportsQuestions.removeFirst().toString())
                    CAT.ROCK -> console.println(rockQuestions.removeFirst().toString())
                    CAT.TECHNO -> console.println(technoQuestions.removeFirst().toString())
                    else -> {}
                }
            } catch (exception: NoSuchElementException) {
                fillQuestions()
                askQuestion()
            }
        }
    }

    private fun incrementCurrentPlayerIndex() {
        currentPlayerIndex++
        if (currentPlayerIndex >= players.size) {
            currentPlayerIndex = 0
        }
    }

    private fun currentCategory(): CAT {
        if (shouldUseExpansionPack) {
            return when (players[currentPlayerIndex].place) {
                0 -> CAT.POP
                1 -> CAT.SCIENCE
                2, 10 -> CAT.SPORT
                3, 11 -> CAT.RAP
                4, 7 -> CAT.GEO
                8 -> CAT.PHILO
                9 -> CAT.LITTERATURE
                else -> if (shouldReplaceRockByTechno) CAT.TECHNO else CAT.ROCK
            }
        } else {
            return when (players[currentPlayerIndex].place) {
                0, 4, 8 -> CAT.POP
                1, 5, 9 -> CAT.SCIENCE
                2, 6, 10 -> CAT.SPORT
                else -> if (shouldReplaceRockByTechno) CAT.TECHNO else CAT.ROCK
            }
        }
    }

    private fun throwIfNotPlayable() {
        if (players.size !in 2..6) throw PlayersNumberError()
        if (minimalGoldRequired < 6) throw MinimalGoldRequiredNotEnoughError()
    }

    private fun didPlayerNotWin(): Boolean = players[currentPlayerIndex].purses < minimalGoldRequired

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
            if (shouldUseExpansionPack) {
                rapQuestions.addLast("Rap Question $i")
                philoQuestions.addLast("Philo Question $i")
                litteratureQuestions.addLast("Litterature Question $i")
                geoQuestions.addLast("Geo Question $i")
            }
        }
        numberOfQuestions += 49
    }
}