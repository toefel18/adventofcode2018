package adventofcode.day8

import java.io.File

fun main(args: Array<String>) {
    val input: List<Int> = File(ClassLoader.getSystemResource("day-08-input.txt").file).readText().split(" ").map { it.toInt() }
    val (tree, _) = buildTree(input)
    println("part1 ${tree.sumMeta()}")
    println("part2 ${tree.calcValue()}")
}

data class Node(val children: List<Node>, val metadata: List<Int>) {
    fun sumMeta(): Long = metadata.sum() + children.map { it.sumMeta() }.sum()
    fun calcValue(): Long = if (children.isEmpty())
        metadata.sum().toLong()
    else
        metadata.filter { it != 0 }.mapNotNull { children.getOrNull(it - 1) }.map { it.calcValue() }.sum()
}

fun buildTree(input: List<Int>): Pair<Node, Int> {
    val (numChildren, numMetadata) = input
    val children = mutableListOf<Node>()
    var remainingInput = input.drop(2)
    var totalCharsConsumed = 2

    for (child in 0 until numChildren) {
        val (node, charsConsumed) = buildTree(remainingInput)
        children.add(node)
        totalCharsConsumed += charsConsumed
        remainingInput = remainingInput.drop(charsConsumed)
    }

    return Pair(Node(children, input.drop(totalCharsConsumed).take(numMetadata)), totalCharsConsumed + numMetadata)
}