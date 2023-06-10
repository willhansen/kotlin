fun idAny(x: Any) = x
fun <T> id(x: T) = x
fun idUnit(x: Unit) = x

class MList {
    // MutableCollection<T>.add returns Boolean, but nobody cares
    fun add(): Boolean = true
}
konst mlist = MList()

fun work() {}

konst xx1 = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) 42
konst xx2: Unit = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) 42
konst xx3 = idAny(<!INVALID_IF_AS_EXPRESSION!>if<!> (true) 42)
konst xx4 = id(<!INVALID_IF_AS_EXPRESSION!>if<!> (true) 42)
konst xx5 = idUnit(<!INVALID_IF_AS_EXPRESSION!>if<!> (true) 42)
konst xx6 = null ?: <!INVALID_IF_AS_EXPRESSION!>if<!> (true) 42
konst xx7 = "" + <!INVALID_IF_AS_EXPRESSION!>if<!> (true) 42

konst wxx1 = <!NO_ELSE_IN_WHEN!>when<!> { true -> 42 }
konst wxx2: Unit = <!TYPE_MISMATCH!><!NO_ELSE_IN_WHEN!>when<!> { true -> 42 }<!>
konst wxx3 = idAny(<!NO_ELSE_IN_WHEN!>when<!> { true -> 42 })
konst wxx4 = id(<!NO_ELSE_IN_WHEN!>when<!> { true -> 42 })
konst wxx5 = idUnit(<!TYPE_MISMATCH!><!NO_ELSE_IN_WHEN!>when<!> { true -> 42 }<!>)
konst wxx6 = null ?: <!NO_ELSE_IN_WHEN!>when<!> { true -> 42 }
konst wxx7 = "" + <!NO_ELSE_IN_WHEN!>when<!> { true -> 42 }

konst fn1 = { if (true) 42 }
konst fn2 = { if (true) mlist.add() }
konst fn3 = { if (true) work() }
konst fn4 = { <!NO_ELSE_IN_WHEN!>when<!> { true -> 42 } }
konst fn5 = { <!NO_ELSE_IN_WHEN!>when<!> { true -> mlist.add() } }
konst fn6 = { when { true -> work() } }

konst ufn1: () -> Unit = { if (true) 42 }
konst ufn2: () -> Unit = { if (true) mlist.add() }
konst ufn3: () -> Unit = { if (true) work() }
konst ufn4: () -> Unit = { when { true -> 42 } }
konst ufn5: () -> Unit = { when { true -> mlist.add() } }
konst ufn6: () -> Unit = { when { true -> work() } }

fun f1() = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) work()
fun f2() = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) mlist.add()
fun f3() = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) 42
fun f4(): Unit = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) work()
fun f5(): Unit = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) mlist.add()
fun f6(): Unit = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) 42
fun g1() = <!NO_ELSE_IN_WHEN!>when<!> { true -> work() }
fun g2() = <!NO_ELSE_IN_WHEN!>when<!> { true -> mlist.add() }
fun g3() = <!NO_ELSE_IN_WHEN!>when<!> { true -> 42 }
fun g4(): Unit = <!NO_ELSE_IN_WHEN!>when<!> { true -> work() }
fun g5(): Unit = <!TYPE_MISMATCH!><!NO_ELSE_IN_WHEN!>when<!> { true -> mlist.add() }<!>
fun g6(): Unit = <!TYPE_MISMATCH!><!NO_ELSE_IN_WHEN!>when<!> { true -> 42 }<!>

fun foo1(x: String?) {
    "" + <!INVALID_IF_AS_EXPRESSION!>if<!> (true) 42
    w@while (true) {
        x ?: <!INVALID_IF_AS_EXPRESSION!>if<!> (true) break
        x ?: <!NO_ELSE_IN_WHEN!>when<!> { true -> break@w }
    }
}

fun foo2() {
    if (true) {
        mlist.add()
    }
    else if (true) {
        mlist.add()
    }
    else if (true) {
        mlist.add()
    }

    when {
        true -> mlist.add()
        else -> when {
            true -> mlist.add()
            else -> when {
                true -> mlist.add()
            }
        }
    }
}
