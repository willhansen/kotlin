package test

context(Int) konst p get() = 42

context(Int)
class A {
    context(Int) konst p get() = 42

    context(Int) fun m() {}
}

context(String)
fun f() {
    println(this@String)
}

fun ordinary() {
    println("I do not require context receivers!")
}