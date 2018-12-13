package adventofcode.twothousant15.day1

import java.io.File
import java.util.*

fun main(args: Array<String>) {
    val input = File(ClassLoader.getSystemResource("twothousant15/day-1-input.txt").file).readText()
            .map { if (it == '(') 1 else -1 }

    var current = 0
    var currentPos = 0
    while (current != -1) {
        current += input[currentPos++]
    }
    println(currentPos)
}