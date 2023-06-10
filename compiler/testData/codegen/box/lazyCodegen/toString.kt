class A (konst p: String) {

    konst _kind: String = "$p"

}

fun box(): String {

    if (A("OK")._kind != "OK") return "fail"

    return "OK"
}