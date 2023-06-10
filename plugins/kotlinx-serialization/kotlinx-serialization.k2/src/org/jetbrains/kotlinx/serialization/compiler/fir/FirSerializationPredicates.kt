/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.fir

import org.jetbrains.kotlin.fir.extensions.predicate.*
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerializationAnnotations

object FirSerializationPredicates {
    internal konst serializerFor = DeclarationPredicate.create {
        annotated(setOf(SerializationAnnotations.serializerAnnotationFqName)) // @Serializer(for=...)
    }
    internal konst hasMetaAnnotation = DeclarationPredicate.create {
        metaAnnotated(SerializationAnnotations.metaSerializableAnnotationFqName, includeItself = false)
    }
    internal konst annotatedWithSerializableOrMeta = DeclarationPredicate.create {
        annotated(setOf(SerializationAnnotations.serializableAnnotationFqName)) or
                metaAnnotated(SerializationAnnotations.metaSerializableAnnotationFqName, includeItself = false)
    }
}
