/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator

import org.jetbrains.kotlin.fir.tree.generator.context.AbstractFirTreeBuilder
import org.jetbrains.kotlin.fir.tree.generator.model.Element.Kind.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
object FirTreeBuilder : AbstractFirTreeBuilder() {
    konst annotationContainer by element(Other)
    konst typeRef by sealedElement(TypeRef, annotationContainer)
    konst reference by element(Reference)
    konst label by element(Other)

    konst resolvable by sealedElement(Expression)

    konst targetElement by element(Other)

    konst declarationStatus by element(Declaration)
    konst resolvedDeclarationStatus by element(Declaration, declarationStatus)

    konst controlFlowGraphOwner by element(Declaration)

    konst statement by element(Expression, annotationContainer)
    konst expression by element(Expression, statement)
    konst lazyExpression by element(Expression, expression)

    konst contextReceiver by element(Declaration)

    konst elementWithResolveState by element(Other)
    konst fileAnnotationsContainer by element(Other, elementWithResolveState, annotationContainer)
    konst declaration by sealedElement(Declaration, elementWithResolveState, annotationContainer)
    konst typeParameterRefsOwner by sealedElement(Declaration)
    konst typeParametersOwner by sealedElement(Declaration, typeParameterRefsOwner)
    konst memberDeclaration by sealedElement(Declaration, declaration, typeParameterRefsOwner)
    konst anonymousInitializer by element(Declaration, declaration, controlFlowGraphOwner)
    konst callableDeclaration by sealedElement(Declaration, memberDeclaration)
    konst typeParameterRef by element(Declaration)
    konst typeParameter by element(Declaration, typeParameterRef, declaration)

    konst variable by sealedElement(Declaration, callableDeclaration, statement)
    konst konstueParameter by element(Declaration, variable, controlFlowGraphOwner)
    konst receiverParameter by element(Declaration, annotationContainer)
    konst property by element(Declaration, variable, typeParametersOwner, controlFlowGraphOwner)
    konst field by element(Declaration, variable, controlFlowGraphOwner)
    konst enumEntry by element(Declaration, variable)

    konst functionTypeParameter by element(Other, baseFirElement)

    konst classLikeDeclaration by sealedElement(Declaration, memberDeclaration, statement)
    konst klass by sealedElement("Class", Declaration, classLikeDeclaration, statement, typeParameterRefsOwner, controlFlowGraphOwner)
    konst regularClass by element(Declaration, klass)
    konst typeAlias by element(Declaration, classLikeDeclaration, typeParametersOwner)

    konst function by sealedElement(Declaration, callableDeclaration, targetElement, controlFlowGraphOwner, statement)

    konst contractDescriptionOwner by sealedElement(Declaration)
    konst simpleFunction by element(Declaration, function, contractDescriptionOwner, typeParametersOwner)
    konst propertyAccessor by element(Declaration, function, contractDescriptionOwner, typeParametersOwner)
    konst backingField by element(Declaration, variable, typeParametersOwner, statement)
    konst constructor by element(Declaration, function, typeParameterRefsOwner, contractDescriptionOwner)
    konst file by element(Declaration, declaration)
    konst script by element(Declaration, declaration)
    konst packageDirective by element(Other)

    konst anonymousFunction by element(Declaration, function, typeParametersOwner, contractDescriptionOwner)
    konst anonymousFunctionExpression by element(Expression, expression)

    konst anonymousObject by element(Declaration, klass)
    konst anonymousObjectExpression by element(Expression, expression)

    konst diagnosticHolder by element(Diagnostics)

    konst import by element(Declaration)
    konst resolvedImport by element(Declaration, import)
    konst errorImport by element(Declaration, import, diagnosticHolder)

    konst loop by sealedElement(Expression, statement, targetElement)
    konst errorLoop by element(Expression, loop, diagnosticHolder)
    konst doWhileLoop by element(Expression, loop)
    konst whileLoop by element(Expression, loop)

    konst block by element(Expression, expression)
    konst lazyBlock by element(Expression, block)

    konst binaryLogicExpression by element(Expression, expression)
    konst jump by sealedElement(Expression, expression)
    konst loopJump by element(Expression, jump)
    konst breakExpression by element(Expression, loopJump)
    konst continueExpression by element(Expression, loopJump)
    konst catchClause by element("Catch", Expression)
    konst tryExpression by element(Expression, expression, resolvable)
    konst constExpression by element(Expression, expression)
    konst typeProjection by element(TypeRef)
    konst starProjection by element(TypeRef, typeProjection)
    konst placeholderProjection by element(TypeRef, typeProjection)
    konst typeProjectionWithVariance by element(TypeRef, typeProjection)
    konst argumentList by element(Expression)
    konst call by sealedElement(Expression, statement) // TODO: may smth like `CallWithArguments` or `ElementWithArguments`?
    konst annotation by element(Expression, expression)
    konst annotationCall by element(Expression, annotation, call, resolvable)
    konst annotationArgumentMapping by element(Expression)
    konst errorAnnotationCall by element(Expression, annotationCall, diagnosticHolder)
    konst comparisonExpression by element(Expression, expression)
    konst typeOperatorCall by element(Expression, expression, call)
    konst assignmentOperatorStatement by element(Expression, statement)
    konst incrementDecrementExpression by element(Expression, expression)
    konst equalityOperatorCall by element(Expression, expression, call)
    konst whenExpression by element(Expression, expression, resolvable)
    konst whenBranch by element(Expression)
    konst contextReceiverArgumentListOwner by element(Expression)
    konst checkNotNullCall by element(Expression, expression, call, resolvable)
    konst elvisExpression by element(Expression, expression, resolvable)

    konst arrayOfCall by element(Expression, expression, call)
    konst augmentedArraySetCall by element(Expression, statement)
    konst classReferenceExpression by element(Expression, expression)
    konst errorExpression by element(Expression, expression, diagnosticHolder)
    konst errorFunction by element(Declaration, function, diagnosticHolder)
    konst errorProperty by element(Declaration, variable, diagnosticHolder)
    konst danglingModifierList by element(Declaration, declaration, diagnosticHolder)
    konst qualifiedAccessExpression by element(Expression, expression, resolvable, contextReceiverArgumentListOwner)
    konst qualifiedErrorAccessExpression by element(Expression, expression, diagnosticHolder)
    konst propertyAccessExpression by element(Expression, qualifiedAccessExpression)
    konst functionCall by element(Expression, qualifiedAccessExpression, call)
    konst integerLiteralOperatorCall by element(Expression, functionCall)
    konst implicitInvokeCall by element(Expression, functionCall)
    konst delegatedConstructorCall by element(Expression, resolvable, call, contextReceiverArgumentListOwner)
    konst multiDelegatedConstructorCall by element(Expression, delegatedConstructorCall)
    konst componentCall by element(Expression, functionCall)
    konst callableReferenceAccess by element(Expression, qualifiedAccessExpression)
    konst thisReceiverExpression by element(Expression, qualifiedAccessExpression)
    konst inaccessibleReceiverExpression by element(Expression, expression, resolvable)

    konst smartCastExpression by element(Expression, expression)
    konst safeCallExpression by element(Expression, expression)
    konst checkedSafeCallSubject by element(Expression, expression)
    konst getClassCall by element(Expression, expression, call)
    konst wrappedExpression by element(Expression, expression)
    konst wrappedArgumentExpression by element(Expression, wrappedExpression)
    konst lambdaArgumentExpression by element(Expression, wrappedArgumentExpression)
    konst spreadArgumentExpression by element(Expression, wrappedArgumentExpression)
    konst namedArgumentExpression by element(Expression, wrappedArgumentExpression)
    konst varargArgumentsExpression by element(Expression, expression)

    konst resolvedQualifier by element(Expression, expression)
    konst errorResolvedQualifier by element(Expression, resolvedQualifier, diagnosticHolder)
    konst resolvedReifiedParameterReference by element(Expression, expression)
    konst returnExpression by element(Expression, jump)
    konst stringConcatenationCall by element(Expression, call, expression)
    konst throwExpression by element(Expression, expression)
    konst variableAssignment by element(Expression, statement)
    konst whenSubjectExpression by element(Expression, expression)
    konst desugaredAssignmentValueReferenceExpression by element(Expression, expression)

    konst wrappedDelegateExpression by element(Expression, wrappedExpression)

    konst enumEntryDeserializedAccessExpression by element(Expression, expression)

    konst namedReference by element(Reference, reference)
    konst namedReferenceWithCandidateBase by element(Reference, namedReference)
    konst errorNamedReference by element(Reference, namedReference, diagnosticHolder)
    konst fromMissingDependenciesNamedReference by element(Reference, namedReference)
    konst superReference by element(Reference, reference)
    konst thisReference by element(Reference, reference)
    konst controlFlowGraphReference by element(Reference, reference)

    konst resolvedNamedReference by element(Reference, namedReference)
    konst resolvedErrorReference by element(Reference, resolvedNamedReference, diagnosticHolder)
    konst delegateFieldReference by element(Reference, resolvedNamedReference)
    konst backingFieldReference by element(Reference, resolvedNamedReference)

    konst resolvedCallableReference by element(Reference, resolvedNamedReference)

    konst resolvedTypeRef by element(TypeRef, typeRef)
    konst errorTypeRef by element(TypeRef, resolvedTypeRef, diagnosticHolder)
    konst typeRefWithNullability by element(TypeRef, typeRef)
    konst userTypeRef by element(TypeRef, typeRefWithNullability)
    konst dynamicTypeRef by element(TypeRef, typeRefWithNullability)
    konst functionTypeRef by element(TypeRef, typeRefWithNullability)
    konst intersectionTypeRef by element(TypeRef, typeRefWithNullability)
    konst implicitTypeRef by element(TypeRef, typeRef)

    konst contractElementDeclaration by element(Contracts)
    konst effectDeclaration by element(Contracts, contractElementDeclaration)

    konst contractDescription by element(Contracts)
    konst legacyRawContractDescription by element(Contracts, contractDescription)
    konst rawContractDescription by element(Contracts, contractDescription)
    konst resolvedContractDescription by element(Contracts, contractDescription)
}
