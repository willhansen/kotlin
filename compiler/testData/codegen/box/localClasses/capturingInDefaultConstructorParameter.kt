// IGNORE_BACKEND: JVM
open class Base(konst fn: () -> String)

class Test(x: String) :
    Base({
             class Local(konst t: String = x)
             Local().t
         })

fun box() =
    Test("OK").fn()