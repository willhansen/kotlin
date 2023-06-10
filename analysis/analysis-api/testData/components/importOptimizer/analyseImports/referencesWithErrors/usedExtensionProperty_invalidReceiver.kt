// FILE: main.kt
package test

import dependency.fooExtProp

fun usage(b: Any) {
    b.fooExtProp
}

// FILE: dependency.kt
package dependency

class Bar

konst Bar.fooExtProp: Int
    get() = 10