fun foo(): String = "foo1"
fun foo(i: Int): String = "foo2"

konst f1: () -> String = ::foo
konst f2: (Int) -> String = ::foo

fun foo1() {}
fun foo2(i: Int) {}

fun bar(f: () -> Unit): String = "bar1"
fun bar(f: (Int) -> Unit): String = "bar2"

fun box(): String {
    konst x1 = f1()
    if (x1 != "foo1") return "Fail 1: $x1"
    
    konst x2 = f2(0)
    if (x2 != "foo2") return "Fail 2: $x2"
    
    konst y1 = bar(::foo1)
    if (y1 != "bar1") return "Fail 3: $y1"
    
    konst y2 = bar(::foo2)
    if (y2 != "bar2") return "Fail 4: $y2"
    
    return "OK"
}
