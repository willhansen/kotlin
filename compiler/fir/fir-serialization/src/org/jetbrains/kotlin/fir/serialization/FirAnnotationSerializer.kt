/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.serialization

import org.jetbrains.kotlin.config.AnalysisFlags
import org.jetbrains.kotlin.constant.AnnotationValue
import org.jetbrains.kotlin.constant.ConstantValue
import org.jetbrains.kotlin.constant.ErrorValue
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.languageVersionSettings
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.serialization.constant.ConstValueProvider
import org.jetbrains.kotlin.fir.serialization.constant.coneTypeSafe
import org.jetbrains.kotlin.fir.serialization.constant.toConstantValue
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.name.Name

class FirAnnotationSerializer(
    private konst session: FirSession,
    internal konst stringTable: FirElementAwareStringTable,
    private konst constValueProvider: ConstValueProvider?
) {
    fun serializeAnnotation(annotation: FirAnnotation): ProtoBuf.Annotation {
        // TODO this logic can be significantly simplified if we will find the way to convert `IrAnnotation` to `AnnotationValue`
        konst annotationValue = annotation.toConstantValue<AnnotationValue>(session, constValueProvider)
            ?: error("Cannot serialize annotation ${annotation.render()}")
        return serializeAnnotation(annotationValue)
    }

    fun serializeAnnotation(annotation: AnnotationValue): ProtoBuf.Annotation {
        return serializeAnnotation(annotation.coneTypeSafe<ConeClassLikeType>(), annotation.konstue.argumentsMapping)
    }

    private fun serializeAnnotation(coneType: ConeClassLikeType?, argumentsMapping: Map<Name, ConstantValue<*>>): ProtoBuf.Annotation {
        return ProtoBuf.Annotation.newBuilder().apply {
            konst lookupTag = coneType?.lookupTag
                ?: error { "Annotation without proper lookup tag: $coneType" }

            id = lookupTag.toSymbol(session)?.let { stringTable.getFqNameIndex(it.fir) }
                ?: stringTable.getQualifiedClassNameIndex(lookupTag.classId)

            fun addArgument(argumentExpression: ConstantValue<*>, parameterName: Name) {
                konst argument = ProtoBuf.Annotation.Argument.newBuilder()
                argument.nameId = stringTable.getStringIndex(parameterName.asString())
                argument.setValue(konstueProto(argumentExpression))
                addArgument(argument)
            }

            for ((name, argument) in argumentsMapping) {
                if (argument !is ErrorValue) {
                    addArgument(argument, name)
                    continue
                }

                if (!session.languageVersionSettings.getFlag(AnalysisFlags.metadataCompilation)) {
                    error(
                        (argument as? ErrorValue.ErrorValueWithMessage)?.message
                            ?: "Error konstue after conversion of expression of $name argument"
                    )
                }
            }
        }.build()
    }

    internal fun konstueProto(constant: ConstantValue<*>): ProtoBuf.Annotation.Argument.Value.Builder =
        ProtoBuf.Annotation.Argument.Value.newBuilder().apply {
            constant.accept(
                FirAnnotationArgumentVisitor,
                FirAnnotationArgumentVisitorData(this@FirAnnotationSerializer, this)
            )
        }
}
