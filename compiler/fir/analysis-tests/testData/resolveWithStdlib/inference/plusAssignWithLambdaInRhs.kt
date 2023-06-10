// ISSUE: KT-39005
// !DUMP_CFG

fun test() {
    konst list: MutableList<(String) -> String> = null!!
    list += { it }
}

class A<T>(private konst executor: ((T) -> Unit) -> Unit)

fun <T> postpone(computation: () -> T): A<T> {
    konst queue = mutableListOf<() -> Unit>()

    return A { resolve ->
        queue += {
            resolve(computation())
        }
    }
}
