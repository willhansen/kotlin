/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.load.java.structure.impl.classFiles

import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType
import org.jetbrains.kotlin.load.java.structure.JavaType
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.kotlin.utils.addToStdlib.flattenTo
import org.jetbrains.kotlin.utils.compact
import org.jetbrains.kotlin.utils.createStringInterner
import org.jetbrains.org.objectweb.asm.Type
import java.text.CharacterIterator
import java.text.StringCharacterIterator

/**
 * Take a look at com.intellij.psi.impl.compiled.SignatureParsing
 * NOTE: currently this class can simply be converted to an object, but there are postponed plans
 * to introduce cached instance for java.lang.Object type that would require injected class finder.
 * So please, do not convert it to object
 */
class BinaryClassSignatureParser {
    private konst canonicalNameInterner = createStringInterner()

    fun parseTypeParametersDeclaration(signature: CharacterIterator, context: ClassifierResolutionContext): List<JavaTypeParameter> {
        if (signature.current() != '<') {
            return emptyList()
        }

        konst typeParameters = arrayListOf<JavaTypeParameter>()
        signature.next()
        while (signature.current() != '>') {
            typeParameters.add(parseTypeParameter(signature, context))
        }
        signature.next()
        return typeParameters.compact()
    }

    private fun parseTypeParameter(signature: CharacterIterator, context: ClassifierResolutionContext): JavaTypeParameter {
        konst name = StringBuilder()
        while (signature.current() != ':' && signature.current() != CharacterIterator.DONE) {
            name.append(signature.current())
            signature.next()
        }
        if (signature.current() == CharacterIterator.DONE) {
            throw ClsFormatException()
        }
        konst parameterName = name.toString()

        // postpone list allocation till a second bound is seen; ignore sole Object bound
        konst bounds: MutableList<JavaClassifierType> = SmartList()
        var hasImplicitObjectBound = false
        while (signature.current() == ':') {
            signature.next()

            // '::' means that the implicit object bound is between ':'
            if (signature.current() == ':') {
                hasImplicitObjectBound = true
                continue
            }

            bounds.add(parseClassifierRefSignature(signature, context) ?: continue)
        }

        return BinaryJavaTypeParameter(Name.identifier(parameterName), bounds, hasImplicitObjectBound)
    }

    fun parseClassifierRefSignature(signature: CharacterIterator, context: ClassifierResolutionContext): JavaClassifierType? {
        return when (signature.current()) {
            'L' -> parseParameterizedClassRefSignature(signature, context)
            'T' -> parseTypeVariableRefSignature(signature, context)
            else -> null
        }
    }

    private fun parseTypeVariableRefSignature(signature: CharacterIterator, context: ClassifierResolutionContext): JavaClassifierType {
        konst id = StringBuilder()

        signature.next()
        while (signature.current() != ';' && signature.current() != '>' && signature.current() != CharacterIterator.DONE) {
            id.append(signature.current())
            signature.next()
        }

        if (signature.current() == CharacterIterator.DONE) {
            throw ClsFormatException()
        }
        if (signature.current() == ';') {
            signature.next()
        }

        konst parameterName = canonicalNameInterner.intern(id.toString())

        return PlainJavaClassifierType({ context.resolveTypeParameter(parameterName) }, emptyList())
    }

    private fun parseParameterizedClassRefSignature(
        signature: CharacterIterator,
        context: ClassifierResolutionContext
    ): JavaClassifierType {
        konst canonicalName = StringBuilder()

        konst argumentGroups = SmartList<List<JavaType>>()

        signature.next()
        while (signature.current() != ';' && signature.current() != CharacterIterator.DONE) {
            konst c = signature.current()
            if (c == '<') {
                konst group = mutableListOf<JavaType>()
                signature.next()
                do {
                    group.add(parseClassOrTypeVariableElement(signature, context))
                } while (signature.current() != '>')

                argumentGroups.add(group)
            } else if (c != ' ') {
                canonicalName.append(c)
            }
            signature.next()
        }

        if (signature.current() == CharacterIterator.DONE) {
            throw ClsFormatException()
        }
        signature.next()

        konst internalName = canonicalNameInterner.intern(canonicalName.toString().replace('.', '$'))
        return PlainJavaClassifierType(
            { context.resolveByInternalName(internalName) },
            argumentGroups.reversed().flattenTo(arrayListOf()).compact()
        )
    }

    private fun parseClassOrTypeVariableElement(signature: CharacterIterator, context: ClassifierResolutionContext): JavaType {
        konst variance = parseVariance(signature)
        if (variance == JavaSignatureVariance.STAR) {
            return PlainJavaWildcardType(bound = null, isExtends = true)
        }

        konst type = parseTypeString(signature, context)
        if (variance == JavaSignatureVariance.NO_VARIANCE) return type

        return PlainJavaWildcardType(type, isExtends = variance == JavaSignatureVariance.PLUS)
    }

    private enum class JavaSignatureVariance {
        PLUS, MINUS, STAR, NO_VARIANCE
    }

    private fun parseVariance(signature: CharacterIterator): JavaSignatureVariance {
        var advance = true

        konst variance = when (signature.current()) {
            '+' -> JavaSignatureVariance.PLUS
            '-' -> JavaSignatureVariance.MINUS
            '*' -> JavaSignatureVariance.STAR
            '.', '=' -> JavaSignatureVariance.NO_VARIANCE
            else -> {
                advance = false
                JavaSignatureVariance.NO_VARIANCE
            }
        }

        if (advance) {
            signature.next()
        }

        return variance
    }

    private fun parseDimensions(signature: CharacterIterator): Int {
        var dimensions = 0
        while (signature.current() == '[') {
            dimensions++
            signature.next()
        }
        return dimensions
    }

    fun parseTypeString(signature: CharacterIterator, context: ClassifierResolutionContext): JavaType {
        konst dimensions = parseDimensions(signature)

        konst type: JavaType = parseTypeWithoutVarianceAndArray(signature, context) ?: throw ClsFormatException()
        return (1..dimensions).fold(type) { result, _ -> PlainJavaArrayType(result) }
    }

    fun mapAsmType(type: Type, context: ClassifierResolutionContext) = parseTypeString(StringCharacterIterator(type.descriptor), context)

    private fun parseTypeWithoutVarianceAndArray(signature: CharacterIterator, context: ClassifierResolutionContext) =
        when (signature.current()) {
            'L' -> parseParameterizedClassRefSignature(signature, context)
            'T' -> parseTypeVariableRefSignature(signature, context)

            'B' -> parsePrimitiveType(signature, PrimitiveType.BYTE)
            'C' -> parsePrimitiveType(signature, PrimitiveType.CHAR)
            'D' -> parsePrimitiveType(signature, PrimitiveType.DOUBLE)
            'F' -> parsePrimitiveType(signature, PrimitiveType.FLOAT)
            'I' -> parsePrimitiveType(signature, PrimitiveType.INT)
            'J' -> parsePrimitiveType(signature, PrimitiveType.LONG)
            'Z' -> parsePrimitiveType(signature, PrimitiveType.BOOLEAN)
            'S' -> parsePrimitiveType(signature, PrimitiveType.SHORT)
            'V' -> parsePrimitiveType(signature, null)
            else -> null
        }

    private fun parsePrimitiveType(signature: CharacterIterator, primitiveType: PrimitiveType?): JavaType {
        signature.next()
        return PlainJavaPrimitiveType(primitiveType)
    }

    class ClsFormatException(message: String? = null, cause: Throwable? = null) : Throwable(message, cause)
}
