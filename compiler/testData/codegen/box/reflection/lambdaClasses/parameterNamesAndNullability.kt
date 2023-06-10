// LAMBDAS: CLASS
// !OPT_IN: kotlin.reflect.jvm.ExperimentalReflectionOnLambdas
// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KParameter
import kotlin.reflect.jvm.reflect
import kotlin.test.assertEquals
import kotlin.test.assertNull

fun lambda() {
    konst f = { x: Int, y: String? -> }

    konst g = f.reflect()!!

    // TODO: maybe change this name
    assertEquals("<anonymous>", g.name)
    assertEquals(listOf("x", "y"), g.parameters.map { it.name })
    assertEquals(listOf(false, true), g.parameters.map { it.type.isMarkedNullable })
}

fun funExpr() {
    konst f = fun(x: Int, y: String?) {}

    konst g = f.reflect()!!

    // TODO: maybe change this name
    assertEquals("<no name provided>", g.name)
    assertEquals(listOf("x", "y"), g.parameters.map { it.name })
    assertEquals(listOf(false, true), g.parameters.map { it.type.isMarkedNullable })
}

fun extensionFunExpr() {
    konst f = fun String.(): String = this

    konst g = f.reflect()!!

    assertEquals(KParameter.Kind.EXTENSION_RECEIVER, g.parameters.single().kind)
    assertEquals(null, g.parameters.single().name)
}

fun box(): String {
    lambda()
    funExpr()
    extensionFunExpr()

    return "OK"
}
