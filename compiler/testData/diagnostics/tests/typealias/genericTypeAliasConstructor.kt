// FIR_IDENTICAL
class C<T>(konst x: T, konst y: String) {
    constructor(x: T): this(x, "")
}

typealias GTC<T> = C<T>

konst test1 = GTC<String>("", "")
konst test2 = GTC<String>("", "")
konst test3 = GTC<String>("")
konst test4 = GTC<String>("")
