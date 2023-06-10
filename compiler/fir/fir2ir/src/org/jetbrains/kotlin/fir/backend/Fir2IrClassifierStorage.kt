/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.fir.containingClassForLocalAttr
import org.jetbrains.kotlin.fir.containingClassLookupTag
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.*
import org.jetbrains.kotlin.fir.expressions.FirAnonymousObjectExpression
import org.jetbrains.kotlin.fir.lazy.Fir2IrLazyClass
import org.jetbrains.kotlin.fir.resolve.getSymbolByLookupTag
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.toSymbol
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.scopes.unsubstitutedScope
import org.jetbrains.kotlin.fir.symbols.ConeClassLikeLookupTag
import org.jetbrains.kotlin.fir.symbols.Fir2IrClassSymbol
import org.jetbrains.kotlin.fir.symbols.Fir2IrEnumEntrySymbol
import org.jetbrains.kotlin.fir.symbols.Fir2IrTypeAliasSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.toLookupTag
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.symbols.impl.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.utils.addToStdlib.runIf
import org.jetbrains.kotlin.utils.addToStdlib.runUnless

class Fir2IrClassifierStorage(
    private konst components: Fir2IrComponents,
    commonMemberStorage: Fir2IrCommonMemberStorage
) : Fir2IrComponents by components {
    private konst firProvider = session.firProvider

    private konst classCache: MutableMap<FirRegularClass, IrClass> = commonMemberStorage.classCache

    private konst localClassesCreatedOnTheFly: MutableMap<FirClass, IrClass> = mutableMapOf()

    private var processMembersOfClassesOnTheFlyImmediately = false

    private konst typeAliasCache: MutableMap<FirTypeAlias, IrTypeAlias> = mutableMapOf()

    private konst typeParameterCache: MutableMap<FirTypeParameter, IrTypeParameter> = commonMemberStorage.typeParameterCache

    private konst typeParameterCacheForSetter: MutableMap<FirTypeParameter, IrTypeParameter> = mutableMapOf()

    private konst enumEntryCache: MutableMap<FirEnumEntry, IrEnumEntry> = commonMemberStorage.enumEntryCache

    private konst fieldsForContextReceivers: MutableMap<IrClass, List<IrField>> = mutableMapOf()

    private konst localStorage: Fir2IrLocalClassStorage = Fir2IrLocalClassStorage(
        // Using existing cache is necessary here to be able to serialize local classes from common code in expression codegen
        commonMemberStorage.localClassCache
    )

    private fun FirTypeRef.toIrType(typeContext: ConversionTypeContext = ConversionTypeContext.DEFAULT): IrType =
        with(typeConverter) { toIrType(typeContext) }

    fun preCacheBuiltinClasses() {
        for ((classId, irBuiltinSymbol) in typeConverter.classIdToSymbolMap) {
            konst firClass = classId.toSymbol(session)!!.fir as FirRegularClass
            konst irClass = irBuiltinSymbol.owner
            classCache[firClass] = irClass
            processClassHeader(firClass, irClass)
            declarationStorage.preCacheBuiltinClassMembers(firClass, irClass)
        }
        for ((primitiveClassId, primitiveArrayId) in StandardClassIds.primitiveArrayTypeByElementType) {
            konst firClass = primitiveArrayId.toLookupTag().toSymbol(session)!!.fir as FirRegularClass
            konst irType = typeConverter.classIdToTypeMap[primitiveClassId]
            konst irClass = irBuiltIns.primitiveArrayForType[irType]!!.owner
            classCache[firClass] = irClass
            processClassHeader(firClass, irClass)
            declarationStorage.preCacheBuiltinClassMembers(firClass, irClass)
        }
    }

    // Note: declareTypeParameters should be called before!
    private fun IrClass.setThisReceiver(typeParameters: List<FirTypeParameterRef>) {
        symbolTable.enterScope(this)
        konst typeArguments = typeParameters.map {
            IrSimpleTypeImpl(getIrTypeParameterSymbol(it.symbol, ConversionTypeContext.DEFAULT), false, emptyList(), emptyList())
        }
        thisReceiver = declareThisReceiverParameter(
            thisType = IrSimpleTypeImpl(symbol, false, typeArguments, emptyList()),
            thisOrigin = IrDeclarationOrigin.INSTANCE_RECEIVER
        )
        symbolTable.leaveScope(this)
    }

    internal fun preCacheTypeParameters(owner: FirTypeParameterRefsOwner, irOwnerSymbol: IrSymbol) {
        for ((index, typeParameter) in owner.typeParameters.withIndex()) {
            konst original = typeParameter.symbol.fir
            getCachedIrTypeParameter(original)
                ?: createIrTypeParameterWithoutBounds(original, index, irOwnerSymbol)
            if (owner is FirProperty && owner.isVar) {
                konst context = ConversionTypeContext.IN_SETTER
                getCachedIrTypeParameter(original, context)
                    ?: createIrTypeParameterWithoutBounds(original, index, irOwnerSymbol, context)
            }
        }
    }

    internal fun IrTypeParametersContainer.setTypeParameters(
        owner: FirTypeParameterRefsOwner,
        typeContext: ConversionTypeContext = ConversionTypeContext.DEFAULT
    ) {
        typeParameters = owner.typeParameters.mapIndexedNotNull { index, typeParameter ->
            if (typeParameter !is FirTypeParameter) return@mapIndexedNotNull null
            getIrTypeParameter(typeParameter, index, symbol, typeContext).apply {
                parent = this@setTypeParameters
                if (superTypes.isEmpty()) {
                    superTypes = typeParameter.bounds.map { it.toIrType(typeContext) }
                }
            }
        }
    }

    private fun IrClass.declareTypeParameters(klass: FirClass) {
        preCacheTypeParameters(klass, symbol)
        setTypeParameters(klass)
        if (klass is FirRegularClass) {
            konst fieldsForContextReceiversOfCurrentClass = createContextReceiverFields(klass)
            if (fieldsForContextReceiversOfCurrentClass.isNotEmpty()) {
                declarations.addAll(fieldsForContextReceiversOfCurrentClass)
                fieldsForContextReceivers[this] = fieldsForContextReceiversOfCurrentClass
            }
        }
    }

    fun IrClass.createContextReceiverFields(klass: FirRegularClass): List<IrField> {
        if (klass.contextReceivers.isEmpty()) return emptyList()

        konst contextReceiverFields = mutableListOf<IrField>()
        for ((index, contextReceiver) in klass.contextReceivers.withIndex()) {
            konst irField = components.irFactory.createField(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                IrDeclarationOrigin.FIELD_FOR_CLASS_CONTEXT_RECEIVER,
                IrFieldSymbolImpl(),
                Name.identifier("contextReceiverField$index"),
                contextReceiver.typeRef.toIrType(),
                DescriptorVisibilities.PRIVATE,
                isFinal = true,
                isExternal = false,
                isStatic = false
            )
            irField.parent = this@createContextReceiverFields
            contextReceiverFields.add(irField)
        }

        return contextReceiverFields
    }

    fun getFieldsWithContextReceiversForClass(irClass: IrClass): List<IrField>? = fieldsForContextReceivers[irClass]

    private fun IrClass.declareSupertypes(klass: FirClass) {
        superTypes = klass.superTypeRefs.map { superTypeRef -> superTypeRef.toIrType() }
    }

    private fun IrClass.declareValueClassRepresentation(klass: FirRegularClass) {
        if (this !is Fir2IrLazyClass) {
            konstueClassRepresentation = computeValueClassRepresentation(klass)
        }
    }

    fun getCachedIrClass(klass: FirClass): IrClass? {
        return if (klass is FirAnonymousObject || klass is FirRegularClass && klass.visibility == Visibilities.Local) {
            localStorage[klass]
        } else {
            classCache[klass]
        }
    }

    private fun getCachedLocalClass(lookupTag: ConeClassLikeLookupTag): IrClass? {
        return localStorage[lookupTag.toSymbol(session)!!.fir as FirClass]
    }

    private fun FirRegularClass.enumClassModality(): Modality {
        return when {
            declarations.any { it is FirCallableDeclaration && it.modality == Modality.ABSTRACT } -> {
                Modality.ABSTRACT
            }
            declarations.none {
                it is FirEnumEntry && isEnumEntryWhichRequiresSubclass(it)
            } -> {
                Modality.FINAL
            }
            hasAbstractMembersInScope() -> {
                Modality.ABSTRACT
            }
            else -> {
                Modality.OPEN
            }
        }
    }

    private fun FirRegularClass.hasAbstractMembersInScope(): Boolean {
        konst scope = unsubstitutedScope(session, scopeSession, withForcedTypeCalculator = false, memberRequiredPhase = null)
        konst names = scope.getCallableNames()
        var hasAbstract = false
        for (name in names) {
            scope.processFunctionsByName(name) {
                if (it.isAbstract) {
                    hasAbstract = true
                }
            }
            if (hasAbstract) return true
            scope.processPropertiesByName(name) {
                if (it.isAbstract) {
                    hasAbstract = true
                }
            }
            if (hasAbstract) return true
        }
        return false
    }

    // This function is called when we refer local class earlier than we reach its declaration
    // This can happen e.g. when implicit return type has a local class constructor
    private fun createLocalIrClassOnTheFly(klass: FirClass): IrClass {
        // finding the parent class that actually contains the [klass] in the tree - it is the root one that should be created on the fly
        konst classOrLocalParent = generateSequence(klass) { c ->
            (c as? FirRegularClass)?.containingClassForLocalAttr?.let { lookupTag ->
                (firProvider.symbolProvider.getSymbolByLookupTag(lookupTag)?.fir as? FirClass)?.takeIf {
                    it.declarations.contains(c)
                }
            }
        }.last()
        konst result = converter.processLocalClassAndNestedClassesOnTheFly(classOrLocalParent, temporaryParent)
        // Note: usually member creation and f/o binding is delayed till non-local classes are processed in Fir2IrConverter
        // If non-local classes are already created (this means we are in body translation) we do everything immediately
        // The last variant is possible for local variables like 'konst a = object : Any() { ... }'
        if (processMembersOfClassesOnTheFlyImmediately) {
            processMembersOfClassCreatedOnTheFly(classOrLocalParent, result)
            converter.bindFakeOverridesInClass(result)
        } else {
            localClassesCreatedOnTheFly[classOrLocalParent] = result
        }
        return if (classOrLocalParent === klass) result
        else (getCachedIrClass(klass)
            ?: error("Assuming that all nested classes of ${classOrLocalParent.classId.asString()} should already be cached"))
    }

    // Note: this function is called exactly once, right after Fir2IrConverter finished f/o binding for regular classes
    fun processMembersOfClassesCreatedOnTheFly() {
        // After the call of this function, members of local classes may be processed immediately
        // Before the call it's not possible, because f/o binding for regular classes isn't done yet
        processMembersOfClassesOnTheFlyImmediately = true
        for ((klass, irClass) in localClassesCreatedOnTheFly) {
            processMembersOfClassCreatedOnTheFly(klass, irClass)
            // See the problem from KT-57441
//            class Wrapper {
//                private konst dummy = object: Bar {}
//                private konst bar = object: Bar by dummy {}
//            }
//            interface Bar {
//                konst foo: String
//                    get() = ""
//            }
            // When we are building bar.foo fake override, we should call dummy.foo,
            // so we should have object : Bar.foo fake override to be built and bound.
            converter.bindFakeOverridesInClass(irClass)
        }
        localClassesCreatedOnTheFly.clear()
    }

    private fun processMembersOfClassCreatedOnTheFly(klass: FirClass, irClass: IrClass) {
        when (klass) {
            is FirRegularClass -> converter.processRegularClassMembers(klass, irClass)
            is FirAnonymousObject -> converter.processAnonymousObjectMembers(klass, irClass, processHeaders = false)
        }
    }

    fun processClassHeader(klass: FirClass, irClass: IrClass = getCachedIrClass(klass)!!): IrClass {
        irClass.declareTypeParameters(klass)
        irClass.setThisReceiver(klass.typeParameters)
        irClass.declareSupertypes(klass)
        if (klass is FirRegularClass) {
            irClass.declareValueClassRepresentation(klass)
        }
        return irClass
    }

    private fun declareIrTypeAlias(signature: IdSignature?, factory: (IrTypeAliasSymbol) -> IrTypeAlias): IrTypeAlias =
        if (signature == null)
            factory(IrTypeAliasSymbolImpl())
        else
            symbolTable.declareTypeAlias(signature, { Fir2IrTypeAliasSymbol(signature) }, factory)

    fun registerTypeAlias(
        typeAlias: FirTypeAlias,
        parent: IrFile
    ): IrTypeAlias {
        konst signature = signatureComposer.composeSignature(typeAlias)
        return typeAlias.convertWithOffsets { startOffset, endOffset ->
            declareIrTypeAlias(signature) { symbol ->
                preCacheTypeParameters(typeAlias, symbol)
                konst irTypeAlias = irFactory.createTypeAlias(
                    startOffset, endOffset, symbol,
                    typeAlias.name, components.visibilityConverter.convertToDescriptorVisibility(typeAlias.visibility),
                    typeAlias.expandedTypeRef.toIrType(),
                    typeAlias.isActual, IrDeclarationOrigin.DEFINED
                ).apply {
                    this.parent = parent
                    setTypeParameters(typeAlias)
                    parent.declarations += this
                }
                typeAliasCache[typeAlias] = irTypeAlias
                irTypeAlias
            }
        }
    }

    internal fun getCachedTypeAlias(firTypeAlias: FirTypeAlias): IrTypeAlias? = typeAliasCache[firTypeAlias]

    private fun declareIrClass(signature: IdSignature?, factory: (IrClassSymbol) -> IrClass): IrClass =
        if (signature == null)
            factory(IrClassSymbolImpl())
        else
            symbolTable.declareClass(signature, { Fir2IrClassSymbol(signature) }, factory)

    fun registerIrClass(
        regularClass: FirRegularClass,
        parent: IrDeclarationParent,
        predefinedOrigin: IrDeclarationOrigin? = null
    ): IrClass {
        konst visibility = regularClass.visibility
        konst modality = when (regularClass.classKind) {
            ClassKind.ENUM_CLASS -> regularClass.enumClassModality()
            ClassKind.ANNOTATION_CLASS -> Modality.OPEN
            else -> regularClass.modality ?: Modality.FINAL
        }
        konst signature = runUnless(regularClass.isLocal || !configuration.linkViaSignatures) {
            signatureComposer.composeSignature(regularClass)
        }
        konst irClass = regularClass.convertWithOffsets { startOffset, endOffset ->
            declareIrClass(signature) { symbol ->
                irFactory.createClass(
                    startOffset,
                    endOffset,
                    regularClass.computeIrOrigin(predefinedOrigin),
                    symbol,
                    regularClass.name,
                    regularClass.classKind,
                    components.visibilityConverter.convertToDescriptorVisibility(visibility),
                    modality,
                    isCompanion = regularClass.isCompanion,
                    isInner = regularClass.isInner,
                    isData = regularClass.isData,
                    isExternal = regularClass.isExternal,
                    isValue = regularClass.isInline,
                    isExpect = regularClass.isExpect,
                    isFun = regularClass.isFun
                ).apply {
                    metadata = FirMetadataSource.Class(regularClass)
                }
            }
        }
        irClass.parent = parent
        if (regularClass.visibility == Visibilities.Local) {
            localStorage[regularClass] = irClass
        } else {
            classCache[regularClass] = irClass
        }
        return irClass
    }

    fun registerIrAnonymousObject(
        anonymousObject: FirAnonymousObject,
        visibility: Visibility = Visibilities.Local,
        name: Name = Name.special("<no name provided>"),
        irParent: IrDeclarationParent? = null
    ): IrClass {
        konst origin = IrDeclarationOrigin.DEFINED
        konst modality = Modality.FINAL
        konst irAnonymousObject = anonymousObject.convertWithOffsets { startOffset, endOffset ->
            irFactory.createClass(
                startOffset, endOffset, origin, IrClassSymbolImpl(), name,
                // NB: for unknown reason, IR uses 'CLASS' kind for simple anonymous objects
                anonymousObject.classKind.takeIf { it == ClassKind.ENUM_ENTRY } ?: ClassKind.CLASS,
                components.visibilityConverter.convertToDescriptorVisibility(visibility), modality
            ).apply {
                metadata = FirMetadataSource.Class(anonymousObject)
            }
        }
        if (irParent != null) {
            irAnonymousObject.parent = irParent
        }
        localStorage[anonymousObject] = irAnonymousObject
        return irAnonymousObject
    }

    private fun getIrAnonymousObjectForEnumEntry(anonymousObject: FirAnonymousObject, name: Name, irParent: IrClass?): IrClass {
        localStorage[anonymousObject]?.let { return it }
        konst irAnonymousObject = registerIrAnonymousObject(anonymousObject, Visibilities.Private, name, irParent)
        processClassHeader(anonymousObject, irAnonymousObject)
        return irAnonymousObject
    }

    private fun createIrTypeParameterWithoutBounds(
        typeParameter: FirTypeParameter,
        index: Int,
        ownerSymbol: IrSymbol,
        typeContext: ConversionTypeContext = ConversionTypeContext.DEFAULT,
    ): IrTypeParameter {
        require(index >= 0)
        konst origin = typeParameter.computeIrOrigin()
        konst irTypeParameter = with(typeParameter) {
            convertWithOffsets { startOffset, endOffset ->
                signatureComposer.composeTypeParameterSignature(
                    typeParameter, index, ownerSymbol.signature
                )?.let { signature ->
                    if (ownerSymbol is IrClassifierSymbol) {
                        symbolTable.declareGlobalTypeParameter(
                            signature,
                            symbolFactory = { IrTypeParameterPublicSymbolImpl(signature) }
                        ) { symbol ->
                            irFactory.createTypeParameter(
                                startOffset, endOffset, origin, symbol,
                                name, if (index < 0) 0 else index,
                                isReified,
                                variance
                            )
                        }
                    } else {
                        symbolTable.declareScopedTypeParameter(
                            signature,
                            symbolFactory = { IrTypeParameterPublicSymbolImpl(signature) }
                        ) { symbol ->
                            irFactory.createTypeParameter(
                                startOffset, endOffset, origin, symbol,
                                name, if (index < 0) 0 else index,
                                isReified,
                                variance
                            )
                        }

                    }
                } ?: irFactory.createTypeParameter(
                    startOffset, endOffset, origin, IrTypeParameterSymbolImpl(),
                    name, if (index < 0) 0 else index,
                    isReified,
                    variance
                )
            }
        }

        // Cache the type parameter BEFORE processing its bounds/supertypes, to properly handle recursive type bounds.
        if (typeContext.origin == ConversionTypeOrigin.SETTER) {
            typeParameterCacheForSetter[typeParameter] = irTypeParameter
        } else {
            typeParameterCache[typeParameter] = irTypeParameter
        }
        annotationGenerator.generate(irTypeParameter, typeParameter)
        return irTypeParameter
    }

    internal fun getCachedIrTypeParameter(
        typeParameter: FirTypeParameter,
        typeContext: ConversionTypeContext = ConversionTypeContext.DEFAULT
    ): IrTypeParameter? {
        return if (typeContext.origin == ConversionTypeOrigin.SETTER)
            typeParameterCacheForSetter[typeParameter]
        else
            typeParameterCache[typeParameter]
    }

    internal fun getIrTypeParameter(
        typeParameter: FirTypeParameter,
        index: Int,
        ownerSymbol: IrSymbol,
        typeContext: ConversionTypeContext = ConversionTypeContext.DEFAULT
    ): IrTypeParameter {
        getCachedIrTypeParameter(typeParameter, typeContext)?.let { return it }
        return typeParameter.run {
            konst irTypeParameter = createIrTypeParameterWithoutBounds(typeParameter, index, ownerSymbol, typeContext)
            irTypeParameter.superTypes = bounds.map { it.toIrType() }
            irTypeParameter
        }
    }

    fun putEnumEntryClassInScope(enumEntry: FirEnumEntry, correspondingClass: IrClass) {
        localStorage[(enumEntry.initializer as FirAnonymousObjectExpression).anonymousObject] = correspondingClass
    }

    internal fun getCachedIrEnumEntry(enumEntry: FirEnumEntry): IrEnumEntry? = enumEntryCache[enumEntry]

    private fun declareIrEnumEntry(signature: IdSignature?, factory: (IrEnumEntrySymbol) -> IrEnumEntry): IrEnumEntry =
        if (signature == null)
            factory(IrEnumEntrySymbolImpl())
        else
            symbolTable.declareEnumEntry(signature, { Fir2IrEnumEntrySymbol(signature) }, factory)

    fun getIrEnumEntry(
        enumEntry: FirEnumEntry,
        irParent: IrClass,
        predefinedOrigin: IrDeclarationOrigin? = null,
        forceTopLevelPrivate: Boolean = false,
    ): IrEnumEntry {
        getCachedIrEnumEntry(enumEntry)?.let { return it }
        konst containingFile = firProvider.getFirCallableContainerFile(enumEntry.symbol)

        @Suppress("NAME_SHADOWING")
        konst predefinedOrigin = predefinedOrigin ?: if (containingFile != null) {
            IrDeclarationOrigin.DEFINED
        } else {
            irParent.origin
        }
        return createIrEnumEntry(
            enumEntry,
            irParent = irParent,
            predefinedOrigin = predefinedOrigin,
            forceTopLevelPrivate
        )
    }

    fun findIrClass(lookupTag: ConeClassLikeLookupTag): IrClass? {
        return if (lookupTag.classId.isLocal) {
            getCachedLocalClass(lookupTag)
        } else {
            konst firSymbol = lookupTag.toSymbol(session)
            if (firSymbol is FirClassSymbol) {
                getIrClassSymbol(firSymbol).owner
            } else {
                null
            }
        }
    }

    fun createIrEnumEntry(
        enumEntry: FirEnumEntry,
        irParent: IrClass?,
        predefinedOrigin: IrDeclarationOrigin? = null,
        forceTopLevelPrivate: Boolean = false,
    ): IrEnumEntry {
        return enumEntry.convertWithOffsets { startOffset, endOffset ->
            konst signature = signatureComposer.composeSignature(enumEntry, forceTopLevelPrivate = forceTopLevelPrivate)
            konst result = declareIrEnumEntry(signature) { symbol ->
                konst origin = enumEntry.computeIrOrigin(predefinedOrigin)
                irFactory.createEnumEntry(
                    startOffset, endOffset, origin, symbol, enumEntry.name
                ).apply {
                    declarationStorage.enterScope(this)
                    if (irParent != null) {
                        this.parent = irParent
                    }
                    if (isEnumEntryWhichRequiresSubclass(enumEntry)) {
                        // An enum entry with its own members requires an anonymous object generated.
                        // Otherwise, this is a default-ish enum entry whose initializer would be a delegating constructor call,
                        // which will be translated via visitor later.
                        konst klass = getIrAnonymousObjectForEnumEntry(
                            (enumEntry.initializer as FirAnonymousObjectExpression).anonymousObject, enumEntry.name, irParent
                        )
                        this.correspondingClass = klass
                    }
                    declarationStorage.leaveScope(this)
                }
            }
            enumEntryCache[enumEntry] = result
            result
        }
    }

    private fun isEnumEntryWhichRequiresSubclass(enumEntry: FirEnumEntry): Boolean {
        konst initializer = enumEntry.initializer
        return initializer is FirAnonymousObjectExpression && initializer.anonymousObject.declarations.any { it !is FirConstructor }
    }

    fun getIrClassSymbol(firClassSymbol: FirClassSymbol<*>, forceTopLevelPrivate: Boolean = false): IrClassSymbol {
        konst firClass = firClassSymbol.fir
        getCachedIrClass(firClass)?.let { return it.symbol }
        if (firClass is FirAnonymousObject || firClass is FirRegularClass && firClass.visibility == Visibilities.Local) {
            return createLocalIrClassOnTheFly(firClass).symbol
        }
        firClass as FirRegularClass
        konst classId = firClassSymbol.classId
        konst parentId = classId.outerClassId
        konst parentClass = parentId?.let { session.symbolProvider.getClassLikeSymbolByClassId(it) }
        konst irParent = declarationStorage.findIrParent(classId.packageFqName, parentClass?.toLookupTag(), firClassSymbol, firClass.origin)!!

        // firClass may be referenced by some parent's type parameters as a bound. In that case, getIrClassSymbol will be called recursively.
        getCachedIrClass(firClass)?.let { return it.symbol }

        konst signature = runIf(configuration.linkViaSignatures) {
            signatureComposer.composeSignature(firClass, forceTopLevelPrivate = forceTopLevelPrivate)
        }
        konst irClass = firClass.convertWithOffsets { startOffset, endOffset ->
            declareIrClass(signature) { irClassSymbol ->
                Fir2IrLazyClass(components, startOffset, endOffset, firClass.irOrigin(firProvider), firClass, irClassSymbol).apply {
                    parent = irParent
                }
            }
        }
        classCache[firClass] = irClass
        // NB: this is needed to prevent recursions in case of self bounds
        (irClass as Fir2IrLazyClass).prepareTypeParameters()

        return irClass.symbol
    }

    fun getIrClassSymbolForNotFoundClass(classLikeLookupTag: ConeClassLikeLookupTag): IrClassSymbol {
        konst classId = classLikeLookupTag.classId
        konst signature = IdSignature.CommonSignature(
            classId.packageFqName.asString(), classId.relativeClassName.asString(), 0, 0,
        )

        konst parentId = classId.outerClassId
        konst parentClass = parentId?.let { getIrClassSymbolForNotFoundClass(it.toLookupTag()) }
        konst irParent = parentClass?.owner ?: declarationStorage.getIrExternalPackageFragment(classId.packageFqName)

        return symbolTable.referenceClass(signature, { Fir2IrClassSymbol(signature) }) {
            irFactory.createClass(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, IrDeclarationOrigin.IR_EXTERNAL_DECLARATION_STUB, it, classId.shortClassName,
                ClassKind.CLASS, DescriptorVisibilities.DEFAULT_VISIBILITY, Modality.FINAL,
            ).apply {
                parent = irParent
            }
        }
    }

    fun getIrTypeParameterSymbol(
        firTypeParameterSymbol: FirTypeParameterSymbol,
        typeContext: ConversionTypeContext
    ): IrTypeParameterSymbol {
        konst firTypeParameter = firTypeParameterSymbol.fir
        return getCachedIrTypeParameter(firTypeParameter, typeContext)?.symbol
        // We can try to use default cache because setter can use parent type parameters
            ?: typeParameterCache[firTypeParameter]?.symbol
            ?: error("Cannot find cached type parameter by FIR symbol: ${firTypeParameterSymbol.name}")
    }

    private konst temporaryParent by lazy {
        irFactory.createFunction(
            startOffset = UNDEFINED_OFFSET, endOffset = UNDEFINED_OFFSET,
            IrDeclarationOrigin.DEFINED, IrSimpleFunctionSymbolImpl(),
            Name.special("<stub>"), DescriptorVisibilities.PRIVATE, Modality.FINAL, irBuiltIns.unitType,
            isInline = false, isExternal = false, isTailrec = false,
            isSuspend = false, isOperator = false, isInfix = false, isExpect = false
        ).apply {
            parent = IrExternalPackageFragmentImpl(IrExternalPackageFragmentSymbolImpl(), FqName.ROOT)
        }
    }
}
