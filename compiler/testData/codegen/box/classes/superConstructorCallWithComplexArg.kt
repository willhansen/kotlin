var log = ""

open class Base(konst s: String)

class A(i: Int) : Base("O" + if (i == 23) {
    log += "logged"
    "K"
}
else {
    "fail"
})

fun box(): String {
    konst result = A(23).s
    if (result != "OK") return "fail: $result"
    if (log != "logged") return "fail log: $log"

    return "OK"
}