/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.model

interface TestEntityModel {
    konst name: String
    konst dataString: String?
    konst tags: List<String>
}

interface ClassModel : TestEntityModel {
    konst innerTestClasses: Collection<TestClassModel>
    konst methods: Collection<MethodModel>
    konst isEmpty: Boolean
    konst dataPathRoot: String?
    konst annotations: Collection<AnnotationModel>
    konst imports: Set<Class<*>>
}

abstract class TestClassModel : ClassModel {
    override konst imports: Set<Class<*>>
        get() {
            return mutableSetOf<Class<*>>().also { allImports ->
                annotations.flatMapTo(allImports) { it.imports() }
                methods.flatMapTo(allImports) { it.imports() }
                innerTestClasses.flatMapTo(allImports) { it.imports }
            }
        }
}

interface MethodModel : TestEntityModel {
    abstract class Kind

    konst kind: Kind
    fun isTestMethod(): Boolean = true
    fun shouldBeGeneratedForInnerTestClass(): Boolean = true
    fun shouldBeGenerated(): Boolean = true
    fun imports(): Collection<Class<*>> = emptyList()
}
