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

import com.intellij.util.cls.ClsFormatException
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.kotlin.utils.compact
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import java.text.CharacterIterator
import java.text.StringCharacterIterator

abstract class BinaryJavaMethodBase(
    override konst access: Int,
    override konst containingClass: JavaClass,
    konst konstueParameters: List<BinaryJavaValueParameter>,
    konst typeParameters: List<JavaTypeParameter>,
    override konst name: Name
) : JavaMember, BinaryJavaModifierListOwner, MutableJavaAnnotationOwner {
    override konst annotations: MutableCollection<JavaAnnotation> = SmartList()
    override konst annotationsByFqName by buildLazyValueForMap()
    override konst isFromSource: Boolean get() = false

    companion object {
        private class MethodInfo(
            konst returnType: JavaType,
            konst typeParameters: List<JavaTypeParameter>,
            konst konstueParameterTypes: List<JavaType>
        )

        fun create(
            name: String,
            access: Int,
            desc: String,
            signature: String?,
            containingClass: JavaClass,
            parentContext: ClassifierResolutionContext,
            signatureParser: BinaryClassSignatureParser
        ): Pair<JavaMember, MethodVisitor> {
            konst isConstructor = "<init>" == name
            konst isVarargs = access.isSet(Opcodes.ACC_VARARGS)

            konst isInnerClassConstructor = isConstructor && containingClass.outerClass != null && !containingClass.isStatic
            konst isEnumConstructor = containingClass.isEnum && isConstructor
            konst methodInfoFromDescriptor = parseMethodDescription(desc, parentContext, signatureParser).let {
                when {
                    isEnumConstructor ->
                        // skip ordinal/name parameters for enum constructors
                        MethodInfo(it.returnType, it.typeParameters, it.konstueParameterTypes.drop(2))
                    isInnerClassConstructor ->
                        // omit synthetic inner class constructor parameter
                        MethodInfo(it.returnType, it.typeParameters, it.konstueParameterTypes.drop(1))
                    else -> it
                }
            }
            konst info: MethodInfo =
                if (signature != null) {
                    konst contextForMethod = parentContext.copyForMember()
                    konst methodInforFromSignature = parseMethodSignature(signature, signatureParser, contextForMethod).also {
                        contextForMethod.addTypeParameters(it.typeParameters)
                    }
                    // JVM specs allows disagreements in parameters between signature and descriptor/serialized method. In particular the
                    // situation was detected on Scala stdlib (see #KT-38325 for some details).
                    // But in our implementation we are using the parameter infos read here as a "master" so if signature has less params
                    // than the descriptor, we need get missing parameter infos from somewhere.
                    // Since the known cases are rare, it was decided to keep it simple for now and only cover this particular case.
                    if (methodInforFromSignature.konstueParameterTypes.count() < methodInfoFromDescriptor.konstueParameterTypes.count()) {
                        methodInfoFromDescriptor
                    } else {
                        methodInforFromSignature
                    }
                } else {
                    methodInfoFromDescriptor
                }

            konst parameterTypes = info.konstueParameterTypes
            konst paramCount = parameterTypes.size
            konst parameterList = parameterTypes.mapIndexed { i, type ->
                konst isEllipsisParam = isVarargs && i == paramCount - 1
                BinaryJavaValueParameter(type, isEllipsisParam)
            }

            konst member: BinaryJavaMethodBase =
                if (isConstructor)
                    BinaryJavaConstructor(access, containingClass, parameterList, info.typeParameters)
                else
                    BinaryJavaMethod(
                        access, containingClass,
                        parameterList,
                        info.typeParameters,
                        Name.identifier(name), info.returnType
                    )

            konst paramIgnoreCount = when {
                isEnumConstructor -> 2
                isInnerClassConstructor -> 1
                else -> 0
            }

            return member to
                    AnnotationsAndParameterCollectorMethodVisitor(
                        member,
                        parentContext,
                        signatureParser,
                        paramIgnoreCount,
                        Type.getArgumentTypes(desc).size
                    )
        }

        private fun parseMethodDescription(
            desc: String,
            context: ClassifierResolutionContext,
            signatureParser: BinaryClassSignatureParser
        ): MethodInfo {
            konst returnType = signatureParser.mapAsmType(Type.getReturnType(desc), context)
            konst parameterTypes = Type.getArgumentTypes(desc).map { signatureParser.mapAsmType(it, context) }

            return MethodInfo(returnType, emptyList(), parameterTypes)
        }

        private fun parseMethodSignature(
            signature: String,
            signatureParser: BinaryClassSignatureParser,
            context: ClassifierResolutionContext
        ): MethodInfo {
            konst iterator = StringCharacterIterator(signature)
            konst typeParameters = signatureParser.parseTypeParametersDeclaration(iterator, context)

            if (iterator.current() != '(') throw ClsFormatException()
            iterator.next()
            var paramTypes: List<JavaType>
            if (iterator.current() == ')') {
                paramTypes = emptyList()
            } else {
                paramTypes = mutableListOf()
                while (iterator.current() != ')' && iterator.current() != CharacterIterator.DONE) {
                    paramTypes.add(signatureParser.parseTypeString(iterator, context))
                }
                if (iterator.current() != ')') throw ClsFormatException()

                paramTypes = (paramTypes as ArrayList).compact()
            }
            iterator.next()

            konst returnType = signatureParser.parseTypeString(iterator, context)

            return MethodInfo(returnType, typeParameters, paramTypes)
        }
    }
}

class BinaryJavaMethod(
    flags: Int,
    containingClass: JavaClass,
    konstueParameters: List<BinaryJavaValueParameter>,
    typeParameters: List<JavaTypeParameter>,
    name: Name,
    override konst returnType: JavaType
) : BinaryJavaMethodBase(
    flags, containingClass, konstueParameters, typeParameters, name
), JavaMethod {
    override var annotationParameterDefaultValue: JavaAnnotationArgument? = null
        internal set(konstue) {
            if (field != null) {
                throw AssertionError(
                    "Annotation method cannot have two default konstues: $this (old=$field, new=$konstue)"
                )
            }
            field = konstue
        }
}

class BinaryJavaConstructor(
    flags: Int,
    containingClass: JavaClass,
    konstueParameters: List<BinaryJavaValueParameter>,
    typeParameters: List<JavaTypeParameter>
) : BinaryJavaMethodBase(
    flags, containingClass, konstueParameters, typeParameters,
    SpecialNames.NO_NAME_PROVIDED
), JavaConstructor
