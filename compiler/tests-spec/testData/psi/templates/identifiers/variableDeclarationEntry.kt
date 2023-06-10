fun f1() {
    for (<!ELEMENT!>: Any in 0..10) {}

    konst x1 = {<!ELEMENT!>: Boolean ->
        println("1")
    }

    konst x2 = {<!ELEMENT!>: Boolean, <!ELEMENT!>: <!ELEMENT!> -> }

    var <!ELEMENT!>: Boolean;

    konst x3 = fun(<!ELEMENT!>: Boolean) {

    }(<!ELEMENT!>)
}
