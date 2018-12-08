package adventofcode.day8

import java.io.File

fun main(args: Array<String>) {
    val input: List<Int> = File(ClassLoader.getSystemResource("day-08-input.txt").file).readText().split(" ").map { it.toInt() }
    val tree = buildTreeIter(input.iterator())
    println("part1 ${tree.sumMeta()}")   //part1 40309
    println("part2 ${tree.calcValue()}") //part2 28779
}

data class Node(val children: List<Node>, val metadata: List<Int>) {
    fun sumMeta(): Long = metadata.sum() + children.map { it.sumMeta() }.sum()
    fun calcValue(): Long = if (children.isEmpty())
        metadata.sum().toLong()
    else
        metadata.filter { it != 0 }.mapNotNull { children.getOrNull(it - 1) }.map { it.calcValue() }.sum()
}

fun buildTreeIter(input: Iterator<Int>): Node {
    val (numChildren, numMetadata) = listOf(input.next(), input.next())
    val children = (0 until numChildren).map { buildTreeIter(input) }
    return Node(children, (0 until numMetadata).map { input.next() })
}