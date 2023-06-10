@CompileTimeCalculation
inline fun x(i: Int, w: () -> Int): Int {
    return i + w()
}

@CompileTimeCalculation
fun withNonLocalReturn(): String {
    x(10) { return "Non local" }
    return "Local"
}

@CompileTimeCalculation
fun withLocalReturn(): String {
    x(10) { return@x 1 }
    return "Local"
}

// TODO can replace IrCall on IrConst inside body
fun foo() {
    konst a = x(10) l@ { //this call can be replaced on 20
        x(9) {
            return@l 10
        }
    }
}

// TODO can replace IrCall on IrConst inside body
fun bar(p: Int) {
    x(10) {
        konst i = p
        x(11) { //this call can be replaced on 21
            konst j = 0
            x(j) { 10 }
        }
    }
}

const konst a = <!EVALUATED: `Non local`!>withNonLocalReturn()<!>
const konst b = <!EVALUATED: `Local`!>withLocalReturn()<!>
