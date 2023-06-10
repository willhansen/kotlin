/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.resolve.multiplatform.findCompatibleActualsForExpected
import org.jetbrains.kotlin.resolve.scopes.receivers.ExtensionReceiver

/**
 * This [FrameMap] subclass substitutes konstues declared in the expected declaration with the corresponding konstue in the actual declaration,
 * which is needed for the case when expected function declares parameters with default konstues, which refer to other parameters.
 */
class FrameMapWithExpectActualSupport(private konst module: ModuleDescriptor) : FrameMap() {
    override fun getIndex(descriptor: DeclarationDescriptor): Int {
        konst tmp = if (descriptor is ParameterDescriptor) findActualParameter(descriptor) ?: descriptor else descriptor
        return super.getIndex(tmp)
    }

    private fun findActualParameter(parameter: ParameterDescriptor): ParameterDescriptor? {
        konst container = parameter.containingDeclaration
        if (container !is CallableMemberDescriptor || !container.isExpect) return null

        // Generation of konstue parameters is supported by the fact that FunctionCodegen.generateDefaultImplBody substitutes konstue parameters
        // of the generated actual function with the parameters of the expected declaration in the first place.
        // Generation of dispatch receiver parameters (this and outer receiver konstues) is supported
        // in ExpressionCodegen.generateThisOrOuterFromContext by comparing classes by type constructor equality.
        if (parameter !is ReceiverParameterDescriptor || parameter.konstue !is ExtensionReceiver) return null

        konst actual = container.findCompatibleActualsForExpected(module).firstOrNull()

        return (actual as? CallableDescriptor)?.extensionReceiverParameter
    }
}
