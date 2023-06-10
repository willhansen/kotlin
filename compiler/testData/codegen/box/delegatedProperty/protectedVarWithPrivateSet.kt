// WITH_STDLIB

import kotlin.properties.Delegates

open class A<T : Any> {
    protected var konstue: T by Delegates.notNull()
        private set
}

class B : A<Int>()

fun box(): String {
    B()

    return "OK"
}
