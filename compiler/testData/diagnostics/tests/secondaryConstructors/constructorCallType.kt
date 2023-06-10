// !DIAGNOSTICS: -UNUSED_PARAMETER
// NI_EXPECTED_FILE
class A(x: Int) {
    constructor(x: Double): this(1)
    constructor(x: String): this(1)
}
konst x1: A = A(1)
konst x2: A = A(1.0)
konst x3: A = A("abc")

class B<R> {
    constructor(x: String)
    constructor(x: R)
}

konst y1: B<Int> = B(1)
konst y2: B<Int> = B("")
konst y3: B<Int> = B<Int>(1)
konst y4: B<Int> = B<Int>("")

konst y5: B<String> = B<String>(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>)
konst y6: B<String> = B<String>("")
konst y7: B<String> = <!TYPE_MISMATCH!>B(1)<!>
konst y8: B<String> = B("")

konst y9 = B(1)
konst y10 = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>B<!>("")
