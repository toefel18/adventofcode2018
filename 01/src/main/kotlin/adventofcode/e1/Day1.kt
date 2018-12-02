package adventofcode.e1

import java.io.File
import java.util.*

fun main(args: Array<String>) {
    val input = File(ClassLoader.getSystemResource("day-01-input.txt").file).readLines()
    println("frequenciesAfterFirstRun:   " + calculateFrequency(frequencyDeltas = input, initialFrequency = 0))
    println("firstFrequencyReachedTwice: " + findFirstFrequencyReachedTwice(frequencyDeltas = input, initialFrequency = 0))
}

// part 1
private fun calculateFrequency(frequencyDeltas: List<String>, initialFrequency: Long): Long{
    return frequencyDeltas
            .map { it.toLong() }
            .fold(initialFrequency) { currentFrequency, delta -> currentFrequency + delta }
}

// part 2
private fun findFirstFrequencyReachedTwice(frequencyDeltas: List<String>, initialFrequency: Long): Long {
    val reachedFrequencies = mutableSetOf<Long>()
    var currentFrequency = initialFrequency
    var deltas = LinkedList<String>()

    while(!reachedFrequencies.contains(currentFrequency)) {
        reachedFrequencies.add(currentFrequency)

        if (deltas.isEmpty()) {
            deltas = LinkedList(frequencyDeltas)
        }

        currentFrequency += deltas.pop()!!.toLong()
    }

    return currentFrequency
}