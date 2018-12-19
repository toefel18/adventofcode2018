package adventofcode.day16

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

fun main(args: Array<String>) {
    val input: List<InputFrame> = File(ClassLoader.getSystemResource("day-16-input.txt").file)
            .readLines()
            .take(3186)
            .windowed(4, 4)
            .map { InputFrame.next(it) }

    val OpcodeNr = 0
    val A = 1
    val B = 2
    val C = 3

    var instructionsMatching3orMoreOpcodes = 0

    val allOpcodeNames = opcodes.map { it.name }

    // init to all names, each opcode could map to any
    val opcodeToMatched: MutableMap<Int, MutableSet<String>> = (0..15).map { it to allOpcodeNames.toMutableSet() }.toMap().toMutableMap()

    for (frame in input) {
        val instruction = frame.encodedOpcode()
        var outputMatches = 0
        val opcodeNr = instruction[OpcodeNr]
        val matchedOpcodeNames = mutableSetOf<String>()
        for (opcode in opcodes) {
            val registers = frame.before()
            val a = if (opcode.aRegister) registers[instruction[A]]!! else instruction[A]
            val b = if (opcode.bRegister) registers[instruction[B]]!! else instruction[B]
            registers[instruction[C]] = opcode.op(a, b)
            if (registers == frame.after()) {
                outputMatches++
                matchedOpcodeNames.add(opcode.name)
            }
        }
        opcodeToMatched[opcodeNr]!!.retainAll(matchedOpcodeNames)
        if (outputMatches >= 3) {
            instructionsMatching3orMoreOpcodes++
        }
    }
    println("$instructionsMatching3orMoreOpcodes")

    val opcodeNrToName = mutableMapOf<Int, String>()

    while (opcodeToMatched.isNotEmpty()) {
        val determinedInstructions = opcodeToMatched.filter { it.value.size == 1 }
        determinedInstructions.forEach { opNr, nameSet -> opcodeNrToName[opNr] = nameSet.first() }
        val namesToRemove = determinedInstructions.flatMap { it.value }
        opcodeToMatched.forEach { _, v -> v.removeAll(namesToRemove) }
        determinedInstructions.forEach { opcodeToMatched.remove(it.key) }
    }

    opcodeNrToName.forEach { println("${it.key}  ${it.value}") }

    val testProgram: List<List<Int>> = File(ClassLoader.getSystemResource("day-16-input.txt").file)
            .readLines()
            .drop(3186)
            .map { line -> line.split(" ").map { it.toInt() } }


    val registers = mutableMapOf<Int, Int>(0 to 0, 1 to 0, 2 to 0, 3 to 0)

    testProgram.forEach { programLine->
        val instructionName = opcodeNrToName[programLine[OpcodeNr]]
        val instruction = opcodes.first { it.name == instructionName }
        val a = if (instruction.aRegister) registers[programLine[A]]!! else programLine[A]
        val b = if (instruction.bRegister) registers[programLine[B]]!! else programLine[B]
        registers[programLine[C]] = instruction.op(a, b)
    }

    println(registers[0])
}
