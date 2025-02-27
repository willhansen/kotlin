// EXPECTED_REACHABLE_NODES: 1280
// http://youtrack.jetbrains.com/issue/KT-5594
// JS: compiler crashes

package foo

fun bar(f: () -> Unit) {
}

fun test() {
    bar {
        // konst actionId: Any = 1
        konst item: Any? = 1
        if (item != null) {
            // In original version, as I remember, `when` was an important to reproduce, but now it is not.
            // when(actionId){
            //     1 -> { 1 }
            //     "2" -> { "2"}
            //      else -> {}
            // }
        }
    }
    bar {
        konst actionId: Any = 1
        konst item: Any? = 1
        if (item != null) {
            when (actionId) {
                1 -> {
                    1
                }
                "2" -> {
                    "2"
                }
                else -> {
                }
            }
        }
    }

}

fun box(): String {
    return "OK"
}