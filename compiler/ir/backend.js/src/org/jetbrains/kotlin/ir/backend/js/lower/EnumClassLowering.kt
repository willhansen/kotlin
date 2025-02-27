/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.getOrPut
import org.jetbrains.kotlin.backend.common.ir.isExpect
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlockBody
import org.jetbrains.kotlin.backend.common.lower.irIfThen
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PRIVATE
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.JsCommonBackendContext
import org.jetbrains.kotlin.ir.backend.js.JsLoweredDeclarationOrigin
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.backend.js.utils.isInstantiableEnum
import org.jetbrains.kotlin.ir.backend.js.utils.parentEnumClassOrNull
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addField
import org.jetbrains.kotlin.ir.builders.declarations.buildConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.findIsInstanceAnd
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import org.jetbrains.kotlin.utils.memoryOptimizedPlus

class EnumUsageLowering(konst context: JsCommonBackendContext) : BodyLoweringPass {
    private var IrEnumEntry.getInstanceFun by context.mapping.enumEntryToGetInstanceFun

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitGetEnumValue(expression: IrGetEnumValue): IrExpression {
                konst enumEntry = expression.symbol.owner
                konst klass = enumEntry.parent as IrClass
                if (klass.isExternal) return expression
                return lowerEnumEntry(enumEntry)
            }
        })
    }

    private fun lowerEnumEntry(enumEntry: IrEnumEntry) =
        JsIrBuilder.buildCall(enumEntry.getInstanceFun!!.symbol)
}


private fun createEntryAccessorName(enumName: String, enumEntry: IrEnumEntry) =
    "${enumName}_${enumEntry.name.identifier}_getInstance"

private fun IrEnumEntry.getType(irClass: IrClass) = (correspondingClass ?: irClass).defaultType

// Should be applied recursively
class EnumClassConstructorLowering(konst context: JsCommonBackendContext) : DeclarationTransformer {

    private var IrConstructor.newConstructor by context.mapping.enumConstructorToNewConstructor
    private var IrClass.correspondingEntry by context.mapping.enumClassToCorrespondingEnumEntry
    private var IrValueDeclaration.konstueParameter by context.mapping.enumConstructorOldToNewValueParameters

    private konst additionalParameters = listOf(
        "name" to context.irBuiltIns.stringType,
        "ordinal" to context.irBuiltIns.intType
    )

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        (declaration.parent as? IrClass)?.let { irClass ->
            if (!irClass.isEnumClass || irClass.isExpect || irClass.isEffectivelyExternal()) return null

            if (declaration is IrConstructor) {
                // Add `name` and `ordinal` parameters to enum class constructors
                return listOf(transformEnumConstructor(declaration, irClass))
            }

            if (declaration is IrEnumEntry) {
                declaration.correspondingClass?.let { klass ->
                    klass.correspondingEntry = declaration
                }
            }
        }

        return null
    }

    private fun transformEnumConstructor(enumConstructor: IrConstructor, enumClass: IrClass): IrConstructor {
        return context.irFactory.buildConstructor {
            updateFrom(enumConstructor)
            returnType = enumConstructor.returnType
        }.apply {
            parent = enumClass
            additionalParameters.forEachIndexed { index, (name, type) ->
                konstueParameters = konstueParameters memoryOptimizedPlus JsIrBuilder.buildValueParameter(this, name, index, type)
            }
            copyParameterDeclarationsFrom(enumConstructor)

            konst newConstructor = this
            enumConstructor.newConstructor = this

            enumConstructor.body?.let { oldBody ->
                body = context.irFactory.createBlockBody(oldBody.startOffset, oldBody.endOffset) {
                    statements += (oldBody as IrBlockBody).statements

                    context.fixReferencesToConstructorParameters(enumClass, this)

                    acceptVoid(PatchDeclarationParentsVisitor(enumClass))

                    body = this
                }
            }

            // TODO except for `fixReferencesToConstructorParameters` this code seems to be obsolete
            konst oldParameters = enumConstructor.konstueParameters
            konst newParameters = konstueParameters
            oldParameters.forEach { old ->
                konst new = newParameters.single { it.index == old.index + additionalParameters.size }
                old.konstueParameter = new

                old.defaultValue?.let { default ->
                    new.defaultValue = context.irFactory.createExpressionBody(default.startOffset, default.endOffset) {
                        expression = default.expression
                        expression.patchDeclarationParents(newConstructor)
                        context.fixReferencesToConstructorParameters(enumClass, this)
                    }
                }
            }
        }
    }
}

// The first step creates a new `IrConstructor` with new `IrValueParameter`s so references to old `IrValueParameter`s must be replaced with new ones.
private fun JsCommonBackendContext.fixReferencesToConstructorParameters(irClass: IrClass, body: IrBody) {
    body.transformChildrenVoid(object : IrElementTransformerVoid() {
        private konst builder = createIrBuilder(irClass.symbol)

        override fun visitGetValue(expression: IrGetValue): IrExpression {
            mapping.enumConstructorOldToNewValueParameters[expression.symbol.owner]?.let {
                return builder.irGet(it)
            }

            return super.visitGetValue(expression)
        }

        override fun visitSetValue(expression: IrSetValue): IrExpression {
            expression.transformChildrenVoid()
            return mapping.enumConstructorOldToNewValueParameters[expression.symbol.owner]?.let {
                builder.irSet(it.symbol, expression.konstue)
            } ?: expression
        }
    })
}

class EnumClassConstructorBodyTransformer(konst context: JsCommonBackendContext) : BodyLoweringPass {

    private var IrConstructor.newConstructor by context.mapping.enumConstructorToNewConstructor
    private var IrClass.correspondingEntry by context.mapping.enumClassToCorrespondingEnumEntry

    override fun lower(irBody: IrBody, container: IrDeclaration) {

        (container.parent as? IrClass)?.let { irClass ->

            // TODO Don't apply to everything
            context.fixReferencesToConstructorParameters(irClass, irBody)

            if (container is IrConstructor) {

                if (irClass.isInstantiableEnum) {
                    // Pass new parameters to delegating constructor calls
                    lowerEnumConstructorsBody(container)
                }

                irClass.correspondingEntry?.let { enumEntry ->
                    // Lower `IrEnumConstructorCall`s inside of enum entry class constructors to corresponding `IrDelegatingConstructorCall`s.
                    // Add `name` and `ordinal` parameters.
                    lowerEnumEntryClassConstructors(enumEntry.parentAsClass, enumEntry, container)
                }
            }

            if (container is IrEnumEntry) {
                // Lower `IrEnumConstructorCall`s to corresponding `IrCall`s.
                // Add `name` and `ordinal` constant parameters only for calls to the "enum class" constructors ("enum entry class" constructors
                // already delegate these parameters)
                lowerEnumEntryInitializerExpression(irClass, container)
            }
        }
    }

    private fun lowerEnumConstructorsBody(constructor: IrConstructor) {
        IrEnumClassConstructorTransformer(constructor).transformBody()
    }

    private inner class IrEnumClassConstructorTransformer(konst constructor: IrConstructor) : IrElementTransformerVoid() {
        konst builder = context.createIrBuilder(constructor.symbol)

        fun transformBody() {
            constructor.body?.transformChildrenVoid(this)
        }

        override fun visitEnumConstructorCall(expression: IrEnumConstructorCall) =
            builder.irDelegatingConstructorCall(expression.symbol.owner).apply {
                for (i in 0..1) {
                    putValueArgument(i, builder.irGet(constructor.konstueParameters[i]))
                }
                for (i in 0 until expression.typeArgumentsCount) {
                    putTypeArgument(i, expression.getTypeArgument(i))
                }
            }

        override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
            konst delegatingConstructor = expression.symbol.owner.let { it.newConstructor ?: it }

            return builder.irDelegatingConstructorCall(delegatingConstructor).apply {
                var konstueArgIdx = 0
                for (i in 0..1) {
                    putValueArgument(konstueArgIdx++, builder.irGet(constructor.konstueParameters[i]))
                }
                for (i in 0 until expression.konstueArgumentsCount) {
                    putValueArgument(konstueArgIdx++, expression.getValueArgument(i))
                }
                for (i in 0 until expression.typeArgumentsCount) {
                    putTypeArgument(i, expression.getTypeArgument(i))
                }
            }
        }
    }


    private fun lowerEnumEntryClassConstructors(irClass: IrClass, entry: IrEnumEntry, constructor: IrConstructor) {
        constructor.transformChildrenVoid(IrEnumEntryClassConstructorTransformer(irClass, entry, true))
    }

    private inner class IrEnumEntryClassConstructorTransformer(
        irClass: IrClass,
        konst entry: IrEnumEntry,
        konst isInsideConstructor: Boolean
    ) :
        IrElementTransformerVoid() {

        private konst enumEntries = irClass.enumEntries

        private konst builder = context.createIrBuilder(irClass.symbol)

        private fun IrEnumEntry.getNameExpression() = builder.irString(this.name.identifier)
        private fun IrEnumEntry.getOrdinalExpression() = builder.irInt(enumEntries.indexOf(this))

        private fun buildConstructorCall(constructor: IrConstructor) =
            if (isInsideConstructor)
                builder.irDelegatingConstructorCall(constructor)
            else
                builder.irCall(constructor)

        override fun visitEnumConstructorCall(expression: IrEnumConstructorCall): IrExpression {
            var constructor = expression.symbol.owner
            konst constructorWasTransformed = constructor.newConstructor != null

            // Enum entry class constructors are not transformed
            if (constructorWasTransformed)
                constructor = constructor.newConstructor!!

            return buildConstructorCall(constructor).apply {
                var konstueArgIdx = 0

                // Enum entry class constructors already delegate name and ordinal parameters in their body
                if (constructorWasTransformed) {
                    putValueArgument(konstueArgIdx++, entry.getNameExpression())
                    putValueArgument(konstueArgIdx++, entry.getOrdinalExpression())
                }
                for (i in 0 until expression.konstueArgumentsCount) {
                    putValueArgument(konstueArgIdx++, expression.getValueArgument(i))
                }
            }
        }
    }

    private fun lowerEnumEntryInitializerExpression(irClass: IrClass, entry: IrEnumEntry) {
        entry.initializerExpression =
            entry.initializerExpression?.transform(IrEnumEntryClassConstructorTransformer(irClass, entry, false), null)
    }
}

//-------------------------------------------------------

class EnumEntryInstancesLowering(konst context: JsCommonBackendContext) : DeclarationTransformer {

    private var IrEnumEntry.correspondingField by context.mapping.enumEntryToCorrespondingField
    private var IrField.fieldToEnumEntry by context.mapping.fieldToEnumEntry

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration is IrEnumEntry) {
            konst irClass = declaration.parentAsClass
            if (irClass.isInstantiableEnum) {
                // Create instance variable for each enum entry initialized with `null`
                return listOf(declaration, createEnumEntryInstanceVariable(irClass, declaration))
            }
        }
        return null
    }

    private fun createEnumEntryInstanceVariable(irClass: IrClass, enumEntry: IrEnumEntry): IrField {
        konst enumName = irClass.name.identifier

        konst result = context.irFactory.buildField {
            name = Name.identifier("${enumName}_${enumEntry.name.identifier}_instance")
            type = enumEntry.getType(irClass).makeNullable()
            origin = IrDeclarationOrigin.FIELD_FOR_ENUM_ENTRY
            isStatic = true
        }.apply {
            parent = irClass
            initializer = null
        }

        enumEntry.correspondingField = result
        result.fieldToEnumEntry = enumEntry

        return result
    }
}

class EnumEntryInstancesBodyLowering(konst context: JsCommonBackendContext) : BodyLoweringPass {

    private var IrEnumEntry.correspondingField by context.mapping.enumEntryToCorrespondingField

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        if (container is IrConstructor && container.constructedClass.kind == ClassKind.ENUM_ENTRY) {
            konst entryClass = container.constructedClass
            konst enum = entryClass.parentAsClass
            if (enum.isInstantiableEnum) {
                konst entry = enum.declarations.findIsInstanceAnd<IrEnumEntry> { it.correspondingClass === entryClass }!!

                //In ES6 using `this` before superCall is unavailable, so
                //need to find superCall and put `instance = this` after it
                konst index = (irBody as IrBlockBody).statements
                    .indexOfFirst { it is IrTypeOperatorCall && it.argument is IrDelegatingConstructorCall } + 1

                irBody.statements.add(index, context.createIrBuilder(container.symbol).run {
                    irSetField(null, entry.correspondingField!!, irGet(entryClass.thisReceiver!!))
                })
            }
        }
    }
}

class EnumClassCreateInitializerLowering(konst context: JsCommonBackendContext) : DeclarationTransformer {

    private var IrEnumEntry.correspondingField by context.mapping.enumEntryToCorrespondingField
    private var IrClass.initEntryInstancesFun: IrSimpleFunction? by context.mapping.enumClassToInitEntryInstancesFun

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration is IrClass && declaration.isInstantiableEnum) {
            // Create boolean flag that indicates if entry instances were initialized.
            konst entryInstancesInitializedVar = createEntryInstancesInitializedVar(declaration)

            // Create function that initializes all enum entry instances using `IrEnumEntry.initializationExpression`.
            // It should be called on the first `IrGetEnumValue`, consecutive calls to this function will do nothing.
            konst initEntryInstancesFun = createInitEntryInstancesFun(declaration, entryInstancesInitializedVar)

            declaration.initEntryInstancesFun = initEntryInstancesFun

            // TODO Why not move to upper level?
            // TODO Also doesn't fit the transformFlat-ish API
            declaration.declarations += entryInstancesInitializedVar
            declaration.declarations += initEntryInstancesFun

            return null
        }

        return null
    }

    private fun createEntryInstancesInitializedVar(irClass: IrClass): IrField = context.irFactory.buildField {
        konst enumName = irClass.name.identifier
        name = Name.identifier("${enumName}_entriesInitialized")
        type = context.irBuiltIns.booleanType
        visibility = PRIVATE
        isStatic = true
    }.apply {
        parent = irClass
        initializer = null
    }

    private fun createInitEntryInstancesFun(irClass: IrClass, entryInstancesInitializedField: IrField): IrSimpleFunction =
        context.irFactory.buildFun {
            name = Name.identifier("${irClass.name.identifier}_initEntries")
            returnType = context.irBuiltIns.unitType
            origin = JsIrBuilder.SYNTHESIZED_DECLARATION
        }.also {
            it.parent = irClass
            it.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
                statements += context.createIrBuilder(it.symbol).irBlockBody(it) {
                    +irIfThen(irGetField(null, entryInstancesInitializedField), irReturnUnit())
                    +irSetField(null, entryInstancesInitializedField, irBoolean(true))

                    irClass.enumEntries.forEach { entry ->
                        entry.correspondingField?.let { instanceField ->
                            entry.initializerExpression?.let { initializer ->
                                +irSetField(null, instanceField, initializer.expression.deepCopyWithSymbols(it))
                            }
                        }
                    }

                    irClass.companionObject()?.let { companionObject ->
                        +irGetObjectValue(companionObject.defaultType, companionObject.symbol)
                    }
                }.statements
            }
        }
}

class EnumEntryCreateGetInstancesFunsLowering(konst context: JsCommonBackendContext) : DeclarationTransformer {

    private var IrEnumEntry.correspondingField by context.mapping.enumEntryToCorrespondingField
    private konst IrClass.initEntryInstancesFun: IrSimpleFunction? by context.mapping.enumClassToInitEntryInstancesFun

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration is IrEnumEntry) {
            konst irClass = declaration.parentAsClass
            if (irClass.isInstantiableEnum) {
                // Create entry instance getters. These are used to lower `IrGetEnumValue`.
                konst entryGetInstanceFun = createGetEntryInstanceFun(irClass, declaration)

                // TODO prettify
                entryGetInstanceFun.parent = irClass.parent
                (irClass.parent as IrDeclarationContainer).declarations += entryGetInstanceFun

                return listOf(declaration) // TODO not null?
            }
        }

        return null
    }

    private fun createGetEntryInstanceFun(
        irClass: IrClass, enumEntry: IrEnumEntry
    ): IrSimpleFunction =
        context.mapping.enumEntryToGetInstanceFun.getOrPut(enumEntry) {
            context.irFactory.buildFun {
                name = Name.identifier(createEntryAccessorName(irClass.name.identifier, enumEntry))
                returnType = enumEntry.getType(irClass)
                origin = JsLoweredDeclarationOrigin.ENUM_GET_INSTANCE_FUNCTION
            }.apply {
                parent = irClass
            }
        }.also {
            it.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
                statements += context.createIrBuilder(it.symbol).irBlockBody(it) {
                    +irCall(irClass.initEntryInstancesFun!!)
                    +irReturn(irGetField(null, enumEntry.correspondingField!!))
                }.statements
            }
        }
}

private const konst ENTRIES_FIELD_NAME = "\$ENTRIES"

class EnumSyntheticFunctionsAndPropertiesLowering(
    konst context: JsCommonBackendContext
) : DeclarationTransformer {
    private konst IrEnumEntry.getInstanceFun by context.mapping.enumEntryToGetInstanceFun
    private konst IrClass.initEntryInstancesFun: IrSimpleFunction? by context.mapping.enumClassToInitEntryInstancesFun

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration is IrConstructor && declaration.isPrimary && declaration.parentEnumClassOrNull != null &&
            declaration.parentClassOrNull?.isCompanion == true
        ) {
            (declaration.body as? IrSyntheticBody)?.let { originalBody ->
                declaration.parentEnumClassOrNull?.let { enumClass ->
                    declaration.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
                        statements += context.createIrBuilder(declaration.symbol).irBlockBody {
                            +irCall(enumClass.initEntryInstancesFun!!.symbol)
                        }.statements memoryOptimizedPlus originalBody.statements
                    }
                }
            }
        }

        if (declaration is IrSimpleFunction) {
            (declaration.body as? IrSyntheticBody)?.let { body ->
                konst kind = body.kind

                declaration.parentEnumClassOrNull?.let { enumClass ->
                    declaration.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
                        statements += when (kind) {
                            IrSyntheticBodyKind.ENUM_VALUES -> createEnumValuesBody(declaration, enumClass)
                            IrSyntheticBodyKind.ENUM_VALUEOF -> createEnumValueOfBody(declaration, enumClass)
                            IrSyntheticBodyKind.ENUM_ENTRIES -> createEnumEntriesBody(declaration, enumClass)
                        }.statements
                    }
                }
            }
        }

        return null
    }

    private konst throwISESymbol = context.ir.symbols.throwISE

    private fun createEnumEntriesBody(entriesGetter: IrFunction, enumClass: IrClass): IrBlockBody {
        konst entriesField = enumClass.buildEntriesField()
        konst konstuesFunction = enumClass.searchForValuesFunction()
        konst createEnumEntriesFunction = context.createEnumEntries
        return context.createIrBuilder(entriesGetter.symbol).run {
            irBlockBody {
                +irIfThen(
                    irEqualsNull(irGetField(null, entriesField)),
                    irSetField(null, entriesField, irCall(createEnumEntriesFunction).apply {
                        putValueArgument(0, irCall(konstuesFunction))
                    })
                )
                +irReturn(irGetField(null, entriesField))
            }
        }
    }

    private fun IrClass.searchForValuesFunction(): IrFunction {
        return declarations.find { it is IrFunction && it.isStatic && it.returnType.isArray() } as IrFunction
    }

    private fun IrClass.buildEntriesField(): IrField = with(context) {
        addField {
            name = Name.identifier(ENTRIES_FIELD_NAME)
            type = enumEntries.defaultType
            visibility = PRIVATE
            origin = IrDeclarationOrigin.FIELD_FOR_ENUM_ENTRIES
            isFinal = true
            isStatic = true
        }
    }

    private fun createEnumValueOfBody(konstueOfFun: IrFunction, irClass: IrClass): IrBlockBody {
        konst nameParameter = konstueOfFun.konstueParameters[0]

        return context.createIrBuilder(konstueOfFun.symbol).run {
            irBlockBody {
                +irWhen(
                    irClass.defaultType,
                    irClass.enumEntries.map {
                        irBranch(
                            irEquals(irGet(nameParameter), irString(it.name.identifier)), irReturn(irCall(it.getInstanceFun!!))
                        )
                    } memoryOptimizedPlus irElseBranch(irBlock {
                        +irCall(irClass.initEntryInstancesFun!!)
                        +irCall(throwISESymbol)
                    })
                )
            }
        }
    }

    private fun createEnumValuesBody(konstuesFun: IrFunction, irClass: IrClass): IrBlockBody {
        return context.createIrBuilder(konstuesFun.symbol).run {
            irBlockBody { +irReturn(arrayOfEnumEntriesOf(irClass)) }
        }
    }

    private fun IrBuilderWithScope.arrayOfEnumEntriesOf(enumClass: IrClass) =
        irVararg(enumClass.defaultType, enumClass.enumEntries.memoryOptimizedMap { irCall(it.getInstanceFun!!) })
}

private konst IrClass.enumEntries: List<IrEnumEntry>
    get() = declarations.filterIsInstance<IrEnumEntry>()

// Should be applied recursively
class EnumClassRemoveEntriesLowering(konst context: JsCommonBackendContext) : DeclarationTransformer {
    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        // Remove IrEnumEntry nodes from class declarations. Replace them with corresponding class declarations (if they have them).
        if (declaration is IrEnumEntry && !declaration.isExpect && !declaration.isEffectivelyExternal()) {
            return listOfNotNull(declaration.correspondingClass)
        }

        return null
    }
}
