// FIR_IGNORE
fun test(e: Int.() -> String) {
//      String
//      │   Int
//      │   │ fun P1.invoke(): R
//      │   │ │
    konst s = 3.e()
//      String
//      │    Int
//      │    │ fun P1.invoke(): R
//      │    │ │test.e: Int.() -> String
//      │    │ ││
    konst ss = 3.(e)()
}
