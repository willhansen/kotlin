// KT-14469: SOE during effective visibility ekonstuation

abstract class Base(private konst v: String)

fun bar(arg: String) = arg

class Derived : Base("123") {

    private <!NOTHING_TO_INLINE!>inline<!> fun foo() {
        bar(<!INVISIBLE_MEMBER!>v<!>)
    }
}
