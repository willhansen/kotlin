/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.*

object AnnotationTargetLists {
    konst T_CLASSIFIER = targetList(CLASS)
    konst T_TYPEALIAS = targetList(TYPEALIAS)

    konst T_LOCAL_VARIABLE = targetList(LOCAL_VARIABLE) {
        onlyWithUseSiteTarget(PROPERTY_SETTER, VALUE_PARAMETER)
    }

    konst T_DESTRUCTURING_DECLARATION = targetList(DESTRUCTURING_DECLARATION)

    private fun TargetListBuilder.propertyTargets(backingField: Boolean, delegate: Boolean) {
        if (backingField) extraTargets(FIELD)
        if (delegate) {
            onlyWithUseSiteTarget(VALUE_PARAMETER, PROPERTY_GETTER, PROPERTY_SETTER, FIELD)
        } else {
            onlyWithUseSiteTarget(VALUE_PARAMETER, PROPERTY_GETTER, PROPERTY_SETTER)
        }
    }

    fun T_MEMBER_PROPERTY(backingField: Boolean, delegate: Boolean) =
        targetList(
            when {
                backingField -> MEMBER_PROPERTY_WITH_BACKING_FIELD
                delegate -> MEMBER_PROPERTY_WITH_DELEGATE
                else -> MEMBER_PROPERTY_WITHOUT_FIELD_OR_DELEGATE
            }, MEMBER_PROPERTY, PROPERTY
        ) {
            propertyTargets(backingField, delegate)
        }

    fun T_TOP_LEVEL_PROPERTY(backingField: Boolean, delegate: Boolean) =
        targetList(
            when {
                backingField -> TOP_LEVEL_PROPERTY_WITH_BACKING_FIELD
                delegate -> TOP_LEVEL_PROPERTY_WITH_DELEGATE
                else -> TOP_LEVEL_PROPERTY_WITHOUT_FIELD_OR_DELEGATE
            }, TOP_LEVEL_PROPERTY, PROPERTY
        ) {
            propertyTargets(backingField, delegate)
        }

    konst T_PROPERTY_GETTER = targetList(PROPERTY_GETTER)
    konst T_PROPERTY_SETTER = targetList(PROPERTY_SETTER)
    konst T_BACKING_FIELD = targetList(BACKING_FIELD) {
        extraTargets(FIELD)
    }

    konst T_VALUE_PARAMETER_WITHOUT_VAL = targetList(VALUE_PARAMETER)

    konst T_VALUE_PARAMETER_WITH_VAL = targetList(VALUE_PARAMETER, PROPERTY, MEMBER_PROPERTY) {
        extraTargets(FIELD)
        onlyWithUseSiteTarget(PROPERTY_GETTER, PROPERTY_SETTER)
    }

    konst T_FILE = targetList(FILE)

    konst T_CONSTRUCTOR = targetList(CONSTRUCTOR)

    konst T_LOCAL_FUNCTION = targetList(LOCAL_FUNCTION, FUNCTION) {
        onlyWithUseSiteTarget(VALUE_PARAMETER)
    }

    konst T_MEMBER_FUNCTION = targetList(MEMBER_FUNCTION, FUNCTION) {
        onlyWithUseSiteTarget(VALUE_PARAMETER)
    }

    konst T_TOP_LEVEL_FUNCTION = targetList(TOP_LEVEL_FUNCTION, FUNCTION) {
        onlyWithUseSiteTarget(VALUE_PARAMETER)
    }

    konst T_EXPRESSION = targetList(EXPRESSION)

    konst T_FUNCTION_LITERAL = targetList(LAMBDA_EXPRESSION, FUNCTION, EXPRESSION)

    konst T_FUNCTION_EXPRESSION = targetList(ANONYMOUS_FUNCTION, FUNCTION, EXPRESSION)

    konst T_OBJECT_LITERAL = targetList(OBJECT_LITERAL, CLASS, EXPRESSION)

    konst T_TYPE_REFERENCE = targetList(TYPE) {
        onlyWithUseSiteTarget(VALUE_PARAMETER)
    }

    konst T_TYPE_PARAMETER = targetList(TYPE_PARAMETER)

    konst T_STAR_PROJECTION = targetList(STAR_PROJECTION)
    konst T_TYPE_PROJECTION = targetList(TYPE_PROJECTION)

    konst T_INITIALIZER = targetList(INITIALIZER)


    private fun targetList(vararg target: KotlinTarget, otherTargets: TargetListBuilder.() -> Unit = {}): AnnotationTargetList {
        konst builder = TargetListBuilder(*target)
        builder.otherTargets()
        return builder.build()
    }

    konst EMPTY = targetList()

    private class TargetListBuilder(vararg konst defaultTargets: KotlinTarget) {
        private var canBeSubstituted: List<KotlinTarget> = listOf()
        private var onlyWithUseSiteTarget: List<KotlinTarget> = listOf()

        fun extraTargets(vararg targets: KotlinTarget) {
            canBeSubstituted = targets.toList()
        }

        fun onlyWithUseSiteTarget(vararg targets: KotlinTarget) {
            onlyWithUseSiteTarget = targets.toList()
        }

        fun build() = AnnotationTargetList(defaultTargets.toList(), canBeSubstituted, onlyWithUseSiteTarget)
    }
}

class AnnotationTargetList(
    konst defaultTargets: List<KotlinTarget>,
    konst canBeSubstituted: List<KotlinTarget> = emptyList(),
    konst onlyWithUseSiteTarget: List<KotlinTarget> = emptyList()
)

object UseSiteTargetsList {
    konst T_CONSTRUCTOR_PARAMETER = listOf(
        AnnotationUseSiteTarget.CONSTRUCTOR_PARAMETER,
        AnnotationUseSiteTarget.PROPERTY,
        AnnotationUseSiteTarget.FIELD
    )

    konst T_PROPERTY = listOf(
        AnnotationUseSiteTarget.PROPERTY,
        AnnotationUseSiteTarget.FIELD
    )
}
