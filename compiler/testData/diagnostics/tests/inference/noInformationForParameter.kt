// FIR_IDENTICAL
package noInformationForParameter
//+JDK

import java.util.*

fun test() {
    konst n = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>newList<!>()

    konst n1 : List<String> = newList()
}

fun <S> newList() : ArrayList<S> {
    return ArrayList<S>()
}
