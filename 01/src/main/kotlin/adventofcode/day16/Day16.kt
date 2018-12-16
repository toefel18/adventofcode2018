package adventofcode.day16

import java.io.File

data class InputFrame(val inputBefore: String, val inputOpcode: String, val inputAfter: String) {
    fun before() : MutableMap<Int, Int> = parse(inputBefore)
    fun encodedOpcode(): List<Int> = numberRegex.find(inputOpcode)!!.groupValues.drop(1).map { it.toInt() }
    fun after() : MutableMap<Int, Int> = parse(inputAfter)

    private fun parse(line: String) : MutableMap<Int, Int> {
        return numberRegex.find(line)!!.groupValues.drop(1)
                .mapIndexed { index, value -> index to value.toInt() }
                .toMap().toMutableMap()
    }
    companion object {
        val numberRegex = ".*(\\d+).*(\\d+).*(\\d+).*(\\d+).*".toRegex()
        fun next(input: List<String>): InputFrame {
            val newFrame = InputFrame(input[0], input[1], input[2])
            return newFrame
        }
    }
}

open class Instruction(val name: String, val aRegister: Boolean, val bRegister: Boolean, val op: (Int, Int) -> Int)
object Addr : Instruction("addr", true, true, { a, b -> a + b })
object Addi : Instruction("addi", true, false, { a, b -> a + b })
object Mulr : Instruction("mulr", true, true, { a, b -> a * b })
object Muli : Instruction("muli", true, false, { a, b -> a * b })
object Banr : Instruction("banr", true, true, { a, b -> a and b })
object Bani : Instruction("bani", true, false, { a, b -> a and b })
object Borr : Instruction("borr", true, true, { a, b -> a or b })
object Bori : Instruction("bori", true, false, { a, b -> a or b })
object Setr : Instruction("setr", true, true, { a, _ -> a })
object Seti : Instruction("seti", false, false, { a, _ -> a })
object Gtir : Instruction("gtir", false, true, { a, b -> if (a > b) 1 else 0 })
object Gtri : Instruction("gtri", true, false, { a, b -> if (a > b) 1 else 0 })
object Gtrr : Instruction("gtrr", true, true, { a, b -> if (a > b) 1 else 0 })
object Eqir : Instruction("eqir", false, true, { a, b -> if (a == b) 1 else 0 })
object Eqri : Instruction("eqri", true, false, { a, b -> if (a == b) 1 else 0 })
object Eqrr : Instruction("eqrr", true, true, { a, b -> if (a == b) 1 else 0 })

val opcodes = listOf(
        Addr, Addi, Mulr, Muli,
        Banr, Bani, Borr, Bori,
        Setr, Seti, Gtir, Gtri,
        Gtrr, Eqir, Eqri, Eqrr)

fun main(args: Array<String>) {
    val input: List<InputFrame> = File(ClassLoader.getSystemResource("day-16-input.txt").file)
            .readLines()
            .take(3186)
            .windowed(4, 4)
            .map { InputFrame.next(it) }

    val A = 1
    val B = 2
    val C = 3

    var instructionsMatching3orMoreOpcodes = 0
    for (frame in input) {
        val instruction = frame.encodedOpcode()
        var outputMatches = 0
        for (opcode in opcodes) {
            val registers = frame.before()
            val a = if (opcode.aRegister) registers[instruction[A]]!! else instruction[A]
            val b = if (opcode.bRegister) registers[instruction[B]]!! else instruction[B]
            registers[instruction[C]] = opcode.op(a, b)
            if (registers == frame.after()) {
                outputMatches++
                println("${frame.inputOpcode} matches ${opcode.name}")
            }
        }
        if (outputMatches >= 3) {
            instructionsMatching3orMoreOpcodes++
        }
    }
    println("$instructionsMatching3orMoreOpcodes")
}
