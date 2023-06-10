class A {
    @field:<!ELEMENT!>
    konst a: Int = ""

    @setparam:<!ELEMENT!>(<!ELEMENT!>)
    konst b: Int = ""

    @receiver:org.jetbrains.<!ELEMENT!><A<B, C>>(<!ELEMENT!>)
    konst c: Int = ""

    @org.jetbrains.<!ELEMENT!>
    konst c: Int = ""

    @<!ELEMENT!><A<B>, C>(<!ELEMENT!>, <!ELEMENT!>, <!ELEMENT!>)
    konst c: Int = ""

    @<!ELEMENT!>
    konst c: Int = ""

    @<!ELEMENT!>.<!ELEMENT!>.<!ELEMENT!><A<B>, C>(<!ELEMENT!>, <!ELEMENT!>, <!ELEMENT!>)
    konst c: Int = ""
}
