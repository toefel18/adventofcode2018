package adventofcode.day2

import java.io.File

fun main(args: Array<String>) {
    val input = File(ClassLoader.getSystemResource("day-02-input.txt").file).readLines()
    val (twos, threes, checksum) = calculateChecksum(input)
    println("$twos x $threes = $checksum")

    val closeIds = findCloseIds(input)

    val firstDiff = closeIds.first()
    val idWithoutDifferentChar = firstDiff.first.removeRange(firstDiff.indices.first()..firstDiff.indices.first())
    println("The box id = $idWithoutDifferentChar")
    println("original   = ${firstDiff.first}")
}

//part two
fun findCloseIds(boxIds: List<String>): MutableSet<Diff> {
    val idsWithTwosOrThrees = boxIds
            .map { countLettersInBoxId(it) }
            .filter { it.countsForThree() || it.countsForTwo() }

    val boxIdsDifferentBy1 = mutableSetOf<Diff>()

    for (first in idsWithTwosOrThrees) {
        for (second in idsWithTwosOrThrees) {
            val diff = first.boxId.diff(second.boxId)
            if (diff.indices.size == 1) {
                boxIdsDifferentBy1.add(diff)
            }
        }
    }

    return boxIdsDifferentBy1
}

//part one
fun calculateChecksum(boxIds: List<String>): Triple<Int, Int, Int> {
    val countedBoxIds = boxIds.map { countLettersInBoxId(it) }
    val twosCount = countedBoxIds.filter { it.countsForTwo() }.size
    val threesCount = countedBoxIds.filter { it.countsForThree() }.size
    return Triple(twosCount, threesCount, twosCount * threesCount)
}

fun countLettersInBoxId(boxId: String): BoxIdLetterCount {
    val letterCounts = boxId.toList()
            .groupingBy { it }
            .eachCount()
            .map { LetterCount(it.key, it.value) }
    return BoxIdLetterCount(boxId, letterCounts)
}

data class BoxIdLetterCount(val boxId: String, val letters: List<LetterCount>) {
    fun countsForTwo() = letters.find { it.count == 2 } != null
    fun countsForThree() = letters.find { it.count == 3 } != null
}

data class LetterCount(val letter: Char, val count: Int)

fun String.diff(other: String): Diff {
    val indices = mutableListOf<Int>()
    for (i in 0 until Math.min(this.length, other.length)) {
        if (this[i] != other[i]) {
            indices.add(i)
        }
    }

    return Diff(this, other, indices)
}

data class Diff(val first:String, val second:String, val indices:List<Int>)
