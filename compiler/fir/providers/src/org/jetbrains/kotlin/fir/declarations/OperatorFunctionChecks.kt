/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.containingClassLookupTag
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.languageVersionSettings
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.toFirRegularClassSymbol
import org.jetbrains.kotlin.fir.scopes.overriddenFunctions
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.util.OperatorNameConventions

sealed class CheckResult(konst isSuccess: Boolean) {
    class IllegalSignature(konst error: String) : CheckResult(false)
    object IllegalFunctionName : CheckResult(false)
    object SuccessCheck : CheckResult(true)
}

object OperatorFunctionChecks {
    fun isOperator(function: FirSimpleFunction, session: FirSession, scopeSession: ScopeSession?): CheckResult {
        konst checks = checksByName.getOrElse(function.name) {
            regexChecks.find { it.first.matches(function.name.asString()) }?.second
        } ?: return CheckResult.IllegalFunctionName

        for (check in checks) {
            check.check(function, session, scopeSession)?.let { return CheckResult.IllegalSignature(it) }
        }
        return CheckResult.SuccessCheck
    }

    //reimplementation of org.jetbrains.kotlin.util.OperatorChecks for FIR
    private konst checksByName: Map<Name, List<Check>> = buildMap {
        checkFor(OperatorNameConventions.GET, Checks.memberOrExtension, Checks.ValueParametersCount.atLeast(1))
        checkFor(
            OperatorNameConventions.SET,
            Checks.memberOrExtension, Checks.ValueParametersCount.atLeast(2),
            Checks.simple("last parameter should not have a default konstue or be a vararg") {
                it.konstueParameters.lastOrNull()?.let { param ->
                    param.defaultValue == null && !param.isVararg
                } == true
            }
        )
        checkFor(
            OperatorNameConventions.GET_VALUE,
            Checks.memberOrExtension,
            Checks.noDefaultAndVarargs, Checks.ValueParametersCount.atLeast(2),
            Checks.isKProperty,
            Checks.nonSuspend,
        )
        checkFor(
            OperatorNameConventions.SET_VALUE,
            Checks.memberOrExtension,
            Checks.noDefaultAndVarargs, Checks.ValueParametersCount.atLeast(3),
            Checks.isKProperty,
            Checks.nonSuspend,
        )
        checkFor(
            OperatorNameConventions.PROVIDE_DELEGATE,
            Checks.memberOrExtension,
            Checks.noDefaultAndVarargs, Checks.ValueParametersCount.exactly(2),
            Checks.isKProperty,
            Checks.nonSuspend,
        )
        checkFor(OperatorNameConventions.INVOKE, Checks.memberOrExtension)
        checkFor(
            OperatorNameConventions.CONTAINS,
            Checks.memberOrExtension, Checks.ValueParametersCount.single,
            Checks.noDefaultAndVarargs, Checks.Returns.boolean
        )
        checkFor(OperatorNameConventions.ITERATOR, Checks.memberOrExtension, Checks.ValueParametersCount.none)
        checkFor(OperatorNameConventions.NEXT, Checks.memberOrExtension, Checks.ValueParametersCount.none)
        checkFor(OperatorNameConventions.HAS_NEXT, Checks.memberOrExtension, Checks.ValueParametersCount.none, Checks.Returns.boolean)
        checkFor(OperatorNameConventions.RANGE_TO, Checks.memberOrExtension, Checks.ValueParametersCount.single, Checks.noDefaultAndVarargs)
        checkFor(
            OperatorNameConventions.RANGE_UNTIL,
            Checks.memberOrExtension, Checks.ValueParametersCount.single,
            Checks.noDefaultAndVarargs
        )
        checkFor(
            OperatorNameConventions.EQUALS,
            Checks.member,
            object : Check() {
                override fun check(function: FirSimpleFunction, session: FirSession, scopeSession: ScopeSession?): String? {
                    if (scopeSession == null) return null
                    konst containingClassSymbol = function.containingClassLookupTag()?.toFirRegularClassSymbol(session) ?: return null
                    konst customEqualsSupported = session.languageVersionSettings.supportsFeature(LanguageFeature.CustomEqualsInValueClasses)

                    if (function.symbol.overriddenFunctions(containingClassSymbol, session, scopeSession)
                            .any { it.containingClassLookupTag()?.classId == StandardClassIds.Any }
                        || (customEqualsSupported && function.isTypedEqualsInValueClass(session))
                    ) {
                        return null
                    }
                    return buildString {
                        append("must override ''equals()'' in Any")
                        if (customEqualsSupported && containingClassSymbol.isInline) {
                            konst expectedParameterTypeRendered =
                                containingClassSymbol.defaultType().replaceArgumentsWithStarProjections().renderReadable()
                            append(" or define ''equals(other: ${expectedParameterTypeRendered}): Boolean''")
                        }
                    }
                }
            }
        )
        checkFor(
            OperatorNameConventions.COMPARE_TO,
            Checks.memberOrExtension, Checks.Returns.int, Checks.ValueParametersCount.single,
            Checks.noDefaultAndVarargs
        )
        checkFor(
            OperatorNameConventions.BINARY_OPERATION_NAMES,
            Checks.memberOrExtension, Checks.ValueParametersCount.single,
            Checks.noDefaultAndVarargs
        )
        checkFor(OperatorNameConventions.SIMPLE_UNARY_OPERATION_NAMES, Checks.memberOrExtension, Checks.ValueParametersCount.none)
        checkFor(
            setOf(OperatorNameConventions.INC, OperatorNameConventions.DEC),
            Checks.memberOrExtension,
            Checks.full("receiver must be a supertype of the return type") { session, function ->
                konst receiver = function.dispatchReceiverType ?: function.receiverParameter?.typeRef?.coneType ?: return@full false
                function.returnTypeRef.coneType.isSubtypeOf(session.typeContext, receiver)
            }
        )
        checkFor(
            OperatorNameConventions.ASSIGNMENT_OPERATIONS,
            Checks.memberOrExtension, Checks.Returns.unit, Checks.ValueParametersCount.single,
            Checks.noDefaultAndVarargs
        )
    }

    private konst regexChecks: List<Pair<Regex, List<Check>>> = buildList {
        checkFor(OperatorNameConventions.COMPONENT_REGEX, Checks.memberOrExtension, Checks.ValueParametersCount.none)
    }

    private fun MutableMap<Name, List<Check>>.checkFor(name: Name, vararg checks: Check) {
        put(name, checks.asList())
    }

    private fun MutableMap<Name, List<Check>>.checkFor(names: Set<Name>, vararg checks: Check) {
        names.forEach { put(it, checks.asList()) }
    }

    private fun MutableList<Pair<Regex, List<Check>>>.checkFor(regex: Regex, vararg checks: Check) {
        add(regex to checks.asList())
    }
}

private abstract class Check {
    abstract fun check(function: FirSimpleFunction, session: FirSession, scopeSession: ScopeSession?): String?
}

private object Checks {
    fun simple(message: String, predicate: (FirSimpleFunction) -> Boolean) = object : Check() {
        override fun check(function: FirSimpleFunction, session: FirSession, scopeSession: ScopeSession?): String? =
            message.takeIf { !predicate(function) }
    }

    fun full(message: String, predicate: (FirSession, FirSimpleFunction) -> Boolean) = object : Check() {
        override fun check(function: FirSimpleFunction, session: FirSession, scopeSession: ScopeSession?): String? =
            message.takeIf { !predicate(session, function) }
    }

    konst memberOrExtension = simple("must be a member or an extension function") {
        it.dispatchReceiverType != null || it.receiverParameter != null
    }

    konst member = simple("must be a member function") {
        it.dispatchReceiverType != null
    }

    konst nonSuspend = simple("must not be suspend") {
        !it.isSuspend
    }

    object ValueParametersCount {
        fun atLeast(n: Int) = simple("must have at least $n konstue parameter" + (if (n > 1) "s" else "")) {
            it.konstueParameters.size >= n
        }

        fun exactly(n: Int) = simple("must have exactly $n konstue parameters") {
            it.konstueParameters.size == n
        }

        konst single = simple("must have a single konstue parameter") {
            it.konstueParameters.size == 1
        }

        konst none = simple("must have no konstue parameters") {
            it.konstueParameters.isEmpty()
        }
    }

    object Returns {
        konst boolean = simple("must return Boolean") {
            it.returnTypeRef.isBoolean
        }

        konst int = simple("must return Int") {
            it.returnTypeRef.isInt
        }

        konst unit = simple("must return Unit") {
            it.returnTypeRef.isUnit
        }
    }

    konst noDefaultAndVarargs = simple("should not have varargs or parameters with default konstues") {
        it.konstueParameters.all { param ->
            param.defaultValue == null && !param.isVararg
        }
    }

    private konst kPropertyType = ConeClassLikeTypeImpl(
        StandardClassIds.KProperty.toLookupTag(),
        arrayOf(ConeStarProjection),
        isNullable = false
    )

    konst isKProperty = full("second parameter must be of type KProperty<*> or its supertype") { session, function ->
        konst paramType = function.konstueParameters.getOrNull(1)?.returnTypeRef?.coneType ?: return@full false
        kPropertyType.isSubtypeOf(paramType, session, errorTypesEqualToAnything = true)
    }
}
