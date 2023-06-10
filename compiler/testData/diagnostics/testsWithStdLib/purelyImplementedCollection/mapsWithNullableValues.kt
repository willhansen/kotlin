// !DIAGNOSTICS: -UNUSED_VARIABLE
// FULL_JDK

import java.util.*

fun bar(): String? = null
konst nullableInt: Int? = null

fun hashMapTest() {
    var x: HashMap<String, Int?> = HashMap<String, Int?>()
    x.put(<!NULL_FOR_NONNULL_TYPE!>null<!>, null)
    x.put("", null)
    x.put(<!TYPE_MISMATCH!>bar()<!>, 1)
    x.put("", 1)

    x[<!NULL_FOR_NONNULL_TYPE!>null<!>] = 1
    x[<!TYPE_MISMATCH!>bar()<!>] = 1
    x[""] = nullableInt
    x[""] = 1

    konst b1: MutableMap<String, Int> = <!TYPE_MISMATCH!>x<!>
    konst b2: MutableMap<String, Int?> = x
    konst b3: Map<String, Int> = <!TYPE_MISMATCH!>x<!>
    konst b4: Map<String, Int?> = x
    konst b5: Map<String?, Int?> = <!TYPE_MISMATCH!>x<!>

    konst b6: Int = <!TYPE_MISMATCH!>x[""]<!>
    konst b7: Int = <!TYPE_MISMATCH!>x.get("")<!>

    konst b8: Int? = x.get("")
}

fun treeMapTest() {
    var x: TreeMap<String, Int?> = TreeMap<String, Int?>()
    x.put(<!NULL_FOR_NONNULL_TYPE!>null<!>, null)
    x.put("", null)
    x.put(<!TYPE_MISMATCH!>bar()<!>, 1)
    x.put("", 1)

    x[<!NULL_FOR_NONNULL_TYPE!>null<!>] = 1
    x[<!TYPE_MISMATCH!>bar()<!>] = 1
    x[""] = nullableInt
    x[""] = 1

    konst b1: MutableMap<String, Int> = <!TYPE_MISMATCH!>x<!>
    konst b2: MutableMap<String, Int?> = x
    konst b3: Map<String, Int> = <!TYPE_MISMATCH!>x<!>
    konst b4: Map<String, Int?> = x
    konst b5: Map<String?, Int?> = <!TYPE_MISMATCH!>x<!>

    konst b6: Int = <!TYPE_MISMATCH!>x[""]<!>
    konst b7: Int = <!TYPE_MISMATCH!>x.get("")<!>

    konst b8: Int? = x.get("")
}
