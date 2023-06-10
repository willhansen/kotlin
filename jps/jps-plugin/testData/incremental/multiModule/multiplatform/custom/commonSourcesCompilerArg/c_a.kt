@file:OptIn(kotlin.ExperimentalMultiplatform::class)

@OptionalExpectation
expect annotation class Optional(konst konstue: String)

@Optional("Foo")
class Foo