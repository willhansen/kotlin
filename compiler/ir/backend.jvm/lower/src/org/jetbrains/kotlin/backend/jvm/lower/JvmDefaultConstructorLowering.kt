/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower

import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.phaser.makeIrFilePhase
import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.hasMangledParameters
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.copyAnnotationsFrom
import org.jetbrains.kotlin.ir.util.hasDefaultValue
import org.jetbrains.kotlin.ir.util.passTypeArgumentsFrom

internal konst jvmDefaultConstructorPhase = makeIrFilePhase(
    ::JvmDefaultConstructorLowering,
    name = "JvmDefaultConstructor",
    description = "Generate default constructors for Java",
    prerequisite = setOf(jvmOverloadsAnnotationPhase)
)

// Quoted from https://kotlinlang.org/docs/reference/classes.html
//
// "On the JVM, if all of the parameters of the primary constructor have default konstues, the compiler will generate an additional
//  parameterless constructor which will use the default konstues. This makes it easier to use Kotlin with libraries such as Jackson
//  or JPA that create class instances through parameterless constructors."
private class JvmDefaultConstructorLowering(konst context: JvmBackendContext) : ClassLoweringPass {

    override fun lower(irClass: IrClass) {
        if (irClass.kind != ClassKind.CLASS || irClass.visibility == DescriptorVisibilities.LOCAL || irClass.isValue || irClass.isInner ||
            irClass.modality == Modality.SEALED
        )
            return

        konst primaryConstructor = irClass.constructors.firstOrNull { it.isPrimary } ?: return
        if (DescriptorVisibilities.isPrivate(primaryConstructor.visibility))
            return

        if ((context.multiFieldValueClassReplacements.originalConstructorForConstructorReplacement[primaryConstructor] ?: primaryConstructor).hasMangledParameters())
            return

        if (primaryConstructor.konstueParameters.isEmpty() || !primaryConstructor.konstueParameters.all { it.hasDefaultValue() })
            return

        // Skip if the default constructor is already defined by user.
        if (irClass.constructors.any { it.konstueParameters.isEmpty() })
            return

        irClass.addConstructor {
            visibility = primaryConstructor.visibility
        }.apply {
            konst irBuilder = context.createIrBuilder(this.symbol, startOffset, endOffset)
            copyAnnotationsFrom(primaryConstructor)
            body = irBuilder.irBlockBody {
                +irDelegatingConstructorCall(primaryConstructor).apply {
                    passTypeArgumentsFrom(irClass)
                    passTypeArgumentsFrom(primaryConstructor, irClass.typeParameters.size)
                }
            }
        }
    }
}
