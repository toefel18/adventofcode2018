package adventofcode.day3

import java.io.File

data class Rect(val id: Int, val x: Int, val y: Int, val width: Int, val height: Int) {
    companion object {
        fun fromString(line: String): Rect {
            val parts = line.split(" @ ", "#", ",", ":", "x")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { it.toInt() }
            return Rect(parts[0], parts[1], parts[2], parts[3], parts[4])
        }
    }
}

data class Cloth(val overlaps: MutableList<Rect> = mutableListOf())

fun main(args: Array<String>) {
    val input = File(ClassLoader.getSystemResource("day-03-input.txt").file).readLines()
    val rects = input.map { Rect.fromString(it) }

    // part 1
    val fabricOverlays: MutableList<MutableList<Int>> = (0..1000).map { MutableList(1000) { 0 } }.toMutableList()
    rects.forEach { rect -> addAreaToFabric(rect, fabricOverlays) }
    println(fabricOverlays.flatten().count { it >= 2 })

    // part 2
    val fabricOverlays2: MutableList<MutableList<Cloth>> = (0..1000).map { MutableList(1000) { Cloth() } }.toMutableList()
    rects.forEach { rect -> addAreaToFabricP2(rect, fabricOverlays2) }

    val rectsWithoutOverlap = rects.toMutableSet()
    fabricOverlays2.flatten().filter { it.overlaps.size > 1 }.forEach { rectsWithoutOverlap.removeAll(it.overlaps) }
    println(rectsWithoutOverlap)
}

fun addAreaToFabric(rect: Rect, fabric: MutableList<MutableList<Int>>) {
    for (x in rect.x..(rect.x + rect.width - 1)) {
        for (y in rect.y..(rect.y + rect.height - 1)) {
            fabric[x][y] = fabric[x][y] + 1
        }
    }
}

fun addAreaToFabricP2(rect: Rect, fabric: MutableList<MutableList<Cloth>>) {
    for (x in rect.x..(rect.x + rect.width - 1)) {
        for (y in rect.y..(rect.y + rect.height - 1)) {
            fabric[x][y].overlaps.add(rect)
        }
    }
}