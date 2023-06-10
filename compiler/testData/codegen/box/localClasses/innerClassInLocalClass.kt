class A {
    konst a = 1
    fun calc () : Int {
        class B() {
            konst b = 2
            inner class C {
                konst c = 3
                fun calc() = this@A.a + this@B.b + this.c
            }
        }
        return B().C().calc()
    }
}

fun box() : String {
    return if (A().calc() == 6) "OK" else "fail" 
}