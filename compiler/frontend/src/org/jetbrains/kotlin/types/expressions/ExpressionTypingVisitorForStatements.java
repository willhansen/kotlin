/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.types.expressions;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.tree.IElementType;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.builtins.KotlinBuiltIns;
import org.jetbrains.kotlin.config.LanguageFeature;
import org.jetbrains.kotlin.descriptors.*;
import org.jetbrains.kotlin.diagnostics.Errors;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.BindingContextUtils;
import org.jetbrains.kotlin.resolve.TemporaryBindingTrace;
import org.jetbrains.kotlin.resolve.calls.ArgumentTypeResolver;
import org.jetbrains.kotlin.resolve.calls.checkers.AssignmentChecker;
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext;
import org.jetbrains.kotlin.resolve.calls.checkers.NewSchemeOfIntegerOperatorResolutionChecker;
import org.jetbrains.kotlin.resolve.calls.context.CallPosition;
import org.jetbrains.kotlin.resolve.calls.context.ContextDependency;
import org.jetbrains.kotlin.resolve.calls.context.ResolutionContext;
import org.jetbrains.kotlin.resolve.calls.context.TemporaryTraceAndCache;
import org.jetbrains.kotlin.resolve.calls.inference.BuilderInferenceSession;
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall;
import org.jetbrains.kotlin.resolve.calls.results.OverloadResolutionResults;
import org.jetbrains.kotlin.resolve.calls.results.OverloadResolutionResultsImpl;
import org.jetbrains.kotlin.resolve.calls.results.OverloadResolutionResultsUtil;
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo;
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValue;
import org.jetbrains.kotlin.resolve.extensions.AssignResolutionAltererExtension;
import org.jetbrains.kotlin.resolve.lazy.ForceResolveUtil;
import org.jetbrains.kotlin.resolve.scopes.LexicalWritableScope;
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver;
import org.jetbrains.kotlin.types.KotlinType;
import org.jetbrains.kotlin.types.KotlinTypeKt;
import org.jetbrains.kotlin.types.TypeUtils;
import org.jetbrains.kotlin.types.checker.KotlinTypeChecker;
import org.jetbrains.kotlin.types.expressions.typeInfoFactory.TypeInfoFactoryKt;
import org.jetbrains.kotlin.util.OperatorNameConventions;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static org.jetbrains.kotlin.diagnostics.Errors.*;
import static org.jetbrains.kotlin.psi.KtPsiUtil.deparenthesize;
import static org.jetbrains.kotlin.resolve.BindingContext.AMBIGUOUS_REFERENCE_TARGET;
import static org.jetbrains.kotlin.resolve.BindingContext.VARIABLE_REASSIGNMENT;
import static org.jetbrains.kotlin.resolve.calls.context.ContextDependency.INDEPENDENT;
import static org.jetbrains.kotlin.types.TypeUtils.NO_EXPECTED_TYPE;
import static org.jetbrains.kotlin.types.TypeUtils.noExpectedType;

@SuppressWarnings("SuspiciousMethodCalls")
public class ExpressionTypingVisitorForStatements extends ExpressionTypingVisitor {
    private final LexicalWritableScope scope;
    private final BasicExpressionTypingVisitor basic;
    private final ControlStructureTypingVisitor controlStructures;
    private final PatternMatchingTypingVisitor patterns;
    private final FunctionsTypingVisitor functions;

    public ExpressionTypingVisitorForStatements(
            @NotNull ExpressionTypingInternals facade,
            @NotNull LexicalWritableScope scope,
            @NotNull BasicExpressionTypingVisitor basic,
            @NotNull ControlStructureTypingVisitor controlStructures,
            @NotNull PatternMatchingTypingVisitor patterns,
            @NotNull FunctionsTypingVisitor functions
    ) {
        super(facade);
        this.scope = scope;
        this.basic = basic;
        this.controlStructures = controlStructures;
        this.patterns = patterns;
        this.functions = functions;
    }

    @Nullable
    private KotlinType checkAssignmentType(
            @Nullable KotlinType assignmentType,
            @NotNull KtBinaryExpression expression,
            @NotNull ExpressionTypingContext context
    ) {
        if (assignmentType != null && !KotlinBuiltIns.isUnit(assignmentType) && !noExpectedType(context.expectedType) &&
            !KotlinTypeKt.isError(context.expectedType) && TypeUtils.equalTypes(context.expectedType, assignmentType)) {
            context.trace.report(Errors.ASSIGNMENT_TYPE_MISMATCH.on(expression, context.expectedType));
            return null;
        }
        return components.dataFlowAnalyzer.checkStatementType(expression, context);
    }

    @Override
    public KotlinTypeInfo visitObjectDeclaration(@NotNull KtObjectDeclaration declaration, ExpressionTypingContext context) {
        components.localClassifierAnalyzer.processClassOrObject(
                scope, context.replaceScope(scope).replaceContextDependency(INDEPENDENT),
                scope.getOwnerDescriptor(),
                declaration);
        return TypeInfoFactoryKt.createTypeInfo(components.dataFlowAnalyzer.checkStatementType(declaration, context), context);
    }

    @Override
    public KotlinTypeInfo visitProperty(@NotNull KtProperty property, ExpressionTypingContext typingContext) {
        Pair<KotlinTypeInfo, VariableDescriptor> typeInfoAndVariableDescriptor = components.localVariableResolver.process(property, typingContext, scope, facade);
        scope.addVariableDescriptor(typeInfoAndVariableDescriptor.getSecond());
        return typeInfoAndVariableDescriptor.getFirst();
    }

    @Override
    public KotlinTypeInfo visitTypeAlias(@NotNull KtTypeAlias typeAlias, ExpressionTypingContext context) {
        TypeAliasDescriptor typeAliasDescriptor = components.descriptorResolver.resolveTypeAliasDescriptor(
                context.scope.getOwnerDescriptor(), context.scope, typeAlias, context.trace);
        scope.addClassifierDescriptor(typeAliasDescriptor);
        ForceResolveUtil.forceResolveAllContents(typeAliasDescriptor);

        facade.getComponents().declarationsCheckerBuilder.withTrace(context.trace).checkLocalTypeAliasDeclaration(typeAlias, typeAliasDescriptor);

        return TypeInfoFactoryKt.createTypeInfo(components.dataFlowAnalyzer.checkStatementType(typeAlias, context), context);
    }

    @Override
    public KotlinTypeInfo visitDestructuringDeclaration(@NotNull KtDestructuringDeclaration multiDeclaration, ExpressionTypingContext context) {
        components.annotationResolver.resolveAnnotationsWithArguments(scope, multiDeclaration.getModifierList(), context.trace);

        KtExpression initializer = multiDeclaration.getInitializer();
        if (initializer == null) {
            context.trace.report(INITIALIZER_REQUIRED_FOR_DESTRUCTURING_DECLARATION.on(multiDeclaration));
        }

        ExpressionReceiver expressionReceiver = initializer != null ? ExpressionTypingUtils.getExpressionReceiver(
                facade, initializer, context.replaceExpectedType(NO_EXPECTED_TYPE).replaceContextDependency(INDEPENDENT)) : null;

        components.destructuringDeclarationResolver
                .defineLocalVariablesFromDestructuringDeclaration(scope, multiDeclaration, expressionReceiver, initializer, context);
        components.modifiersChecker.withTrace(context.trace).checkModifiersForDestructuringDeclaration(multiDeclaration);
        components.identifierChecker.checkDeclaration(multiDeclaration, context.trace);

        if (expressionReceiver == null) {
            return TypeInfoFactoryKt.noTypeInfo(context);
        }
        else {
            return facade.getTypeInfo(initializer, context)
                    .replaceType(components.dataFlowAnalyzer.checkStatementType(multiDeclaration, context));
        }
    }

    @Override
    public KotlinTypeInfo visitNamedFunction(@NotNull KtNamedFunction function, ExpressionTypingContext context) {
        return functions.visitNamedFunction(function, context, /* isDeclaration = */ function.getName() != null, scope);
    }

    @Override
    public KotlinTypeInfo visitClass(@NotNull KtClass klass, ExpressionTypingContext context) {
        components.localClassifierAnalyzer.processClassOrObject(
                scope, context.replaceScope(scope).replaceContextDependency(INDEPENDENT),
                scope.getOwnerDescriptor(),
                klass);
        return TypeInfoFactoryKt.createTypeInfo(components.dataFlowAnalyzer.checkStatementType(klass, context), context);
    }

    @Override
    public KotlinTypeInfo visitDeclaration(@NotNull KtDeclaration dcl, ExpressionTypingContext context) {
        return TypeInfoFactoryKt.createTypeInfo(components.dataFlowAnalyzer.checkStatementType(dcl, context), context);
    }

    @Override
    public KotlinTypeInfo visitBinaryExpression(@NotNull KtBinaryExpression expression, ExpressionTypingContext context) {
        KtSimpleNameExpression operationSign = expression.getOperationReference();
        IElementType operationType = operationSign.getReferencedNameElementType();
        KotlinTypeInfo result;
        if (operationType == KtTokens.EQ) {
            result = visitAssignment(expression, context);
        }
        else if (OperatorConventions.ASSIGNMENT_OPERATIONS.containsKey(operationType)) {
            result = visitAssignmentOperation(expression, context);
        }
        else {
            return facade.getTypeInfo(expression, context);
        }
        return components.dataFlowAnalyzer.checkType(result, expression, context);
    }

    @NotNull
    protected KotlinTypeInfo visitAssignmentOperation(KtBinaryExpression expression, ExpressionTypingContext contextWithExpectedType) {
        //There is a temporary binding trace for an opportunity to resolve set method for array if needed (the initial trace should be used there)
        TemporaryTraceAndCache temporary = TemporaryTraceAndCache.create(
                contextWithExpectedType, "trace to resolve array set method for binary expression", expression);
        ExpressionTypingContext context = contextWithExpectedType.replaceExpectedType(NO_EXPECTED_TYPE)
                .replaceTraceAndCache(temporary).replaceContextDependency(INDEPENDENT);

        KtSimpleNameExpression operationSign = expression.getOperationReference();
        IElementType operationType = operationSign.getReferencedNameElementType();
        KtExpression leftOperand = expression.getLeft();
        KotlinTypeInfo leftInfo = ExpressionTypingUtils.getTypeInfoOrNullType(leftOperand, context, facade);
        KotlinType leftType = leftInfo.getType();

        KtExpression right = expression.getRight();
        KtExpression left = leftOperand == null ? null : deparenthesize(leftOperand);
        if (right == null || left == null) {
            temporary.commit();
            return leftInfo.clearType();
        }

        if (leftType == null) {
            KotlinTypeInfo rightInfo = facade.getTypeInfo(right, context.replaceDataFlowInfo(leftInfo.getDataFlowInfo()));
            context.trace.report(UNRESOLVED_REFERENCE.on(operationSign, operationSign));
            temporary.commit();
            return rightInfo.clearType();
        } else if (!ArgumentTypeResolver.isFunctionLiteralOrCallableReference(right, context) &&
                   !context.languageVersionSettings.supportsFeature(LanguageFeature.NewInference)
        ) {
            // Cache the type info for the right hand side so that we don't ekonstuate it twice if there is no konstid plusAssign.
            // We skip over function literals and references, since ArgumentTypeResolver will only resolve the shape of the
            // function type before attempting to resolve the call.
            facade.getTypeInfo(right, context.replaceContextDependency(ContextDependency.DEPENDENT));
        }
        ExpressionReceiver receiver = ExpressionReceiver.Companion.create(left, leftType, context.trace.getBindingContext());

        // We check that defined only one of '+=' and '+' operations, and call it (in the case '+' we then also assign)
        // Check for '+='
        Name name = OperatorConventions.ASSIGNMENT_OPERATIONS.get(operationType);
        TemporaryTraceAndCache temporaryForAssignmentOperation = TemporaryTraceAndCache.create(
                context, "trace to check assignment operation like '+=' for", expression);
        OverloadResolutionResults<FunctionDescriptor> assignmentOperationDescriptors =
                components.callResolver.resolveBinaryCall(
                        context.replaceTraceAndCache(temporaryForAssignmentOperation).replaceScope(scope),
                        receiver, expression, name
                );
        KotlinType assignmentOperationType = OverloadResolutionResultsUtil.getResultingType(assignmentOperationDescriptors, context);

        OverloadResolutionResults<FunctionDescriptor> binaryOperationDescriptors;
        KotlinType binaryOperationType;
        TemporaryTraceAndCache temporaryForBinaryOperation = TemporaryTraceAndCache.create(
                context, "trace to check binary operation like '+' for", expression);
        TemporaryBindingTrace ignoreReportsTrace = TemporaryBindingTrace.create(context.trace, "Trace for checking assignability");
        ExpressionTypingContext contextForBinaryOperation = null;

        boolean lhsAssignable = basic.checkLValue(ignoreReportsTrace, context, left, right, expression, false);

        if (assignmentOperationType == null || lhsAssignable) {
            contextForBinaryOperation = context.replaceTraceAndCache(temporaryForBinaryOperation).replaceScope(scope);
            // Check for '+'
            // We should clear calls info for coroutine inference within right side as here we analyze it a second time in another context
            if (context.inferenceSession instanceof BuilderInferenceSession) {
                ((BuilderInferenceSession) context.inferenceSession).clearCallsInfoByContainingElement(right);
            }
            Name counterpartName = OperatorConventions.BINARY_OPERATION_NAMES.get(OperatorConventions.ASSIGNMENT_OPERATION_COUNTERPARTS.get(operationType));
            binaryOperationDescriptors =
                    components.callResolver.resolveBinaryCall(contextForBinaryOperation, receiver, expression, counterpartName);

            binaryOperationType = OverloadResolutionResultsUtil.getResultingType(binaryOperationDescriptors, context);
        } else {
            binaryOperationDescriptors = OverloadResolutionResultsImpl.nameNotFound();
            binaryOperationType = null;
        }

        KotlinType type = assignmentOperationType != null ? assignmentOperationType : binaryOperationType;
        KotlinTypeInfo rightInfo = leftInfo;

        boolean hasRemAssignOperation = atLeastOneOperation(assignmentOperationDescriptors.getResultingCalls(), OperatorNameConventions.REM_ASSIGN);
        boolean hasRemBinaryOperation = atLeastOneOperation(binaryOperationDescriptors.getResultingCalls(), OperatorNameConventions.REM);

        boolean oneTypeOfModRemOperations = hasRemAssignOperation == hasRemBinaryOperation;

        boolean maybeAmbiguity = assignmentOperationDescriptors.isSuccess() && binaryOperationDescriptors.isSuccess() && oneTypeOfModRemOperations;
        boolean isResolvedToPlusAssign = assignmentOperationType != null &&
                                       (assignmentOperationDescriptors.isSuccess() || !binaryOperationDescriptors.isSuccess()) &&
                                       (!hasRemBinaryOperation || !binaryOperationDescriptors.isSuccess());

        KotlinTypeInfo rhsResolutionResult;
        // We complete resolution for 'plus' only if there may be ambiguity (in this case we can disambiguate it),
        // or it definitely won't be resolved to plus assign (in this case we would analyse right side twice)
        if (maybeAmbiguity || !isResolvedToPlusAssign) {
            rhsResolutionResult = completePlusResolution(contextForBinaryOperation, expression, binaryOperationType, left, leftInfo);
        } else {
            rhsResolutionResult = null;
        }

        if (maybeAmbiguity && rhsResolutionResult != null) {
            // Both 'plus()' and 'plusAssign()' available => ambiguity
            OverloadResolutionResults<FunctionDescriptor> ambiguityResolutionResults = OverloadResolutionResultsUtil.ambiguity(assignmentOperationDescriptors, binaryOperationDescriptors);
            context.trace.report(ASSIGN_OPERATOR_AMBIGUITY.on(operationSign, ambiguityResolutionResults.getResultingCalls()));
            Collection<DeclarationDescriptor> descriptors = new HashSet<>();
            for (ResolvedCall<?> resolvedCall : ambiguityResolutionResults.getResultingCalls()) {
                descriptors.add(resolvedCall.getResultingDescriptor());
            }
            rightInfo = rhsResolutionResult;
            context.trace.record(AMBIGUOUS_REFERENCE_TARGET, operationSign, descriptors);
        } else if (isResolvedToPlusAssign) {
            // There's 'plusAssign()', so we do a.plusAssign(b)
            temporaryForAssignmentOperation.commit();
            if (!KotlinTypeChecker.DEFAULT.equalTypes(components.builtIns.getUnitType(), assignmentOperationType)) {
                context.trace.report(ASSIGNMENT_OPERATOR_SHOULD_RETURN_UNIT.on(operationSign, assignmentOperationDescriptors.getResultingDescriptor(), operationSign));
            }
        } else {
            if (rhsResolutionResult != null) {
                rightInfo = rhsResolutionResult;
            }
            // There's only 'plus()', so we try 'a = a + b'
            temporaryForBinaryOperation.commit();
            context.trace.record(VARIABLE_REASSIGNMENT, expression);
        }
        temporary.commit();
        return rightInfo.replaceType(checkAssignmentType(type, expression, contextWithExpectedType));
    }

    private KotlinTypeInfo completePlusResolution(
            ExpressionTypingContext context,
            KtBinaryExpression expression,
            KotlinType binaryOperationType,
            KtExpression leftDeparentized,
            KotlinTypeInfo leftInfo
    ) {
        KtExpression leftOperand = expression.getLeft();
        KtExpression rightOperand = expression.getRight();

        if (leftOperand == null || rightOperand == null) return null;

        if (leftDeparentized instanceof KtArrayAccessExpression) {
            ExpressionTypingContext contextForResolve = context.replaceScope(scope).replaceBindingTrace(TemporaryBindingTrace.create(
                    context.trace, "trace to resolve array set method for assignment", expression));
            basic.resolveImplicitArrayAccessSetMethod((KtArrayAccessExpression) leftDeparentized, rightOperand, contextForResolve, context.trace);
        }
        KotlinTypeInfo rightInfo = facade.getTypeInfo(rightOperand, context.replaceDataFlowInfo(leftInfo.getDataFlowInfo()));

        boolean refineJavaFieldInTypeProperly =
                components.languageVersionSettings.supportsFeature(LanguageFeature.RefineTypeCheckingOnAssignmentsToJavaFields);

        BindingContext bindingContext = context.trace.getBindingContext();
        KotlinType leftType = leftInfo.getType();

        KotlinType expectedType = refineJavaFieldInTypeProperly
                                  ? refineTypeByPropertyInType(bindingContext, leftOperand, leftType)
                                  : refineTypeFromPropertySetterIfPossible(bindingContext, leftOperand, leftType);

        Ref<Boolean> hasErrorsOnTypeChecking = Ref.create(false);
        components.dataFlowAnalyzer.checkType(
                binaryOperationType,
                expression,
                context.replaceExpectedType(expectedType)
                        .replaceDataFlowInfo(rightInfo.getDataFlowInfo())
                        .replaceCallPosition(new CallPosition.PropertyAssignment(leftDeparentized, false)),
                hasErrorsOnTypeChecking,
                true
        );
        basic.checkLValue(context.trace, context, leftOperand, rightOperand, expression, false);

        if (!refineJavaFieldInTypeProperly) {
            checkPropertyInTypeWithWarnings(
                    context, expression, binaryOperationType, rightInfo.getDataFlowInfo(), leftOperand, leftType, expectedType
            );
        }

        return !hasErrorsOnTypeChecking.get() ? rightInfo : null;
    }

    private static boolean atLeastOneOperation(Collection<? extends ResolvedCall<FunctionDescriptor>> calls, Name operationName) {
        for (ResolvedCall<FunctionDescriptor> call : calls) {
            if (call.getCandidateDescriptor().getName().equals(operationName)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    private static KotlinType refineTypeFromPropertySetterIfPossible(
            @NotNull BindingContext bindingContext,
            @Nullable KtElement leftOperand,
            @Nullable KotlinType leftOperandType
    ) {
        VariableDescriptor descriptor = BindingContextUtils.extractVariableFromResolvedCall(bindingContext, leftOperand);

        if (descriptor instanceof PropertyDescriptor) {
            PropertySetterDescriptor setter = ((PropertyDescriptor) descriptor).getSetter();
            if (setter != null) return setter.getValueParameters().get(0).getType();
        }

        return leftOperandType;
    }

    @Nullable
    private static KotlinType refineTypeByPropertyInType(
            @NotNull BindingContext bindingContext,
            @Nullable KtElement leftOperand,
            @Nullable KotlinType leftOperandType
    ) {
        VariableDescriptor descriptor = BindingContextUtils.extractVariableFromResolvedCall(bindingContext, leftOperand);

        if (descriptor instanceof PropertyDescriptor) {
            KotlinType inType = ((PropertyDescriptor) descriptor).getInType();
            if (inType != null) return inType;
        }

        return leftOperandType;
    }

    @NotNull
    protected KotlinTypeInfo visitAssignment(KtBinaryExpression expression, ExpressionTypingContext contextWithExpectedType) {
        ExpressionTypingContext context =
                contextWithExpectedType.replaceExpectedType(NO_EXPECTED_TYPE).replaceScope(scope).replaceContextDependency(INDEPENDENT);
        KtExpression leftOperand = expression.getLeft();
        if (leftOperand instanceof KtAnnotatedExpression) {
            basic.resolveAnnotationsOnExpression((KtAnnotatedExpression) leftOperand, context);
        }
        KtExpression left = deparenthesize(leftOperand);
        KtExpression right = expression.getRight();
        if (left instanceof KtArrayAccessExpression) {
            KtArrayAccessExpression arrayAccessExpression = (KtArrayAccessExpression) left;
            if (right == null) return TypeInfoFactoryKt.noTypeInfo(context);
            KotlinTypeInfo typeInfo = basic.resolveArrayAccessSetMethod(arrayAccessExpression, right, context, context.trace);
            basic.checkLValue(context.trace, context, arrayAccessExpression, right, expression, true);
            return typeInfo.replaceType(checkAssignmentType(typeInfo.getType(), expression, contextWithExpectedType));
        }
        KotlinTypeInfo leftInfo = ExpressionTypingUtils.getTypeInfoOrNullType(
                left,
                context.replaceCallPosition(new CallPosition.PropertyAssignment(left, true)),
                facade
        );

        BindingContext bindingContext = context.trace.getBindingContext();
        KotlinType leftType = leftInfo.getType();

        boolean refineJavaFieldInTypeProperly =
                components.languageVersionSettings.supportsFeature(LanguageFeature.RefineTypeCheckingOnAssignmentsToJavaFields);
        KotlinType expectedType = refineJavaFieldInTypeProperly
                                  ? refineTypeByPropertyInType(bindingContext, leftOperand, leftType)
                                  : refineTypeFromPropertySetterIfPossible(bindingContext, leftOperand, leftType);

        List<AssignResolutionAltererExtension> assignAlterers = AssignResolutionAltererExtension.Companion.getInstances(expression.getProject());
        if (!assignAlterers.isEmpty()) {
            KotlinTypeInfo alteredTypeInfo = assignAlterers.stream()
                    .filter((it) -> it.needOverloadAssign(expression, leftType, bindingContext))
                    .map((it) -> it.resolveAssign(bindingContext, expression, leftOperand, left, leftInfo, context, components, scope))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            if (alteredTypeInfo != null) {
                return alteredTypeInfo;
            }
        }

        DataFlowInfo dataFlowInfo = leftInfo.getDataFlowInfo();
        KotlinTypeInfo resultInfo;
        if (right != null) {
            resultInfo = facade.getTypeInfo(
                    right,
                    context.replaceDataFlowInfo(dataFlowInfo)
                            .replaceExpectedType(expectedType)
                            .replaceCallPosition(new CallPosition.PropertyAssignment(leftOperand, false))
            );

            dataFlowInfo = resultInfo.getDataFlowInfo();
            KotlinType rightType = resultInfo.getType();
            if (left != null && expectedType != null && rightType != null) {
                DataFlowValue leftValue = components.dataFlowValueFactory.createDataFlowValue(left, expectedType, context);
                DataFlowValue rightValue = components.dataFlowValueFactory.createDataFlowValue(right, rightType, context);
                // We cannot say here anything new about rightValue except it has the same konstue as leftValue
                resultInfo = resultInfo.replaceDataFlowInfo(dataFlowInfo.assign(leftValue, rightValue, components.languageVersionSettings));
                NewSchemeOfIntegerOperatorResolutionChecker.checkArgument(expectedType, right, context.trace, components.moduleDescriptor);
            }
        }
        else {
            resultInfo = leftInfo;
        }
        if (expectedType != null && leftOperand != null) { //if expectedType == null, some other error has been generated
            basic.checkLValue(context.trace, context, leftOperand, right, expression, false);

            CallCheckerContext callCheckerContext =
                    new CallCheckerContext(
                            context,
                            components.deprecationResolver,
                            components.moduleDescriptor,
                            components.missingSupertypesResolver,
                            components.callComponents,
                            context.trace
                    );
            for (AssignmentChecker checker : components.assignmentCheckers) {
                checker.check(expression, callCheckerContext);
            }
        }

        if (!refineJavaFieldInTypeProperly) {
            checkPropertyInTypeWithWarnings(
                    context, expression, resultInfo.getType(), resultInfo.getDataFlowInfo(), leftOperand, leftType, expectedType
            );
        }

        return resultInfo.replaceType(components.dataFlowAnalyzer.checkStatementType(expression, contextWithExpectedType));
    }

    private void checkPropertyInTypeWithWarnings(
            @NotNull ResolutionContext<?> context,
            @NotNull KtBinaryExpression expression,
            @Nullable KotlinType rhsType,
            @NotNull DataFlowInfo rhsDataFlowInfo,
            @Nullable KtExpression lhsOperand,
            @Nullable KotlinType lhsType,
            @Nullable KotlinType expectedType
    ) {
        if (rhsType == null || expectedType == null) return;

        KotlinType expectedTypeByInType = refineTypeByPropertyInType(context.trace.getBindingContext(), lhsOperand, lhsType);

        if (expectedTypeByInType != null && expectedType != expectedTypeByInType && !TypeUtils.equalTypes(expectedType, expectedTypeByInType)) {
            Ref<Boolean> hasErrorsOnTypeChecking = Ref.create(false);
            components.dataFlowAnalyzer.checkType(
                    rhsType,
                    expression,
                    context.replaceExpectedType(expectedTypeByInType)
                            .replaceDataFlowInfo(rhsDataFlowInfo)
                            .replaceCallPosition(new CallPosition.PropertyAssignment(lhsOperand, false)),
                    hasErrorsOnTypeChecking,
                    false
            );
            if (hasErrorsOnTypeChecking.get()) {
                context.trace.report(TYPE_MISMATCH_WARNING.on(expression, expectedTypeByInType, rhsType));
            }
        }
    }

    @Override
    public KotlinTypeInfo visitExpression(@NotNull KtExpression expression, ExpressionTypingContext context) {
        return facade.getTypeInfo(expression, context);
    }

    @Override
    public KotlinTypeInfo visitKtElement(@NotNull KtElement element, ExpressionTypingContext context) {
        context.trace.report(UNSUPPORTED.on(element, "in a block"));
        return TypeInfoFactoryKt.noTypeInfo(context);
    }

    @Override
    public KotlinTypeInfo visitWhileExpression(@NotNull KtWhileExpression expression, ExpressionTypingContext context) {
        return controlStructures.visitWhileExpression(expression, context, true);
    }

    @Override
    public KotlinTypeInfo visitDoWhileExpression(@NotNull KtDoWhileExpression expression, ExpressionTypingContext context) {
        return controlStructures.visitDoWhileExpression(expression, context, true);
    }

    @Override
    public KotlinTypeInfo visitForExpression(@NotNull KtForExpression expression, ExpressionTypingContext context) {
        return controlStructures.visitForExpression(expression, context, true);
    }

    @Override
    public KotlinTypeInfo visitAnnotatedExpression(
            @NotNull KtAnnotatedExpression expression, ExpressionTypingContext data
    ) {
        return basic.visitAnnotatedExpression(expression, data, true);
    }

    @Override
    public KotlinTypeInfo visitIfExpression(@NotNull KtIfExpression expression, ExpressionTypingContext context) {
        return controlStructures.visitIfExpression(expression, context);
    }

    @Override
    public KotlinTypeInfo visitWhenExpression(@NotNull KtWhenExpression expression, ExpressionTypingContext context) {
        return patterns.visitWhenExpression(expression, context, true);
    }

    @Override
    public KotlinTypeInfo visitBlockExpression(@NotNull KtBlockExpression expression, ExpressionTypingContext context) {
        return components.expressionTypingServices.getBlockReturnedType(expression, context, true);
    }

    @Override
    public KotlinTypeInfo visitLabeledExpression(@NotNull KtLabeledExpression expression, ExpressionTypingContext context) {
        return basic.visitLabeledExpression(expression, context, true);
    }
}
