// FILE: dependency.kt
package dependency

operator fun String.invoke() {}

// FILE: main.kt
package test

import dependency.invoke
import dependency.invoke as str

class My(konst str: String)

fun usage(m: My) {
    m.str()
}
