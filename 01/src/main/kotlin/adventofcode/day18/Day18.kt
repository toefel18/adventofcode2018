package adventofcode.day18

import java.io.File


fun main(args: Array<String>) {
    val input = File(ClassLoader.getSystemResource("day-18-input.txt").file)
            .readLines()
            .map { it.toCharArray() }

    var currentAcres = input
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

        val trees = currentAcres.sumBy{ it.count{ it == '|'} }
        val lumberyard = currentAcres.sumBy{ it.count{ it == '#'} }
        println("total resource value $trees * $lumberyard = ${trees * lumberyard}")

//        if (i % 100000 == 0) {
//            println("${(i.toDouble() / 1000000000)* 100}% ")
//            currentAcres.map { String(it) }.forEach { println(it) }
//
//            val trees = currentAcres.sumBy{ it.count{ it == '|'} }
//            val lumberyard = currentAcres.sumBy{ it.count{ it == '#'} }
//            println("total resource value $trees * $lumberyard = ${trees * lumberyard}")
//        }
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
