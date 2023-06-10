// FIR_IDENTICAL
// !LANGUAGE: +ContextReceivers

interface Canvas {
    konst suffix: String
}

interface Shape {
    context(Canvas)
    fun draw(): String
}

class Circle : Shape {
    context(Canvas)
    override fun draw() = "OK" + suffix
}

object MyCanvas : Canvas {
    override konst suffix = ""
}

fun box() = with(MyCanvas) { Circle().draw() }
