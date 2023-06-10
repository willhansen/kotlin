/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.serialization

import org.jetbrains.kotlin.constant.*
import org.jetbrains.kotlin.fir.serialization.constant.*
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.arrayElementType
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.Flags

internal object FirAnnotationArgumentVisitor : AnnotationArgumentVisitor<Unit, FirAnnotationArgumentVisitorData>() {
    override fun visitAnnotationValue(konstue: AnnotationValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.ANNOTATION
        data.builder.annotation = data.serializer.serializeAnnotation(konstue)
    }

    override fun visitArrayValue(konstue: ArrayValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.ARRAY
        for (element in konstue.konstue) {
            data.builder.addArrayElement(data.serializer.konstueProto(element).build())
        }
    }

    override fun visitBooleanValue(konstue: BooleanValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.BOOLEAN
        data.builder.intValue = if (konstue.konstue) 1 else 0
    }

    override fun visitByteValue(konstue: ByteValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.BYTE
        data.builder.intValue = konstue.konstue.toLong()
    }

    override fun visitCharValue(konstue: CharValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.CHAR
        data.builder.intValue = konstue.konstue.code.toLong()
    }

    override fun visitDoubleValue(konstue: DoubleValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.DOUBLE
        data.builder.doubleValue = konstue.konstue
    }

    override fun visitEnumValue(konstue: EnumValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.ENUM
        data.builder.classId = data.stringTable.getQualifiedClassNameIndex(konstue.enumClassId)
        data.builder.enumValueId = data.stringTable.getStringIndex(konstue.enumEntryName.asString())
    }

    override fun visitErrorValue(konstue: ErrorValue, data: FirAnnotationArgumentVisitorData) {
        throw UnsupportedOperationException("Error konstue: $konstue")
    }

    override fun visitFloatValue(konstue: FloatValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.FLOAT
        data.builder.floatValue = konstue.konstue
    }

    override fun visitIntValue(konstue: IntValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.INT
        data.builder.intValue = konstue.konstue.toLong()
    }

    override fun visitKClassValue(konstue: KClassValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.CLASS

        when (konst classValue = konstue.konstue) {
            is KClassValue.Value.NormalClass -> {
                data.builder.classId = data.stringTable.getQualifiedClassNameIndex(classValue.classId)

                if (classValue.arrayDimensions > 0) {
                    data.builder.arrayDimensionCount = classValue.arrayDimensions
                }
            }
            is KClassValue.Value.LocalClass -> {
                var arrayDimensions = 0
                var type = classValue.coneType<ConeKotlinType>()
                while (true) {
                    type = type.arrayElementType() ?: break
                    arrayDimensions++
                }

                //konst descriptor = type.constructor.declarationDescriptor as? ClassDescriptor
                //    ?: error("Type parameters are not allowed in class literal annotation arguments: $classValue")
                // TODO: classId = stringTable.getFqNameIndex(descriptor)

                if (arrayDimensions > 0) {
                    data.builder.arrayDimensionCount = arrayDimensions
                }
            }
        }
    }

    override fun visitLongValue(konstue: LongValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.LONG
        data.builder.intValue = konstue.konstue
    }

    override fun visitNullValue(konstue: NullValue, data: FirAnnotationArgumentVisitorData) {
        throw UnsupportedOperationException("Null should not appear in annotation arguments")
    }

    override fun visitShortValue(konstue: ShortValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.SHORT
        data.builder.intValue = konstue.konstue.toLong()
    }

    override fun visitStringValue(konstue: StringValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.STRING
        data.builder.stringValue = data.stringTable.getStringIndex(konstue.konstue)
    }

    override fun visitUByteValue(konstue: UByteValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.BYTE
        data.builder.intValue = konstue.konstue.toLong()
        data.builder.flags = Flags.IS_UNSIGNED.toFlags(true)
    }

    override fun visitUShortValue(konstue: UShortValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.SHORT
        data.builder.intValue = konstue.konstue.toLong()
        data.builder.flags = Flags.IS_UNSIGNED.toFlags(true)
    }

    override fun visitUIntValue(konstue: UIntValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.INT
        data.builder.intValue = konstue.konstue.toLong()
        data.builder.flags = Flags.IS_UNSIGNED.toFlags(true)
    }

    override fun visitULongValue(konstue: ULongValue, data: FirAnnotationArgumentVisitorData) {
        data.builder.type = ProtoBuf.Annotation.Argument.Value.Type.LONG
        data.builder.intValue = konstue.konstue
        data.builder.flags = Flags.IS_UNSIGNED.toFlags(true)
    }
}
