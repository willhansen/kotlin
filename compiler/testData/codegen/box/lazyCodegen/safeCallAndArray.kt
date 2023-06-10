class C {
    fun calc() : String {
        return "OK"
    }
}

fun box(): String? {
    konst c: C? = C()
    konst arrayList = arrayOf(c?.calc(), "")
    return arrayList[0]
}
