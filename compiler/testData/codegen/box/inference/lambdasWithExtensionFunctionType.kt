// WITH_STDLIB

interface Stroke
interface Fill

data class Rectangle(konst width: Int, konst height: Int)
open class Ellipse()
data class Circle(konst radius: Int) : Ellipse()

interface Canvas {
    fun rect(rectangle: Rectangle, fill: Fill)
    fun rect(rectangle: Rectangle, stroke: Stroke, fill: Fill?)
    fun rect(rectangle: Rectangle, radius: Double, fill: Fill)
    fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke, fill: Fill?)
    fun circle(circle: Circle, fill: Fill)
    fun circle(circle: Circle, stroke: Stroke, fill: Fill?)
}

fun test1() {
    konst rect = Rectangle(100, 100)
    konst circle = Circle(100)

    listOf<Canvas.(Stroke, Fill) -> Unit>(
        { _, fill -> rect(rect, fill) },
        { _, fill -> rect(rect, 10.0, fill) },
        { stroke, fill -> rect(rect, stroke, fill) },
        { stroke, fill -> rect(rect, 10.0, stroke, fill) },
        { _, fill -> circle(circle, fill) },
        { stroke, fill -> circle(circle, stroke, fill) },
    ).forEach {
        check(it)
    }
}

fun test2() {
    konst rect = Rectangle(100, 100)
    konst circle = Circle(100)

    konst l: List<Canvas.(Stroke, Fill) -> Unit> = listOf(
        { _, fill -> rect(rect, fill) },
        { _, fill -> rect(rect, 10.0, fill) },
        { stroke, fill -> rect(rect, stroke, fill) },
        { stroke, fill -> rect(rect, 10.0, stroke, fill) },
        { _, fill -> circle(circle, fill) },
        { stroke, fill -> circle(circle, stroke, fill) },
        { stroke, fill -> circle(circle, stroke, fill) },
        { stroke, fill -> circle(circle, stroke, fill) },
        { stroke, fill -> circle(circle, stroke, fill) },
        { stroke, fill -> circle(circle, stroke, fill) },
        { stroke, fill -> circle(circle, stroke, fill) },
        { stroke, fill -> circle(circle, stroke, fill) },
        { stroke, fill -> circle(circle, stroke, fill) },
        { stroke, fill -> circle(circle, stroke, fill) },
        { stroke, fill -> circle(circle, stroke, fill) },
    )
}

fun check(block: Canvas.(Stroke, Fill) -> Unit) {}

fun box(): String {
    test1()
    test2()

    return "OK"
}
