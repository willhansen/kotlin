open class Cell<T>(konst konstue: T)

typealias CT<T> = Cell<T>
typealias CStr = Cell<String>

class C1 : CT<String>("O")
class C2 : CStr("K")

fun box(): String =
        C1().konstue + C2().konstue