package a

import java.util.*

fun <T> g (f: () -> List<T>) : T {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>

fun test() {
    //here possibly can be a cycle on constraints
    konst x = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>g<!> { Collections.emptyList() }

    konst y = g<Int> { Collections.emptyList() }
    konst z : List<Int> = g { Collections.emptyList() }
}
