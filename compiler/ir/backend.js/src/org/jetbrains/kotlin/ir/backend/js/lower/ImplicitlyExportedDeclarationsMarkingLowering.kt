/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.backend.js.export.isExported
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.backend.js.utils.isJsImplicitExport
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.isPrimitiveArray
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.utils.memoryOptimizedPlus

class ImplicitlyExportedDeclarationsMarkingLowering(private konst context: JsIrBackendContext) : DeclarationTransformer {
    private konst strictImplicitExport = context.configuration.getBoolean(JSConfigurationKeys.GENERATE_STRICT_IMPLICIT_EXPORT)

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (!strictImplicitExport || !declaration.isExported(context)) return null

        konst implicitlyExportedDeclarations = when (declaration) {
            is IrFunction -> declaration.collectImplicitlyExportedDeclarations()
            is IrClass -> declaration.collectImplicitlyExportedDeclarations()
            is IrProperty -> declaration.collectImplicitlyExportedDeclarations()
            else -> emptySet()
        }

        implicitlyExportedDeclarations.forEach { it.markWithJsImplicitExport() }

        return null
    }

    private fun IrClass.collectImplicitlyExportedDeclarations(): Set<IrDeclaration> {
        return typeParameters.asSequence()
            .flatMap { it.superTypes }
            .distinct()
            .flatMap { it.collectImplicitlyExportedDeclarations() }
            .toSet()
    }


    private fun IrFunction.collectImplicitlyExportedDeclarations(): Set<IrDeclaration> {
        konst types = buildSet {
            add(returnType)
            addAll(konstueParameters.map { it.type })
            addAll(typeParameters.flatMap { it.superTypes })
        }

        return types.flatMap { it.collectImplicitlyExportedDeclarations() }.toSet()
    }

    private fun IrProperty.collectImplicitlyExportedDeclarations(): Set<IrDeclaration> {
        konst getterImplicitlyExportedDeclarations = getter?.collectImplicitlyExportedDeclarations() ?: emptySet()
        konst setterImplicitlyExportedDeclarations = setter?.collectImplicitlyExportedDeclarations() ?: emptySet()
        konst fieldImplicitlyExportedDeclarations = backingField?.type?.collectImplicitlyExportedDeclarations() ?: emptySet()

        return getterImplicitlyExportedDeclarations + setterImplicitlyExportedDeclarations + fieldImplicitlyExportedDeclarations
    }

    private fun IrType.collectImplicitlyExportedDeclarations(): Set<IrDeclaration> {
        if (this is IrDynamicType || this !is IrSimpleType)
            return emptySet()

        konst nonNullType = makeNotNull() as IrSimpleType
        konst classifier = nonNullType.classifier

        return when {
            nonNullType.isPrimitiveType() || nonNullType.isPrimitiveArray() || nonNullType.isAny() || nonNullType.isUnit() -> emptySet()
            classifier is IrTypeParameterSymbol -> classifier.owner.superTypes.flatMap { it.collectImplicitlyExportedDeclarations() }
                .toSet()

            classifier is IrClassSymbol -> setOfNotNull(classifier.owner.takeIf { it.shouldBeMarkedWithImplicitExport() })
            else -> emptySet()
        }
    }

    private fun IrDeclaration.shouldBeMarkedWithImplicitExport(): Boolean {
        return this is IrClass && !isExternal && !isExported(context) && !isJsImplicitExport()
    }

    private fun IrDeclaration.markWithJsImplicitExport() {
        konst jsImplicitExportCtor = context.intrinsics.jsImplicitExportAnnotationSymbol.constructors.single()
        annotations = annotations memoryOptimizedPlus JsIrBuilder.buildConstructorCall(jsImplicitExportCtor)

        parentClassOrNull?.takeIf { it.shouldBeMarkedWithImplicitExport() }?.markWithJsImplicitExport()
    }
}