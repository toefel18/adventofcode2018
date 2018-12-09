package adventofcode.day9

import java.io.File
import java.util.*

fun main(args: Array<String>) {
    val input = File(ClassLoader.getSystemResource("day-09-input.txt").file).readText().split(" ")
    val players = input[0].toInt()
    val lastMarble = input[6].toInt()
    val game = FastGame()
    println("game with $players players and ${lastMarble} last marble value; highscore = ${game.play(players, lastMarble)}")
    println("game with $players players and ${lastMarble * 100} last marble value; highscore = ${game.play(players, lastMarble * 100)}")
}

fun Int.isMultipleOf(other: Int) = this % other == 0
fun SortedMap<Int, Player>.next(current: Int) = if (this.containsKey(current + 1)) this[current + 1]!! else this[this.firstKey()]!!
class Player(val id: Int, var score: Long = 0)
typealias Marble = Int

class FastGame {
    fun play(playerCount: Int, lastMarble: Int): Long {
        val players = (1..playerCount).map { Pair(it, Player(it)) }.toMap().toSortedMap()
        var currentMarble: NodeRing = NodeRing.createNodeRingWith3Nodes()
        var currentPlayer = players[2]!!
        for (nextMarble in 3..lastMarble) {
            currentPlayer = players.next(currentPlayer.id)
            if (nextMarble.isMultipleOf(23)) {
                currentPlayer.score += nextMarble
                val (nextCurrent, removedMarble) = currentMarble.deleteMarble7Back()
                currentPlayer.score += removedMarble
                currentMarble = nextCurrent
            } else {
                currentMarble = currentMarble.addMarbleBetweenNextTwo(nextMarble)
            }
        }
        return players.map { it.value.score }.max()!!
    }
}

// basically a link list
data class NodeRing(val value: Marble, var next: NodeRing?, var previous: NodeRing?) {
    fun addMarbleBetweenNextTwo(value: Marble): NodeRing = next!!.insert(value)

    // returns the next current node and the marble
    fun deleteMarble7Back(): Pair<NodeRing, Marble> {
        var fastNode: NodeRing = this
        for (i in 0 until 7) fastNode = fastNode.previous!!
        return fastNode.delete()
    }

    private fun insert(value: Marble): NodeRing {
        val next = this.next
        val new = NodeRing(value, next, this)
        next!!.previous = new
        this.next = new
        return new
    }

    fun delete(): Pair<NodeRing, Marble> {
        val next = this.next!!
        val prev = this.previous!!
        next.previous = prev
        prev.next = next
        return Pair(next, value)
    }

    override fun toString(): String = "$value"

    companion object {
        fun createNodeRingWith3Nodes(): NodeRing {
            val node0 = NodeRing(0, null, null)
            val node1 = NodeRing(1, node0, null)
            val node2 = NodeRing(2, node1, node0)
            node0.next = node2
            node0.previous = node1
            node1.previous = node2
            return node0
        }
    }
}