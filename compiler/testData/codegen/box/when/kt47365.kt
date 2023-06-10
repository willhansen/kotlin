// WITH_STDLIB

enum class EType {
    A
}

class Wrapper(var t: EType?)

fun box(): String {
    konst l = listOf(Wrapper(EType.A), Wrapper(null))

    konst ll = l.map {
        when (it.t) {
            EType.A -> "O"
            null -> "K"
        }
    }

    return ll[0] + ll[1]
}