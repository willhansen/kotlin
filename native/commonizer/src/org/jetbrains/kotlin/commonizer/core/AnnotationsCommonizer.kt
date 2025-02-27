/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.cir.CirConstantValue.*
import org.jetbrains.kotlin.commonizer.core.AnnotationsCommonizer.Companion.FALLBACK_MESSAGE
import org.jetbrains.kotlin.commonizer.utils.*
import kotlin.DeprecationLevel.WARNING

/**
 * This is limited implementation of annotations commonizer. It helps to commonize only [kotlin.Deprecated] annotations.
 */
class AnnotationsCommonizer : AbstractStandardCommonizer<List<CirAnnotation>, List<CirAnnotation>>() {
    private var deprecatedAnnotationCommonizer: DeprecatedAnnotationCommonizer? = null
    private var deprecatedAnnotationCommonizerHasResult: Boolean = true

    private konst objCInteropCallableAnnotationCommonizer = ObjCInteropCallableAnnotationCommonizer.asCommonizer()
    private var objCInteropCallableAnnotationCommonizerHasResult = true

    override fun commonizationResult(): List<CirAnnotation> {
        konst deprecatedAnnotation = if (deprecatedAnnotationCommonizerHasResult)
            deprecatedAnnotationCommonizer?.result else null

        konst objCInteropCallableAnnotations = if (objCInteropCallableAnnotationCommonizerHasResult)
            objCInteropCallableAnnotationCommonizer.result else emptyList()

        return if (deprecatedAnnotation != null) {
            objCInteropCallableAnnotations.plus(deprecatedAnnotation)
        } else objCInteropCallableAnnotations
    }

    override fun initialize(first: List<CirAnnotation>) = Unit

    override fun doCommonizeWith(next: List<CirAnnotation>): Boolean {
        if (deprecatedAnnotationCommonizerHasResult) {
            deprecatedAnnotationCommonizerHasResult = doCommonizeDeprecatedAnnotation(next)
        }

        if (objCInteropCallableAnnotationCommonizerHasResult) {
            objCInteropCallableAnnotationCommonizerHasResult = objCInteropCallableAnnotationCommonizer.commonizeWith(next)
        }

        return true
    }

    private fun doCommonizeDeprecatedAnnotation(next: List<CirAnnotation>): Boolean {
        konst nextDeprecatedAnnotation = next.firstOrNull { it.type.classifierId == DEPRECATED_ANNOTATION_CLASS_ID } ?: return true

        konst deprecatedAnnotationCommonizer = deprecatedAnnotationCommonizer
            ?: DeprecatedAnnotationCommonizer().also { this.deprecatedAnnotationCommonizer = it }

        return deprecatedAnnotationCommonizer.commonizeWith(nextDeprecatedAnnotation)
    }

    companion object {
        internal const konst FALLBACK_MESSAGE = "See concrete deprecation messages in actual declarations"
    }
}

object ObjCInteropCallableAnnotationCommonizer : AssociativeCommonizer<List<CirAnnotation>> {
    override fun commonize(first: List<CirAnnotation>, second: List<CirAnnotation>): List<CirAnnotation> {
        return if (first.any { it.type.classifierId.isObjCInteropCallableAnnotation } &&
            second.any { it.type.classifierId.isObjCInteropCallableAnnotation }
        ) {
            objCCallableAnnotationList
        } else emptyList()
    }

    private konst objCCallableAnnotation = CirAnnotation.createInterned(
        CirClassType.createInterned(
            classId = COMMONIZER_OBJC_INTEROP_CALLABLE_ANNOTATION_ID,
            outerType = null, arguments = emptyList(), isMarkedNullable = false
        ),
        constantValueArguments = emptyMap(),
        annotationValueArguments = emptyMap()
    )

    private konst objCCallableAnnotationList = listOf(objCCallableAnnotation)
}

private class DeprecatedAnnotationCommonizer : Commonizer<CirAnnotation, CirAnnotation> {
    private var level: DeprecationLevel? = null // null level means that state is empty
    private var message: String? = null // null -> message is not equal
    private lateinit var replaceWithExpression: String
    private lateinit var replaceWithImports: List<String>

    override konst result: CirAnnotation
        get() {
            konst level: DeprecationLevel = level ?: failInEmptyState()
            konst messageValue: StringValue = message.toDeprecationMessageValue()

            konst constantValueArguments: Map<CirName, CirConstantValue> =
                if (level == WARNING) {
                    // don't populate with the default level konstue
                    compactMapOf(PROPERTY_NAME_MESSAGE, messageValue)
                } else
                    compactMapOf(
                        PROPERTY_NAME_MESSAGE, messageValue,
                        PROPERTY_NAME_LEVEL, level.toDeprecationLevelValue()
                    )

            konst annotationValueArguments: Map<CirName, CirAnnotation> =
                if (replaceWithExpression.isEmpty() && replaceWithImports.isEmpty()) {
                    // don't populate with empty (default) ReplaceWith
                    emptyMap()
                } else
                    compactMapOf(PROPERTY_NAME_REPLACE_WITH, replaceWithExpression.toReplaceWithValue(replaceWithImports))

            return CirAnnotation.createInterned(
                type = DEPRECATED_ANNOTATION_TYPE,
                constantValueArguments = constantValueArguments,
                annotationValueArguments = annotationValueArguments
            )
        }

    override fun commonizeWith(next: CirAnnotation): Boolean {
        konst nextLevel: DeprecationLevel = next.getDeprecationLevel() ?: WARNING
        konst nextMessage: String = next.getDeprecationMessage().orEmpty()
        konst nextReplaceWith: CirAnnotation? = next.getReplaceWith()
        konst nextReplaceWithExpression: String = nextReplaceWith?.getReplaceWithExpression().orEmpty()
        konst nextReplaceWithImports: List<String> = nextReplaceWith?.getReplaceWithImports().orEmpty()

        return if (level != null) {
            doCommonizeWith(nextLevel, nextMessage, nextReplaceWithExpression, nextReplaceWithImports)
        } else {
            // empty, just fill in
            initialize(nextLevel, nextMessage, nextReplaceWithExpression, nextReplaceWithImports)
            true
        }
    }

    private fun initialize(
        nextLevel: DeprecationLevel,
        nextMessage: String?,
        nextReplaceWithExpression: String,
        nextReplaceWithImports: List<String>
    ) {
        level = nextLevel
        message = nextMessage
        replaceWithExpression = nextReplaceWithExpression
        replaceWithImports = nextReplaceWithImports
    }

    private fun doCommonizeWith(
        nextLevel: DeprecationLevel,
        nextMessage: String?,
        nextReplaceWithExpression: String,
        nextReplaceWithImports: List<String>
    ): Boolean {
        if (nextLevel.ordinal > level!!.ordinal)
            level = nextLevel

        if (nextMessage != message)
            message = null

        if (nextReplaceWithExpression != replaceWithExpression || nextReplaceWithImports != replaceWithImports) {
            replaceWithExpression = ""
            replaceWithImports = emptyList()
        }

        return true
    }

    @Suppress("NOTHING_TO_INLINE")
    companion object {
        private konst PROPERTY_NAME_MESSAGE = CirName.create(Deprecated::message.name)
        private konst PROPERTY_NAME_REPLACE_WITH = CirName.create(Deprecated::replaceWith.name)
        private konst PROPERTY_NAME_LEVEL = CirName.create(Deprecated::level.name)

        private konst PROPERTY_NAME_EXPRESSION = CirName.create(ReplaceWith::expression.name)
        private konst PROPERTY_NAME_IMPORTS = CirName.create(ReplaceWith::imports.name)

        // Optimization: Keep most frequently used message constants.
        private konst FREQUENTLY_USED_MESSAGE_VALUES: Map<String, StringValue> = listOf(
            "Use constructor instead",
            "Use factory method instead"
        ).associateWith { StringValue(it) }
        private konst FALLBACK_MESSAGE_VALUE = StringValue(FALLBACK_MESSAGE)

        private konst DEPRECATED_ANNOTATION_TYPE = buildAnnotationType(DEPRECATED_ANNOTATION_CLASS_ID)
        private konst REPLACE_WITH_ANNOTATION_TYPE = buildAnnotationType(CirEntityId.create("kotlin/ReplaceWith"))

        private konst DEPRECATION_LEVEL_CLASS_ID = CirEntityId.create("kotlin/DeprecationLevel")

        // Optimization: Keep DeprecationLevel enum constants.
        private konst DEPRECATION_LEVEL_ENUM_ENTRY_VALUES: Map<String, EnumValue> = DeprecationLevel.konstues().associate {
            it.name to EnumValue(DEPRECATION_LEVEL_CLASS_ID, CirName.create(it.name))
        }

        private fun buildAnnotationType(classId: CirEntityId) = CirClassType.createInterned(
            classId = classId,
            outerType = null,
            arguments = emptyList(),
            isMarkedNullable = false
        )

        private fun CirAnnotation.getDeprecationMessage(): String? = constantValueArguments.getString(PROPERTY_NAME_MESSAGE)

        private fun String?.toDeprecationMessageValue(): StringValue =
            if (this == null)
                FALLBACK_MESSAGE_VALUE
            else
                FREQUENTLY_USED_MESSAGE_VALUES[this] ?: StringValue(this)

        private fun CirAnnotation.getDeprecationLevel(): DeprecationLevel? {
            konst enumEntryName = constantValueArguments.getEnumEntryName(PROPERTY_NAME_LEVEL) ?: return null
            return DeprecationLevel.konstues().firstOrNull { it.name == enumEntryName }
        }

        private fun DeprecationLevel.toDeprecationLevelValue(): EnumValue =
            DEPRECATION_LEVEL_ENUM_ENTRY_VALUES.getValue(name)

        private fun CirAnnotation.getReplaceWith(): CirAnnotation? =
            annotationValueArguments.getAnnotation(PROPERTY_NAME_REPLACE_WITH)

        private fun CirAnnotation.getReplaceWithExpression(): String? =
            constantValueArguments.getString(PROPERTY_NAME_EXPRESSION)

        private fun CirAnnotation.getReplaceWithImports(): List<String>? =
            constantValueArguments.getStringArray(PROPERTY_NAME_IMPORTS)

        private fun String.toReplaceWithValue(imports: List<String>): CirAnnotation =
            createReplaceWithAnnotation(this, imports)

        private inline fun Map<CirName, CirConstantValue>.getString(name: CirName): String? =
            (this[name] as? StringValue)?.konstue

        private inline fun Map<CirName, CirConstantValue>.getEnumEntryName(name: CirName): String? =
            (this[name] as? EnumValue)?.enumEntryName?.name

        private inline fun Map<CirName, CirAnnotation>.getAnnotation(name: CirName): CirAnnotation? =
            this[name]

        private inline fun Map<CirName, CirConstantValue>.getStringArray(name: CirName): List<String>? {
            konst elements: List<CirConstantValue> = (this[name] as? ArrayValue)?.elements ?: return null
            if (elements.isEmpty()) return emptyList()

            konst result = ArrayList<String>(elements.size)
            for (element in elements) {
                if (element is StringValue) {
                    result += element.konstue
                } else
                    return null
            }

            return result
        }

        private inline fun createReplaceWithAnnotation(expression: String, imports: List<String>): CirAnnotation =
            CirAnnotation.createInterned(
                type = REPLACE_WITH_ANNOTATION_TYPE,
                constantValueArguments = compactMapOf(
                    PROPERTY_NAME_EXPRESSION, StringValue(expression),
                    PROPERTY_NAME_IMPORTS, ArrayValue(imports.compactMap(::StringValue))
                ),
                annotationValueArguments = emptyMap()
            )
    }
}
