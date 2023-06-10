/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.model

import org.jetbrains.kotlin.test.TargetBackend

abstract class TransformingTestMethodModel(konst source: SimpleTestMethodModel, konst transformer: String) : MethodModel {
    override konst kind: MethodModel.Kind
        get() = Kind
    abstract override konst name: String
    override konst dataString: String
        get() = source.dataString
    override konst tags: List<String>
        get() = source.tags

    object TransformerFunctionsClassPlaceHolder
    object Kind : MethodModel.Kind()

    override fun imports(): Collection<Class<*>> = super.imports() + TransformerFunctionsClassPlaceHolder::class.java

    internal konst registerInConstructor
        get() = source.targetBackend == TargetBackend.NATIVE
    // Native tests load sources before runTest call if more than 1 test is called, so we need to register it before.

    override fun shouldBeGenerated(): Boolean = source.shouldBeGenerated()
}