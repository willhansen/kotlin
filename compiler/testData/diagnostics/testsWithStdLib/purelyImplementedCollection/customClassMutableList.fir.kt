// !DIAGNOSTICS: -UNUSED_VARIABLE
// JAVAC_EXPECTED_FILE

import java.util.*;

// FILE: A.java
@kotlin.jvm.PurelyImplements("kotlin.collections.MutableList")
class A<T> extends AbstractList<T> {
    @Override
    public T get(int index) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}

// FILE: b.kt

fun bar(): String? = null

fun foo() {
    var x = A<String>()
    x.add(null)
    x.add(bar())
    x.add("")

    x[0] = null
    x[0] = bar()
    x[0] = ""

    konst b1: MutableList<String?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>
    konst b2: MutableList<String> = x
    konst b3: List<String?> = x

    konst b4: Collection<String?> = x
    konst b6: MutableCollection<String?> = <!INITIALIZER_TYPE_MISMATCH!>x<!>
}
