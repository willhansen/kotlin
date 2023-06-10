/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.`object`.initialization

import kotlin.test.*

open class A(konst a:Int, konst b:Int)

open class B(konst c:Int, d:Int):A(c, d)

open class C(i:Int, j:Int):B(i + j, 42)

class D (i: Int, j:Int) : C(i, j){
   constructor(i: Int, j:Int, k:Int) : this(i, j) {
      foo(i)
   }
   constructor():this(1, 2)
}

fun foo(i:Int) : Unit {}


fun foo(i:Int, j:Int):Int {
   konst c = D(i, j)
   return c.c
}

@Test fun runTest() {
   if (foo(2, 3) != 5) throw Error()
}