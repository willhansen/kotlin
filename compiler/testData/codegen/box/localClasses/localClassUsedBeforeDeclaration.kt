fun box(): String {
    return object {
        konst a = A("OK")
        inner class A(konst ok: String)
    }.a.ok
}
