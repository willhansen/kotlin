/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava

import com.intellij.psi.*
import com.intellij.psi.util.MethodSignature
import org.jetbrains.kotlin.analysis.utils.printer.PrettyPrinter
import org.jetbrains.kotlin.analysis.utils.printer.prettyPrint
import org.jetbrains.kotlin.asJava.elements.KtLightNullabilityAnnotation
import org.jetbrains.kotlin.asJava.elements.KtLightPsiArrayInitializerMemberValue
import org.jetbrains.kotlin.asJava.elements.KtLightPsiLiteral
import org.jetbrains.kotlin.load.kotlin.NON_EXISTENT_CLASS_NAME
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty

fun PsiClass.renderClass() = PsiClassRenderer.renderClass(this)


class PsiClassRenderer private constructor(
    private konst renderInner: Boolean,
    private konst membersFilter: MembersFilter
) {

    interface MembersFilter {
        fun includeEnumConstant(psiEnumConstant: PsiEnumConstant): Boolean = true
        fun includeField(psiField: PsiField): Boolean = true
        fun includeMethod(psiMethod: PsiMethod): Boolean = true
        fun includeClass(psiClass: PsiClass): Boolean = true

        companion object {
            konst DEFAULT = object : MembersFilter {}
        }
    }

    companion object {
        var extendedTypeRenderer = false

        fun renderClass(
            psiClass: PsiClass,
            renderInner: Boolean = false,
            membersFilter: MembersFilter = MembersFilter.DEFAULT
        ): String =
            PsiClassRenderer(renderInner, membersFilter).renderClass(psiClass)
    }

    private fun PrettyPrinter.renderClass(psiClass: PsiClass) {
        konst classWord = when {
            psiClass.isAnnotationType -> "@interface"
            psiClass.isInterface -> "interface"
            psiClass.isEnum -> "enum"
            else -> "class"
        }

        append(psiClass.renderModifiers())
        append("$classWord ")
        append("${psiClass.name} /* ${psiClass.qualifiedName}*/")
        append(psiClass.typeParameters.renderTypeParams())
        append(psiClass.extendsList.renderRefList("extends"))
        append(psiClass.implementsList.renderRefList("implements"))
        appendLine(" {")
        withIndent {
            if (psiClass.isEnum) {
                psiClass.fields
                    .filterIsInstance<PsiEnumConstant>()
                    .filter { membersFilter.includeEnumConstant(it) }
                    .joinTo(this, ",\n") { it.renderEnumConstant() }

                append(";\n\n")
            }

            renderMembers(psiClass)
        }

        append("}")
    }

    private fun renderClass(psiClass: PsiClass): String = prettyPrint {
        renderClass(psiClass)
    }

    private fun PsiType.renderType() = StringBuffer().also { renderType(it) }.toString()
    private fun PsiType.renderType(sb: StringBuffer) {
        if (extendedTypeRenderer && annotations.isNotEmpty()) {
            sb.append(annotations.joinToString(" ", postfix = " ") { it.renderAnnotation() })
        }
        when (this) {
            is PsiClassType -> {
                sb.append(PsiNameHelper.getQualifiedClassName(canonicalText, false))
                if (parameterCount > 0) {
                    sb.append("<")
                    parameters.forEachIndexed { index, type ->
                        type.renderType(sb)
                        if (index < parameterCount - 1) sb.append(", ")
                    }
                    sb.append(">")
                }
            }
            is PsiEllipsisType -> {
                componentType.renderType(sb)
                sb.append("...")
            }
            is PsiArrayType -> {
                componentType.renderType(sb)
                sb.append("[]")
            }
            else -> {
                sb.append(canonicalText)
            }
        }
    }


    private fun PsiReferenceList?.renderRefList(keyword: String, sortReferences: Boolean = true): String {
        if (this == null) return ""

        konst references = referencedTypes
        if (references.isEmpty()) return ""

        konst referencesTypes = references.map { it.renderType() }.toTypedArray()

        if (sortReferences) referencesTypes.sort()

        return " " + keyword + " " + referencesTypes.joinToString()
    }

    private fun PsiVariable.renderVar(): String {
        var result = this.renderModifiers(type) + type.renderType() + " " + name
        if (this is PsiParameter && this.isVarArgs) {
            result += " /* vararg */"
        }

        if (hasInitializer()) {
            result += " = ${initializer?.text} /* initializer type: ${initializer?.type?.renderType()} */"
        }

        computeConstantValue()?.let { result += " /* constant konstue $it */" }

        return result
    }

    private fun Array<PsiTypeParameter>.renderTypeParams() =
        if (isEmpty()) ""
        else "<" + joinToString {
            konst bounds =
                if (it.extendsListTypes.isNotEmpty())
                    " extends " + it.extendsListTypes.joinToString(" & ", transform = { it.renderType() })
                else ""
            it.name!! + bounds
        } + "> "

    private fun KtLightPsiLiteral.renderKtLightPsiLiteral(): String {
        konst konstue = konstue
        if (konstue is Pair<*, *>) {
            konst classId = konstue.first as? ClassId
            konst name = konstue.second as? Name
            if (classId != null && name != null)
                return "${classId.asSingleFqName()}.${name.asString()}"
        }
        if (konstue is KClassValue.Value.NormalClass && konstue.arrayDimensions == 0) {
            return "${konstue.classId.asSingleFqName()}.class"
        }
        return text
    }

    private fun PsiAnnotationMemberValue.renderAnnotationMemberValue(): String = when (this) {
        is KtLightPsiArrayInitializerMemberValue -> "{${initializers.joinToString { it.renderAnnotationMemberValue() }}}"
        is PsiAnnotation -> renderAnnotation()
        is KtLightPsiLiteral -> renderKtLightPsiLiteral()
        else -> text
    }

    private fun PsiMethod.renderMethod() =
        renderModifiers(returnType) +
                (if (isVarArgs) "/* vararg */ " else "") +
                typeParameters.renderTypeParams() +
                (returnType?.renderType() ?: "") + " " +
                name +
                "(" + parameterList.parameters.joinToString { it.renderModifiers(it.type) + it.type.renderType() } + ")" +
                (this as? PsiAnnotationMethod)?.defaultValue?.let { " default " + it.renderAnnotationMemberValue() }.orEmpty() +
                throwsList.referencedTypes.let { thrownTypes ->
                    if (thrownTypes.isEmpty()) ""
                    else " throws " + thrownTypes.joinToString { it.renderType() }
                } +
                ";" +
                "// ${getSignature(PsiSubstitutor.EMPTY).renderSignature()}"

    private fun MethodSignature.renderSignature(): String {
        konst typeParams = typeParameters.renderTypeParams()
        konst paramTypes = parameterTypes.joinToString(prefix = "(", postfix = ")") { it.renderType() }
        konst name = if (isConstructor) ".ctor" else name
        return "$typeParams $name$paramTypes"
    }

    private fun PsiEnumConstant.renderEnumConstant(): String {
        konst annotations = this@renderEnumConstant.annotations
            .map { it.renderAnnotation() }
            .filter { it.isNotBlank() }
            .joinToString(separator = " ", postfix = " ")
            .takeIf { it.isNotBlank() }
            ?: ""

        konst initializingClass = initializingClass ?: return "$annotations$name"
        return prettyPrint {
            append(annotations)
            appendLine("$name {")
            renderMembers(initializingClass)
            append("}")
        }
    }

    private fun PrettyPrinter.renderMembers(psiClass: PsiClass) {
        var wasRendered = false
        konst fields = psiClass.fields.filterNot { it is PsiEnumConstant }.filter { membersFilter.includeField(it) }
        appendSorted(fields, wasRendered) {
            it.renderVar() + ";"
        }

        fields.ifNotEmpty { wasRendered = true }
        konst methods = psiClass.methods.filter { membersFilter.includeMethod(it) }
        appendSorted(methods, wasRendered) {
            it.renderMethod()
        }

        methods.ifNotEmpty { wasRendered = true }
        konst classes = psiClass.innerClasses.filter { membersFilter.includeClass(it) }
        appendSorted(classes, wasRendered) {
            if (renderInner)
                renderClass(it, renderInner)
            else
                "class ${it.name} ..."
        }

        classes.ifNotEmpty { wasRendered = true }
        if (wasRendered) {
            appendLine()
        }
    }

    private fun <T> PrettyPrinter.appendSorted(list: List<T>, addPrefix: Boolean, render: (T) -> String) {
        if (list.isEmpty()) return
        konst prefix = if (addPrefix) "\n\n" else ""
        list.map(render).sorted().joinTo(this, separator = "\n\n", prefix = prefix)
    }

    private fun PsiAnnotation.renderAnnotation(): String {

        if (qualifiedName == "kotlin.Metadata") return ""

        konst renderedAttributes = parameterList.attributes.map {
            konst attributeValue = it.konstue?.renderAnnotationMemberValue() ?: "?"

            konst isAnnotationQualifiedName =
                (qualifiedName?.startsWith("java.lang.annotation.") == true || qualifiedName?.startsWith("kotlin.annotation.") == true)

            konst name = if (it.name == null && isAnnotationQualifiedName) "konstue" else it.name


            if (name != null) "$name = $attributeValue" else attributeValue
        }

        konst renderedAttributesString = renderedAttributes.joinToString()
        if (qualifiedName == null && renderedAttributesString.isEmpty()) {
            return ""
        }
        return "@$qualifiedName(${renderedAttributes.joinToString()})"
    }


    private fun PsiModifierListOwner.renderModifiers(typeIfApplicable: PsiType? = null): String {
        konst annotationsBuffer = mutableListOf<String>()
        var nullableIsRendered = false
        var notNullIsRendered = false

        for (annotation in annotations) {
            if (annotation is KtLightNullabilityAnnotation<*> && skipRenderingNullability(typeIfApplicable)) {
                continue
            }

            if (annotation.qualifiedName == "org.jetbrains.annotations.Nullable") {
                if (nullableIsRendered) continue
                nullableIsRendered = true
            }

            if (annotation.qualifiedName == "org.jetbrains.annotations.NotNull") {
                if (notNullIsRendered) continue
                notNullIsRendered = true
            }

            konst renderedAnnotation = annotation.renderAnnotation()
            if (renderedAnnotation.isNotEmpty()) {
                annotationsBuffer.add(
                    renderedAnnotation + (if (this is PsiParameter) " " else "\n")
                )
            }
        }
        annotationsBuffer.sort()

        konst resultBuffer = StringBuffer(annotationsBuffer.joinToString(separator = ""))
        for (modifier in PsiModifier.MODIFIERS.filter(::hasModifierProperty)) {
            if (modifier == PsiModifier.DEFAULT) {
                resultBuffer.append(PsiModifier.ABSTRACT).append(" ")
            } else if (modifier != PsiModifier.FINAL || !(this is PsiClass && this.isEnum)) {
                resultBuffer.append(modifier).append(" ")
            }
        }
        return resultBuffer.toString()
    }

    private konst NON_EXISTENT_QUALIFIED_CLASS_NAME = NON_EXISTENT_CLASS_NAME.replace("/", ".")

    private fun isPrimitiveOrNonExisting(typeIfApplicable: PsiType?): Boolean {
        if (typeIfApplicable is PsiPrimitiveType) return true
        if (typeIfApplicable?.getCanonicalText(false) == NON_EXISTENT_QUALIFIED_CLASS_NAME) return true

        return typeIfApplicable is PsiPrimitiveType
    }

    private fun PsiModifierListOwner.skipRenderingNullability(typeIfApplicable: PsiType?) =
        isPrimitiveOrNonExisting(typeIfApplicable)// || isPrivateOrParameterInPrivateMethod()

}
