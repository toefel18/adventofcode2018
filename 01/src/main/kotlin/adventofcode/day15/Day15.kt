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

    fun print() {
        spots.forEach { row -> row.forEach { spot -> print(spot.toString()) }; println() }
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
            .map { creature.pos + it.offset } // get points in all byName
            .mapNotNull { creaturesByPos[it] } // get all creatures at those positions
            .filter { !creature::class.isInstance(it) } //get all enemies

    val weakestEnemyComparator = compareBy<Creature>({ it.health }, { it.pos })

    private fun selectWeakest(enemiesInRange: List<Creature>): Creature? = enemiesInRange
            .sortedWith(weakestEnemyComparator)
            .firstOrNull()

    fun enemiesAlive() = creatures.filter { !it.dead() }.map { it.logo }.containsAll(listOf('G', 'E'))

    fun tick() {
        round++
        val ordered = creaturesByPositionSorted()
        for ((_, creature) in ordered) {
            if (creature.dead()) continue
            if (!enemiesAlive()) break
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

    fun removeIfDead(creature: Creature) {
        if (creature.dead()) {
            (map.spotAt(creature.pos) as Floor).creature = null
            creatures.remove(creature)
        }
    }

    private fun calculateNextPos(creature: Creature): Point? {
        val adjacentSpotsEnemies: List<Spot> = adjacentSpots(creature.enemy())
        val path = Dijkstra.getPathByClosestTargetInReadingOrder(creature.pos, adjacentSpotsEnemies.map { it.pos }, map)
        return path?.stepsTaken?.firstOrNull()
    }

    fun print() {
        map.print()
        println("Round $round")
        creaturesByPositionSorted().forEach { pos, creature -> println("${creature.logo} ${creature.health.toString().padStart(3, ' ')} row=${creature.pos.x} col=${creature.pos.y} ") }
    }
}

data class Path(val stepsTaken: List<Point>) {
    val length: Int
        get() = stepsTaken.size
}

object Dijkstra {
    fun getPathByClosestTargetInReadingOrder(start: Point, targets: List<Point>, map: Map): Path? {
        val paths = targets
                .mapNotNull { shortestPathInReadingOrder(start, it, map) }
                .sortedWith(compareBy({ it.stepsTaken.size }, { it.stepsTaken.last() }))
        return paths.firstOrNull()
    }

    fun shortestPathInReadingOrder(start: Point, target: Point, map: Map): Path? {
        val visited: MutableMap<Point, Path> = mutableMapOf(start to Path(listOf()))
        val queue = PriorityQueue<Path>(compareBy { it.length }) //val queue = PriorityQueue<Path>(compareBy { abs(target.x - it.finalPos.x ) + abs(target.y-it.finalPos.y)})
        queue.addAll(Direction.values()
                .map { Path(listOf(start + it.offset)) }
                .filter { map.spotAt(it.stepsTaken.first()).available() })

        queue.forEach { visited[it.stepsTaken.first()] = it }

        var shortestPathInReadingOrder: Path? = null
        while (!queue.isEmpty()) {
            val currentPath = queue.poll()
            if (currentPath.stepsTaken.last() == target) {
                if (shortestPathInReadingOrder == null) {
                    shortestPathInReadingOrder = currentPath
                } else if (currentPath.stepsTaken.first() < shortestPathInReadingOrder.stepsTaken.first()) {
                    shortestPathInReadingOrder = currentPath
                }
                continue
            }

            if (shortestPathInReadingOrder != null && shortestPathInReadingOrder.stepsTaken.size < currentPath.stepsTaken.size) {
                // current paths length is longer than shortest path, and we process them by length, so
                break
            }

            // calculate new byName
            val newPaths = Direction.values()
                    .map { currentPath.stepsTaken.last() + it.offset }
                    .filter { notVisitedOrSameDistance(it, currentPath, visited) }
                    .filter { map.spotAt(it).available() }
                    .map { Path(currentPath.stepsTaken + it) }

            // add new byName to visited with the number of steps it took.
            // all distances should be the same or smaller (due to the filter notVisitedOrSameDistance)
            newPaths.forEach { visited[it.stepsTaken.last()] = it }
            queue.addAll(newPaths)
        }

        return shortestPathInReadingOrder
    }

    private fun notVisitedOrSameDistance(point: Point, currentPath: Path, visited: MutableMap<Point, Path>): Boolean {
        val visitedBy = visited[point]
        val newPathLenght = currentPath.length + 1
        return if (visitedBy == null || newPathLenght < visitedBy.length) {
            true // not visited or shorter path (unlikely)
        } else {
            // only consider next route if length is the same, and new path's first step is first in reading order
            newPathLenght == visitedBy.length && currentPath.stepsTaken.first() < visitedBy.stepsTaken.first()
        }
    }
}


fun main(args: Array<String>) {
//    playGame("day-15-test-input1-47-590-27730.txt", 27730) //this fails with 1 round difference
//    playGame("day-15-test-input2-37-982-36334.txt", 36334)
//    playGame("day-15-test-input3-46-859-39514.txt", 39514)
//    playGame("day-15-test-input4-35-793-27755.txt", 27755)
//    playGame("day-15-test-input5-54-536-28944.txt", 28944)
//    playGame("day-15-test-input6-20-937-18740.txt", 18740)
    playGame("day-15-input.txt", 261855)
}

private fun playGame(resourceName: String, expectedOutcome: Int) {
    val input: List<List<Spot>> = File(ClassLoader.getSystemResource(resourceName).file)
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
    while (game.enemiesAlive()) {
        game.tick()
    }
    game.round--
    val totalHealth = creatures.filter { !it.dead() }.map { it.health }.sum()
    val score = totalHealth * game.round
    game.print()
    println("$resourceName score = $score  (succeeded = ${score == expectedOutcome})   rounds=${game.round} totalHealth=${totalHealth}")
}
