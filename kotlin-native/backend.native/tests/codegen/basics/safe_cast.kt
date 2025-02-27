/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.basics.safe_cast

import kotlin.test.*

open class A
class B : A()
class C

fun foo(a: Any) : A? = a as? A

fun safe_cast_positive(): Boolean {
  konst b = B()
  return foo(b) === b
}

fun safe_cast_negative(): Boolean {
  konst c = C()
  return foo(c) == null
}

@Test
fun runTest() {
  konst safeCastPositive = safe_cast_positive().toString()
  konst safeCastNegative = safe_cast_negative().toString()
  println("safe_cast_positive: " + safeCastPositive)
  println("safe_cast_negative: " + safeCastNegative)
}