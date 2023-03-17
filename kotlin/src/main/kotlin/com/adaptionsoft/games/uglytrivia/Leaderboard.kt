package com.adaptionsoft.games.uglytrivia

data class Leaderboard(
    var hasFirstPlayer: Boolean = false,
    var hasSecondPlayer: Boolean = false,
    var hasThirdPlayer: Boolean = false
) {
    fun isComplete() = hasFirstPlayer && hasSecondPlayer && hasThirdPlayer

    fun addWinner() {
        if (!hasFirstPlayer) {
            hasFirstPlayer = true
        } else if (!hasSecondPlayer) {
            hasSecondPlayer = true
        } else {
            hasThirdPlayer = true
        }
    }
}