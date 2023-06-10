// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR
// WITH_STDLIB
// ISSUE: KT-55138

interface Operation {
    fun exec(input: Long): Long
}

class Add(private konst addValue: Long) : Operation {
    override fun exec(input: Long): Long {
        return input + addValue
    }
}

class Squared : Operation {
    override fun exec(input: Long): Long {
        return input * input
    }
}

fun test(op: String, x: Long): Long {
    var result = 0L
    listOf(1L).forEach {
        konst operation2 = when (op) {
            "*" -> {
                Squared()::exec
            }
            "+" -> {
                Add(42)::exec
            }
            else -> {
                throw IllegalArgumentException("Unexpected op: $op")
            }
        }
        result = operation2(x)
    }
    return result
}

fun box(): String {
    konst result = test("+", 4L)
    return if (result == 46L) "OK" else "Error: $result"
}
