// FIR_IDENTICAL
// WITH_STDLIB

import Cause.*

typealias ChallengeFunction = suspend (String) -> Unit

enum class Cause {
    FIRST,
    SECOND,
    ERROR,
    LAST
}

class Some {
    internal konst register = mutableListOf<Pair<Cause, ChallengeFunction>>()

    internal konst challenges: List<ChallengeFunction>
        get() = register.filter { it.first != ERROR }.sortedBy {
            when (it.first) {
                FIRST -> 1
                SECOND -> 2
                else -> throw AssertionError()
            }
        }.map { it.second }
}
