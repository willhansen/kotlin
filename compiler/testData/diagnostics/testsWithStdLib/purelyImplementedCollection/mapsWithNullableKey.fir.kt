// !DIAGNOSTICS: -UNUSED_VARIABLE
// FULL_JDK

import java.util.*

fun bar(): String? = null
konst nullableInt: Int? = null

fun hashMapTest() {
    var x: HashMap<String?, Int> = HashMap<String?, Int>()
    x.put(null, <!NULL_FOR_NONNULL_TYPE!>null<!>)
    x.put("", <!NULL_FOR_NONNULL_TYPE!>null<!>)
    x.put(bar(), 1)
    x.put("", 1)

    x[null] = 1
    x[bar()] = 1
    x[""] = <!ARGUMENT_TYPE_MISMATCH!>nullableInt<!>
    x[""] = 1
    x[""] = <!NULL_FOR_NONNULL_TYPE!>null<!>

    konst b1: MutableMap<String?, Int?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>
    konst b2: MutableMap<String?, Int> = x
    konst b3: Map<String?, Int> = x
    konst b4: Map<String?, Int?> = x
    konst b5: Map<String, Int?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>

    konst b6: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>x[""]<!>
    konst b7: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>x[null]<!>
    konst b8: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>x.get("")<!>

    konst b9: Int? = x.get("")
}

fun treeMapTest() {
    var x: TreeMap<String?, Int> = TreeMap<String?, Int>()
    x.put(null, <!NULL_FOR_NONNULL_TYPE!>null<!>)
    x.put("", <!NULL_FOR_NONNULL_TYPE!>null<!>)
    x.put(bar(), 1)
    x.put("", 1)

    x[null] = 1
    x[bar()] = 1
    x[""] = <!ARGUMENT_TYPE_MISMATCH!>nullableInt<!>
    x[""] = 1

    konst b1: MutableMap<String?, Int?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>
    konst b2: MutableMap<String?, Int> = x
    konst b3: Map<String?, Int> = x
    konst b4: Map<String?, Int?> = x
    konst b5: Map<String, Int?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>

    konst b6: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>x[""]<!>
    konst b7: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>x.get("")<!>

    konst b8: Int? = x.get("")
}
