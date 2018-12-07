package adventofcode.day6

import java.io.File
import java.lang.StringBuilder

class Vertex(val name: String, val next: MutableList<Vertex> = mutableListOf(), val previous: MutableList<Vertex> = mutableListOf()) : Comparable<Vertex> {
    override fun compareTo(other: Vertex): Int = name.compareTo(other.name)
    override fun toString(): String = "Vertex $name"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Vertex
        if (name != other.name) return false
        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    fun duration() = 61 + (name.first() - 'A')
}

fun main(args: Array<String>) {
    val vertexMatcher = ".*([A-Z]).*([A-Z]).*".toRegex()
    val input: List<String> = File(ClassLoader.getSystemResource("day-07-input.txt").file).readLines()
    val vertices = input.flatMap { line -> vertexMatcher.matchEntire(line)!!.groupValues.drop(1) }
            .distinct()
            .map { it to Vertex(it) }
            .toMap()

    input.forEach { line ->
        val (first, dependsOnFirst) = vertexMatcher.matchEntire(line)!!.groupValues.drop(1)
        vertices[first]!!.next.add(vertices[dependsOnFirst]!!)
    }

    // compute reverse
    vertices.values.forEach { vertex -> vertex.next.forEach { it.previous.add(vertex) } }
    // print adjacency list
    vertices.values.forEach { println("${it.name} -> ${it.next.map { it.name }}   <-   ${it.previous.map { it.name }}}") }

    val startVertices = vertices.values.filter { it.previous.isEmpty() }
    val verticesToProcess = startVertices.toSortedSet()
    val resultOrder = StringBuilder()

    while (verticesToProcess.isNotEmpty()) {
        val current = verticesToProcess.find { v -> nextWithoutPreviousPending(resultOrder, v) }!!
        resultOrder.append(current.name)
        verticesToProcess.remove(current)
        current.next.filter { !resultOrder.contains(it.name) }.forEach { verticesToProcess.add(it) }
    }

    println(resultOrder.toString())

    // part 2

    //reset state
    resultOrder.clear()
    val remaining = startVertices.toSortedSet()
    val workers = Workers()
    var currentSecond = 0

    while (remaining.isNotEmpty() || workers.notFinished()) {
        val availableVertices = remaining
                .filter { nextWithoutPreviousPending(resultOrder, it) }
                .filter { !workers.verticesInProgress().contains(it) }
                .toSortedSet()

        // assign available vertices in-order
        val availableWorkers = workers.availableWorkers()
        availableVertices.zip(availableWorkers).forEach { (vertex, worker) ->
            println("Worker ${worker.id} assiged ${vertex.name} on second $currentSecond")
            worker.assign(vertex)
         }

        currentSecond++
        workers.tick()

        workers.finishedWorkers().sortedWith(compareBy { it.current!!.name }).forEach { worker ->
            val finishedVertex = worker.current!!
            println("Worker ${worker.id} finished working on ${finishedVertex.name} on second $currentSecond")

            resultOrder.append(finishedVertex.name)
            remaining.addAll(finishedVertex.next)
            remaining.remove(finishedVertex)
            worker.reset()
        }
    }

    println("${resultOrder} processed in $currentSecond seconds")
}

data class Workers(val workers: List<Worker> = List(5) { idx -> Worker(null, 0, idx.toString()) }) {
    fun notFinished(): Boolean = workers.maxBy { it.secondsRemaining }!!.secondsRemaining > 0
    fun availableWorkers() = workers.filter { it.current == null }
    fun verticesInProgress() = workers.flatMap { listOfNotNull(it.current) }
    fun finishedWorkers() = workers.filter { it.current != null && it.secondsRemaining == 0 }
    fun tick() = workers.filter { it.current != null }.forEach { it.secondsRemaining = it.secondsRemaining - 1 }
}

data class Worker(var current: Vertex?, var secondsRemaining: Int, val id: String) {
    fun assign(vertex: Vertex) {
        this.current = vertex
        this.secondsRemaining = vertex.duration()
    }

    fun reset() {
        current = null
        secondsRemaining = 0
    }
}

fun nextWithoutPreviousPending(resultOrder: StringBuilder, it: Vertex) =
        it.previous.isEmpty() || it.previous.filter { !resultOrder.contains(it.name) }.isEmpty()