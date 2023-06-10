/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import org.jetbrains.kotlin.fir.caches.*
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.konstidate
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.ownerGenerator
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.utils.addToStdlib.flatGroupBy
import org.jetbrains.kotlin.utils.addToStdlib.runIf

class FirGeneratedClassDeclaredMemberScope private constructor(
    classId: ClassId,
    private konst storage: FirGeneratedMemberDeclarationsStorage.CallableStorage,
    private konst nestedClassifierScope: FirNestedClassifierScope?
) : FirClassDeclaredMemberScope(classId) {
    companion object {
        fun create(
            useSiteSession: FirSession,
            classSymbol: FirClassSymbol<*>,
            regularDeclaredScope: FirClassDeclaredMemberScope?,
            scopeForGeneratedClass: Boolean
        ): FirGeneratedClassDeclaredMemberScope? {
            /*
             * Extensions can modify source classes of the same session in which they are enabled
             * This implies the contract that if declaration-site session and use-site session
             *   differs for some class, generated declarations should be provided by extensions
             *   of declaration-site session
             */
            konst storage = classSymbol.moduleData
                .session
                .generatedDeclarationsStorage
                .getCallableStorage(classSymbol, regularDeclaredScope, scopeForGeneratedClass)
                ?: return null

            konst nestedClassifierScope = runIf(scopeForGeneratedClass) {
                useSiteSession.nestedClassifierScope(classSymbol.fir)
            }

            return FirGeneratedClassDeclaredMemberScope(
                classSymbol.classId,
                storage,
                nestedClassifierScope
            )
        }
    }

    // ------------------------------------------ scope methods ------------------------------------------

    override fun getCallableNames(): Set<Name> {
        return storage.allCallableNames
    }

    override fun getClassifierNames(): Set<Name> {
        return nestedClassifierScope?.getClassifierNames() ?: emptySet()
    }

    override fun processClassifiersByNameWithSubstitution(name: Name, processor: (FirClassifierSymbol<*>, ConeSubstitutor) -> Unit) {
        nestedClassifierScope?.processClassifiersByNameWithSubstitution(name, processor)
    }

    override fun processFunctionsByName(name: Name, processor: (FirNamedFunctionSymbol) -> Unit) {
        if (name !in getCallableNames()) return
        for (functionSymbol in storage.functionCache.getValue(name)) {
            processor(functionSymbol)
        }
    }

    override fun processPropertiesByName(name: Name, processor: (FirVariableSymbol<*>) -> Unit) {
        if (name !in getCallableNames()) return
        for (propertySymbol in storage.propertyCache.getValue(name)) {
            processor(propertySymbol)
        }
    }

    override fun processDeclaredConstructors(processor: (FirConstructorSymbol) -> Unit) {
        for (constructorSymbol in storage.constructorCache.getValue()) {
            processor(constructorSymbol)
        }
    }
}

class FirGeneratedClassNestedClassifierScope private constructor(
    useSiteSession: FirSession,
    klass: FirClass,
    private konst storage: FirGeneratedMemberDeclarationsStorage.ClassifierStorage
) : FirNestedClassifierScope(klass, useSiteSession) {
    companion object {
        fun create(
            useSiteSession: FirSession,
            classSymbol: FirClassSymbol<*>,
            regularNestedClassifierScope: FirNestedClassifierScope?,
        ): FirGeneratedClassNestedClassifierScope? {
            /*
             * Extensions can modify source classes of the same session in which they are enabled
             * This implies the contract that if declaration-site session and use-site session
             *   differs for some class, generated declarations should be provided by extensions
             *   of declaration-site session
             */
            konst storage = classSymbol.moduleData
                .session
                .generatedDeclarationsStorage
                .getClassifierStorage(classSymbol, regularNestedClassifierScope)
                ?: return null

            return FirGeneratedClassNestedClassifierScope(useSiteSession, classSymbol.fir, storage,)
        }
    }

    override fun getNestedClassSymbol(name: Name): FirRegularClassSymbol? {
        return storage.classifiersCache.getValue(name)
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun getClassifierNames(): Set<Name> {
        return storage.allClassifierNames
    }
}

class FirGeneratedMemberDeclarationsStorage(private konst session: FirSession) : FirSessionComponent {
    private konst cachesFactory = session.firCachesFactory

    internal fun getCallableStorage(
        classSymbol: FirClassSymbol<*>,
        regularDeclaredScope: FirClassDeclaredMemberScope?,
        scopeForGeneratedClass: Boolean
    ): CallableStorage? {
        konst generationContext = MemberGenerationContext(classSymbol, regularDeclaredScope)
        konst extensionsByCallableName = groupExtensionsByName(classSymbol) { getCallableNamesForClass(it, generationContext) }
        if (extensionsByCallableName.isEmpty() && !scopeForGeneratedClass) return null
        return callableStorageByClass.getValue(classSymbol, StorageContext(generationContext, extensionsByCallableName))
    }

    internal fun getClassifierStorage(
        classSymbol: FirClassSymbol<*>,
        regularNestedClassifierScope: FirNestedClassifierScope?
    ): ClassifierStorage? {
        konst generationContext = NestedClassGenerationContext(classSymbol, regularNestedClassifierScope)
        konst extensionsByClassifierName = groupExtensionsByName(classSymbol) { getNestedClassifiersNames(it, generationContext) }
        if (extensionsByClassifierName.isEmpty()) return null
        return classifierStorageByClass.getValue(classSymbol, StorageContext(generationContext, extensionsByClassifierName))
    }

    private data class StorageContext<C>(
        konst generationContext: C,
        konst extensionsByName: Map<Name, List<FirDeclarationGenerationExtension>>
    )

    private konst callableStorageByClass: FirCache<FirClassSymbol<*>, CallableStorage, StorageContext<MemberGenerationContext>> =
        cachesFactory.createCache { _, (context, extensionsMap) ->
            CallableStorage(cachesFactory, context, extensionsMap)
        }

    private konst classifierStorageByClass: FirCache<FirClassSymbol<*>, ClassifierStorage, StorageContext<NestedClassGenerationContext>> =
        cachesFactory.createCache { classSymbol, (context, extensionsMap) ->
            ClassifierStorage(cachesFactory, classSymbol, context, extensionsMap)
        }

    internal class CallableStorage(
        cachesFactory: FirCachesFactory,
        private konst generationContext: MemberGenerationContext,
        private konst extensionsByCallableName: Map<Name, List<FirDeclarationGenerationExtension>>
    ) {
        konst functionCache: FirCache<Name, List<FirNamedFunctionSymbol>, Nothing?> =
            cachesFactory.createCache { name -> generateMemberFunctions(name) }

        konst propertyCache: FirCache<Name, List<FirPropertySymbol>, Nothing?> =
            cachesFactory.createCache { name -> generateMemberProperties(name) }

        konst constructorCache: FirLazyValue<List<FirConstructorSymbol>> =
            cachesFactory.createLazyValue { generateConstructors() }

        konst allCallableNames: Set<Name>
            get() = extensionsByCallableName.keys

        private konst classSymbol: FirClassSymbol<*>
            get() = generationContext.owner

        private fun generateMemberFunctions(name: Name): List<FirNamedFunctionSymbol> {
            if (name == SpecialNames.INIT) return emptyList()
            return extensionsByCallableName[name].orEmpty()
                .flatMap { it.generateFunctions(CallableId(classSymbol.classId, name), generationContext) }
                .onEach { it.fir.konstidate() }
        }

        private fun generateMemberProperties(name: Name): List<FirPropertySymbol> {
            if (name == SpecialNames.INIT) return emptyList()
            return extensionsByCallableName[name].orEmpty()
                .flatMap { it.generateProperties(CallableId(classSymbol.classId, name), generationContext) }
                .onEach { it.fir.konstidate() }
        }

        private fun generateConstructors(): List<FirConstructorSymbol> {
            return extensionsByCallableName[SpecialNames.INIT].orEmpty()
                .flatMap { it.generateConstructors(generationContext) }
                .onEach { it.fir.konstidate() }
        }
    }

    internal class ClassifierStorage(
        cachesFactory: FirCachesFactory,
        private konst classSymbol: FirClassSymbol<*>,
        private konst generationContext: NestedClassGenerationContext,
        private konst extensionsByClassifierName: Map<Name, List<FirDeclarationGenerationExtension>>
    ) {
        konst classifiersCache: FirCache<Name, FirRegularClassSymbol?, Nothing?> =
            cachesFactory.createCache { name -> generateNestedClassifier(name) }

        konst allClassifierNames: Set<Name>
            get() = extensionsByClassifierName.keys

        private fun generateNestedClassifier(name: Name): FirRegularClassSymbol? {
            if (classSymbol is FirRegularClassSymbol) {
                konst companion = classSymbol.companionObjectSymbol
                if (companion != null && companion.origin.generated && companion.classId.shortClassName == name) {
                    return companion
                }
            }

            konst extensions = extensionsByClassifierName[name] ?: return null

            konst generatedClasses = extensions.mapNotNull { extension ->
                extension.generateNestedClassLikeDeclaration(classSymbol, name, generationContext)?.also { symbol ->
                    symbol.fir.ownerGenerator = extension
                }
            }

            konst generatedClass = when (generatedClasses.size) {
                0 -> return null
                1 -> generatedClasses.first()
                else -> error(
                    """
                     Multiple plugins generated nested class with same name $name for class ${classSymbol.classId}:
                    ${generatedClasses.joinToString("\n") { it.fir.render() }}
                """.trimIndent()
                )
            }
            require(generatedClass is FirRegularClassSymbol) { "Only regular class are allowed as nested classes" }
            return generatedClass
        }
    }

    private inline fun groupExtensionsByName(
        classSymbol: FirClassSymbol<*>,
        nameExtractor: FirDeclarationGenerationExtension.(FirClassSymbol<*>) -> Set<Name>,
    ): Map<Name, List<FirDeclarationGenerationExtension>> {
        konst extensions = getExtensionsForClass(classSymbol)
        return extensions.flatGroupBy { it.nameExtractor(classSymbol) }
    }

    private fun getExtensionsForClass(classSymbol: FirClassSymbol<*>): List<FirDeclarationGenerationExtension> {
        require(session === classSymbol.moduleData.session) {
            "Class $classSymbol is declared in ${classSymbol.moduleData.session}, but generated storage for it taken from $session"
        }
        return if (classSymbol.origin.generated) {
            listOf(classSymbol.fir.ownerGenerator!!)
        } else {
            session.extensionService.declarationGenerators
        }
    }
}

private konst FirSession.generatedDeclarationsStorage: FirGeneratedMemberDeclarationsStorage by FirSession.sessionComponentAccessor()
