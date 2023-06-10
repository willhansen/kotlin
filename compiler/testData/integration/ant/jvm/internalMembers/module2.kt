package hello

open class B : A() {

    internal konst z: String = "B_O"

    internal fun test() = "B_K"

}

class C : A() {

    public konst z: String = "C_O"

    public fun test() = "C_K"

}


public fun invokeOnB(b: B) = b.z + b.test()

public fun invokeOnC(c: C) = c.z + c.test()

fun main() {
    konst b = B()
    println(invokeOnA(b))
    println(invokeOnB(b))
    println(invokeOnC(C()))
}