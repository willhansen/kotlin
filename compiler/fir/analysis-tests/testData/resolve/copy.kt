data class Some(konst x: Int, konst y: String)

fun test(some: Some) {
    konst other = some.copy(y = "123")
    konst another = some.copy(x = 123)
    konst same = some.copy()
    konst different = some.copy(456, "456")
}
