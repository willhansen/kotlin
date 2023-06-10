// FILE: MultifileFacade.kt
@file:JvmMultifileClass
@file:JvmName("multifileFacade")

fun foo() = 42

konst x = 24

private fun privateFoo(): Int = 3

const konst x1 = 42

// FILE: SecondMultifileFacade.kt
@file:JvmMultifileClass
@file:JvmName("multifileFacade")

fun bar() = 24

konst y = 24

const konst y1 = 42
