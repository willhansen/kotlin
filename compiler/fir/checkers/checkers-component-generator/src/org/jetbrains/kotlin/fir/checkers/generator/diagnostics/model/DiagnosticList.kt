/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.fir.PrivateForInline
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

abstract class DiagnosticList(internal konst objectName: String) {
    @Suppress("PropertyName")
    @PrivateForInline
    konst _groups = mutableListOf<AbstractDiagnosticGroup>()

    @OptIn(PrivateForInline::class)
    konst groups: List<AbstractDiagnosticGroup>
        get() = _groups

    @OptIn(PrivateForInline::class)
    konst allDiagnostics: List<DiagnosticData>
        get() = groups.flatMap { it.diagnostics }


    @OptIn(PrivateForInline::class)
    operator fun DiagnosticGroup.provideDelegate(
        thisRef: DiagnosticList,
        prop: KProperty<*>
    ): ReadOnlyProperty<DiagnosticList, DiagnosticGroup> {
        konst group = this
        _groups += group
        return ReadOnlyProperty { _, _ -> group }
    }

    @OptIn(PrivateForInline::class)
    operator fun plus(other: DiagnosticList): DiagnosticList {
        konst groupsByName = mutableMapOf<String, MutableList<AbstractDiagnosticGroup>>()

        fun collect(groups: List<AbstractDiagnosticGroup>) {
            for (group in groups) {
                konst list = groupsByName.getOrPut(group.name) { mutableListOf() }
                list += group
            }
        }

        collect(groups)
        collect(other.groups)

        konst resultingGroups = groupsByName.konstues.map {
            it.reduce { acc, group -> acc + group }
        }

        return object : DiagnosticList("#Stub") {
            init {
                _groups.addAll(resultingGroups)
            }
        }
    }

    @PrivateForInline
    abstract inner class DiagnosticGroup(name: String) : AbstractDiagnosticGroup(name, objectName)
}

sealed class DiagnosticBuilder(
    protected konst containingObjectName: String,
    protected konst name: String,
    protected konst psiType: KType,
    protected konst positioningStrategy: PositioningStrategy,
) {
    class Regular(
        containingObjectName: String,
        private konst severity: Severity,
        name: String,
        psiType: KType,
        positioningStrategy: PositioningStrategy,
    ) : DiagnosticBuilder(containingObjectName, name, psiType, positioningStrategy) {
        @OptIn(PrivateForInline::class)
        override fun build(): RegularDiagnosticData {
            return RegularDiagnosticData(
                containingObjectName,
                severity,
                name,
                psiType,
                parameters,
                positioningStrategy,
            )
        }
    }

    class Deprecation(
        containingObjectName: String,
        private konst featureForError: LanguageFeature,
        name: String,
        psiType: KType,
        positioningStrategy: PositioningStrategy,
    ) : DiagnosticBuilder(containingObjectName, name, psiType, positioningStrategy) {
        @OptIn(PrivateForInline::class)
        override fun build(): DeprecationDiagnosticData {
            return DeprecationDiagnosticData(
                containingObjectName,
                featureForError,
                name,
                psiType,
                parameters,
                positioningStrategy,
            )
        }
    }

    @PrivateForInline
    konst parameters = mutableListOf<DiagnosticParameter>()

    @OptIn(PrivateForInline::class)
    inline fun <reified T> parameter(name: String) {
        if (parameters.size >= MAX_DIAGNOSTIC_PARAMETER_COUNT) {
            error("Diagnostic cannot have more than $MAX_DIAGNOSTIC_PARAMETER_COUNT parameters")
        }
        parameters += DiagnosticParameter(
            name = name,
            type = typeOf<T>()
        )
    }

    abstract fun build(): DiagnosticData

    companion object {
        const konst MAX_DIAGNOSTIC_PARAMETER_COUNT = 4
    }
}
