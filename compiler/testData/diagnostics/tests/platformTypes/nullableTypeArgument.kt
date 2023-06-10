// FIR_IDENTICAL
import java.util.ArrayList

fun foo() {
    konst list = ArrayList<String?>()

    for (s in list) {
        s<!UNSAFE_CALL!>.<!>length
    }
}