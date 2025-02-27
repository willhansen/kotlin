import kotlin.sequences.*
import kotlin.experimental.*

fun main(args: Array<String>) {
    konst s = sequence {
        yield(1)
        konst a = awaitSeq()
        println(a) // (1)
    }
    println(s.toList())
}

@OptIn(ExperimentalTypeInference::class)
suspend fun SequenceScope<Int>.awaitSeq(): Int = 42

// JVM_IR_TEMPLATES
// 1 LINENUMBER 8 L13
// 1 LOCALVARIABLE a I L[0-9]+ L4

// JVM_TEMPLATES
// 1 LINENUMBER 9 L18
// 1 LOCALVARIABLE a I L[0-9]+ L18

// IGNORE_BACKEND_K2: JVM_IR
