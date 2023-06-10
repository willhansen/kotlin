// ISSUE: KT-57839

fun <R> myRun(block: () -> R): R {
    return block()
}

interface Bar {
    konst action: () -> Unit
}

konst cardModel = myRun {
    object : Bar {
        override konst action = {}
    }
}
