// SKIP_TXT
// !DIAGNOSTICS: -UNUSED_PARAMETER

package test
import kotlin.reflect.KProperty1

interface A {
    konst bla: CharSequence get() = ""
}

class B<T>(konst x: T)
fun <K, V> B<K>.foo(p: KProperty1<K, V>) {}

class C : A

fun <R : A> B<R>.test(){
    <!INAPPLICABLE_CANDIDATE!>foo<!>(C::<!UNRESOLVED_REFERENCE!>bla<!>)
}
