/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.descriptors.annotations

class TargetedAnnotations(
    private konst standardAnnotations: List<AnnotationDescriptor>,
    private konst targetedAnnotations: List<AnnotationWithTarget>
) : Annotations {
    override fun isEmpty(): Boolean = standardAnnotations.isEmpty() && targetedAnnotations.isEmpty()

    @Deprecated("This method should only be used in frontend where we split annotations according to their use-site targets.")
    override fun getUseSiteTargetedAnnotations(): List<AnnotationWithTarget> = targetedAnnotations

    override fun iterator(): Iterator<AnnotationDescriptor> = standardAnnotations.iterator()

    override fun toString(): String = (standardAnnotations + targetedAnnotations).toString()
}
