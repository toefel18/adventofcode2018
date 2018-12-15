package adventofcode.day15

import java.io.File
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.math.max
import kotlin.reflect.KClass

data class Point(val x: Int, val y: Int) : Comparable<Point> {
    override fun compareTo(other: Point): Int = comparator.compare(this, other)
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)

    companion object {
        val comparator = compareBy<Point>({ it.x }, { it.y })
    }
}

enum class Direction(val offset: Point) {
    LEFT(Point(0, -1)),
    TOP(Point(-1, 0)),
    RIGHT(Point(0, 1)),
    BOTTOM(Point(1, 0));
}

// types of players
abstract class Creature(var pos: Point, val logo: Char, val attackPower: Int = 3, var health: Int = 200) {
    override fun toString() = "$logo"
    fun attack(enemyInRange: Creature) {
        enemyInRange.health = max(enemyInRange.health - attackPower, 0)
    }

    fun dead() = health == 0
    abstract fun enemy(): KClass<out Creature>
}

class Goblin(pos: Point) : Creature(pos, 'G') {
    override fun enemy(): KClass<out Creature> = Elf::class
}

class Elf(pos: Point) : Creature(pos, 'E') {
    override fun enemy(): KClass<out Creature> = Goblin::class
}

// Types on map
abstract class Spot(val pos: Point, val logo: Char) {
    override fun toString() = "$logo"
    abstract fun available(): Boolean
}

class Wall(pos: Point) : Spot(pos, '#') {
    override fun available(): Boolean = false
}

class Floor(pos: Point, var creature: Creature? = null) : Spot(pos, '.') {
    override fun available(): Boolean = creature == null
    override fun toString() = (creature?.logo ?: logo).toString()
}

class Map(val spots: List<List<Spot>>) {
    fun spotAt(point: Point) = spots[point.x][point.y]
    fun spotAt(point: Point, direction: Direction): Spot {
        val newPoint = point + direction.offset
        return spots[newPoint.x][newPoint.y]
    }

    fun print(adjacentSpots: List<Spot>, adjacentSpots1: List<Spot>) {
        spots.forEach { row ->
            row.forEach { spot ->
                run {
//                    if (adjacentSpots.contains(spot)) {
//                        print("0")
//                    } else if (adjacentSpots1.contains(spot)) {
//                        print("X")
//                    } else {
                        print(spot.toString())
//                    }
                }
            }; println()
        }
    }
}

class Game(val map: Map, val creatures: MutableList<Creature>, var round: Int = 0) {
    fun creaturesByPositionSorted(): SortedMap<Point, Creature> = creatures
            .filter { !it.dead() }
            .map { it.pos to it }
            .toMap(TreeMap())

    fun adjacentSpots(type: KClass<out Creature>): List<Spot> = creatures
            .filter { type.isInstance(it) } //only take goblins or elves
            .flatMap { creature -> Direction.values().map { direction -> map.spotAt(creature.pos, direction) } }
            .filter { it.available() }

    fun enemiesInRange(creature: Creature, creaturesByPos: SortedMap<Point, Creature>) = Direction.values()
            .map { creature.pos + it.offset } // get points in all directions
            .mapNotNull { creaturesByPos[it] } // get all creatures at those positions
            .filter { !creature::class.isInstance(it) } //get all enemies

    val weakestEnemyComparator = compareBy<Creature>({ it.health }, { it.pos })

    private fun selectWeakest(enemiesInRange: List<Creature>): Creature? = enemiesInRange
            .sortedWith(weakestEnemyComparator)
            .firstOrNull()

    fun enemiesAlive() = creatures.filter { !it.dead() }.map { it.logo }.containsAll(listOf('G', 'E'))

    fun tick() {
        round++

        for (creature in creatures.toList()) {
            if (creature.dead()) continue
            val creaturesByPosSorted = creaturesByPositionSorted()
            var enemyInRange: Creature? = selectWeakest(enemiesInRange(creature, creaturesByPosSorted))
            if (enemyInRange != null) {
                creature.attack(enemyInRange)
                removeIfDead(creature)
                removeIfDead(enemyInRange)
            } else {
                val nextPos = calculateNextPos(creature)
                if (nextPos != null) {
                    // do the move
                    (map.spotAt(creature.pos) as Floor).creature = null
                    (map.spotAt(nextPos) as Floor).creature = creature
                    creature.pos = nextPos

                    enemyInRange = selectWeakest(enemiesInRange(creature, creaturesByPosSorted))
                    if (enemyInRange != null) {
                        creature.attack(enemyInRange)
                        removeIfDead(creature)
                        removeIfDead(enemyInRange)
                    }
                }
            }
        }
    }

    fun removeIfDead(creature:Creature) {
        if (creature.dead()) {
            (map.spotAt(creature.pos) as Floor).creature = null
            creatures.remove(creature)
        }
    }

    private fun calculateNextPos(creature: Creature):Point? {
        val adjacentSpotsEnemies = adjacentSpots(type = creature.enemy())
        val paths = DijkstraShortestPath.getPaths(creature.pos, adjacentSpotsEnemies.map { it.pos }, map)
        val nextPos = paths
                .map { creature.pos + it.stepsTaken.first().offset } // take points after first offsets
                .sortedWith(Point.comparator) // reading order
                .firstOrNull()
        return nextPos
    }

    fun print() {
        map.print(adjacentSpots(Goblin::class), adjacentSpots(Elf::class))
        println("Round $round")
        creaturesByPositionSorted().forEach { pos, creature -> println("${creature.logo} ${creature.health.toString().padStart(3, ' ')} row=${creature.pos.x} col=${creature.pos.y} ") }
    }
}

data class Path(val pos: Point, val stepsTaken: List<Direction>)

object DijkstraShortestPath {
    fun getPaths(start: Point, targets: List<Point>, map: Map): List<Path> {
        val time = System.currentTimeMillis()
        println("started")
        try {
            val paths = targets
                    .flatMap { calculateShortestPaths(start, it, map) }
                    .sortedBy { it.stepsTaken.size }

            println("${paths.size}")
            if (!paths.isEmpty()) {
                val sizeShortest = paths.first().stepsTaken.size
                return paths.takeWhile { it.stepsTaken.size == sizeShortest }
            }
            return emptyList()
        } finally {
            println("${System.currentTimeMillis() - time}")
        }
    }

    fun calculateShortestPaths(start: Point, target: Point, map: Map): List<Path> {

            // maps the visited points and what the shortestPath to that point was
            val visited = mutableMapOf(start to 0)
            val queue = LinkedList<Path>()
            queue.addAll(Direction.values().map { Path(start + it.offset, listOf(it)) }.filter { map.spotAt(it.pos).available() })
            queue.forEach { visited[it.pos] = 1 }

            val results: MutableList<Path> = mutableListOf()
            var itemsAfterShortest = 0
            val startz =  System.currentTimeMillis()
            while (!queue.isEmpty()) {
                val currentPath = queue.pollFirst()
                if (currentPath.pos == target) {
                    results.add(currentPath)
                    continue
                }
                if (currentPath.stepsTaken.size > results.firstOrNull()?.stepsTaken?.size ?: Integer.MAX_VALUE) {
                    itemsAfterShortest++
                }
                if (itemsAfterShortest > 20) {
                    break
                }

                // calculate new directions
                val newPaths = Direction.values()
                        // Pair because we need to carry 2 values
                        .map { Pair(currentPath.pos + it.offset, it) }
                        // skip if the next node is already visited by a SHORTER path, if the visited path is the
                        // same length or longer, keep going
//                    .map { println(visited.getOrDefault(it.first, currentPath.stepsTaken.size) >= currentPath.stepsTaken.size); it }
                        .filter { visited.getOrDefault(it.first, currentPath.stepsTaken.size) >= currentPath.stepsTaken.size }
                        // only take spots if available
                        .filter { map.spotAt(it.first).available() }
                        .map { Path(it.first, currentPath.stepsTaken + it.second) }

                // add new directions to visited with the number of steps it took.
                // this should not overwrite smaller entries because of the filter in the step above
                newPaths.forEach { visited[it.pos] = it.stepsTaken.size }

                queue.addAll(newPaths)
            }
            println("${System.currentTimeMillis() - startz}")
            return results.sortedBy { it.stepsTaken.size }
    }
}

fun main(args: Array<String>) {
    val input: List<List<Spot>> = File(ClassLoader.getSystemResource("day-15-input.txt").file)
            .readLines()
            .mapIndexed { x, row ->
                row.mapIndexed { y, char ->
                    when (char) {
                        '#' -> Wall(Point(x, y))
                        '.' -> Floor(Point(x, y))
                        'E' -> Floor(Point(x, y), Elf(Point(x, y)))
                        'G' -> Floor(Point(x, y), Goblin(Point(x, y)))
                        else -> throw IllegalArgumentException("invalid input, unknown char $char at $x,$y")
                    }
                }
            }

    val creatures = input
            .flatMap { row -> row.mapNotNull { if (it is Floor) it.creature else null } }
            .toMutableList()

    val map = Map(input)
    val game = Game(map, creatures)
    game.print()
    while(game.enemiesAlive()) {
        game.tick()
        game.print()
    }
    val totalHealth = creatures.filter { !it.dead() }.map { it.health }.sum()
    println("score = ${game.round} * ${totalHealth -3} = ${game.round * (totalHealth-3)}")





}
