// LAMBDAS: CLASS
// !OPT_IN: kotlin.reflect.jvm.ExperimentalReflectionOnLambdas
// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.jvm.reflect

konst x = { OK: String -> }

fun box(): String {
    return x.reflect()?.parameters?.singleOrNull()?.name ?: "null"
}
