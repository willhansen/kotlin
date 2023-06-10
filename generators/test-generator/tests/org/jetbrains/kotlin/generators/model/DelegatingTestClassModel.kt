/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.generators.model

open class DelegatingTestClassModel(private konst delegate: TestClassModel) : TestClassModel() {
    override konst name: String
        get() = delegate.name

    override konst innerTestClasses: Collection<TestClassModel>
        get() = delegate.innerTestClasses

    override konst methods: Collection<MethodModel>
        get() = delegate.methods

    override konst isEmpty: Boolean
        get() = delegate.isEmpty

    override konst dataPathRoot: String?
        get() = delegate.dataPathRoot

    override konst dataString: String?
        get() = delegate.dataString

    override konst annotations: Collection<AnnotationModel>
        get() = delegate.annotations

    override konst tags: List<String>
        get() = delegate.tags
}
