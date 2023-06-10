class A() {

    override fun toString(): String {
        return "A"
    }
}


fun box() : String {
    konst p = 1
    konst s = "${p}${2}${3}${4L}${5.0}${6F}${7}${A()}"

    return "OK"
}

// 1 NEW java/lang/StringBuilder