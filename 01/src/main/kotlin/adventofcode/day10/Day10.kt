package adventofcode.day10

import java.io.File
import kotlin.math.abs

data class Box(val min: Point,val max: Point) {
    fun area() = abs(max.x - min.x) * abs(max.y - min.y)
}
data class Vector(var pos: Point, val direction: Point)
data class Point(val x: Long, val y: Long) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)
}

data class Grid(val stars: List<Vector>) {

    fun tick() = stars.forEach { it.pos = it.pos + it.direction }

    fun box(): Box {
        val positions = stars.map { it.pos }
        val minX = positions.minBy { it.x }!!.x
        val maxX = positions.maxBy { it.x }!!.x
        val minY = positions.minBy { it.y }!!.y
        val maxY = positions.maxBy { it.y }!!.y
        return Box(Point(minX, minY), Point(maxX, maxY))
    }

    fun print() {
        val bounds = box()
        val positions = stars.map { it.pos }
        for(y in bounds.min.y..bounds.max.y) { //y first to rotate output
                for (x in bounds.min.x..bounds.max.x) {
                print(if (positions.contains(Point(x, y))) "#" else ".")
            }
            println()
        }
    }
}


fun main(args: Array<String>) {
    val regex = ".*?(-?[0-9]+).*?(-?[0-9]+).*?(-?[0-9]+).*?(-?[0-9]+)".toRegex()
    val input: List<Vector> = File(ClassLoader.getSystemResource("day-10-input.txt").file).readLines()
            .map { regex.find(it)!!.groupValues.drop(1) }
            .map { Vector(Point(it[0].toLong(), it[1].toLong()), Point(it[2].toLong(), it[3].toLong())) }

    val grid = Grid(input)
    var iteration = 0
    var boxSize = Long.MAX_VALUE

    while(true){
        val currentBox = grid.box().area()
        println("iteration ${iteration++.toString().padStart(5, '0')}  size $boxSize")

        if (currentBox < 1000) { //this point can be found by iterating until box is the smallest.
            grid.print()
        }

        if (currentBox < boxSize) {
            boxSize = currentBox
        } else {
            println("Increasing again, stopping")
            break
        }

        grid.tick()
    }

}
