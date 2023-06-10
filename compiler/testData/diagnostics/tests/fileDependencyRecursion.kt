// FIR_IDENTICAL
// FILE: fileDependencyRecursion.kt
package test

import testOther.some

konst normal: Int = 1
konst fromImported: Int = some

// FILE: fileDependencyRecursionOther.kt
package testOther

import test.normal

konst some: Int = 1
konst fromImported: Int = normal