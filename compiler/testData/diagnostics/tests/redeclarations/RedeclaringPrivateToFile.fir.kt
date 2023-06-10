// FILE: a.kt
package a

interface A
interface B : A

private fun konstidFun() {}
private konst konstidVal = 1

<!CONFLICTING_OVERLOADS!>private fun inkonstidFun0()<!> {}
private konst <!REDECLARATION!>inkonstidProp0<!> = 1

// NB inkonstidFun0 and inkonstidProp0 are conflicting overloads, since the following is an ambiguity:
fun useInkonstidFun0() = inkonstidFun0()
fun useInkonstidProp0() = inkonstidProp0

<!CONFLICTING_OVERLOADS!>private fun inkonstidFun1()<!> {}
<!CONFLICTING_OVERLOADS!>private fun inkonstidFun1()<!> {}

<!CONFLICTING_OVERLOADS!>private fun inkonstidFun2()<!> {}
<!CONFLICTING_OVERLOADS!>public fun inkonstidFun2()<!> {}

<!CONFLICTING_OVERLOADS!>public fun inkonstidFun3()<!> {}

<!CONFLICTING_OVERLOADS!>private fun inkonstidFun4()<!> {}
<!CONFLICTING_OVERLOADS!>public fun inkonstidFun4()<!> {}

public fun konstidFun2(a: A) = a
public fun konstidFun2(b: B) = b

// FILE: b.kt
package a

private fun konstidFun() {}
private konst konstidVal = 1

<!CONFLICTING_OVERLOADS!>private fun inkonstidFun0()<!> {}

private konst <!REDECLARATION!>inkonstidProp0<!> = 1

<!CONFLICTING_OVERLOADS!>internal fun inkonstidFun3()<!> {}
<!CONFLICTING_OVERLOADS!>internal fun inkonstidFun4()<!> {}

// FILE: c.kt
package a

public fun inkonstidFun0() {}

public konst inkonstidProp0 = 1
