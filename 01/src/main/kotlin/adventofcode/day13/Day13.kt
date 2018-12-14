@file:JvmName("Day13")

package adventofcode.day13

import java.io.File
import java.lang.IllegalStateException
import java.util.*

val ANSI_RESET = "\u001B[0m"
val ANSI_BLACK = "\u001B[30m"
val ANSI_RED = "\u001B[31m"
val ANSI_GREEN = "\u001B[32m"
val ANSI_YELLOW = "\u001B[33m"
val ANSI_BLUE = "\u001B[34m"
val ANSI_PURPLE = "\u001B[35m"
val ANSI_CYAN = "\u001B[36m"
val ANSI_WHITE = "\u001B[37m"

enum class Direction(val c: Char, val offset: Point) {
    // order matters
    LEFT('<', Point(0, -1)),
    TOP('^', Point(-1, 0)),
    RIGHT('>', Point(0, 1)),
    BOTTOM('v', Point(1, 0));

    override fun toString(): String = "$c"
}

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
}

data class Infra(val pos: Point, val char: Char)

class Cart(
        var pos: Point,
        var direction: Direction,
        val nextTurn: ArrayDeque<String> = ArrayDeque(listOf("L", "S", "R")),
        var crashed: Boolean = false) {

    fun rotateTurn() = nextTurn.addLast(nextTurn.pollFirst())

    // Probably a better, more concise solution
    fun move(infra: List<List<Infra>>): Pair<Point, Direction> {
        val nextPos = pos + direction.offset
        val node = infra[nextPos.x][nextPos.y]
        val nextDirection = when {
            node.char == '\\' && direction == Direction.LEFT -> Direction.TOP
            node.char == '\\' && direction == Direction.TOP -> Direction.LEFT
            node.char == '\\' && direction == Direction.BOTTOM -> Direction.RIGHT
            node.char == '\\' && direction == Direction.RIGHT -> Direction.BOTTOM
            node.char == '/' && direction == Direction.LEFT -> Direction.BOTTOM
            node.char == '/' && direction == Direction.TOP -> Direction.RIGHT
            node.char == '/' && direction == Direction.BOTTOM -> Direction.LEFT
            node.char == '/' && direction == Direction.RIGHT -> Direction.TOP
            node.char == '|' || node.char == '-' -> direction
            node.char == '+' -> {
                val turn = nextTurn.first
                rotateTurn()
                when {
                    direction == Direction.RIGHT && turn == "L" -> Direction.TOP
                    direction == Direction.RIGHT && turn == "S" -> Direction.RIGHT
                    direction == Direction.RIGHT && turn == "R" -> Direction.BOTTOM

                    direction == Direction.LEFT && turn == "L" -> Direction.BOTTOM
                    direction == Direction.LEFT && turn == "S" -> Direction.LEFT
                    direction == Direction.LEFT && turn == "R" -> Direction.TOP

                    direction == Direction.BOTTOM && turn == "L" -> Direction.RIGHT
                    direction == Direction.BOTTOM && turn == "S" -> Direction.BOTTOM
                    direction == Direction.BOTTOM && turn == "R" -> Direction.LEFT

                    direction == Direction.TOP && turn == "L" -> Direction.LEFT
                    direction == Direction.TOP && turn == "S" -> Direction.TOP
                    direction == Direction.TOP && turn == "R" -> Direction.RIGHT
                    else -> throw IllegalStateException()
                }
            }
            else -> throw IllegalStateException("${node.char}")
        }
        return Pair(nextPos, nextDirection)
    }

}

class Track(val infra: List<List<Infra>>, var carts: MutableList<Cart>) {

    // input boolean is for part 2
    fun tick(preventCrashesBeforehand: Boolean) {
        val cartsInOrder = carts
                .filter { !it.crashed }
                .sortedWith(compareBy({ it.pos.x }, { it.pos.y }))

        for (cart in cartsInOrder) {
            if (cart.crashed) continue
            val (newPos, newDirection) = cart.move(infra)

            if (preventCrashesBeforehand && carts.groupBy { it.pos }[newPos] != null) {
                cart.crashed = true
                carts.groupBy { it.pos }[newPos]!!.forEach { it.crashed = true }
                carts.removeIf { it.crashed }
                continue
            }

            cart.pos = newPos
            cart.direction = newDirection

            val cartsByPos = carts.groupBy { it.pos }
            if (cartsByPos[cart.pos]!!.size > 1) {
                cartsByPos[cart.pos]!!.forEach { it.crashed = true }
                println("Collision at ${cart.pos.y},${cart.pos.x}")
            }
        }
    }

    fun print() {
        val positionToCarts = carts.groupBy { it.pos }

        infra.forEachIndexed { idx, row ->
            print("${idx} ".padStart(4, '0'))
            row.forEach { infra ->
                val cartsAtPosition = positionToCarts[infra.pos]
                when {
                    cartsAtPosition == null -> print(infra.char)
                    cartsAtPosition.first().crashed -> print("${ANSI_RED}X$ANSI_RESET")
                    else -> print("${ANSI_RED}${cartsAtPosition.first().direction}$ANSI_RESET")
                }
            }
            println()
        }
        println(carts.filter { it.crashed }.map { "${it.pos.y},${it.pos.x}" })
    }
}


fun main(args: Array<String>) {
    val input = File(ClassLoader.getSystemResource("day-13-input.txt").file).readLines().map { it.toCharArray() }
    val carts = mutableListOf<Cart>()
    val trackInfra = parseInput(input, carts)
    val track = Track(trackInfra, carts)
    track.print()
    while (track.carts.count { !it.crashed } > 1) {
        track.tick(preventCrashesBeforehand = false)
    }

    // part 2
    val carts2 = mutableListOf<Cart>()
    val trackInfra2 = parseInput(input, carts2)
    val track2 = Track(trackInfra2, carts2)
    while (track2.carts.size > 1) {
        track2.tick(preventCrashesBeforehand = true)
    }
    println("Last remaining cart ${track2.carts.filter { !it.crashed }.map { "${it.pos.y},${it.pos.x}" }}")
}

private fun parseInput(input: List<CharArray>, carts: MutableList<Cart>): List<List<Infra>> {
    return input
            .mapIndexed { x, row ->
                row.mapIndexed { y, char ->
                    when (char) {
                        '<' -> {
                            carts.add(Cart(Point(x, y), Direction.LEFT))
                        }
                        '>' -> {
                            carts.add(Cart(Point(x, y), Direction.RIGHT))
                        }
                        '^' -> {
                            carts.add(Cart(Point(x, y), Direction.TOP))
                        }
                        'v' -> {
                            carts.add(Cart(Point(x, y), Direction.BOTTOM))
                        }
                    }
                    val infraChar = if (char in listOf('^', 'v')) '|' else if (char in listOf('<', '>')) '-' else char
                    Infra(Point(x, y), infraChar)
                }
            }
}
