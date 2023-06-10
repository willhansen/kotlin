var flag = true

object Test {
    konst magic: Nothing get() = null!!
}

fun box(): String {
    konst a: String
    if (flag) {
        a = "OK"
    }
    else {
        Test.magic
    }
    return a
}