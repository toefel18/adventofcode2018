package adventofcode.day19

import java.io.File


data class InputFrame(val inputBefore: String, val inputOpcode: String, val inputAfter: String) {
    fun before(): MutableMap<Int, Int> = parse(inputBefore)
    fun encodedOpcode(): List<Int> = inputOpcode.split(" ").map { it.toInt() }
    fun after(): MutableMap<Int, Int> = parse(inputAfter)

    private fun parse(line: String): MutableMap<Int, Int> {
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

open class Instruction(val name: String, val aRegister: Boolean, val bRegister: Boolean, val op: (Int, Int) -> Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Instruction

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

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

val opcodesByName = opcodes.map { it.name to it }.toMap()

data class ProgramLine(val index: Int, val instruction: Instruction, val a: Int, val b: Int, val c: Int)

fun main(args: Array<String>) {
    val input: List<String> = File(ClassLoader.getSystemResource("day-19-input.txt").file).readLines()
    var ipRegister = input.first().split(" ")[1].toInt()
    val program = input.drop(1).mapIndexed { index, line ->
        val instruction = line.split(" ")
        ProgramLine(index, opcodesByName[instruction[0]]!!, instruction[1].toInt(), instruction[2].toInt(), instruction[3].toInt())
    }

    program.forEach { println("${it.index}, ${it.instruction.name} ${it.a} ${it.b} ${it.c}") }

    val registers = mutableMapOf<Int, Int>(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)
    val A = 1
    val B = 2
    val C = 3

    // fetch decode execute loop
    while (true){
        val ip = registers[ipRegister]!!

        if (ip < 0 || ip >= program.size) {
            println("reached end of program")
            break
        }

        val programLine = program[ip]
        val a = if (programLine.instruction.aRegister) registers[programLine.a]!! else programLine.a
        val b = if (programLine.instruction.bRegister) registers[programLine.b]!! else programLine.b
        registers[programLine.c] = programLine.instruction.op(a, b)
        registers[ipRegister] = registers[ipRegister]!! + 1
    }

    println(registers)
}
