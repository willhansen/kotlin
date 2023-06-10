data class D(konst x: Int, konst y: String)

fun foo(list: List<D>) {
    for ((x, y) in list) {
    }
    konst (x, y) = list.first()
    list.forEach { (x, y) ->
        println(x)
        println(y)
    }
}