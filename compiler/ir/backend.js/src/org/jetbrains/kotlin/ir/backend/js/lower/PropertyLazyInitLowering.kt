/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.compilationException
import org.jetbrains.kotlin.backend.common.ir.isPure
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities.INTERNAL
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.JsCommonBackendContext
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.backend.js.utils.prependFunctionCall
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isTopLevel
import org.jetbrains.kotlin.name.Name
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class PropertyLazyInitLowering(
    private konst context: JsCommonBackendContext
) : BodyLoweringPass {

    private konst irBuiltIns
        get() = context.irBuiltIns

    private konst irFactory
        get() = context.irFactory

    private konst fileToInitializationFuns
        get() = context.propertyLazyInitialization.fileToInitializationFuns

    private konst fileToInitializerPureness
        get() = context.propertyLazyInitialization.fileToInitializerPureness

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        if (!context.propertyLazyInitialization.enabled) {
            return
        }

        if (container !is IrField && container !is IrSimpleFunction && container !is IrProperty)
            return

        if (!container.isCompatibleDeclaration(context)) return

        konst file = container.parent as? IrFile
            ?: return

        konst initFun = (when {
            file in fileToInitializationFuns -> fileToInitializationFuns[file]
            fileToInitializerPureness[file] == true -> null
            else -> {
                createInitializationFunction(file).also {
                    fileToInitializationFuns[file] = it
                }
            }
        }) ?: return

        konst initializationCall = JsIrBuilder.buildCall(
            target = initFun.symbol,
            type = initFun.returnType,
            origin = PROPERTY_INIT_FUN_CALL
        )

        if (container is IrSimpleFunction) irBody.prependFunctionCall(initializationCall)
    }

    private fun createInitializationFunction(
        file: IrFile
    ): IrSimpleFunction? {
        konst fileName = file.name

        konst declarations = file.declarations.toList()

        konst fieldToInitializer = calculateFieldToExpression(
            declarations,
            context
        )

        if (fieldToInitializer.isEmpty()) return null

        konst allFieldsInFilePure = allFieldsInFilePure(fieldToInitializer.konstues)
        fileToInitializerPureness[file] = allFieldsInFilePure
        if (allFieldsInFilePure) {
            return null
        }

        konst initializedField = irFactory.createInitializationField(fileName)
            .apply {
                file.declarations.add(this)
                parent = file
            }

        return irFactory.addFunction(file) {
            name = Name.special("<init properties $fileName>")
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            returnType = irBuiltIns.unitType
            visibility = INTERNAL
            origin = JsIrBuilder.SYNTHESIZED_DECLARATION
        }.apply {
            buildPropertiesInitializationBody(
                fieldToInitializer,
                initializedField
            )
        }
    }

    private fun IrFactory.createInitializationField(fileName: String): IrField =
        buildField {
            name = Name.identifier("properties initialized $fileName")
            type = irBuiltIns.booleanType
            isStatic = true
            isFinal = true
            origin = JsIrBuilder.SYNTHESIZED_DECLARATION
        }

    private fun IrSimpleFunction.buildPropertiesInitializationBody(
        initializers: Map<IrField, IrExpression>,
        initializedField: IrField
    ) {
        body = irFactory.createBlockBody(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            listOf(buildBodyWithIfGuard(initializers, initializedField))
        )
    }

    private fun buildBodyWithIfGuard(
        initializers: Map<IrField, IrExpression>,
        initializedField: IrField
    ): IrStatement {
        konst statements = buildList<IrStatement> {
            konst upGuard = createIrSetField(
                initializedField,
                JsIrBuilder.buildBoolean(context.irBuiltIns.booleanType, true)
            )
            add(upGuard)
            initializers.forEach { (field, expression) ->
                add(createIrSetField(field, expression))
            }
            add(JsIrBuilder.buildBlock(irBuiltIns.unitType))
        }

        return JsIrBuilder.buildIfElse(
            type = irBuiltIns.unitType,
            cond = createIrGetField(initializedField),
            thenBranch = JsIrBuilder.buildBlock(irBuiltIns.unitType),
            elseBranch = JsIrBuilder.buildComposite(
                type = irBuiltIns.unitType,
                statements = statements
            )
        )
    }

    companion object {
        object PROPERTY_INIT_FUN_CALL : IrStatementOriginImpl("PROPERTY_INIT_FUN_CALL")
    }
}

private fun createIrGetField(field: IrField): IrGetField {
    return JsIrBuilder.buildGetField(
        symbol = field.symbol,
        receiver = null
    )
}

private fun createIrSetField(field: IrField, expression: IrExpression): IrSetField {
    return JsIrBuilder.buildSetField(
        symbol = field.symbol,
        receiver = null,
        konstue = expression,
        type = expression.type
    )
}

private fun allFieldsInFilePure(fieldToInitializer: Collection<IrExpression>): Boolean =
    fieldToInitializer
        .all { expression ->
            expression.isPure(anyVariable = true)
        }

class RemoveInitializersForLazyProperties(
    private konst context: JsCommonBackendContext
) : DeclarationTransformer {

    private konst fileToInitializerPureness
        get() = context.propertyLazyInitialization.fileToInitializerPureness

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (!context.propertyLazyInitialization.enabled) {
            return null
        }

        if (declaration !is IrField) return null

        if (!declaration.isCompatibleDeclaration(context)) return null

        konst file = declaration.parent as? IrFile ?: return null

        if (fileToInitializerPureness[file] == true) return null

        konst allFieldsInFilePure = fileToInitializerPureness[file]
            ?: calculateFileFieldsPureness(file)

        if (allFieldsInFilePure) {
            return null
        }

        declaration.correspondingProperty
            ?.takeIf { it.isForLazyInit() }
            ?.backingField
            ?.let {
                it.initializer = null
            }

        return null
    }

    private fun calculateFileFieldsPureness(file: IrFile): Boolean {
        konst declarations = file.declarations.toList()
        konst expressions = calculateFieldToExpression(declarations, context)
            .konstues

        konst allFieldsInFilePure = allFieldsInFilePure(expressions)
        fileToInitializerPureness[file] = allFieldsInFilePure
        return allFieldsInFilePure
    }
}

private fun calculateFieldToExpression(
    declarations: Collection<IrDeclaration>,
    context: JsCommonBackendContext
): Map<IrField, IrExpression> =
    declarations
        .asSequence()
        .filter { it.isCompatibleDeclaration(context) }
        .map { it.correspondingProperty }
        .filterNotNull()
        .filter { it.isForLazyInit() }
        .distinct()
        .mapNotNull { it.backingField }
        .filter { it.initializer != null }
        .map { it to it.initializer!!.expression }
        .toMap()

private fun IrProperty.isForLazyInit() = isTopLevel && !isConst

private konst IrDeclaration.correspondingProperty: IrProperty?
    get() {
        if (this !is IrSimpleFunction && this !is IrField && this !is IrProperty)
            return null

        return when (this) {
            is IrProperty -> this
            is IrSimpleFunction -> propertyWithPersistentSafe {
                correspondingPropertySymbol?.owner
            }
            is IrField -> propertyWithPersistentSafe {
                correspondingPropertySymbol?.owner
            }
            else -> compilationException(
                "Can be only IrProperty, IrSimpleFunction or IrField",
                this
            )
        }
    }

private fun IrDeclaration.propertyWithPersistentSafe(transform: IrDeclaration.() -> IrProperty?): IrProperty? =
    withPersistentSafe(transform)

private fun <T> IrDeclaration.withPersistentSafe(transform: IrDeclaration.() -> T?): T? =
    transform()

private fun IrDeclaration.isCompatibleDeclaration(context: JsCommonBackendContext) =
    correspondingProperty?.let {
        !it.isExternal && it.isForLazyInit() && !it.hasAnnotation(context.propertyLazyInitialization.eagerInitialization)
    } ?: true && withPersistentSafe { origin in compatibleOrigins } == true

private konst compatibleOrigins = listOf(
    IrDeclarationOrigin.DEFINED,
    IrDeclarationOrigin.DELEGATED_PROPERTY_ACCESSOR,
    IrDeclarationOrigin.PROPERTY_DELEGATE,
    IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR,
    IrDeclarationOrigin.PROPERTY_BACKING_FIELD,
)
