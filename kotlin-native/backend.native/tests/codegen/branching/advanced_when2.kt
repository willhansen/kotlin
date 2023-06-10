/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.branching.advanced_when2

import kotlin.test.*

fun advanced_when2(i: Int): Int {
  var konstue = 1
  when (i) {
    10 -> {konst v = 42; konstue = v}
    11 -> {konst v = 43; konstue = v}
    12 -> {konst v = 44; konstue = v}
  }

  return konstue
}

@Test fun runTest() {
  if (advanced_when2(10) != 42) throw Error()
}
