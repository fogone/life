package ru.nobirds.life

import java.io.BufferedReader
import java.util.Random

enum class Cell {
    LIVE
    DEAD
}

trait GenerationConstructor {

    val size:Int

    fun construct(x:Int,y:Int):Cell

}

class RandomGenerationConstructor(override val size:Int) : GenerationConstructor {

    private val random = Random()

    override fun construct(x: Int, y: Int): Cell {
        return if (random.nextBoolean()) Cell.LIVE else Cell.DEAD
    }
}

class ChessGenerationConstructor(override val size:Int, private val m:Int) : GenerationConstructor {

    private var counter = 1

    override fun construct(x: Int, y: Int): Cell {
        return if (counter++ mod m == 0) Cell.LIVE else Cell.DEAD
    }
}

class FileGenerationConstructor(fileName:String) : GenerationConstructor {

    private val matrix = toGeneration(TextUtils.readLines(fileName))

    override val size: Int = matrix.size

    override fun construct(x: Int, y: Int): Cell = matrix[x][y]

    private fun toGeneration(lines:List<String>): Array<Array<Cell>> {
        return Array<Array<Cell>>(lines.size) { x ->
            var line = lines[x].trim()
            Array<Cell>(line.size) { y ->
                if(line[y] == '0') Cell.DEAD
                else Cell.LIVE
            }
        }
    }

}

class LifeRulesGenerationConstructor(private val generation:Generation): GenerationConstructor {

    override val size: Int = generation.size

    override fun construct(x: Int, y: Int): Cell {
        val isLive = generation.isLive(x, y)
        val liveNeighbourhoods = generation.liveNeighbourhoods(x, y)

        return when {
            !isLive && liveNeighbourhoods == 3 -> Cell.LIVE
            isLive && liveNeighbourhoods in 2..3 -> Cell.LIVE
            else -> Cell.DEAD
        }
    }

}

class Generation(constructor:GenerationConstructor) {

    val size = constructor.size

    private val cells:Array<Array<Cell>> = Array<Array<Cell>>(size) { x ->
        Array<Cell>(size) { y -> constructor.construct(x, y) }
    }

    private fun get(x:Int, y:Int): Cell {
        val (dx, dy) = calculate(x, y)
        return cells[dx][dy]
    }

    private fun calculate(x:Int, y:Int): Pair<Int, Int> {
        var dx = x mod size
        if (dx < 0) dx = size + dx

        var dy = y mod size
        if (dy < 0) dy = size + dy

        return Pair<Int, Int>(dx, dy)
    }


    public fun isLive(x:Int, y:Int): Boolean = this[x, y] == Cell.LIVE

    public fun liveNeighbourhoods(x:Int, y:Int): Int {
        return Constants.NEIGHBOURHOODS
                .fold(0) { sum, item -> sum + if(isLive(x+item.first, y+item.second)) 1 else 0 }
    }

    public fun changed(generation:Generation): Int = find { x, y ->
        isLive(x, y) != generation.isLive(x, y)
    }

    public fun forEach(block:(Int,Int)->Unit): Unit {
        val range = 0..size

        range.forEach { x ->
            range.forEach { y ->
                block(x, y)
            }
        }
    }

    private fun find(block:(Int, Int)->Boolean): Int {
        var result = 0

        forEach { x, y ->
            if(block(x, y))
                result++
        }

        return result
    }

    public fun live(): Int {
        var count = 0
        forEach { x, y ->
            if(isLive(x, y)) count++
        }
        return count
    }
}
