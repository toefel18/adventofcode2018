package adventofcode.day5

import java.io.File

fun main(args: Array<String>) {
    val input = File(ClassLoader.getSystemResource("day-05-input.txt").file).readText()
    println(input.length)

    // part 1
    val collapsed = collapsePolymers(input)
    println(collapsed.toString())
    println(collapsed.toString().length)

    // part 2
    findShortest(input)
}

fun findShortest(input: String): Unit {
    val lengths: MutableList<Pair<Char, Int>> = mutableListOf()
    for (letter in 'a'..'z') {
        val length = collapsePolymers(input.replace(letter.toString(), "", true)).toString().length
        println("Without $letter length = $length")
        lengths.add(Pair(letter, length))
    }
    println(lengths.toList().sortedWith(compareBy { it.second }).first())
}

fun collapsePolymers(input: String): java.lang.StringBuilder {
    val builder = StringBuilder(input)
    var currentIndex = 0
    while (currentIndex < builder.length - 1) {
        val current = builder[currentIndex]
        val next = builder[currentIndex + 1]
        if (current.equals(next, ignoreCase = true) && !current.equals(next, false)) {
            builder.deleteCharAt(currentIndex)
            builder.deleteCharAt(currentIndex)
            currentIndex = 0
            continue
        }
        currentIndex += 1
    }
    return builder
}
