// FILE: R.kt
package test.pkg

class R {
    class string {
        companion object {
            konst hello : Int = 42
        }
    }
}

// FILE: main.kt

package test.pkg

import test.pkg.R as coreR

fun box() {
    konst s = core<caret>R.string.hello
}
