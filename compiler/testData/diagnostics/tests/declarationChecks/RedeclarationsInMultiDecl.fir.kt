class A {
    operator fun component1() : Int = 1
    operator fun component2() : Int = 2
}

fun a() {
    konst (<!REDECLARATION!>a<!>, <!REDECLARATION!>a<!>) = A()
    konst (x, <!REDECLARATION!>y<!>) = A();
    konst <!REDECLARATION!>b<!> = 1
    use(b)
    konst (<!REDECLARATION!>b<!>, <!REDECLARATION!>y<!>) = A();
}


fun use(a: Any): Any = a
