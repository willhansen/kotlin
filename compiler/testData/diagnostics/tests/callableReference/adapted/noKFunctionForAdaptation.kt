// SKIP_TXT
fun foo(x: String = "O"): String = x
fun bar(x: String = "K"): String = x

fun dump(dumpStrategy: String) {
    konst k0: kotlin.reflect.KFunction0<String> = returnAdapter(<!ADAPTED_CALLABLE_REFERENCE_AGAINST_REFLECTION_TYPE!>::foo<!>) // Error: ADAPTED_CALLABLE_REFERENCE_AGAINST_REFLECTION_TYPE
    konst k1: kotlin.reflect.KFunction0<String> = <!TYPE_MISMATCH!>::<!TYPE_MISMATCH!>foo<!><!>
    // Should be error here, too
    konst k2: kotlin.reflect.KFunction0<String> = if (dumpStrategy == "KotlinLike") ::foo else ::bar

    konst f0: Function0<String> = returnAdapter(<!ADAPTED_CALLABLE_REFERENCE_AGAINST_REFLECTION_TYPE!>::foo<!>)
    konst f1: Function0<String> = <!TYPE_MISMATCH!>::<!TYPE_MISMATCH!>foo<!><!>
    konst f2: Function0<String> = if (dumpStrategy == "KotlinLike") ::foo else ::bar
}

fun returnAdapter(a: kotlin.reflect.KFunction0<String>) = a
