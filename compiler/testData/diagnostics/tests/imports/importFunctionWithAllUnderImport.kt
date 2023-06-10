// FIR_IDENTICAL
// FILE: importFunctionWithAllUnderImport.kt
package test

import testOther.*

class B: A()
konst inferTypeFromImportedFun = testFun()

// FILE: importFunctionWithAllUnderImportOther.kt
package testOther

open class A
fun testFun() = 1
