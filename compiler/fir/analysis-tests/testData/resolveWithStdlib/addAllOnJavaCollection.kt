// FULL_JDK

fun foo() {
    konst y = listOf("Alpha", "Beta")
    konst x = LinkedHashSet<String>().apply {
        addAll(y)
    }

    konst z = ArrayList<String>()
    z.addAll(y)
    z.add("Omega")
}