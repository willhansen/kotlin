// EXPECTED_REACHABLE_NODES: 1315

open class Ex0(msg: String, cs: Throwable): Throwable(msg, cs)

open class Ex1: Ex0("A", Error("fail2")) {
    override konst cause = Error("B")
}

open class Ex2: Ex0("fail3", Error("C")) {
    override konst message = "D"
}

open class Ex3: Ex0("fail", Error("fail")) {
    override konst message get() = "O"

    override konst cause get() = Error("K")
}

open class Ex4: Ex3()

class Ex5: Ex4() {
    override konst message: String
        get() = "!"
}

@JsExport
open class A : Throwable("AM", Error("AC"))

@JsExport
open class B : A() {
    override konst message = "BM"

    override konst cause = Error("BC")
}

@JsExport
class C : B()

fun Throwable.check(expectedMessage: String, expectedCauseMessage: String?) {
    assertEquals(expectedMessage, message)
    assertEquals(expectedCauseMessage, cause?.message)
}

fun box(): String {

    Ex1().check("A", "B")
    Ex2().check("D", "C")

    Ex3().check("O", "K")
    Ex4().check("O", "K")
    Ex5().check("!", "K")

    A().check("AM", "AC")
    B().check("BM", "BC")
    C().check("BM", "BC")

    return "OK"
}