/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.lightTree.converter

import com.intellij.lang.LighterASTNode
import com.intellij.psi.TokenType
import com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.kotlin.ElementTypeUtils.getOperationSymbol
import org.jetbrains.kotlin.ElementTypeUtils.isExpression
import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.KtLightSourceElement
import org.jetbrains.kotlin.KtNodeTypes.*
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.builder.*
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirVariable
import org.jetbrains.kotlin.fir.declarations.builder.buildAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.builder.buildProperty
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameter
import org.jetbrains.kotlin.fir.declarations.impl.FirDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.diagnostics.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.expressions.impl.FirContractCallBlock
import org.jetbrains.kotlin.fir.expressions.impl.FirSingleExpressionBlock
import org.jetbrains.kotlin.fir.expressions.impl.buildSingleExpressionBlock
import org.jetbrains.kotlin.fir.lightTree.fir.ValueParameter
import org.jetbrains.kotlin.fir.lightTree.fir.WhenEntry
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.references.builder.buildErrorNamedReference
import org.jetbrains.kotlin.fir.references.builder.buildExplicitSuperReference
import org.jetbrains.kotlin.fir.references.builder.buildExplicitThisReference
import org.jetbrains.kotlin.fir.references.builder.buildSimpleNamedReference
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.lexer.KtTokens.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.psi.stubs.elements.KtConstantExpressionElementType
import org.jetbrains.kotlin.psi.stubs.elements.KtNameReferenceExpressionElementType
import org.jetbrains.kotlin.types.expressions.OperatorConventions
import org.jetbrains.kotlin.util.OperatorNameConventions

class ExpressionsConverter(
    session: FirSession,
    tree: FlyweightCapableTreeStructure<LighterASTNode>,
    private konst declarationsConverter: DeclarationsConverter,
    context: Context<LighterASTNode> = Context(),
) : BaseConverter(session, tree, context) {

    inline fun <reified R : FirElement> getAsFirExpression(expression: LighterASTNode?, errorReason: String = ""): R {
        konst converted = expression?.let { convertExpression(it, errorReason) }
        return converted as? R
            ?: buildErrorExpression(
                expression?.toFirSourceElement(),
                ConeSimpleDiagnostic(errorReason, DiagnosticKind.ExpressionExpected),
                converted,
            ) as R
    }

    /*****    EXPRESSIONS    *****/
    fun convertExpression(expression: LighterASTNode, errorReason: String): FirElement {
        return when (expression.tokenType) {
            LAMBDA_EXPRESSION -> convertLambdaExpression(expression)
            BINARY_EXPRESSION -> convertBinaryExpression(expression)
            BINARY_WITH_TYPE -> convertBinaryWithTypeRHSExpression(expression) {
                this.getOperationSymbol().toFirOperation()
            }
            IS_EXPRESSION -> convertBinaryWithTypeRHSExpression(expression) {
                if (this == "is") FirOperation.IS else FirOperation.NOT_IS
            }
            LABELED_EXPRESSION -> convertLabeledExpression(expression)
            PREFIX_EXPRESSION, POSTFIX_EXPRESSION -> convertUnaryExpression(expression)
            ANNOTATED_EXPRESSION -> convertAnnotatedExpression(expression)
            CLASS_LITERAL_EXPRESSION -> convertClassLiteralExpression(expression)
            CALLABLE_REFERENCE_EXPRESSION -> convertCallableReferenceExpression(expression)
            in QUALIFIED_ACCESS -> convertQualifiedExpression(expression)
            CALL_EXPRESSION -> convertCallExpression(expression)
            WHEN -> convertWhenExpression(expression)
            ARRAY_ACCESS_EXPRESSION -> convertArrayAccessExpression(expression)
            COLLECTION_LITERAL_EXPRESSION -> convertCollectionLiteralExpression(expression)
            STRING_TEMPLATE -> convertStringTemplate(expression)
            is KtConstantExpressionElementType -> convertConstantExpression(expression)
            REFERENCE_EXPRESSION -> convertSimpleNameExpression(expression)
            DO_WHILE -> convertDoWhile(expression)
            WHILE -> convertWhile(expression)
            FOR -> convertFor(expression)
            TRY -> convertTryExpression(expression)
            IF -> convertIfExpression(expression)
            BREAK, CONTINUE -> convertLoopJump(expression)
            RETURN -> convertReturn(expression)
            THROW -> convertThrow(expression)
            PARENTHESIZED -> {
                konst content = expression.getExpressionInParentheses()
                context.forwardLabelUsagePermission(expression, content)
                getAsFirExpression(content, "Empty parentheses")
            }
            PROPERTY_DELEGATE, INDICES, CONDITION, LOOP_RANGE ->
                getAsFirExpression(expression.getExpressionInParentheses(), errorReason)
            THIS_EXPRESSION -> convertThisExpression(expression)
            SUPER_EXPRESSION -> convertSuperExpression(expression)

            OBJECT_LITERAL -> declarationsConverter.convertObjectLiteral(expression)
            FUN -> declarationsConverter.convertFunctionDeclaration(expression)
            DESTRUCTURING_DECLARATION -> declarationsConverter.convertDestructingDeclaration(expression).toFirDestructingDeclaration(baseModuleData)
            else -> buildErrorExpression(null, ConeSimpleDiagnostic(errorReason, DiagnosticKind.ExpressionExpected))
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseFunctionLiteral
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitLambdaExpression
     */
    private fun convertLambdaExpression(lambdaExpression: LighterASTNode): FirExpression {
        konst konstueParameterList = mutableListOf<ValueParameter>()
        var block: LighterASTNode? = null
        var hasArrow = false

        konst functionSymbol = FirAnonymousFunctionSymbol()
        lambdaExpression.getChildNodesByType(FUNCTION_LITERAL).first().forEachChildren {
            when (it.tokenType) {
                VALUE_PARAMETER_LIST -> konstueParameterList += declarationsConverter.convertValueParameters(it, functionSymbol, ValueParameterDeclaration.LAMBDA)
                BLOCK -> block = it
                ARROW -> hasArrow = true
            }
        }

        konst expressionSource = lambdaExpression.toFirSourceElement()
        konst target: FirFunctionTarget
        konst anonymousFunction = buildAnonymousFunction {
            source = expressionSource
            moduleData = baseModuleData
            origin = FirDeclarationOrigin.Source
            returnTypeRef = implicitType
            receiverParameter = implicitType.asReceiverParameter()
            symbol = functionSymbol
            isLambda = true
            hasExplicitParameterList = hasArrow
            label = context.getLastLabel(lambdaExpression) ?: context.calleeNamesForLambda.lastOrNull()?.let {
                buildLabel {
                    source = expressionSource.fakeElement(KtFakeSourceElementKind.GeneratedLambdaLabel)
                    name = it.asString()
                }
            }
            target = FirFunctionTarget(labelName = label?.name, isLambda = true)
            context.firFunctionTargets += target
            konst destructuringStatements = mutableListOf<FirStatement>()
            for (konstueParameter in konstueParameterList) {
                konst multiDeclaration = konstueParameter.destructuringDeclaration
                konstueParameters += if (multiDeclaration != null) {
                    konst name = SpecialNames.DESTRUCT
                    konst multiParameter = buildValueParameter {
                        source = konstueParameter.firValueParameter.source
                        containingFunctionSymbol = functionSymbol
                        moduleData = baseModuleData
                        origin = FirDeclarationOrigin.Source
                        returnTypeRef = konstueParameter.firValueParameter.returnTypeRef
                        this.name = name
                        symbol = FirValueParameterSymbol(name)
                        defaultValue = null
                        isCrossinline = false
                        isNoinline = false
                        isVararg = false
                    }
                    destructuringStatements += generateDestructuringBlock(
                        baseModuleData,
                        multiDeclaration,
                        multiParameter,
                        tmpVariable = false
                    ).statements
                    multiParameter
                } else {
                    konstueParameter.firValueParameter
                }
            }

            body = if (block != null) {
                declarationsConverter.convertBlockExpressionWithoutBuilding(block!!).apply {
                    statements.firstOrNull()?.let {
                        if (it.isContractBlockFirCheck()) {
                            this@buildAnonymousFunction.contractDescription = it.toLegacyRawContractDescription()
                            statements[0] = FirContractCallBlock(it)
                        }
                    }

                    if (statements.isEmpty()) {
                        statements.add(
                            buildReturnExpression {
                                source = expressionSource.fakeElement(KtFakeSourceElementKind.ImplicitReturn.FromExpressionBody)
                                this.target = target
                                result = buildUnitExpression {
                                    source = expressionSource.fakeElement(KtFakeSourceElementKind.ImplicitUnit)
                                }
                            }
                        )
                    }
                    statements.addAll(0, destructuringStatements)
                }.build()
            } else {
                buildSingleExpressionBlock(buildErrorExpression(null, ConeSyntaxDiagnostic("Lambda has no body")))
            }
            context.firFunctionTargets.removeLast()
        }.also {
            target.bind(it)
        }
        return buildAnonymousFunctionExpression {
            source = expressionSource
            this.anonymousFunction = anonymousFunction
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseBinaryExpression
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitBinaryExpression
     */
    private fun convertBinaryExpression(binaryExpression: LighterASTNode): FirStatement {
        var isLeftArgument = true
        lateinit var operationTokenName: String
        var leftArgNode: LighterASTNode? = null
        var rightArg: LighterASTNode? = null
        var operationReferenceSource: KtLightSourceElement? = null
        binaryExpression.forEachChildren {
            when (it.tokenType) {
                OPERATION_REFERENCE -> {
                    isLeftArgument = false
                    operationTokenName = it.asText
                    operationReferenceSource = it.toFirSourceElement()
                }
                else -> if (it.isExpression()) {
                    if (isLeftArgument) {
                        leftArgNode = it
                    } else {
                        rightArg = it
                    }
                }
            }
        }

        konst baseSource = binaryExpression.toFirSourceElement()
        konst operationToken = operationTokenName.getOperationSymbol()
        if (operationToken == IDENTIFIER) {
            context.calleeNamesForLambda += operationTokenName.nameAsSafeName()
        } else {
            context.calleeNamesForLambda += null
        }

        konst rightArgAsFir =
            if (rightArg != null)
                getAsFirExpression<FirExpression>(rightArg, "No right operand")
            else
                buildErrorExpression(null, ConeSyntaxDiagnostic("No right operand"))

        konst leftArgAsFir = getAsFirExpression<FirExpression>(leftArgNode, "No left operand")

        // No need for the callee name since arguments are already generated
        context.calleeNamesForLambda.removeLast()

        when (operationToken) {
            ELVIS ->
                return leftArgAsFir.generateNotNullOrOther(rightArgAsFir, baseSource)
            ANDAND, OROR ->
                return leftArgAsFir.generateLazyLogicalOperation(rightArgAsFir, operationToken == ANDAND, baseSource)
            in OperatorConventions.IN_OPERATIONS ->
                return rightArgAsFir.generateContainsOperation(leftArgAsFir, operationToken == NOT_IN, baseSource, operationReferenceSource)
            in OperatorConventions.COMPARISON_OPERATIONS ->
                return leftArgAsFir.generateComparisonExpression(rightArgAsFir, operationToken, baseSource, operationReferenceSource)
        }
        konst conventionCallName = operationToken.toBinaryName()
        return if (conventionCallName != null || operationToken == IDENTIFIER) {
            buildFunctionCall {
                source = binaryExpression.toFirSourceElement()
                calleeReference = buildSimpleNamedReference {
                    source = operationReferenceSource ?: this@buildFunctionCall.source
                    name = conventionCallName ?: operationTokenName.nameAsSafeName()
                }
                explicitReceiver = leftArgAsFir
                argumentList = buildUnaryArgumentList(rightArgAsFir)
                origin = if (conventionCallName != null) FirFunctionCallOrigin.Operator else FirFunctionCallOrigin.Infix
            }
        } else {
            konst firOperation = operationToken.toFirOperation()
            if (firOperation in FirOperation.ASSIGNMENTS) {
                return leftArgNode.generateAssignment(
                    binaryExpression.toFirSourceElement(),
                    leftArgNode?.toFirSourceElement(),
                    rightArgAsFir,
                    firOperation,
                    leftArgAsFir.annotations,
                    rightArg,
                ) { getAsFirExpression(this) }
            } else {
                buildEqualityOperatorCall {
                    source = binaryExpression.toFirSourceElement()
                    operation = firOperation
                    argumentList = buildBinaryArgumentList(leftArgAsFir, rightArgAsFir)
                }
            }
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.Precedence.parseRightHandSide
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitBinaryWithTypeRHSExpression
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitIsExpression
     */
    private fun convertBinaryWithTypeRHSExpression(
        binaryExpression: LighterASTNode,
        toFirOperation: String.() -> FirOperation
    ): FirTypeOperatorCall {
        lateinit var operationTokenName: String
        var leftArgAsFir: FirExpression? = null
        lateinit var firType: FirTypeRef
        binaryExpression.forEachChildren {
            when (it.tokenType) {
                OPERATION_REFERENCE -> operationTokenName = it.asText
                TYPE_REFERENCE -> firType = declarationsConverter.convertType(it)
                else -> if (it.isExpression()) leftArgAsFir = getAsFirExpression(it, "No left operand")
            }
        }

        return buildTypeOperatorCall {
            source = binaryExpression.toFirSourceElement()
            operation = operationTokenName.toFirOperation()
            conversionTypeRef = firType
            argumentList = buildUnaryArgumentList(
                leftArgAsFir ?: buildErrorExpression(null, ConeSyntaxDiagnostic("No left operand"))
            )
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseLabeledExpression
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitLabeledExpression
     */
    private fun convertLabeledExpression(labeledExpression: LighterASTNode): FirElement {
        var firExpression: FirElement? = null
        var errorLabelSource: KtSourceElement? = null

        labeledExpression.forEachChildren {
            context.setNewLabelUserNode(it)
            when (it.tokenType) {
                LABEL_QUALIFIER -> {
                    konst rawName = it.toString()
                    konst pair = buildLabelAndErrorSource(
                        rawName.substring(0, rawName.length - 1),
                        it.getChildNodesByType(LABEL).single().toFirSourceElement()
                    )
                    context.addNewLabel(pair.first)
                    errorLabelSource = pair.second
                }
                BLOCK -> firExpression = declarationsConverter.convertBlock(it)
                PROPERTY -> firExpression = declarationsConverter.convertPropertyDeclaration(it)
                else -> if (it.isExpression()) firExpression = getAsFirExpression(it)
            }
        }

        context.dropLastLabel()

        return buildExpressionWithErrorLabel(firExpression, errorLabelSource, labeledExpression.toFirSourceElement())
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parsePostfixExpression
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parsePrefixExpression
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitUnaryExpression
     */
    private fun convertUnaryExpression(unaryExpression: LighterASTNode): FirExpression {
        lateinit var operationTokenName: String
        var argument: LighterASTNode? = null
        var operationReference: LighterASTNode? = null
        unaryExpression.forEachChildren {
            when (it.tokenType) {
                OPERATION_REFERENCE -> {
                    operationReference = it
                    operationTokenName = it.asText
                }
                else -> if (it.isExpression()) argument = it
            }
        }

        konst operationToken = operationTokenName.getOperationSymbol()
        konst conventionCallName = operationToken.toUnaryName()
        return when {
            operationToken == EXCLEXCL -> {
                buildCheckNotNullCall {
                    source = unaryExpression.toFirSourceElement()
                    argumentList = buildUnaryArgumentList(getAsFirExpression<FirExpression>(argument, "No operand"))
                }

            }
            conventionCallName != null -> {
                if (operationToken in OperatorConventions.INCREMENT_OPERATIONS) {
                    return generateIncrementOrDecrementBlock(
                        unaryExpression,
                        operationReference,
                        argument,
                        callName = conventionCallName,
                        prefix = unaryExpression.tokenType == PREFIX_EXPRESSION
                    ) { getAsFirExpression(this) }
                }
                konst receiver = getAsFirExpression<FirExpression>(argument, "No operand")
                convertUnaryPlusMinusCallOnIntegerLiteralIfNecessary(unaryExpression, receiver, operationToken)?.let { return it }
                buildFunctionCall {
                    source = unaryExpression.toFirSourceElement()
                    calleeReference = buildSimpleNamedReference {
                        source = operationReference?.toFirSourceElement() ?: this@buildFunctionCall.source
                        name = conventionCallName
                    }
                    explicitReceiver = receiver
                    origin = FirFunctionCallOrigin.Operator
                }
            }
            else -> throw IllegalStateException("Unexpected expression: ${unaryExpression.asText}")
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parsePrefixExpression
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitAnnotatedExpression
     */
    private fun convertAnnotatedExpression(annotatedExpression: LighterASTNode): FirElement {
        var firExpression: FirElement? = null
        konst firAnnotationList = mutableListOf<FirAnnotation>()
        annotatedExpression.forEachChildren {
            when (it.tokenType) {
                ANNOTATION -> firAnnotationList += declarationsConverter.convertAnnotation(it)
                ANNOTATION_ENTRY -> firAnnotationList += declarationsConverter.convertAnnotationEntry(it)
                BLOCK -> firExpression = declarationsConverter.convertBlockExpression(it)
                else -> if (it.isExpression()) {
                    context.forwardLabelUsagePermission(annotatedExpression, it)
                    firExpression = getAsFirExpression(it)
                }
            }
        }

        konst result = firExpression ?: buildErrorExpression(null, ConeNotAnnotationContainer("???"))
        require(result is FirAnnotationContainer)
        result.replaceAnnotations(result.annotations.smartPlus(firAnnotationList))
        return result
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseDoubleColonSuffix
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitClassLiteralExpression
     */
    private fun convertClassLiteralExpression(classLiteralExpression: LighterASTNode): FirExpression {
        var firReceiverExpression: FirExpression? = null
        classLiteralExpression.forEachChildren {
            if (it.isExpression()) firReceiverExpression = getAsFirExpression(it, "No receiver in class literal")
        }

        return buildGetClassCall {
            source = classLiteralExpression.toFirSourceElement()
            argumentList = buildUnaryArgumentList(
                firReceiverExpression
                    ?: buildErrorExpression(null, ConeSyntaxDiagnostic("No receiver in class literal"))
            )
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseDoubleColonSuffix
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitCallableReferenceExpression
     */
    private fun convertCallableReferenceExpression(callableReferenceExpression: LighterASTNode): FirExpression {
        var isReceiver = true
        var hasQuestionMarkAtLHS = false
        var firReceiverExpression: FirExpression? = null
        lateinit var namedReference: FirNamedReference
        callableReferenceExpression.forEachChildren {
            when (it.tokenType) {
                COLONCOLON -> isReceiver = false
                QUEST -> hasQuestionMarkAtLHS = true
                else -> if (it.isExpression()) {
                    if (isReceiver) {
                        firReceiverExpression = getAsFirExpression(it, "Incorrect receiver expression")
                    } else {
                        namedReference = createSimpleNamedReference(it.toFirSourceElement(), it)
                    }
                }
            }
        }

        return buildCallableReferenceAccess {
            source = callableReferenceExpression.toFirSourceElement()
            calleeReference = namedReference
            explicitReceiver = firReceiverExpression
            this.hasQuestionMarkAtLHS = hasQuestionMarkAtLHS
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parsePostfixExpression
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitQualifiedExpression
     */
    private fun convertQualifiedExpression(dotQualifiedExpression: LighterASTNode): FirExpression {
        var isSelector = false
        var isSafe = false
        var firSelector: FirExpression? = null
        var firReceiver: FirExpression? = null //before dot
        dotQualifiedExpression.forEachChildren {
            when (konst tokenType = it.tokenType) {
                DOT -> isSelector = true
                SAFE_ACCESS -> {
                    isSafe = true
                    isSelector = true
                }
                else -> {
                    konst isEffectiveSelector = isSelector && tokenType != TokenType.ERROR_ELEMENT
                    konst firExpression =
                        getAsFirExpression<FirExpression>(it, "Incorrect ${if (isEffectiveSelector) "selector" else "receiver"} expression")
                    if (isEffectiveSelector) {
                        konst callExpressionCallee = if (tokenType == CALL_EXPRESSION) it.getFirstChildExpressionUnwrapped() else null
                        firSelector =
                            if (tokenType is KtNameReferenceExpressionElementType ||
                                (tokenType == CALL_EXPRESSION && callExpressionCallee?.tokenType != LAMBDA_EXPRESSION)
                            ) {
                                firExpression
                            } else {
                                buildErrorExpression {
                                    source = callExpressionCallee?.toFirSourceElement() ?: it.toFirSourceElement()
                                    diagnostic = ConeSimpleDiagnostic(
                                        "The expression cannot be a selector (occur after a dot)",
                                        if (callExpressionCallee == null) DiagnosticKind.IllegalSelector else DiagnosticKind.NoReceiverAllowed
                                    )
                                    expression = firExpression
                                }
                            }
                    } else {
                        firReceiver = firExpression
                    }
                }
            }
        }

        var result = firSelector
        (firSelector as? FirQualifiedAccessExpression)?.let {
            if (isSafe) {
                @OptIn(FirImplementationDetail::class)
                it.replaceSource(dotQualifiedExpression.toFirSourceElement(KtFakeSourceElementKind.DesugaredSafeCallExpression))
                return it.createSafeCall(
                    firReceiver!!,
                    dotQualifiedExpression.toFirSourceElement()
                )
            }

            result = convertFirSelector(it, dotQualifiedExpression.toFirSourceElement(), firReceiver!!)
        }

        konst receiver = firReceiver
        if (receiver != null) {
            (firSelector as? FirErrorExpression)?.let { errorExpression ->
                return buildQualifiedErrorAccessExpression {
                    this.receiver = receiver
                    this.selector = errorExpression
                    source = dotQualifiedExpression.toFirSourceElement()
                    diagnostic = ConeSyntaxDiagnostic("Qualified expression with unexpected selector")
                }
            }
        }

        return result ?: buildErrorExpression {
            source = null
            diagnostic = ConeSyntaxDiagnostic("Qualified expression without selector")

            // if there is no selector, we still want to resolve the receiver
            expression = firReceiver
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseCallSuffix
     */
    private fun convertCallExpression(callSuffix: LighterASTNode): FirExpression {
        var name: String? = null
        konst firTypeArguments = mutableListOf<FirTypeProjection>()
        konst konstueArguments = mutableListOf<LighterASTNode>()
        var additionalArgument: FirExpression? = null
        var hasArguments = false
        var superNode: LighterASTNode? = null
        callSuffix.forEachChildren { child ->
            fun process(node: LighterASTNode) {
                when (node.tokenType) {
                    REFERENCE_EXPRESSION -> {
                        name = node.asText
                    }
                    SUPER_EXPRESSION -> {
                        superNode = node
                    }
                    PARENTHESIZED -> node.getExpressionInParentheses()?.let { process(it) } ?: run {
                        additionalArgument = getAsFirExpression(node, "Incorrect invoke receiver")
                    }
                    TYPE_ARGUMENT_LIST -> {
                        firTypeArguments += declarationsConverter.convertTypeArguments(node, allowedUnderscoredTypeArgument = true)
                    }
                    VALUE_ARGUMENT_LIST, LAMBDA_ARGUMENT -> {
                        hasArguments = true
                        konstueArguments += node
                    }
                    else -> if (node.tokenType != TokenType.ERROR_ELEMENT) {
                        additionalArgument = getAsFirExpression(node, "Incorrect invoke receiver")
                    }
                }
            }

            process(child)
        }

        konst source = callSuffix.toFirSourceElement()

        konst (calleeReference, explicitReceiver, isImplicitInvoke) = when {
            name != null -> CalleeAndReceiver(
                buildSimpleNamedReference {
                    this.source = callSuffix.getFirstChildExpressionUnwrapped()?.toFirSourceElement() ?: source
                    this.name = name.nameAsSafeName()
                }
            )

            additionalArgument != null -> {
                CalleeAndReceiver(
                    buildSimpleNamedReference {
                        this.source = source
                        this.name = OperatorNameConventions.INVOKE
                    },
                    additionalArgument!!,
                    isImplicitInvoke = true
                )
            }

            superNode != null -> {
                CalleeAndReceiver(
                    buildErrorNamedReference {
                        konst node = superNode!!
                        this.source = node.toFirSourceElement()
                        diagnostic = ConeSimpleDiagnostic("Super cannot be a callee", DiagnosticKind.SuperNotAllowed)
                    }
                )
            }

            else -> CalleeAndReceiver(
                buildErrorNamedReference {
                    this.source = source
                    diagnostic = ConeSyntaxDiagnostic("Call has no callee")
                }
            )
        }

        konst builder: FirQualifiedAccessExpressionBuilder = if (hasArguments) {
            konst builder = if (isImplicitInvoke) FirImplicitInvokeCallBuilder() else FirFunctionCallBuilder()
            builder.apply {
                this.source = source
                this.calleeReference = calleeReference

                context.calleeNamesForLambda += calleeReference.name
                this.extractArgumentsFrom(konstueArguments.flatMap { convertValueArguments(it) })
                context.calleeNamesForLambda.removeLast()
            }
        } else {
            FirPropertyAccessExpressionBuilder().apply {
                this.source = source
                this.calleeReference = calleeReference
            }
        }
        return builder.apply {
            this.explicitReceiver = explicitReceiver
            typeArguments += firTypeArguments
        }.build()
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseStringTemplate
     */
    private fun convertStringTemplate(stringTemplate: LighterASTNode): FirExpression {
        return stringTemplate.getChildrenAsArray().toInterpolatingCall(stringTemplate) { convertShortOrLongStringTemplate(it) }
    }

    private fun LighterASTNode?.convertShortOrLongStringTemplate(errorReason: String): FirExpression {
        var firExpression: FirExpression? = null
        this?.forEachChildren(LONG_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_END) {
            firExpression = getAsFirExpression(it, errorReason)
        }
        return firExpression ?: buildErrorExpression(null, ConeSyntaxDiagnostic(errorReason))
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseLiteralConstant
     */
    private fun convertConstantExpression(constantExpression: LighterASTNode): FirExpression {
        return generateConstantExpressionByLiteral(constantExpression)
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseWhen
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitWhenExpression
     */
    private fun convertWhenExpression(whenExpression: LighterASTNode): FirExpression {
        var subjectExpression: FirExpression? = null
        var subjectVariable: FirVariable? = null
        konst whenEntryNodes = mutableListOf<LighterASTNode>()
        konst whenEntries = mutableListOf<WhenEntry>()
        whenExpression.forEachChildren {
            when (it.tokenType) {
                PROPERTY -> subjectVariable = (declarationsConverter.convertPropertyDeclaration(it) as FirVariable).let { variable ->
                    buildProperty {
                        source = it.toFirSourceElement()
                        origin = FirDeclarationOrigin.Source
                        moduleData = baseModuleData
                        returnTypeRef = variable.returnTypeRef
                        name = variable.name
                        initializer = variable.initializer
                        isVar = false
                        symbol = FirPropertySymbol(variable.name)
                        isLocal = true
                        status = FirDeclarationStatusImpl(Visibilities.Local, Modality.FINAL)
                    }
                }
                DESTRUCTURING_DECLARATION -> subjectExpression =
                    getAsFirExpression(it, "Incorrect when subject expression: ${whenExpression.asText}")
                WHEN_ENTRY -> whenEntryNodes += it
                else -> if (it.isExpression()) subjectExpression =
                    getAsFirExpression(it, "Incorrect when subject expression: ${whenExpression.asText}")
            }
        }
        subjectExpression = subjectVariable?.initializer ?: subjectExpression
        konst hasSubject = subjectExpression != null

        @OptIn(FirContractViolation::class)
        konst subject = FirExpressionRef<FirWhenExpression>()
        whenEntryNodes.mapTo(whenEntries) {
            convertWhenEntry(
                it,
                subject.takeIf { hasSubject }
            )
        }
        return buildWhenExpression {
            source = whenExpression.toFirSourceElement()
            this.subject = subjectExpression
            this.subjectVariable = subjectVariable
            usedAsExpression = whenExpression.usedAsExpression
            for (entry in whenEntries) {
                konst branch = entry.firBlock
                konst entrySource = entry.node.toFirSourceElement()
                branches += if (!entry.isElse) {
                    if (hasSubject) {
                        konst firCondition = entry.toFirWhenCondition()
                        buildWhenBranch {
                            source = entrySource
                            condition = firCondition
                            result = branch
                        }
                    } else {
                        konst firCondition = entry.toFirWhenConditionWithoutSubject()
                        buildWhenBranch {
                            source = entrySource
                            condition = firCondition
                            result = branch
                        }
                    }
                } else {
                    buildWhenBranch {
                        source = entrySource
                        condition = buildElseIfTrueCondition()
                        result = branch
                    }
                }
            }
        }.also {
            if (hasSubject) {
                subject.bind(it)
            }
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseWhenEntry
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseWhenEntryNotElse
     */
    private fun convertWhenEntry(
        whenEntry: LighterASTNode,
        whenRefWithSubject: FirExpressionRef<FirWhenExpression>?
    ): WhenEntry {
        var isElse = false
        var firBlock: FirBlock = buildEmptyExpressionBlock()
        konst conditions = mutableListOf<FirExpression>()
        whenEntry.forEachChildren {
            when (it.tokenType) {
                WHEN_CONDITION_EXPRESSION -> conditions += convertWhenConditionExpression(it, whenRefWithSubject)
                WHEN_CONDITION_IN_RANGE -> conditions += convertWhenConditionInRange(it, whenRefWithSubject)
                WHEN_CONDITION_IS_PATTERN -> conditions += convertWhenConditionIsPattern(it, whenRefWithSubject)
                ELSE_KEYWORD -> isElse = true
                BLOCK -> firBlock = declarationsConverter.convertBlock(it)
                else -> if (it.isExpression()) firBlock = declarationsConverter.convertBlock(it)
            }
        }

        return WhenEntry(conditions, firBlock, whenEntry, isElse)
    }

    private fun convertWhenConditionExpression(
        whenCondition: LighterASTNode,
        whenRefWithSubject: FirExpressionRef<FirWhenExpression>?
    ): FirExpression {
        var firExpression: FirExpression? = null
        whenCondition.forEachChildren {
            when (it.tokenType) {
                else -> if (it.isExpression()) firExpression = getAsFirExpression(it, "No expression in condition with expression")
            }
        }

        konst calculatedFirExpression = firExpression ?: buildErrorExpression(
            source = null,
            ConeSyntaxDiagnostic("No expression in condition with expression")
        )

        if (whenRefWithSubject == null) {
            return calculatedFirExpression
        }

        return buildEqualityOperatorCall {
            source = whenCondition.toFirSourceElement(KtFakeSourceElementKind.WhenCondition)
            operation = FirOperation.EQ
            argumentList = buildBinaryArgumentList(
                left = buildWhenSubjectExpression {
                    source = whenCondition.toFirSourceElement()
                    whenRef = whenRefWithSubject
                },
                right = calculatedFirExpression
            )
        }
    }

    private fun convertWhenConditionInRange(
        whenCondition: LighterASTNode,
        whenRefWithSubject: FirExpressionRef<FirWhenExpression>?
    ): FirExpression {
        var isNegate = false
        var firExpression: FirExpression? = null
        var conditionSource: KtLightSourceElement? = null
        whenCondition.forEachChildren {
            when {
                it.tokenType == OPERATION_REFERENCE && it.asText == NOT_IN.konstue -> {
                    conditionSource = it.toFirSourceElement()
                    isNegate = true
                }
                it.tokenType == OPERATION_REFERENCE -> {
                    conditionSource = it.toFirSourceElement()
                }
                else -> if (it.isExpression()) firExpression = getAsFirExpression(it, "No range in condition with range")
            }
        }

        konst subjectExpression = if (whenRefWithSubject != null) {
            buildWhenSubjectExpression {
                whenRef = whenRefWithSubject
                source = whenCondition.toFirSourceElement()
            }
        } else {
            return buildErrorExpression {
                source = whenCondition.toFirSourceElement()
                diagnostic = ConeSimpleDiagnostic(
                    "No expression in condition with expression",
                    DiagnosticKind.ExpressionExpected
                )
            }
        }

        konst calculatedFirExpression = firExpression ?: buildErrorExpression(
            null,
            ConeSyntaxDiagnostic("No range in condition with range")
        )

        return calculatedFirExpression.generateContainsOperation(
            subjectExpression,
            inverted = isNegate,
            baseSource = whenCondition.toFirSourceElement(),
            operationReferenceSource = conditionSource
        )
    }

    private fun convertWhenConditionIsPattern(
        whenCondition: LighterASTNode,
        whenRefWithSubject: FirExpressionRef<FirWhenExpression>?
    ): FirExpression {
        lateinit var firOperation: FirOperation
        lateinit var firType: FirTypeRef
        whenCondition.forEachChildren {
            when (it.tokenType) {
                TYPE_REFERENCE -> firType = declarationsConverter.convertType(it)
                IS_KEYWORD -> firOperation = FirOperation.IS
                NOT_IS -> firOperation = FirOperation.NOT_IS
            }
        }

        konst subjectExpression = if (whenRefWithSubject != null) {
            buildWhenSubjectExpression {
                source = whenCondition.toFirSourceElement()
                whenRef = whenRefWithSubject
            }
        } else {
            return buildErrorExpression {
                source = whenCondition.toFirSourceElement()
                diagnostic = ConeSimpleDiagnostic(
                    "No expression in condition with expression",
                    DiagnosticKind.ExpressionExpected
                )
            }
        }

        return buildTypeOperatorCall {
            source = whenCondition.toFirSourceElement()
            operation = firOperation
            conversionTypeRef = firType
            argumentList = buildUnaryArgumentList(subjectExpression)
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseArrayAccess
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitArrayAccessExpression
     */
    private fun convertArrayAccessExpression(arrayAccess: LighterASTNode): FirExpression {
        var firExpression: FirExpression? = null
        konst indices: MutableList<FirExpression> = mutableListOf()
        arrayAccess.forEachChildren {
            when (it.tokenType) {
                INDICES -> indices += convertIndices(it)
                else -> if (it.isExpression()) firExpression = getAsFirExpression(it, "No array expression")
            }
        }
        konst getArgument = context.arraySetArgument.remove(arrayAccess)
        return buildFunctionCall {
            konst isGet = getArgument == null
            source = (if (isGet) arrayAccess else arrayAccess.getParent()!!).toFirSourceElement()
            calleeReference = buildSimpleNamedReference {
                source = arrayAccess.toFirSourceElement().fakeElement(KtFakeSourceElementKind.ArrayAccessNameReference)
                name = if (isGet) OperatorNameConventions.GET else OperatorNameConventions.SET
            }
            explicitReceiver =
                firExpression ?: buildErrorExpression(null, ConeSyntaxDiagnostic("No array expression"))
            argumentList = buildArgumentList {
                arguments += indices
                getArgument?.let { arguments += it }
            }
            origin = FirFunctionCallOrigin.Operator
        }.pullUpSafeCallIfNecessary()
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseCollectionLiteralExpression
     */
    private fun convertCollectionLiteralExpression(expression: LighterASTNode): FirExpression {
        konst firExpressionList = mutableListOf<FirExpression>()
        expression.forEachChildren {
            if (it.isExpression()) firExpressionList += getAsFirExpression<FirExpression>(it, "Incorrect collection literal argument")
        }

        return buildArrayOfCall {
            source = expression.toFirSourceElement()
            argumentList = buildArgumentList {
                arguments += firExpressionList
            }
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseAsCollectionLiteralExpression
     */
    private fun convertIndices(indices: LighterASTNode): List<FirExpression> {
        konst firExpressionList: MutableList<FirExpression> = mutableListOf()
        indices.forEachChildren {
            if (it.isExpression()) firExpressionList += getAsFirExpression<FirExpression>(it, "Incorrect index expression")
        }

        return firExpressionList
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseSimpleNameExpression
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitSimpleNameExpression
     */
    private fun convertSimpleNameExpression(referenceExpression: LighterASTNode): FirQualifiedAccessExpression {
        konst nameSource = referenceExpression.toFirSourceElement()
        konst referenceSourceElement = if (nameSource.kind is KtFakeSourceElementKind) {
            nameSource
        } else {
            nameSource.fakeElement(KtFakeSourceElementKind.ReferenceInAtomicQualifiedAccess)
        }
        return buildPropertyAccessExpression {
            konst rawText = referenceExpression.asText
            if (rawText.isUnderscore) {
                nonFatalDiagnostics.add(ConeUnderscoreUsageWithoutBackticks(nameSource))
            }
            source = nameSource
            calleeReference = createSimpleNamedReference(referenceSourceElement, referenceExpression)
        }
    }

    private fun createSimpleNamedReference(
        sourceElement: KtSourceElement,
        referenceExpression: LighterASTNode
    ): FirNamedReference {
        return buildSimpleNamedReference {
            source = sourceElement
            name = referenceExpression.asText.nameAsSafeName()
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseDoWhile
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitDoWhileExpression
     */
    private fun convertDoWhile(doWhileLoop: LighterASTNode): FirElement {
        var block: LighterASTNode? = null
        var firCondition: FirExpression? = null

        konst target: FirLoopTarget
        return FirDoWhileLoopBuilder().apply {
            source = doWhileLoop.toFirSourceElement()
            // For break/continue in the do-while loop condition, prepare the loop target first so that it can refer to the same loop.
            target = prepareTarget(doWhileLoop)
            doWhileLoop.forEachChildren {
                when (it.tokenType) {
                    BODY -> block = it
                    CONDITION -> firCondition = getAsFirExpression(it, "No condition in do-while loop")
                }
            }
            condition =
                firCondition ?: buildErrorExpression(null, ConeSyntaxDiagnostic("No condition in do-while loop"))
        }.configure(target) { convertLoopBody(block) }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseWhile
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitWhileExpression
     */
    private fun convertWhile(whileLoop: LighterASTNode): FirElement {
        var block: LighterASTNode? = null
        var firCondition: FirExpression? = null
        whileLoop.forEachChildren {
            when (it.tokenType) {
                BODY -> block = it
                CONDITION -> firCondition = getAsFirExpression(it, "No condition in while loop")
            }
        }

        konst target: FirLoopTarget
        return FirWhileLoopBuilder().apply {
            source = whileLoop.toFirSourceElement()
            condition =
                firCondition ?: buildErrorExpression(null, ConeSyntaxDiagnostic("No condition in while loop"))
            // break/continue in the while loop condition will refer to an outer loop if any.
            // So, prepare the loop target after building the condition.
            target = prepareTarget(whileLoop)
        }.configure(target) { convertLoopBody(block) }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseFor
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitForExpression
     */
    private fun convertFor(forLoop: LighterASTNode): FirElement {
        var parameter: ValueParameter? = null
        var rangeExpression: FirExpression? = null
        var blockNode: LighterASTNode? = null
        forLoop.forEachChildren {
            when (it.tokenType) {
                VALUE_PARAMETER -> parameter = declarationsConverter.convertValueParameter(it, null, ValueParameterDeclaration.FOR_LOOP)
                LOOP_RANGE -> rangeExpression = getAsFirExpression(it, "No range in for loop")
                BODY -> blockNode = it
            }
        }

        konst calculatedRangeExpression =
            rangeExpression ?: buildErrorExpression(null, ConeSyntaxDiagnostic("No range in for loop"))
        konst fakeSource = forLoop.toFirSourceElement(KtFakeSourceElementKind.DesugaredForLoop)
        konst target: FirLoopTarget
        // NB: FirForLoopChecker relies on this block existence and structure
        return buildBlock {
            source = fakeSource
            konst iteratorVal = generateTemporaryVariable(
                baseModuleData,
                calculatedRangeExpression.source?.fakeElement(KtFakeSourceElementKind.DesugaredForLoop),
                SpecialNames.ITERATOR,
                buildFunctionCall {
                    source = fakeSource
                    calleeReference = buildSimpleNamedReference {
                        source = fakeSource
                        name = OperatorNameConventions.ITERATOR
                    }
                    explicitReceiver = calculatedRangeExpression
                }
            )
            statements += iteratorVal
            statements += FirWhileLoopBuilder().apply {
                source = fakeSource
                condition = buildFunctionCall {
                    source = fakeSource
                    calleeReference = buildSimpleNamedReference {
                        source = fakeSource
                        name = OperatorNameConventions.HAS_NEXT
                    }
                    explicitReceiver = generateResolvedAccessExpression(fakeSource, iteratorVal)
                }
                // break/continue in the for loop condition will refer to an outer loop if any.
                // So, prepare the loop target after building the condition.
                target = prepareTarget(forLoop)
            }.configure(target) {
                buildBlock block@{
                    source = blockNode?.toFirSourceElement()
                    konst konstueParameter = parameter ?: return@block
                    konst multiDeclaration = konstueParameter.destructuringDeclaration
                    konst firLoopParameter = generateTemporaryVariable(
                        baseModuleData,
                        konstueParameter.source,
                        if (multiDeclaration != null) SpecialNames.DESTRUCT else konstueParameter.name,
                        buildFunctionCall {
                            source = fakeSource
                            calleeReference = buildSimpleNamedReference {
                                source = fakeSource
                                name = OperatorNameConventions.NEXT
                            }
                            explicitReceiver = generateResolvedAccessExpression(fakeSource, iteratorVal)
                        },
                        konstueParameter.returnTypeRef
                    )
                    if (multiDeclaration != null) {
                        konst destructuringBlock = generateDestructuringBlock(
                            baseModuleData,
                            multiDeclaration,
                            firLoopParameter,
                            tmpVariable = true
                        )
                        statements.addAll(destructuringBlock.statements)
                    } else {
                        statements.add(firLoopParameter)
                    }
                    statements += convertLoopBody(blockNode)
                }
            }
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseLoopBody
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.toFirBlock
     */
    private fun convertLoopBody(body: LighterASTNode?): FirBlock {
        return convertLoopOrIfBody(body) ?: buildEmptyExpressionBlock()
    }

    private fun convertLoopOrIfBody(body: LighterASTNode?): FirBlock? {
        var firBlock: FirBlock? = null
        var firStatement: FirStatement? = null
        body?.forEachChildren {
            when (it.tokenType) {
                BLOCK -> firBlock = declarationsConverter.convertBlockExpression(it)
                ANNOTATED_EXPRESSION -> {
                    if (it.getChildNodeByType(BLOCK) != null) {
                        firBlock = getAsFirExpression(it)
                    } else {
                        firStatement = getAsFirExpression(it)
                    }
                }
                else -> if (it.isExpression()) firStatement = getAsFirExpression(it)
            }
        }

        return firStatement?.let { FirSingleExpressionBlock(it) } ?: firBlock
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseTry
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitTryExpression
     */
    private fun convertTryExpression(tryExpression: LighterASTNode): FirExpression {
        lateinit var tryBlock: FirBlock
        konst catchClauses = mutableListOf<Triple<ValueParameter?, FirBlock, KtLightSourceElement>>()
        var finallyBlock: FirBlock? = null
        tryExpression.forEachChildren {
            when (it.tokenType) {
                BLOCK -> tryBlock = declarationsConverter.convertBlock(it)
                CATCH -> convertCatchClause(it)?.also { oneClause -> catchClauses += oneClause }
                FINALLY -> finallyBlock = convertFinally(it)
            }
        }
        return buildTryExpression {
            source = tryExpression.toFirSourceElement()
            this.tryBlock = tryBlock
            this.finallyBlock = finallyBlock
            for ((parameter, block, clauseSource) in catchClauses) {
                if (parameter == null) continue
                catches += buildCatch {
                    this.parameter = buildProperty {
                        source = parameter.source
                        moduleData = baseModuleData
                        origin = FirDeclarationOrigin.Source
                        returnTypeRef = parameter.returnTypeRef
                        isVar = false
                        status = FirResolvedDeclarationStatusImpl(Visibilities.Local, Modality.FINAL, EffectiveVisibility.Local)
                        isLocal = true
                        this.name = parameter.name
                        symbol = FirPropertySymbol(CallableId(name))
                        annotations += parameter.annotations
                    }.also {
                        it.isCatchParameter = true
                    }
                    this.block = block
                    this.source = clauseSource
                }
            }
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseTry
     */
    private fun convertCatchClause(catchClause: LighterASTNode): Triple<ValueParameter?, FirBlock, KtLightSourceElement>? {
        var konstueParameter: ValueParameter? = null
        var blockNode: LighterASTNode? = null
        catchClause.forEachChildren {
            when (it.tokenType) {
                VALUE_PARAMETER_LIST -> konstueParameter = declarationsConverter.convertValueParameters(it, FirAnonymousFunctionSymbol()/*TODO*/, ValueParameterDeclaration.CATCH)
                    .firstOrNull() ?: return null
                BLOCK -> blockNode = it
            }
        }

        return Triple(konstueParameter, declarationsConverter.convertBlock(blockNode), catchClause.toFirSourceElement())
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseTry
     */
    private fun convertFinally(finallyExpression: LighterASTNode): FirBlock {
        var blockNode: LighterASTNode? = null
        finallyExpression.forEachChildren {
            when (it.tokenType) {
                BLOCK -> blockNode = it
            }
        }

        return declarationsConverter.convertBlock(blockNode)
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseIf
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitIfExpression
     */
    private fun convertIfExpression(ifExpression: LighterASTNode): FirExpression {
        var components = parseIfExpression(ifExpression)

        return buildWhenExpression {
            source = ifExpression.toFirSourceElement()
            whenBranches@ while (true) {
                with(components) {
                    konst trueBranch = convertLoopBody(thenBlock)
                    branches += buildWhenBranch {
                        source = thenBlock?.toFirSourceElement()
                        condition = firCondition ?: buildErrorExpression(
                            null,
                            ConeSyntaxDiagnostic("If statement should have condition")
                        )
                        result = trueBranch
                    }
                }
                if (components.elseBlock == null) break@whenBranches
                var cascadeIf = false
                components.elseBlock?.forEachChildren {
                    if (it.tokenType == IF) {
                        cascadeIf = true
                        components = parseIfExpression(it)
                    }
                }
                if (!cascadeIf) {
                    with(components) {
                        konst elseBranch = convertLoopOrIfBody(elseBlock)
                        if (elseBranch != null) {
                            branches += buildWhenBranch {
                                source = elseBlock?.toFirSourceElement()
                                condition = buildElseIfTrueCondition()
                                result = elseBranch
                            }
                        }
                    }
                    break@whenBranches
                }
            }
            usedAsExpression = ifExpression.usedAsExpression
        }
    }

    private class IfNodeComponents(konst firCondition: FirExpression?, konst thenBlock: LighterASTNode?, konst elseBlock: LighterASTNode?)

    private fun parseIfExpression(ifExpression: LighterASTNode): IfNodeComponents {
        var firCondition: FirExpression? = null
        var thenBlock: LighterASTNode? = null
        var elseBlock: LighterASTNode? = null
        ifExpression.forEachChildren {
            when (it.tokenType) {
                CONDITION -> firCondition = getAsFirExpression(it, "If statement should have condition")
                THEN -> thenBlock = it
                ELSE -> elseBlock = it
            }
        }
        return IfNodeComponents(firCondition, thenBlock, elseBlock)
    }

    private konst LighterASTNode.usedAsExpression: Boolean
        get() {
            var parent = getParent() ?: return true
            while (parent.elementType == ANNOTATED_EXPRESSION ||
                parent.elementType == LABELED_EXPRESSION
            ) {
                parent = parent.getParent() ?: return true
            }
            konst parentTokenType = parent.tokenType
            if (parentTokenType == BLOCK) return false
            if (parentTokenType == THEN || parentTokenType == ELSE || parentTokenType == WHEN_ENTRY) {
                return parent.getParent()?.usedAsExpression ?: true
            }
            if (parentTokenType != BODY) return true
            konst type = parent.getParent()?.tokenType ?: return true
            return !(type == FOR || type == WHILE || type == DO_WHILE)
        }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseJump
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitBreakExpression
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitContinueExpression
     */
    private fun convertLoopJump(jump: LighterASTNode): FirExpression {
        var isBreak = true
        jump.forEachChildren {
            when (it.tokenType) {
                CONTINUE_KEYWORD -> isBreak = false
                //BREAK -> isBreak = true
            }
        }

        konst jumpBuilder = if (isBreak) FirBreakExpressionBuilder() else FirContinueExpressionBuilder()
        konst sourceElement = jump.toFirSourceElement()
        return jumpBuilder.apply {
            source = sourceElement
        }.bindLabel(jump).build()
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseReturn
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitReturnExpression
     */
    private fun convertReturn(returnExpression: LighterASTNode): FirExpression {
        var labelName: String? = null
        var firExpression: FirExpression? = null
        returnExpression.forEachChildren {
            when (it.tokenType) {
                LABEL_QUALIFIER -> labelName = it.getAsStringWithoutBacktick().replace("@", "")
                else -> if (it.isExpression()) firExpression = getAsFirExpression(it, "Incorrect return expression")
            }
        }

        konst calculatedFirExpression = firExpression ?: buildUnitExpression {
            source = returnExpression.toFirSourceElement(KtFakeSourceElementKind.ImplicitUnit)
        }
        return calculatedFirExpression.toReturn(
            baseSource = returnExpression.toFirSourceElement(),
            labelName = labelName,
            fromKtReturnExpression = true
        )
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseThrow
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitThrowExpression
     */
    private fun convertThrow(throwExpression: LighterASTNode): FirExpression {
        var firExpression: FirExpression? = null
        throwExpression.forEachChildren {
            if (it.isExpression()) firExpression = getAsFirExpression(it, "Nothing to throw")
        }

        return buildThrowExpression {
            source = throwExpression.toFirSourceElement()
            exception = firExpression ?: buildErrorExpression(null, ConeSyntaxDiagnostic("Nothing to throw"))
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseThisExpression
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitThisExpression
     */
    private fun convertThisExpression(thisExpression: LighterASTNode): FirQualifiedAccessExpression {
        konst label: String? = thisExpression.getLabelName()
        return buildThisReceiverExpression {
            source = thisExpression.toFirSourceElement()
            calleeReference = buildExplicitThisReference { labelName = label }
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseSuperExpression
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.visitSuperExpression
     */
    private fun convertSuperExpression(superExpression: LighterASTNode): FirQualifiedAccessExpression {
        konst label: String? = superExpression.getLabelName()
        var superTypeRef: FirTypeRef = implicitType
        superExpression.forEachChildren {
            when (it.tokenType) {
                TYPE_REFERENCE -> superTypeRef = declarationsConverter.convertType(it)
            }
        }

        return buildPropertyAccessExpression {
            source = superExpression.toFirSourceElement()
            calleeReference = buildExplicitSuperReference {
                labelName = label
                this.superTypeRef = superTypeRef
            }
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseValueArgumentList
     */
    fun convertValueArguments(konstueArguments: LighterASTNode): List<FirExpression> {
        return konstueArguments.forEachChildrenReturnList { node, container ->
            when (node.tokenType) {
                VALUE_ARGUMENT -> container += convertValueArgument(node)
                LAMBDA_EXPRESSION,
                LABELED_EXPRESSION,
                ANNOTATED_EXPRESSION -> container += buildLambdaArgumentExpression {
                    source = konstueArguments.toFirSourceElement()
                    expression = getAsFirExpression(node)
                }
            }
        }
    }

    /**
     * @see org.jetbrains.kotlin.parsing.KotlinExpressionParsing.parseValueArgument
     * @see org.jetbrains.kotlin.fir.builder.RawFirBuilder.Visitor.toFirExpression(org.jetbrains.kotlin.psi.ValueArgument)
     */
    private fun convertValueArgument(konstueArgument: LighterASTNode): FirExpression {
        var identifier: String? = null
        var isSpread = false
        var firExpression: FirExpression? = null
        konstueArgument.forEachChildren {
            when (it.tokenType) {
                VALUE_ARGUMENT_NAME -> identifier = it.asText
                MUL -> isSpread = true
                STRING_TEMPLATE -> firExpression = convertStringTemplate(it)
                is KtConstantExpressionElementType -> firExpression = convertConstantExpression(it)
                else -> if (it.isExpression()) firExpression = getAsFirExpression(it, "Argument is absent")
            }
        }
        konst calculatedFirExpression =
            firExpression ?: buildErrorExpression(null, ConeSyntaxDiagnostic("Argument is absent"))
        return when {
            identifier != null -> buildNamedArgumentExpression {
                source = konstueArgument.toFirSourceElement()
                expression = calculatedFirExpression
                this.isSpread = isSpread
                name = identifier.nameAsSafeName()
            }
            isSpread -> buildSpreadArgumentExpression {
                source = konstueArgument.toFirSourceElement()
                expression = calculatedFirExpression
            }
            else -> calculatedFirExpression
        }
    }
}
