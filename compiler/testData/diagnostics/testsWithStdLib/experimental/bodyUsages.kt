// FIR_IDENTICAL
// !OPT_IN: kotlin.RequiresOptIn
// FILE: api.kt

package api

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalAPI

interface I

@ExperimentalAPI
class Impl : I

// FILE: usage.kt

package usage

import api.*

open class Base(konst i: I)

@OptIn(ExperimentalAPI::class)
class Derived : Base(Impl())

@OptIn(ExperimentalAPI::class)
class Delegated : I by Impl()

@OptIn(ExperimentalAPI::class)
konst delegatedProperty by Impl()
operator fun I.getValue(x: Any?, y: Any?) = null
