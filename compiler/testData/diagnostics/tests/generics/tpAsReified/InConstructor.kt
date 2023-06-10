// FIR_IDENTICAL
class C<<!REIFIED_TYPE_PARAMETER_NO_INLINE!>reified<!> T>

fun <T> id(p: T): T = p

fun <A> main() {
    <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>C<!>()

    konst a: C<A> = <!TYPE_PARAMETER_AS_REIFIED!>C<!>()
    C<<!TYPE_PARAMETER_AS_REIFIED!>A<!>>()

    konst b: C<Int> = C()
    C<Int>()

    // TODO svtk, uncomment when extensions are called for nested calls!
    //konst < !UNUSED_VARIABLE!>с< !>: C<A> = id(< !TYPE_PARAMETER_AS_REIFIED!>C< !>())
}
