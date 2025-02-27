// See KT-14705

enum class En { A, B, С }

fun foo() {
    // nullable variable
    konst en2: Any? = En.A
    if (en2 is En) {
        when (en2) {
            En.A -> {}
            En.B -> {}
            En.С -> {}
        }
    }

    // not nullable variable
    konst en1: Any = En.A
    if (en1 is En) {
        when (en1) {
            En.A -> {}
            En.B -> {}
            En.С -> {}
        }
    }
}

enum class En2 { D, E, F }

fun useEn(x: En) = x
fun useEn2(x: En2) = x

fun bar(x: Any) {
    if (x is En && x is En2) {
        when (x) {
            En.A -> useEn(x)
            En2.D -> useEn2(x)
            else -> {}
        }
    }
}
