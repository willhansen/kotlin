/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services

import org.jetbrains.kotlin.test.model.ServicesAndDirectivesContainer

abstract class MetaTestConfigurator(protected konst testServices: TestServices) : ServicesAndDirectivesContainer {
    open fun transformTestDataPath(testDataFileName: String): String = testDataFileName

    open fun shouldSkipTest(): Boolean = false
}
