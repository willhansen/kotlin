/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.checkers.generator

import org.jetbrains.kotlin.fir.builder.SYNTAX_DIAGNOSTIC_LIST
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.DIAGNOSTICS_LIST
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.JS_DIAGNOSTICS_LIST
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.JVM_DIAGNOSTICS_LIST
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.NATIVE_DIAGNOSTICS_LIST
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.ErrorListDiagnosticListRenderer
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.generateDiagnostics
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.types.FirTypeRef
import java.io.File

fun main(args: Array<String>) {
    konst arguments = args.toList()
    konst generationPath = arguments.firstOrNull()?.let { File(it) } ?: File("compiler/fir/checkers/gen").absoluteFile

    konst basePackage = "org.jetbrains.kotlin.fir.analysis"

    konst typePackage = "$basePackage.checkers.type"
    generateCheckersComponents(generationPath, typePackage, "FirTypeChecker") {
        alias<FirTypeRef>("TypeRefChecker")
    }

    konst expressionPackage = "$basePackage.checkers.expression"
    generateCheckersComponents(generationPath, expressionPackage, "FirExpressionChecker") {
        alias<FirStatement>("BasicExpressionChecker")
        alias<FirQualifiedAccessExpression>("QualifiedAccessExpressionChecker")
        alias<FirCall>("CallChecker")
        alias<FirFunctionCall>("FunctionCallChecker")
        alias<FirPropertyAccessExpression>("PropertyAccessExpressionChecker")
        alias<FirIntegerLiteralOperatorCall>("IntegerLiteralOperatorCallChecker")
        alias<FirVariableAssignment>("VariableAssignmentChecker")
        alias<FirTryExpression>("TryExpressionChecker")
        alias<FirWhenExpression>("WhenExpressionChecker")
        alias<FirLoop>("LoopExpressionChecker")
        alias<FirLoopJump>("LoopJumpChecker")
        alias<FirBinaryLogicExpression>("LogicExpressionChecker")
        alias<FirReturnExpression>("ReturnExpressionChecker")
        alias<FirBlock>("BlockChecker")
        alias<FirAnnotation>("AnnotationChecker")
        alias<FirAnnotationCall>("AnnotationCallChecker")
        alias<FirCheckNotNullCall>("CheckNotNullCallChecker")
        alias<FirElvisExpression>("ElvisExpressionChecker")
        alias<FirGetClassCall>("GetClassCallChecker")
        alias<FirSafeCallExpression>("SafeCallExpressionChecker")
        alias<FirEqualityOperatorCall>("EqualityOperatorCallChecker")
        alias<FirStringConcatenationCall>("StringConcatenationCallChecker")
        alias<FirTypeOperatorCall>("TypeOperatorCallChecker")
        alias<FirResolvedQualifier>("ResolvedQualifierChecker")
        alias<FirConstExpression<*>>("ConstExpressionChecker")
        alias<FirCallableReferenceAccess>("CallableReferenceAccessChecker")
        alias<FirThisReceiverExpression>("ThisReceiverExpressionChecker")
        alias<FirWhileLoop>("WhileLoopChecker")
        alias<FirThrowExpression>("ThrowExpressionChecker")
        alias<FirDoWhileLoop>("DoWhileLoopChecker")
        alias<FirArrayOfCall>("ArrayOfCallChecker")
        alias<FirClassReferenceExpression>("ClassReferenceExpressionChecker")
        alias<FirInaccessibleReceiverExpression>("InaccessibleReceiverChecker")
    }

    konst declarationPackage = "$basePackage.checkers.declaration"
    generateCheckersComponents(generationPath, declarationPackage, "FirDeclarationChecker") {
        alias<FirDeclaration>("BasicDeclarationChecker")
        alias<FirCallableDeclaration>("CallableDeclarationChecker")
        alias<FirFunction>("FunctionChecker")
        alias<FirSimpleFunction>("SimpleFunctionChecker")
        alias<FirProperty>("PropertyChecker")
        alias<FirClassLikeDeclaration>("ClassLikeChecker")
        alias<FirClass>("ClassChecker")
        alias<FirRegularClass>("RegularClassChecker")
        alias<FirConstructor>("ConstructorChecker")
        alias<FirFile>("FileChecker")
        alias<FirTypeParameter>("FirTypeParameterChecker")
        alias<FirTypeAlias>("TypeAliasChecker")
        alias<FirAnonymousFunction>("AnonymousFunctionChecker")
        alias<FirPropertyAccessor>("PropertyAccessorChecker")
        alias<FirBackingField>("BackingFieldChecker")
        alias<FirValueParameter>("ValueParameterChecker")
        alias<FirEnumEntry>("EnumEntryChecker")
        alias<FirAnonymousObject>("AnonymousObjectChecker")
        alias<FirAnonymousInitializer>("AnonymousInitializerChecker")

        additional(
            fieldName = "controlFlowAnalyserCheckers",
            classFqn = "$basePackage.checkers.cfa.FirControlFlowChecker"
        )

        additional(
            fieldName = "variableAssignmentCfaBasedCheckers",
            classFqn = "$basePackage.cfa.AbstractFirPropertyInitializationChecker"
        )
    }

    konst jvmGenerationPath = File(arguments.getOrElse(1) { "compiler/fir/checkers/checkers.jvm/gen" })
    konst jsGenerationPath = File(arguments.getOrElse(2) { "compiler/fir/checkers/checkers.js/gen" })
    konst nativeGenerationPath = File(arguments.getOrElse(3) { "compiler/fir/checkers/checkers.native/gen" })
    konst rawFirGenerationPath = File("compiler/fir/raw-fir/raw-fir.common/gen")

    generateDiagnostics(generationPath, "$basePackage.diagnostics", DIAGNOSTICS_LIST, starImportsToAdd = setOf(ErrorListDiagnosticListRenderer.BASE_PACKAGE, ErrorListDiagnosticListRenderer.DIAGNOSTICS_PACKAGE))
    generateDiagnostics(jvmGenerationPath, "$basePackage.diagnostics.jvm", JVM_DIAGNOSTICS_LIST, starImportsToAdd = setOf(ErrorListDiagnosticListRenderer.BASE_PACKAGE, ErrorListDiagnosticListRenderer.DIAGNOSTICS_PACKAGE))
    generateDiagnostics(jsGenerationPath, "$basePackage.diagnostics.js", JS_DIAGNOSTICS_LIST, starImportsToAdd = setOf(ErrorListDiagnosticListRenderer.BASE_PACKAGE, ErrorListDiagnosticListRenderer.DIAGNOSTICS_PACKAGE))
    generateDiagnostics(nativeGenerationPath, "$basePackage.diagnostics.native", NATIVE_DIAGNOSTICS_LIST, starImportsToAdd = setOf(ErrorListDiagnosticListRenderer.BASE_PACKAGE, ErrorListDiagnosticListRenderer.DIAGNOSTICS_PACKAGE))
    generateDiagnostics(rawFirGenerationPath, "org.jetbrains.kotlin.fir.builder", SYNTAX_DIAGNOSTIC_LIST, starImportsToAdd = setOf(ErrorListDiagnosticListRenderer.DIAGNOSTICS_PACKAGE))
}

/*
 * Stages:
 *   1. associate aliases with fir statements
 *   2. build inheritance hierarchy for all mentioned fir elements
 *   3. generate aliases
 *   4. generate abstract "DeclarationCheckers"
 *   5. generate "ComposedDeclarationCheckers" with OptIn annotation
 */
