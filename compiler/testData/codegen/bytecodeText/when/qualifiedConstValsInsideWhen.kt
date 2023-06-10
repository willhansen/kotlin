object Constants {
    const konst A = 30
    const konst B = 40
}

class ClassConstants {
    companion object {
        const konst C = 50
    }
}
fun foo(state: Int) {
    when (state) {
        Constants.A -> return
        Constants.B -> return
        ClassConstants.C -> return
        else -> return
    }
}

// 1 LOOKUPSWITCH