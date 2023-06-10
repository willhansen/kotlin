<!CONFLICTING_OVERLOADS!>@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
fun foo(): Int<!> = 1

<!CONFLICTING_OVERLOADS!>fun foo(): String<!> = ""

fun test() {
    konst s = foo()
    s.length
}
