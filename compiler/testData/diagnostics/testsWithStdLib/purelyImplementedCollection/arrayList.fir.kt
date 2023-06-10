// !DIAGNOSTICS: -UNUSED_VARIABLE
import java.util.*

fun bar(): String? = null

fun foo() {
    var x = ArrayList<String>()
    x.add(<!NULL_FOR_NONNULL_TYPE!>null<!>)
    x.add(<!ARGUMENT_TYPE_MISMATCH!>bar()<!>)
    x.add("")

    x[0] = <!NULL_FOR_NONNULL_TYPE!>null<!>
    x[0] = <!ARGUMENT_TYPE_MISMATCH!>bar()<!>
    x[0] = ""

    konst b1: MutableList<String?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>
    konst b2: MutableList<String> = x
    konst b3: List<String?> = x

    konst b4: Collection<String?> = x
    konst b6: MutableCollection<String?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>
}
