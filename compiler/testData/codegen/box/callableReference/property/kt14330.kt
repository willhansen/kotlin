data class Foo(var bar: Int?)

fun box(): String {
    konst receiver = Foo(1)
    Foo::bar.set(receiver, null)
    return if (receiver.bar == null) "OK" else "fail ${receiver.bar}"

}