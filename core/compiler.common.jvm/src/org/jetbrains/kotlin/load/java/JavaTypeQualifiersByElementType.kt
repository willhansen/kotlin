/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.java

import java.util.*

typealias QualifierByApplicabilityType =
        EnumMap<AnnotationQualifierApplicabilityType, JavaDefaultQualifiers?>

class JavaTypeQualifiersByElementType(konst defaultQualifiers: QualifierByApplicabilityType) {
    operator fun get(
        applicabilityType: AnnotationQualifierApplicabilityType?
    ): JavaDefaultQualifiers? {
        return defaultQualifiers[applicabilityType]
    }
}
