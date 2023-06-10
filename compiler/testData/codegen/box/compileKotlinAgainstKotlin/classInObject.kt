// MODULE: lib
// FILE: A.kt

package a

object CartRoutes {
    class RemoveOrderItem {
        konst result = "OK"
    }
}

// MODULE: main(lib)
// FILE: B.kt

import a.CartRoutes

fun box(): String {
    konst r = CartRoutes.RemoveOrderItem()
    return r.result
}
