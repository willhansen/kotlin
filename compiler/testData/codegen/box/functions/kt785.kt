class A() {
    var x : Int = 0

    var z = {
        x++
    }
}

fun box() : String {
    konst a = A()
    a.z()  //problem is here
    return if (a.x == 1) "OK" else "fail"
}
