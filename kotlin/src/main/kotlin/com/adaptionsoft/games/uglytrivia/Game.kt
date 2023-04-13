package com.adaptionsoft.games.uglytrivia

import com.adaptionsoft.games.uglytrivia.console.IConsole
import com.adaptionsoft.games.uglytrivia.console.SystemConsole
import com.adaptionsoft.games.uglytrivia.errors.MinimalGoldRequiredNotEnoughError
import com.adaptionsoft.games.uglytrivia.errors.PlayersNumberError
import java.lang.StringBuilder
import java.time.LocalDateTime
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList

class Game(
    private val console: IConsole = SystemConsole(),
    private val shouldReplaceRockByTechno: Boolean = false,
    private val minimalGoldRequired: Int = 6,
    private val shouldUseExpansionPack: Boolean = false,
    private val cellsInJail: Int = 0
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
    private var peopleQuestions = LinkedList<String>()

    private var currentPlayerIndex = 0
    private var isGettingOutOfPenaltyBox: Boolean = false

    private var numberOfQuestions = 0
    private var currentCategory: Category? = null

    val isLeaderboardComplete: Boolean
        get() = leaderboard.isComplete()

    enum class Category {
        POP, SCIENCE, SPORT, ROCK, TECHNO, RAP, PHILO, LITTERATURE, GEO, PEOPLE
    }

    init {
        fillQuestions()
    }

    fun getParameters(): String {
        val builder = StringBuilder()
        builder.append("minimalGoldRequired: $minimalGoldRequired")
        builder.append("shouldUseExpansionPack: $shouldUseExpansionPack")
        builder.append("shouldReplaceRockByTechno: $shouldReplaceRockByTechno")
        builder.append("players: ${players.map { it.name }}")
        return builder.toString()
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

    fun wasCorrectlyAnswered() {
        try {
            // Check if player has been removed because doesn't want to answer
            players[currentPlayerIndex]
        } catch (error: IndexOutOfBoundsException) {
            incrementCurrentPlayerIndex()
            return
        }
        if (players[currentPlayerIndex].hasJoker) {
            players[currentPlayerIndex].hasJoker = false
            incrementCurrentPlayerIndex()
            return
        }
        if (players[currentPlayerIndex].isInPenaltyBox) {
            if (isGettingOutOfPenaltyBox) {
                console.println("Answer was correct!!!!")
                players[currentPlayerIndex].correctAnswerStrike++
                players[currentPlayerIndex].purses += players[currentPlayerIndex].correctAnswerStrike
                console.println(players[currentPlayerIndex].name
                        + " now has "
                        + players[currentPlayerIndex].purses
                        + " Gold Coins.")

                if (didPlayerWin()) {
                    leaderboard.addWinner()
                    players[currentPlayerIndex].hasWin = true
                }
            }
            incrementCurrentPlayerIndex()
        } else {
            console.println("Answer was correct!!!!")
            players[currentPlayerIndex].correctAnswerStrike++
            players[currentPlayerIndex].purses += players[currentPlayerIndex].correctAnswerStrike
            console.println(players[currentPlayerIndex].name
                    + " now has "
                    + players[currentPlayerIndex].purses
                    + " Gold Coins.")

            if (didPlayerWin()) {
                leaderboard.addWinner()
                players[currentPlayerIndex].hasWin = true
                console.println("${players[currentPlayerIndex].name} win")
            }
            currentPlayerIndex++
            if (currentPlayerIndex == players.size) currentPlayerIndex = 0
        }
    }

    fun getQuestionDistribution(): ArrayList<Pair<Category, Int>> {
        val arrayList = arrayListOf<Pair<Category, Int>>()

        arrayList.add(Pair(Category.POP, popQuestions.size))
        arrayList.add(Pair(Category.SCIENCE, scienceQuestions.size))
        arrayList.add(Pair(Category.SPORT, sportsQuestions.size))
        arrayList.add(Pair(Category.ROCK, rockQuestions.size))
        arrayList.add(Pair(Category.TECHNO, technoQuestions.size))
        arrayList.add(Pair(Category.RAP, rapQuestions.size))
        arrayList.add(Pair(Category.PHILO, philoQuestions.size))
        arrayList.add(Pair(Category.LITTERATURE, litteratureQuestions.size))
        arrayList.add(Pair(Category.GEO, geoQuestions.size))
        arrayList.add(Pair(Category.PEOPLE, peopleQuestions.size))

        return arrayList
    }

    fun wrongAnswer() {
        try {
            // Check if player has been removed because doesn't want to answer
            players[currentPlayerIndex]
        } catch (error: IndexOutOfBoundsException) {
            if (players.size == 0) {
                return
            }
            incrementCurrentPlayerIndex()
            return
        }
        if (players[currentPlayerIndex].hasJoker) {
            players[currentPlayerIndex].hasJoker = false
            incrementCurrentPlayerIndex()
            return
        }
        console.println("Question was incorrectly answered")
        console.println(players[currentPlayerIndex].name + " was sent to the penalty box")
        currentCategory = players[currentPlayerIndex].nextCategoryWhenLoosing
        players[currentPlayerIndex].correctAnswerStrike = 0
        players[currentPlayerIndex].isInPenaltyBox = true
        players[currentPlayerIndex].dateInJail = Date()
        val numberInJail = players.count { it.isInPenaltyBox }
        if (cellsInJail in 1 until numberInJail) {
            val player = players.filter { it.isInPenaltyBox }.sortedBy { it.dateInJail }.firstOrNull()
            player?.dateInJail = null
            player?.isInPenaltyBox = false
            console.println("${player?.name} is getting out penalty box because of number of places in jail")
        }
        players[currentPlayerIndex].timesGotInPrison++
        if (players[currentPlayerIndex].timesGotInPrison % 3 == 0) {
            console.println("${players[currentPlayerIndex].name} earns a joker after ${players[currentPlayerIndex].timesGotInPrison} turn in jail")
            players[currentPlayerIndex].hasJoker = true
        }

        incrementCurrentPlayerIndex()
    }

    private fun askQuestion() {
        if (players[currentPlayerIndex].hasJoker) {
            console.println("${players[currentPlayerIndex].name} uses his joker")
        } else if (players[currentPlayerIndex].doesNotWantToAnswer) {
            console.println("${players[currentPlayerIndex].name} doesn't want to answer and left the game")
            players.removeAt(currentPlayerIndex)
        } else {
            try {
                when (currentCategory()) {
                    Category.POP -> console.println(popQuestions.removeFirst().toString())
                    Category.SCIENCE -> console.println(scienceQuestions.removeFirst().toString())
                    Category.SPORT -> console.println(sportsQuestions.removeFirst().toString())
                    Category.ROCK -> console.println(rockQuestions.removeFirst().toString())
                    Category.TECHNO -> console.println(technoQuestions.removeFirst().toString())
                    Category.RAP -> console.println(rapQuestions.removeFirst().toString())
                    Category.PHILO -> console.println(philoQuestions.removeFirst().toString())
                    Category.LITTERATURE -> console.println(litteratureQuestions.removeFirst().toString())
                    Category.GEO -> console.println(geoQuestions.removeFirst().toString())
                    Category.PEOPLE -> console.println(peopleQuestions.removeFirst().toString())
                }
            } catch (exception: NoSuchElementException) {
                fillQuestions()
                askQuestion()
            }
        }
    }

    private fun incrementCurrentPlayerIndex() {
        if (leaderboard.isComplete()) {
            return
        }
        currentPlayerIndex++
        if (currentPlayerIndex >= players.size) {
            currentPlayerIndex = 0
        }
        if (players[currentPlayerIndex].hasWin) {
            incrementCurrentPlayerIndex()
        }
    }

    private fun currentCategory(): Category {
        if (currentCategory != null) {
            val toReturn = currentCategory!!
            currentCategory = null
            return toReturn
        }
        if (shouldUseExpansionPack) {
            return when (players[currentPlayerIndex].place) {
                0 -> Category.POP
                1 -> Category.SCIENCE
                2 -> Category.SPORT
                3, 11 -> Category.RAP
                4, 7 -> Category.GEO
                8 -> Category.PHILO
                9 -> Category.LITTERATURE
                10 -> Category.PEOPLE
                else -> if (shouldReplaceRockByTechno) Category.TECHNO else Category.ROCK
            }
        } else {
            return when (players[currentPlayerIndex].place) {
                0, 4, 8 -> Category.POP
                1, 5, 9 -> Category.SCIENCE
                2, 6, 10 -> Category.SPORT
                else -> if (shouldReplaceRockByTechno) Category.TECHNO else Category.ROCK
            }
        }
    }

    private fun throwIfNotPlayable() {
        if (players.size !in 2..6) throw PlayersNumberError()
        if (minimalGoldRequired < 6) throw MinimalGoldRequiredNotEnoughError()
    }

    private fun didPlayerWin(): Boolean = players[currentPlayerIndex].purses >= minimalGoldRequired

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
                peopleQuestions.addLast("People Question $i")
            }
        }
        numberOfQuestions += 49
    }
}