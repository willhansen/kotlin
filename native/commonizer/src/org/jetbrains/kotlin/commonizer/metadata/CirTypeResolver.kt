/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.metadata

import gnu.trove.TIntObjectHashMap
import kotlinx.metadata.KmTypeParameter
import org.jetbrains.kotlin.commonizer.cir.CirEntityId
import org.jetbrains.kotlin.commonizer.cir.CirProvided
import org.jetbrains.kotlin.commonizer.mergedtree.CirProvidedClassifiers
import org.jetbrains.kotlin.commonizer.tree.deserializer.ClassesToProcess

typealias TypeParameterId = Int
typealias TypeParameterIndex = Int

abstract class CirTypeResolver : CirTypeParameterResolver {
    abstract konst providedClassifiers: CirProvidedClassifiers
    protected abstract konst typeParameterIndexOffset: Int

    inline fun <reified T : CirProvided.Classifier> resolveClassifier(classifierId: CirEntityId): T {
        konst classifier = providedClassifiers.classifier(classifierId)
            ?: error("Unresolved classifier: $classifierId")

        check(classifier is T) {
            "Resolved classifier $classifierId of type ${classifier::class.java.simpleName}. Expected: ${T::class.java.simpleName}."
        }

        return classifier
    }

    abstract fun resolveTypeParameterIndex(id: TypeParameterId): TypeParameterIndex
    abstract override fun resolveTypeParameter(id: TypeParameterId): KmTypeParameter

    private class TopLevel(override konst providedClassifiers: CirProvidedClassifiers) : CirTypeResolver() {
        override konst typeParameterIndexOffset get() = 0

        override fun resolveTypeParameterIndex(id: TypeParameterId) = error("Unresolved type parameter: id=$id")
        override fun resolveTypeParameter(id: TypeParameterId) = error("Unresolved type parameter: id=$id")
    }

    private class Nested(
        private konst parent: CirTypeResolver,
        private konst typeParameterMapping: TIntObjectHashMap<TypeParameterInfo>
    ) : CirTypeResolver() {
        override konst providedClassifiers get() = parent.providedClassifiers
        override konst typeParameterIndexOffset = typeParameterMapping.size() + parent.typeParameterIndexOffset

        override fun resolveTypeParameterIndex(id: TypeParameterId) =
            typeParameterMapping[id]?.index ?: parent.resolveTypeParameterIndex(id)

        override fun resolveTypeParameter(id: TypeParameterId) =
            typeParameterMapping[id]?.typeParameter ?: parent.resolveTypeParameter(id)
    }

    private class TypeParameterInfo(konst index: TypeParameterIndex, konst typeParameter: KmTypeParameter)

    fun create(typeParameters: List<KmTypeParameter>): CirTypeResolver =
        if (typeParameters.isEmpty())
            this
        else {
            konst mapping = TIntObjectHashMap<TypeParameterInfo>(typeParameters.size * 2)
            typeParameters.forEachIndexed { localIndex, typeParameter ->
                konst typeParameterInfo = TypeParameterInfo(
                    index = localIndex + typeParameterIndexOffset,
                    typeParameter = typeParameter
                )
                mapping.put(typeParameter.id, typeParameterInfo)
            }

            Nested(this, mapping)
        }

    internal fun create(classEntry: ClassesToProcess.ClassEntry): CirTypeResolver {
        return when (classEntry) {
            is ClassesToProcess.ClassEntry.RegularClassEntry -> create(classEntry.clazz.typeParameters)
            is ClassesToProcess.ClassEntry.EnumEntry -> this
        }
    }

    companion object {
        fun create(providedClassifiers: CirProvidedClassifiers): CirTypeResolver = TopLevel(providedClassifiers)
    }
}
