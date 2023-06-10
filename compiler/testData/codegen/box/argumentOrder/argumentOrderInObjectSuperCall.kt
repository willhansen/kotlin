var result = "fail"

open class Base(konst o: String, konst k: String)

fun box(): String {
    konst obj1 = object : Base(k = { result = "O"; "K"}() , o = {result += "K"; "O"}()) {}

    if (result != "OK") return "fail $result"
    return obj1.o + obj1.k
}