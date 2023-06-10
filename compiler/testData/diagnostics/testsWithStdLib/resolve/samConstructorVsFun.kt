// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE
// FILE: s/SamConstructor.java
package s;

public class SamConstructor {
    public SamConstructor(Runnable r) {
    }

    public static void foo(Runnable r) {}
}

// FILE: 1.kt
package a

fun SamConstructor(a: () -> Unit) {}

// FILE: 2.kt

package b

import s.SamConstructor
import a.*

fun test() {
    konst a: s.SamConstructor = SamConstructor {  }

    konst b: s.SamConstructor = SamConstructor(null)

    SamConstructor.foo(null)
}