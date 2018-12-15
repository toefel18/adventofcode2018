package adventofcode.day11

import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executors


data class Point(val x: Int, val y: Int)
data class PointPowerSize(val power: Long, val x: Int, val y: Int, val size: Int)

fun main(args: Array<String>) {
    val gridSerialNumber = 7403
    val width = 300
    val height = 300
    val grid: List<List<Long>> = List(width) { x ->
        List(height) { y ->
            val rackId = x + 1 + 10.toLong()  // x indexed at 0 but exercise requires 1
            var powerLevel = rackId * (y + 1) // y indexed at 0 but exercise requires 1
            powerLevel += gridSerialNumber
            powerLevel *= rackId
            powerLevel %= 1000
            powerLevel /= 100
            powerLevel -= 5
            powerLevel
        }
    }

    var highestValue: Long = Long.MIN_VALUE
    var coord = Point(-1, -1)

    for (x in 0..(grid.size - 3)) {
        for (y in 0..(grid[x].size - 3)) {
            val power = (x..x + 2).flatMap { currentX -> (y..y + 2).map { currentY -> grid[currentX][currentY] } }.sum()
            if (power > highestValue) {
                highestValue = power
                coord = Point(x + 1, y + 1)
            }
        }
    }
    println("$highestValue, $coord")
    println("${findGridWithMaxPower(3, grid)}")

    // part2
    val start = Instant.now()
    println("max power ${findGridWithMaxPowerForAllSizes(grid)}")
    println("took ${Duration.between(start, Instant.now()).seconds} seconds")
}

fun findGridWithMaxPowerForAllSizes(grid: List<List<Long>>): PointPowerSize {
    val executor = Executors.newFixedThreadPool(8)
    return (1..300)
            .map { size -> executor.submit<PointPowerSize>{ findGridWithMaxPower(size, grid) }}
            .map { it.get() }
            .maxBy { it.power }!!
}

fun findGridWithMaxPower(size: Int, grid: List<List<Long>>): PointPowerSize {
    println("started $size")
    var highestValue: Long = Long.MIN_VALUE
    var highestPowerCoord = PointPowerSize(Long.MIN_VALUE, -1, -1, 0)
    for (x in 0..(grid.size - size - 1)) {
        for (y in 0..(grid[x].size - size - 1)) {
            val power = (x until (x + size)).flatMap { xCoord -> (y until (y + size)).map { yCoord -> grid[xCoord][yCoord] } }.sum()
            if (power > highestValue) {
                highestValue = power
                highestPowerCoord = PointPowerSize(power, x + 1, y + 1, size)
            }
        }
    }
    println("finished $size = $highestPowerCoord")
    return highestPowerCoord
}
