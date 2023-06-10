// WITH_STDLIB

package test

internal annotation class MyAnnotation

@MyAnnotation
internal class State @JvmOverloads constructor(
        konst someInt: Int,
        konst someLong: Long,
        konst someString: String = ""
)
