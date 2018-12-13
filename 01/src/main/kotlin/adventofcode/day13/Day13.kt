package adventofcode.day12

import java.io.File
import java.lang.IllegalStateException
import java.util.*

enum class Direction(val c: Char, val offset: Point) {
    LEFT('<', Point(-1, 0)),
    RIGHT('>', Point(1, 0)),
    TOP('^', Point(0, -1)),
    BOTTOM('v', Point(0, 1)),
    STRAIGHT('O', Point(0, 0));

    override fun toString(): String {
        return "$c"
    }
}

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
}

abstract class Infra(val pos: Point, val char: Char) {
    abstract fun calculateNextDirection(currentInfra: Infra, direction: Direction): Pair<Point, Direction>
}

class HRail(pos: Point) : Infra(pos, '-') {
    override fun calculateNextDirection(currentInfra: Infra, direction: Direction): Pair<Point, Direction> {
        if (!(direction == Direction.LEFT || direction == Direction.RIGHT)) throw IllegalStateException()
        return Pair(pos + direction.offset, direction)
    }
}

class VRail(pos: Point) : Infra(pos, '|') {
    override fun calculateNextDirection(currentInfra: Infra, direction: Direction): Pair<Point, Direction> {
        if (!(direction == Direction.TOP || direction == Direction.BOTTOM)) throw IllegalStateException()
        return Pair(pos + direction.offset, direction)
    }
}
class Turn(pos: Point, char: Char) : Infra(pos, char)
class Intersection(pos: Point) : Infra(pos, '+')
class Nothing(pos: Point) : Infra(pos, ' ')

data class Cart(
        var pos: Point,
        var direction: Direction,
        val nextTurn: ArrayDeque<Direction> = ArrayDeque(listOf(Direction.LEFT, Direction.STRAIGHT, Direction.RIGHT))) {
    fun rotateTurn() = nextTurn.addLast(nextTurn.pollFirst())
}

class Track(val infra: List<List<Infra>>, var carts: List<Cart>) {

    fun tick() {
        val cartsInOrder = carts.sortedWith(compareBy({ it.pos.y }, { it.pos.x }))

        for (cart in cartsInOrder) {
            var newPos = cart.pos + cart.direction.offset
            val currentInfra = infra[cart.pos.x][cart.pos.y]
            val nextInfra = infra[newPos.x][newPos.y]

            nextInfra.calculateNextDirection(currentInfra, cart.direction)
        }
    }

    fun print() {
        val positionToCarts = carts.groupBy { it.pos }

        infra.forEach { row ->
            row.forEach { infra ->
                val cartsAtPosition = positionToCarts[infra.pos]
                when {
                    cartsAtPosition == null -> print(infra.char)
                    cartsAtPosition.size > 1 -> print("X")
                    else -> print(cartsAtPosition.first().direction)
                }
            }
            println()
        }
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
                        else -> Nothing(Point(x, y))
                    }
                }
            }

    val track = Track(trackInfra, carts)
    track.print()
}
