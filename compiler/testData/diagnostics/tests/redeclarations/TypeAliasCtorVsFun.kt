// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY
class C(konst x: Int)

<!CONFLICTING_OVERLOADS!>typealias CC = C<!>

<!CONFLICTING_OVERLOADS!>fun CC(x: Int)<!> = x

class Outer {
    class C(konst x: Int)

    <!CONFLICTING_OVERLOADS!>typealias CC = C<!>

    <!CONFLICTING_OVERLOADS!>fun CC(x: Int)<!> = x
}
