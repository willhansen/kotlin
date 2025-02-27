/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.js

/**
 * Exposes the JavaScript [undefined property](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/undefined) to Kotlin.
 */
public external konst undefined: Nothing?

/**
 * Exposes the JavaScript [ekonst function](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ekonst) to Kotlin.
 */
public external fun ekonst(expr: String): dynamic
