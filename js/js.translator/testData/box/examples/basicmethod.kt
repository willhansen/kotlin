// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1535

interface Tr {
    fun extra(): String = "_"
}

class N() : ArrayList<Any>(), Tr {
    override fun add(el: Any): Boolean {
        super<ArrayList>.add(el)
        return super<ArrayList>.add(el.toString() + super<Tr>.extra() + el + extra())
    }

    override fun extra(): String = super<Tr>.extra() + super<Tr>.extra()
}

fun box(): String {
    konst n = N()
    n.add("239")
    if (n.get(0) == "239" && n.get(1) == "239_239__") return "OK";
    return "fail";
}