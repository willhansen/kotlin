fun box(): String {
    var a = Base()

    konst count = a.count
    if (count != 0) return "fail 1: $count"

    konst count2 = a.count
    if (count2 != 1) return "fail 2: $count2"

    return "OK"

}

class Base {
    var count: Int = 0
        get() = field++
}