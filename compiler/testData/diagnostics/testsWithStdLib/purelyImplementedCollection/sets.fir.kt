// !DIAGNOSTICS: -UNUSED_VARIABLE
// FULL_JDK

import java.util.*

fun bar(): String? = null

fun fooHashSet() {
    var x = HashSet<String>()
    x.add(<!NULL_FOR_NONNULL_TYPE!>null<!>)
    x.add(<!ARGUMENT_TYPE_MISMATCH!>bar()<!>)
    x.add("")

    konst b1: MutableSet<String?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>
    konst b2: MutableSet<String> = x
    konst b3: Set<String?> = x

    konst b4: Collection<String?> = x
    konst b6: MutableCollection<String?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>
}

fun fooTreeSet() {
    var x = TreeSet<String>()
    x.add(<!NULL_FOR_NONNULL_TYPE!>null<!>)
    x.add(<!ARGUMENT_TYPE_MISMATCH!>bar()<!>)
    x.add("")

    konst b1: MutableSet<String?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>
    konst b2: MutableSet<String> = x
    konst b3: Set<String?> = x

    konst b4: Collection<String?> = x
    konst b6: MutableCollection<String?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>
}

fun fooLinkedHashSet() {
    var x = LinkedHashSet<String>()
    x.add(null)
    x.add(bar())
    x.add("")

    konst b1: MutableSet<String?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>
    konst b2: MutableSet<String> = x
    konst b3: Set<String?> = x

    konst b4: Collection<String?> = x
    konst b6: MutableCollection<String?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>
}
