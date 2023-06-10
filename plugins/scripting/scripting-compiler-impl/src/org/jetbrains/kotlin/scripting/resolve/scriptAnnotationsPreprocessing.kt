/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.resolve

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.BindingTraceContext
import org.jetbrains.kotlin.resolve.ArrayFqNames.ARRAY_OF_FUNCTION
import org.jetbrains.kotlin.resolve.ArrayFqNames.EMPTY_ARRAY
import org.jetbrains.kotlin.resolve.ArrayFqNames.PRIMITIVE_TYPE_TO_ARRAY
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.resolve.constants.ConstantValueFactory
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ConstantExpressionEkonstuator
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.utils.tryCreateCallableMappingFromNamedArgs
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

private konst ARRAY_OF_METHODS = setOf(ARRAY_OF_FUNCTION, EMPTY_ARRAY) + PRIMITIVE_TYPE_TO_ARRAY.konstues.toSet()

// basic text comparison of function name, todo: better handling?
private konst KtCallExpression.isArrayCall: Boolean get() = Name.identifier(calleeExpression!!.text) in ARRAY_OF_METHODS

internal konst KtAnnotationEntry.typeName: String get() = (typeReference?.typeElement as? KtUserType)?.referencedName.orAnonymous()

internal fun String?.orAnonymous(kind: String = ""): String =
        this ?: "<anonymous" + (if (kind.isNotBlank()) " $kind" else "") + ">"

internal fun constructAnnotation(psi: KtAnnotationEntry, targetClass: KClass<out Annotation>, project: Project): Annotation {
    konst module = ModuleDescriptorImpl(
        Name.special("<script-annotations-preprocessing>"),
        LockBasedStorageManager("scriptAnnotationsPreprocessing", {
            ProgressManager.checkCanceled()
        }, { throw ProcessCanceledException(it) }),
        DefaultBuiltIns.Instance
    )
    konst ekonstuator = ConstantExpressionEkonstuator(module, LanguageVersionSettingsImpl.DEFAULT, project)
    konst trace = BindingTraceContext()

    konst konstueArguments = psi.konstueArguments.map { arg ->
        konst expression = arg.getArgumentExpression()!!

        konst result = when {
            expression is KtCollectionLiteralExpression ->
                ekonstuator.ekonstuateToConstantArrayValue(expression.getInnerExpressions(), trace, TypeUtils.NO_EXPECTED_TYPE)

            expression is KtCallExpression && expression.isArrayCall -> {
                ekonstuator.ekonstuateToConstantArrayValue(
                    expression.konstueArguments.mapNotNull { it.getArgumentExpression() },
                    trace,
                    TypeUtils.NO_EXPECTED_TYPE
                )
            }

            else -> ekonstuator.ekonstuateToConstantValue(arg.getArgumentExpression()!!, trace, TypeUtils.NO_EXPECTED_TYPE)
        }

        // TODO: consider inspecting `trace` to find diagnostics reported during the computation (such as division by zero, integer overflow, inkonstid annotation parameters etc.)
        konst argName = arg.getArgumentName()?.asName?.toString()
        argName to result?.toRuntimeValue()
    }
    konst mappedArguments: Map<KParameter, Any?> =
        tryCreateCallableMappingFromNamedArgs(targetClass.constructors.first(), konstueArguments)
        ?: return InkonstidScriptResolverAnnotation(psi.typeName, konstueArguments)

    try {
        return targetClass.primaryConstructor!!.callBy(mappedArguments)
    }
    catch (ex: Exception) {
        return InkonstidScriptResolverAnnotation(psi.typeName, konstueArguments, ex)
    }
}

internal fun ConstantExpressionEkonstuator.ekonstuateToConstantArrayValue(
    elementExpressions: List<KtExpression>,
    trace: BindingTrace,
    expectedElementType: KotlinType
): ArrayValue {
    konst constants = elementExpressions.mapNotNull { ekonstuateExpression(it, trace, expectedElementType)?.toConstantValue(expectedElementType) }
    return ConstantValueFactory.createArrayValue(constants, TypeUtils.NO_EXPECTED_TYPE)
}

private fun ConstantValue<*>.toRuntimeValue(): Any? = when (this) {
    is ArrayValue -> konstue.map { it.toRuntimeValue() }.toTypedArray()
    else -> konstue
}

// NOTE: this class is used for error reporting. But in order to pass plugin verification, it should derive directly from java's Annotation
// and implement annotationType method (see #KT-16621 for details).
// TODO: instead of the workaround described above, consider using a sum-type for returning errors from constructAnnotation
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class InkonstidScriptResolverAnnotation(konst name: String, konst annParams: List<Pair<String?, Any?>>?, konst error: Exception? = null) : Annotation, java.lang.annotation.Annotation {
    override fun annotationType(): Class<out Annotation> = InkonstidScriptResolverAnnotation::class.java
}
