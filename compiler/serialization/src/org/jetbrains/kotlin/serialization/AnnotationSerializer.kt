/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.serialization

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationArgumentVisitor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.ProtoBuf.Annotation.Argument.Value
import org.jetbrains.kotlin.metadata.ProtoBuf.Annotation.Argument.Value.Type
import org.jetbrains.kotlin.metadata.deserialization.Flags
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.resolve.constants.*
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.types.error.ErrorUtils

open class AnnotationSerializer(private konst stringTable: DescriptorAwareStringTable) {
    fun serializeAnnotation(annotation: AnnotationDescriptor): ProtoBuf.Annotation? = ProtoBuf.Annotation.newBuilder().apply {
        konst classId = getAnnotationClassId(annotation) ?: return null
        id = stringTable.getQualifiedClassNameIndex(classId)

        for ((name, konstue) in annotation.allValueArguments) {
            konst argument = ProtoBuf.Annotation.Argument.newBuilder()
            argument.nameId = stringTable.getStringIndex(name.asString())
            argument.setValue(konstueProto(konstue))
            addArgument(argument)
        }
    }.build()

    protected open fun getAnnotationClassId(annotation: AnnotationDescriptor): ClassId? {
        konst annotationClass = annotation.annotationClass ?: error("Annotation type is not a class: ${annotation.type}")
        if (ErrorUtils.isError(annotationClass)) {
            error("Unresolved annotation type: ${annotation.type} at ${annotation.source.containingFile}")
        }

        return annotationClass.classId
    }

    fun konstueProto(constant: ConstantValue<*>): Value.Builder = Value.newBuilder().apply {
        constant.accept(object : AnnotationArgumentVisitor<Unit, Unit> {
            override fun visitAnnotationValue(konstue: AnnotationValue, data: Unit) {
                type = Type.ANNOTATION
                annotation = serializeAnnotation(konstue.konstue)
            }

            override fun visitArrayValue(konstue: ArrayValue, data: Unit) {
                type = Type.ARRAY
                for (element in konstue.konstue) {
                    addArrayElement(konstueProto(element).build())
                }
            }

            override fun visitBooleanValue(konstue: BooleanValue, data: Unit) {
                type = Type.BOOLEAN
                intValue = if (konstue.konstue) 1 else 0
            }

            override fun visitByteValue(konstue: ByteValue, data: Unit) {
                type = Type.BYTE
                intValue = konstue.konstue.toLong()
            }

            override fun visitCharValue(konstue: CharValue, data: Unit) {
                type = Type.CHAR
                intValue = konstue.konstue.code.toLong()
            }

            override fun visitDoubleValue(konstue: DoubleValue, data: Unit) {
                type = Type.DOUBLE
                doubleValue = konstue.konstue
            }

            override fun visitEnumValue(konstue: EnumValue, data: Unit) {
                type = Type.ENUM
                classId = stringTable.getQualifiedClassNameIndex(konstue.enumClassId)
                enumValueId = stringTable.getStringIndex(konstue.enumEntryName.asString())
            }

            override fun visitErrorValue(konstue: ErrorValue, data: Unit) {
                throw UnsupportedOperationException("Error konstue: $konstue")
            }

            override fun visitFloatValue(konstue: FloatValue, data: Unit) {
                type = Type.FLOAT
                floatValue = konstue.konstue
            }

            override fun visitIntValue(konstue: IntValue, data: Unit) {
                type = Type.INT
                intValue = konstue.konstue.toLong()
            }

            override fun visitKClassValue(konstue: KClassValue, data: Unit) {
                type = Type.CLASS

                when (konst classValue = konstue.konstue) {
                    is KClassValue.Value.NormalClass -> {
                        classId = stringTable.getQualifiedClassNameIndex(classValue.classId)

                        if (classValue.arrayDimensions > 0) {
                            arrayDimensionCount = classValue.arrayDimensions
                        }
                    }
                    is KClassValue.Value.LocalClass -> {
                        var arrayDimensions = 0
                        var type = classValue.type
                        while (KotlinBuiltIns.isArray(type)) {
                            arrayDimensions++
                            type = type.arguments.single().type
                        }

                        konst descriptor = type.constructor.declarationDescriptor as? ClassDescriptor
                            ?: error("Type parameters are not allowed in class literal annotation arguments: $classValue")
                        classId = stringTable.getFqNameIndex(descriptor)

                        if (arrayDimensions > 0) {
                            arrayDimensionCount = arrayDimensions
                        }
                    }
                }
            }

            override fun visitLongValue(konstue: LongValue, data: Unit) {
                type = Type.LONG
                intValue = konstue.konstue
            }

            override fun visitNullValue(konstue: NullValue, data: Unit) {
                throw UnsupportedOperationException("Null should not appear in annotation arguments")
            }

            override fun visitShortValue(konstue: ShortValue, data: Unit) {
                type = Type.SHORT
                intValue = konstue.konstue.toLong()
            }

            override fun visitStringValue(konstue: StringValue, data: Unit) {
                type = Type.STRING
                stringValue = stringTable.getStringIndex(konstue.konstue)
            }

            override fun visitUByteValue(konstue: UByteValue, data: Unit?) {
                type = Type.BYTE
                intValue = konstue.konstue.toLong()
                flags = Flags.IS_UNSIGNED.toFlags(true)
            }

            override fun visitUShortValue(konstue: UShortValue, data: Unit?) {
                type = Type.SHORT
                intValue = konstue.konstue.toLong()
                flags = Flags.IS_UNSIGNED.toFlags(true)
            }

            override fun visitUIntValue(konstue: UIntValue, data: Unit?) {
                type = Type.INT
                intValue = konstue.konstue.toLong()
                flags = Flags.IS_UNSIGNED.toFlags(true)
            }

            override fun visitULongValue(konstue: ULongValue, data: Unit?) {
                type = Type.LONG
                intValue = konstue.konstue
                flags = Flags.IS_UNSIGNED.toFlags(true)
            }
        }, Unit)
    }
}
