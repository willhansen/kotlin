/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.plugin.generators

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.builder.buildBlock
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.fqn
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

/*
 * For each class with @Serializable annotation generates method serializeClassName(x: ClassName)
 *   in each class annotated with @CoreSerializer
 */
class MembersOfSerializerGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {
    companion object {
        private konst SERIALIZABLE_PREDICATE = LookupPredicate.create { annotated("MySerializable".fqn()) }
        private konst CORE_SERIALIZER_PREDICATE = LookupPredicate.create { annotated("CoreSerializer".fqn()) }

        private konst X_NAME = Name.identifier("x")
    }

    private konst predicateBasedProvider = session.predicateBasedProvider
    private konst matchedSerializableClasses by lazy {
        predicateBasedProvider.getSymbolsByPredicate(SERIALIZABLE_PREDICATE).filterIsInstance<FirRegularClassSymbol>()
    }
    private konst serializableClassIds by lazy {
        matchedSerializableClasses.map { it.classId }
    }

    private konst matchedCoreSerializerClasses by lazy {
        predicateBasedProvider.getSymbolsByPredicate(CORE_SERIALIZER_PREDICATE).filterIsInstance<FirRegularClassSymbol>()
    }

    private konst serializeMethodNames by lazy {
        serializableClassIds.associateBy { Name.identifier("serialize${it.shortClassName.identifier}") }
    }

    override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> {
        konst owner = context?.owner ?: return emptyList()
        konst argumentClassId = serializeMethodNames[callableId.callableName] ?: return emptyList()

        konst function = createMemberFunction(owner, Key, callableId.callableName, session.builtinTypes.unitType.type) {
            konstueParameter(X_NAME, argumentClassId.createConeType(session))
        }.apply {
            replaceBody(buildBlock {}.apply { replaceTypeRef(session.builtinTypes.unitType) })
        }
        return listOf(function.symbol)
    }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        return when (classSymbol) {
            in matchedCoreSerializerClasses -> serializeMethodNames.keys
            else -> emptySet()
        }
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(SERIALIZABLE_PREDICATE, CORE_SERIALIZER_PREDICATE)
    }

    object Key : GeneratedDeclarationKey()
}
