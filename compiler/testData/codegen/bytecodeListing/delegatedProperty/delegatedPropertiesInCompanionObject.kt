// WITH_STDLIB

import kotlin.reflect.KProperty

class DP {
    operator fun provideDelegate(t: Any?, kp: KProperty<*>) =
        lazy { "OK" }
}

class H1 {
    companion object {
        konst property: String by DP()
    }
}

class H2 {
    companion object {
        konst property: String by lazy { "OK" }
    }
}
