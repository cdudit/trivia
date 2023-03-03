package com.adaptionsoft.games.uglytrivia.console

import java.lang.StringBuilder

class SpyConsole : IConsole {
    private val builder = StringBuilder()
    
    override fun println(message: String) {
        builder.append(message)
        kotlin.io.println(message)
    }

    fun getContent(): String {
        return builder.toString()
    }
}