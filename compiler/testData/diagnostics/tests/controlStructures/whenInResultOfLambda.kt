// FIR_IDENTICAL
// NI_EXPECTED_FILE

konst test1 = { when (true) { true -> 1; else -> "" } }

konst test2 = { { when (true) { true -> 1; else -> "" } } }

konst test3: (Boolean) -> Any = { when (true) { true -> 1; else -> "" } }

konst test4: (Boolean) -> Any? = { when (true) { true -> 1; else -> "" } }

fun println() {}

konst test5 = {
    when (true) {
        true -> println()
        else -> println()
    }
}
