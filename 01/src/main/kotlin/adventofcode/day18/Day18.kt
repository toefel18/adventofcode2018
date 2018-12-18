package adventofcode.day18

import java.io.File


fun main(args: Array<String>) {
    val input = File(ClassLoader.getSystemResource("day-18-input.txt").file)
            .readLines()
            .map { it.toCharArray() }

    part1(input)
    part2(input)
}

private fun part1(input: List<CharArray>) {
    var currentAcres = input.map { it.copyOf() }
    println("0")
    currentAcres.map { String(it) }.forEach { println(it) }

    for (i in 1..10) {
        val newAcres = currentAcres.map { it.copyOf() }

        for (y in currentAcres.indices) {
            for (x in currentAcres[y].indices) {
                val adjacent = adjacentOf(y, x, currentAcres)
                if (currentAcres[y][x] == '.' && adjacent.count { it == '|' } >= 3) {
                    newAcres[y][x] = '|'
                } else if (currentAcres[y][x] == '|' && adjacent.count { it == '#' } >= 3) {
                    newAcres[y][x] = '#'
                } else if (currentAcres[y][x] == '#') {
                    if (adjacent.count { it == '#' } >= 1 && adjacent.count { it == '|' } >= 1) {
                        newAcres[y][x] = '#'
                    } else {
                        newAcres[y][x] = '.'
                    }
                }
            }
        }

        currentAcres = newAcres
        println("$i")
        currentAcres.map { String(it) }.forEach { println(it) }

        val trees = currentAcres.sumBy { it.count { it == '|' } }
        val lumberyard = currentAcres.sumBy { it.count { it == '#' } }
        println("total resource value $trees * $lumberyard = ${trees * lumberyard}")
    }
}


private fun part2(input: List<CharArray>) {
    var currentAcres = input.map { it.copyOf() }
    println("0")
    currentAcres.map { String(it) }.forEach { println(it) }
    val valuesSeen = mutableSetOf<Int>()

    var lastValue = 0
    for (i in 1..1000000000) {
        val newAcres = currentAcres.map { it.copyOf() }

        for (y in currentAcres.indices) {
            for (x in currentAcres[y].indices) {
                val adjacent = adjacentOf(y, x, currentAcres)
                if (currentAcres[y][x] == '.' && adjacent.count { it == '|' } >= 3) {
                    newAcres[y][x] = '|'
                } else if (currentAcres[y][x] == '|' && adjacent.count { it == '#' } >= 3) {
                    newAcres[y][x] = '#'
                } else if (currentAcres[y][x] == '#') {
                    if (adjacent.count { it == '#' } >= 1 && adjacent.count { it == '|' } >= 1) {
                        newAcres[y][x] = '#'
                    } else {
                        newAcres[y][x] = '.'
                    }
                }
            }
        }

        currentAcres = newAcres

        val trees = currentAcres.sumBy { it.count { it == '|' } }
        val lumberyard = currentAcres.sumBy { it.count { it == '#' } }
        val value = trees * lumberyard
        val seen = valuesSeen.contains(value)
        println("$i total resource value $trees * $lumberyard = $value   ${lastValue - value}     $seen")
        if (i == 1000) {
            println("Pattern repeats every 28 iterations, iteration 1000 is a multiple of 1000000000 and contains the answer, break")
            break
        }
        valuesSeen.add(value)
        lastValue = value
    }
}

fun adjacentOf(y: Int, x: Int, currentAcres: List<CharArray>): List<Char> {
    val result = mutableListOf<Char>()

    for (yIndex in (y - 1..y + 1)) {
        for (xIndex in (x - 1..x + 1)) {
            if (yIndex >= 0 && yIndex < currentAcres.size) {
                if (xIndex >= 0 && xIndex < currentAcres[yIndex].size) {
                    if (!(xIndex == x && yIndex == y)) {
                        result.add(currentAcres[yIndex][xIndex])
                    }
                }
            }
        }
    }
    return result
}
