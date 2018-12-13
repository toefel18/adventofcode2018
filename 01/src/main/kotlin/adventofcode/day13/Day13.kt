@file:JvmName("Day13")

package adventofcode.day13

import java.io.File
import java.lang.IllegalStateException
import java.util.*
import javax.swing.text.StyledEditorKit

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
        Thread.sleep(1000)
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
        var crashed : Boolean = false) {
    fun rotateTurn() = nextTurn.addLast(nextTurn.pollFirst())
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

    fun tick() {
        val cartsInOrder = carts.filter { !it.crashed }.sortedWith(compareBy({ it.pos.x }, { it.pos.y }))

        cartsInOrder.forEach { println(it.pos) }

        for (cart in cartsInOrder) {
            if (cart.crashed) continue
            val newPos = cart.pos + cart.direction.offset
            val nextInfra = infra[newPos.x][newPos.y]
            cart.direction = nextInfra.calculateNextDirection(cart)
            cart.pos = newPos

            val cartsByPos = (carts - cart).groupBy { it.pos }
            if (cartsByPos[cart.pos] != null) {
                cart.crashed = true
                cartsByPos[cart.pos]!!.forEach { it.crashed = true }
                println("Collision at ${cart.pos}")
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
                    else -> print("${ANSI_YELLOW}${cartsAtPosition.first().direction}$ANSI_RESET")
                }
            }
            println()
        }
        println(carts.filter { it.crashed }.map { it.pos })
    }
}


fun main(args: Array<String>) {
    val input = File(ClassLoader.getSystemResource("day-13-test-input.txt").file).readLines().map { it.toCharArray() }
    val carts = mutableListOf<Cart>()
    val trackInfra = input
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

    val track = Track(trackInfra, carts)
    track.print()
    while (true) {
        track.tick()
        track.print()
        Thread.sleep(100)
        if (track.carts.size == 0) {
            break;
        }
    }
}
