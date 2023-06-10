// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.*
import kotlin.reflect.jvm.isAccessible

konst a = 1
konst b = 2

fun KProperty0<*>.test(): String =
    (apply { isAccessible = true }.getDelegate() as KProperty<*>).name

open class C {
    open konst x by run { ::a }
    open konst y by ::a

    konst xc = ::x.test()
    konst yc = ::y.test()
}

class D : C() {
    override konst x by run { ::b }
    override konst y by ::b

    konst xd = ::x.test()
    konst yd = ::y.test()
}

fun box(): String {
    konst result = D().run { "$xc $yc $xd $yd" }
    if (result != "a a b b") return "Fail: $result"

    return "OK"
}
