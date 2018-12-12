package adventofcode.day12

import java.io.File
import java.util.*

class Pots(var potZeroIndex: Int, var currentGen: LinkedList<Char>) {

    fun processGeneration(rules: Map<String, Char>) {
        growIfNecessary()
        val newGeneration = LinkedList<Char>()
        for (window in currentGen.windowed(5, 1)) {
            val pattern = String(window.toCharArray())
            newGeneration.add(rules[pattern] ?: '.')
        }
        potZeroIndex -= 2
        currentGen = newGeneration
    }

    fun sum() = currentGen.mapIndexed { index, c -> if (c == '#') index - potZeroIndex else 0 }.sum()

    fun print() {
        while (potZeroIndex > 5) {
            potZeroIndex--
            currentGen.pop()
        }
        (0 until potZeroIndex).forEach { print(" ") }
        println("0")
        currentGen.forEach { print(it) }
        println()
    }

    private fun growIfNecessary() {
        while (potZeroIndex < 5 || currentGen.indexOf('#') < 5) {
            currentGen.push('.')
            potZeroIndex += 1
        }
        while (currentGen.takeLast(5) != listOf('.', '.', '.', '.', '.')) {
            currentGen.add('.')
        }
    }
}


fun main(args: Array<String>) {
    val input: List<String> = File(ClassLoader.getSystemResource("day-12-input.txt").file).readLines()
    val currentGeneration = input.first().substring(15)
    println(currentGeneration)

    val mapOfRules = input.drop(2).map { it.substring(0, 5) to it.drop(9).take(1).toCharArray().first() }.toMap()
    mapOfRules.forEach { (k, v) -> println("$k = $v") }

    val rowOfPots = Pots(0, LinkedList(currentGeneration.toList()))
    rowOfPots.print()

    for (i in 1..101) {
        println("$i     ${rowOfPots.sum()}")
        rowOfPots.processGeneration(mapOfRules)
        rowOfPots.print()
    }

    // after this the pattern repeats, you can calculate the result manually
}