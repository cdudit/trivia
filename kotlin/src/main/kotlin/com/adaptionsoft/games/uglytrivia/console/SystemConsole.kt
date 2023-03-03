package com.adaptionsoft.games.uglytrivia.console

class SystemConsole: IConsole {
    override fun println(message: String) {
        kotlin.io.println(message)
    }
}