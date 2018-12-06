package adventofcode.day6

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Point(val x: Int, val y: Int)
typealias Place = Point //place is a point which came as input

data class GridPoint(val pos: Point, var closestDistance: Int, var closestTo: MutableList<Place>)
data class Grid(val offsetX: Int, val offsetY: Int, val points: List<List<GridPoint>>) {
    fun forEachPoint(func: (GridPoint) -> Unit) = points.flatten().forEach { func(it) }

    fun markDistances(place: Place, distance: Distance) {
        forEachPoint { point ->
            val dist = distance(place, point.pos)
            when {
                dist == point.closestDistance -> {
                    point.closestTo.add(place)
                }
                dist < point.closestDistance -> {
                    point.closestDistance = dist
                    point.closestTo = mutableListOf(place)
                }
            }
        }
    }

    fun resetTiedDistances() {
        forEachPoint { if (it.closestTo.size > 1) it.closestTo.clear() }
    }

    fun countArea(pos: Place): Pair<Place, Int> {
        var area = 0
        forEachPoint { if (it.closestTo.contains(pos)) area++ }
        return Pair(pos, area)
    }

    fun getPlacesWithInfiniteArea(): MutableSet<Place> {
        val border = mutableListOf<GridPoint>()
        for (x in 0 until points.size) {
            border.add(points[x][0])
            border.add(points[x][points[x].size - 1])
        }

        for (y in 0 until points[0].size) {
            border.add(points[0][y])
            border.add(points[points.size - 1][y])
        }
        return border.mapNotNull { it.closestTo.firstOrNull() }.toMutableSet()
    }
}

fun createGrid(input: List<Place>): Grid {
    var (minX, maxX, minY, maxY) = listOf(Int.MAX_VALUE, Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE)
    input.forEach {
        minX = min(it.x, minX)
        maxX = max(it.x, maxX)
        minY = min(it.y, minY)
        maxY = max(it.y, maxY)
    }
    return Grid(minX, minY, List(maxX - minX) { row -> List(maxY - minY) { column -> GridPoint(Point(minX + row, minY + column), Int.MAX_VALUE, mutableListOf()) } })
}

typealias Distance = (a: Point, b: Point) -> Int

val manhattan: Distance = { a: Point, b: Point -> abs(a.x - b.x) + abs(a.y - b.y) }

fun main(args: Array<String>) {
    val input: List<Place> = File(ClassLoader.getSystemResource("day-06-input.txt").file).readLines()
            .map { Place(it.split(", ")[0].toInt(), it.split(", ")[1].toInt()) }

    // part1
    val grid = createGrid(input)
    input.forEach { place -> grid.markDistances(place, manhattan) }
    grid.resetTiedDistances()
    val edges = grid.getPlacesWithInfiniteArea()
    val placesOfInterest = input.minus(edges)

    println(placesOfInterest.map { place -> grid.countArea(place) }
            .sortedWith(compareBy({ it.second }))
            .last())

    drawImage(grid, input, placesOfInterest, "/tmp/aoc-area.png")

    // part2
    part2()
}


private fun drawImage(grid: Grid, input: List<Place>, placesOfInterest: List<Place>, location: String) {
    val img = BufferedImage(grid.points.size, grid.points[0].size, BufferedImage.TYPE_INT_RGB)
    val graphics = img.createGraphics()
    grid.forEachPoint {
        graphics.color = if (it.closestTo.isEmpty()) Color.black else Color(it.closestTo.first().x % 256, it.closestTo.first().y % 256, 255)
        graphics.drawRect(it.pos.x - grid.offsetX, it.pos.y - grid.offsetY, 1, 1)
    }
    grid.forEachPoint {
        if (!it.closestTo.isEmpty() && it.closestTo.first() == Point(149, 272)) {
            graphics.color = Color.GREEN
            graphics.drawRect(it.pos.x - grid.offsetX, it.pos.y - grid.offsetY, 1, 1)
        }
    }
    graphics.color = Color.red
    input.forEach {
        graphics.drawRect(it.x - grid.offsetX, it.y - grid.offsetY, 1, 1)
    }
    graphics.color = Color.yellow
    placesOfInterest.forEach {
        graphics.drawRect(it.x - grid.offsetX, it.y - grid.offsetY, 1, 1)
    }
    graphics.dispose()
    val file = File(location)
    ImageIO.write(img, "png", file)
}

// COPY instead of spending time to make reusable

fun part2() {
    val input: List<Place> = File(ClassLoader.getSystemResource("day-06-input.txt").file).readLines()
            .map { Place(it.split(", ")[0].toInt(), it.split(", ")[1].toInt()) }

    // part1
    val grid = createGrid2(input)
    grid.forEachPoint { point -> input.forEach { place -> point.distances.add(manhattan(point.pos, place)) } }
    var area = 0
    grid.forEachPoint { if (it.distances.sum() < 10000) area++ }
    println("\nPart2 \nTotal area $area")
}

data class GridPoint2(val pos: Point, val distances: MutableList<Int>)

data class Grid2(val offsetX: Int, val offsetY: Int, val points: List<List<GridPoint2>>) {
    fun forEachPoint(func: (GridPoint2) -> Unit) = points.flatten().forEach { func(it) }
}

fun createGrid2(input: List<Place>): Grid2 {
    var (minX, maxX, minY, maxY) = listOf(Int.MAX_VALUE, Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE)
    input.forEach {
        minX = min(it.x, minX)
        maxX = max(it.x, maxX)
        minY = min(it.y, minY)
        maxY = max(it.y, maxY)
    }
    return Grid2(minX, minY, List(maxX - minX) { row -> List(maxY - minY) { column -> GridPoint2(Point(minX + row, minY + column), mutableListOf()) } })
}

