package ru.nobirds.life

import java.io.BufferedReader
import java.util.Random

enum class Cell {
    LIVE
    DEAD
}

trait GenerationConstructor {

    val size:Size

    fun construct(x:Int,y:Int):Cell

}

class RandomGenerationConstructor(override val size:Size) : GenerationConstructor {

    private val random = Random()

    override fun construct(x: Int, y: Int): Cell {
        return if (random.nextBoolean()) Cell.LIVE else Cell.DEAD
    }
}

class ChessGenerationConstructor(override val size:Size, private val m:Int) : GenerationConstructor {

    private var counter = 1

    override fun construct(x: Int, y: Int): Cell {
        return if (counter++ mod m == 0) Cell.LIVE else Cell.DEAD
    }
}

class FileGenerationConstructor(fileName:String) : GenerationConstructor {

    private val matrix = toGeneration(TextUtils.readLines(fileName))

    override val size: Size = getSize(matrix)

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

    private fun getSize(matrix:Array<Array<Cell>>): Size {
        val x = matrix.size
        val y = matrix[0].size
        return Size(x, y)
    }
}

class LifeRulesGenerationConstructor(private val generation:Generation): GenerationConstructor {

    override val size: Size = generation.size

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

    public val size:Size = constructor.size

    private val cells:Array<Array<Cell>> = Array<Array<Cell>>(size.x) { x ->
        Array<Cell>(size.y) { y -> constructor.construct(x, y) }
    }

    private fun get(x:Int, y:Int): Cell {
        val (dx, dy) = calculate(x, y)
        return cells[dx][dy]
    }

    private fun calculatePosition(v:Int, size:Int): Int {
        val dv = v mod size
        return if (dv < 0) size + dv else dv
    }

    private fun calculate(x:Int, y:Int): Size =
        Size(calculatePosition(x, size.x), calculatePosition(y, size.y))

    public fun isLive(x:Int, y:Int): Boolean = this[x, y] == Cell.LIVE

    public fun liveNeighbourhoods(x:Int, y:Int): Int {
        return Constants.NEIGHBOURHOODS
                .fold(0) { sum, item -> sum + if(isLive(x+item.first, y+item.second)) 1 else 0 }
    }

    public fun changed(generation:Generation): Int = find { x, y ->
        isLive(x, y) != generation.isLive(x, y)
    }

    public fun live(): Int = find { x, y -> isLive(x, y) }

    private fun find(block:(Int, Int)->Boolean): Int {
        var result = 0

        size.forEach { x, y ->
            if(block(x, y))
                result++
        }

        return result
    }

}
