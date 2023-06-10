/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.model

import org.jetbrains.kotlin.test.TargetBackend

class RunTestMethodModel(
    konst targetBackend: TargetBackend,
    konst testMethodName: String,
    konst testRunnerMethodName: String,
    konst additionalRunnerArguments: List<String> = emptyList(),
    konst withTransformer: Boolean = false
) : MethodModel {
    object Kind : MethodModel.Kind()

    override konst kind: MethodModel.Kind
        get() = Kind

    override konst name = METHOD_NAME
    override konst dataString: String? = null

    override konst tags: List<String>
        get() = emptyList()

    override fun imports(): Collection<Class<*>> {
        return super.imports() + if (isWithTargetBackend()) setOf(TargetBackend::class.java) else emptySet()
    }

    fun isWithTargetBackend(): Boolean {
        return !(targetBackend == TargetBackend.ANY && additionalRunnerArguments.isEmpty() && testRunnerMethodName == METHOD_NAME)
    }

    companion object {
        const konst METHOD_NAME = "runTest"
    }
}
