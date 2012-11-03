package ru.nobirds.life

import java.io.BufferedReader
import java.util.ArrayList


fun wait(millis:Long):Unit {
    val thread = Thread.currentThread()
    synchronized(thread) {
        thread.wait(millis)
    }
}

data class Size(val x:Int,val y:Int) {

    public fun forEach(block:(Int,Int)->Unit): Unit {
        (0..x).forEach { x ->
            (0..y).forEach { y ->
                block(x, y)
            }
        }
    }

}

object TextUtils {

    fun readLines(fileName:String): List<String> {
        val stream = ClassLoader.getSystemClassLoader()!!.getResourceAsStream(fileName)!!

        val result = ArrayList<String>()

        stream.use { s ->
            BufferedReader(s.reader("UTF-8")).forEachLine { result.add(it)  }
        }

        return result
    }

}

