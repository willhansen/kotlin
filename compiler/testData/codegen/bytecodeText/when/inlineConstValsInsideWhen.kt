const konst A = 10
private const konst B = 20

object Constants {
    const konst C = 30
}

fun foo(state: Int) {
    when (state) {
        A -> return
        B -> return
        Constants.C -> return
        else -> return
    }
}

// 1 LOOKUPSWITCH