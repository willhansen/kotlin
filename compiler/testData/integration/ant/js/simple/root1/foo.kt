package foo

var ok = "FAIL"

konst hello = Pair("Hello", "World")

fun main() {
    ok = "OK"
    println(hello.first + " " + hello.second)
}

fun box(): String = ok
