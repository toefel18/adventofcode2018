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
                val nextPos = if (creature is Elf) calculateNextPos(creature) else calculateNextPos(creature)
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

        return if (path == null) {
            creature.pos
        } else {
            creature.pos + path.stepsTaken.first().offset
        }
    }

    fun print() {
        map.print(adjacentSpots(Goblin::class), adjacentSpots(Elf::class))
        println("Round $round")
        creaturesByPositionSorted().forEach { pos, creature -> println("${creature.logo} ${creature.health.toString().padStart(3, ' ')} row=${creature.pos.x} col=${creature.pos.y} ") }
    }
}

data class Path(val finalPos: Point, val stepsTaken: List<Direction>)

object Dijkstra {
    fun getPathByClosestTargetInReadingOrder(start: Point, targets: List<Point>, map: Map): Path? {
        val time = System.currentTimeMillis()
        try {
            val paths = targets
                    .mapNotNull { shortestPathInReadingOrder(start, it, map) }
                    .sortedWith(compareBy({it.stepsTaken.size}, { it.finalPos }))

//        println("winner $paths")
            return paths.firstOrNull()
        } finally {
//            println("${System.currentTimeMillis() - time}")
        }
    }

    fun shortestPathInReadingOrder(start: Point, target: Point, map: Map): Path? {

        // maps the visited points and what the shortestPath to that point was
        // maps visited points to shortest distance
        val visited = mutableMapOf(start to 0)
        val queue = PriorityQueue<Path>(compareBy { it.stepsTaken.size }) //val queue = PriorityQueue<Path>(compareBy { abs(target.x - it.finalPos.x ) + abs(target.y-it.finalPos.y)})
        queue.addAll(Direction.values().map { Path(start + it.offset, listOf(it)) }.filter { map.spotAt(it.finalPos).available() })
        queue.forEach { visited[it.finalPos] = 1 }

        val candiateResults: MutableList<Path> = mutableListOf()
        while (!queue.isEmpty()) {
            val currentPath = queue.poll()
            if (currentPath.finalPos == target) {
                candiateResults.add(currentPath)
                continue
            }

            if (candiateResults.isNotEmpty()) {
                if (currentPath.stepsTaken.size > candiateResults.first().stepsTaken.size) {
                    break
                }
            }
//
//            if (candiateResults.size > 10) {
//                break
//            }

            // calculate new directions
            val newPaths = Direction.values()
                    // Pair because we need to carry 2 values
                    .map { Pair(currentPath.finalPos + it.offset, it) }
//                    .map { println(notVisitedOrSameDistance(currentPath, it.first, visited) ); it }
//                    .filter { visited.containsKey(it.first) }
                    .filter { notVisitedOrSameDistance(currentPath, it.first, visited) }
                    .filter { map.spotAt(it.first).available() }
                    .map { Path(it.first, currentPath.stepsTaken + it.second) }

            // add new directions to visited with the number of steps it took.
            // all distances should be the same or smaller (due to the filter notVisitedOrSameDistance)
            newPaths.forEach { visited[it.finalPos] = it.stepsTaken.size }
            println("${visited.size}")
            queue.addAll(newPaths)
        }

        val shortestPathSize = candiateResults.map { it.stepsTaken.size }.min()
        println("$start $target ${candiateResults.size}")
        val actualResults = candiateResults
                .filter { it.stepsTaken.size == shortestPathSize } //select the shortest
                .sortedBy { start + it.stepsTaken.first().offset } // sort by reading order
        actualResults.forEach { println("   $it") }
        println("visited ${visited.keys.size}")
//        println("Selected-to-point ${actualResults.firstOrNull()}")

        return actualResults.firstOrNull()
    }

    private fun notVisitedOrSameDistance(path: Path, point: Point, visited: MutableMap<Point, Int>): Boolean {
        val distance = visited[point]
        if (distance == null) {
            return true // not visited
        } else {
            val newPathLength = path.stepsTaken.size + 1 //+1 to reach the current point
            return newPathLength == distance
        }
    }
}


fun main(args: Array<String>) {
//    playGame("day-15-test-input.txt", 28944)
    pathTest()
//    playGame("day-15-test-input2-37-982-36334.txt",36334)
//    playGame("day-15-test-input3-46-859-39514.txt",39514)
//    playGame("day-15-test-input4-35-793-27755.txt",27755)
//    playGame("day-15-test-input5-54-536-28944.txt",28944)
//    playGame("day-15-test-input6-20-937-18740.txt",18740)
//    playGame("day-15-input.txt",0)
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
    while (game.enemiesAlive()) {
        game.tick()
        game.print()
//        println("round ${game.round}")
//        if (game.round > 1000) {
//            game.print()
//            break
//        }
    }
    game.round--
    val totalHealth = creatures.filter { !it.dead() }.map { it.health }.sum()
    val score = totalHealth * game.round
    game.print()
    println("$resourceName score = $score  (succeeded = ${score == expectedOutcome})   rounds=${game.round} totalHealth=${totalHealth}")
}

fun pathTest() {
    val input: List<List<Spot>> = File(ClassLoader.getSystemResource("day-15-path-test.txt").file)
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

    println(Dijkstra.shortestPathInReadingOrder(Point(1,1), Point(4,7), game.map))
//    game.print()
//
//    val source = game.creaturesByPositionSorted().iterator().next().key!!
//    val targets = game.adjacentSpots(Elf::class).map { it.pos }
//    println("source $source")
//    println("targets $targets")
//    val chosenPath = Dijkstra.getPathByClosestTargetInReadingOrder(source, targets, game.map)
//    println(chosenPath)
}
