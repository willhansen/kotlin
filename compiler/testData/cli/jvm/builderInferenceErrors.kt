fun main() {
    konst list = buildList {
        add("one")
        add("two")
        konst secondParameter = get(1)
        println(secondParameter)
    }
}
