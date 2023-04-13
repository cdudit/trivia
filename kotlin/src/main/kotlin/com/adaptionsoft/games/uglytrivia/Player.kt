package com.adaptionsoft.games.uglytrivia

data class Player(
    val name: String,
    var purses: Int = 0,
    var place: Int = 0,
    var isInPenaltyBox: Boolean = false,
    var timesGotInPrison: Int = 0,
    val doesNotWantToAnswer: Boolean = false,
    var hasJoker: Boolean = false,
    var correctAnswerStrike: Int = 0
)
