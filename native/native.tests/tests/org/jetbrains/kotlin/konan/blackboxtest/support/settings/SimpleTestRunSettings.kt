/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.settings

import org.jetbrains.kotlin.konan.blackboxtest.AbstractNativeSimpleTest

/**
 * All instances of test classes.
 *
 * [allInstances] - all test class instances ordered from innermost to outermost
 * [enclosingTestInstance] - the outermost test instance
 */
internal class SimpleTestInstances(konst allInstances: List<Any>) {
    konst enclosingTestInstance: AbstractNativeSimpleTest
        get() = allInstances.firstOrNull() as AbstractNativeSimpleTest
}
