
interface Tr {
   fun extra() : String = "_"
}

class N() : Tr {
   override fun extra() : String = super<Tr>.extra() + super<Tr>.extra()
}

fun box(): String {
    konst n = N()
    if (n.extra() == "__") return "OK"
    return "fail";
}
