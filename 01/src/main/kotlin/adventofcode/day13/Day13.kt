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

/**
 * This looks like a mess because there are two ways of calculating the next move, still needs cleanup!
 */


enum class Direction(val c: Char, val offset: Point) {
    // order matters
    LEFT('<', Point(0, -1)),
    TOP('^', Point(-1, 0)),
    RIGHT('>', Point(0, 1)),
    BOTTOM('v', Point(1, 0));

    override fun toString(): String = "$c"

    companion object {
        fun from(from: Point, to: Point): Direction = when {
            from.x > to.x -> Direction.BOTTOM
            from.x < to.x -> Direction.TOP
            from.y > to.y -> Direction.RIGHT
            from.y < to.y -> Direction.LEFT
            else -> throw IllegalStateException("Invalid direction")
        }
    }
}

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
}

abstract class Infra(val pos: Point, val char: Char) {
    abstract fun calculateNextDirection(cart: Cart): Direction
}

class HRail(pos: Point) : Infra(pos, '-') {
    override fun calculateNextDirection(cart: Cart): Direction {
        if (!(cart.direction == Direction.LEFT || cart.direction == Direction.RIGHT)) throw IllegalStateException("Illegal direction, expecting horizontal but was ${cart.direction} at $pos")
        return cart.direction
    }
}

class VRail(pos: Point) : Infra(pos, '|') {
    override fun calculateNextDirection(cart: Cart): Direction {
        if (!(cart.direction == Direction.TOP || cart.direction == Direction.BOTTOM)) throw IllegalStateException("Illegal direction, expecting vertical but was ${cart.direction} at $pos")
        return cart.direction
    }
}

class Turn(pos: Point, char: Char) : Infra(pos, char) {
    override fun calculateNextDirection(cart: Cart): Direction {
        val from = Direction.from(cart.pos, this.pos)
        return when {
            char == '/' && from == Direction.BOTTOM -> Direction.RIGHT
            char == '/' && from == Direction.RIGHT -> Direction.BOTTOM
            char == '/' && from == Direction.LEFT -> Direction.TOP
            char == '/' && from == Direction.TOP -> Direction.LEFT
            char == '\\' && from == Direction.BOTTOM -> Direction.LEFT
            char == '\\' && from == Direction.RIGHT -> Direction.TOP
            char == '\\' && from == Direction.LEFT -> Direction.BOTTOM
            char == '\\' && from == Direction.TOP -> Direction.RIGHT
            else -> throw IllegalStateException("Unknown turn $pos")
        }
    }
}

class Intersection(pos: Point) : Infra(pos, '+') {
    val directions = (Direction.values() + Direction.values()).toList()

    override fun calculateNextDirection(cart: Cart): Direction {
        val from = Direction.from(cart.pos, pos)
        val untilReachesNextTurn = when (cart.nextTurn.first) {
            "L" -> 1
            "S" -> 2
            "R" -> 3
            else -> throw IllegalStateException("only L S and R are supported $pos")
        }
        cart.rotateTurn()
        val x = directions.dropWhile { it != from }.drop(untilReachesNextTurn).first()
        return x
    }
}

class DeadEnd(pos: Point) : Infra(pos, ' ') {
    override fun calculateNextDirection(cart: Cart): Direction {
        throw IllegalStateException("Reached dead end $pos")
    }
}

class Cart(
        var pos: Point,
        var direction: Direction,
        val nextTurn: ArrayDeque<String> = ArrayDeque(listOf("L", "S", "R")),
        var crashed: Boolean = false) {

    fun rotateTurn() = nextTurn.addLast(nextTurn.pollFirst())

    // Probably a better solution
//    fun move(infra: List<List<Infra>>) {
//        val nextPos = pos + direction.offset
//        val node = infra[nextPos.x][nextPos.y]
//        val nextDirection = when {
//            node.char == '\\' && direction == Direction.LEFT -> Direction.TOP
//            node.char == '\\' && direction == Direction.TOP -> Direction.LEFT
//            node.char == '\\' && direction == Direction.BOTTOM -> Direction.RIGHT
//            node.char == '\\' && direction == Direction.RIGHT -> Direction.BOTTOM
//            node.char == '/' && direction == Direction.LEFT -> Direction.BOTTOM
//            node.char == '/' && direction == Direction.TOP -> Direction.RIGHT
//            node.char == '/' && direction == Direction.BOTTOM -> Direction.LEFT
//            node.char == '/' && direction == Direction.RIGHT -> Direction.TOP
//            node.char == '|' || node.char == '-' -> direction
//            node.char == '+' -> {
//                val turn = nextTurn.first
//                rotateTurn()
//                when {
//                    direction == Direction.RIGHT && turn == "L" -> Direction.TOP
//                    direction == Direction.RIGHT && turn == "S" -> Direction.RIGHT
//                    direction == Direction.RIGHT && turn == "R" -> Direction.BOTTOM
//
//                    direction == Direction.LEFT && turn == "L" -> Direction.BOTTOM
//                    direction == Direction.LEFT && turn == "S" -> Direction.LEFT
//                    direction == Direction.LEFT && turn == "R" -> Direction.TOP
//
//                    direction == Direction.BOTTOM && turn == "L" -> Direction.RIGHT
//                    direction == Direction.BOTTOM && turn == "S" -> Direction.BOTTOM
//                    direction == Direction.BOTTOM && turn == "R" -> Direction.LEFT
//
//                    direction == Direction.TOP && turn == "L" -> Direction.LEFT
//                    direction == Direction.TOP && turn == "S" -> Direction.TOP
//                    direction == Direction.TOP && turn == "R" -> Direction.RIGHT
//                    else -> throw IllegalStateException()
//                }
//            }
//            else -> throw IllegalStateException()
//        }
//        this.pos = nextPos
//        this.direction = nextDirection
//    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cart

        if (pos != other.pos) return false
        if (direction != other.direction) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pos.hashCode()
        result = 31 * result + direction.hashCode()
        return result
    }
}

class Track(val infra: List<List<Infra>>, var carts: MutableList<Cart>) {

    fun tickPart1() {
        val cartsInOrder = carts
                .filter { !it.crashed }
                .sortedWith(compareBy({ it.pos.x }, { it.pos.y }))

        for (cart in cartsInOrder) {
            val newPos = cart.pos + cart.direction.offset
            val nextInfra = infra[newPos.x][newPos.y]
            cart.direction = nextInfra.calculateNextDirection(cart)
            cart.pos = newPos

            val cartsByPos = (carts).groupBy { it.pos }
            if (cartsByPos[cart.pos]!!.size > 1) {
                cartsByPos[cart.pos]!!.forEach { it.crashed = true }
                println("Collision at ${cart.pos.y},${cart.pos.x}")
            }
        }
    }


    fun tickPart2() {
        val cartsInOrder = carts
                .filter { !it.crashed }
                .sortedWith(compareBy({ it.pos.x }, { it.pos.y }))

        for (cart in cartsInOrder) {
            if (!carts.contains(cart)) continue
            val newPos = cart.pos + cart.direction.offset
            val nextInfra = infra[newPos.x][newPos.y]
            val wouldBeCrash = carts.find { it.pos == newPos }

            if (wouldBeCrash != null) {
                carts.remove(cart)
                carts.remove(wouldBeCrash)
                continue
            }

            cart.direction = nextInfra.calculateNextDirection(cart)
            cart.pos = newPos

            val cartsByPos = (carts).groupBy { it.pos }
            if (cartsByPos[cart.pos]!!.size > 1) {
                cartsByPos[cart.pos]!!.forEach { it.crashed = true }
                println("Collision at ${cart.pos.y},${cart.pos.x}  remaining ${carts.filter { !it.crashed }.size}")
            }
        }
        if (carts.filter { !it.crashed }.size == 1) {
            println(carts.filter { !it.crashed }.map { "END RESULT ${it.pos.y},${it.pos.x}" })
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
                    else -> print("${ANSI_YELLOW}${cartsAtPosition.first().direction}$ANSI_RESET")
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
    while (track.carts.filter { !it.crashed }.size > 1) {
        track.tickPart1()
    }

    val carts2 = mutableListOf<Cart>()
    val trackInfra2 = parseInput(input, carts2)
    val track2 = Track(trackInfra2, carts2)
    while (track2.carts.filter { !it.crashed }.size > 1) {
        track2.tickPart2()
    }

    Thread.sleep(100)

}

private fun parseInput(input: List<CharArray>, carts: MutableList<Cart>): List<List<Infra>> {
    return input
            .mapIndexed { x, row ->
                row.mapIndexed { y, char ->
                    when (char) {
                        '/', '\\' -> Turn(Point(x, y), char)
                        '+' -> Intersection(Point(x, y))
                        '-' -> HRail(Point(x, y))
                        '|' -> VRail(Point(x, y))
                        '<' -> {
                            carts.add(Cart(Point(x, y), Direction.LEFT))
                            HRail(Point(x, y))
                        }
                        '>' -> {
                            carts.add(Cart(Point(x, y), Direction.RIGHT))
                            HRail(Point(x, y))
                        }
                        '^' -> {
                            carts.add(Cart(Point(x, y), Direction.TOP))
                            VRail(Point(x, y))
                        }
                        'v' -> {
                            carts.add(Cart(Point(x, y), Direction.BOTTOM))
                            VRail(Point(x, y))
                        }
                        else -> DeadEnd(Point(x, y))
                    }
                }
            }
}
