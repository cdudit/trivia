package com.adaptionsoft.games.uglytrivia

import com.adaptionsoft.games.uglytrivia.console.IConsole
import com.adaptionsoft.games.uglytrivia.console.SystemConsole
import java.util.*

class Game(
    private val console: IConsole = SystemConsole(),
    private val shouldReplaceRockByTechno: Boolean = false
) {
    internal var players = ArrayList<Any>()
    internal var places = IntArray(6)
    internal var purses = IntArray(6)
    internal var inPenaltyBox = BooleanArray(6)

    internal var popQuestions = LinkedList<Any>()
    internal var scienceQuestions = LinkedList<Any>()
    internal var sportsQuestions = LinkedList<Any>()
    internal var rockQuestions = LinkedList<Any>()

    internal var currentPlayer = 0
    internal var isGettingOutOfPenaltyBox: Boolean = false

    enum class CAT {
        POP, SCIENCE, SPORT, ROCK
    }

    init {
        for (i in 0..49) {
            popQuestions.addLast("Pop Question " + i)
            scienceQuestions.addLast("Science Question " + i)
            sportsQuestions.addLast("Sports Question " + i)
            rockQuestions.addLast(createRockQuestion(i))
        }
    }

    fun createRockQuestion(index: Int): String {
        return "Rock Question " + index
    }

    fun add(playerName: String): Boolean {


        players.add(playerName)
        places[howManyPlayers()] = 0
        purses[howManyPlayers()] = 0
        inPenaltyBox[howManyPlayers()] = false

        console.println(playerName + " was added")
        console.println("They are player number " + players.size)
        return true
    }

    fun howManyPlayers(): Int {
        return players.size
    }

    fun isPlayable(): Boolean = (players.count() in 2..6)

    fun roll(roll: Int) {
        console.println(players[currentPlayer].toString() + " is the current player")
        console.println("They have rolled a " + roll)

        if (inPenaltyBox[currentPlayer]) {
            if (roll % 2 != 0) {
                isGettingOutOfPenaltyBox = true

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

    private fun askQuestion() {
        if (currentCategory() === CAT.POP)
            console.println(popQuestions.removeFirst().toString())
        if (currentCategory() === CAT.SCIENCE)
            console.println(scienceQuestions.removeFirst().toString())
        if (currentCategory() === CAT.SPORT)
            console.println(sportsQuestions.removeFirst().toString())
        if (currentCategory() === CAT.ROCK)
            console.println(rockQuestions.removeFirst().toString())
    }

    private fun currentCategory(): CAT {
        if (places[currentPlayer] == 0) return CAT.POP
        if (places[currentPlayer] == 4) return CAT.POP
        if (places[currentPlayer] == 8) return CAT.POP
        if (places[currentPlayer] == 1) return CAT.SCIENCE
        if (places[currentPlayer] == 5) return CAT.SCIENCE
        if (places[currentPlayer] == 9) return CAT.SCIENCE
        if (places[currentPlayer] == 2) return CAT.SPORT
        if (places[currentPlayer] == 6) return CAT.SPORT
        if (places[currentPlayer] == 10) return CAT.SPORT
        return CAT.ROCK
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

                val winner = didPlayerWin()
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

            val winner = didPlayerWin()
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

    private fun didPlayerWin(): Boolean {
        return purses[currentPlayer] != 6
    }

    fun getPlayers(players: List<Player>): Int{
        return players.count()
    }

    fun isPlayable(playersCount: Int): Boolean{
        return playersCount in 2 .. 6
    }
}