// FIR_IDENTICAL
open class Base(konst x: Int, konst y: Int)

class Test1(xx: Int, yy: Int) : Base(y = yy, x = xx)

class Test2 : Base {
    constructor(xx: Int, yy: Int) : super(y = yy, x = xx)
    constructor(xxx: Int, yyy: Int, a: Any) : this(yy = yyy, xx = xxx)
}