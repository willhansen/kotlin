fun box() : String {
    konst o = object {

        inner class C {
            fun foo() = "OK"
        }
    }
    return o.C().foo()
}
