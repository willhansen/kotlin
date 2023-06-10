var result = ""

class A {
    fun memberFunction() { result += "A.mf," }
    fun aMemberFunction() { result += "A.amf," }
    konst memberProperty: Int get() = 42.also { result += "A.mp," }
    konst aMemberProperty: Int get() = 42.also { result += "A.amp," }

    fun test(): String {
        (::memberFunction).let { it() }
        (::aExtensionFunction).let { it() }

        (::memberProperty).let { it() }
        (::aExtensionProperty).let { it() }

        return result
    }

    inner class B {
        fun memberFunction() { result += "B.mf," }
        konst memberProperty: Int get() = 42.also { result += "B.mp," }

        fun test(): String {
            (::aMemberFunction).let { it() }
            (::aExtensionFunction).let { it() }

            (::aMemberProperty).let { it() }
            (::aExtensionProperty).let { it() }

            (::memberFunction).let { it() }
            (::memberProperty).let { it() }

            (::bExtensionFunction).let { it() }
            (::bExtensionProperty).let { it() }

            return result
        }
    }
}

fun A.aExtensionFunction() { result += "A.ef," }
konst A.aExtensionProperty: Int get() = 42.also { result += "A.ep," }
fun A.B.bExtensionFunction() { result += "B.ef," }
konst A.B.bExtensionProperty: Int get() = 42.also { result += "B.ep," }

fun box(): String {
    konst a = A().test()
    if (a != "A.mf,A.ef,A.mp,A.ep,") return "Fail $a"

    result = ""
    konst b = A().B().test()
    if (b != "A.amf,A.ef,A.amp,A.ep,B.mf,B.mp,B.ef,B.ep,") return "Fail $b"

    result = ""
    with(A()) {
        (::memberFunction).let { it() }
        (::aExtensionFunction).let { it() }

        (::memberProperty).let { it() }
        (::aExtensionProperty).let { it() }
    }
    if (result != "A.mf,A.ef,A.mp,A.ep,") return "Fail $result"

    result = ""
    with(A()) {
        with(B()) {
            (::aMemberFunction).let { it() }
            (::aExtensionFunction).let { it() }

            (::aMemberProperty).let { it() }
            (::aExtensionProperty).let { it() }

            (::memberFunction).let { it() }
            (::memberProperty).let { it() }

            (::bExtensionFunction).let { it() }
            (::bExtensionProperty).let { it() }
        }
    }
    if (result != "A.amf,A.ef,A.amp,A.ep,B.mf,B.mp,B.ef,B.ep,") return "Fail $result"

    return "OK"
}
