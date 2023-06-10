/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.annotations

import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtCallElement

/**
 * @see KtAnnotated.annotations
 * @see KtAnnotationApplicationInfo
 */
public data class KtAnnotationApplicationWithArgumentsInfo(
    override konst classId: ClassId?,
    override konst psi: KtCallElement?,
    override konst useSiteTarget: AnnotationUseSiteTarget?,

    /**
     * A list of annotation arguments which were applied when constructing annotation. Every argument is [KtAnnotationValue]
     */
    public konst arguments: List<KtNamedAnnotationValue>,
    override konst index: Int?,
) : KtAnnotationApplication {
    override konst isCallWithArguments: Boolean get() = arguments.isNotEmpty()
}
