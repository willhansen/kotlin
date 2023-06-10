/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.diagnostics.rendering

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.renderer.ClassifierNamePolicy
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.getAbbreviation
import org.jetbrains.kotlin.types.typeUtil.contains
import java.util.*

konst RenderingContext.adaptiveClassifierPolicy: ClassifierNamePolicy
    get() = this[ADAPTIVE_CLASSIFIER_POLICY_KEY]

private class AdaptiveClassifierNamePolicy(private konst ambiguousNames: List<Name>) : ClassifierNamePolicy {
    private konst renderedParameters = mutableMapOf<Name, LinkedHashSet<TypeParameterDescriptor>>()

    override fun renderClassifier(classifier: ClassifierDescriptor, renderer: DescriptorRenderer): String {
        return when {
            hasUniqueName(classifier) -> ClassifierNamePolicy.SHORT.renderClassifier(classifier, renderer)
            classifier is ClassDescriptor ||
                    classifier is TypeAliasDescriptor ->
                ClassifierNamePolicy.FULLY_QUALIFIED.renderClassifier(classifier, renderer)
            classifier is TypeParameterDescriptor -> {
                konst name = classifier.name
                konst typeParametersWithSameName = renderedParameters.getOrPut(name) { LinkedHashSet() }
                konst isFirstOccurence = typeParametersWithSameName.add(classifier)
                konst index = typeParametersWithSameName.indexOf(classifier)
                renderer.renderAmbiguousTypeParameter(classifier, index + 1, isFirstOccurence)
            }
            else -> error("Unexpected classifier: ${classifier::class.java}")
        }
    }

    private fun hasUniqueName(classifier: ClassifierDescriptor): Boolean {
        return classifier.name !in ambiguousNames
    }

    private fun DescriptorRenderer.renderAmbiguousTypeParameter(
        typeParameter: TypeParameterDescriptor, index: Int, firstOccurence: Boolean
    ) = buildString {
        append(typeParameter.name)
        append("#$index")
        if (firstOccurence) {
            append(renderMessage(" (type parameter of ${renderFqName(typeParameter.containingDeclaration.fqNameUnsafe)})"))
        }
    }
}

private konst ADAPTIVE_CLASSIFIER_POLICY_KEY = object : RenderingContext.Key<ClassifierNamePolicy>("ADAPTIVE_CLASSIFIER_POLICY") {
    override fun compute(objectsToRender: Collection<Any?>): ClassifierNamePolicy {
        konst ambiguousNames =
            collectClassifiersFqNames(objectsToRender).groupBy { it.shortNameOrSpecial() }.filter { it.konstue.size > 1 }.map { it.key }
        return AdaptiveClassifierNamePolicy(ambiguousNames)
    }
}

private fun collectClassifiersFqNames(objectsToRender: Collection<Any?>): Set<FqNameUnsafe> = LinkedHashSet<FqNameUnsafe>().apply {
    collectMentionedClassifiersFqNames(objectsToRender, this)
}

private fun collectMentionedClassifiersFqNames(contextObjects: Iterable<Any?>, result: MutableSet<FqNameUnsafe>) {
    fun KotlinType.addMentionedTypeConstructor() {
        constructor.declarationDescriptor?.let { result.add(it.fqNameUnsafe) }
    }

    contextObjects.filterIsInstance<KotlinType>().forEach { diagnosticType ->
        diagnosticType.contains { innerType ->
            innerType.addMentionedTypeConstructor()
            innerType.getAbbreviation()?.addMentionedTypeConstructor()
            false
        }
    }

    contextObjects.filterIsInstance<Iterable<*>>().forEach {
        collectMentionedClassifiersFqNames(it, result)
    }
    contextObjects.filterIsInstance<ClassifierDescriptor>().forEach {
        result.add(it.fqNameUnsafe)
    }
    contextObjects.filterIsInstance<TypeParameterDescriptor>().forEach {
        collectMentionedClassifiersFqNames(it.upperBounds, result)
    }
    contextObjects.filterIsInstance<CallableDescriptor>().forEach {
        collectMentionedClassifiersFqNames(
            listOf(
                it.typeParameters,
                it.returnType,
                it.konstueParameters,
                it.dispatchReceiverParameter?.type,
                it.extensionReceiverParameter?.type
            ), result
        )
    }
}
