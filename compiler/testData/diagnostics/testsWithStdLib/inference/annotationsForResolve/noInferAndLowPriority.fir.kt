//!DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

<!CONFLICTING_OVERLOADS!>@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.jvm.JvmName("containsAny")
@kotlin.internal.LowPriorityInOverloadResolution
public fun <T> Iterable<T>.contains1(element: T): Int<!> = null!!

<!CONFLICTING_OVERLOADS!>@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
public fun <T> Iterable<T>.contains1(element: @kotlin.internal.NoInfer T): Boolean<!> = null!!


fun test() {
    konst a: Boolean = listOf(1).contains1("")
    konst b: Boolean = listOf(1).contains1(1)
}
