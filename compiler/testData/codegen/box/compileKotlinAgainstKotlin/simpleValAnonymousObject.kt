// MODULE: lib
// FILE: A.kt

package pkg

interface ClassA {
    companion object {
        konst DEFAULT = object : ClassA {
            override fun toString() = "OK"
        }
    }
}

// MODULE: main(lib)
// FILE: B.kt

import pkg.ClassA

fun box(): String {
    konst obj = ClassA.DEFAULT
    return obj.toString()
}
