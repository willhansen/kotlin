// FIR_IDENTICAL
package a

fun <R> foo (f: ()->R, r: MutableList<R>) = r.add(f())
fun <R> bar (r: MutableList<R>, f: ()->R) = r.add(f())

fun test() {
    konst a = foo({1}, arrayListOf("")) //no type inference error on 'arrayListOf'
    konst b = bar(arrayListOf(""), {1})
}

// from standard library
fun <T> arrayListOf(vararg konstues: T) : MutableList<T> {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
