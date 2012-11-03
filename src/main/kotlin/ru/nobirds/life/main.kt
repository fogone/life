package ru.nobirds.life

import java.awt.Color
import kotlin.concurrent.thread
import javax.swing.JFrame
import javax.swing.ImageIcon
import javax.swing.JLabel
import java.awt.GraphicsEnvironment
import java.awt.Graphics2D
import javax.swing.JOptionPane
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.ChartPanel
import org.jfree.data.xy.XYDataset
import org.jfree.chart.axis.ValueAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.renderer.xy.XYItemRenderer
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer
import org.jfree.data.xy.DefaultXYDataset
import kotlin.swing.panel
import org.jfree.data.RangeType
import org.jfree.chart.renderer.xy.XYStepRenderer
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer

class ChartData(val key:String, val size:Int) {

    private var xdata = DoubleArray(0)
    private var ydata = DoubleArray(0)

    fun add(x:Int,y:Int):Array<DoubleArray>  {
        xdata = if (xdata.size < size) xdata.copyOf(xdata.size + 1)
        else xdata.copyOfRange(1, xdata.size)
        xdata[xdata.lastIndex] = x.toDouble()

        ydata = if(ydata.size < size) ydata.copyOf(ydata.size + 1)
        else ydata.copyOfRange(1, ydata.size)
        ydata[ydata.lastIndex] = y.toDouble()

        return array(xdata, ydata)
    }
}

class LifeGameFrame(val firstGeneration:GenerationConstructor) : JFrame("Life game") {

    private val rectSize = 5
    private val chartDataSize = 50

    private val liveChartData = ChartData("live", chartDataSize)
    private val changedChartData = ChartData("changed", chartDataSize)

    private val dataset = DefaultXYDataset()
    private val turnAxis = NumberAxis("Turns")

    private val image = getGraphicsConfiguration()!!
            .createCompatibleImage(firstGeneration.size.x * rectSize, firstGeneration.size.y * rectSize)!!


    {
        add(panel {
            add(JLabel(ImageIcon(image)))
            add(ChartPanel(createChart(dataset)))
        })

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        pack()
        setVisible(true)
    }

    private fun createChart(dataset:XYDataset):JFreeChart {
        return JFreeChart(XYPlot(dataset, turnAxis, NumberAxis("Points"), XYLineAndShapeRenderer()))
    }

    private fun addToChart(game:Game) {
        if(game.turnsCount() > chartDataSize)
            turnAxis.setRange((game.turnsCount()-chartDataSize).toDouble(), game.turnsCount().toDouble())
        else
            turnAxis.setRange(0.toDouble(), chartDataSize.toDouble())

        dataset.addSeries("live", liveChartData.add(game.turnsCount(), game.liveCount()))
        dataset.addSeries("changed", changedChartData.add(game.turnsCount(), game.changed()))
    }

    val game = Game(firstGeneration) { game, generation ->
        generation.size.forEach { x, y ->
            val color = if(generation.isLive(x, y)) Color.WHITE else Color.BLACK

            val g = image.getGraphics()!!
            g.setColor(color)
            g.fillRect(x * rectSize + 1, y * rectSize + 1, rectSize - 1, rectSize - 1)
        }

        setTitle("Life game, turn " + game.turnsCount() + ", live " + game.liveCount() + ", changed " + game.changed())
        addToChart(game)
        repaint()
    }

}

fun main(args: Array<String>) {
    val generationConstructor = RandomGenerationConstructor(Size(100, 50))

    val frame = LifeGameFrame(generationConstructor)

    thread {
        val game = frame.game

        while (!game.over()) {
            game.turn()
            wait(100)
        }

        JOptionPane.showMessageDialog(frame, "Life game is over, total turns " + game.turnsCount())

        frame.dispose()
    }

}