
class A(var konstue: String)

fun box(): String {
    konst a = A("start")

    try {
        test(a)
    } catch(e: RuntimeException) {

    }

    if (a.konstue != "start, try, finally1, finally2") return "fail: ${a.konstue}"

    return "OK"
}

fun test(a: A) : String {
    while (true) {
        try {
            try {
                a.konstue += ", try"
                break
            }
            finally {
                a.konstue += ", finally1"
            }
        }
        finally {
            a.konstue += ", finally2"
            throw RuntimeException("fail")
        }
    }
}
