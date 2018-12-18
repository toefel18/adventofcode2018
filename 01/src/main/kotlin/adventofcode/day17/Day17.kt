package adventofcode.day17

import java.io.File

data class Point(val x: Int, val y: Int) : Comparable<Point> {
    override fun compareTo(other: Point): Int = comparator.compare(this, other)
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
    operator fun plus(direction: Direction) = Point(x + direction.offset.x, y + direction.offset.y)

    fun down() = this + Direction.BOTTOM
    fun up() = this + Direction.TOP
    fun left() = this + Direction.LEFT
    fun right() = this + Direction.RIGHT

    companion object {
        val comparator = compareBy<Point>({ it.x }, { it.y })
    }
}

enum class Direction(val offset: Point) {
    LEFT(Point(-1, 0)),
    TOP(Point(0, -1)),
    RIGHT(Point(1, 0)),
    BOTTOM(Point(0, 1));
}

data class Matter(val pos: Point, var char: Char)
data class Box(val min: Point, val max: Point) {
    companion object {
        fun of(positions: List<Point>): Box {
            val minX = positions.minBy { it.x }!!.x
            val maxX = positions.maxBy { it.x }!!.x
            val minY = positions.minBy { it.y }!!.y
            val maxY = positions.maxBy { it.y }!!.y
            return Box(Point(minX, minY), Point(maxX, maxY))
        }
    }
}

data class Flow(val head: Point, var previous: Flow?)

data class Area(val points: List<List<Matter>>, val waterSource: Point, val bounds: Box, val boundsWithoutSource: Box) {

    fun print() {
        points.forEach { row ->
            row.forEach {
                print(it.char)
            }; println()
        }
        println()
    }

    fun at(point: Point): Matter {
        val miny = point.y - bounds.min.y
        val minx = point.x - bounds.min.x + 1
        if (minx < 0) {
            return Matter(Point(minx, miny), '.')
        }
        return points[miny][minx]
    }

    fun xOutOfBounds(point: Point): Boolean = point.x - bounds.min.x + 1 < 0

    fun startFlowing() {
        val first = waterSource.down()

        var flow = Flow(waterSource.down(), null)
        val visited = mutableSetOf<Point>()

        var iter = 0
        //recursive might not work due to stack depth
        while (true) {
            println("iteration ${iter++}")
            if (at(flow.head).char == '~') {
                flow = flow.previous!!
                continue
            }

            if (iter > 1 && flow.previous == null) {
                break
            }

            at(flow.head).char = '|'
            visited.add(flow.head)

            if (flow.head.down().y > bounds.max.y) {
                flow = flow.previous!!
                // backtrack or exit
            }

            val down = at(flow.head.down())
            if (!visited.contains(down.pos) && down.char == '.') {
                flow = Flow(down.pos, flow)
                continue
            }

            // only move left or right when water below is still or clayed
            if (!(down.char == '~' || down.char == '#')) {
                flow = flow.previous!!
                continue
            }

            val right = at(flow.head.right())
            if (!visited.contains(right.pos) && right.char == '.') {
                flow = Flow(right.pos, flow)
                continue
            }

            val left = at(flow.head.left())
            if (!visited.contains(left.pos) && left.char == '.') {
                flow = Flow(left.pos, flow)
                continue
            }

            markAsStillIfContained(flow.head)

            //backtrack
            flow = flow.previous!!
            if (flow.head == first) {
                break
            }
        }
    }

    private fun containedInDirection(current: Point, direction: Direction): Boolean {
        if (xOutOfBounds(current)) return false
        val atDir = at(current + direction)
        val down = at(current + Direction.BOTTOM)
        if (atDir.char == '#' && (down.char == '~' || down.char == '#')) return true
        if (atDir.char == '|') return containedInDirection(atDir.pos, direction)
        return false
    }

    private fun markInDirection(current: Point, direction: Direction) {
        val atDir = at(current + direction)
        if (atDir.char == '#') return
        atDir.char = '~'
        markInDirection(atDir.pos, direction)
    }

    private fun markAsStillIfContained(head: Point) {
        if (containedInDirection(head, Direction.LEFT) && containedInDirection(head, Direction.RIGHT)) {
            at(head).char = '~'
            markInDirection(head, Direction.LEFT)
            markInDirection(head, Direction.RIGHT)
        }
    }

    fun countWateredParts(): Int {
        var sum = 0
        for (row in points) {
            for (col in row) {
                if (col.pos.y >= boundsWithoutSource.min.y && col.pos.y <= boundsWithoutSource.max.y && (col.char == '~' || col.char == '|') ) {
                    sum++
                }
            }
        }

        return sum
    }

    fun countWateredPartsAtRest(): Int {
        var sum = 0
        for (row in points) {
            for (col in row) {
                if (col.pos.y >= boundsWithoutSource.min.y && col.pos.y <= boundsWithoutSource.max.y && (col.char == '~') ) {
                    sum++
                }
            }
        }

        return sum
    }
}

fun main(args: Array<String>) {
    val pointsOfClay = File(ClassLoader.getSystemResource("day-17-input.txt").file)
            .readLines()
            .flatMap { expandToPoints(it) }

    val waterSource = Point(500, 0)
    val boundsWithoutWaterSource = Box.of(pointsOfClay)
    val bounds = Box.of(pointsOfClay + waterSource)



    val area = Area(
            (bounds.min.y..bounds.max.y).map { y ->
                (bounds.min.x - 1..bounds.max.x + 10).map { x ->
                    val point = Point(x, y)
                    Matter(point, if (pointsOfClay.contains(point)) {
                        '#'
                    } else if (point == waterSource) {
                        '+'
                    } else {
                        '.'
                    })
                }
            }, waterSource, bounds, boundsWithoutWaterSource)

    area.startFlowing()

    area.print()
    println("Number of parts under water = ${area.countWateredParts()}  parts at rest ${area.countWateredPartsAtRest()}")

    Thread.sleep(100)
//    input
}

fun expandToPoints(it: String): List<Point> {
    val (firstAxisAndRange, secondAxisAndRange) = it.split(", ")
    val (firstAxis, firstRangeSpec) = firstAxisAndRange.split("=")
    val (_, secondRangeSpec) = secondAxisAndRange.split("=")

    val firstRange = toRange(firstRangeSpec)
    val secondRange = toRange(secondRangeSpec)

    return if (firstAxis == "x") {
        firstRange.flatMap { x -> secondRange.map { y -> Point(x, y) } }
    } else {
        secondRange.flatMap { x -> firstRange.map { y -> Point(x, y) } }
    }
}

fun toRange(spec: String): IntRange = if (spec.contains(".")) {
    val (from, to) = spec.split("..")
    from.toInt()..to.toInt()
} else {
    spec.toInt()..spec.toInt()
}
