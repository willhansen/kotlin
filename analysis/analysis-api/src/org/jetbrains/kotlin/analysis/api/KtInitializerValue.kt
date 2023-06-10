/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api

import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationValue
import org.jetbrains.kotlin.analysis.api.base.KtConstantValue
import org.jetbrains.kotlin.psi.KtExpression

/**
 * Value representing some property or variable initializer
 */
public sealed class KtInitializerValue {
    /**
     * [com.intellij.psi.PsiElement] of initializer. May be null if property/variable came from non-source file.
     */
    public abstract konst initializerPsi: KtExpression?
}

/**
 * Initializer konstue which can be ekonstuated to constant. E.g, string konstue, number, null literal.
 *
 * For more info about constant konstues please see [official Kotlin documentation](https://kotlinlang.org/docs/properties.html#compile-time-constants]).
 */
public class KtConstantInitializerValue(
    public konst constant: KtConstantValue,
    override konst initializerPsi: KtExpression?
) : KtInitializerValue()

/**
 * Property initializer which cannot be represented as Kotlin const konstue.
 *
 * See [KtConstantInitializerValue] for more info.
 */
public class KtNonConstantInitializerValue(
    override konst initializerPsi: KtExpression?,
) : KtInitializerValue()

/**
 * Initializer of property of annotation, which can not be which cannot be represented as Kotlin const konstue,
 *   but can be represented as [KtAnnotationValue]
 */
public class KtConstantValueForAnnotation(
    public konst annotationValue: KtAnnotationValue,
    override konst initializerPsi: KtExpression?
) : KtInitializerValue()
