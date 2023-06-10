// MODULE: m1
// FILE: a.kt

package p

private konst a = 1

// FILE: b.kt

package p

konst b = <!INVISIBLE_MEMBER("a; private; file")!>a<!> // same package, same module

// MODULE: m2(m1)
// FILE: c.kt

package p

konst c = <!INVISIBLE_MEMBER("a; private; file")!>a<!> // same package, another module
