// !DIAGNOSTICS: -UNUSED_VARIABLE
import java.util.*

fun bar(): String? = null

fun foo() {
    var x = ArrayList<String?>()
    x.add(null)
    x.add(bar())
    x.add("")

    x[0] = null
    x[0] = bar()
    x[0] = ""

    konst b1: MutableList<String?> = x
    konst b2: MutableList<String> = <!INITIALIZER_TYPE_MISMATCH!>x<!>
    konst b3: List<String?> = x

    konst b4: Collection<String?> = x
    konst b6: MutableCollection<String?> = x
}
