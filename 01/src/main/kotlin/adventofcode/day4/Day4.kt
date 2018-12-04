package adventofcode.day4

import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class SleepRithem(val id: String, val start: LocalDateTime, val end: LocalDateTime) {
    fun durationInMinutes() = Duration.between(start, end).toMinutes()
}

fun main(args: Array<String>) {
    val input = File(ClassLoader.getSystemResource("day-04-input.txt").file).readLines()
    val sortedInput = input.sortedWith(compareBy { it.take(19) })
    val sleepRithems = parse(sortedInput)
    val rithemsByGuard = sleepRithems.groupBy { it.id }
    val guardToHoursAsleep = rithemsByGuard.mapValues { it.value.map { it.durationInMinutes() }.sum() }
    val idOfGuardMostAsleep = guardToHoursAsleep.toList().sortedWith(compareBy { it.second }).last().first

    val xTimesAsleep = mutableMapOf<String, Int>()
    rithemsByGuard[idOfGuardMostAsleep]!!.forEach { sleepPeriod ->
        val minutesAsleep = Duration.between(sleepPeriod.start, sleepPeriod.end).toMinutes()
        (0 until minutesAsleep).forEach {
            val ts = sleepPeriod.start.plusMinutes(it)
            val timeAsleep = ts.format(DateTimeFormatter.ofPattern("HH:mm"))
            xTimesAsleep[timeAsleep] = xTimesAsleep.getOrDefault(timeAsleep, 0) + 1
        }
    }

    val timeMostAsleep = xTimesAsleep.toList().sortedWith(compareBy({ it.second })).last().first.substring(3)
    println("$idOfGuardMostAsleep x $timeMostAsleep = ${idOfGuardMostAsleep.toLong() * timeMostAsleep.toLong()}")

    //  part 2

    // contains "id x hourMostAsleep = multiplicationresult", value is hourMostAsleep (for sorting)
    val results: MutableList<Triple<String, Long, Long>> = mutableListOf()
    for (idOfGuard in rithemsByGuard.keys) {
        val timesMostAsleepByGuard = mutableMapOf<String, Int>()
        rithemsByGuard[idOfGuard]!!.forEach { sleepPeriod ->
            val minutesAsleep = Duration.between(sleepPeriod.start, sleepPeriod.end).toMinutes()
            (0 until minutesAsleep).forEach {
                val ts = sleepPeriod.start.plusMinutes(it)
                val timeAsleep = ts.format(DateTimeFormatter.ofPattern("HH:mm"))
                timesMostAsleepByGuard[timeAsleep] = timesMostAsleepByGuard.getOrDefault(timeAsleep, 0) + 1
            }
        }

        val mostAsleep = timesMostAsleepByGuard.toList().sortedWith(compareBy({ it.second })).last()

        val minute = mostAsleep.first.substring(3).toLong()
        val occurrences = mostAsleep.second

        val multiplyResult = idOfGuard.toLong() * minute
        val key = "$idOfGuard x $minute (occurred $occurrences times) = $multiplyResult"
        results.add(Triple(key, minute, occurrences.toLong()))
    }
    println("")
//    results.sortedWith(compareBy({ it.third })).forEach { println(it) }
    println(results.sortedWith(compareBy({ it.third })).last().first)
}

private fun parse(sorted: List<String>): MutableList<SleepRithem> {
    val sleepRithem = mutableListOf<SleepRithem>()
    var currentGuard = ""
    var startedFallingAsleep: LocalDateTime = LocalDateTime.MIN

    for (line in sorted) {
        when {
            line.endsWith("begins shift") -> currentGuard = extractGuardId(line)
            line.endsWith("falls asleep") -> startedFallingAsleep = extractDateTime(line)
            line.endsWith("wakes up") -> sleepRithem.add(SleepRithem(currentGuard, startedFallingAsleep, extractDateTime(line)))
        }
    }

    return sleepRithem
}

fun extractDateTime(line: String): LocalDateTime = LocalDateTime.parse(line.drop(1).takeWhile { it != ']' }.replace(' ', 'T'))
fun extractGuardId(line: String) = line.drop(26).takeWhile { it != ' ' }