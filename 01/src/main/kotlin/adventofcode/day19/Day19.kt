package adventofcode.day19

import java.io.File

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

val opcodesByName = opcodes.map { it.name to it }.toMap()

data class ProgramLine(val index: Int, val instruction: Instruction, val a: Int, val b: Int, val c: Int)

fun main(args: Array<String>) {
    val input: List<String> = File(ClassLoader.getSystemResource("day-19-input.txt").file).readLines()
    val ipRegister = input.first().split(" ")[1].toInt()
    val program = input.drop(1).mapIndexed { index, line ->
        val instruction = line.split(" ")
        ProgramLine(index, opcodesByName[instruction[0]]!!, instruction[1].toInt(), instruction[2].toInt(), instruction[3].toInt())
    }

    program.forEach { println("${it.index}, ${it.instruction.name} ${it.a} ${it.b} ${it.c}") }
    val registers = mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)
    registers[0] = 0

    var iter = 0

    // fetch decode execute loop
    while (true) {
        println("${iter++} $registers")
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
