package test

import kotlin.internal.flexible.ft

abstract class FlexibleTypes() {
    abstract fun collection(): ft<List<Int>, List<Any>>

    abstract konst p: ft<Int, Int?>

    fun withBody(): ft<Int, Int?> { return 1 }
}