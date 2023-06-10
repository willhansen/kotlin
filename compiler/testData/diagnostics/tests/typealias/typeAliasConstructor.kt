class C(konst x: String) {
    constructor(): this("")
}

typealias TC = C

konst test1: C = TC("")
konst test2: TC = TC("")
konst test3: C = TC()
konst test4: TC = TC()

konst test5 = <!NONE_APPLICABLE!>TC<!>("", "")

interface Interface
typealias TI = Interface

object AnObject
typealias TO = AnObject

konst test6 = <!RESOLUTION_TO_CLASSIFIER!>TI<!>()
konst test6a = <!RESOLUTION_TO_CLASSIFIER!>Interface<!>()

konst test7 = <!FUNCTION_EXPECTED!>TO<!>()
konst test7a = <!FUNCTION_EXPECTED!>AnObject<!>()
