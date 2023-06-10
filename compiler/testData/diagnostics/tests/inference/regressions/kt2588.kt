// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
//T-2588 Allow to specify exact super type (expected) in inference if many

import java.util.HashSet

class MyClass<T>()

interface A
interface D
class B : A, D
class C : A, D

fun <T> hashSetOf(vararg konstues: T): HashSet<T> = throw Exception("$konstues")

fun foo(b: MyClass<B>, c: MyClass<C>) {
    konst set1 : Set<MyClass<out D>> = hashSetOf(b, c) //type inference expected type mismatch
    konst set2  = hashSetOf(b, c) //Set<MyClass<out Any>> is inferred
}