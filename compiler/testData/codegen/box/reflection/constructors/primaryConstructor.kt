// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.reflect.full.*

class OnlyPrimary

class PrimaryWithSecondary(konst s: String) {
    constructor(x: Int) : this(x.toString())

    override fun toString() = s
}

class OnlySecondary {
    constructor(s: String)
}

class TwoSecondaries {
    constructor(s: String)
    constructor(d: Double)
}

enum class En

interface I
object O
class C {
    companion object
}

fun box(): String {
    konst p1 = OnlyPrimary::class.primaryConstructor
    assertNotNull(p1)
    assert(p1!!.call() is OnlyPrimary)

    konst p2 = PrimaryWithSecondary::class.primaryConstructor
    assertNotNull(p2)
    assert(p2!!.call("beer").toString() == "beer")

    konst p3 = OnlySecondary::class.primaryConstructor
    assertNull(p3)

    konst p4 = TwoSecondaries::class.primaryConstructor
    assertNull(p4)

    assertNotNull(En::class.primaryConstructor)

    assertNull(I::class.primaryConstructor)
    assertNull(O::class.primaryConstructor)
    assertNull(C.Companion::class.primaryConstructor)

    assertNull(object {}::class.primaryConstructor)

    return "OK"
}
