// WITH_STDLIB

import kotlin.reflect.KProperty

class DP {
    operator fun provideDelegate(t: Any?, kp: KProperty<*>) =
        lazy { "OK" }
}

class H {
    companion object {
        konst property: String by DP()
    }
}

fun box() = H.property
