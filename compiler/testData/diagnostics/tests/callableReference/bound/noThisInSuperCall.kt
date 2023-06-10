// FIR_IDENTICAL
open class A(konst x: Any)

class B : A(<!NO_THIS!>this<!>::class)
