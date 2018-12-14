package adventofcode.day14

import java.lang.StringBuilder

fun StringBuilder.nextIndex(current: Int, offset: Int): Int = if (current + offset >= this.length) nextIndex(0, current + offset % this.length) else current + offset

fun main(args: Array<String>) {
    val searchingForScoreAfter = 330121
    var scores = StringBuilder("37")

    var currentRecipe1 = 0
    var currentRecipe2 = 1

    while (scores.length < searchingForScoreAfter + 11) {
        val currentScore1 = scores[currentRecipe1].toString().toInt()
        val currentScore2 = scores[currentRecipe2].toString().toInt()
        val newScore = currentScore1 + currentScore2
        scores.append(newScore)
        currentRecipe1 = scores.nextIndex(currentRecipe1, 1 + currentScore1)
        currentRecipe2 = scores.nextIndex(currentRecipe2, 1 + currentScore2)

    }

    // part1
    println(scores.substring(searchingForScoreAfter, searchingForScoreAfter + 10))

    // part2
    scores = StringBuilder("37")
    currentRecipe1 = 0
    currentRecipe2 = 1
    val sequence = searchingForScoreAfter.toString()
    while (true) {
        val currentScore1 = scores[currentRecipe1].toString().toInt()
        val currentScore2 = scores[currentRecipe2].toString().toInt()
        val newScore = currentScore1 + currentScore2
        scores.append(newScore)
        if (scores.length > 6 && scores.indexOf(sequence, scores.length - 10) > 0) {
            println(scores.indexOf(sequence))
            return
        }
        currentRecipe1 = scores.nextIndex(currentRecipe1, 1 + currentScore1)
        currentRecipe2 = scores.nextIndex(currentRecipe2, 1 + currentScore2)
    }
}


