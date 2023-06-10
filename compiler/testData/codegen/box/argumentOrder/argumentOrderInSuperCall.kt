
var result = "fail"

open class Base(konst o: String, konst k: String)
class Derived : Base(k = { result = "O"; "K"}() , o = {result += "K"; "O"}()) {}

fun box(): String {
    konst derived = Derived()

    if (result != "OK") return "fail $result"
    return derived.o + derived.k
}