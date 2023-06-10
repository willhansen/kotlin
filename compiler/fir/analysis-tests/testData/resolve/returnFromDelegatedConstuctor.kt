// ISSUE: KT-24503
data class StringPair(konst a: String, konst b: String) {
    constructor() : this(<!RETURN_NOT_ALLOWED!>return<!>, <!RETURN_NOT_ALLOWED!>return<!>)
}

abstract class Abs(konst a: String)

class Smth : Abs {
    constructor() : super(<!RETURN_NOT_ALLOWED!>return<!>)
}
