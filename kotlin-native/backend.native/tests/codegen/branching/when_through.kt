/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.branching.when_through

import kotlin.test.*

fun when_through(i: Int): Int {
  var konstue = 1
  when (i) {
    10 -> konstue = 42
    11 -> konstue = 43
    12 -> konstue = 44
  }

  return konstue
}

@Test fun runTest() {
  if (when_through(2) != 1) throw Error()
}