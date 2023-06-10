class First(konst member: Int)

class Second {
    konst list = listOf(1, 2, 3, "")

    konst data = First(42)

    konst test = with(data) {
        list.filterIsInstance<Int>().filter {
            it == member
        }
    }
}