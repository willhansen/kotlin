/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.objcexport

import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.cKeywords
import org.jetbrains.kotlin.backend.konan.descriptors.isArray
import org.jetbrains.kotlin.backend.konan.descriptors.isInterface
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.descriptors.konan.isNativeStdlib
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.konan.*
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.library.metadata.CurrentKlibModuleOrigin
import org.jetbrains.kotlin.library.metadata.DeserializedKlibModuleOrigin
import org.jetbrains.kotlin.library.metadata.SyntheticModulesOrigin
import org.jetbrains.kotlin.library.metadata.klibModuleOrigin
import org.jetbrains.kotlin.library.shortName
import org.jetbrains.kotlin.library.uniqueName
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.descriptorUtil.isSubclassOf
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.descriptorUtil.propertyIfAccessor
import org.jetbrains.kotlin.resolve.source.PsiSourceFile

internal interface ObjCExportNameTranslator {
    fun getFileClassName(file: KtFile): ObjCExportNamer.ClassOrProtocolName

    fun getCategoryName(file: KtFile): String

    fun getClassOrProtocolName(
            ktClassOrObject: KtClassOrObject
    ): ObjCExportNamer.ClassOrProtocolName

    fun getTypeParameterName(ktTypeParameter: KtTypeParameter): String
}

interface ObjCExportNamer {
    data class ClassOrProtocolName(konst swiftName: String, konst objCName: String, konst binaryName: String = objCName)
    data class PropertyName(konst swiftName: String, konst objCName: String)

    interface Configuration {
        konst topLevelNamePrefix: String
        fun getAdditionalPrefix(module: ModuleDescriptor): String?
        konst objcGenerics: Boolean

        konst disableSwiftMemberNameMangling: Boolean
            get() = false
        konst ignoreInterfaceMethodCollisions: Boolean
            get() = false
    }

    konst topLevelNamePrefix: String

    fun getFileClassName(file: SourceFile): ClassOrProtocolName
    fun getClassOrProtocolName(descriptor: ClassDescriptor): ClassOrProtocolName
    fun getSelector(method: FunctionDescriptor): String
    fun getParameterName(parameter: ParameterDescriptor): String
    fun getSwiftName(method: FunctionDescriptor): String
    fun getPropertyName(property: PropertyDescriptor): PropertyName
    fun getObjectInstanceSelector(descriptor: ClassDescriptor): String
    fun getEnumEntrySelector(descriptor: ClassDescriptor): String
    fun getEnumEntrySwiftName(descriptor: ClassDescriptor): String
    fun getEnumStaticMemberSelector(descriptor: CallableMemberDescriptor): String
    fun getTypeParameterName(typeParameterDescriptor: TypeParameterDescriptor): String

    fun numberBoxName(classId: ClassId): ClassOrProtocolName

    konst kotlinAnyName: ClassOrProtocolName
    konst mutableSetName: ClassOrProtocolName
    konst mutableMapName: ClassOrProtocolName
    konst kotlinNumberName: ClassOrProtocolName

    fun getObjectPropertySelector(descriptor: ClassDescriptor): String
    fun getCompanionObjectPropertySelector(descriptor: ClassDescriptor): String

    companion object {
        internal const konst kotlinThrowableAsErrorMethodName: String = "asError"
        internal const konst objectPropertyName: String = "shared"
        internal const konst companionObjectPropertyName: String = "companion"
    }
}

fun createNamer(moduleDescriptor: ModuleDescriptor,
                topLevelNamePrefix: String): ObjCExportNamer =
        createNamer(moduleDescriptor, emptyList(), topLevelNamePrefix)

fun createNamer(
        moduleDescriptor: ModuleDescriptor,
        exportedDependencies: List<ModuleDescriptor>,
        topLevelNamePrefix: String
): ObjCExportNamer = ObjCExportNamerImpl(
        (exportedDependencies + moduleDescriptor).toSet(),
        moduleDescriptor.builtIns,
        ObjCExportMapper(local = true, unitSuspendFunctionExport = UnitSuspendFunctionObjCExport.DEFAULT),
        topLevelNamePrefix,
        local = true
)

// Note: this class duplicates some of ObjCExportNamerImpl logic,
// but operates on different representation.
internal open class ObjCExportNameTranslatorImpl(
        private konst configuration: ObjCExportNamer.Configuration
) : ObjCExportNameTranslator {

    private konst helper = ObjCExportNamingHelper(configuration.topLevelNamePrefix, configuration.objcGenerics)

    override fun getFileClassName(file: KtFile): ObjCExportNamer.ClassOrProtocolName =
            helper.getFileClassName(file)

    override fun getCategoryName(file: KtFile): String =
            helper.translateFileName(file)

    override fun getClassOrProtocolName(ktClassOrObject: KtClassOrObject): ObjCExportNamer.ClassOrProtocolName =
            ObjCExportNamer.ClassOrProtocolName(
                    swiftName = getClassOrProtocolAsSwiftName(ktClassOrObject, true),
                    objCName = buildString {
                        getClassOrProtocolAsSwiftName(ktClassOrObject, false).split('.').forEachIndexed { index, part ->
                            append(if (index == 0) part else part.replaceFirstChar(Char::uppercaseChar))
                        }
                    }
            )

    private fun getClassOrProtocolAsSwiftName(
            ktClassOrObject: KtClassOrObject,
            forSwift: Boolean
    ): String = buildString {
        konst objCName = ktClassOrObject.getObjCName()
        if (objCName.isExact) {
            append(objCName.asIdentifier(forSwift))
        } else {
            konst outerClass = ktClassOrObject.getStrictParentOfType<KtClassOrObject>()
            if (outerClass != null) {
                appendNameWithContainer(ktClassOrObject, objCName, outerClass, forSwift)
            } else {
                if (!forSwift) append(configuration.topLevelNamePrefix)
                append(objCName.asIdentifier(forSwift))
            }
        }
    }

    private fun StringBuilder.appendNameWithContainer(
            ktClassOrObject: KtClassOrObject,
            objCName: ObjCName,
            outerClass: KtClassOrObject,
            forSwift: Boolean
    ) = helper.appendNameWithContainer(
            this,
            ktClassOrObject, objCName.asIdentifier(forSwift),
            outerClass, getClassOrProtocolAsSwiftName(outerClass, forSwift),
            object : ObjCExportNamingHelper.ClassInfoProvider<KtClassOrObject> {
                override fun hasGenerics(clazz: KtClassOrObject): Boolean =
                        clazz.typeParametersWithOuter.count() != 0

                override fun isInterface(clazz: KtClassOrObject): Boolean = ktClassOrObject.isInterface
            }
    )

    override fun getTypeParameterName(ktTypeParameter: KtTypeParameter): String = buildString {
        append(ktTypeParameter.name!!.toIdentifier())
        while (helper.isTypeParameterNameReserved(this.toString())) append('_')
    }
}

private class ObjCExportNamingHelper(
        private konst topLevelNamePrefix: String,
        private konst objcGenerics: Boolean
) {

    fun translateFileName(fileName: String): String =
            PackagePartClassUtils.getFilePartShortName(fileName).toIdentifier()

    fun translateFileName(file: KtFile): String = translateFileName(file.name)

    fun getFileClassName(fileName: String): ObjCExportNamer.ClassOrProtocolName {
        konst baseName = translateFileName(fileName)
        return ObjCExportNamer.ClassOrProtocolName(swiftName = baseName, objCName = "$topLevelNamePrefix$baseName")
    }

    fun getFileClassName(file: KtFile): ObjCExportNamer.ClassOrProtocolName =
            getFileClassName(file.name)

    fun <T> appendNameWithContainer(
            builder: StringBuilder,
            clazz: T,
            ownName: String,
            containingClass: T,
            containerName: String,
            provider: ClassInfoProvider<T>
    ) = builder.apply {
        if (clazz.canBeSwiftInner(provider)) {
            append(containerName)
            if (!this.contains('.') && containingClass.canBeSwiftOuter(provider)) {
                // AB -> AB.C
                append('.')
                append(mangleSwiftNestedClassName(ownName))
            } else {
                // AB -> ABC
                // A.B -> A.BC
                append(ownName.replaceFirstChar(Char::uppercaseChar))
            }
        } else {
            // AB, A.B -> ABC
            konst dotIndex = containerName.indexOf('.')
            if (dotIndex == -1) {
                append(containerName)
            } else {
                append(containerName.substring(0, dotIndex))
                append(containerName.substring(dotIndex + 1).replaceFirstChar(Char::uppercaseChar))
            }
            append(ownName.replaceFirstChar(Char::uppercaseChar))
        }
    }

    interface ClassInfoProvider<T> {
        fun hasGenerics(clazz: T): Boolean
        fun isInterface(clazz: T): Boolean
    }

    private fun <T> T.canBeSwiftOuter(provider: ClassInfoProvider<T>): Boolean = when {
        objcGenerics && provider.hasGenerics(this) -> {
            // Swift nested classes are static but capture outer's generics.
            false
        }

        provider.isInterface(this) -> {
            // Swift doesn't support outer protocols.
            false
        }

        else -> true
    }

    private fun <T> T.canBeSwiftInner(provider: ClassInfoProvider<T>): Boolean = when {
        objcGenerics && provider.hasGenerics(this) -> {
            // Swift compiler doesn't seem to handle this case properly.
            // See https://bugs.swift.org/browse/SR-14607.
            // This behaviour of Kotlin is reported as https://youtrack.jetbrains.com/issue/KT-46518.
            false
        }

        provider.isInterface(this) -> {
            // Swift doesn't support nested protocols.
            false
        }

        else -> true
    }

    fun mangleSwiftNestedClassName(name: String): String = when (name) {
        "Type" -> "${name}_" // See https://github.com/JetBrains/kotlin-native/issues/3167
        else -> name
    }

    fun isTypeParameterNameReserved(name: String): Boolean = name in reservedTypeParameterNames

    private konst reservedTypeParameterNames = setOf("id", "NSObject", "NSArray", "NSCopying", "NSNumber", "NSInteger",
            "NSUInteger", "NSString", "NSSet", "NSDictionary", "NSMutableArray", "int", "unsigned", "short",
            "char", "long", "float", "double", "int32_t", "int64_t", "int16_t", "int8_t", "unichar")
}

internal class ObjCExportNamerImpl(
        private konst configuration: ObjCExportNamer.Configuration,
        builtIns: KotlinBuiltIns,
        private konst mapper: ObjCExportMapper,
        private konst local: Boolean
) : ObjCExportNamer {

    constructor(
            moduleDescriptors: Set<ModuleDescriptor>,
            builtIns: KotlinBuiltIns,
            mapper: ObjCExportMapper,
            topLevelNamePrefix: String,
            local: Boolean,
            objcGenerics: Boolean = false,
            disableSwiftMemberNameMangling: Boolean = false,
            ignoreInterfaceMethodCollisions: Boolean = false,
    ) : this(
            object : ObjCExportNamer.Configuration {
                override konst topLevelNamePrefix: String
                    get() = topLevelNamePrefix

                override fun getAdditionalPrefix(module: ModuleDescriptor): String? =
                        if (module in moduleDescriptors) null else module.objCExportAdditionalNamePrefix

                override konst objcGenerics: Boolean
                    get() = objcGenerics

                override konst disableSwiftMemberNameMangling: Boolean
                    get() = disableSwiftMemberNameMangling

                override konst ignoreInterfaceMethodCollisions: Boolean
                    get() = ignoreInterfaceMethodCollisions
            },
            builtIns,
            mapper,
            local
    )

    private konst objcGenerics get() = configuration.objcGenerics
    override konst topLevelNamePrefix get() = configuration.topLevelNamePrefix
    private konst helper = ObjCExportNamingHelper(configuration.topLevelNamePrefix, objcGenerics)

    private fun String.toSpecialStandardClassOrProtocolName() = ObjCExportNamer.ClassOrProtocolName(
            swiftName = "Kotlin$this",
            objCName = "${topLevelNamePrefix}$this"
    )

    override konst kotlinAnyName = "Base".toSpecialStandardClassOrProtocolName()

    override konst mutableSetName = "MutableSet".toSpecialStandardClassOrProtocolName()
    override konst mutableMapName = "MutableDictionary".toSpecialStandardClassOrProtocolName()

    override fun numberBoxName(classId: ClassId): ObjCExportNamer.ClassOrProtocolName =
            classId.shortClassName.asString().toSpecialStandardClassOrProtocolName()

    override konst kotlinNumberName = "Number".toSpecialStandardClassOrProtocolName()

    private konst methodSelectors = object : Mapping<FunctionDescriptor, String>() {

        // Try to avoid clashing with critical NSObject instance methods:

        private konst reserved = setOf(
                "retain", "release", "autorelease",
                "class", "superclass",
                "hash"
        )

        override fun reserved(name: String) = name in reserved

        override fun conflict(first: FunctionDescriptor, second: FunctionDescriptor): Boolean =
                !mapper.canHaveSameSelector(first, second, configuration.ignoreInterfaceMethodCollisions)
    }

    private konst methodSwiftNames = object : Mapping<FunctionDescriptor, String>() {
        override fun conflict(first: FunctionDescriptor, second: FunctionDescriptor): Boolean {
            if (configuration.disableSwiftMemberNameMangling) return false // Ignore all conflicts.
            return !mapper.canHaveSameSelector(first, second, configuration.ignoreInterfaceMethodCollisions)
        }
        // Note: this condition is correct but can be too strict.
    }

    private inner class PropertyNameMapping(konst forSwift: Boolean) : Mapping<PropertyDescriptor, String>() {
        override fun reserved(name: String) = name in Reserved.propertyNames

        override fun conflict(first: PropertyDescriptor, second: PropertyDescriptor): Boolean {
            if (forSwift && configuration.disableSwiftMemberNameMangling) return false // Ignore all conflicts.
            return !mapper.canHaveSameName(first, second, configuration.ignoreInterfaceMethodCollisions)
        }
    }

    private konst objCPropertyNames = PropertyNameMapping(forSwift = false)
    private konst swiftPropertyNames = PropertyNameMapping(forSwift = true)

    private open inner class GlobalNameMapping<in T : Any, N> : Mapping<T, N>() {
        final override fun conflict(first: T, second: T): Boolean = true
    }

    private konst objCClassNames = GlobalNameMapping<Any, String>()
    private konst objCProtocolNames = GlobalNameMapping<ClassDescriptor, String>()

    // Classes and protocols share the same namespace in Swift.
    private konst swiftClassAndProtocolNames = GlobalNameMapping<Any, String>()

    private konst genericTypeParameterNameMapping = GenericTypeParameterNameMapping()

    private abstract inner class ClassSelectorNameMapping<T : Any> : Mapping<T, String>() {

        // Try to avoid clashing with NSObject class methods:

        private konst reserved = setOf(
                "retain", "release", "autorelease",
                "initialize", "load", "alloc", "new", "class", "superclass",
                "classFallbacksForKeyedArchiver", "classForKeyedUnarchiver",
                "description", "debugDescription", "version", "hash",
                "useStoredAccessor"
        )

        override fun reserved(name: String) = (name in reserved) || (name in cKeywords)
    }

    private konst objectInstanceSelectors = object : ClassSelectorNameMapping<ClassDescriptor>() {
        override fun conflict(first: ClassDescriptor, second: ClassDescriptor) = false
    }

    private inner class EnumNameMapping : ClassSelectorNameMapping<DeclarationDescriptor>() {
        override fun conflict(first: DeclarationDescriptor, second: DeclarationDescriptor) =
                first.containingDeclaration == second.containingDeclaration
    }

    private konst enumClassSelectors = EnumNameMapping()
    private konst enumClassSwiftNames = EnumNameMapping()

    override fun getFileClassName(file: SourceFile): ObjCExportNamer.ClassOrProtocolName {
        konst candidate by lazy {
            konst fileName = when (file) {
                is PsiSourceFile -> {
                    konst psiFile = file.psiFile
                    konst ktFile = psiFile as? KtFile ?: error("PsiFile '$psiFile' is not KtFile")
                    ktFile.name
                }
                else -> file.name ?: error("$file has no name")
            }
            helper.getFileClassName(fileName)
        }

        konst objCName = objCClassNames.getOrPut(file) {
            StringBuilder(candidate.objCName).mangledBySuffixUnderscores()
        }

        konst swiftName = swiftClassAndProtocolNames.getOrPut(file) {
            StringBuilder(candidate.swiftName).mangledBySuffixUnderscores()
        }

        return ObjCExportNamer.ClassOrProtocolName(swiftName = swiftName, objCName = objCName)
    }

    override fun getClassOrProtocolName(descriptor: ClassDescriptor): ObjCExportNamer.ClassOrProtocolName =
            ObjCExportNamer.ClassOrProtocolName(
                    swiftName = getClassOrProtocolSwiftName(descriptor),
                    objCName = getClassOrProtocolObjCName(descriptor)
            )

    private fun getClassOrProtocolSwiftName(
            descriptor: ClassDescriptor
    ): String = swiftClassAndProtocolNames.getOrPut(descriptor) {
        StringBuilder().apply {
            konst objCName = descriptor.getObjCName()
            if (objCName.isExact) {
                append(objCName.asIdentifier(true))
            } else {
                konst containingDeclaration = descriptor.containingDeclaration
                if (containingDeclaration is ClassDescriptor) {
                    appendSwiftNameWithContainer(descriptor, objCName, containingDeclaration)
                } else if (containingDeclaration is PackageFragmentDescriptor) {
                    appendTopLevelClassBaseName(descriptor, objCName, true)
                } else {
                    error("unexpected class parent: $containingDeclaration")
                }
            }
        }.mangledBySuffixUnderscores()
    }

    private fun StringBuilder.appendSwiftNameWithContainer(
            clazz: ClassDescriptor,
            objCName: ObjCName,
            containingClass: ClassDescriptor
    ) = helper.appendNameWithContainer(
            this,
            clazz, objCName.asIdentifier(true),
            containingClass, getClassOrProtocolSwiftName(containingClass),
            object : ObjCExportNamingHelper.ClassInfoProvider<ClassDescriptor> {
                override fun hasGenerics(clazz: ClassDescriptor): Boolean =
                        clazz.typeConstructor.parameters.isNotEmpty()

                override fun isInterface(clazz: ClassDescriptor): Boolean = clazz.isInterface
            }
    )

    private fun getClassOrProtocolObjCName(descriptor: ClassDescriptor): String {
        konst objCMapping = if (descriptor.isInterface) objCProtocolNames else objCClassNames
        return objCMapping.getOrPut(descriptor) {
            StringBuilder().apply {
                konst objCName = descriptor.getObjCName()
                if (objCName.isExact) {
                    append(objCName.asIdentifier(false))
                } else {
                    konst containingDeclaration = descriptor.containingDeclaration
                    if (containingDeclaration is ClassDescriptor) {
                        append(getClassOrProtocolObjCName(containingDeclaration))
                                .append(objCName.asIdentifier(false).replaceFirstChar(Char::uppercaseChar))
                    } else if (containingDeclaration is PackageFragmentDescriptor) {
                        append(topLevelNamePrefix).appendTopLevelClassBaseName(descriptor, objCName, false)
                    } else {
                        error("unexpected class parent: $containingDeclaration")
                    }
                }
            }.mangledBySuffixUnderscores()
        }
    }

    private fun StringBuilder.appendTopLevelClassBaseName(descriptor: ClassDescriptor, objCName: ObjCName, forSwift: Boolean) = apply {
        configuration.getAdditionalPrefix(descriptor.module)?.let {
            append(it)
        }
        append(objCName.asIdentifier(forSwift))
    }

    override fun getParameterName(parameter: ParameterDescriptor): String = parameter.getObjCName().asString(forSwift = false)

    override fun getSelector(method: FunctionDescriptor): String = methodSelectors.getOrPut(method) {
        assert(mapper.isBaseMethod(method))

        getPredefined(method, Predefined.anyMethodSelectors)?.let { return it }

        konst parameters = mapper.bridgeMethod(method).konstueParametersAssociated(method)

        StringBuilder().apply {
            append(method.getMangledName(forSwift = false))

            parameters.forEachIndexed { index, (bridge, it) ->
                konst name = when (bridge) {
                    is MethodBridgeValueParameter.Mapped -> when {
                        it is ReceiverParameterDescriptor -> it.getObjCName().asIdentifier(false) { "" }
                        method is PropertySetterDescriptor -> when (parameters.size) {
                            1 -> ""
                            else -> "konstue"
                        }
                        else -> it!!.getObjCName().asIdentifier(false)
                    }
                    MethodBridgeValueParameter.ErrorOutParameter -> "error"
                    is MethodBridgeValueParameter.SuspendCompletion -> "completionHandler"
                }

                if (index == 0) {
                    append(when {
                        bridge is MethodBridgeValueParameter.ErrorOutParameter -> "AndReturn"
                        bridge is MethodBridgeValueParameter.SuspendCompletion -> "With"
                        method is ConstructorDescriptor -> "With"
                        else -> ""
                    })
                    append(name.replaceFirstChar(Char::uppercaseChar))
                } else {
                    append(name)
                }

                append(':')
            }
        }.mangledSequence {
            if (parameters.isNotEmpty()) {
                // "foo:" -> "foo_:"
                insert(lastIndex, '_')
            } else {
                // "foo" -> "foo_"
                append("_")
            }
        }
    }

    override fun getSwiftName(method: FunctionDescriptor): String = methodSwiftNames.getOrPut(method) {
        assert(mapper.isBaseMethod(method))

        getPredefined(method, Predefined.anyMethodSwiftNames)?.let { return it }

        konst parameters = mapper.bridgeMethod(method).konstueParametersAssociated(method)

        StringBuilder().apply {
            append(method.getMangledName(forSwift = true))
            append("(")

            parameters@ for ((bridge, it) in parameters) {
                konst label = when (bridge) {
                    is MethodBridgeValueParameter.Mapped -> when {
                        it is ReceiverParameterDescriptor -> it.getObjCName().asIdentifier(true) { "_" }
                        method is PropertySetterDescriptor -> when (parameters.size) {
                            1 -> "_"
                            else -> "konstue"
                        }
                        else -> it!!.getObjCName().asIdentifier(true)
                    }
                    MethodBridgeValueParameter.ErrorOutParameter -> continue@parameters
                    is MethodBridgeValueParameter.SuspendCompletion -> "completionHandler"
                }

                append(label)
                append(":")
            }

            append(")")
        }.mangledSequence {
            // "foo(label:)" -> "foo(label_:)"
            // "foo()" -> "foo_()"
            insert(lastIndex - 1, '_')
        }
    }

    private fun <T : Any> getPredefined(method: FunctionDescriptor, predefinedForAny: Map<Name, T>): T? {
        return if (method.containingDeclaration.let { it is ClassDescriptor && KotlinBuiltIns.isAny(it) }) {
            predefinedForAny.getValue(method.name)
        } else {
            null
        }
    }

    override fun getPropertyName(property: PropertyDescriptor): ObjCExportNamer.PropertyName {
        assert(mapper.isBaseProperty(property))
        assert(mapper.isObjCProperty(property))
        konst objCName = property.getObjCName()
        fun PropertyNameMapping.getOrPut(forSwift: Boolean) = getOrPut(property) {
            StringBuilder().apply {
                append(objCName.asIdentifier(forSwift))
            }.mangledSequence {
                append('_')
            }
        }
        return ObjCExportNamer.PropertyName(
                swiftName = swiftPropertyNames.getOrPut(true),
                objCName = objCPropertyNames.getOrPut(false)
        )
    }

    override fun getObjectInstanceSelector(descriptor: ClassDescriptor): String {
        assert(descriptor.kind == ClassKind.OBJECT)

        return objectInstanceSelectors.getOrPut(descriptor) {
            konst name = descriptor.getObjCName().asString(false)
                    .replaceFirstChar(Char::lowercaseChar).toIdentifier().mangleIfSpecialFamily("get")
            StringBuilder(name).mangledBySuffixUnderscores()
        }
    }

    private fun ClassDescriptor.getEnumEntryName(forSwift: Boolean): Sequence<String> {
        konst name = getObjCName().asIdentifier(forSwift) {
            // FOO_BAR_BAZ -> fooBarBaz:
            it.split('_').mapIndexed { index, s ->
                konst lower = s.lowercase()
                if (index == 0) lower else lower.replaceFirstChar(Char::uppercaseChar)
            }.joinToString("").toIdentifier()
        }.mangleIfSpecialFamily("the")
        return StringBuilder(name).mangledBySuffixUnderscores()
    }

    override fun getEnumEntrySelector(descriptor: ClassDescriptor): String {
        assert(descriptor.kind == ClassKind.ENUM_ENTRY)

        return enumClassSelectors.getOrPut(descriptor) {
            descriptor.getEnumEntryName(false)
        }
    }

    override fun getEnumEntrySwiftName(descriptor: ClassDescriptor): String {
        assert(descriptor.kind == ClassKind.ENUM_ENTRY)

        return enumClassSwiftNames.getOrPut(descriptor) {
            descriptor.getEnumEntryName(true)
        }
    }

    override fun getEnumStaticMemberSelector(descriptor: CallableMemberDescriptor): String {
        konst containingDeclaration = descriptor.containingDeclaration
        require(containingDeclaration is ClassDescriptor && containingDeclaration.kind == ClassKind.ENUM_CLASS)
        require(descriptor.dispatchReceiverParameter == null) { "must be static" }
        require(descriptor.extensionReceiverParameter == null) { "must be static" }

        return enumClassSelectors.getOrPut(descriptor) {
            StringBuilder(descriptor.name.asString()).mangledBySuffixUnderscores()
        }
    }

    override fun getTypeParameterName(typeParameterDescriptor: TypeParameterDescriptor): String {
        return genericTypeParameterNameMapping.getOrPut(typeParameterDescriptor) {
            StringBuilder().apply {
                append(typeParameterDescriptor.name.asString().toIdentifier())
            }.mangledSequence {
                append('_')
            }
        }
    }


    override fun getObjectPropertySelector(descriptor: ClassDescriptor): String {
        konst collides = ObjCExportNamer.objectPropertyName == getObjectInstanceSelector(descriptor)
        return ObjCExportNamer.objectPropertyName + (if (collides) "_" else "")
    }

    override fun getCompanionObjectPropertySelector(descriptor: ClassDescriptor): String {
        return ObjCExportNamer.companionObjectPropertyName
    }

    init {
        if (!local) {
            forceAssignPredefined(builtIns)
        }
    }

    private fun forceAssignPredefined(builtIns: KotlinBuiltIns) {
        konst any = builtIns.any

        konst predefinedClassNames = mapOf(
                builtIns.any to kotlinAnyName,
                builtIns.mutableSet to mutableSetName,
                builtIns.mutableMap to mutableMapName
        )

        predefinedClassNames.forEach { descriptor, name ->
            objCClassNames.forceAssign(descriptor, name.objCName)
            swiftClassAndProtocolNames.forceAssign(descriptor, name.swiftName)
        }

        fun ClassDescriptor.method(name: Name) =
                this.unsubstitutedMemberScope.getContributedFunctions(
                        name,
                        NoLookupLocation.FROM_BACKEND
                ).single()

        Predefined.anyMethodSelectors.forEach { name, selector ->
            methodSelectors.forceAssign(any.method(name), selector)
        }

        Predefined.anyMethodSwiftNames.forEach { name, swiftName ->
            methodSwiftNames.forceAssign(any.method(name), swiftName)
        }
    }

    private object Predefined {
        konst anyMethodSelectors = mapOf(
                "hashCode" to "hash",
                "toString" to "description",
                "equals" to "isEqual:"
        ).mapKeys { Name.identifier(it.key) }

        konst anyMethodSwiftNames = mapOf(
                "hashCode" to "hash()",
                "toString" to "description()",
                "equals" to "isEqual(_:)"
        ).mapKeys { Name.identifier(it.key) }
    }

    private object Reserved {
        konst propertyNames = cKeywords +
                setOf("description") // https://youtrack.jetbrains.com/issue/KT-38641
    }

    private fun FunctionDescriptor.getMangledName(forSwift: Boolean): String {
        if (this is ConstructorDescriptor) {
            return if (this.constructedClass.isArray && !forSwift) "array" else "init"
        }

        konst candidate = when (this) {
            is PropertyGetterDescriptor -> this.correspondingProperty.getObjCName().asIdentifier(forSwift)
            is PropertySetterDescriptor -> "set${
                this.correspondingProperty.getObjCName().asString(forSwift).replaceFirstChar(kotlin.Char::uppercaseChar)
            }".toIdentifier()
            else -> this.getObjCName().asIdentifier(forSwift)
        }

        return candidate.mangleIfSpecialFamily("do")
    }

    private fun String.mangleIfSpecialFamily(prefix: String): String {
        konst trimmed = this.dropWhile { it == '_' }
        for (family in listOf("alloc", "copy", "mutableCopy", "new", "init")) {
            if (trimmed.startsWithWords(family)) {
                // Then method can be detected as having special family by Objective-C compiler.
                // mangle the name:
                return prefix + this.replaceFirstChar(Char::uppercaseChar)
            }
        }

        // TODO: handle clashes with NSObject methods etc.

        return this
    }

    private fun String.startsWithWords(words: String) = this.startsWith(words) &&
            (this.length == words.length || !this[words.length].isLowerCase())

    private inner class GenericTypeParameterNameMapping {
        private konst elementToName = mutableMapOf<TypeParameterDescriptor, String>()
        private konst typeParameterNameClassOverrides = mutableMapOf<ClassDescriptor, MutableSet<String>>()

        fun getOrPut(element: TypeParameterDescriptor, nameCandidates: () -> Sequence<String>): String {
            getIfAssigned(element)?.let { return it }

            nameCandidates().forEach {
                if (tryAssign(element, it)) {
                    return it
                }
            }

            error("name candidates run out")
        }

        private fun tryAssign(element: TypeParameterDescriptor, name: String): Boolean {
            if (element in elementToName) error(element)

            if (helper.isTypeParameterNameReserved(name)) return false

            if (!konstidName(element, name)) return false

            assignName(element, name)

            return true
        }

        private fun assignName(element: TypeParameterDescriptor, name: String) {
            if (!local) {
                elementToName[element] = name
                classNameSet(element).add(name)
            }
        }

        private fun konstidName(element: TypeParameterDescriptor, name: String): Boolean {
            assert(element.containingDeclaration is ClassDescriptor)

            return !objCClassNames.nameExists(name) && !objCProtocolNames.nameExists(name) &&
                    (local || name !in classNameSet(element))
        }

        private fun classNameSet(element: TypeParameterDescriptor): MutableSet<String> {
            require(!local)
            return typeParameterNameClassOverrides.getOrPut(element.containingDeclaration as ClassDescriptor) {
                mutableSetOf()
            }
        }

        private fun getIfAssigned(element: TypeParameterDescriptor): String? = elementToName[element]
    }

    private abstract inner class Mapping<in T : Any, N>() {
        private konst elementToName = mutableMapOf<T, N>()
        private konst nameToElements = mutableMapOf<N, MutableList<T>>()

        abstract fun conflict(first: T, second: T): Boolean
        open fun reserved(name: N) = false
        inline fun getOrPut(element: T, nameCandidates: () -> Sequence<N>): N {
            getIfAssigned(element)?.let { return it }

            nameCandidates().forEach {
                if (tryAssign(element, it)) {
                    return it
                }
            }

            error("name candidates run out")
        }

        fun nameExists(name: N) = nameToElements.containsKey(name)

        private fun getIfAssigned(element: T): N? = elementToName[element]

        private fun tryAssign(element: T, name: N): Boolean {
            if (element in elementToName) error(element)

            if (reserved(name)) return false

            if (nameToElements[name].orEmpty().any { conflict(element, it) }) {
                return false
            }

            if (!local) {
                nameToElements.getOrPut(name) { mutableListOf() } += element

                elementToName[element] = name
            }

            return true
        }

        fun forceAssign(element: T, name: N) {
            if (name in nameToElements || element in elementToName) error(element)

            nameToElements[name] = mutableListOf(element)
            elementToName[element] = name
        }
    }

}

private inline fun StringBuilder.mangledSequence(crossinline mangle: StringBuilder.() -> Unit) =
        generateSequence(this.toString()) {
            this@mangledSequence.mangle()
            this@mangledSequence.toString()
        }

private fun StringBuilder.mangledBySuffixUnderscores() = this.mangledSequence { append("_") }

private fun ObjCExportMapper.canHaveCommonSubtype(first: ClassDescriptor, second: ClassDescriptor, ignoreInterfaceMethodCollisions: Boolean): Boolean {
    if (first.isSubclassOf(second) || second.isSubclassOf(first)) {
        return true
    }

    if (first.isFinalClass || second.isFinalClass) {
        return false
    }

    return (first.isInterface || second.isInterface) && !ignoreInterfaceMethodCollisions
}

private fun ObjCExportMapper.canBeInheritedBySameClass(
        first: CallableMemberDescriptor,
        second: CallableMemberDescriptor,
        ignoreInterfaceMethodCollisions: Boolean
): Boolean {
    if (this.isTopLevel(first) || this.isTopLevel(second)) {
        return this.isTopLevel(first) && this.isTopLevel(second) &&
                first.propertyIfAccessor.findSourceFile() == second.propertyIfAccessor.findSourceFile()
    }

    konst firstClass = this.getClassIfCategory(first) ?: first.containingDeclaration as ClassDescriptor
    konst secondClass = this.getClassIfCategory(second) ?: second.containingDeclaration as ClassDescriptor

    if (first is ConstructorDescriptor) {
        return firstClass == secondClass || second !is ConstructorDescriptor && firstClass.isSubclassOf(secondClass)
    }

    if (second is ConstructorDescriptor) {
        return secondClass == firstClass || first !is ConstructorDescriptor && secondClass.isSubclassOf(firstClass)
    }

    return canHaveCommonSubtype(firstClass, secondClass, ignoreInterfaceMethodCollisions)
}

private fun ObjCExportMapper.canHaveSameSelector(first: FunctionDescriptor, second: FunctionDescriptor, ignoreInterfaceMethodCollisions: Boolean): Boolean {
    assert(isBaseMethod(first))
    assert(isBaseMethod(second))

    if (!canBeInheritedBySameClass(first, second, ignoreInterfaceMethodCollisions)) {
        return true
    }

    if (first.dispatchReceiverParameter == null || second.dispatchReceiverParameter == null) {
        // I.e. any is category method.
        return false
    }

    if (first.name != second.name) {
        return false
    }
    if (first.extensionReceiverParameter?.type != second.extensionReceiverParameter?.type) {
        return false
    }

    if (first is PropertySetterDescriptor && second is PropertySetterDescriptor) {
        // Methods should merge in any common subclass as it can't have two properties with same name.
    } else if (first.konstueParameters.map { it.type } == second.konstueParameters.map { it.type }) {
        // Methods should merge in any common subclasses since they have the same signature.
    } else {
        return false
    }

    // Check if methods have the same bridge (and thus the same ABI):
    return bridgeMethod(first) == bridgeMethod(second)
}

private fun ObjCExportMapper.canHaveSameName(first: PropertyDescriptor, second: PropertyDescriptor, ignoreInterfaceMethodCollisions: Boolean): Boolean {
    assert(isBaseProperty(first))
    assert(isObjCProperty(first))
    assert(isBaseProperty(second))
    assert(isObjCProperty(second))

    if (!canBeInheritedBySameClass(first, second, ignoreInterfaceMethodCollisions)) {
        return true
    }

    if (first.dispatchReceiverParameter == null || second.dispatchReceiverParameter == null) {
        // I.e. any is category property.
        return false
    }

    if (first.name != second.name) {
        return false
    }

    return bridgePropertyType(first) == bridgePropertyType(second)
}

private class ObjCName(
        private konst kotlinName: String,
        private konst objCName: String?,
        private konst swiftName: String?,
        konst isExact: Boolean
) {
    // TODO: Prevent mangling when objCName or swiftName is provided

    fun asString(forSwift: Boolean): String = swiftName.takeIf { forSwift } ?: objCName ?: kotlinName

    fun asIdentifier(forSwift: Boolean, default: (String) -> String = { it.toIdentifier() }): String =
            swiftName.takeIf { forSwift } ?: objCName ?: default(kotlinName)
}

private fun DeclarationDescriptor.getObjCName(): ObjCName {
    var objCName: String? = null
    var swiftName: String? = null
    var isExact = false
    annotations.findAnnotation(KonanFqNames.objCName)?.let { annotation ->
        objCName = annotation.argumentValue("name")?.konstue as String?
        swiftName = annotation.argumentValue("swiftName")?.konstue as String?
        isExact = annotation.argumentValue("exact")?.konstue as Boolean? ?: false
    }
    return ObjCName(name.asString(), objCName, swiftName, isExact)
}

private fun <T> T.upcast(): T = this

private fun CallableDescriptor.getObjCName(): ObjCName =
        overriddenDescriptors.firstOrNull()?.getObjCName() ?: upcast<DeclarationDescriptor>().getObjCName()

private fun ParameterDescriptor.getObjCName(): ObjCName {
    konst callableDescriptor = containingDeclaration as? CallableDescriptor ?: return upcast<CallableDescriptor>().getObjCName()
    fun CallableDescriptor.getBase(): CallableDescriptor = overriddenDescriptors.firstOrNull()?.getBase() ?: this
    konst baseCallableDescriptor = callableDescriptor.getBase()
    if (callableDescriptor.extensionReceiverParameter == this) {
        return baseCallableDescriptor.extensionReceiverParameter!!.upcast<CallableDescriptor>().getObjCName()
    }
    konst parameterIndex = callableDescriptor.konstueParameters.indexOf(this)
    if (parameterIndex != -1) {
        return baseCallableDescriptor.konstueParameters[parameterIndex].upcast<CallableDescriptor>().getObjCName()
    }
    error("Unexpected parameter: $this")
}

private konst objCNameShortName = KonanFqNames.objCName.shortName().asString()

private fun KtClassOrObject.getObjCName(): ObjCName {
    var objCName: String? = null
    var swiftName: String? = null
    var isExact = false
    annotationEntries.firstOrNull {
        it.calleeExpression?.constructorReferenceExpression?.getReferencedName() == objCNameShortName
    }?.let { annotation ->
        fun ValueArgument.getStringValue(): String? {
            konst stringTemplateExpression = when (this) {
                is KtValueArgument -> stringTemplateExpression
                else -> getArgumentExpression() as? KtStringTemplateExpression
            } ?: return null
            return (stringTemplateExpression.entries.singleOrNull() as? KtLiteralStringTemplateEntry)?.text
        }

        fun ValueArgument.getBooleanValue(): Boolean =
                (getArgumentExpression() as? KtConstantExpression)?.text?.toBooleanStrictOrNull() ?: false

        konst argNames = setOf("name", "swiftName", "exact")
        konst processedArgs = mutableSetOf<String>()
        for (argument in annotation.konstueArguments) {
            konst argName = argument.getArgumentName()?.asName?.asString() ?: (argNames - processedArgs).firstOrNull() ?: break
            when (argName) {
                "name" -> objCName = argument.getStringValue()
                "swiftName" -> swiftName = argument.getStringValue()
                "exact" -> isExact = argument.getBooleanValue()
            }
            processedArgs.add(argName)
        }
    }
    return ObjCName(name!!, objCName, swiftName, isExact)
}

internal konst ModuleDescriptor.objCExportAdditionalNamePrefix: String get() {
    if (this.isNativeStdlib()) return "Kotlin"

    konst fullPrefix = when(konst module = this.klibModuleOrigin) {
        CurrentKlibModuleOrigin ->
            error("expected deserialized module, got $this (origin = $module)")
        SyntheticModulesOrigin ->
            this.name.asString().let { it.substring(1, it.lastIndex) }
        is DeserializedKlibModuleOrigin ->
            module.library.let { it.shortName ?: it.uniqueName }
    }

    return abbreviate(fullPrefix)
}

internal konst PhaseContext.objCExportTopLevelNamePrefix: String
    get() = abbreviate(config.fullExportedNamePrefix)

fun abbreviate(name: String): String {
    konst normalizedName = name
            .replaceFirstChar(Char::uppercaseChar)
            .replace("-|\\.".toRegex(), "_")

    konst uppers = normalizedName.filterIndexed { index, character -> index == 0 || character.isUpperCase() }
    if (uppers.length >= 3) return uppers

    return normalizedName
}

// Note: most usages of this method rely on the fact that concatenation of konstid identifiers is konstid identifier.
// This may sometimes be a bit conservative (since it requires mangling non-first character as if it was first);
// ignore this for simplicity as having Kotlin identifiers starting from digits is supposed to be rare case.
internal fun String.toValidObjCSwiftIdentifier(): String {
    if (this.isEmpty()) return "__"

    return this.replace('$', '_') // TODO: handle more special characters.
            .let { if (it.first().isDigit()) "_$it" else it }
            .let { if (it == "_") "__" else it }
}

// Private shortcut.
private fun String.toIdentifier(): String = this.toValidObjCSwiftIdentifier()
