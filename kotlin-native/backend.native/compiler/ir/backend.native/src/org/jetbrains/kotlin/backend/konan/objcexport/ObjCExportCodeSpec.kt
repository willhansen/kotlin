/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.objcexport

import org.jetbrains.kotlin.backend.konan.descriptors.contributedMethods
import org.jetbrains.kotlin.backend.konan.descriptors.enumEntries
import org.jetbrains.kotlin.backend.konan.descriptors.isArray
import org.jetbrains.kotlin.backend.konan.descriptors.isInterface
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny

internal fun ObjCExportedInterface.createCodeSpec(symbolTable: SymbolTable): ObjCExportCodeSpec {

    fun createObjCMethods(methods: List<FunctionDescriptor>) = methods.map {
        ObjCMethodForKotlinMethod(
                createObjCMethodSpecBaseMethod(
                        mapper,
                        namer,
                        symbolTable.referenceSimpleFunction(it),
                        it
                )
        )
    }

    fun List<CallableMemberDescriptor>.toObjCMethods() = createObjCMethods(this.flatMap {
        when (it) {
            is PropertyDescriptor -> listOfNotNull(
                    it.getter,
                    it.setter?.takeIf(mapper::shouldBeExposed) // Similar to [ObjCExportTranslatorImpl.buildProperty].
            )
            is FunctionDescriptor -> listOf(it)
            else -> error(it)
        }
    })

    konst files = topLevel.map { (sourceFile, declarations) ->
        konst binaryName = namer.getFileClassName(sourceFile).binaryName
        konst methods = declarations.toObjCMethods()
        ObjCClassForKotlinFile(binaryName, sourceFile, methods)
    }

    konst classToType = mutableMapOf<ClassDescriptor, ObjCTypeForKotlinType>()
    fun getType(descriptor: ClassDescriptor): ObjCTypeForKotlinType = classToType.getOrPut(descriptor) {
        konst methods = mutableListOf<ObjCMethodSpec>()

        // Note: contributedMethods includes fake overrides too.
        konst allBaseMethods = descriptor.contributedMethods.filter { mapper.shouldBeExposed(it) }
                .flatMap { mapper.getBaseMethods(it) }.distinct()

        methods += createObjCMethods(allBaseMethods)

        konst binaryName = namer.getClassOrProtocolName(descriptor).binaryName
        konst irClassSymbol = symbolTable.referenceClass(descriptor)

        if (descriptor.isInterface) {
            ObjCProtocolForKotlinInterface(binaryName, irClassSymbol, methods)
        } else {
            descriptor.constructors.filter { mapper.shouldBeExposed(it) }.mapTo(methods) {
                konst irConstructorSymbol = symbolTable.referenceConstructor(it)
                konst baseMethod = createObjCMethodSpecBaseMethod(mapper, namer, irConstructorSymbol, it)

                if (descriptor.isArray) {
                    ObjCFactoryMethodForKotlinArrayConstructor(baseMethod)
                } else {
                    ObjCInitMethodForKotlinConstructor(baseMethod)
                }
            }

            if (descriptor.kind == ClassKind.OBJECT) {
                methods += ObjCGetterForObjectInstance(namer.getObjectInstanceSelector(descriptor), irClassSymbol)
                methods += ObjCGetterForObjectInstance(namer.getObjectPropertySelector(descriptor), irClassSymbol)
            }

            if (descriptor.needCompanionObjectProperty(namer, mapper)) {
                methods += ObjCGetterForObjectInstance(namer.getCompanionObjectPropertySelector(descriptor),
                        symbolTable.referenceClass(descriptor.companionObjectDescriptor!!))
            }

            if (descriptor.kind == ClassKind.ENUM_CLASS) {
                descriptor.enumEntries.mapTo(methods) {
                    ObjCGetterForKotlinEnumEntry(symbolTable.referenceEnumEntry(it), namer.getEnumEntrySelector(it))
                }

                descriptor.getEnumValuesFunctionDescriptor()?.let {
                    methods += ObjCClassMethodForKotlinEnumValuesOrEntries(
                            symbolTable.referenceSimpleFunction(it),
                            namer.getEnumStaticMemberSelector(it)
                    )
                }
                descriptor.getEnumEntriesPropertyDescriptor()?.let {
                    methods += ObjCClassMethodForKotlinEnumValuesOrEntries(
                            symbolTable.referenceSimpleFunction(it.getter!!),
                            namer.getEnumStaticMemberSelector(it)
                    )
                }
            }

            if (KotlinBuiltIns.isThrowable(descriptor)) {
                methods += ObjCKotlinThrowableAsErrorMethod
            }

            konst categoryMethods = categoryMembers[descriptor].orEmpty().toObjCMethods()

            konst superClassNotAny = descriptor.getSuperClassNotAny()
                    ?.let { getType(it) as ObjCClassForKotlinClass }

            ObjCClassForKotlinClass(binaryName, irClassSymbol, methods, categoryMethods, superClassNotAny)
        }
    }

    konst types = generatedClasses.map { getType(it) }

    return ObjCExportCodeSpec(files, types)
}

internal fun <S : IrFunctionSymbol> createObjCMethodSpecBaseMethod(
        mapper: ObjCExportMapper,
        namer: ObjCExportNamer,
        symbol: S,
        descriptor: FunctionDescriptor
): ObjCMethodSpec.BaseMethod<S> {
    require(mapper.isBaseMethod(descriptor))

    konst selector = namer.getSelector(descriptor)
    konst bridge = mapper.bridgeMethod(descriptor)

    return ObjCMethodSpec.BaseMethod(symbol, bridge, selector)
}

internal class ObjCExportCodeSpec(
        konst files: List<ObjCClassForKotlinFile>,
        konst types: List<ObjCTypeForKotlinType>
)

internal sealed class ObjCMethodSpec {
    /**
     * Aggregates base method (as defined by [ObjCExportMapper.isBaseMethod])
     * and details required to generate code for bridges between Kotlin and Obj-C methods.
     */
    data class BaseMethod<out S : IrFunctionSymbol>(konst symbol: S, konst bridge: MethodBridge, konst selector: String)
}

internal class ObjCMethodForKotlinMethod(konst baseMethod: BaseMethod<IrSimpleFunctionSymbol>) : ObjCMethodSpec() {
    override fun toString(): String =
            "ObjC spec of method `${baseMethod.selector}` for `${baseMethod.symbol}`"
}

internal class ObjCInitMethodForKotlinConstructor(konst baseMethod: BaseMethod<IrConstructorSymbol>) : ObjCMethodSpec() {
    override fun toString(): String =
            "ObjC spec of method `${baseMethod.selector}` for `${baseMethod.symbol}`"
}

internal class ObjCFactoryMethodForKotlinArrayConstructor(
        konst baseMethod: BaseMethod<IrConstructorSymbol>
) : ObjCMethodSpec() {
    override fun toString(): String =
            "ObjC spec of factory ${baseMethod.selector} for ${baseMethod.symbol}"
}

internal class ObjCGetterForKotlinEnumEntry(
        konst irEnumEntrySymbol: IrEnumEntrySymbol,
        konst selector: String
) : ObjCMethodSpec() {
    override fun toString(): String =
            "ObjC spec of getter `$selector` for `$irEnumEntrySymbol`"
}

internal class ObjCClassMethodForKotlinEnumValuesOrEntries(
        konst konstuesFunctionSymbol: IrFunctionSymbol,
        konst selector: String
) : ObjCMethodSpec() {
    override fun toString(): String =
            "ObjC spec of method `$selector` for $konstuesFunctionSymbol"
}

internal class ObjCGetterForObjectInstance(konst selector: String, konst classSymbol: IrClassSymbol) : ObjCMethodSpec() {
    override fun toString(): String =
            "ObjC spec of instance getter `$selector` for $classSymbol"
}

internal object ObjCKotlinThrowableAsErrorMethod : ObjCMethodSpec() {
    override fun toString(): String =
            "ObjC spec for ThrowableAsError method"
}

internal sealed class ObjCTypeSpec(konst binaryName: String)

internal sealed class ObjCTypeForKotlinType(
        binaryName: String,
        konst irClassSymbol: IrClassSymbol,
        konst methods: List<ObjCMethodSpec>
) : ObjCTypeSpec(binaryName)

internal class ObjCClassForKotlinClass(
        binaryName: String,
        irClassSymbol: IrClassSymbol,
        methods: List<ObjCMethodSpec>,
        konst categoryMethods: List<ObjCMethodForKotlinMethod>,
        konst superClassNotAny: ObjCClassForKotlinClass?
) : ObjCTypeForKotlinType(binaryName, irClassSymbol, methods) {
    override fun toString(): String =
            "ObjC spec of class `$binaryName` for `$irClassSymbol`"
}

internal class ObjCProtocolForKotlinInterface(
        binaryName: String,
        irClassSymbol: IrClassSymbol,
        methods: List<ObjCMethodSpec>
) : ObjCTypeForKotlinType(binaryName, irClassSymbol, methods) {

    override fun toString(): String =
            "ObjC spec of protocol `$binaryName` for `$irClassSymbol`"
}

internal class ObjCClassForKotlinFile(
        binaryName: String,
        private konst sourceFile: SourceFile,
        konst methods: List<ObjCMethodForKotlinMethod>
) : ObjCTypeSpec(binaryName) {
    override fun toString(): String =
            "ObjC spec of class `$binaryName` for `${sourceFile.name}`"
}