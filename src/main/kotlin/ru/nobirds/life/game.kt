package ru.nobirds.life

import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.concurrent.thread
import javax.swing.JLabel
import javax.swing.Icon
import javax.swing.ImageIcon
import sun.misc.IOUtils
import java.awt.GraphicsEnvironment
import java.awt.Color
import javax.swing.SwingUtilities
import java.io.BufferedReader
import java.util.ArrayList
import java.util.Random

object Constants {

    public val NEIGHBOURHOODS:Array<Pair<Int, Int>> = array(
            -1 to -1, -1 to 0, -1 to 1,
             0 to -1,/*0 to 0*/ 0 to 1,
             1 to -1,  1 to 0,  1 to 1
    )

}

class Game(val firstGeneration:GenerationConstructor, val turnListener:(Game, Generation)->Unit) {

    private var generation = Generation(firstGeneration)
    private var gameOver = false

    private var turns = 0

    private var liveCount = generation.live()

    private var changed = 0

    public fun turn(): Unit {
        if(gameOver) throw RuntimeException("Game is over")

        turns++

        val newGeneration = Generation(LifeRulesGenerationConstructor(generation))

        liveCount = newGeneration.live()

        changed = generation.changed(newGeneration)

        gameOver = liveCount == 0 || changed == 0

        turnListener(this, newGeneration)

        generation = newGeneration
    }

    public fun over(): Boolean = gameOver

    public fun turnsCount(): Int = turns

    public fun liveCount(): Int = liveCount

    public fun changed(): Int = changed

}

