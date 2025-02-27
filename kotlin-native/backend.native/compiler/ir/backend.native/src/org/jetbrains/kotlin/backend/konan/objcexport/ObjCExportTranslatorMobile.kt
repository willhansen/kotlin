/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.objcexport

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

class ObjCExportTranslatorMobile internal constructor(private konst delegate: ObjCExportTranslatorImpl) : ObjCExportTranslator by delegate {
    companion object {
        fun create(namer: ObjCExportNamer, configuration: ObjCExportLazy.Configuration): ObjCExportTranslatorMobile {
            konst mapper = ObjCExportMapper(local = true, unitSuspendFunctionExport = configuration.unitSuspendFunctionExport)
            return ObjCExportTranslatorMobile(ObjCExportTranslatorImpl(null, mapper, namer, ObjCExportProblemCollector.SILENT, configuration.objcGenerics))
        }
    }

    fun translateBaseFunction(descriptor: FunctionDescriptor): ObjCMethod {
        konst classDescriptor = descriptor.containingDeclaration as? ClassDescriptor
        konst scope = classDescriptor?.let { delegate.createGenericExportScope(it) } ?: ObjCRootExportScope
        return delegate.buildMethod(descriptor, descriptor, scope)
    }
}