/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.Stack;
import kotlin.Pair;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.backend.common.CodegenUtil;
import org.jetbrains.kotlin.backend.common.SamType;
import org.jetbrains.kotlin.builtins.KotlinBuiltIns;
import org.jetbrains.kotlin.codegen.binding.CalculatedClosure;
import org.jetbrains.kotlin.codegen.binding.CodegenBinding;
import org.jetbrains.kotlin.codegen.binding.MutableClosure;
import org.jetbrains.kotlin.codegen.context.*;
import org.jetbrains.kotlin.codegen.coroutines.CoroutineCodegenForLambda;
import org.jetbrains.kotlin.codegen.coroutines.CoroutineCodegenUtilKt;
import org.jetbrains.kotlin.codegen.coroutines.ResolvedCallWithRealDescriptor;
import org.jetbrains.kotlin.codegen.coroutines.SuspensionPointKind;
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension;
import org.jetbrains.kotlin.codegen.inline.*;
import org.jetbrains.kotlin.codegen.intrinsics.*;
import org.jetbrains.kotlin.codegen.pseudoInsns.PseudoInsnsKt;
import org.jetbrains.kotlin.codegen.range.RangeValue;
import org.jetbrains.kotlin.codegen.range.RangeValuesKt;
import org.jetbrains.kotlin.codegen.range.forLoop.ForLoopGenerator;
import org.jetbrains.kotlin.codegen.signature.BothSignatureWriter;
import org.jetbrains.kotlin.codegen.signature.JvmSignatureWriter;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.codegen.state.KotlinTypeMapper;
import org.jetbrains.kotlin.codegen.when.SwitchCodegen;
import org.jetbrains.kotlin.codegen.when.SwitchCodegenProvider;
import org.jetbrains.kotlin.config.ApiVersion;
import org.jetbrains.kotlin.config.JVMAssertionsMode;
import org.jetbrains.kotlin.config.JVMConfigurationKeys;
import org.jetbrains.kotlin.config.LanguageFeature;
import org.jetbrains.kotlin.descriptors.*;
import org.jetbrains.kotlin.descriptors.impl.AnonymousFunctionDescriptor;
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor;
import org.jetbrains.kotlin.descriptors.impl.SyntheticFieldDescriptor;
import org.jetbrains.kotlin.descriptors.impl.TypeAliasConstructorDescriptor;
import org.jetbrains.kotlin.diagnostics.Errors;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.load.java.DescriptorsJvmAbiUtil;
import org.jetbrains.kotlin.load.java.descriptors.JavaPropertyDescriptor;
import org.jetbrains.kotlin.load.kotlin.DescriptorBasedTypeSignatureMappingKt;
import org.jetbrains.kotlin.load.kotlin.MethodSignatureMappingKt;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.psi.psiUtil.PsiUtilsKt;
import org.jetbrains.kotlin.resolve.*;
import org.jetbrains.kotlin.resolve.calls.util.CallResolverUtilKt;
import org.jetbrains.kotlin.resolve.calls.util.CallUtilKt;
import org.jetbrains.kotlin.resolve.calls.inference.CapturedTypeConstructorKt;
import org.jetbrains.kotlin.resolve.calls.model.*;
import org.jetbrains.kotlin.resolve.calls.util.CallMaker;
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForObject;
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForTypeAliasObject;
import org.jetbrains.kotlin.resolve.calls.util.UnderscoreUtilKt;
import org.jetbrains.kotlin.resolve.checkers.PrimitiveNumericComparisonInfo;
import org.jetbrains.kotlin.resolve.constants.*;
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ConstantExpressionEkonstuatorKt;
import org.jetbrains.kotlin.resolve.descriptorUtil.DescriptorUtilsKt;
import org.jetbrains.kotlin.resolve.inline.InlineUtil;
import org.jetbrains.kotlin.resolve.jvm.AsmTypes;
import org.jetbrains.kotlin.resolve.jvm.JvmBindingContextSlices;
import org.jetbrains.kotlin.resolve.jvm.JvmConstantsKt;
import org.jetbrains.kotlin.resolve.jvm.RuntimeAssertionInfo;
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKt;
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodParameterKind;
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodParameterSignature;
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature;
import org.jetbrains.kotlin.resolve.sam.SamConstructorDescriptor;
import org.jetbrains.kotlin.resolve.scopes.receivers.*;
import org.jetbrains.kotlin.synthetic.SyntheticJavaPropertyDescriptor;
import org.jetbrains.kotlin.types.*;
import org.jetbrains.kotlin.types.checker.ClassicTypeSystemContextImpl;
import org.jetbrains.kotlin.types.expressions.DoubleColonLHS;
import org.jetbrains.kotlin.types.model.TypeParameterMarker;
import org.jetbrains.kotlin.types.typesApproximation.CapturedTypeApproximationKt;
import org.jetbrains.kotlin.util.OperatorNameConventions;
import org.jetbrains.org.objectweb.asm.Label;
import org.jetbrains.org.objectweb.asm.MethodVisitor;
import org.jetbrains.org.objectweb.asm.Opcodes;
import org.jetbrains.org.objectweb.asm.Type;
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter;
import org.jetbrains.org.objectweb.asm.commons.Method;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.jetbrains.kotlin.builtins.KotlinBuiltIns.isInt;
import static org.jetbrains.kotlin.codegen.AsmUtil.boxType;
import static org.jetbrains.kotlin.codegen.AsmUtil.*;
import static org.jetbrains.kotlin.codegen.BaseExpressionCodegenKt.putReifiedOperationMarkerIfTypeIsReifiedParameter;
import static org.jetbrains.kotlin.codegen.CodegenUtilKt.*;
import static org.jetbrains.kotlin.codegen.DescriptorAsmUtil.boxType;
import static org.jetbrains.kotlin.codegen.DescriptorAsmUtil.*;
import static org.jetbrains.kotlin.codegen.JvmCodegenUtil.*;
import static org.jetbrains.kotlin.codegen.binding.CodegenBinding.*;
import static org.jetbrains.kotlin.codegen.inline.InlineCodegenUtilsKt.*;
import static org.jetbrains.kotlin.resolve.BindingContext.*;
import static org.jetbrains.kotlin.resolve.BindingContextUtils.getDelegationConstructorCall;
import static org.jetbrains.kotlin.resolve.BindingContextUtils.isBoxedLocalCapturedInClosure;
import static org.jetbrains.kotlin.resolve.DescriptorUtils.*;
import static org.jetbrains.kotlin.resolve.descriptorUtil.DescriptorUtilsKt.getInlineClassRepresentation;
import static org.jetbrains.kotlin.resolve.jvm.AsmTypes.*;
import static org.jetbrains.kotlin.types.RangeUtilKt.isPrimitiveNumberClassDescriptor;
import static org.jetbrains.kotlin.types.expressions.ExpressionTypingUtils.isFunctionExpression;
import static org.jetbrains.kotlin.types.expressions.ExpressionTypingUtils.isFunctionLiteral;
import static org.jetbrains.org.objectweb.asm.Opcodes.*;

public class ExpressionCodegen extends KtVisitor<StackValue, StackValue> implements LocalLookup, BaseExpressionCodegen {
    private final GenerationState state;
    final KotlinTypeMapper typeMapper;
    private final BindingContext bindingContext;

    public final InstructionAdapter v;
    public final FrameMap myFrameMap;
    public final MethodContext context;
    private final Type returnType;

    private final CodegenStatementVisitor statementVisitor = new CodegenStatementVisitor(this);
    private final MemberCodegen<?> parentCodegen;
    private final TailRecursionCodegen tailRecursionCodegen;
    public final CallGenerator defaultCallGenerator = new CallGenerator.DefaultCallGenerator(this);
    private final SwitchCodegenProvider switchCodegenProvider;
    private final TypeSystemCommonBackendContext typeSystem;

    private final Stack<BlockStackElement> blockStackElements = new Stack<>();

    /*
     * When we create a temporary variable to hold some konstue not to compute it many times
     * we put it into this map to emit access to that variable instead of ekonstuating the whole expression
     */
    public final Map<KtElement, StackValue> tempVariables = new HashMap<>();

    private int myLastLineNumber = -1;
    private boolean shouldMarkLineNumbers = true;
    private int finallyDepth = 0;

    public ExpressionCodegen(
            @NotNull MethodVisitor mv,
            @NotNull FrameMap frameMap,
            @NotNull Type returnType,
            @NotNull MethodContext context,
            @NotNull GenerationState state,
            @NotNull MemberCodegen<?> parentCodegen
    ) {
        this.state = state;
        this.typeMapper = state.getTypeMapper();
        this.bindingContext = state.getBindingContext();
        this.v = new InstructionAdapter(mv);
        this.myFrameMap = frameMap;
        this.context = context;
        this.returnType = returnType;

        this.parentCodegen = parentCodegen;
        this.tailRecursionCodegen = new TailRecursionCodegen(context, this, this.v, state);
        this.switchCodegenProvider = new SwitchCodegenProvider(this);
        this.typeSystem = new ClassicTypeSystemContextImpl(state.getModule().getBuiltIns());
    }

    @Nullable
    private static FunctionDescriptor getOriginalSuspendLambdaDescriptorFromContext(MethodContext context) {
        if ((context.getParentContext() instanceof ClosureContext) &&
            (context.getParentContext().closure != null) &&
            context.getParentContext().closure.isSuspend()) {
            return ((ClosureContext) context.getParentContext()).getOriginalSuspendLambdaDescriptor();
        }

        return null;
    }

    public static class BlockStackElement {
    }

    static class LoopBlockStackElement extends BlockStackElement {
        final Label continueLabel;
        final Label breakLabel;
        public final KtSimpleNameExpression targetLabel;

        LoopBlockStackElement(Label breakLabel, Label continueLabel, KtSimpleNameExpression targetLabel) {
            this.breakLabel = breakLabel;
            this.continueLabel = continueLabel;
            this.targetLabel = targetLabel;
        }
    }

    static class TryBlockStackElement extends BlockStackElement {
        List<Label> gaps = new ArrayList<>();

        void addGapLabel(Label label){
            gaps.add(label);
        }
    }

    static class TryWithFinallyBlockStackElement extends TryBlockStackElement {
        final KtTryExpression expression;

        TryWithFinallyBlockStackElement(KtTryExpression expression) {
            this.expression = expression;
        }
    }

    @NotNull
    public GenerationState getState() {
        return state;
    }

    @NotNull
    public BindingContext getBindingContext() {
        return bindingContext;
    }

    @NotNull
    public MemberCodegen<?> getParentCodegen() {
        return parentCodegen;
    }

    @NotNull
    public KotlinTypeMapper getTypeMapper() {
        return typeMapper;
    }

    @NotNull
    public ObjectLiteralResult generateObjectLiteral(@NotNull KtObjectLiteralExpression literal) {
        KtObjectDeclaration objectDeclaration = literal.getObjectDeclaration();

        ClassDescriptor classDescriptor = bindingContext.get(CLASS, objectDeclaration);
        assert classDescriptor != null;

        Type asmType = asmTypeForAnonymousClass(bindingContext, objectDeclaration);
        ClassBuilder classBuilder = state.getFactory().newVisitor(
                JvmDeclarationOriginKt.OtherOrigin(objectDeclaration, classDescriptor),
                asmType,
                literal.getContainingFile()
        );

        ClassContext objectContext = context.intoAnonymousClass(classDescriptor, this, OwnerKind.IMPLEMENTATION);

        MemberCodegen literalCodegen = new ImplementationBodyCodegen(
                objectDeclaration, objectContext, classBuilder, state, getParentCodegen(),
                /* isLocal = */ true);
        literalCodegen.generate();

        addReifiedParametersFromSignature(literalCodegen, classDescriptor);
        propagateChildReifiedTypeParametersUsages(literalCodegen.getReifiedTypeParametersUsages());

        return new ObjectLiteralResult(
                literalCodegen.getReifiedTypeParametersUsages().wereUsedReifiedParameters(),
                classDescriptor
        );
    }

    private static void addReifiedParametersFromSignature(@NotNull MemberCodegen<?> member, @NotNull ClassDescriptor descriptor) {
        for (KotlinType type : descriptor.getTypeConstructor().getSupertypes()) {
            processTypeArguments(member, type);
        }
    }

    private static void processTypeArguments(@NotNull MemberCodegen<?> member, KotlinType type) {
        for (TypeProjection supertypeArgument : type.getArguments()) {
            if (supertypeArgument.isStarProjection()) continue;

            TypeParameterDescriptor parameterDescriptor = TypeUtils.getTypeParameterDescriptorOrNull(supertypeArgument.getType());
            if (parameterDescriptor != null) {
                if (parameterDescriptor.isReified()) {
                    member.getReifiedTypeParametersUsages().addUsedReifiedParameter(parameterDescriptor.getName().asString());
                }
            } else {
                processTypeArguments(member, supertypeArgument.getType());
            }
        }
    }

    private static class ObjectLiteralResult {
        private final boolean wereReifiedMarkers;
        private final ClassDescriptor classDescriptor;

        public ObjectLiteralResult(boolean wereReifiedMarkers, @NotNull ClassDescriptor classDescriptor) {
            this.wereReifiedMarkers = wereReifiedMarkers;
            this.classDescriptor = classDescriptor;
        }
    }

    @NotNull
    private StackValue castToRequiredTypeOfInterfaceIfNeeded(
            StackValue inner,
            @NotNull ClassDescriptor provided,
            @NotNull ClassDescriptor required
    ) {
        if (!isJvmInterface(provided) && isJvmInterface(required)) {
            SimpleType requiredDefaultType = required.getDefaultType();
            return StackValue.coercion(inner, asmType(requiredDefaultType), requiredDefaultType);
        }

        return inner;
    }

    public StackValue genQualified(StackValue receiver, KtElement selector) {
        return genQualified(receiver, selector, this);
    }

    private StackValue genQualified(StackValue receiver, KtElement selector, KtVisitor<StackValue, StackValue> visitor) {
        if (tempVariables.containsKey(selector)) {
            throw new IllegalStateException("Inconsistent state: expression saved to a temporary variable is a selector");
        }
        if (!(selector instanceof KtBlockExpression)) {
            markStartLineNumber(selector);
        }
        try {
            if (selector instanceof KtExpression) {
                StackValue samValue = genSamInterfaceValue((KtExpression) selector, visitor);
                if (samValue != null) {
                    return samValue;
                }
            }

            StackValue stackValue = selector.accept(visitor, receiver);

            stackValue = coerceAndBoxInlineClassIfNeeded(selector, stackValue);

            RuntimeAssertionInfo runtimeAssertionInfo = null;
            if (selector instanceof KtExpression) {
                KtExpression expression = (KtExpression) selector;
                runtimeAssertionInfo = bindingContext.get(JvmBindingContextSlices.RUNTIME_ASSERTION_INFO, expression);
                if (runtimeAssertionInfo == null &&
                    state.getLanguageVersionSettings().supportsFeature(LanguageFeature.StrictJavaNullabilityAssertions)) {
                    runtimeAssertionInfo = bindingContext.get(JvmBindingContextSlices.BODY_RUNTIME_ASSERTION_INFO, expression);
                }
            }

            if (BuiltinSpecialBridgesKt.isValueArgumentForCallToMethodWithTypeCheckBarrier(selector, bindingContext)) return stackValue;

            return genNotNullAssertions(state, stackValue, runtimeAssertionInfo);
        }
        catch (ProcessCanceledException | CompilationException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new CompilationException("Failed to generate expression: " + selector.getClass().getSimpleName(), e, selector);
        }
    }

    private StackValue coerceAndBoxInlineClassIfNeeded(KtElement selector, StackValue stackValue) {
        ResolvedCall<? extends CallableDescriptor> resolvedCall = CallUtilKt.getResolvedCall(selector, bindingContext);
        if (resolvedCall == null) return stackValue;
        CallableDescriptor descriptor = resolvedCall.getResultingDescriptor();
        if (!(descriptor instanceof FunctionDescriptor)) return stackValue;
        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
        if (!functionDescriptor.isSuspend()) return stackValue;

        // When we call suspend operator fun invoke using parens, we cannot box receiver as return type inline class
        if (resolvedCall instanceof VariableAsFunctionResolvedCall &&
            functionDescriptor.isOperator() && functionDescriptor.getName().getIdentifier().equals("invoke")
        ) return stackValue;

        KotlinType unboxedInlineClass = CoroutineCodegenUtilKt
                .originalReturnTypeOfSuspendFunctionReturningUnboxedInlineClass(functionDescriptor, typeMapper);

        StackValue stackValueToWrap = stackValue;
        KotlinType originalKotlinType = unboxedInlineClass != null ? unboxedInlineClass : stackValueToWrap.kotlinType;
        Type originalType = unboxedInlineClass != null ? typeMapper.mapType(unboxedInlineClass) : stackValueToWrap.type;

        stackValue = new StackValue(originalType, originalKotlinType) {
            @Override
            public void putSelector(
                    @NotNull Type type, @Nullable KotlinType kotlinType, @NotNull InstructionAdapter v
            ) {
                if (((originalKotlinType != null && InlineClassesUtilsKt.isInlineClassType(originalKotlinType)) ||
                    (kotlinType != null && InlineClassesUtilsKt.isInlineClassType(kotlinType)))
                ) {
                    stackValueToWrap.put(v);
                    // Suspend functions always return Ljava/lang/Object;, so, before inline-class boxing/unboxing we need to generate CHECKCAST
                    StackValue.coerce(OBJECT_TYPE, originalType, v);
                    // Box/unbox inline class
                    StackValue.coerce(originalType, originalKotlinType, type, kotlinType, v);
                } else {
                    // No inline class -> usual coerce is enough
                    stackValueToWrap.put(type, kotlinType, v);
                }
            }
        };
        return stackValue;
    }

    public StackValue gen(KtElement expr) {
        StackValue tempVar = tempVariables.get(expr);
        return tempVar != null ? tempVar : genQualified(StackValue.none(), expr);
    }

    public void gen(KtElement expr, Type type) {
        gen(expr, type, null);
    }

    public void gen(KtElement expr, Type type, KotlinType kotlinType) {
        StackValue konstue = Type.VOID_TYPE.equals(type) ? genStatement(expr) : gen(expr);
        putStackValue(expr, type, kotlinType, konstue);
    }

    private void putStackValue(@Nullable KtElement expr, @NotNull Type type, @Nullable KotlinType kotlinType, @NotNull StackValue konstue) {
        // for repl store the result of the last line into special field
        if (konstue.type != Type.VOID_TYPE) {
            ScriptContext context = getScriptContext();
            if (context != null && expr == context.getLastStatement()) {
                FieldInfo resultFieldInfo = context.getResultFieldInfo();
                if (resultFieldInfo != null) {
                    StackValue.Field resultValue = StackValue.field(resultFieldInfo, StackValue.LOCAL_0);
                    resultValue.store(konstue, v);
                    state.getScriptSpecific().setResultType(resultFieldInfo.getFieldKotlinType());
                    state.getScriptSpecific().setResultFieldName(resultFieldInfo.getFieldName());
                    return;
                }
            }
        }

        konstue.put(type, kotlinType, v);
    }

    @Nullable
    private ScriptContext getScriptContext() {
        CodegenContext context = getContext();
        while (context != null && !(context instanceof ScriptContext)) {
            context = context.getParentContext();
        }
        return (ScriptContext) context;
    }

    private StackValue genLazy(KtElement expr, Type type) {
        StackValue konstue = gen(expr);
        return StackValue.coercion(konstue, type, null);
    }

    private StackValue genLazy(KtElement expr, Type type, KotlinType kotlinType) {
        StackValue konstue = gen(expr);
        return StackValue.coercion(konstue, type, kotlinType);
    }

    private StackValue genStatement(KtElement statement) {
        return genQualified(StackValue.none(), statement, statementVisitor);
    }

    @Override
    public StackValue visitClass(@NotNull KtClass klass, StackValue data) {
        return visitClassOrObject(klass);
    }

    @Override
    public StackValue visitTypeAlias(@NotNull KtTypeAlias typeAlias, StackValue data) {
        return StackValue.none();
    }

    private StackValue visitClassOrObject(KtClassOrObject declaration) {
        ClassDescriptor descriptor = bindingContext.get(CLASS, declaration);
        assert descriptor != null;

        Type asmType = asmTypeForAnonymousClass(bindingContext, declaration);
        ClassBuilder classBuilder = state.getFactory().newVisitor(JvmDeclarationOriginKt.OtherOrigin(declaration, descriptor), asmType, declaration.getContainingFile());

        ClassContext objectContext = context.intoAnonymousClass(descriptor, this, OwnerKind.IMPLEMENTATION);
        new ImplementationBodyCodegen(declaration, objectContext, classBuilder, state, getParentCodegen(), /* isLocal = */ true).generate();

        return StackValue.none();
    }

    @Override
    public StackValue visitObjectDeclaration(@NotNull KtObjectDeclaration declaration, StackValue data) {
        return visitClassOrObject(declaration);
    }

    @Override
    public StackValue visitExpression(@NotNull KtExpression expression, StackValue receiver) {
        throw new UnsupportedOperationException("Codegen for " + expression + " is not yet implemented");
    }

    @Override
    public StackValue visitSuperExpression(@NotNull KtSuperExpression expression, StackValue data) {
        return StackValue.thisOrOuter(this, getSuperCallLabelTarget(context, expression), true, false);
    }

    @NotNull
    public static ClassDescriptor getSuperCallLabelTarget(
            @NotNull CodegenContext<?> context,
            @NotNull KtSuperExpression expression
    ) {
        KotlinType thisTypeForSuperCall = context.getState().getBindingContext().get(BindingContext.THIS_TYPE_FOR_SUPER_EXPRESSION, expression);
        assert thisTypeForSuperCall != null : "This type for superCall ''" + expression.getText() + "'' should be not null!";
        ClassifierDescriptor descriptor = thisTypeForSuperCall.getConstructor().getDeclarationDescriptor();
        assert descriptor instanceof ClassDescriptor :
                "'This' reference target for ''" + expression.getText() + "''should be class descriptor, but was " + descriptor;
        return (ClassDescriptor) descriptor;
    }

    @NotNull
    public Type asmType(@NotNull KotlinType type) {
        return typeMapper.mapType(type);
    }

    @NotNull
    public Type mapTypeAsDeclaration(@NotNull KotlinType type) {
        return typeMapper.mapTypeAsDeclaration(type);
    }

    @NotNull
    public Type expressionType(@Nullable KtExpression expression) {
        return CodegenUtilKt.asmType(expression, typeMapper, bindingContext);
    }

    @Nullable
    public KotlinType kotlinType(@Nullable KtExpression expression) {
        return CodegenUtilKt.kotlinType(expression, bindingContext);
    }

    @Override
    public StackValue visitParenthesizedExpression(@NotNull KtParenthesizedExpression expression, StackValue receiver) {
        return genQualified(receiver, expression.getExpression());
    }

    @Override
    public StackValue visitAnnotatedExpression(@NotNull KtAnnotatedExpression expression, StackValue receiver) {
        return genQualified(receiver, expression.getBaseExpression());
    }

    private static boolean isEmptyExpression(@Nullable KtElement expr) {
        if (expr == null) {
            return true;
        }
        if (expr instanceof KtBlockExpression) {
            KtBlockExpression blockExpression = (KtBlockExpression) expr;
            List<KtExpression> statements = blockExpression.getStatements();
            if (statements.size() == 0 || statements.size() == 1 && isEmptyExpression(statements.get(0))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public StackValue visitIfExpression(@NotNull KtIfExpression expression, StackValue receiver) {
        return generateIfExpression(expression, false);
    }

    /* package */ StackValue generateIfExpression(@NotNull KtIfExpression expression, boolean isStatement) {
        Type asmType = isStatement ? Type.VOID_TYPE : expressionType(expression);
        KotlinType kotlinType = isStatement ? null : kotlinType(expression);
        StackValue condition = gen(expression.getCondition());

        KtExpression thenExpression = expression.getThen();
        KtExpression elseExpression = expression.getElse();

        if (isEmptyExpression(thenExpression)) {
            if (isEmptyExpression(elseExpression)) {
                return StackValue.coercion(condition, asmType, kotlinType);
            }
            return generateSingleBranchIf(condition, expression, elseExpression, false, isStatement);
        }
        else {
            if (isEmptyExpression(elseExpression)) {
                return generateSingleBranchIf(condition, expression, thenExpression, true, isStatement);
            }
        }

        return StackValue.operation(asmType, kotlinType, v -> {
            Label elseLabel = new Label();
            BranchedValue.Companion.condJump(condition, elseLabel, true, v);

            Label end = new Label();

            gen(thenExpression, asmType, kotlinType);

            v.goTo(end);
            v.mark(elseLabel);

            gen(elseExpression, asmType, kotlinType);

            markLineNumber(expression, isStatement);
            v.mark(end);
            return Unit.INSTANCE;
        });
    }

    @Override
    public StackValue visitWhileExpression(@NotNull KtWhileExpression expression, StackValue receiver) {
        return StackValue.operation(Type.VOID_TYPE, adapter -> {
            generateWhile(expression);
            return Unit.INSTANCE;
        });
    }

    private void generateWhile(@NotNull KtWhileExpression expression) {
        Label condition = new Label();
        v.mark(condition);

        Label end = new Label();
        blockStackElements.push(new LoopBlockStackElement(end, condition, targetLabel(expression)));

        StackValue conditionValue = gen(expression.getCondition());
        BranchedValue.Companion.loopJump(conditionValue, end, true, v);

        generateLoopBody(expression.getBody());

        markStartLineNumber(expression);
        v.goTo(condition);

        v.mark(end);

        blockStackElements.pop();
    }

    @Override
    public StackValue visitDoWhileExpression(@NotNull KtDoWhileExpression expression, StackValue receiver) {
        return StackValue.operation(Type.VOID_TYPE, adapter -> {
            generateDoWhile(expression);
            return Unit.INSTANCE;
        });
    }

    private void generateDoWhile(@NotNull KtDoWhileExpression expression) {
        Label beginLoopLabel = new Label();
        v.mark(beginLoopLabel);

        Label breakLabel = new Label();
        Label continueLabel = new Label();

        blockStackElements.push(new LoopBlockStackElement(breakLabel, continueLabel, targetLabel(expression)));

        PseudoInsnsKt.fakeAlwaysFalseIfeq(v, continueLabel);
        PseudoInsnsKt.fakeAlwaysFalseIfeq(v, breakLabel);

        KtExpression body = expression.getBody();
        KtExpression condition = expression.getCondition();
        StackValue conditionValue;

        StackValueWithLeaveTask leaveTask = null;
        if (body instanceof KtBlockExpression) {
            // If body's a block, it can contain variable declarations which may be used in the condition of a do-while loop.
            // We handle this case separately because otherwise such variable will be out of the frame map after the block ends
            List<KtExpression> doWhileStatements = ((KtBlockExpression) body).getStatements();

            List<KtExpression> statements = new ArrayList<>(doWhileStatements.size() + 1);
            statements.addAll(doWhileStatements);
            statements.add(condition);

            //Need to split leave task and condition cause otherwise BranchedValue optimizations wouldn't work
            leaveTask = generateBlock(statements, false, continueLabel, null);
            conditionValue = leaveTask.getStackValue();
        }
        else {
            if (body != null) {
                gen(body, Type.VOID_TYPE);
            }
            v.mark(continueLabel);
            conditionValue = gen(condition);
        }

        BranchedValue.Companion.loopJump(conditionValue, beginLoopLabel, false, v);
        if (leaveTask != null) {
            leaveTask.getLeaveTasks().invoke(conditionValue);
        }
        v.mark(breakLabel);

        blockStackElements.pop();
    }

    @Override
    public StackValue visitForExpression(@NotNull KtForExpression forExpression, StackValue receiver) {
        return StackValue.operation(Type.VOID_TYPE, adapter -> {
            generateFor(forExpression);
            return Unit.INSTANCE;
        });
    }

    private void generateFor(@NotNull KtForExpression forExpression) {
        KtExpression range = forExpression.getLoopRange();
        assert range != null : "No loop range in for expression";
        RangeValue rangeValue = RangeValuesKt.createRangeValueForExpression(this, range);
        generateForLoop(rangeValue.createForLoopGenerator(this, forExpression));
    }

    @NotNull
    public static KotlinType getExpectedReceiverType(@NotNull ResolvedCall<? extends CallableDescriptor> resolvedCall) {
        ReceiverParameterDescriptor extensionReceiver = resolvedCall.getResultingDescriptor().getExtensionReceiverParameter();
        assert extensionReceiver != null : "Extension receiver should be non-null";
        return extensionReceiver.getType();
    }

    @Nullable
    public static KtExpression getSingleArgumentExpression(@NotNull ResolvedCall<? extends CallableDescriptor> resolvedCall) {
        List<ResolvedValueArgument> resolvedValueArguments = resolvedCall.getValueArgumentsByIndex();
        if (resolvedValueArguments == null) return null;
        if (resolvedValueArguments.size() != 1) return null;
        List<ValueArgument> konstueArguments = resolvedValueArguments.get(0).getArguments();
        if (konstueArguments.size() != 1) return null;
        return konstueArguments.get(0).getArgumentExpression();
    }

    private OwnerKind contextKind() {
        return context.getContextKind();
    }

    private void generateForLoop(ForLoopGenerator generator) {
        Label loopExit = new Label();
        Label loopEntry = new Label();
        Label continueLabel = new Label();

        generator.beforeLoop();
        generator.checkEmptyLoop(loopExit);

        v.mark(loopEntry);
        resetLastLineNumber();
        markStartLineNumber(generator.getForExpression());
        v.nop();

        generator.checkPreCondition(loopExit);

        // Some forms of for-loop can be optimized as post-condition loops.
        PseudoInsnsKt.fakeAlwaysFalseIfeq(v, continueLabel);

        // Renew line number cause it could be reset by inline (resetLastLineNumber) in generator.checkPreCondition(loopExit).
        markStartLineNumber(generator.getForExpression());
        v.nop();
        generator.beforeBody();

        blockStackElements.push(new LoopBlockStackElement(loopExit, continueLabel, targetLabel(generator.getForExpression())));
        generator.body();
        blockStackElements.pop();
        v.mark(continueLabel);
        generator.afterBody(loopExit);

        v.goTo(loopEntry);

        v.mark(loopExit);
        generator.afterLoop();
    }

    public void generateLoopBody(@Nullable KtExpression body) {
        if (body != null) {
            gen(body, Type.VOID_TYPE);
        }
    }


    @Override
    public StackValue visitBreakExpression(@NotNull KtBreakExpression expression, StackValue receiver) {
        return generateBreakOrContinueExpression(expression, true, new Label(), new ArrayList<>());
    }

    @Override
    public StackValue visitContinueExpression(@NotNull KtContinueExpression expression, StackValue receiver) {
        return generateBreakOrContinueExpression(expression, false, new Label(), new ArrayList<>());
    }

    @NotNull
    private StackValue generateBreakOrContinueExpression(
            @NotNull KtExpressionWithLabel expression,
            boolean isBreak,
            @NotNull Label afterBreakContinueLabel,
            @NotNull List<TryBlockStackElement> nestedTryBlocksWithoutFinally
    ) {
        assert expression instanceof KtContinueExpression || expression instanceof KtBreakExpression;

        if (blockStackElements.isEmpty()) {
            throw new UnsupportedOperationException("Target label for break/continue not found");
        }

        BlockStackElement stackElement = blockStackElements.peek();

        if (stackElement instanceof TryWithFinallyBlockStackElement) {
            TryWithFinallyBlockStackElement tryWithFinallyBlockStackElement = (TryWithFinallyBlockStackElement) stackElement;
            genFinallyBlockOrGoto(tryWithFinallyBlockStackElement, null, afterBreakContinueLabel, nestedTryBlocksWithoutFinally);
            nestedTryBlocksWithoutFinally.clear();
        }
        else if (stackElement instanceof TryBlockStackElement) {
            nestedTryBlocksWithoutFinally.add((TryBlockStackElement) stackElement);
        }
        else if (stackElement instanceof LoopBlockStackElement) {
            LoopBlockStackElement loopBlockStackElement = (LoopBlockStackElement) stackElement;
            KtSimpleNameExpression labelElement = expression.getTargetLabel();
            if (labelElement == null ||
                loopBlockStackElement.targetLabel != null &&
                labelElement.getReferencedName().equals(loopBlockStackElement.targetLabel.getReferencedName())) {
                Label label = isBreak ? loopBlockStackElement.breakLabel : loopBlockStackElement.continueLabel;
                return StackValue.operation(
                        Type.VOID_TYPE,
                        getNothingType(),
                        adapter -> {
                            PseudoInsnsKt.fixStackAndJump(v, label);
                            v.mark(afterBreakContinueLabel);
                            return Unit.INSTANCE;
                        }
                );
            }
        }
        else {
            throw new UnsupportedOperationException("Wrong BlockStackElement in processing stack");
        }

        blockStackElements.pop();
        StackValue result = generateBreakOrContinueExpression(expression, isBreak, afterBreakContinueLabel, nestedTryBlocksWithoutFinally);
        blockStackElements.push(stackElement);
        return result;
    }

    private StackValue generateSingleBranchIf(
            StackValue condition,
            KtIfExpression ifExpression,
            KtExpression expression,
            boolean inverse,
            boolean isStatement
    ) {
        Type targetType = isStatement ? Type.VOID_TYPE : expressionType(ifExpression);
        KotlinType targetKotlinType = isStatement ? null : kotlinType(ifExpression);
        return StackValue.operation(targetType, v -> {
            Label elseLabel = new Label();
            BranchedValue.Companion.condJump(condition, elseLabel, inverse, v);

            if (isStatement) {
                gen(expression, Type.VOID_TYPE);
                v.mark(elseLabel);
            }
            else {
                gen(expression, targetType, targetKotlinType);
                Label end = new Label();
                v.goTo(end);

                v.mark(elseLabel);
                StackValue.putUnitInstance(v);

                markStartLineNumber(ifExpression);
                v.mark(end);
            }
            return null;
        });
    }

    @Override
    public StackValue visitConstantExpression(@NotNull KtConstantExpression expression, StackValue receiver) {
        ConstantValue<?> compileTimeValue = getPrimitiveOrStringCompileTimeConstant(expression);
        assert compileTimeValue != null;
        return StackValue.constant(compileTimeValue.getValue(), expressionType(expression), kotlinType(expression));
    }

    @Nullable
    public ConstantValue<?> getCompileTimeConstant(@NotNull KtExpression expression) {
        return getCompileTimeConstant(expression, bindingContext, state.getShouldInlineConstVals());
    }

    @Nullable
    public ConstantValue<?> getPrimitiveOrStringCompileTimeConstant(@NotNull KtExpression expression) {
        return getPrimitiveOrStringCompileTimeConstant(expression, bindingContext, state.getShouldInlineConstVals());
    }

    @Nullable
    public static ConstantValue<?> getPrimitiveOrStringCompileTimeConstant(
            @NotNull KtExpression expression,
            @NotNull BindingContext bindingContext,
            boolean shouldInlineConstVals) {
        ConstantValue<?> constant = getCompileTimeConstant(expression, bindingContext, false, shouldInlineConstVals);
        if (constant == null || ConstantExpressionEkonstuatorKt.isStandaloneOnlyConstant(constant)) {
            return null;
        }
        return constant;
    }

    @Nullable
    public static ConstantValue<?> getCompileTimeConstant(
            @NotNull KtExpression expression,
            @NotNull BindingContext bindingContext,
            boolean shouldInlineConstVals) {
        return getCompileTimeConstant(expression, bindingContext, false, shouldInlineConstVals);
    }

    @Nullable
    public static ConstantValue<?> getCompileTimeConstant(
            @NotNull KtExpression expression,
            @NotNull BindingContext bindingContext,
            boolean takeUpConstValsAsConst,
            boolean shouldInlineConstVals
    ) {
        return JvmConstantsKt.getCompileTimeConstant(expression, bindingContext, takeUpConstValsAsConst, shouldInlineConstVals);
    }

    @Override
    public StackValue visitStringTemplateExpression(@NotNull KtStringTemplateExpression expression, StackValue receiver) {
        List<StringTemplateEntry> entries = preprocessStringTemplate(expression);

        Type type = expressionType(expression);

        if (entries.size() == 0) {
            return StackValue.constant("", type);
        }
        else if (entries.size() == 1) {
            StringTemplateEntry entry = entries.get(0);
            if (entry instanceof StringTemplateEntry.Expression) {
                KtExpression expr = ((StringTemplateEntry.Expression) entry).expression;
                return genToString(gen(expr), expressionType(expr), kotlinType(expr), typeMapper);
            }
            else {
                return StackValue.constant(((StringTemplateEntry.Constant) entry).konstue, type);
            }
        }
        else {
            return StackValue.operation(type, v -> {
                StringConcatGenerator generator = StringConcatGenerator.Companion.create(state, v);
                generator.genStringBuilderConstructorIfNeded();
                invokeAppendForEntries(generator, entries);
                generator.genToString();
                return Unit.INSTANCE;
            });
        }
    }

    private void invokeAppendForEntries(StringConcatGenerator generator, List<StringTemplateEntry> entries) {
        for (StringTemplateEntry entry : entries) {
            if (entry instanceof StringTemplateEntry.Expression) {
                invokeAppend(generator, ((StringTemplateEntry.Expression) entry).expression);
            }
            else {
                String konstue = ((StringTemplateEntry.Constant) entry).konstue;
                if (konstue.length() == 1) {
                    generator.putValueOrProcessConstant(StackValue.constant(konstue.charAt(0), Type.CHAR_TYPE, null));
                }
                else {
                    generator.addStringConstant(konstue);
                }
            }
        }
    }

    private static abstract class StringTemplateEntry {
        static class Constant extends StringTemplateEntry {
            final String konstue;

            Constant(String konstue) {
                this.konstue = konstue;
            }
        }

        static class Expression extends StringTemplateEntry {
            final KtExpression expression;

            Expression(KtExpression expression) {
                this.expression = expression;
            }
        }
    }

    private @NotNull List<StringTemplateEntry> preprocessStringTemplate(@NotNull KtStringTemplateExpression expression) {
        KtStringTemplateEntry[] entries = expression.getEntries();

        List<StringTemplateEntry> result = new ArrayList<>(entries.length);

        StringBuilder constantValue = new StringBuilder("");
        for (KtStringTemplateEntry entry : entries) {
            if (entry instanceof KtLiteralStringTemplateEntry) {
                constantValue.append(entry.getText());
            }
            else if (entry instanceof KtEscapeStringTemplateEntry) {
                constantValue.append(((KtEscapeStringTemplateEntry) entry).getUnescapedValue());
            }
            else if (entry instanceof KtStringTemplateEntryWithExpression) {
                KtExpression entryExpression = entry.getExpression();
                if (entryExpression == null) throw new AssertionError("No expression in " + entry);

                ConstantValue<?> compileTimeConstant = getPrimitiveOrStringCompileTimeConstant(entryExpression);

                if (compileTimeConstant != null && isConstantValueInlinableInStringTemplate(compileTimeConstant)) {
                    constantValue.append(String.konstueOf(compileTimeConstant.getValue()));
                }
                else {
                    String accumulatedConstantValue = constantValue.toString();
                    if (accumulatedConstantValue.length() > 0) {
                        result.add(new StringTemplateEntry.Constant(accumulatedConstantValue));
                    }
                    constantValue.setLength(0);

                    result.add(new StringTemplateEntry.Expression(entryExpression));
                }
            }
            else {
                throw new AssertionError("Unexpected string template entry: " + entry);
            }
        }

        String leftoverConstantValue = constantValue.toString();
        if (leftoverConstantValue.length() > 0) {
            result.add(new StringTemplateEntry.Constant(leftoverConstantValue));
        }

        return result;
    }

    private static boolean isConstantValueInlinableInStringTemplate(@NotNull ConstantValue<?> constant) {
        return constant instanceof StringValue ||
               constant instanceof BooleanValue ||
               constant instanceof DoubleValue ||
               constant instanceof FloatValue ||
               constant instanceof IntegerValueConstant ||
               constant instanceof NullValue;
    }


    @Override
    public StackValue visitBlockExpression(@NotNull KtBlockExpression expression, StackValue receiver) {
        return generateBlock(expression, false);
    }

    @Override
    public StackValue visitNamedFunction(@NotNull KtNamedFunction function, StackValue data) {
        return visitNamedFunction(function, data, false);
    }

    public StackValue visitNamedFunction(@NotNull KtNamedFunction function, StackValue data, boolean isStatement) {
        assert data == StackValue.none();

        if (KtPsiUtil.isScriptDeclaration(function)) {
            return StackValue.none();
        }

        StackValue closure = genClosure(function, null);
        if (isStatement) {
            DeclarationDescriptor descriptor = bindingContext.get(DECLARATION_TO_DESCRIPTOR, function);
            int index = lookupLocalIndex(descriptor);
            closure.put(OBJECT_TYPE, null, v);
            v.store(index, OBJECT_TYPE);
            return StackValue.none();
        }
        else {
            return closure;
        }
    }

    @Override
    public StackValue visitLambdaExpression(@NotNull KtLambdaExpression expression, StackValue receiver) {
        if (Boolean.TRUE.equals(bindingContext.get(BLOCK, expression))) {
            return gen(expression.getFunctionLiteral().getBodyExpression());
        }
        else {
            return genClosure(expression.getFunctionLiteral(), null);
        }
    }

    @NotNull
    private StackValue genClosure(KtDeclarationWithBody declaration, @Nullable SamType samType) {
        FunctionDescriptor descriptor = bindingContext.get(BindingContext.FUNCTION, declaration);
        assert descriptor != null : "Function is not resolved to descriptor: " + declaration.getText();

        return genClosure(
                declaration, descriptor, new ClosureGenerationStrategy(state, declaration), samType, null, null
        );
    }

    @NotNull
    private StackValue genClosure(
            @NotNull KtElement declaration,
            @NotNull FunctionDescriptor descriptor,
            @NotNull FunctionGenerationStrategy strategy,
            @Nullable SamType samType,
            @Nullable ResolvedCall<FunctionDescriptor> functionReferenceCall,
            @Nullable StackValue functionReferenceReceiver
    ) {
        ClassBuilder cv = state.getFactory().newVisitor(
                JvmDeclarationOriginKt.OtherOrigin(declaration, descriptor),
                asmTypeForAnonymousClass(bindingContext, descriptor),
                declaration.getContainingFile()
        );

        ClosureCodegen coroutineCodegen = CoroutineCodegenForLambda.create(this, descriptor, declaration, cv);
        ClosureContext closureContext =
                descriptor.isSuspend()
                ? this.context.intoCoroutineClosure(
                        CoroutineCodegenUtilKt.getOrCreateJvmSuspendFunctionView(descriptor, state),
                        descriptor, this, state.getTypeMapper()
                )
                : this.context.intoClosure(descriptor, this, typeMapper);
        ClosureCodegen closureCodegen =
                coroutineCodegen != null
                ? coroutineCodegen
                : new ClosureCodegen(
                        state, declaration, samType, closureContext, functionReferenceCall, strategy, parentCodegen, cv
                );

        closureCodegen.generate();

        return putClosureInstanceOnStack(closureCodegen, functionReferenceReceiver);
    }

    @NotNull
    private StackValue putClosureInstanceOnStack(
            @NotNull ClosureCodegen closureCodegen,
            @Nullable StackValue functionReferenceReceiver
    ) {
        if (closureCodegen.getReifiedTypeParametersUsages().wereUsedReifiedParameters()) {
            ReifiedTypeInliner.putNeedClassReificationMarker(v);
            propagateChildReifiedTypeParametersUsages(closureCodegen.getReifiedTypeParametersUsages());
        }

        return closureCodegen.putInstanceOnStack(this, functionReferenceReceiver);
    }

    @Override
    public StackValue visitObjectLiteralExpression(@NotNull KtObjectLiteralExpression expression, StackValue receiver) {
        ObjectLiteralResult objectLiteralResult = generateObjectLiteral(expression);
        ClassDescriptor classDescriptor = objectLiteralResult.classDescriptor;
        Type type = typeMapper.mapType(classDescriptor);

        return StackValue.operation(type, v -> {
            if (objectLiteralResult.wereReifiedMarkers) {
                ReifiedTypeInliner.putNeedClassReificationMarker(v);
            }
            v.anew(type);
            v.dup();

            pushClosureOnStack(classDescriptor, true, defaultCallGenerator, /* functionReferenceReceiver = */ null);

            ConstructorDescriptor primaryConstructor = classDescriptor.getUnsubstitutedPrimaryConstructor();
            assert primaryConstructor != null : "There should be primary constructor for object literal";
            ResolvedCall<ConstructorDescriptor> superCall = getDelegationConstructorCall(bindingContext, primaryConstructor);
            if (superCall != null) {
                // For an anonymous object, we should also generate all non-default arguments that it captures for its super call
                ConstructorDescriptor superConstructor = superCall.getResultingDescriptor();
                ConstructorDescriptor constructorToCall = SamCodegenUtil.resolveSamAdapter(superConstructor);
                List<ValueParameterDescriptor> superValueParameters = superConstructor.getValueParameters();
                int params = superValueParameters.size();
                List<Type> superMappedTypes = typeMapper.mapToCallableMethod(constructorToCall, false).getValueParameterTypes();
                assert superMappedTypes.size() >= params : String
                        .format("Incorrect number of mapped parameters vs arguments: %d < %d for %s",
                                superMappedTypes.size(), params, classDescriptor);

                List<ResolvedValueArgument> konstueArguments = new ArrayList<>(params);
                List<ValueParameterDescriptor> konstueParameters = new ArrayList<>(params);
                List<Type> mappedTypes = new ArrayList<>(params);
                for (ValueParameterDescriptor parameter : superCall.getValueArguments().keySet()) {
                    ResolvedValueArgument argument = superCall.getValueArguments().get(parameter);
                    if (!(argument instanceof DefaultValueArgument)) {
                        konstueArguments.add(argument);
                        konstueParameters.add(parameter);
                        mappedTypes.add(superMappedTypes.get(parameter.getIndex()));
                    }
                }
                ArgumentGenerator argumentGenerator =
                        new CallBasedArgumentGenerator(this, defaultCallGenerator, konstueParameters, mappedTypes);

                argumentGenerator.generate(konstueArguments, konstueArguments, null);
            }

            Collection<ClassConstructorDescriptor> constructors = classDescriptor.getConstructors();
            assert constructors.size() == 1 : "Unexpected number of constructors for class: " + classDescriptor + " " + constructors;
            ConstructorDescriptor constructorDescriptor = CollectionsKt.single(constructors);

            Method constructor = typeMapper.mapAsmMethod(SamCodegenUtil.resolveSamAdapter(constructorDescriptor));
            v.invokespecial(type.getInternalName(), "<init>", constructor.getDescriptor(), false);
            return Unit.INSTANCE;
        });
    }

    public void pushClosureOnStack(
            @NotNull ClassDescriptor classDescriptor,
            boolean putThis,
            @NotNull CallGenerator callGenerator,
            @Nullable StackValue functionReferenceReceiver
    ) {
        CalculatedClosure closure = bindingContext.get(CLOSURE, classDescriptor);
        if (closure == null) return;

        int paramIndex = 0;

        if (putThis) {
            ClassDescriptor captureThis = closure.getCapturedOuterClassDescriptor();
            if (captureThis != null) {
                StackValue thisOrOuter = generateThisOrOuter(captureThis, false);
                assert !isPrimitive(thisOrOuter.type) || InlineClassesUtilsKt.isInlineClass(captureThis) :
                        "This or outer for " + captureThis + " should be non-primitive: " + thisOrOuter.type;
                callGenerator.putCapturedValueOnStack(thisOrOuter, thisOrOuter.type, paramIndex++);
            }
        }

        KotlinType captureReceiver = closure.getCapturedReceiverFromOuterContext();
        if (captureReceiver != null) {
            StackValue capturedReceiver = functionReferenceReceiver;
            if (capturedReceiver == null) {
                CallableDescriptor descriptor = unwrapOriginalReceiverOwnerForSuspendLambda(context);
                if (descriptor.getExtensionReceiverParameter() != null) {
                    capturedReceiver = generateExtensionReceiver(descriptor);
                }
            }
            if (capturedReceiver == null) {
                CallableDescriptor descriptor = closure.getEnclosingCallableDescriptorWithReceiver();
                if (descriptor != null) {
                    capturedReceiver = generateExtensionReceiver(descriptor);
                }
            }
            assert capturedReceiver != null : "Captured receiver is null for closure " + closure;
            callGenerator.putCapturedValueOnStack(capturedReceiver, capturedReceiver.type, paramIndex++);
        }

        for (Map.Entry<DeclarationDescriptor, EnclosedValueDescriptor> entry : closure.getCaptureVariables().entrySet()) {
            DeclarationDescriptor declarationDescriptor = entry.getKey();
            EnclosedValueDescriptor konstueDescriptor = entry.getValue();

            Type sharedVarType = typeMapper.getSharedVarType(declarationDescriptor);
            boolean asSharedVar = sharedVarType != null;
            if (sharedVarType == null) {
                sharedVarType = typeMapper.mapType((VariableDescriptor) declarationDescriptor);
            }
            StackValue capturedVar = lookupOuterValue(konstueDescriptor, asSharedVar);
            callGenerator.putCapturedValueOnStack(capturedVar, sharedVarType, paramIndex++);
        }


        ClassDescriptor superClass = DescriptorUtilsKt.getSuperClassNotAny(classDescriptor);
        if (superClass != null) {
            pushClosureOnStack(
                    superClass, putThis && closure.getCapturedOuterClassDescriptor() == null, callGenerator, /* functionReferenceReceiver = */ null
            );
        }

        if (closure.isSuspend()) {
            // resultContinuation
            if (closure.isSuspendLambda()) {
                // When inlining crossinline lambda, the ACONST_NULL is never popped.
                // Thus, do not generate it. Otherwise, it leads to VerifyError on run-time.
                boolean isCrossinlineLambda = (callGenerator instanceof PsiInlineCodegen) &&
                                              Objects.requireNonNull(((PsiInlineCodegen) callGenerator).getActiveLambda(),
                                                                     "no active lambda found").isCrossInline();
                if (!isCrossinlineLambda) {
                    v.aconst(null);
                }
            }
            else if (classDescriptor instanceof SyntheticClassDescriptorForLambda &&
                     ((SyntheticClassDescriptorForLambda) classDescriptor).isCallableReference()) {
                // Constructor of callable reference to suspend function does not accept continuation:
                // do nothing.
            }
            else {
                assert context.getFunctionDescriptor().isSuspend() : "Coroutines closure must be created only inside suspend functions";
                ValueParameterDescriptor continuationParameter = CollectionsKt.last(context.getFunctionDescriptor().getValueParameters());
                StackValue continuationValue = findLocalOrCapturedValue(continuationParameter);

                assert continuationValue != null : "Couldn't find a konstue for continuation parameter of " + context.getFunctionDescriptor();

                callGenerator.putCapturedValueOnStack(continuationValue, continuationValue.type, paramIndex);
            }
        }
    }

    @NotNull
    private static CallableDescriptor unwrapOriginalReceiverOwnerForSuspendLambda(@NotNull MethodContext context) {
        FunctionDescriptor originalForInvokeSuspend =
                context.getFunctionDescriptor().getUserData(CoroutineCodegenUtilKt.INITIAL_SUSPEND_DESCRIPTOR_FOR_INVOKE_SUSPEND);

        if (originalForInvokeSuspend != null) {
            return originalForInvokeSuspend;
        }

        if (context.getFunctionDescriptor().isSuspend()) {
            return CoroutineCodegenUtilKt.unwrapInitialDescriptorForSuspendFunction(context.getFunctionDescriptor());
        }

        return context.getFunctionDescriptor();
    }

    /* package */ StackValue generateBlock(@NotNull KtBlockExpression expression, boolean isStatement) {
        if (expression.getParent() instanceof KtNamedFunction) {
            // For functions end of block should be end of function label
            return generateBlock(expression.getStatements(), isStatement, null, context.getMethodEndLabel());
        }
        return generateBlock(expression.getStatements(), isStatement, null, null);
    }

    @NotNull
    private StackValue lookupOuterValue(EnclosedValueDescriptor d, boolean asSharedVar) {
        DeclarationDescriptor descriptor = d.getDescriptor();
        for (LocalLookup.LocalLookupCase aCase : LocalLookup.LocalLookupCase.konstues()) {
            if (aCase.isCase(descriptor)) {
                StackValue outerValue = aCase.outerValue(d, this);
                if (asSharedVar && outerValue instanceof StackValue.FieldForSharedVar) {
                    StackValue.FieldForSharedVar fieldForSharedVar = (StackValue.FieldForSharedVar) outerValue;
                    return fieldForSharedVar.receiver;
                }
                return outerValue;
            }
        }
        throw new IllegalStateException("Can't get outer konstue in " + this + " for " + d);
    }

    private StackValueWithLeaveTask generateBlock(
            @NotNull List<KtExpression> statements,
            boolean isStatement,
            @Nullable Label labelBeforeLastExpression,
            @Nullable Label labelBlockEnd
    ) {
        Label blockEnd = labelBlockEnd != null ? labelBlockEnd : new Label();

        List<Function<StackValue, Void>> leaveTasks = Lists.newArrayList();

        @Nullable
        StackValue blockResult = null;

        for (Iterator<KtExpression> iterator = statements.iterator(); iterator.hasNext(); ) {
            KtExpression possiblyLabeledStatement = iterator.next();

            KtElement statement = KtPsiUtil.safeDeparenthesize(possiblyLabeledStatement);

            if (statement instanceof KtNamedDeclaration) {
                KtNamedDeclaration declaration = (KtNamedDeclaration) statement;
                if (KtPsiUtil.isScriptDeclaration(declaration)) {
                    continue;
                }
            }

            putDescriptorIntoFrameMap(statement);

            boolean isExpression = !iterator.hasNext() && !isStatement;
            if (isExpression && labelBeforeLastExpression != null) {
                v.mark(labelBeforeLastExpression);
            }

            // Note that this statementResult konstue is potentially unused (in case of handleResult coroutine call)
            // It's supposed here that no bytecode is emitted until 'put' call on relevant StackValue object
            StackValue statementResult = isExpression ? gen(possiblyLabeledStatement) : genStatement(possiblyLabeledStatement);

            if (!iterator.hasNext()) {
                blockResult = statementResult;
            }
            else {
                statementResult.put(Type.VOID_TYPE, null, v);
            }

            addLeaveTaskToRemoveDescriptorFromFrameMap(statement, blockEnd, leaveTasks);
        }

        if (statements.isEmpty()) {
            blockResult = StackValue.none();
        }

        assert blockResult != null : "Block result should be initialized in the loop or the condition above";

        return new StackValueWithLeaveTask(blockResult, konstue -> {
            if (labelBlockEnd == null) {
                v.mark(blockEnd);
            }
            for (Function<StackValue, Void> task : Lists.reverse(leaveTasks)) {
                task.fun(konstue);
            }
            return Unit.INSTANCE;
        });
    }

    @Nullable
    private StackValue getCoroutineInstanceValueForSuspensionPoint(@NotNull ResolvedCall<?> resolvedCall) {
        FunctionDescriptor enclosingSuspendLambdaForSuspensionPoint =
                bindingContext.get(ENCLOSING_SUSPEND_FUNCTION_FOR_SUSPEND_FUNCTION_CALL, resolvedCall.getCall());

        if (enclosingSuspendLambdaForSuspensionPoint == null) return null;
        return genCoroutineInstanceForSuspendLambda(enclosingSuspendLambdaForSuspensionPoint);
    }

    @Nullable
    public StackValue genCoroutineInstanceForSuspendLambda(@NotNull FunctionDescriptor suspendFunction) {
        if (!CoroutineCodegenUtilKt.isSuspendLambdaOrLocalFunction(suspendFunction)) return null;

        ClassDescriptor suspendLambdaClassDescriptor = bindingContext.get(CodegenBinding.CLASS_FOR_CALLABLE, suspendFunction);
        assert suspendLambdaClassDescriptor != null : "Coroutine class descriptor should not be null";

        return StackValue.thisOrOuter(this, suspendLambdaClassDescriptor, false, false);
    }

    @NotNull
    private Type getVariableType(@NotNull VariableDescriptor variableDescriptor) {
        Type sharedVarType = typeMapper.getSharedVarType(variableDescriptor);
        return sharedVarType != null ? sharedVarType : getVariableTypeNoSharing(variableDescriptor);
    }

    @NotNull
    private Type getVariableTypeNoSharing(@NotNull VariableDescriptor variableDescriptor) {
        KotlinType varType = isDelegatedLocalVariable(variableDescriptor)
                             ? JvmCodegenUtil.getPropertyDelegateType((VariableDescriptorWithAccessors) variableDescriptor, bindingContext)
                             : variableDescriptor.getType();

        if (variableDescriptor instanceof ValueParameterDescriptor &&
                MethodSignatureMappingKt.forceSingleValueParameterBoxing(
                        (CallableDescriptor) variableDescriptor.getContainingDeclaration()
                )
        ) {
            //noinspection ConstantConditions
            return asmType(TypeUtils.makeNullable(varType));
        }
        else {
            //noinspection ConstantConditions
            return asmType(varType);
        }
    }

    private void putDescriptorIntoFrameMap(@NotNull KtElement statement) {
        if (statement instanceof KtDestructuringDeclaration) {
            KtDestructuringDeclaration multiDeclaration = (KtDestructuringDeclaration) statement;
            for (KtDestructuringDeclarationEntry entry : multiDeclaration.getEntries()) {
                putLocalVariableIntoFrameMap(entry);
            }
        }

        if (statement instanceof KtVariableDeclaration) {
            putLocalVariableIntoFrameMap((KtVariableDeclaration) statement);
        }

        if (statement instanceof KtNamedFunction) {
            DeclarationDescriptor descriptor = bindingContext.get(DECLARATION_TO_DESCRIPTOR, statement);
            assert descriptor instanceof FunctionDescriptor : "Couldn't find function declaration in binding context " + statement.getText();
            Type type = asmTypeForAnonymousClass(bindingContext, (FunctionDescriptor) descriptor);
            myFrameMap.enter(descriptor, type);
        }
    }

    private void putLocalVariableIntoFrameMap(@NotNull KtVariableDeclaration statement) {
        VariableDescriptor variableDescriptor = getVariableDescriptorNotNull(statement);
        // Do not modify local variables table for variables like _ in konst (_, y) = pair
        // They always will have special name
        if (variableDescriptor.getName().isSpecial()) return;

        Type type = getVariableType(variableDescriptor);
        int index = myFrameMap.enter(variableDescriptor, type);

        if (isDelegatedLocalVariable(variableDescriptor)) {
            myFrameMap.enter(getDelegatedLocalVariableMetadata(variableDescriptor, bindingContext), AsmTypes.K_PROPERTY0_TYPE);
        }

        if (isSharedVarType(type)) {
            markLineNumber(statement, false);
            v.anew(type);
            v.dup();
            v.invokespecial(type.getInternalName(), "<init>", "()V", false);
            v.store(index, OBJECT_TYPE);
        }
    }

    private void addLeaveTaskToRemoveDescriptorFromFrameMap(
            @NotNull KtElement statement,
            @NotNull Label blockEnd,
            @NotNull List<Function<StackValue, Void>> leaveTasks
    ) {
        if (statement instanceof KtDestructuringDeclaration) {
            KtDestructuringDeclaration multiDeclaration = (KtDestructuringDeclaration) statement;
            for (KtDestructuringDeclarationEntry entry : multiDeclaration.getEntries()) {
                addLeaveTaskToRemoveLocalVariableFromFrameMap(entry, blockEnd, leaveTasks);
            }
        }

        if (statement instanceof KtVariableDeclaration) {
            addLeaveTaskToRemoveLocalVariableFromFrameMap((KtVariableDeclaration) statement, blockEnd, leaveTasks);
        }

        if (statement instanceof KtNamedFunction) {
            addLeaveTaskToRemoveNamedFunctionFromFrameMap((KtNamedFunction) statement, blockEnd, leaveTasks);
        }
    }

    private void addLeaveTaskToRemoveLocalVariableFromFrameMap(
            @NotNull KtVariableDeclaration statement,
            Label blockEnd,
            @NotNull List<Function<StackValue, Void>> leaveTasks
    ) {
        VariableDescriptor variableDescriptor = getVariableDescriptorNotNull(statement);

        // Do not modify local variables table for variables like _ in konst (_, y) = pair
        // They always will have special name
        if (variableDescriptor.getName().isSpecial()) return;

        Type type = getVariableType(variableDescriptor);

        Label scopeStart = new Label();
        v.mark(scopeStart);

        leaveTasks.add(answer -> {
            if (isDelegatedLocalVariable(variableDescriptor)) {
                myFrameMap.leave(getDelegatedLocalVariableMetadata(variableDescriptor, bindingContext));
            }

            int index = myFrameMap.leave(variableDescriptor);

            v.visitLocalVariable(variableDescriptor.getName().asString(), type.getDescriptor(), null, scopeStart, blockEnd, index);
            return null;
        });
    }

    private void addLeaveTaskToRemoveNamedFunctionFromFrameMap(
            @NotNull KtNamedFunction localFunction,
            Label blockEnd,
            @NotNull List<Function<StackValue, Void>> leaveTasks
    ) {
        FunctionDescriptor functionDescriptor = (FunctionDescriptor) bindingContext.get(DECLARATION_TO_DESCRIPTOR, localFunction);
        assert functionDescriptor != null;

        Type type = asmTypeForAnonymousClass(bindingContext, functionDescriptor);

        Label scopeStart = new Label();
        v.mark(scopeStart);

        leaveTasks.add(answer -> {
            int index = myFrameMap.leave(functionDescriptor);

            String functionIndex = StringsKt.substringAfterLast(type.getInternalName(), '$', "");
            assert !functionIndex.isEmpty();

            String localVariableName = AsmUtil.LOCAL_FUNCTION_VARIABLE_PREFIX
                    + CommonVariableAsmNameManglingUtils.mangleNameIfNeeded(functionDescriptor.getName().asString())
                    + "$" + functionIndex;

            v.visitLocalVariable(localVariableName, type.getDescriptor(), null, scopeStart, blockEnd, index);
            return null;
        });
    }

    public <T> T runWithShouldMarkLineNumbers(boolean enable, Supplier<T> operation) {
        boolean originalStatus = shouldMarkLineNumbers;
        this.shouldMarkLineNumbers = enable;
        try {
            return operation.get();
        } finally {
            this.shouldMarkLineNumbers = originalStatus;
        }
    }

    public void markStartLineNumber(@NotNull KtElement element) {
        markLineNumber(element, false);
    }

    public void markLineNumber(@NotNull PsiElement statement, boolean markEndOffset) {
        if (!shouldMarkLineNumbers) return;

        Integer lineNumber = CodegenUtil.getLineNumberForElement(statement, markEndOffset);
        if (lineNumber == null || lineNumber == myLastLineNumber) {
            return;
        }
        myLastLineNumber = lineNumber;

        Label label = new Label();
        v.visitLabel(label);
        v.visitLineNumber(lineNumber, label);
    }

    @Override
    public void markLineNumberAfterInlineIfNeeded(boolean registerLineNumberAfterwards) {
        if (!shouldMarkLineNumbers || registerLineNumberAfterwards) {
            //if it used as general argument
            if (myLastLineNumber > -1) {
                Label label = new Label();
                v.visitLabel(label);
                v.visitLineNumber(myLastLineNumber, label);
            }
        } else {
            resetLastLineNumber();
        }
    }

    private void resetLastLineNumber() {
        myLastLineNumber = -1;
    }

    @Override
    public int getLastLineNumber() {
        return myLastLineNumber;
    }

    private boolean doFinallyOnReturn(@NotNull Label afterReturnLabel, @NotNull List<TryBlockStackElement> nestedTryBlocksWithoutFinally) {
        if(!blockStackElements.isEmpty()) {
            BlockStackElement stackElement = blockStackElements.peek();
            if (stackElement instanceof TryWithFinallyBlockStackElement) {
                TryWithFinallyBlockStackElement tryWithFinallyBlockStackElement = (TryWithFinallyBlockStackElement) stackElement;
                genFinallyBlockOrGoto(tryWithFinallyBlockStackElement, null, afterReturnLabel, nestedTryBlocksWithoutFinally);
                nestedTryBlocksWithoutFinally.clear();
            }
            else if (stackElement instanceof TryBlockStackElement)  {
                nestedTryBlocksWithoutFinally.add((TryBlockStackElement) stackElement);
            }
            else if (stackElement instanceof LoopBlockStackElement) {

            } else {
                throw new UnsupportedOperationException("Wrong BlockStackElement in processing stack");
            }

            blockStackElements.pop();
            try {
                return doFinallyOnReturn(afterReturnLabel, nestedTryBlocksWithoutFinally);
            } finally {
                blockStackElements.push(stackElement);
            }
        }
        return false;
    }

    public boolean hasFinallyBlocks() {
        for (BlockStackElement element : blockStackElements) {
            if (element instanceof TryWithFinallyBlockStackElement) {
                return true;
            }
        }
        return false;
    }

    private void genFinallyBlockOrGoto(
            @Nullable TryWithFinallyBlockStackElement tryWithFinallyBlockStackElement,
            @Nullable Label tryCatchBlockEnd,
            @Nullable Label afterJumpLabel,
            @NotNull List<TryBlockStackElement> nestedTryBlocksWithoutFinally
    ) {
        if (tryWithFinallyBlockStackElement != null) {
            finallyDepth++;
            assert tryWithFinallyBlockStackElement.gaps.size() % 2 == 0 : "Finally block gaps are inconsistent";

            BlockStackElement topOfStack = blockStackElements.pop();
            assert topOfStack == tryWithFinallyBlockStackElement : "Top element of stack doesn't equals processing finally block";

            KtTryExpression ktTryExpression = tryWithFinallyBlockStackElement.expression;
            Label finallyStart = linkedLabel();
            v.mark(finallyStart);
            tryWithFinallyBlockStackElement.addGapLabel(finallyStart);
            addGapLabelsForNestedTryCatchWithoutFinally(state, nestedTryBlocksWithoutFinally, finallyStart);
            if (isFinallyMarkerRequired(context)) {
                generateFinallyMarker(v, finallyDepth, true);
            }
            //noinspection ConstantConditions
            gen(ktTryExpression.getFinallyBlock().getFinalExpression(), Type.VOID_TYPE);

            if (isFinallyMarkerRequired(context)) {
                generateFinallyMarker(v, finallyDepth, false);
            }
        }

        if (tryCatchBlockEnd != null) {
            if (tryWithFinallyBlockStackElement != null) {
                markLineNumber(tryWithFinallyBlockStackElement.expression, true);
            }

            v.goTo(tryCatchBlockEnd);
        }

        if (tryWithFinallyBlockStackElement != null) {
            finallyDepth--;
            Label finallyEnd = afterJumpLabel != null ? afterJumpLabel : new Label();
            if (afterJumpLabel == null) {
                v.mark(finallyEnd);
            }
            tryWithFinallyBlockStackElement.addGapLabel(finallyEnd);
            addGapLabelsForNestedTryCatchWithoutFinally(state, nestedTryBlocksWithoutFinally, finallyEnd);

            blockStackElements.push(tryWithFinallyBlockStackElement);
        }
    }

    private static void addGapLabelsForNestedTryCatchWithoutFinally(
            @NotNull GenerationState state,
            @NotNull List<TryBlockStackElement> nestedTryBlocksWithoutFinally,
            @NotNull Label label
    ) {
        if (state.getLanguageVersionSettings().supportsFeature(LanguageFeature.ProperFinally)) {
            for (TryBlockStackElement tryBlock : nestedTryBlocksWithoutFinally) {
                tryBlock.addGapLabel(label);
            }
        }
    }

    private KotlinType getNothingType() {
        return state.getModule().getBuiltIns().getNothingType();
    }

    @Override
    public StackValue visitReturnExpression(@NotNull KtReturnExpression expression, StackValue receiver) {
        return StackValue.operation(Type.VOID_TYPE, getNothingType(), adapter -> {
            KtExpression returnedExpression = expression.getReturnedExpression();
            CallableMemberDescriptor descriptor = getContext().getContextDescriptor();
            NonLocalReturnInfo nonLocalReturn = getNonLocalReturnInfo(descriptor, expression);
            boolean isNonLocalReturn = nonLocalReturn != null;
            if (isNonLocalReturn && state.isInlineDisabled()) {
                state.getDiagnostics().report(Errors.NON_LOCAL_RETURN_IN_DISABLED_INLINE.on(expression));
                genThrow(v, "java/lang/UnsupportedOperationException",
                         "Non-local returns are not allowed with inlining disabled");
                return Unit.INSTANCE;
            }

            Type returnType;
            KotlinType returnKotlinType;
            if (isNonLocalReturn) {
                FunctionDescriptor returnTarget =
                        nonLocalReturn.descriptor instanceof FunctionDescriptor
                        ? (FunctionDescriptor) nonLocalReturn.descriptor
                        : null;
                if (returnTarget == null || !returnTarget.isSuspend()) {
                    JvmKotlinType jvmKotlinType = nonLocalReturn.getJvmKotlinType(typeMapper);
                    returnType = jvmKotlinType.getType();
                    returnKotlinType = jvmKotlinType.getKotlinType();
                } else if (returnTarget instanceof AnonymousFunctionDescriptor) {
                    // Suspend lambdas always return Any?
                    returnType = OBJECT_TYPE;
                    returnKotlinType = state.getModule().getBuiltIns().getNullableAnyType();
                } else {
                    // This is inline lambda, but return target is ordinary, yet suspend, function.
                    // Find inline-site and check, whether it is suspend functions returning unboxed inline class
                    CodegenContext<?> inlineSiteContext = this.context.getFirstCrossInlineOrNonInlineContext();
                    KotlinType originalInlineClass = null;
                    if (inlineSiteContext instanceof MethodContext) {
                        FunctionDescriptor view = CoroutineCodegenUtilKt.getOrCreateJvmSuspendFunctionView(returnTarget, state);
                        originalInlineClass =
                                CoroutineCodegenUtilKt.originalReturnTypeOfSuspendFunctionReturningUnboxedInlineClass(view, typeMapper);
                    }
                    if (originalInlineClass != null) {
                        // As an optimization, suspend functions, returning inline classes with reference underlying
                        // type return unboxed inline class. Save the type so the coercer will not box it.
                        returnType = typeMapper.mapType(originalInlineClass);
                        returnKotlinType = originalInlineClass;
                    } else {
                        JvmKotlinType jvmKotlinType = nonLocalReturn.getJvmKotlinType(typeMapper);
                        returnType = jvmKotlinType.getType();
                        returnKotlinType = jvmKotlinType.getKotlinType();
                    }
                }
            }
            else {
                KotlinType originalInlineClass = CoroutineCodegenUtilKt
                        .originalReturnTypeOfSuspendFunctionReturningUnboxedInlineClass(context.getFunctionDescriptor(), typeMapper);
                if (originalInlineClass != null) {
                    returnType = typeMapper.mapType(originalInlineClass);
                    returnKotlinType = originalInlineClass;
                } else {
                    returnType = this.returnType;
                    returnKotlinType = this.context.getFunctionDescriptor().getReturnType();
                }
            }
            StackValue konstueToReturn = returnedExpression != null ? gen(returnedExpression) : StackValue.none();

            putStackValue(returnedExpression, returnType, returnKotlinType, konstueToReturn);

            Label afterReturnLabel = new Label();
            generateFinallyBlocksIfNeeded(returnType, returnKotlinType, afterReturnLabel);
            markLineNumber(expression, false);
            if (isNonLocalReturn) {
                generateGlobalReturnFlag(v, nonLocalReturn.labelName);
                v.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
            }
            else {
                v.areturn(this.returnType);
            }

            v.mark(afterReturnLabel);
            return Unit.INSTANCE;
        });
    }

    public void generateFinallyBlocksIfNeeded(
            @NotNull Type returnType,
            @Nullable KotlinType returnKotlinType,
            @NotNull Label afterReturnLabel
    ) {
        if (hasFinallyBlocks()) {
            if (!Type.VOID_TYPE.equals(returnType)) {
                int returnValIndex = myFrameMap.enterTemp(returnType);
                StackValue.Local localForReturnValue = StackValue.local(returnValIndex, returnType, returnKotlinType);
                localForReturnValue.store(StackValue.onStack(returnType, returnKotlinType), v);
                doFinallyOnReturn(afterReturnLabel, new ArrayList<>());
                localForReturnValue.put(returnType, null, v);
                myFrameMap.leaveTemp(returnType);
            }
            else {
                doFinallyOnReturn(afterReturnLabel, new ArrayList<>());
            }
        }
    }

    @Nullable
    private NonLocalReturnInfo getNonLocalReturnInfo(@NotNull CallableMemberDescriptor descriptor, @NotNull KtReturnExpression expression) {
        //call inside lambda
        if (isFunctionLiteral(descriptor) || isFunctionExpression(descriptor)) {
            if (expression.getLabelName() == null) {
                if (isFunctionLiteral(descriptor)) {
                    //non labeled return couldn't be local in lambda
                    FunctionDescriptor containingFunction =
                            BindingContextUtils.getContainingFunctionSkipFunctionLiterals(descriptor, true).getFirst();
                    //FIRST_FUN_LABEL to prevent clashing with existing labels
                    return new NonLocalReturnInfo(containingFunction, FIRST_FUN_LABEL);
                } else {
                    //local
                    return null;
                }
            }

            PsiElement element = bindingContext.get(LABEL_TARGET, expression.getTargetLabel());
            if (element != DescriptorToSourceUtils.getSourceFromDescriptor(context.getContextDescriptor())) {
                DeclarationDescriptor elementDescriptor = typeMapper.getBindingContext().get(DECLARATION_TO_DESCRIPTOR, element);
                assert element != null : "Expression should be not null " + expression.getText();
                assert elementDescriptor != null : "Descriptor should be not null: " + element.getText();
                CallableDescriptor function = (CallableDescriptor) elementDescriptor;
                return new NonLocalReturnInfo(function, expression.getLabelName());
            }
        }
        return null;
    }

    public void returnExpression(@NotNull KtExpression expr) {
        boolean isBlockedNamedFunction = expr instanceof KtBlockExpression && expr.getParent() instanceof KtNamedFunction;

        FunctionDescriptor originalSuspendLambdaDescriptor = getOriginalSuspendLambdaDescriptorFromContext(context);
        boolean isVoidCoroutineLambda =
                originalSuspendLambdaDescriptor != null && DescriptorBasedTypeSignatureMappingKt
                        .hasVoidReturnType(originalSuspendLambdaDescriptor);

        KotlinType originalReturnTypeOfSuspendFunctionReturningUnboxedInlineClass =
                CoroutineCodegenUtilKt.originalReturnTypeOfSuspendFunctionReturningUnboxedInlineClass(
                        context.getFunctionDescriptor(), typeMapper);

        // If generating body for named block-bodied function or Unit-typed coroutine lambda, generate it as sequence of statements
        Type typeForExpression =
                isBlockedNamedFunction || isVoidCoroutineLambda
                ? Type.VOID_TYPE
                : originalReturnTypeOfSuspendFunctionReturningUnboxedInlineClass != null
                  ? typeMapper.mapType(originalReturnTypeOfSuspendFunctionReturningUnboxedInlineClass)
                  : returnType;

        KotlinType kotlinTypeForExpression =
                isBlockedNamedFunction || isVoidCoroutineLambda
                ? null
                : originalReturnTypeOfSuspendFunctionReturningUnboxedInlineClass != null
                  ? originalReturnTypeOfSuspendFunctionReturningUnboxedInlineClass
                  : context.getFunctionDescriptor().getReturnType();

        gen(expr, typeForExpression, kotlinTypeForExpression);

        // If it does not end with return we should return something
        // because if we don't there can be VerifyError (specific cases with Nothing-typed expressions)
        if (!endsWithReturn(expr)) {
            if (isLambdaVoidBody(expr, typeForExpression)) {
                markLineNumber((KtFunctionLiteral) expr.getParent(), true);
            } else if (!isLambdaBody(expr)){
                markLineNumber(expr, true);
            }

            if (typeForExpression.getSort() == Type.VOID) {
                StackValue.none().put(returnType, null, v);
            }

            v.areturn(returnType);
        }
    }

    private static boolean endsWithReturn(@NotNull KtElement bodyExpression) {
        if (bodyExpression instanceof KtBlockExpression) {
            List<KtExpression> statements = ((KtBlockExpression) bodyExpression).getStatements();
            return statements.size() > 0 && statements.get(statements.size() - 1) instanceof KtReturnExpression;
        }

        return bodyExpression instanceof KtReturnExpression;
    }

    private static boolean isLambdaVoidBody(@NotNull KtElement bodyExpression, @NotNull Type returnType) {
        return isLambdaBody(bodyExpression) && Type.VOID_TYPE.equals(returnType);
    }

    private static boolean isLambdaBody(@NotNull KtElement bodyExpression) {
        if (bodyExpression instanceof KtBlockExpression) {
            PsiElement parent = bodyExpression.getParent();
            return parent instanceof KtFunctionLiteral;
        }
        return false;
    }

    @Override
    public StackValue visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression, @NotNull StackValue receiver) {
        ResolvedCall<?> resolvedCall = CallUtilKt.getResolvedCall(expression, bindingContext);

        DeclarationDescriptor descriptor;
        if (resolvedCall == null) {
            descriptor = bindingContext.get(REFERENCE_TARGET, expression);
        }
        else {
            if (resolvedCall instanceof VariableAsFunctionResolvedCall) {
                VariableAsFunctionResolvedCall call = (VariableAsFunctionResolvedCall) resolvedCall;
                resolvedCall = call.getVariableCall();
            }

            descriptor = resolvedCall.getResultingDescriptor();

            //Check early if KCallableNameProperty is applicable to prevent closure generation
            StackValue intrinsicResult = applyIntrinsic(descriptor, KCallableNameProperty.class, resolvedCall, receiver);
            if (intrinsicResult != null) return intrinsicResult;

            receiver = StackValue.receiver(resolvedCall, receiver, this, null);
            if (descriptor instanceof FakeCallableDescriptorForObject) {
                descriptor = ((FakeCallableDescriptorForObject) descriptor).getReferencedDescriptor();
            }
        }

        assert descriptor != null : "Couldn't find descriptor for '" + expression.getText() + "'";
        descriptor = descriptor.getOriginal();

        boolean isSyntheticField = descriptor instanceof SyntheticFieldDescriptor;
        if (isSyntheticField) {
            descriptor = ((SyntheticFieldDescriptor) descriptor).getPropertyDescriptor();
        }

        StackValue intrinsicResult = applyIntrinsic(descriptor, IntrinsicPropertyGetter.class, resolvedCall, receiver);
        if (intrinsicResult != null) return intrinsicResult;

        return generateNonIntrinsicSimpleNameExpression(expression, receiver, descriptor, resolvedCall, isSyntheticField);
    }

    public StackValue generateNonIntrinsicSimpleNameExpression(
            @NotNull KtSimpleNameExpression expression,
            @NotNull StackValue receiver,
            @NotNull DeclarationDescriptor descriptor,
            @Nullable ResolvedCall<?> resolvedCall,
            boolean isSyntheticField
    ) {
        if (descriptor instanceof PropertyDescriptor) {
            PropertyDescriptor propertyDescriptor = (PropertyDescriptor) descriptor;

            if (InlineClassesUtilsKt.isUnderlyingPropertyOfInlineClass(propertyDescriptor)) {
                KotlinType propertyType = propertyDescriptor.getType();
                return StackValue.underlyingValueOfInlineClass(typeMapper.mapType(propertyType), propertyType, receiver);
            }

            Collection<ExpressionCodegenExtension> codegenExtensions = ExpressionCodegenExtension.Companion.getInstances(state.getProject());
            if (!codegenExtensions.isEmpty() && resolvedCall != null) {
                ExpressionCodegenExtension.Context context = new ExpressionCodegenExtension.Context(this, typeMapper, v);
                KotlinType returnType = propertyDescriptor.getReturnType();
                for (ExpressionCodegenExtension extension : codegenExtensions) {
                    if (returnType != null) {
                        StackValue konstue = extension.applyProperty(receiver, resolvedCall, context);
                        if (konstue != null) return konstue;
                    }
                }
            }

            boolean directToField = isSyntheticField && contextKind() != OwnerKind.DEFAULT_IMPLS;
            ClassDescriptor superCallTarget = resolvedCall == null ? null : getSuperCallTarget(resolvedCall.getCall());

            if (directToField) {
                receiver = StackValue.receiverWithoutReceiverArgument(receiver);
            }

            return intermediateValueForProperty(propertyDescriptor, directToField, directToField, superCallTarget, false, receiver,
                                                resolvedCall, false);
        }

        if (descriptor instanceof FakeCallableDescriptorForTypeAliasObject) {
            descriptor = ((FakeCallableDescriptorForTypeAliasObject) descriptor).getTypeAliasDescriptor();
        }
        if (descriptor instanceof TypeAliasDescriptor) {
            ClassDescriptor classDescriptor = ((TypeAliasDescriptor) descriptor).getClassDescriptor();
            assert classDescriptor != null :
                    "Type alias " + descriptor + " static member reference should be rejected by type checker, " +
                    "since there is no class corresponding to this type alias.";
            descriptor = classDescriptor;
        }

        if (descriptor instanceof ClassDescriptor) {
            ClassDescriptor classDescriptor = (ClassDescriptor) descriptor;
            if (shouldGenerateSingletonAsThisOrOuterFromContext(classDescriptor)) {
                return generateThisOrOuterFromContext(classDescriptor, false, false);
            }
            if (isCompanionObject(classDescriptor)) {
                return generateCompanionObjectInstance(classDescriptor);
            }
            if (isObject(classDescriptor)) {
                return StackValue.singleton(classDescriptor, typeMapper);
            }
            if (isEnumEntry(classDescriptor)) {
                return StackValue.enumEntry(classDescriptor, typeMapper);
            }
            ClassDescriptor companionObjectDescriptor = classDescriptor.getCompanionObjectDescriptor();
            if (companionObjectDescriptor != null) {
                return generateCompanionObjectInstance(companionObjectDescriptor);
            }
            return StackValue.none();
        }

        StackValue localOrCaptured = findLocalOrCapturedValue(descriptor);
        if (localOrCaptured != null) {
            if (descriptor instanceof ValueParameterDescriptor) {
                KotlinType inlineClassType = ((ValueParameterDescriptor) descriptor).getType();
                if (InlineClassesCodegenUtilKt.isInlineClassWithUnderlyingTypeAnyOrAnyN(inlineClassType) &&
                    InlineClassesCodegenUtilKt.isGenericParameter((CallableDescriptor) descriptor) &&
                    // TODO: HACK around bridgeGenerationCrossinline
                    !(context instanceof InlineLambdaContext) &&
                    // Do not unbox parameters of suspend lambda, they are unboxed in `invoke` method
                    !CoroutineCodegenUtilKt.isInvokeSuspendOfLambda(context.getFunctionDescriptor())
                ) {
                    ClassDescriptor inlineClass = (ClassDescriptor) inlineClassType.getConstructor().getDeclarationDescriptor();
                    InlineClassRepresentation<SimpleType> representation = getInlineClassRepresentation(inlineClass);
                    assert representation != null : "Not an inline class: " + inlineClassType;
                    KotlinType underlyingType = representation.getUnderlyingType();
                    return StackValue.underlyingValueOfInlineClass(typeMapper.mapType(underlyingType), underlyingType, localOrCaptured);
                }
            }
            return localOrCaptured;
        }
        throw new UnsupportedOperationException("don't know how to generate reference " + descriptor);
    }

    private StackValue generateCompanionObjectInstance(ClassDescriptor companionObjectDescriptor) {
        assert isCompanionObject(companionObjectDescriptor) :
                "Companion object expected: " + companionObjectDescriptor;

        CodegenContext accessorContext = getContextForCompanionObjectAccessorIfRequiredOrNull(companionObjectDescriptor, context);
        if (accessorContext == null) {
            return StackValue.singleton(companionObjectDescriptor, typeMapper);
        }

        // Should use accessor
        DeclarationDescriptor accessorContextDescriptor = accessorContext.getContextDescriptor();
        assert accessorContextDescriptor instanceof ClassDescriptor :
                "Expected to put accessor for companion object " + companionObjectDescriptor +
                " into a class, but got " + accessorContextDescriptor;

        accessorContext.getOrCreateAccessorForCompanionObject(companionObjectDescriptor);

        Type hostClassType = typeMapper.mapClass((ClassDescriptor) accessorContextDescriptor);
        Type companionObjectType = typeMapper.mapClass(companionObjectDescriptor);

        // TODO we actually have corresponding accessor descriptor, it might be a better idea to use general method call generation.
        return StackValue.operation(
                companionObjectType,
                companionObjectDescriptor.getDefaultType(),
                v1 -> {
                    v1.invokestatic(
                            hostClassType.getInternalName(),
                            getCompanionObjectAccessorName(companionObjectDescriptor),
                            Type.getMethodDescriptor(companionObjectType),
                            /* itf */ false
                    );
                    return null;
                });
    }

    private boolean shouldGenerateSingletonAsThisOrOuterFromContext(ClassDescriptor classDescriptor) {
        if (!isPossiblyUninitializedSingleton(classDescriptor)) return false;
        if (!isInsideSingleton(classDescriptor)) return false;

        // We are inside a singleton class 'S' with possibly uninitialized static instance
        // (enum entry, interface companion object).
        // Such singleton can be referenced by name, or as an explicit or implicit 'this'.
        // For a given singleton class 'S' we either use 'this@S' from context (local or captured),
        // or 'S' as a static instance.
        //
        // Local or captured 'this@S' should be used if:
        // - we are in the constructor for 'S',
        //   and corresponding instance is initialized by super or delegating constructor call;
        // - we are in any other member of 'S' or any of its inner classes.
        //
        // Otherwise, a static instance should be used.

        CodegenContext context = this.context;
        while (context != null) {
            if (context instanceof ConstructorContext) {
                ConstructorContext constructorContext = (ConstructorContext) context;
                ClassDescriptor constructedClass = constructorContext.getConstructorDescriptor().getConstructedClass();
                if (constructedClass == classDescriptor) {
                    return constructorContext.isThisInitialized();
                }
            }
            else if (context instanceof ClassContext) {
                ClassDescriptor contextClass = ((ClassContext) context).getContextDescriptor();
                if (isInInnerClassesChainFor(contextClass, classDescriptor)) {
                    return true;
                }
            }
            context = context.getParentContext();
        }
        return false;
    }

    private static boolean isInInnerClassesChainFor(ClassDescriptor innerClass, ClassDescriptor outerClass) {
        if (innerClass == outerClass) return true;
        if (!innerClass.isInner()) return false;

        DeclarationDescriptor containingDeclaration = innerClass.getContainingDeclaration();
        if (!(containingDeclaration instanceof ClassDescriptor)) return false;
        return isInInnerClassesChainFor((ClassDescriptor) containingDeclaration, outerClass);
    }

    @Nullable
    private StackValue applyIntrinsic(
            DeclarationDescriptor descriptor,
            Class<? extends IntrinsicPropertyGetter> intrinsicType,
            ResolvedCall<?> resolvedCall,
            @NotNull StackValue receiver
    ) {
        if (descriptor instanceof CallableMemberDescriptor) {
            CallableMemberDescriptor memberDescriptor = DescriptorUtils.unwrapFakeOverride((CallableMemberDescriptor) descriptor);
            IntrinsicMethod intrinsic = state.getIntrinsics().getIntrinsic(memberDescriptor);
            if (intrinsicType.isInstance(intrinsic)) {
                //TODO: intrinsic properties (see intermediateValueForProperty)
                Type returnType = typeMapper.mapType(memberDescriptor);
                return ((IntrinsicPropertyGetter) intrinsic).generate(resolvedCall, this, returnType, receiver);
            }
        }
        return null;
    }

    @Nullable
    public ClassDescriptor getSuperCallTarget(@NotNull Call call) {
        KtSuperExpression superExpression = CallResolverUtilKt.getSuperCallExpression(call);
        return superExpression == null ? null : getSuperCallLabelTarget(context, superExpression);
    }

    @Nullable
    public StackValue findLocalOrCapturedValue(@NotNull DeclarationDescriptor descriptor) {
        int index = lookupLocalIndex(descriptor);
        if (index >= 0) {
            return stackValueForLocal(descriptor, index);
        }

        return findCapturedValue(descriptor);
    }

    @Nullable
    public StackValue findCapturedValue(@NotNull DeclarationDescriptor descriptor) {
        if (context instanceof ConstructorContext) {
            return lookupCapturedValueInConstructorParameters(descriptor);
        }

        StackValue konstue = context.lookupInContext(descriptor, StackValue.LOCAL_0, state, false);
        if (isDelegatedLocalVariable(descriptor) && konstue != null) {
            VariableDescriptor metadata = getDelegatedLocalVariableMetadata((VariableDescriptor) descriptor, bindingContext);
            StackValue metadataValue = context.lookupInContext(metadata, StackValue.LOCAL_0, state, false);
            assert metadataValue != null : "Metadata stack konstue should be non-null for local delegated property: " + descriptor;
            return delegatedVariableValue(konstue, metadataValue, (VariableDescriptorWithAccessors) descriptor, typeMapper);
        }

        return konstue;
    }

    @Nullable
    private StackValue lookupCapturedValueInConstructorParameters(@NotNull DeclarationDescriptor descriptor) {
        StackValue parentResult = context.lookupInContext(descriptor, StackValue.LOCAL_0, state, false);
        if (context.closure == null || parentResult == null) return parentResult;

        int parameterOffsetInConstructor = context.closure.getCapturedParameterOffsetInConstructor(descriptor);
        // when captured parameter is singleton
        // see compiler/testData/codegen/box/objects/objectInLocalAnonymousObject.kt (fun local() captured in A)
        if (parameterOffsetInConstructor == -1) return adjustVariableValue(parentResult, descriptor);

        assert parentResult instanceof StackValue.Field || parentResult instanceof StackValue.FieldForSharedVar
                : "Part of closure should be either Field or FieldForSharedVar";

        if (parentResult instanceof StackValue.FieldForSharedVar) {
            return StackValue.shared(parameterOffsetInConstructor, parentResult.type);
        }

        return adjustVariableValue(StackValue.local(parameterOffsetInConstructor, parentResult.type, parentResult.kotlinType), descriptor);
    }

    private StackValue stackValueForLocal(DeclarationDescriptor descriptor, int index) {
        if (descriptor instanceof VariableDescriptor) {
            VariableDescriptor variableDescriptor = (VariableDescriptor) descriptor;

            Type sharedVarType = typeMapper.getSharedVarType(descriptor);
            Type varType = getVariableTypeNoSharing(variableDescriptor);
            KotlinType delegateKotlinType =
                    isDelegatedLocalVariable(descriptor)
                    ? JvmCodegenUtil.getPropertyDelegateType((VariableDescriptorWithAccessors) descriptor, bindingContext)
                    : null;
            if (sharedVarType != null) {
                return StackValue.shared(index, varType, variableDescriptor, delegateKotlinType);
            }
            else {
                return adjustVariableValue(StackValue.local(index, varType, variableDescriptor, delegateKotlinType), variableDescriptor);
            }
        }
        else {
            return StackValue.local(index, OBJECT_TYPE);
        }
    }

    @Override
    public boolean isLocal(DeclarationDescriptor descriptor) {
        if (lookupLocalIndex(descriptor) != -1) return true;

        if (context.isContextWithUninitializedThis()) {
            LocalLookup outerLookup = context.getParentContext().getEnclosingLocalLookup();
            if (outerLookup != null) {
                return outerLookup.isLocal(descriptor);
            }
        }

        return false;
    }

    public int lookupLocalIndex(DeclarationDescriptor descriptor) {
        int index = myFrameMap.getIndex(descriptor);
        if (index != -1) return index;

        if (!(descriptor instanceof ValueParameterDescriptor)) return -1;
        DeclarationDescriptor synonym = bindingContext.get(CodegenBinding.PARAMETER_SYNONYM, (ValueParameterDescriptor) descriptor);
        if (synonym == null) return -1;

        return myFrameMap.getIndex(synonym);
    }

    @NotNull
    public StackValue.Property intermediateValueForProperty(
            @NotNull PropertyDescriptor propertyDescriptor,
            boolean forceField,
            @Nullable ClassDescriptor superCallTarget,
            @NotNull StackValue receiver
    ) {
        return intermediateValueForProperty(propertyDescriptor, forceField, false, superCallTarget, false, receiver, null, false);
    }

    private CodegenContext getBackingFieldContext(
            @NotNull AccessorKind accessorKind,
            @NotNull DeclarationDescriptor containingDeclaration
    ) {
        switch (accessorKind) {
            case NORMAL:
                if (containingDeclaration instanceof ClassDescriptor) {
                    CodegenContext parentWithDescriptor = context.findParentContextWithDescriptor(containingDeclaration);
                    if (parentWithDescriptor != null) {
                        return parentWithDescriptor;
                    }
                }
                return context.getParentContext();
            // For companion object property, backing field lives in object containing class
            // Otherwise, it lives in its containing declaration
            case IN_CLASS_COMPANION:
                return context.findParentContextWithDescriptor(containingDeclaration.getContainingDeclaration());
            case FIELD_FROM_LOCAL:
                return context.findParentContextWithDescriptor(containingDeclaration);
            case LATEINIT_INTRINSIC:
                return context.findParentContextWithDescriptor(containingDeclaration);
            default:
                throw new IllegalStateException("Unknown field accessor kind: " + accessorKind);
        }
    }

    private static boolean isDefaultAccessor(@Nullable PropertyAccessorDescriptor accessor) {
        return accessor == null || accessor.isDefault();
    }

    public StackValue.Property intermediateValueForProperty(
            @NotNull PropertyDescriptor propertyDescriptor,
            boolean forceField,
            boolean syntheticBackingField,
            @Nullable ClassDescriptor superCallTarget,
            boolean skipAccessorsForPrivateFieldInOuterClass,
            @NotNull StackValue receiver,
            @Nullable ResolvedCall resolvedCall,
            boolean skipLateinitAssertion
    ) {
        if (propertyDescriptor instanceof SyntheticJavaPropertyDescriptor) {
            return intermediateValueForSyntheticExtensionProperty((SyntheticJavaPropertyDescriptor) propertyDescriptor, receiver);
        }

        if (propertyDescriptor instanceof PropertyImportedFromObject) {
            propertyDescriptor = ((PropertyImportedFromObject) propertyDescriptor).getCallableFromObject();
        }

        DeclarationDescriptor containingDeclaration = propertyDescriptor.getContainingDeclaration();

        boolean isBackingFieldMovedFromCompanion = DescriptorsJvmAbiUtil.isPropertyWithBackingFieldInOuterClass(propertyDescriptor);
        AccessorKind fieldAccessorKind;
        if (skipLateinitAssertion) {
            fieldAccessorKind = AccessorKind.LATEINIT_INTRINSIC;
        }
        else if (isBackingFieldMovedFromCompanion &&
                 (forceField ||
                  (DescriptorVisibilities.isPrivate(propertyDescriptor.getVisibility()) &&
                   isDefaultAccessor(propertyDescriptor.getGetter()) && isDefaultAccessor(propertyDescriptor.getSetter())))) {
            fieldAccessorKind = JvmCodegenUtil.isDebuggerContext(context) ? AccessorKind.NORMAL : AccessorKind.IN_CLASS_COMPANION;
        }
        else //noinspection ConstantConditions
            if ((syntheticBackingField &&
                  context.getFirstCrossInlineOrNonInlineContext().getParentContext().getContextDescriptor() != containingDeclaration)) {
            fieldAccessorKind = AccessorKind.FIELD_FROM_LOCAL;
        }
        else {
            fieldAccessorKind = AccessorKind.NORMAL;
        }
        boolean isStaticBackingField = DescriptorUtils.isStaticDeclaration(propertyDescriptor) ||
                                       DescriptorAsmUtil.isInstancePropertyWithStaticBackingField(propertyDescriptor);
        boolean isSuper = superCallTarget != null;
        boolean isExtensionProperty = propertyDescriptor.getExtensionReceiverParameter() != null;

        KotlinType delegateType = JvmCodegenUtil.getPropertyDelegateType(propertyDescriptor, bindingContext);
        boolean isDelegatedProperty = delegateType != null;

        CallableMethod callableGetter = null;
        CallableMethod callableSetter = null;

        CodegenContext<?> backingFieldContext = getBackingFieldContext(fieldAccessorKind, containingDeclaration);
        boolean isPrivateProperty =
                fieldAccessorKind != AccessorKind.NORMAL &&
                (DescriptorAsmUtil.getVisibilityForBackingField(propertyDescriptor, isDelegatedProperty) & ACC_PRIVATE) != 0;
        DeclarationDescriptor ownerDescriptor;
        boolean skipPropertyAccessors;

        PropertyDescriptor originalPropertyDescriptor = DescriptorUtils.unwrapFakeOverride(propertyDescriptor);
        boolean directAccessToGetter =
                couldUseDirectAccessToProperty(propertyDescriptor, true, isDelegatedProperty, context, state.getShouldInlineConstVals());
        boolean directAccessToSetter =
                couldUseDirectAccessToProperty(propertyDescriptor, false, isDelegatedProperty, context, state.getShouldInlineConstVals());

        if (fieldAccessorKind == AccessorKind.LATEINIT_INTRINSIC) {
            skipPropertyAccessors =
                    (!isPrivateProperty || context.getClassOrPackageParentContext() == backingFieldContext) &&
                    !isBackingFieldMovedFromCompanion;

            if (!skipPropertyAccessors) {
                if (isBackingFieldMovedFromCompanion && context.getContextDescriptor() instanceof AccessorForPropertyBackingField) {
                    CodegenContext<?> parentContext = backingFieldContext.getParentContext();
                    propertyDescriptor =
                            parentContext.getAccessor(propertyDescriptor, AccessorKind.IN_CLASS_COMPANION, delegateType, superCallTarget);
                }
                else {
                    propertyDescriptor =
                            backingFieldContext.getAccessor(propertyDescriptor, fieldAccessorKind, delegateType, superCallTarget);
                }
            }
            ownerDescriptor = propertyDescriptor;
        }
        else if (fieldAccessorKind == AccessorKind.IN_CLASS_COMPANION || fieldAccessorKind == AccessorKind.FIELD_FROM_LOCAL) {
            // Do not use accessor 'access<property name>$cp' when the property can be accessed directly by its backing field.
            skipPropertyAccessors = skipAccessorsForPrivateFieldInOuterClass ||
                                    (directAccessToGetter && (!propertyDescriptor.isVar() || directAccessToSetter));

            if (!skipPropertyAccessors) {
                propertyDescriptor = backingFieldContext.getAccessor(propertyDescriptor, fieldAccessorKind, delegateType, superCallTarget);
                assert propertyDescriptor instanceof AccessorForPropertyBackingField :
                        "Unexpected accessor descriptor: " + propertyDescriptor;
                ownerDescriptor = propertyDescriptor;
            }
            else {
                ownerDescriptor = containingDeclaration;
            }
        }
        else {
            skipPropertyAccessors = forceField;

            if (JvmCodegenUtil.isDebuggerContext(context)
                && DescriptorVisibilities.isPrivate(propertyDescriptor.getVisibility())
                && bindingContext.get(BACKING_FIELD_REQUIRED, propertyDescriptor) == Boolean.TRUE
            ) {
                skipPropertyAccessors = true;
            }

            ownerDescriptor = isBackingFieldMovedFromCompanion ? containingDeclaration : propertyDescriptor;
        }

        if (!skipPropertyAccessors) {
            if (!directAccessToGetter || !directAccessToSetter) {
                propertyDescriptor = context.getAccessorForSuperCallIfNeeded(propertyDescriptor, superCallTarget, state);
                propertyDescriptor = context.accessibleDescriptor(propertyDescriptor, superCallTarget);
                if (!isConstOrHasJvmFieldAnnotation(propertyDescriptor)) {
                    if (!directAccessToGetter) {
                        PropertyGetterDescriptor getter = propertyDescriptor.getGetter();
                        if (getter != null) {
                            callableGetter = typeMapper.mapToCallableMethod(getter, isSuper);
                        }
                    }
                    if (propertyDescriptor.isVar() && !directAccessToSetter) {
                        PropertySetterDescriptor setter = propertyDescriptor.getSetter();
                        if (setter != null) {
                            callableSetter = typeMapper.mapToCallableMethod(setter, isSuper);
                        }
                    }
                }
            }
        }

        if (!isStaticBackingField) {
            propertyDescriptor = DescriptorUtils.unwrapFakeOverride(propertyDescriptor);
        }

        Type backingFieldOwner = typeMapper.mapOwner(ownerDescriptor);

        String fieldName;
        if (isExtensionProperty && !isDelegatedProperty) {
            fieldName = null;
        }
        else if (originalPropertyDescriptor.getContainingDeclaration() == backingFieldContext.getContextDescriptor()) {
            assert backingFieldContext instanceof FieldOwnerContext
                    : "Actual context is " + backingFieldContext + " but should be instance of FieldOwnerContext";
            fieldName = ((FieldOwnerContext) backingFieldContext).getFieldName(propertyDescriptor, isDelegatedProperty);
        }
        else {
            fieldName = KotlinTypeMapper.mapDefaultFieldName(propertyDescriptor, isDelegatedProperty);
        }

        KotlinType propertyType = propertyDescriptor.getOriginal().getType();
        if (propertyDescriptor instanceof JavaPropertyDescriptor && InlineClassesUtilsKt.isInlineClassType(propertyType)) {
            propertyType = TypeUtils.makeNullable(propertyType);
        }

        return StackValue.property(
                propertyDescriptor, backingFieldOwner,
                typeMapper.mapType(isDelegatedProperty && forceField ? delegateType : propertyType),
                isStaticBackingField, fieldName, callableGetter, callableSetter, receiver, this, resolvedCall, skipLateinitAssertion,
                isDelegatedProperty && forceField ? delegateType : null
        );
    }

    @NotNull
    private StackValue.Property intermediateValueForSyntheticExtensionProperty(
            @NotNull SyntheticJavaPropertyDescriptor propertyDescriptor,
            @NotNull StackValue receiver
    ) {
        Type type = typeMapper.mapType(propertyDescriptor.getOriginal().getType());
        CallableMethod callableGetter =
                typeMapper.mapToCallableMethod(context.accessibleDescriptor(propertyDescriptor.getGetMethod(), null), false);
        FunctionDescriptor setMethod = propertyDescriptor.getSetMethod();
        CallableMethod callableSetter =
                setMethod != null ? typeMapper.mapToCallableMethod(context.accessibleDescriptor(setMethod, null), false) : null;
        return StackValue.property(propertyDescriptor, null, type, false, null, callableGetter, callableSetter, receiver, this,
                                   null, false, null);
    }

    @Override
    public StackValue visitCallExpression(@NotNull KtCallExpression expression, StackValue receiver) {
        ResolvedCall<?> resolvedCall = CallUtilKt.getResolvedCallWithAssert(expression, bindingContext);
        FunctionDescriptor descriptor = accessibleFunctionDescriptor(resolvedCall);

        if (descriptor instanceof ConstructorDescriptor) {
            return generateNewCall(expression, resolvedCall);
        }

        if (descriptor.getOriginal() instanceof SamConstructorDescriptor) {
            KtExpression argumentExpression = bindingContext.get(SAM_CONSTRUCTOR_TO_ARGUMENT, expression);
            assert argumentExpression != null : "Argument expression is not saved for a SAM constructor: " + descriptor;
            return genSamInterfaceValue(argumentExpression, this);
        }

        return invokeFunction(resolvedCall, receiver);
    }

    @Override
    public StackValue visitCollectionLiteralExpression(
            @NotNull KtCollectionLiteralExpression expression, StackValue data
    ) {
        ResolvedCall<FunctionDescriptor> resolvedCall = bindingContext.get(COLLECTION_LITERAL_CALL, expression);
        assert resolvedCall != null : "No resolved call for " + PsiUtilsKt.getTextWithLocation(expression);
        return invokeFunction(resolvedCall, data);
    }

    @Nullable
    private StackValue genSamInterfaceValue(
            @NotNull KtExpression probablyParenthesizedExpression,
            @NotNull KtVisitor<StackValue, StackValue> visitor
    ) {
        KtExpression expression = KtPsiUtil.deparenthesize(probablyParenthesizedExpression);
        SamType samType = bindingContext.get(SAM_VALUE, probablyParenthesizedExpression);
        if (samType == null || expression == null) return null;

        if (expression instanceof KtLambdaExpression) {
            return genClosure(((KtLambdaExpression) expression).getFunctionLiteral(), samType);
        }

        if (expression instanceof KtNamedFunction) {
            return genClosure((KtNamedFunction) expression, samType);
        }

        Type asmType = state.getSamWrapperClasses().getSamWrapperClass(samType, expression.getContainingKtFile(), this, context.getContextDescriptor());

        return StackValue.operation(asmType, v -> {
            Label afterAll = new Label();

            Type functionType = typeMapper.mapType(samType.getKotlinFunctionType());
            expression.accept(visitor, StackValue.none()).put(functionType, null, v);

            v.dup();
            v.ifnull(afterAll);

            int tmp = myFrameMap.enterTemp(functionType);
            v.store(tmp, functionType);
            v.anew(asmType);
            v.dup();
            v.load(tmp, functionType);
            v.invokespecial(asmType.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, functionType), false);
            myFrameMap.leaveTemp(functionType);

            v.mark(afterAll);

            return null;
        });
    }

    @NotNull
    protected FunctionDescriptor accessibleFunctionDescriptor(@NotNull ResolvedCall<?> resolvedCall) {
        FunctionDescriptor descriptor = (FunctionDescriptor) resolvedCall.getResultingDescriptor();
        if (descriptor instanceof TypeAliasConstructorDescriptor) {
            descriptor = ((TypeAliasConstructorDescriptor) descriptor).getUnderlyingConstructorDescriptor();
        }
        FunctionDescriptor originalIfSamAdapter = SamCodegenUtil.getOriginalIfSamAdapter(descriptor);
        if (originalIfSamAdapter != null) {
            descriptor = originalIfSamAdapter;
        }

        descriptor = CoroutineCodegenUtilKt.unwrapInitialDescriptorForSuspendFunction(descriptor);

        if (CallUtilKt.usesDefaultArguments(resolvedCall)) {
            // $default method is not private, so you need no accessor to call it
            return descriptor;
        }
        else if (shouldForceAccessorForConstructor(descriptor.getOriginal())) {
            return createAccessorForHiddenConstructor(resolvedCall, descriptor);
        }
        else {
            return context.accessibleDescriptor(descriptor, getSuperCallTarget(resolvedCall.getCall()));
        }
    }

    private boolean shouldForceAccessorForConstructor(FunctionDescriptor descriptor) {
        // Force using accessors on hidden constructors only
        if (!isHiddenConstructor(descriptor)) {
            return false;
        }
        // Don't use accessor when calling hidden constructor from the same class.
        if (descriptor.getContainingDeclaration() == context.getContextDescriptor().getContainingDeclaration()) {
            return false;
        }
        return true;
    }

    @NotNull
    private AccessorForConstructorDescriptor createAccessorForHiddenConstructor(
            @NotNull ResolvedCall<?> resolvedCall,
            FunctionDescriptor descriptor
    ) {
        return new AccessorForConstructorDescriptor(
                (ClassConstructorDescriptor) descriptor.getOriginal(),
                descriptor.getContainingDeclaration(),
                getSuperCallTarget(resolvedCall.getCall()),
                AccessorKind.NORMAL
        );
    }

    @NotNull
    public StackValue invokeFunction(@NotNull ResolvedCall<?> resolvedCall, @NotNull StackValue receiver) {
        return invokeFunction(resolvedCall.getCall(), resolvedCall, receiver);
    }

    @NotNull
    public StackValue invokeFunction(@NotNull Call call, @NotNull ResolvedCall<?> resolvedCall, @NotNull StackValue receiver) {
        ResolvedCallWithRealDescriptor callWithRealDescriptor =
                CoroutineCodegenUtilKt.replaceSuspensionFunctionWithRealDescriptor(resolvedCall, state);
        if (callWithRealDescriptor != null) {
            prepareCoroutineArgumentForSuspendCall(resolvedCall, callWithRealDescriptor.getFakeContinuationExpression());
            return invokeFunction(callWithRealDescriptor.getResolvedCall(), receiver);
        }

        FunctionDescriptor fd = accessibleFunctionDescriptor(resolvedCall);
        ClassDescriptor superCallTarget = getSuperCallTarget(call);

        fd = context.getAccessorForSuperCallIfNeeded(fd, superCallTarget, state);

        Collection<ExpressionCodegenExtension> codegenExtensions = ExpressionCodegenExtension.Companion.getInstances(state.getProject());
        if (!codegenExtensions.isEmpty()) {
            ExpressionCodegenExtension.Context context = new ExpressionCodegenExtension.Context(this, typeMapper, v);
            for (ExpressionCodegenExtension extension : codegenExtensions) {
                StackValue stackValue = extension.applyFunction(receiver, resolvedCall, context);
                if (stackValue != null) return stackValue;
            }
        }

        Callable callable = resolveToCallable(fd, superCallTarget != null, resolvedCall);

        return callable.invokeMethodWithArguments(resolvedCall, receiver, this);
    }

    private void prepareCoroutineArgumentForSuspendCall(
            @NotNull ResolvedCall<?> resolvedCall,
            @NotNull KtExpression continuationExpression
    ) {
        StackValue coroutineInstanceValueForSuspensionPoint = getCoroutineInstanceValueForSuspensionPoint(resolvedCall);
        StackValue coroutineInstanceValue =
                coroutineInstanceValueForSuspensionPoint != null
                ? coroutineInstanceValueForSuspensionPoint
                : getContinuationParameterFromEnclosingSuspendFunction(resolvedCall);

        tempVariables.put(continuationExpression, coroutineInstanceValue);
    }

    private StackValue getContinuationParameterFromEnclosingSuspendFunction(@NotNull ResolvedCall<?> resolvedCall) {
        FunctionDescriptor enclosingSuspendFunction =
                bindingContext.get(BindingContext.ENCLOSING_SUSPEND_FUNCTION_FOR_SUSPEND_FUNCTION_CALL, resolvedCall.getCall());

        assert enclosingSuspendFunction != null
                : "Suspend functions may be called either as suspension points or from another suspend function";

        FunctionDescriptor enclosingSuspendFunctionJvmView =
                bindingContext.get(CodegenBinding.SUSPEND_FUNCTION_TO_JVM_VIEW, enclosingSuspendFunction);

        assert enclosingSuspendFunctionJvmView != null : "No JVM view function found for " + enclosingSuspendFunction;

        return getContinuationParameterFromEnclosingSuspendFunctionDescriptor(enclosingSuspendFunctionJvmView);
    }

    @Nullable
    public StackValue getContinuationParameterFromEnclosingSuspendFunctionDescriptor(@NotNull FunctionDescriptor enclosingSuspendFunctionJvmView) {
        ValueParameterDescriptor continuationParameter =
                enclosingSuspendFunctionJvmView.getValueParameters()
                        .get(enclosingSuspendFunctionJvmView.getValueParameters().size() - 1);

        return findLocalOrCapturedValue(continuationParameter);
    }

    @Nullable
    // Find the first parent of the current context which corresponds to a subclass of a given class
    public static CodegenContext getParentContextSubclassOf(ClassDescriptor descriptor, CodegenContext context) {
        CodegenContext c = context;
        while (c != null) {
            if (c instanceof ClassContext && DescriptorUtils.isSubclass(c.getThisDescriptor(), descriptor)) {
                return c;
            }
            c = c.getParentContext();
        }
        return null;
    }

    @Nullable
    public static CodegenContext getCompanionObjectContextSubclassOf(ClassDescriptor descriptor, CodegenContext context) {
        for (CodegenContext current = context; current != null; current = current.getParentContext()) {
            if (!(current instanceof ClassContext)) continue;

            ClassContext classContext = (ClassContext) current;
            ClassDescriptor companionObject = classContext.getContextDescriptor().getCompanionObjectDescriptor();
            if (companionObject == null) continue;

            if (DescriptorUtils.isSubclass(companionObject, descriptor)) {
                return classContext.getCompanionObjectContext();
            }
        }

        return null;
    }

    @NotNull
    Callable resolveToCallable(@NotNull FunctionDescriptor fd, boolean superCall, @NotNull ResolvedCall<?> resolvedCall) {
        IntrinsicMethod intrinsic = state.getIntrinsics().getIntrinsic(fd);
        if (intrinsic != null) {
            return intrinsic.toCallable(fd, superCall, resolvedCall, this);
        }

        fd = SamCodegenUtil.resolveSamAdapter(fd);

        List<ResolvedValueArgument> konstueArguments = resolvedCall.getValueArgumentsByIndex();
        if (konstueArguments != null && konstueArguments.stream().anyMatch(it -> it instanceof DefaultValueArgument)) {
            // When we invoke a function with some arguments mapped as defaults,
            // we later reroute this call to an overridden function in a base class that processes the default arguments.
            // If the base class is generic, this overridden function can have a different Kotlin signature
            // (see KT-38681 and related issues).
            // Here we replace a function with a corresponding overridden function,
            // and the rest is figured out by argument generation and type mapper.
            fd = ArgumentGeneratorKt.getFunctionWithDefaultArguments(fd);
        }

        CallableMethod method = typeMapper.mapToCallableMethod(fd, superCall, null, resolvedCall);

        if (method.getAsmMethod().getName().contains("-") &&
            !state.getConfiguration().getBoolean(JVMConfigurationKeys.USE_OLD_INLINE_CLASSES_MANGLING_SCHEME)
        ) {
            Boolean classFileContainsMethod =
                    InlineClassesCodegenUtilKt.classFileContainsMethod(fd, state, method.getAsmMethod());
            if (classFileContainsMethod != null && !classFileContainsMethod) {
                typeMapper.setUseOldManglingRulesForFunctionAcceptingInlineClass(true);
                method = typeMapper.mapToCallableMethod(fd, superCall, null, resolvedCall);
                typeMapper.setUseOldManglingRulesForFunctionAcceptingInlineClass(false);
            }
        }

        return method;
    }

    public void invokeMethodWithArguments(
            @NotNull Callable callableMethod,
            @NotNull ResolvedCall<?> resolvedCall,
            @NotNull StackValue receiver
    ) {
        CallGenerator callGenerator = getOrCreateCallGenerator(resolvedCall);
        CallableDescriptor descriptor = resolvedCall.getResultingDescriptor();

        assert callGenerator == defaultCallGenerator || !tailRecursionCodegen.isTailRecursion(resolvedCall) :
                "Tail recursive method can't be inlined: " + descriptor;

        ArgumentGenerator argumentGenerator = new CallBasedArgumentGenerator(this, callGenerator, descriptor.getValueParameters(),
                                                                             callableMethod.getValueParameterTypes());

        invokeMethodWithArguments(callableMethod, resolvedCall, receiver, callGenerator, argumentGenerator);
    }

    public void invokeMethodWithArguments(
            @NotNull Callable callableMethod,
            @NotNull ResolvedCall<?> resolvedCall,
            @NotNull StackValue receiver,
            @NotNull CallGenerator callGenerator,
            @NotNull ArgumentGenerator argumentGenerator
    ) {
        if (AssertCodegenUtilKt.isAssertCall(resolvedCall) && !state.getAssertionsMode().equals(JVMAssertionsMode.LEGACY)) {
            AssertCodegenUtilKt.generateAssert(state.getAssertionsMode(), resolvedCall, this, parentCodegen);
            return;
        }

        SuspensionPointKind suspensionPointKind =
                CoroutineCodegenUtilKt.isSuspensionPoint(resolvedCall, this);
        boolean maybeSuspensionPoint = suspensionPointKind != SuspensionPointKind.NEVER && !insideCallableReference();
        boolean isConstructor = resolvedCall.getResultingDescriptor() instanceof ConstructorDescriptor;
        if (!(callableMethod instanceof IntrinsicWithSpecialReceiver)) {
            putReceiverAndInlineMarkerIfNeeded(callableMethod, resolvedCall, receiver, maybeSuspensionPoint, isConstructor);
        }

        callGenerator.processHiddenParameters();
        callGenerator.putHiddenParamsIntoLocals();

        List<ResolvedValueArgument> konstueArguments = resolvedCall.getValueArgumentsByIndex();
        assert konstueArguments != null : "Failed to arrange konstue arguments by index: " + resolvedCall.getResultingDescriptor();

        DefaultCallArgs defaultArgs =
                argumentGenerator.generate(
                        konstueArguments,
                        new ArrayList<>(resolvedCall.getValueArguments().konstues()),
                        resolvedCall.getResultingDescriptor()
                );

        if (tailRecursionCodegen.isTailRecursion(resolvedCall)) {
            tailRecursionCodegen.generateTailRecursion(resolvedCall);
            return;
        }

        boolean defaultMaskWasGenerated = defaultArgs.generateOnStackIfNeeded(callGenerator, isConstructor);

        // Extra constructor marker argument
        if (callableMethod instanceof CallableMethod) {
            List<JvmMethodParameterSignature> callableParameters = ((CallableMethod) callableMethod).getValueParameters();
            for (JvmMethodParameterSignature parameter: callableParameters) {
                if (parameter.getKind() == JvmMethodParameterKind.CONSTRUCTOR_MARKER) {
                    callGenerator.putValueIfNeeded(
                            new JvmKotlinType(parameter.getAsmType(), null), StackValue.constant(null, parameter.getAsmType())
                    );
                }
            }
        }

        if (maybeSuspensionPoint) {
            addSuspendMarker(v, true, suspensionPointKind == SuspensionPointKind.NOT_INLINE);
        }

        callGenerator.genCall(callableMethod, resolvedCall, defaultMaskWasGenerated, this);

        if (maybeSuspensionPoint) {
            // The order is important! Do not reorder!
            // CoroutineTransformerMethodVisitor expects the marker exactly in this order.
            addReturnsUnitMarkerIfNecessary(v, resolvedCall);
            addSuspendMarker(v, false, suspensionPointKind == SuspensionPointKind.NOT_INLINE);
            addUnboxInlineClassMarkersIfNeeded(v, resolvedCall.getResultingDescriptor(), typeMapper);
            addInlineMarker(v, false);
        }

        // If a suspend function returns unboxed inline class, we should check for COROUTINE_SUSPENDED and only then box the inline class.
        if (insideCallableReference() && resolvedCall.getResultingDescriptor() instanceof FunctionDescriptor) {
            KotlinType unboxedInlineClass = CoroutineCodegenUtilKt.originalReturnTypeOfSuspendFunctionReturningUnboxedInlineClass(
                    (FunctionDescriptor) resolvedCall.getResultingDescriptor(), typeMapper);
            if (unboxedInlineClass != null) {
                CoroutineCodegenUtilKt.generateCoroutineSuspendedCheck(v);
            }
        }

        KotlinType returnType = resolvedCall.getResultingDescriptor().getReturnType();
        if (returnType != null && KotlinBuiltIns.isNothing(returnType)) {
            if (state.getUseKotlinNothingValueException()) {
                v.anew(Type.getObjectType("kotlin/KotlinNothingValueException"));
                v.dup();
                v.invokespecial("kotlin/KotlinNothingValueException", "<init>", "()V", false);
            } else {
                v.aconst(null);
            }
            v.athrow();
        }
    }

    private boolean insideCallableReference() {
        return (parentCodegen instanceof ClosureCodegen) && ((ClosureCodegen) parentCodegen).isCallableReference();
    }

    private void putReceiverAndInlineMarkerIfNeeded(
            @NotNull Callable callableMethod,
            @NotNull ResolvedCall<?> resolvedCall,
            @NotNull StackValue receiver,
            boolean isSuspendCall,
            boolean isConstructor
    ) {
        boolean isSafeCallOrOnStack = receiver instanceof StackValue.SafeCall || receiver instanceof StackValue.OnStack;

        if (isSuspendCall && !isSafeCallOrOnStack && !tailRecursionCodegen.isTailRecursion(resolvedCall)) {
            // Inline markers are used to spill the stack before coroutine suspension
            addInlineMarker(v, true);
        }

        if (!isConstructor) { // otherwise already
            receiver = StackValue.receiver(resolvedCall, receiver, this, callableMethod);
            receiver.put(receiver.type, receiver.kotlinType, v);

            // In regular cases we add an inline marker just before receiver is loaded (to spill the stack before a suspension)
            // But in case of safe call things we get the following bytecode:

            // ---- inlineMarkerBefore()
            // LOAD $receiver
            // IFNULL L1
            // ---- load the rest of the arguments
            // INVOKEVIRTUAL suspendCall()
            // ---- inlineMarkerBefore()
            // GOTO L2
            // L1
            // ACONST_NULL
            // L2
            // ...
            //
            // The problem is that the stack before the call is not restored in case of null receiver.
            // The solution is to spill stack just after receiver is loaded (after IFNULL) in case of safe call.
            // But the problem is that we should leave the receiver itself on the stack, so we store it in a temporary variable.
            if (isSuspendCall && isSafeCallOrOnStack) {
                boolean bothReceivers =
                        receiver instanceof CallReceiver
                        && ((CallReceiver) receiver).getDispatchReceiver().type.getSort() != Type.VOID
                        && ((CallReceiver) receiver).getExtensionReceiver().type.getSort() != Type.VOID;
                Type firstReceiverType =
                        bothReceivers
                        ? ((CallReceiver) receiver).getDispatchReceiver().type
                        : receiver.type;

                Type secondReceiverType = bothReceivers ? receiver.type : null;

                int tmpVarForFirstReceiver = myFrameMap.enterTemp(firstReceiverType);
                int tmpVarForSecondReceiver = -1;

                if (secondReceiverType != null) {
                    tmpVarForSecondReceiver = myFrameMap.enterTemp(secondReceiverType);
                    v.store(tmpVarForSecondReceiver, secondReceiverType);
                }
                v.store(tmpVarForFirstReceiver, firstReceiverType);

                addInlineMarker(v, true);

                v.load(tmpVarForFirstReceiver, firstReceiverType);
                if (secondReceiverType != null) {
                    v.load(tmpVarForSecondReceiver, secondReceiverType);
                    myFrameMap.leaveTemp(secondReceiverType);
                }

                myFrameMap.leaveTemp(firstReceiverType);
            }

            callableMethod.afterReceiverGeneration(v, myFrameMap, state);
        }
    }

    @NotNull
    private CallGenerator getOrCreateCallGenerator(
            @NotNull CallableDescriptor descriptor,
            @Nullable KtElement callElement,
            @Nullable TypeParameterMappings<KotlinType> typeParameterMappings,
            boolean isDefaultCompilation
    ) {
        if (callElement == null) return defaultCallGenerator;

        boolean isIntrinsic = descriptor instanceof CallableMemberDescriptor &&
                              state.getIntrinsics().getIntrinsic((CallableMemberDescriptor) descriptor) != null;

        boolean isInline = (InlineUtil.isInline(descriptor) && !isIntrinsic) || InlineUtil.isArrayConstructorWithLambda(descriptor);

        // We should inline callable containing reified type parameters even if inline is disabled
        // because they may contain something to reify and straight call will probably fail at runtime
        boolean shouldInline = isInline && (!state.isInlineDisabled() || InlineUtil.containsReifiedTypeParameters(descriptor));
        if (!shouldInline) return defaultCallGenerator;

        FunctionDescriptor original =
                CoroutineCodegenUtilKt.getOriginalSuspendFunctionView(
                        unwrapInitialSignatureDescriptor(DescriptorUtils.unwrapFakeOverride((FunctionDescriptor) descriptor.getOriginal())),
                        bindingContext
                );

        FunctionDescriptor functionDescriptor =
                InlineUtil.isArrayConstructorWithLambda(original)
                ? FictitiousArrayConstructor.create((ConstructorDescriptor) original) : original.getOriginal();
        PsiSourceCompilerForInline sourceCompiler = new PsiSourceCompilerForInline(this, callElement, functionDescriptor);

        JvmMethodSignature signature = typeMapper.mapSignatureWithGeneric(functionDescriptor, sourceCompiler.getContext().getContextKind());
        if (signature.getAsmMethod().getName().contains("-") &&
            !state.getConfiguration().getBoolean(JVMConfigurationKeys.USE_OLD_INLINE_CLASSES_MANGLING_SCHEME)
        ) {
            Boolean classFileContainsMethod =
                    InlineClassesCodegenUtilKt.classFileContainsMethod(functionDescriptor, state, signature.getAsmMethod());
            if (classFileContainsMethod != null && !classFileContainsMethod) {
                typeMapper.setUseOldManglingRulesForFunctionAcceptingInlineClass(true);
                signature = typeMapper.mapSignatureWithGeneric(functionDescriptor, sourceCompiler.getContext().getContextKind());
                typeMapper.setUseOldManglingRulesForFunctionAcceptingInlineClass(false);
            }
        }
        if (isDefaultCompilation) {
            return new InlineCodegenForDefaultBody(functionDescriptor, this, state, signature, sourceCompiler);
        } else {
            return new PsiInlineCodegen(
                    this, state, functionDescriptor, signature, typeParameterMappings, sourceCompiler,
                    typeMapper.mapImplementationOwner(functionDescriptor), typeMapper.mapOwner(descriptor), callElement
            );
        }
    }

    @NotNull
    protected CallGenerator getOrCreateCallGeneratorForDefaultImplBody(@NotNull FunctionDescriptor descriptor, @Nullable KtNamedFunction function) {
        return getOrCreateCallGenerator(descriptor, function, null, true);
    }

    CallGenerator getOrCreateCallGenerator(@NotNull ResolvedCall<?> resolvedCall) {
        return getOrCreateCallGenerator(resolvedCall, resolvedCall.getResultingDescriptor());
    }

    @NotNull
    CallGenerator getOrCreateCallGenerator(@NotNull ResolvedCall<?> resolvedCall, @NotNull CallableDescriptor descriptor) {
        TypeParameterMappings<KotlinType> mappings = new TypeParameterMappings<>(
                typeSystem,
                getTypeArgumentsForResolvedCall(resolvedCall, descriptor),
                InlineUtil.isArrayConstructorWithLambda(resolvedCall.getResultingDescriptor()),
                (KotlinType type, BothSignatureWriter sw) -> typeMapper.mapTypeArgument(approximateCapturedType(type), sw)
        );
        return getOrCreateCallGenerator(descriptor, resolvedCall.getCall().getCallElement(), mappings, false);
    }

    @NotNull
    private KotlinType approximateCapturedType(@NotNull KotlinType type) {
        if (state.getLanguageVersionSettings().supportsFeature(LanguageFeature.NewInference)) {
            TypeApproximator approximator = new TypeApproximator(state.getModule().getBuiltIns(), state.getLanguageVersionSettings());

            KotlinType approximatedType =
                    TypeUtils.contains(type, (containedType) -> CapturedTypeConstructorKt.isCaptured(containedType)) ?
                    (KotlinType) approximator.approximateToSuperType(
                            type, TypeApproximatorConfiguration.InternalTypesApproximation.INSTANCE
                    ) : null;
            return approximatedType != null ? approximatedType : type;
        } else {
            return CapturedTypeApproximationKt.approximateCapturedTypes(type).getUpper();
        }
    }

    @NotNull
    private static Map<TypeParameterDescriptor, KotlinType> getTypeArgumentsForResolvedCall(
            @NotNull ResolvedCall<?> resolvedCall,
            @NotNull CallableDescriptor descriptor
    ) {
        if (!(descriptor instanceof TypeAliasConstructorDescriptor)) {
            return resolvedCall.getTypeArguments();
        }

        TypeAliasConstructorDescriptor typeAliasConstructorDescriptor = (TypeAliasConstructorDescriptor) descriptor;
        ClassConstructorDescriptor underlyingConstructorDescriptor = typeAliasConstructorDescriptor.getUnderlyingConstructorDescriptor();
        KotlinType resultingType = typeAliasConstructorDescriptor.getReturnType();
        List<TypeProjection> typeArgumentsForReturnType = resultingType.getArguments();
        List<TypeParameterDescriptor> typeParameters = underlyingConstructorDescriptor.getTypeParameters();

        assert typeParameters.size() == typeArgumentsForReturnType.size() :
                "Type parameters of the underlying constructor " + underlyingConstructorDescriptor +
                "should correspond to type arguments for the resulting type " + resultingType;

        Map<TypeParameterDescriptor, KotlinType> typeArgumentsMap = Maps.newHashMapWithExpectedSize(typeParameters.size());
        for (TypeParameterDescriptor typeParameter: typeParameters) {
            KotlinType typeArgument = typeArgumentsForReturnType.get(typeParameter.getIndex()).getType();
            typeArgumentsMap.put(typeParameter, typeArgument);
        }

        return typeArgumentsMap;
    }

    @NotNull
    public StackValue generateReceiverValue(@Nullable ReceiverValue receiverValue, boolean isSuper) {
        if (receiverValue instanceof ImplicitClassReceiver) {
            ClassDescriptor receiverDescriptor = ((ImplicitClassReceiver) receiverValue).getDeclarationDescriptor();
            return generateInstanceReceiver(receiverDescriptor, isSuper,
                                         receiverValue instanceof CastImplicitClassReceiver || isEnumEntry(receiverDescriptor));
        }
        else if (receiverValue instanceof ExtensionReceiver) {
            return generateExtensionReceiver(((ExtensionReceiver) receiverValue).getDeclarationDescriptor());
        }
        else if (receiverValue instanceof ExpressionReceiver) {
            ExpressionReceiver expressionReceiver = (ExpressionReceiver) receiverValue;
            StackValue stackValue = gen(expressionReceiver.getExpression());
            if (!state.isReceiverAssertionsDisabled()) {
                RuntimeAssertionInfo runtimeAssertionInfo =
                        bindingContext.get(JvmBindingContextSlices.RECEIVER_RUNTIME_ASSERTION_INFO, expressionReceiver);
                stackValue = genNotNullAssertions(state, stackValue, runtimeAssertionInfo);
            }
            return stackValue;
        }
        else {
            throw new UnsupportedOperationException("Unsupported receiver konstue: " + receiverValue);
        }
    }

    private StackValue generateInstanceReceiver(
            @NotNull ClassDescriptor receiverDescriptor,
            boolean isSuper,
            boolean castReceiver

    ) {
        if (DescriptorUtils.isCompanionObject(receiverDescriptor)) {
            CallableMemberDescriptor contextDescriptor = context.getContextDescriptor();
            if (contextDescriptor instanceof FunctionDescriptor && receiverDescriptor == contextDescriptor.getContainingDeclaration()) {
                return StackValue.LOCAL_0;
            }
            else if (isPossiblyUninitializedSingleton(receiverDescriptor) && isInsideSingleton(receiverDescriptor)) {
                return generateThisOrOuterFromContext(receiverDescriptor, false, false);
            }
            else {
                return generateCompanionObjectInstance(receiverDescriptor);
            }
        }
        else if (receiverDescriptor instanceof ScriptDescriptor) {
            StackValue contextReceiverValueOrNull = generateScriptReceiver((ScriptDescriptor) receiverDescriptor);

            return contextReceiverValueOrNull != null ? contextReceiverValueOrNull : StackValue.thisOrOuter(this, receiverDescriptor, isSuper, castReceiver);
        }
        else {
            return StackValue.thisOrOuter(this, receiverDescriptor, isSuper, castReceiver);
        }
    }

    private CodegenContext getContextForCompanionObjectAccessorIfRequiredOrNull(
            @NotNull ClassDescriptor companionObjectDescriptor,
            @NotNull CodegenContext contextBeforeInline
    ) {
        if (isDebuggerContext(contextBeforeInline)) {
            return null;
        }

        if (!state.getLanguageVersionSettings().supportsFeature(LanguageFeature.ProperVisibilityForCompanionObjectInstanceField)) {
            return null;
        }

        int accessFlag = DescriptorAsmUtil.getVisibilityAccessFlag(companionObjectDescriptor);

        CodegenContext context = contextBeforeInline.getFirstCrossInlineOrNonInlineContext();
        boolean isInlineMethodContext = context.isInlineMethodContext();
        DeclarationDescriptor contextDescriptor = context.getContextDescriptor();

        ClassDescriptor containingClassOfCompanionObject = DescriptorUtils.getContainingClass(companionObjectDescriptor);

        if ((accessFlag & ACC_PRIVATE) != 0) {
            // Private companion object
            if (!isInlineMethodContext && contextDescriptor.getContainingDeclaration() == containingClassOfCompanionObject) {
                return null;
            }
            return context.findParentContextWithDescriptor(containingClassOfCompanionObject);
        } else if ((accessFlag & ACC_PROTECTED) != 0) {
            // Protected companion object
            if (!isInlineMethodContext && canAccessProtectedMembers(contextDescriptor, containingClassOfCompanionObject)) {
                return null;
            }
            CodegenContext accessorContext = getParentContextSubclassOf(containingClassOfCompanionObject, context);
            if (accessorContext == null) {
                accessorContext = getCompanionObjectContextSubclassOf(containingClassOfCompanionObject, context);
            }
            assert accessorContext != null :
                    "Class context for accessor to a protected companion object " + companionObjectDescriptor + " not found:\n" +
                    JvmCodegenUtil.dumpContextHierarchy(context);
            return accessorContext;
        } else {
            // Non-protected && non-private companion object
            return null;
        }
    }

    private static boolean canAccessProtectedMembers(DeclarationDescriptor contextDescriptor, ClassDescriptor classDescriptor) {
        DeclarationDescriptor containingDeclaration = contextDescriptor.getContainingDeclaration();
        return containingDeclaration == classDescriptor ||
               JvmCodegenUtil.isInSamePackage(contextDescriptor, classDescriptor) ||
               containingDeclaration instanceof ClassDescriptor &&
               DescriptorUtils.isSubclass((ClassDescriptor) containingDeclaration, classDescriptor);
    }

    @NotNull
    public StackValue generateExtensionReceiver(@NotNull CallableDescriptor descriptor) {
        ReceiverParameterDescriptor parameter = descriptor.getExtensionReceiverParameter();
        if (myFrameMap.getIndex(parameter) != -1) {
            KotlinType type = parameter.getReturnType();
            return StackValue.local(
                    myFrameMap.getIndex(parameter),
                    typeMapper.mapType(type),
                    type
            );
        }

        StackValue result = context.generateReceiver(descriptor, state, false);
        if (result == null) {
            throw new NullPointerException("Extension receiver was null for callable " + descriptor);
        }

        return result;
    }

    private StackValue generateScriptReceiver(@NotNull ScriptDescriptor receiver) {
        CodegenContext cur = context;
        StackValue result = StackValue.LOCAL_0;
        boolean inStartConstructorContext = cur instanceof ConstructorContext;
        while (cur != null) {
            if (!inStartConstructorContext) {
                cur = getNotNullParentContextForMethod(cur);
            }

            if (cur instanceof ScriptContext) {
                ScriptContext scriptContext = (ScriptContext) cur;

                if (scriptContext.getScriptDescriptor() == receiver) {
                    //TODO lazy
                    return result;
                }
                Type currentScriptType = typeMapper.mapType(scriptContext.getScriptDescriptor());
                SimpleType receiverKotlinType = receiver.getDefaultType();
                Type classType = typeMapper.mapType(receiverKotlinType);
                String fieldName = scriptContext.getScriptFieldName(receiver);
                return StackValue.field(classType, receiverKotlinType, currentScriptType, fieldName, false, result, receiver);
            }
            // might be null inside the generated synthetic class during expression ekonstuation
            result = cur.getOuterExpression(result, true);

            if (inStartConstructorContext) {
                cur = getNotNullParentContextForMethod(cur);
                inStartConstructorContext = false;
            }

            cur = cur.getParentContext();
        }

        return result;
    }

    @NotNull
    public StackValue generateThisOrOuter(@NotNull ClassDescriptor calleeContainingClass, boolean isSuper) {
        return generateThisOrOuter(calleeContainingClass, isSuper, false);
    }

    private boolean isInsideSingleton(@NotNull ClassDescriptor singletonClassDescriptor) {
        assert singletonClassDescriptor.getKind().isSingleton() :
                "Singleton expected: " + singletonClassDescriptor;

        DeclarationDescriptor descriptor = context.getContextDescriptor();
        while (descriptor != null) {
            if (descriptor == singletonClassDescriptor) return true;

            if (descriptor instanceof ClassDescriptor &&
                !(((ClassDescriptor) descriptor).isInner() || DescriptorUtils.isAnonymousObject(descriptor))) {
                return false;
            }

            descriptor = descriptor.getContainingDeclaration();
        }

        return false;
    }

    @NotNull
    public StackValue generateThisOrOuter(@NotNull ClassDescriptor thisOrOuterClass, boolean isSuper, boolean forceOuter) {
        if (!thisOrOuterClass.getKind().isSingleton()) {
            return generateThisOrOuterFromContext(thisOrOuterClass, isSuper, forceOuter);
        }

        if (thisOrOuterClass.equals(context.getThisDescriptor()) &&
            !CodegenUtilKt.isJvmStaticInObjectOrClassOrInterface(context.getFunctionDescriptor())) {
            return StackValue.local(0, typeMapper.mapType(thisOrOuterClass));
        }
        else if (shouldGenerateSingletonAsThisOrOuterFromContext(thisOrOuterClass)) {
            return generateThisOrOuterFromContext(thisOrOuterClass, isSuper, forceOuter);
        }
        else if (isEnumEntry(thisOrOuterClass)) {
            return StackValue.enumEntry(thisOrOuterClass, typeMapper);
        }
        else {
            return StackValue.singleton(thisOrOuterClass, typeMapper);
        }
    }

    private StackValue generateThisOrOuterFromContext(@NotNull ClassDescriptor thisOrOuterClass, boolean isSuper, boolean forceOuter) {
        CodegenContext cur = context;
        SimpleType contextType = cur.getThisDescriptor().getDefaultType();
        StackValue result = StackValue.local(0, asmType(contextType), contextType);
        boolean inStartConstructorContext = cur instanceof ConstructorContext;
        while (cur != null) {
            ClassDescriptor thisDescriptor = cur.getThisDescriptor();

            // We use equals on type constructors (instead of reference equality on classes) to support the case where default parameter
            // konstues of actual functions loaded from the expected function refer to the expected class.
            if (!isSuper && thisDescriptor.getTypeConstructor().equals(thisOrOuterClass.getTypeConstructor())) {
                return result;
            }

            if (!forceOuter && isSuper && DescriptorUtils.isSubclass(thisDescriptor, thisOrOuterClass)) {
                return castToRequiredTypeOfInterfaceIfNeeded(result, thisDescriptor, thisOrOuterClass);
            }

            forceOuter = false;

            CodegenContext enclosingContext;

            //for constructor super call we should access to outer instance through parameter in locals, in other cases through field for captured outer
            if (inStartConstructorContext) {
                result = cur.getOuterExpression(result, false);
                cur = getNotNullParentContextForMethod(cur);
                enclosingContext = cur.getEnclosingClassContext();
                inStartConstructorContext = false;
            }
            else {
                cur = getNotNullParentContextForMethod(cur);
                enclosingContext = cur.getEnclosingClassContext();

                if (!(thisOrOuterClass instanceof ScriptDescriptor)) {
                    if (cur instanceof ScriptLikeContext) {
                        /* for now the script codegen only passes this branch,
                           since the method context for script constructor is defined using function context */

                        return ((ScriptLikeContext) cur).getOuterReceiverExpression(result, thisOrOuterClass);
                    } else if (enclosingContext instanceof ScriptLikeContext) {
                        MutableClosure closure = cur.closure;
                        if (closure != null) {
                            StackValue captured = ((ScriptLikeContext) enclosingContext).captureVariable(closure, thisOrOuterClass);
                            if (captured != null) {
                                return captured;
                            }
                        }
                    }
                }

                result = cur.getOuterExpression(result, false);
            }

            cur = enclosingContext;
        }

        throw new UnsupportedOperationException();
    }

    @NotNull
    private static CodegenContext getNotNullParentContextForMethod(CodegenContext cur) {
        if (cur instanceof MethodContext) {
            cur = cur.getParentContext();
        }
        assert cur != null;
        return cur;
    }

    private boolean canSkipArrayCopyForSpreadArgument(KtExpression spreadArgument) {
        ResolvedCall<? extends CallableDescriptor> resolvedCall = CallUtilKt.getResolvedCall(spreadArgument, bindingContext);
        if (resolvedCall == null) return false;

        CallableDescriptor calleeDescriptor = resolvedCall.getResultingDescriptor();
        return (calleeDescriptor instanceof ConstructorDescriptor) ||
               CompileTimeConstantUtils.isArrayFunctionCall(resolvedCall) ||
               (DescriptorUtils.getFqName(calleeDescriptor).asString().equals("kotlin.arrayOfNulls"));
    }

    @NotNull
    public StackValue genVarargs(@NotNull VarargValueArgument konstueArgument, @NotNull KotlinType outType) {
        Type type = asmType(outType);
        assert type.getSort() == Type.ARRAY;
        Type elementType = correctElementType(type);
        List<ValueArgument> arguments = konstueArgument.getArguments();
        int size = arguments.size();

        // Named arguments in vararg are treated as having an implicit spread operator.
        // There can be only single such argument for a given vararg parameter, but here it doesn't really matter:
        // we'll just build a copy of array as if an array argument was passed as positional with an explicit spread operator.
        boolean hasSpreadOperator =
                state.getLanguageVersionSettings()
                        .supportsFeature(LanguageFeature.AllowAssigningArrayElementsToVarargsInNamedFormForFunctions)
                ? arguments.stream().anyMatch(argument -> argument.getSpreadElement() != null || argument.isNamed())
                : arguments.stream().anyMatch(argument -> argument.getSpreadElement() != null);

        if (hasSpreadOperator) {
            boolean arrayOfReferences = KotlinBuiltIns.isArray(outType);
            if (size == 1) {
                Type arrayType = getArrayType(arrayOfReferences ? AsmTypes.OBJECT_TYPE : elementType);
                return StackValue.operation(type, outType, adapter -> {
                    KtExpression spreadArgument = arguments.get(0).getArgumentExpression();
                    gen(spreadArgument, type, outType);
                    if (!canSkipArrayCopyForSpreadArgument(spreadArgument)) {
                        v.dup();
                        v.arraylength();
                        v.invokestatic("java/util/Arrays", "copyOf", Type.getMethodDescriptor(arrayType, arrayType, Type.INT_TYPE), false);
                    }
                    if (arrayOfReferences) {
                        v.checkcast(type);
                    }
                    return Unit.INSTANCE;
                });
            }
            else {
                String owner;
                String addDescriptor;
                String toArrayDescriptor;
                if (arrayOfReferences) {
                    owner = "kotlin/jvm/internal/SpreadBuilder";
                    addDescriptor = "(Ljava/lang/Object;)V";
                    toArrayDescriptor = "([Ljava/lang/Object;)[Ljava/lang/Object;";
                }
                else {
                    String spreadBuilderClassName = AsmUtil.asmPrimitiveTypeToLangPrimitiveType(elementType).getTypeName().getIdentifier() + "SpreadBuilder";
                    owner = "kotlin/jvm/internal/" + spreadBuilderClassName;
                    addDescriptor = "(" + elementType.getDescriptor() + ")V";
                    toArrayDescriptor = "()" + type.getDescriptor();
                }

                return StackValue.operation(type, adapter -> {
                    v.anew(Type.getObjectType(owner));
                    v.dup();
                    v.iconst(size);
                    v.invokespecial(owner, "<init>", "(I)V", false);
                    for (int i = 0; i != size; ++i) {
                        v.dup();
                        ValueArgument argument = arguments.get(i);
                        KtExpression argumentExpression = argument.getArgumentExpression();
                        KotlinType argumentKotlinType = kotlinType(argumentExpression);
                        if (argument.getSpreadElement() != null) {
                            gen(argumentExpression, OBJECT_TYPE, argumentKotlinType);

                            if (argumentKotlinType != null && InlineClassesUtilsKt.isInlineClassType(argumentKotlinType)) {
                                // we're going to pass konstue of inline class type to j/l/Object, which would result in boxing and then
                                // will cause check cast error on toArray() call. To mitigate this problem, we unbox this konstue and pass
                                // primitive array to the method. Note that bytecode optimisations will remove box/unbox calls.
                                StackValue.coerce(
                                        OBJECT_TYPE, argumentKotlinType,
                                        asmType(argumentKotlinType), argumentKotlinType,
                                        v
                                );
                            }

                            v.invokevirtual(owner, "addSpread", "(Ljava/lang/Object;)V", false);
                        }
                        else {
                            gen(argumentExpression, elementType, argumentKotlinType);
                            v.invokevirtual(owner, "add", addDescriptor, false);
                        }
                    }
                    if (arrayOfReferences) {
                        v.dup();
                        v.invokevirtual(owner, "size", "()I", false);
                        newArrayInstruction(outType);
                        v.invokevirtual(owner, "toArray", toArrayDescriptor, false);
                        v.checkcast(type);
                    }
                    else {
                        v.invokevirtual(owner, "toArray", toArrayDescriptor, false);
                    }
                    return Unit.INSTANCE;
                });
            }
        }
        else {
            return StackValue.operation(type, outType, adapter -> {
                v.iconst(arguments.size());
                newArrayInstruction(outType);
                KotlinType elementKotlinType = outType.getConstructor().getBuiltIns().getArrayElementType(outType);
                for (int i = 0; i != size; ++i) {
                    v.dup();
                    StackValue rightSide = gen(arguments.get(i).getArgumentExpression());
                    StackValue
                            .arrayElement(elementType, elementKotlinType, StackValue.onStack(type, outType), StackValue.constant(i))
                            .store(rightSide, v);
                }
                return Unit.INSTANCE;
            });
        }
    }

    public int indexOfLocalNotDelegated(KtReferenceExpression lhs) {
        DeclarationDescriptor declarationDescriptor = bindingContext.get(REFERENCE_TARGET, lhs);
        if (isBoxedLocalCapturedInClosure(bindingContext, declarationDescriptor)) {
            return -1;
        }
        if (declarationDescriptor instanceof LocalVariableDescriptor && ((LocalVariableDescriptor) declarationDescriptor).isDelegated()) {
            return -1;
        }
        return lookupLocalIndex(declarationDescriptor);
    }

    @Override
    public StackValue visitClassLiteralExpression(@NotNull KtClassLiteralExpression expression, StackValue data) {
        KtExpression receiverExpression = expression.getReceiverExpression();
        assert receiverExpression != null : "Class literal expression should have a left-hand side";
        DoubleColonLHS lhs = bindingContext.get(DOUBLE_COLON_LHS, receiverExpression);
        assert lhs != null : "Class literal expression should have LHS resolved";
        return generateClassLiteralReference(lhs, receiverExpression, /* wrapIntoKClass = */ true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public StackValue visitCallableReferenceExpression(@NotNull KtCallableReferenceExpression expression, StackValue data) {
        ResolvedCall<?> resolvedCall = CallUtilKt.getResolvedCallWithAssert(expression.getCallableReference(), bindingContext);

        StackValue receiver = generateCallableReferenceReceiver(resolvedCall);

        FunctionDescriptor functionDescriptor = bindingContext.get(BindingContext.FUNCTION, expression);
        if (functionDescriptor != null) {
            FunctionReferenceGenerationStrategy strategy = new FunctionReferenceGenerationStrategy(
                    state, functionDescriptor, resolvedCall,
                    receiver != null ? new JvmKotlinType(receiver.type, receiver.kotlinType) : null,
                    null, false
            );

            return genClosure(expression, functionDescriptor, strategy, null, (ResolvedCall<FunctionDescriptor>) resolvedCall, receiver);
        }

        return generatePropertyReference(
                expression, getVariableDescriptorNotNull(expression),
                (VariableDescriptor) resolvedCall.getResultingDescriptor(), receiver
        );
    }

    @Nullable
    public StackValue generateCallableReferenceReceiver(@NotNull ResolvedCall<?> resolvedCall) {
        ReceiverValue receiver = getBoundCallableReferenceReceiver(resolvedCall);
        if (receiver == null) return null;

        KotlinType receiverType = receiver.getType();
        return StackValue.coercion(generateReceiverValue(receiver, false), asmType(receiverType), receiverType);
    }

    @NotNull
    private StackValue generatePropertyReference(
            @NotNull KtElement element,
            @NotNull VariableDescriptor variableDescriptor,
            @NotNull VariableDescriptor target,
            @Nullable StackValue receiverValue
    ) {
        ClassDescriptor classDescriptor = bindingContext.get(CLASS_FOR_CALLABLE, variableDescriptor);
        if (classDescriptor == null) {
            throw new IllegalStateException(
                    "Property reference class was not found: " + variableDescriptor +
                    "\nTried to generate: " + element.getText());
        }

        ClassBuilder classBuilder = state.getFactory().newVisitor(
                JvmDeclarationOriginKt.OtherOrigin(element),
                typeMapper.mapClass(classDescriptor),
                element.getContainingFile()
        );

        PropertyReferenceCodegen codegen = new PropertyReferenceCodegen(
                state, parentCodegen, context.intoAnonymousClass(classDescriptor, this, OwnerKind.IMPLEMENTATION),
                element, classBuilder, variableDescriptor, target,
                receiverValue != null ? new JvmKotlinType(receiverValue.type, receiverValue.kotlinType) : null
        );
        codegen.generate();

        return codegen.putInstanceOnStack(receiverValue);
    }

    @NotNull
    public StackValue generateClassLiteralReference(
            @NotNull DoubleColonLHS lhs,
            @Nullable KtExpression receiverExpression,
            boolean wrapIntoKClass
    ) {
        return StackValue.operation(wrapIntoKClass ? K_CLASS_TYPE : JAVA_CLASS_TYPE, v -> {
            KotlinType type = lhs.getType();
            if (lhs instanceof DoubleColonLHS.Expression && !((DoubleColonLHS.Expression) lhs).isObjectQualifier()) {
                JavaClassProperty.INSTANCE.generateImpl(v, gen(receiverExpression));
            }
            else {
                if (TypeUtils.isTypeParameter(type)) {
                    assert TypeUtils.isReifiedTypeParameter(type) :
                            "Non-reified type parameter under ::class should be rejected by type checker: " + type;
                    putReifiedOperationMarkerIfTypeIsReifiedParameter(this, type, ReifiedTypeInliner.OperationKind.JAVA_CLASS);
                }

                putJavaLangClassInstance(v, typeMapper.mapType(type), type, typeMapper);
            }

            if (wrapIntoKClass) {
                wrapJavaClassIntoKClass(v);
            }

            return Unit.INSTANCE;
        });
    }

    @Override
    public StackValue visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression, StackValue receiver) {
        StackValue receiverValue = StackValue.none(); //gen(expression.getReceiverExpression())
        return genQualified(receiverValue, expression.getSelectorExpression());
    }

    private StackValue generateExpressionWithNullFallback(@NotNull KtExpression expression, @NotNull Label ifnull) {
        KtExpression deparenthesized = KtPsiUtil.deparenthesize(expression);
        assert deparenthesized != null : "Unexpected empty expression";

        expression = deparenthesized;
        Type type = expressionType(expression);
        KotlinType kotlinType = kotlinType(expression);

        if (expression instanceof KtSafeQualifiedExpression && !isPrimitive(type)) {
            return StackValue.coercion(generateSafeQualifiedExpression((KtSafeQualifiedExpression) expression, ifnull), type, kotlinType);
        }
        else {
            return genLazy(expression, type, kotlinType);
        }
    }

    private StackValue generateSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression, @NotNull Label ifNull) {
        KtExpression receiver = expression.getReceiverExpression();
        KtExpression selector = expression.getSelectorExpression();

        Type receiverType = expressionType(receiver);
        KotlinType receiverKotlinType = kotlinType(receiver);
        StackValue receiverValue = generateExpressionWithNullFallback(receiver, ifNull);

        //Do not optimize for primitives cause in case of safe call extension receiver should be generated before dispatch one
        StackValue newReceiver = new StackValue.SafeCall(
                receiverType, receiverKotlinType, receiverValue, isPrimitive(receiverType) ? null : ifNull
        );
        return genQualified(newReceiver, selector);
    }

    @Override
    public StackValue visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression, StackValue unused) {
        Label ifnull = new Label();
        Type type = boxType(expressionType(expression));
        KotlinType kotlinType = kotlinType(expression);

        StackValue konstue = generateSafeQualifiedExpression(expression, ifnull);
        StackValue newReceiver = StackValue.coercion(konstue, type, kotlinType);
        StackValue result;

        if (!isPrimitive(expressionType(expression.getReceiverExpression()))) {
            result = new StackValue.SafeFallback(type, kotlinType, ifnull, newReceiver);
        } else {
            result = newReceiver;
        }

        return result;
    }

    @Override
    public StackValue visitBinaryExpression(@NotNull KtBinaryExpression expression, @NotNull StackValue receiver) {
        KtSimpleNameExpression reference = expression.getOperationReference();
        IElementType opToken = reference.getReferencedNameElementType();
        if (opToken == KtTokens.EQ) {
            return generateAssignmentExpression(expression);
        }
        else if (KtTokens.AUGMENTED_ASSIGNMENTS.contains(opToken)) {
            return generateAugmentedAssignment(expression);
        }
        else if (opToken == KtTokens.ANDAND) {
            return generateBooleanAnd(expression);
        }
        else if (opToken == KtTokens.OROR) {
            return generateBooleanOr(expression);
        }
        else if (opToken == KtTokens.EQEQ || opToken == KtTokens.EXCLEQ ||
                 opToken == KtTokens.EQEQEQ || opToken == KtTokens.EXCLEQEQEQ) {
            return generateEquals(
                    expression.getLeft(), expression.getRight(), opToken, null, null,
                    bindingContext.get(BindingContext.PRIMITIVE_NUMERIC_COMPARISON_INFO, expression)
            );
        }
        else if (opToken == KtTokens.LT || opToken == KtTokens.LTEQ ||
                 opToken == KtTokens.GT || opToken == KtTokens.GTEQ) {
            return generateComparison(expression, receiver);
        }
        else if (opToken == KtTokens.ELVIS) {
            return generateElvis(expression);
        }
        else if (opToken == KtTokens.IN_KEYWORD || opToken == KtTokens.NOT_IN) {
            return generateIn(StackValue.expression(expressionType(expression.getLeft()), expression.getLeft(), this),
                              expression.getRight(), reference);
        }
        else {
            ConstantValue<?> compileTimeConstant = getPrimitiveOrStringCompileTimeConstant(expression);
            if (compileTimeConstant != null) {
                return StackValue.constant(compileTimeConstant.getValue(), expressionType(expression));
            }

            ResolvedCall<?> resolvedCall = CallUtilKt.getResolvedCallWithAssert(expression, bindingContext);
            FunctionDescriptor descriptor = (FunctionDescriptor) resolvedCall.getResultingDescriptor();

            if (descriptor instanceof ConstructorDescriptor) {
                return generateConstructorCall(resolvedCall, expressionType(expression));
            }

            return invokeFunction(resolvedCall, receiver);
        }
    }

    private StackValue generateIn(StackValue leftValue, KtExpression rangeExpression, KtSimpleNameExpression operationReference) {
        KtExpression deparenthesized = KtPsiUtil.deparenthesize(rangeExpression);
        assert deparenthesized != null : "For with empty range expression";
        RangeValue rangeValue = RangeValuesKt.createRangeValueForExpression(this, deparenthesized);
        return rangeValue.createInExpressionGenerator(this, operationReference).generate(leftValue);
    }

    private StackValue generateBooleanAnd(KtBinaryExpression expression) {
        return StackValue.and(gen(expression.getLeft()), gen(expression.getRight()));
    }

    private StackValue generateBooleanOr(KtBinaryExpression expression) {
        return StackValue.or(gen(expression.getLeft()), gen(expression.getRight()));
    }

    private StackValue genLazyUnlessProvided(@Nullable StackValue pregenerated, @NotNull KtExpression expr, @NotNull Type type) {
        return genLazyUnlessProvided(pregenerated, expr, type, null);
    }

    private StackValue genLazyUnlessProvided(
            @Nullable StackValue pregenerated,
            @NotNull KtExpression expr,
            @NotNull Type type,
            @Nullable KotlinType kotlinType
    ) {
        return pregenerated != null ? StackValue.coercion(pregenerated, type, kotlinType) : genLazy(expr, type, kotlinType);
    }

    private StackValue genUnlessProvided(@Nullable StackValue pregenerated, @NotNull KtExpression expr, @NotNull Type type) {
        if (pregenerated != null) {
            pregenerated.put(type, null, v);
        }
        else {
            gen(expr, type);
        }
        return StackValue.onStack(type);
    }

    private StackValue generateEquals(
            @Nullable KtExpression left,
            @Nullable KtExpression right,
            @NotNull IElementType opToken,
            @Nullable StackValue pregeneratedSubject,
            @Nullable KotlinType subjectKotlinType,
            @Nullable PrimitiveNumericComparisonInfo primitiveNumericComparisonInfo
    ) {
        if (left == null || right == null) {
            return StackValue.operation(Type.VOID_TYPE, v -> {
                v.aconst(null);
                v.athrow();
                return Unit.INSTANCE;
            });
        }

        KotlinType leftKotlinType = (subjectKotlinType != null) ? subjectKotlinType : kotlinType(left);
        KotlinType rightKotlinType = kotlinType(right);
        boolean leftIsInlineClass = leftKotlinType != null && InlineClassesUtilsKt.isInlineClassType(leftKotlinType);
        boolean rightIsInlineClass = rightKotlinType != null && InlineClassesUtilsKt.isInlineClassType(rightKotlinType);

        Type leftType = pregeneratedSubject != null ? pregeneratedSubject.type : expressionType(left);
        Type rightType = expressionType(right);

        boolean leftHasPrimitiveEquality =
                isPrimitive(leftType) && (!leftIsInlineClass || isInlineClassTypeWithPrimitiveEquality(leftKotlinType));
        boolean rightHasPrimitiveEquality =
                isPrimitive(rightType) && (!rightIsInlineClass || isInlineClassTypeWithPrimitiveEquality(rightKotlinType));

        if (KtPsiUtil.isNullConstant(left)) {
            return genCmpWithNull(right, opToken, null);
        }

        if (KtPsiUtil.isNullConstant(right)) {
            return genCmpWithNull(left, opToken, pregeneratedSubject);
        }

        if (leftIsInlineClass || rightIsInlineClass) {
            boolean leftIsUnboxed = leftIsInlineClass && StackValue.isUnboxedInlineClass(leftKotlinType, leftType);
            boolean rightIsUnboxed = rightIsInlineClass && StackValue.isUnboxedInlineClass(rightKotlinType, rightType);
            if (!leftIsUnboxed || !rightIsUnboxed || !leftHasPrimitiveEquality || !rightHasPrimitiveEquality) {
                return genEqualsForInlineClasses(
                        left, right, opToken, pregeneratedSubject, leftKotlinType, rightKotlinType,
                        leftType, rightType, leftIsInlineClass, leftIsUnboxed, rightIsUnboxed
                );
            }
        }

        if (isIntZero(left, leftType) && rightHasPrimitiveEquality && isIntPrimitive(rightType)) {
            return genCmpWithZero(right, opToken, null);
        }

        if (isIntZero(right, rightType) && leftHasPrimitiveEquality && isIntPrimitive(leftType)) {
            return genCmpWithZero(left, opToken, pregeneratedSubject);
        }

        if (pregeneratedSubject == null && left instanceof KtSafeQualifiedExpression &&
            isSelectorPureNonNullType((KtSafeQualifiedExpression) left) &&
            isPrimitive(rightType) && rightHasPrimitiveEquality) {
            return genCmpSafeCallToPrimitive((KtSafeQualifiedExpression) left, right, rightType, opToken);
        }

        if (isPrimitive(leftType) && leftHasPrimitiveEquality &&
            right instanceof KtSafeQualifiedExpression &&
            isSelectorPureNonNullType(((KtSafeQualifiedExpression) right))) {
            return genCmpPrimitiveToSafeCall(left, leftType, (KtSafeQualifiedExpression) right, opToken, pregeneratedSubject);
        }

        if (BoxedToPrimitiveEquality.isApplicable(opToken, leftType, rightType)) {
            return BoxedToPrimitiveEquality.create(
                    opToken,
                    genLazyUnlessProvided(pregeneratedSubject, left, leftType), leftType,
                    genLazy(right, rightType), rightType,
                    myFrameMap
            );
        }

        if (PrimitiveToBoxedEquality.isApplicable(opToken, leftType, rightType)) {
            return PrimitiveToBoxedEquality.create(
                    opToken,
                    genLazyUnlessProvided(pregeneratedSubject, left, leftType), leftType,
                    genLazy(right, rightType), rightType
            );
        }

        if (PrimitiveToObjectEquality.isApplicable(opToken, leftType, rightType)) {
            return PrimitiveToObjectEquality.create(
                    opToken,
                    genLazyUnlessProvided(pregeneratedSubject, left, leftType), leftType,
                    genLazy(right, rightType), rightType
            );
        }


        if (isPrimitive(leftType) != isPrimitive(rightType)) {
            leftType = boxType(leftType);
            rightType = boxType(rightType);
        }

        if (opToken == KtTokens.EQEQEQ || opToken == KtTokens.EXCLEQEQEQ) {
            // TODO: always casting to the type of the left operand in case of primitives looks wrong
            Type operandType = isPrimitive(leftType) ? leftType : OBJECT_TYPE;
            return StackValue.cmp(
                    opToken,
                    operandType,
                    genLazyUnlessProvided(pregeneratedSubject, left, leftType),
                    genLazy(right, rightType)
            );
        }

        if ((opToken == KtTokens.EQEQ || opToken == KtTokens.EXCLEQ) &&
            (isEnumExpression(left) || isEnumExpression(right))) {
            // Reference equality can be used for enums.
            return StackValue.cmp(
                    opToken == KtTokens.EQEQ ? KtTokens.EQEQEQ : KtTokens.EXCLEQEQEQ,
                    OBJECT_TYPE,
                    genLazyUnlessProvided(pregeneratedSubject, left, leftType),
                    genLazy(right, rightType)
            );
        }

        return genEqualsForExpressionsPreferIeee754Arithmetic(
                left, right, opToken, leftType, rightType, pregeneratedSubject, primitiveNumericComparisonInfo
        );
    }

    @NotNull
    private StackValue genEqualsForInlineClasses(
            @NotNull KtExpression left,
            @NotNull KtExpression right,
            @NotNull IElementType opToken,
            @Nullable StackValue pregeneratedSubject,
            @NotNull KotlinType leftKotlinType,
            @NotNull KotlinType rightKotlinType,
            @NotNull Type leftType,
            @NotNull Type rightType,
            boolean leftIsInlineClass,
            boolean leftIsUnboxed,
            boolean rightIsUnboxed
    ) {
        StackValue leftValue = genLazyUnlessProvided(pregeneratedSubject, left, leftType, leftKotlinType);
        StackValue rightValue = genLazy(right, rightType, rightKotlinType);

        return StackValue.operation(Type.BOOLEAN_TYPE, v -> {
            KotlinType nullableAnyType = state.getModule().getBuiltIns().getNullableAnyType();
            Label endLabel = new Label();
            boolean flipComparison = opToken == KtTokens.EXCLEQ || opToken == KtTokens.EXCLEQEQEQ;

            // Don't call equals-impl0 if the class file version is too low to support it.
            boolean useUnboxedEquals =
                    (leftIsUnboxed || (leftIsInlineClass && rightIsUnboxed)) &&
                    JvmCodegenUtil.typeHasSpecializedInlineClassEquality(leftKotlinType, state);

            leftValue.put(leftType, leftKotlinType, v);
            //noinspection SuspiciousNameCombination
            Type afterTopType = leftType;
            if (!useUnboxedEquals) {
                StackValue.coerce(leftType, leftKotlinType, OBJECT_TYPE, nullableAnyType, v);
                afterTopType = OBJECT_TYPE;
            }

            rightValue.put(rightType, rightKotlinType, v);
            //noinspection SuspiciousNameCombination
            Type topType = rightType;
            if (!useUnboxedEquals || !rightIsUnboxed) {
                StackValue.coerce(rightType, rightKotlinType, OBJECT_TYPE, nullableAnyType, v);
                topType = OBJECT_TYPE;
            }

            if (useUnboxedEquals) {
                String className = typeMapper.mapTypeAsDeclaration(leftKotlinType).getInternalName();
                // Nullable inline class wrappers around non-nullable types are unboxed, yet
                // equals-impl expects a non-nullable first argument and equals-impl0 expects
                // both arguments to be non-nullable.
                if (TypeUtils.isNullableType(leftKotlinType)) {
                    AsmUtil.dupSecond(v, topType, afterTopType);
                    Label nonNullLabel = new Label();
                    v.ifnonnull(nonNullLabel);

                    if (TypeUtils.isNullableType(rightKotlinType)) {
                        Label branchJumpLabel = new Label();
                        v.ifnonnull(branchJumpLabel);
                        AsmUtil.pop(v, afterTopType);
                        v.iconst(flipComparison ? 0 : 1);
                        v.goTo(endLabel);
                        v.visitLabel(branchJumpLabel);
                        AsmUtil.pop(v, afterTopType);
                    } else {
                        AsmUtil.pop2(v, topType, afterTopType);
                    }
                    v.iconst(flipComparison ? 1 : 0);
                    v.goTo(endLabel);

                    v.visitLabel(nonNullLabel);
                }

                if (rightIsUnboxed) {
                    // Call equals-impl0 if both arguments are unboxed
                    if (TypeUtils.isNullableType(rightKotlinType)) {
                        // left is already non-null, if right is null the comparison is false
                        AsmUtil.dup(v, topType);
                        Label nonNullLabel = new Label();
                        v.ifnonnull(nonNullLabel);
                        AsmUtil.pop2(v, topType, afterTopType);
                        v.iconst(flipComparison ? 1 : 0);
                        v.goTo(endLabel);
                        v.visitLabel(nonNullLabel);
                    }
                    if (!leftIsUnboxed) {
                        AsmUtil.swap(v, topType, afterTopType);
                        KotlinType nonNullableLeftKotlinType = TypeUtils.makeNotNullable(leftKotlinType);
                        Type nonNullableLeftType = typeMapper.mapType(nonNullableLeftKotlinType);
                        StackValue.coerce(leftType, leftKotlinType, nonNullableLeftType, nonNullableLeftKotlinType, v);
                        afterTopType = nonNullableLeftType;
                        AsmUtil.swap(v, afterTopType, topType);
                    }
                    String descriptor = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, afterTopType, afterTopType);
                    v.invokestatic(className, InlineClassDescriptorResolver.SPECIALIZED_EQUALS_NAME.asString(), descriptor, false);
                } else {
                    String descriptor = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, afterTopType, OBJECT_TYPE);
                    v.invokestatic(className, "equals-impl", descriptor, false);
                }
            } else {
                // Call Intrinsics.areEqual when both arguments are boxed
                genAreEqualCall(v);
            }

            if (flipComparison) {
                genInvertBoolean(v);
            }

            v.visitLabel(endLabel);
            return Unit.INSTANCE;
        });
    }

    private boolean isEnumExpression(@Nullable KtExpression expression) {
        KotlinType expressionType = bindingContext.getType(expression);
        if (expressionType == null) return false;
        return isEnumClass(expressionType.getConstructor().getDeclarationDescriptor());
    }


    private boolean isSelectorPureNonNullType(@NotNull KtSafeQualifiedExpression safeExpression) {
        KtExpression expression = safeExpression.getSelectorExpression();
        if (expression == null) return false;
        ResolvedCall<?> resolvedCall = CallUtilKt.getResolvedCall(expression, bindingContext);
        if (resolvedCall == null) return false;
        KotlinType returnType = resolvedCall.getResultingDescriptor().getReturnType();
        return returnType != null && !TypeUtils.isNullableType(returnType);
    }

    private StackValue genCmpPrimitiveToSafeCall(
            @NotNull KtExpression left,
            @NotNull Type leftType,
            @NotNull KtSafeQualifiedExpression right,
            @NotNull IElementType opToken,
            @Nullable StackValue pregeneratedLeft
    ) {
        Label rightIsNull = new Label();
        return new PrimitiveToSafeCallEquality(
                opToken,
                leftType,
                genLazyUnlessProvided(pregeneratedLeft, left, leftType),
                generateSafeQualifiedExpression(right, rightIsNull),
                expressionType(right.getReceiverExpression()),
                rightIsNull
        );
    }

    private StackValue genCmpSafeCallToPrimitive(
            @NotNull KtSafeQualifiedExpression left,
            @NotNull KtExpression right,
            @NotNull Type rightType,
            @NotNull IElementType opToken
    ) {
        Label leftIsNull = new Label();
        return new SafeCallToPrimitiveEquality(
                opToken,
                rightType,
                generateSafeQualifiedExpression(left, leftIsNull),
                genLazy(right, rightType),
                expressionType(left.getReceiverExpression()),
                leftIsNull
        );
    }

    @Nullable
    private static KotlinType getLeftOperandType(@Nullable PrimitiveNumericComparisonInfo numericComparisonInfo) {
        if (numericComparisonInfo == null) return null;
        return numericComparisonInfo.getLeftPrimitiveType();
    }

    @Nullable
    private static KotlinType getRightOperandType(@Nullable PrimitiveNumericComparisonInfo numericComparisonInfo) {
        if (numericComparisonInfo == null) return null;
        return numericComparisonInfo.getRightPrimitiveType();
    }

    /*tries to use IEEE 754 arithmetic*/
    private StackValue genEqualsForExpressionsPreferIeee754Arithmetic(
            @NotNull KtExpression left,
            @NotNull KtExpression right,
            @NotNull IElementType opToken,
            @NotNull Type leftType,
            @NotNull Type rightType,
            @Nullable StackValue pregeneratedLeft,
            @Nullable PrimitiveNumericComparisonInfo primitiveNumericComparisonInfo
    ) {
        assert (opToken == KtTokens.EQEQ || opToken == KtTokens.EXCLEQ) : "Optoken should be '==' or '!=', but: " + opToken;

        TypeAndNullability left754Type = calcTypeForIeee754ArithmeticIfNeeded(left, getLeftOperandType(primitiveNumericComparisonInfo));
        TypeAndNullability right754Type = calcTypeForIeee754ArithmeticIfNeeded(right, getRightOperandType(primitiveNumericComparisonInfo));

        if (left754Type != null && right754Type != null) {
            Type comparisonType = comparisonOperandType(left754Type.type, right754Type.type);
            if (comparisonType == Type.FLOAT_TYPE || comparisonType == Type.DOUBLE_TYPE) {
                if (left754Type.type.equals(right754Type.type)) {
                    //check nullability cause there is some optimizations in codegen for non-nullable case
                    if (left754Type.isNullable || right754Type.isNullable) {
                        if (state.getLanguageVersionSettings().getApiVersion().compareTo(ApiVersion.KOTLIN_1_1) >= 0) {
                            return StackValue.operation(Type.BOOLEAN_TYPE, v -> {
                                generate754EqualsForNullableTypesViaIntrinsic(v, opToken, pregeneratedLeft, left, left754Type, right,
                                                                              right754Type);
                                return Unit.INSTANCE;
                            });
                        }
                        else {
                            return StackValue.operation(Type.BOOLEAN_TYPE, v -> {
                                generate754EqualsForNullableTypes(v, opToken, pregeneratedLeft, left, left754Type, right, right754Type);
                                return Unit.INSTANCE;
                            });
                        }
                    }
                    else {
                        leftType = left754Type.type;
                        rightType = right754Type.type;
                    }
                }
                else if (shouldUseProperIeee754Comparisons()) {
                    return Ieee754Equality.create(
                            myFrameMap,
                            genLazy(left, boxIfNullable(left754Type)),
                            genLazy(right, boxIfNullable(right754Type)),
                            comparisonType,
                            opToken
                    );
                }
            }
        }

        return genEqualsForExpressionsOnStack(
                opToken,
                genLazyUnlessProvided(pregeneratedLeft, left, leftType, kotlinType(left)),
                genLazy(right, rightType, kotlinType(right))
        );
    }

    @NotNull
    private static Type boxIfNullable(@NotNull TypeAndNullability ieee754Type) {
        if (ieee754Type.isNullable) return AsmUtil.boxType(ieee754Type.type);
        return ieee754Type.type;
    }

    private void generate754EqualsForNullableTypesViaIntrinsic(
            @NotNull InstructionAdapter v,
            @NotNull IElementType opToken,
            @Nullable StackValue pregeneratedLeft,
            @Nullable KtExpression left,
            @NotNull TypeAndNullability left754Type,
            @Nullable KtExpression right,
            @NotNull TypeAndNullability right754Type
    ) {
        Type leftType = left754Type.isNullable ? AsmUtil.boxType(left754Type.type) : left754Type.type;

        genUnlessProvided(pregeneratedLeft, left, leftType);
        Type rightType = right754Type.isNullable ? AsmUtil.boxType(right754Type.type) : right754Type.type;
        gen(right, rightType);

        DescriptorAsmUtil.genIEEE754EqualForNullableTypesCall(v, leftType, rightType);

        if (opToken == KtTokens.EXCLEQ) {
            genInvertBoolean(v);
        }
    }

    private void generate754EqualsForNullableTypes(
            @NotNull InstructionAdapter v,
            @NotNull IElementType opToken,
            @Nullable StackValue pregeneratedLeft,
            @Nullable KtExpression left,
            @NotNull TypeAndNullability left754Type,
            @Nullable KtExpression right,
            @NotNull TypeAndNullability right754Type
    ) {
        int equals = opToken == KtTokens.EQEQ ? 1 : 0;
        int notEquals = opToken != KtTokens.EQEQ ? 1 : 0;
        Label end = new Label();
        StackValue leftValue = pregeneratedLeft != null ? pregeneratedLeft : gen(left);
        leftValue.put(leftValue.type, leftValue.kotlinType, v);
        leftValue = StackValue.onStack(leftValue.type);
        Type leftType = left754Type.type;
        Type rightType = right754Type.type;
        if (left754Type.isNullable) {
            leftValue.dup(v, false);
            Label leftIsNull = new Label();
            v.ifnull(leftIsNull);
            StackValue.coercion(leftValue, leftType, null).put(leftType, null, v);
            StackValue nonNullLeftValue = StackValue.onStack(leftType);

            StackValue rightValue = gen(right);
            rightValue.put(rightValue.type, rightValue.kotlinType, v);
            rightValue = StackValue.onStack(rightValue.type);
            if (right754Type.isNullable) {
                rightValue.dup(v, false);
                Label rightIsNotNull = new Label();
                v.ifnonnull(rightIsNotNull);
                AsmUtil.pop(v, rightValue.type);
                AsmUtil.pop(v, nonNullLeftValue.type);
                v.iconst(notEquals);
                v.goTo(end);
                v.mark(rightIsNotNull);
            }

            StackValue.coercion(rightValue, rightType, null).put(rightType, null, v);
            StackValue nonNullRightValue = StackValue.onStack(rightType);
            StackValue.cmp(opToken, leftType, nonNullLeftValue, nonNullRightValue).put(Type.BOOLEAN_TYPE, null, v);
            v.goTo(end);

            //left is null case
            v.mark(leftIsNull);
            AsmUtil.pop(v, leftValue.type);//pop null left
            rightValue = gen(right);
            rightValue.put(rightValue.type, rightValue.kotlinType, v);
            rightValue = StackValue.onStack(rightValue.type);
            if (right754Type.isNullable) {
                Label rightIsNotNull = new Label();
                v.ifnonnull(rightIsNotNull);
                v.iconst(equals);
                v.goTo(end);
                v.mark(rightIsNotNull);
                v.iconst(notEquals);
                //v.goTo(end);
            }
            else {
                AsmUtil.pop(v, rightValue.type);
                v.iconst(notEquals);
                //v.goTo(end);
            }

            v.mark(end);
            return;
        }
        else {
            StackValue.coercion(leftValue, leftType, null).put(leftType, null, v);
            leftValue = StackValue.onStack(leftType);
        }

        //right is nullable cause left is not
        StackValue rightValue = gen(right);
        rightValue.put(rightValue.type, rightValue.kotlinType, v);
        rightValue = StackValue.onStack(rightValue.type);

        rightValue.dup(v, false);
        Label rightIsNotNull = new Label();
        v.ifnonnull(rightIsNotNull);
        AsmUtil.pop(v, rightValue.type);
        AsmUtil.pop(v, leftValue.type);
        v.iconst(notEquals);
        v.goTo(end);

        v.mark(rightIsNotNull);
        StackValue.coercion(rightValue, rightType, null).put(rightType, null, v);
        StackValue nonNullRightValue = StackValue.onStack(rightType);
        StackValue.cmp(opToken, leftType, leftValue, nonNullRightValue).put(Type.BOOLEAN_TYPE, null, v);

        v.mark(end);
    }

    private boolean isIntZero(KtExpression expr, Type exprType) {
        ConstantValue<?> exprValue = getPrimitiveOrStringCompileTimeConstant(expr);
        return isIntPrimitive(exprType) && exprValue != null && Integer.konstueOf(0).equals(exprValue.getValue());
    }

    private StackValue genCmpWithZero(KtExpression exp, IElementType opToken, @Nullable StackValue pregeneratedExpr) {
        StackValue argument;
        if (pregeneratedExpr == null) {
            KotlinType kotlinType = kotlinType(exp);
            assert kotlinType != null : "No KotlinType for expression " + exp.getText();
            argument = genLazy(exp, asmType(kotlinType), kotlinType);
        }
        else {
            argument = pregeneratedExpr;
        }
        return StackValue.compareIntWithZero(
                argument,
                (KtTokens.EQEQ == opToken || KtTokens.EQEQEQ == opToken) ? IFNE : IFEQ
        );
    }

    private StackValue genCmpWithNull(KtExpression exp, IElementType opToken, @Nullable StackValue pregeneratedExpr) {
        KotlinType kotlinType = kotlinType(exp);
        Type type = expressionType(exp);
        StackValue argument = pregeneratedExpr != null ? pregeneratedExpr : gen(exp);

        if (kotlinType == null || TypeUtils.isNullableType(kotlinType) || !InlineClassesUtilsKt.isInlineClassType(kotlinType)) {
            return StackValue.compareWithNull(argument, (KtTokens.EQEQ == opToken || KtTokens.EQEQEQ == opToken) ? IFNONNULL : IFNULL);
        } else {
            // If exp is an unboxed inline class konstue the comparison is vacuous
            return StackValue.operation(Type.BOOLEAN_TYPE, v -> {
                argument.put(type, kotlinType, v);
                AsmUtil.pop(v, type);
                v.iconst((opToken == KtTokens.EXCLEQ || opToken == KtTokens.EXCLEQEQEQ) ? 1 : 0);
                return Unit.INSTANCE;
            });
        }
    }

    private StackValue generateElvis(@NotNull KtBinaryExpression expression) {
        KtExpression left = expression.getLeft();

        Type exprType = expressionType(expression);
        KotlinType exprKotlinType = kotlinType(expression);

        Type leftType = expressionType(left);
        KotlinType leftKotlinType = kotlinType(left);

        Label ifNull = new Label();

        assert left != null : "left expression in elvis should be not null: " + expression.getText();
        StackValue konstue = generateExpressionWithNullFallback(left, ifNull);

        if (isPrimitive(leftType)) {
            return konstue;
        }

        return StackValue.operation(exprType, exprKotlinType, v -> {
            konstue.put(konstue.type, konstue.kotlinType, v);
            v.dup();

            v.ifnull(ifNull);
            StackValue.onStack(leftType, leftKotlinType).put(exprType, exprKotlinType, v);

            Label end = new Label();
            v.goTo(end);

            v.mark(ifNull);
            Integer leftLineNumber = CodegenUtil.getLineNumberForElement(left, false);
            Integer rightLineNumber = CodegenUtil.getLineNumberForElement(expression.getRight(), false);
            if (rightLineNumber != null && rightLineNumber.equals(leftLineNumber)) {
                v.visitLineNumber(rightLineNumber, ifNull);
            }
            v.pop();
            gen(expression.getRight(), exprType, exprKotlinType);
            v.mark(end);
            return null;
        });
    }

    private StackValue generateComparison(KtBinaryExpression expression, StackValue receiver) {
        ResolvedCall<?> resolvedCall = CallUtilKt.getResolvedCallWithAssert(expression, bindingContext);
        PrimitiveNumericComparisonInfo primitiveNumericComparisonInfo =
                bindingContext.get(BindingContext.PRIMITIVE_NUMERIC_COMPARISON_INFO, expression);

        KtExpression left = expression.getLeft();
        KtExpression right = expression.getRight();

        Type type;
        StackValue leftValue;
        StackValue rightValue;
        Type leftType = expressionType(left);
        Type rightType = expressionType(right);
        TypeAndNullability left754Type = calcTypeForIeee754ArithmeticIfNeeded(left, getLeftOperandType(primitiveNumericComparisonInfo));
        TypeAndNullability right754Type = calcTypeForIeee754ArithmeticIfNeeded(right, getRightOperandType(primitiveNumericComparisonInfo));
        boolean isSame754ArithmeticTypes = left754Type != null && right754Type != null && left754Type.type.equals(right754Type.type);
        boolean properIeee754Comparisons = shouldUseProperIeee754Comparisons();
        boolean isStandardCompareTo = primitiveNumericComparisonInfo != null;

        if (properIeee754Comparisons && isStandardCompareTo && left754Type != null && right754Type != null) {
            type = comparisonOperandType(left754Type.type, right754Type.type);
            leftValue = gen(left);
            rightValue = gen(right);
        }
        else if (!properIeee754Comparisons && isStandardCompareTo &&
                 ((isPrimitive(leftType) && isPrimitive(rightType)) || isSame754ArithmeticTypes)) {
            type = isSame754ArithmeticTypes ? left754Type.type : comparisonOperandType(leftType, rightType);
            leftValue = gen(left);
            rightValue = gen(right);
        }
        else {
            type = Type.INT_TYPE;
            leftValue = invokeFunction(resolvedCall, receiver);
            rightValue = StackValue.constant(0, type);
        }
        return StackValue.cmp(expression.getOperationToken(), type, leftValue, rightValue);
    }

    @Nullable
    private TypeAndNullability calcTypeForIeee754ArithmeticIfNeeded(
            @Nullable KtExpression expression,
            @Nullable KotlinType inferredPrimitiveType
    ) {
        if (expression == null) {
            return null;
        }
        else if (shouldUseProperIeee754Comparisons()) {
            return Ieee754Kt.calcProperTypeForIeee754ArithmeticIfNeeded(expression, bindingContext, inferredPrimitiveType, typeMapper);
        }
        else {
            return Ieee754Kt.legacyCalcTypeForIeee754ArithmeticIfNeeded(
                    expression, bindingContext, context.getFunctionDescriptor(), state.getLanguageVersionSettings());
        }
    }

    private boolean shouldUseProperIeee754Comparisons() {
        return state.getLanguageVersionSettings().supportsFeature(LanguageFeature.ProperIeee754Comparisons);
    }

    private StackValue generateAssignmentExpression(KtBinaryExpression expression) {
        return StackValue.operation(Type.VOID_TYPE, adapter -> {
            StackValue stackValue = gen(expression.getLeft());
            KtExpression right = expression.getRight();
            assert right != null : expression.getText();
            stackValue.store(gen(right), v);

            return Unit.INSTANCE;
        });
    }

    private StackValue generateAugmentedAssignment(KtBinaryExpression expression) {
        return StackValue.operation(Type.VOID_TYPE, adapter -> {
            ResolvedCall<?> resolvedCall = CallUtilKt.getResolvedCallWithAssert(expression, bindingContext);

            ResolvedCallWithRealDescriptor callWithRealDescriptor =
                    CoroutineCodegenUtilKt.replaceSuspensionFunctionWithRealDescriptor(resolvedCall, state);
            if (callWithRealDescriptor != null) {
                prepareCoroutineArgumentForSuspendCall(resolvedCall, callWithRealDescriptor.getFakeContinuationExpression());
                resolvedCall = callWithRealDescriptor.getResolvedCall();
            }

            FunctionDescriptor descriptor = accessibleFunctionDescriptor(resolvedCall);
            Callable callable = resolveToCallable(descriptor, false, resolvedCall);
            KtExpression lhs = expression.getLeft();
            JvmKotlinType jvmKotlinType = new JvmKotlinType(expressionType(lhs), kotlinType(lhs));

            boolean keepReturnValue = Boolean.TRUE.equals(bindingContext.get(VARIABLE_REASSIGNMENT, expression))
                                      || !KotlinBuiltIns.isUnit(descriptor.getReturnType());

            putCallAugAssignMethod(expression, resolvedCall, callable, descriptor.getReturnType(), jvmKotlinType, keepReturnValue);

            return Unit.INSTANCE;
        });
    }

    private void putCallAugAssignMethod(
            @NotNull KtBinaryExpression expression,
            @NotNull ResolvedCall<?> resolvedCall,
            @NotNull Callable callable,
            @Nullable KotlinType returnKotlinType,
            @NotNull JvmKotlinType jvmKotlinType,
            boolean keepReturnValue
    ) {
        StackValue konstue = gen(expression.getLeft());
        if (keepReturnValue) {
            konstue = StackValue.complexWriteReadReceiver(konstue);
        }
        konstue.put(jvmKotlinType.getType(), jvmKotlinType.getKotlinType(), v);
        StackValue receiver = StackValue.onStack(jvmKotlinType.getType(), jvmKotlinType.getKotlinType());

        callable.invokeMethodWithArguments(resolvedCall, receiver, this).put(callable.getReturnType(), null, v);

        if (keepReturnValue) {
            konstue.store(StackValue.onStack(callable.getReturnType(), returnKotlinType), v, true);
        }
    }

    public void invokeAppend(StringConcatGenerator generator, KtExpression expr) {
        expr = KtPsiUtil.safeDeparenthesize(expr);

        ConstantValue<?> compileTimeConstant = getPrimitiveOrStringCompileTimeConstant(expr);

        if (compileTimeConstant == null) {
            if (expr instanceof KtBinaryExpression) {
                KtBinaryExpression binaryExpression = (KtBinaryExpression) expr;
                if (binaryExpression.getOperationToken() == KtTokens.PLUS) {
                    KtExpression left = binaryExpression.getLeft();
                    KtExpression right = binaryExpression.getRight();
                    Type leftType = expressionType(left);

                    if (leftType.equals(JAVA_STRING_TYPE)) {
                        invokeAppend(generator, left);
                        invokeAppend(generator, right);
                        return;
                    }
                }
            }
            else if (expr instanceof KtStringTemplateExpression) {
                List<StringTemplateEntry> entries = preprocessStringTemplate((KtStringTemplateExpression) expr);
                invokeAppendForEntries(generator, entries);
                return;
            }
        }

        Type exprType = expressionType(expr);
        KotlinType exprKotlinType = kotlinType(expr);
        if (exprKotlinType != null && InlineClassesUtilsKt.isInlineClassType(exprKotlinType) &&
            FlexibleTypesKt.isNullabilityFlexible(exprKotlinType)) {
            exprKotlinType = TypeUtils.makeNullable(exprKotlinType);
        }
        StackValue konstue;
        if (compileTimeConstant != null) {
            konstue = StackValue.constant(compileTimeConstant.getValue(), exprType, exprKotlinType);
        } else {
            konstue = gen(expr);
        }

        genInvokeAppendMethod(generator, exprType, exprKotlinType, typeMapper, konstue);
    }

    @Nullable
    private static KtSimpleNameExpression targetLabel(KtExpression expression) {
        if (expression.getParent() instanceof KtLabeledExpression) {
            return ((KtLabeledExpression) expression.getParent()).getTargetLabel();
        }
        return null;
    }

    @Override
    public StackValue visitLabeledExpression(
            @NotNull KtLabeledExpression expression, StackValue receiver
    ) {
        return genQualified(receiver, expression.getBaseExpression());
    }

    @Override
    public StackValue visitPrefixExpression(@NotNull KtPrefixExpression expression, @NotNull StackValue receiver) {
        ConstantValue<?> compileTimeConstant = getPrimitiveOrStringCompileTimeConstant(expression);
        if (compileTimeConstant != null) {
            return StackValue.constant(compileTimeConstant.getValue(), expressionType(expression));
        }

        DeclarationDescriptor originalOperation = bindingContext.get(REFERENCE_TARGET, expression.getOperationReference());
        ResolvedCall<?> resolvedCall = CallUtilKt.getResolvedCallWithAssert(expression, bindingContext);
        CallableDescriptor op = resolvedCall.getResultingDescriptor();

        assert op instanceof FunctionDescriptor || originalOperation == null : String.konstueOf(op);
        String operationName = originalOperation == null ? "" : originalOperation.getName().asString();
        if (!(operationName.equals("inc") || operationName.equals("dec"))) {
            return invokeFunction(resolvedCall, receiver);
        }

        int increment = operationName.equals("inc") ? 1 : -1;
        Type type = expressionType(expression.getBaseExpression());
        StackValue konstue = gen(expression.getBaseExpression());
        return StackValue.preIncrement(type, konstue, increment, resolvedCall, this);
    }

    @Override
    public StackValue visitPostfixExpression(@NotNull KtPostfixExpression expression, StackValue receiver) {
        if (expression.getOperationReference().getReferencedNameElementType() == KtTokens.EXCLEXCL) {
            StackValue base = genQualified(receiver, expression.getBaseExpression());
            if (isPrimitive(base.type)) return base;

            return StackValue.operation(base.type, base.kotlinType, v -> {
                base.put(base.type, base.kotlinType, v);
                v.dup();
                if (state.getUnifiedNullChecks()) {
                    v.invokestatic(IntrinsicMethods.INTRINSICS_CLASS_NAME, "checkNotNull", "(Ljava/lang/Object;)V", false);
                } else {
                    Label ok = new Label();
                    v.ifnonnull(ok);
                    v.invokestatic(IntrinsicMethods.INTRINSICS_CLASS_NAME, "throwNpe", "()V", false);
                    v.mark(ok);
                }
                return null;
            });
        }

        DeclarationDescriptor originalOperation = bindingContext.get(REFERENCE_TARGET, expression.getOperationReference());
        String originalOperationName = originalOperation != null ? originalOperation.getName().asString() : null;
        ResolvedCall<?> resolvedCall = CallUtilKt.getResolvedCallWithAssert(expression, bindingContext);
        DeclarationDescriptor op = resolvedCall.getResultingDescriptor();
        if (!(op instanceof FunctionDescriptor) || originalOperation == null) {
            throw new UnsupportedOperationException("Don't know how to generate this postfix expression: " + originalOperationName + " " + op);
        }


        Type asmResultType = expressionType(expression);
        Type asmBaseType = expressionType(expression.getBaseExpression());
        KotlinType kotlinBaseType = kotlinType(expression.getBaseExpression());

        DeclarationDescriptor cls = op.getContainingDeclaration();

        int increment;
        if (originalOperationName.equals("inc")) {
            increment = 1;
        }
        else if (originalOperationName.equals("dec")) {
            increment = -1;
        }
        else {
            throw new UnsupportedOperationException("Unsupported postfix operation: " + originalOperationName + " " + op);
        }

        boolean isPrimitiveNumberClassDescriptor = isPrimitiveNumberClassDescriptor(cls);
        if (isPrimitiveNumberClassDescriptor && AsmUtil.isPrimitive(asmBaseType)) {
            KtExpression operand = expression.getBaseExpression();
            // Optimization for j = i++, when j and i are Int without any smart cast: we just work with primitive int
            if (operand instanceof KtReferenceExpression && asmResultType == Type.INT_TYPE &&
                bindingContext.get(BindingContext.SMARTCAST, operand) == null) {
                int index = indexOfLocalNotDelegated((KtReferenceExpression) operand);
                if (index >= 0) {
                    return StackValue.postIncrement(index, increment);
                }
            }
        }

        return StackValue.operation(asmBaseType, kotlinBaseType, v -> {
            StackValue konstue = StackValue.complexWriteReadReceiver(gen(expression.getBaseExpression()));

            konstue.put(asmBaseType, kotlinBaseType, v);
            AsmUtil.dup(v, asmBaseType);

            StackValue previousValue = StackValue.local(myFrameMap.enterTemp(asmBaseType), asmBaseType, kotlinBaseType);
            previousValue.store(StackValue.onStack(asmBaseType, kotlinBaseType), v);

            Type storeType;
            KotlinType storeKotlinType;
            if (isPrimitiveNumberClassDescriptor && AsmUtil.isPrimitive(asmBaseType)) {
                genIncrement(asmBaseType, increment, v);
                storeType = asmBaseType;
                storeKotlinType = kotlinBaseType;
            }
            else {
                StackValue result = invokeFunction(resolvedCall, StackValue.onStack(asmBaseType, kotlinBaseType));
                result.put(result.type, result.kotlinType, v);
                storeType = result.type;
                storeKotlinType = result.kotlinType;
            }

            konstue.store(StackValue.onStack(storeType, storeKotlinType), v, true);

            previousValue.put(asmBaseType, kotlinBaseType, v);

            myFrameMap.leaveTemp(asmBaseType);

            return Unit.INSTANCE;
        });
    }

    @Override
    public StackValue visitProperty(@NotNull KtProperty property, StackValue receiver) {
        KtExpression initializer = property.getInitializer();
        KtExpression delegateExpression = property.getDelegateExpression();

        if (initializer != null) {
            assert delegateExpression == null : PsiUtilsKt.getElementTextWithContext(property);
            initializeLocalVariable(property, gen(initializer));
        }
        else if (delegateExpression != null) {
            initializeLocalVariable(property, gen(delegateExpression));
        }
        else if (property.hasModifier(KtTokens.LATEINIT_KEYWORD)) {
            initializeLocalVariable(property, null);
        }
        else {
            initializeLocalVariableWithFakeDefaultValue(property);
        }

        return StackValue.none();
    }
    @Override
    public StackValue visitDestructuringDeclaration(@NotNull KtDestructuringDeclaration multiDeclaration, StackValue receiver) {
        return initializeDestructuringDeclaration(multiDeclaration, false);
    }

    public StackValue initializeDestructuringDeclaration(
            @NotNull KtDestructuringDeclaration multiDeclaration,
            boolean asProperty
    ) {
        KtExpression initializer = multiDeclaration.getInitializer();
        if (initializer == null) return StackValue.none();

        KotlinType initializerType = bindingContext.getType(initializer);
        assert initializerType != null;

        Type initializerAsmType = asmType(initializerType);

        TransientReceiver initializerAsReceiver = new TransientReceiver(initializerType);

        int tempVarIndex = myFrameMap.enterTemp(initializerAsmType);

        gen(initializer, initializerAsmType, initializerType);
        v.store(tempVarIndex, initializerAsmType);
        StackValue.Local local = StackValue.local(tempVarIndex, initializerAsmType, initializerType);

        initializeDestructuringDeclarationVariables(multiDeclaration, initializerAsReceiver, local, asProperty);

        myFrameMap.leaveTemp(initializerAsmType);

        return StackValue.none();
    }

    public void initializeDestructuringDeclarationVariables(
            @NotNull KtDestructuringDeclaration destructuringDeclaration,
            @NotNull ReceiverValue receiver,
            @NotNull StackValue receiverStackValue
    ) {
        initializeDestructuringDeclarationVariables(destructuringDeclaration, receiver, receiverStackValue, false);
    }

    private void initializeDestructuringDeclarationVariables(
            @NotNull KtDestructuringDeclaration destructuringDeclaration,
            @NotNull ReceiverValue receiver,
            @NotNull StackValue receiverStackValue,
            boolean asProperty
    ) {
        for (KtDestructuringDeclarationEntry variableDeclaration : destructuringDeclaration.getEntries()) {
            ResolvedCall<FunctionDescriptor> resolvedCall = bindingContext.get(COMPONENT_RESOLVED_CALL, variableDeclaration);
            assert resolvedCall != null : "Resolved call is null for " + variableDeclaration.getText();
            Call call = makeFakeCall(receiver);

            VariableDescriptor variableDescriptor = getVariableDescriptorNotNull(variableDeclaration);

            // Do not call `componentX` for destructuring entry called _
            if (UnderscoreUtilKt.isSingleUnderscore(variableDeclaration)) continue;

            if (asProperty && variableDescriptor instanceof PropertyDescriptor) {
                StackValue.Property propertyValue = intermediateValueForProperty(
                        (PropertyDescriptor) variableDescriptor, true, false, null, true, StackValue.LOCAL_0, null, false
                );

                propertyValue.store(invokeFunction(call, resolvedCall, receiverStackValue), v);
            }
            else {
                initializeLocalVariable(variableDeclaration, invokeFunction(call, resolvedCall, receiverStackValue));
            }
        }
    }

    @NotNull
    private StackValue getVariableMetadataValue(@NotNull VariableDescriptorWithAccessors variableDescriptor) {
        StackValue konstue = findLocalOrCapturedValue(getDelegatedLocalVariableMetadata(variableDescriptor, bindingContext));
        assert konstue != null : "Can't find stack konstue for local delegated variable metadata: " + variableDescriptor;
        return konstue;
    }

    @NotNull
    private StackValue adjustVariableValue(@NotNull StackValue varValue, DeclarationDescriptor descriptor) {
        if (!isDelegatedLocalVariable(descriptor)) return varValue;

        VariableDescriptorWithAccessors variableDescriptor = (VariableDescriptorWithAccessors) descriptor;
        StackValue metadataValue = getVariableMetadataValue(variableDescriptor);
        return delegatedVariableValue(varValue, metadataValue, variableDescriptor, typeMapper);
    }

    private void initializeLocalVariable(
            @NotNull KtVariableDeclaration variableDeclaration,
            @Nullable StackValue initializer
    ) {
        LocalVariableDescriptor variableDescriptor = (LocalVariableDescriptor) getVariableDescriptorNotNull(variableDeclaration);

        if (KtPsiUtil.isScriptDeclaration(variableDeclaration)) {
            return;
        }
        int index = lookupLocalIndex(variableDescriptor);

        if (index < 0) {
            throw new IllegalStateException("Local variable not found for " + variableDescriptor);
        }

        Type sharedVarType = typeMapper.getSharedVarType(variableDescriptor);

        Type varType = getVariableTypeNoSharing(variableDescriptor);

        KotlinType delegateKotlinType = JvmCodegenUtil.getPropertyDelegateType(variableDescriptor, bindingContext);
        StackValue storeTo = sharedVarType == null ?
                             StackValue.local(index, varType, variableDescriptor, delegateKotlinType) :
                             StackValue.shared(index, varType, variableDescriptor, delegateKotlinType);

        storeTo.putReceiver(v, false);
        if (variableDescriptor.isLateInit()) {
            assert initializer == null : "Initializer should be null for lateinit var " + variableDescriptor + ": " + initializer;
            v.aconst(null);
            storeTo.storeSelector(storeTo.type, storeTo.kotlinType, v);
            return;
        }

        assert initializer != null : "Initializer should be not null for " + variableDescriptor;
        initializer.put(initializer.type, initializer.kotlinType, v);

        markLineNumber(variableDeclaration, false);
        Type resultType = initializer.type;
        KotlinType resultKotlinType = initializer.kotlinType;

        if (isDelegatedLocalVariable(variableDescriptor)) {
            StackValue metadataValue = getVariableMetadataValue(variableDescriptor);
            initializePropertyMetadata((KtProperty) variableDeclaration, variableDescriptor, metadataValue);

            ResolvedCall<FunctionDescriptor> provideDelegateCall = bindingContext.get(PROVIDE_DELEGATE_RESOLVED_CALL, variableDescriptor);
            if (provideDelegateCall != null) {
                StackValue provideDelegateValue = generateProvideDelegateCallForLocalVariable(initializer, metadataValue, provideDelegateCall);
                resultType = provideDelegateValue.type;
                resultKotlinType = provideDelegateValue.kotlinType;
            }
        }

        storeTo.storeSelector(resultType, resultKotlinType, v);
    }

    // This is a workaround to avoid bugs with incorrect range of uninitialized variable:
    // 1) JDI error 305
    // 2) Expected R, got . in code with contracts
    // 3) D8 dropping the whole LVT
    private void initializeLocalVariableWithFakeDefaultValue(@NotNull KtProperty variableDeclaration) {
        LocalVariableDescriptor variableDescriptor = (LocalVariableDescriptor) getVariableDescriptorNotNull(variableDeclaration);

        // Boxed variables already have a konstue
        if (isBoxedLocalCapturedInClosure(bindingContext, variableDescriptor)) return;

        assert !variableDeclaration.hasDelegateExpressionOrInitializer() &&
                !variableDescriptor.isLateInit() : variableDeclaration.getText() + " in not variable declaration without initializer";

        KotlinType kotlinType = variableDescriptor.getType();
        Type type = typeMapper.mapType(kotlinType);

        if (type == Type.VOID_TYPE) return;

        int index = lookupLocalIndex(variableDescriptor);
        assert index >= 0: variableDescriptor + " is not in frame map";

        switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                v.iconst(0);
                break;
            case Type.FLOAT:
                v.fconst(0.0f);
                break;
            case Type.LONG:
                v.lconst(0L);
                break;
            case Type.DOUBLE:
                v.dconst(0.0);
                break;
            default:
                v.aconst(null);
        }
        v.store(index, type);
    }

    @NotNull
    private StackValue generateProvideDelegateCallForLocalVariable(
            @NotNull StackValue initializer,
            StackValue metadataValue,
            ResolvedCall<FunctionDescriptor> provideDelegateResolvedCall
    ) {
        StackValue provideDelegateReceiver = StackValue.onStack(initializer.type, initializer.kotlinType);

        List<? extends ValueArgument> arguments = provideDelegateResolvedCall.getCall().getValueArguments();
        assert arguments.size() == 2 :
                "Resolved call for '" +
                OperatorNameConventions.PROVIDE_DELEGATE.asString() +
                "' should have exactly 2 konstue parameters";

        tempVariables.put(arguments.get(0).asElement(), StackValue.constant(null, AsmTypes.OBJECT_TYPE));
        tempVariables.put(
                arguments.get(1).asElement(),
                new StackValue(K_PROPERTY_TYPE) {
                    @Override
                    public void putSelector(@NotNull Type type, @Nullable KotlinType kotlinType, @NotNull InstructionAdapter v) {
                        metadataValue.put(type, kotlinType, v);
                    }
                }
        );

        StackValue result = invokeFunction(provideDelegateResolvedCall, provideDelegateReceiver);
        result.put(result.type, result.kotlinType, v);
        tempVariables.remove(arguments.get(0).asElement());
        tempVariables.remove(arguments.get(1).asElement());
        return result;
    }

    @NotNull
    public VariableDescriptor getVariableDescriptorNotNull(@NotNull KtElement declaration) {
        VariableDescriptor descriptor = bindingContext.get(VARIABLE, declaration);
        assert descriptor != null :  "Couldn't find variable declaration in binding context " + declaration.getText();
        return descriptor;
    }

    private void initializePropertyMetadata(
            @NotNull KtProperty variable,
            @NotNull LocalVariableDescriptor variableDescriptor,
            @NotNull StackValue metadataVar
    ) {
        // TODO: do not generate anonymous classes for local delegated properties in inline functions
        // We can use the $$delegatedProperties array as in non-inline functions and upon inlining, detect elements at what indices
        // of that array are used in the inline function body, load the corresponding initializing bytecode from <clinit> of the
        // container class (where the PropertyReferenceNImpl instance is created), copy and adapt it at the call site
        StackValue konstue;
        if (PropertyCodegen.isDelegatedPropertyWithOptimizedMetadata(variableDescriptor, bindingContext)) {
            konstue = PropertyCodegen.getOptimizedDelegatedPropertyMetadataValue();
        } else if (context.getFunctionDescriptor().isInline()) {
            //noinspection ConstantConditions
            konstue = generatePropertyReference(variable.getDelegate(), variableDescriptor, variableDescriptor, null);
        }
        else {
            konstue = PropertyCodegen.getDelegatedPropertyMetadata(variableDescriptor, bindingContext);
        }
        konstue.put(K_PROPERTY_TYPE, null, v);
        metadataVar.storeSelector(K_PROPERTY_TYPE, null, v);
    }

    @NotNull
    private StackValue generateNewCall(@NotNull KtCallExpression expression, @NotNull ResolvedCall<?> resolvedCall) {
        Type type = expressionType(expression);
        if (type.getSort() == Type.ARRAY) {
            KotlinType kotlinType = kotlinType(expression);
            assert kotlinType != null : "No kotlinType for expression of type " + type + ": " + expression.getText();
            if (KotlinBuiltIns.isArrayOrPrimitiveArray(kotlinType)) {
                //noinspection ConstantConditions
                return generateNewArray(expression, bindingContext.getType(expression), resolvedCall);
            }
        }

        return generateConstructorCall(resolvedCall, type);
    }

    @NotNull
    public ClassConstructorDescriptor getConstructorDescriptor(@NotNull ResolvedCall<?> resolvedCall) {
        FunctionDescriptor accessibleDescriptor = accessibleFunctionDescriptor(resolvedCall);
        assert accessibleDescriptor instanceof ClassConstructorDescriptor :
                "getConstructorDescriptor must be called only for constructors: " + accessibleDescriptor;
        return (ClassConstructorDescriptor) accessibleDescriptor;
    }

    @Nullable
    private static ReceiverValue getConstructorReceiver(@NotNull ResolvedCall<?> resolvedCall) {
        CallableDescriptor constructor = resolvedCall.getResultingDescriptor();
        if (constructor.getExtensionReceiverParameter() != null) {
            // see comment on `withDispatchReceiver` parameter in
            // org.jetbrains.kotlin.descriptors.impl.TypeAliasConstructorDescriptorImpl.Companion.createIfAvailable
            assert constructor instanceof TypeAliasConstructorDescriptor :
                    "Only type alias constructor can have an extension receiver: " + constructor;
            return resolvedCall.getExtensionReceiver();
        }
        else if (constructor.getDispatchReceiverParameter() != null) {
            return resolvedCall.getDispatchReceiver();
        }
        else {
            return null;
        }
    }

    @NotNull
    public StackValue generateConstructorCall(@NotNull ResolvedCall<?> resolvedCall, @NotNull Type objectType) {
        return StackValue.functionCall(objectType, resolvedCall.getResultingDescriptor().getReturnType(), v -> {
            ClassConstructorDescriptor constructor = getConstructorDescriptor(resolvedCall);
            ReceiverParameterDescriptor dispatchReceiver = constructor.getDispatchReceiverParameter();
            ClassDescriptor containingDeclaration = constructor.getContainingDeclaration();

            if (!InlineClassesUtilsKt.isInlineClass(containingDeclaration)) {
                v.anew(objectType);
                v.dup();
            }

            if (dispatchReceiver != null) {
                KotlinType kotlinType = dispatchReceiver.getType();
                Type receiverType = typeMapper.mapType(kotlinType);
                ReceiverValue receiver = getConstructorReceiver(resolvedCall);
                boolean callSuper = containingDeclaration.isInner() && receiver instanceof ImplicitClassReceiver;
                generateReceiverValue(receiver, callSuper).put(receiverType, kotlinType, v);
            }

            // Resolved call to local class constructor doesn't have dispatchReceiver, so we need to generate closure on stack
            // See StackValue.receiver for more info
            pushClosureOnStack(
                    containingDeclaration, dispatchReceiver == null, defaultCallGenerator, /* functionReferenceReceiver = */ null
            );

            constructor = SamCodegenUtil.resolveSamAdapter(constructor);
            OwnerKind inlineClassKindOrNull =
                    InlineClassesUtilsKt.isInlineClass(constructor.getContainingDeclaration()) ? OwnerKind.ERASED_INLINE_CLASS : null;
            CallableMethod method = typeMapper.mapToCallableMethod(constructor, false, inlineClassKindOrNull);
            invokeMethodWithArguments(method, resolvedCall, StackValue.none());

            return Unit.INSTANCE;
        });
    }

    public StackValue generateNewArray(
            @NotNull KtCallExpression expression, @NotNull KotlinType arrayType, @NotNull ResolvedCall<?> resolvedCall
    ) {
        List<KtValueArgument> args = expression.getValueArguments();
        assert args.size() == 1 || args.size() == 2 : "Unknown constructor called: " + args.size() + " arguments";

        if (args.size() == 1) {
            KtExpression sizeExpression = args.get(0).getArgumentExpression();
            return StackValue.operation(typeMapper.mapType(arrayType), v -> {
                gen(sizeExpression, Type.INT_TYPE);
                newArrayInstruction(arrayType);
                return Unit.INSTANCE;
            });
        }

        return invokeFunction(resolvedCall, StackValue.none());
    }

    public void newArrayInstruction(@NotNull KotlinType arrayType) {
        if (KotlinBuiltIns.isArray(arrayType)) {
            KotlinType elementKotlinType = arrayType.getArguments().get(0).getType();
            putReifiedOperationMarkerIfTypeIsReifiedParameter(this, elementKotlinType, ReifiedTypeInliner.OperationKind.NEW_ARRAY);
            v.newarray(boxType(typeMapper.mapTypeAsDeclaration(elementKotlinType)));
        }
        else {
            Type type = typeMapper.mapType(arrayType);
            v.newarray(correctElementType(type));
        }
    }

    @Override
    public StackValue visitArrayAccessExpression(@NotNull KtArrayAccessExpression expression, StackValue receiver) {
        KtExpression array = expression.getArrayExpression();
        KotlinType arrayKotlinType = array != null ? bindingContext.getType(array) : null;
        Type arrayType = expressionType(array);
        List<KtExpression> indices = expression.getIndexExpressions();
        FunctionDescriptor operationDescriptor = (FunctionDescriptor) bindingContext.get(REFERENCE_TARGET, expression);
        assert operationDescriptor != null;
        boolean isInlineClassType = arrayKotlinType != null && InlineClassesUtilsKt.isInlineClassType(arrayKotlinType);
        if (arrayType.getSort() == Type.ARRAY &&
            indices.size() == 1 &&
            isInt(operationDescriptor.getValueParameters().get(0).getType()) &&
            !isInlineClassType
        ) {
            assert arrayKotlinType != null;
            KotlinType elementKotlinType = state.getModule().getBuiltIns().getArrayElementType(arrayKotlinType);
            Type elementType;
            if (KotlinBuiltIns.isArray(arrayKotlinType)) {
                elementType = boxType(asmType(elementKotlinType), elementKotlinType, typeMapper);
            }
            else {
                elementType = correctElementType(arrayType);
            }
            StackValue arrayValue = genLazy(array, arrayType);
            StackValue index = genLazy(indices.get(0), Type.INT_TYPE);

            return StackValue.arrayElement(elementType, elementKotlinType, arrayValue, index);
        }
        else {
            ResolvedCall<FunctionDescriptor> resolvedSetCall = bindingContext.get(INDEXED_LVALUE_SET, expression);
            ResolvedCall<FunctionDescriptor> resolvedGetCall = bindingContext.get(INDEXED_LVALUE_GET, expression);

            boolean isGetter = OperatorNameConventions.GET.equals(operationDescriptor.getName());

            ResolvedCall<FunctionDescriptor> resolvedCall = isGetter ? resolvedGetCall : resolvedSetCall;
            assert resolvedCall != null : "No resolved call for " + operationDescriptor;
            Callable callable = resolveToCallable(accessibleFunctionDescriptor(resolvedCall), false, resolvedCall);
            Callable callableMethod = typeMapper.mapToCallableMethod(SamCodegenUtil.resolveSamAdapter(operationDescriptor), false);
            Type[] argumentTypes = callableMethod.getParameterTypes();

            StackValue.CollectionElementReceiver collectionElementReceiver = createCollectionElementReceiver(
                    expression, receiver, operationDescriptor, isGetter, resolvedGetCall, resolvedSetCall, callable
            );

            Type elementType = isGetter ? callableMethod.getReturnType() : ArrayUtil.getLastElement(argumentTypes);
            KotlinType elementKotlinType = isGetter ?
                                           operationDescriptor.getOriginal().getReturnType() :
                                           CollectionsKt.last(operationDescriptor.getOriginal().getValueParameters()).getType();
            return StackValue.collectionElement(
                    collectionElementReceiver, elementType, elementKotlinType, resolvedGetCall, resolvedSetCall, this
            );
        }
    }

    @NotNull
    private StackValue.CollectionElementReceiver createCollectionElementReceiver(
            @NotNull KtArrayAccessExpression expression,
            @NotNull StackValue receiver,
            @NotNull FunctionDescriptor operationDescriptor,
            boolean isGetter,
            ResolvedCall<FunctionDescriptor> resolvedGetCall,
            ResolvedCall<FunctionDescriptor> resolvedSetCall,
            @NotNull Callable callable
    ) {
        ResolvedCall<FunctionDescriptor> resolvedCall = isGetter ? resolvedGetCall : resolvedSetCall;
        assert resolvedCall != null : "couldn't find resolved call: " + expression.getText();

        List<ResolvedValueArgument> konstueArguments = resolvedCall.getValueArgumentsByIndex();
        assert konstueArguments != null : "Failed to arrange konstue arguments by index: " + operationDescriptor;

        if (!isGetter) {
            assert konstueArguments.size() >= 2 : "Setter call should have at least 2 arguments: " + operationDescriptor;
            // Skip generation of the right hand side of an indexed assignment, which is the last konstue argument
            konstueArguments.remove(konstueArguments.size() - 1);
        }

        return new StackValue.CollectionElementReceiver(
                callable, receiver, resolvedGetCall, resolvedSetCall, isGetter, this, konstueArguments
        );
    }

    @Override
    public StackValue visitThrowExpression(@NotNull KtThrowExpression expression, StackValue receiver) {
        return StackValue.operation(Type.VOID_TYPE, getNothingType(), adapter -> {
            gen(expression.getThrownExpression(), JAVA_THROWABLE_TYPE);
            v.athrow();
            return Unit.INSTANCE;
        });
    }

    @Override
    public StackValue visitThisExpression(@NotNull KtThisExpression expression, StackValue receiver) {
        DeclarationDescriptor descriptor = bindingContext.get(REFERENCE_TARGET, expression.getInstanceReference());
        if (descriptor instanceof ClassDescriptor) {
            return generateInstanceReceiver((ClassDescriptor) descriptor, false, true);
            //TODO rewrite with context.lookupInContext()
            //return StackValue.thisOrOuter(this, (ClassDescriptor) descriptor, false, true);
        }
        if (descriptor instanceof CallableDescriptor) {
            return generateExtensionReceiver((CallableDescriptor) descriptor);
        }
        throw new UnsupportedOperationException("Neither this nor receiver: " + descriptor + expression.getParent().getContainingFile().getText());
    }

    @Override
    public StackValue visitTryExpression(@NotNull KtTryExpression expression, StackValue receiver) {
        return generateTryExpression(expression, false);
    }

    public StackValue generateTryExpression(KtTryExpression expression, boolean isStatement) {
        /*
The "returned" konstue of try expression with no finally is either the last expression in the try block or the last expression in the catch block
(or blocks).
         */

        Type expectedAsmType = isStatement ? Type.VOID_TYPE : expressionType(expression);
        KotlinType expectedKotlinType = isStatement ? null : kotlinType(expression);

        return StackValue.operation(expectedAsmType, expectedKotlinType, v -> {
            KtFinallySection finallyBlock = expression.getFinallyBlock();
            TryWithFinallyBlockStackElement tryWithFinallyBlockStackElement = null;
            TryBlockStackElement element;
            if (finallyBlock != null) {
                tryWithFinallyBlockStackElement = new TryWithFinallyBlockStackElement(expression);
                element = tryWithFinallyBlockStackElement;
                blockStackElements.push(tryWithFinallyBlockStackElement);
            }
            else {
                element = new TryBlockStackElement();
                blockStackElements.push(element);
            }

            //PseudoInsnsPackage.saveStackBeforeTryExpr(v);

            Label tryStart = new Label();
            v.mark(tryStart);
            v.nop(); // prevent verify error on empty try

            gen(expression.getTryBlock(), expectedAsmType, expectedKotlinType);

            int savedValue = -1;
            if (!isStatement) {
                savedValue = myFrameMap.enterTemp(expectedAsmType);
                v.store(savedValue, expectedAsmType);
            }

            Label tryEnd = new Label();
            v.mark(tryEnd);

            //do it before finally block generation
            List<Label> tryBlockRegions = getCurrentCatchInterkonsts(element, tryStart, tryEnd);

            Label end = new Label();

            genFinallyBlockOrGoto(tryWithFinallyBlockStackElement, end, null, new ArrayList<>());

            List<KtCatchClause> clauses = expression.getCatchClauses();
            for (int i = 0, size = clauses.size(); i < size; i++) {
                KtCatchClause clause = clauses.get(i);

                Label clauseStart = new Label();
                v.mark(clauseStart);

                KtExpression catchBody = clause.getCatchBody();
                if (catchBody != null) {
                    markLineNumber(catchBody, false);
                }

                VariableDescriptor descriptor = bindingContext.get(VALUE_PARAMETER, clause.getCatchParameter());
                assert descriptor != null;
                Type descriptorType = asmType(descriptor.getType());
                myFrameMap.enter(descriptor, descriptorType);
                int index = lookupLocalIndex(descriptor);
                v.store(index, descriptorType);

                Label catchVariableStart = new Label();
                v.mark(catchVariableStart);

                gen(catchBody, expectedAsmType, expectedKotlinType);

                if (!isStatement) {
                    v.store(savedValue, expectedAsmType);
                }

                myFrameMap.leave(descriptor);

                Label clauseEnd = new Label();
                v.mark(clauseEnd);

                v.visitLocalVariable(descriptor.getName().asString(), descriptorType.getDescriptor(), null,
                                     catchVariableStart, clauseEnd, index);

                genFinallyBlockOrGoto(tryWithFinallyBlockStackElement, i != size - 1 || finallyBlock != null ? end : null, null, new ArrayList<>());

                generateExceptionTable(clauseStart, tryBlockRegions, descriptorType.getInternalName());
            }


            //for default catch clause
            if (finallyBlock != null) {
                Label defaultCatchStart = new Label();
                v.mark(defaultCatchStart);
                int savedException = myFrameMap.enterTemp(JAVA_THROWABLE_TYPE);
                v.store(savedException, JAVA_THROWABLE_TYPE);

                Label defaultCatchEnd = new Label();
                v.mark(defaultCatchEnd);

                //do it before finally block generation
                //javac also generates entry in exception table for default catch clause too!!!! so defaultCatchEnd as end parameter
                List<Label> defaultCatchRegions = getCurrentCatchInterkonsts(tryWithFinallyBlockStackElement, tryStart, defaultCatchEnd);


                genFinallyBlockOrGoto(tryWithFinallyBlockStackElement, null, null, new ArrayList<>());

                v.load(savedException, JAVA_THROWABLE_TYPE);
                myFrameMap.leaveTemp(JAVA_THROWABLE_TYPE);

                v.athrow();

                generateExceptionTable(defaultCatchStart, defaultCatchRegions, null);
            }

            markLineNumber(expression, isStatement);
            v.mark(end);

            if (!isStatement) {
                v.load(savedValue, expectedAsmType);
                myFrameMap.leaveTemp(expectedAsmType);
            }


            blockStackElements.pop();

            return Unit.INSTANCE;
        });
    }

    private void generateExceptionTable(@NotNull Label catchStart, @NotNull List<Label> catchedRegions, @Nullable String exception) {
        for (int i = 0; i < catchedRegions.size(); i += 2) {
            Label startRegion = catchedRegions.get(i);
            Label endRegion = catchedRegions.get(i + 1);

            if (startRegion.info == null || endRegion.info == null || catchStart.info == null) {
                throw new IllegalStateException("All labels should have nodes added to instruction list");
            }

            v.visitTryCatchBlock(startRegion, endRegion, catchStart, exception);
        }
    }

    @NotNull
    private static List<Label> getCurrentCatchInterkonsts(
            @Nullable TryBlockStackElement finallyBlockStackElement,
            @NotNull Label blockStart,
            @NotNull Label blockEnd
    ) {
        List<Label> gapsInBlock =
                finallyBlockStackElement != null ? new ArrayList<>(finallyBlockStackElement.gaps) : Collections.emptyList();
        assert gapsInBlock.size() % 2 == 0;
        List<Label> blockRegions = new ArrayList<>(gapsInBlock.size() + 2);
        blockRegions.add(blockStart);
        blockRegions.addAll(gapsInBlock);
        blockRegions.add(blockEnd);
        return blockRegions;
    }

    @Override
    public StackValue visitBinaryWithTypeRHSExpression(@NotNull KtBinaryExpressionWithTypeRHS expression, StackValue receiver) {
        KtExpression left = expression.getLeft();
        IElementType opToken = expression.getOperationReference().getReferencedNameElementType();

        KotlinType rightKotlinType = bindingContext.get(TYPE, expression.getRight());
        assert rightKotlinType != null;

        StackValue konstue = genQualified(receiver, left);

        KotlinType leftKotlinType = konstue.kotlinType;
        Type boxedLeftType;
        if (leftKotlinType != null && InlineClassesUtilsKt.isInlineClassType(leftKotlinType)) {
            boxedLeftType = typeMapper.mapTypeAsDeclaration(leftKotlinType);
        } else {
            boxedLeftType = boxType(konstue.type);
        }

        Type boxedRightType = boxType(typeMapper.mapTypeAsDeclaration(rightKotlinType));
        return StackValue.operation(boxedRightType, rightKotlinType, v -> {
            konstue.put(boxedLeftType, konstue.kotlinType, v);

            if (konstue.type == Type.VOID_TYPE) {
                StackValue.putUnitInstance(v);
            }

            boolean safeAs = opToken == KtTokens.AS_SAFE;
            if (TypeUtils.isReifiedTypeParameter(rightKotlinType)) {
                putReifiedOperationMarkerIfTypeIsReifiedParameter(this, rightKotlinType,
                                                                  safeAs ? ReifiedTypeInliner.OperationKind.SAFE_AS
                                                                         : ReifiedTypeInliner.OperationKind.AS);
                v.checkcast(boxedRightType);
                return Unit.INSTANCE;
            }

            CodegenUtilKt.generateAsCast(
                    v, rightKotlinType, boxedRightType, safeAs, state.getUnifiedNullChecks()
            );

            return Unit.INSTANCE;
        });
    }

    @Override
    public StackValue visitIsExpression(@NotNull KtIsExpression expression, StackValue receiver) {
        StackValue match = StackValue.expression(OBJECT_TYPE, expression.getLeftHandSide(), this);
        return generateIsCheck(match, expression.getTypeReference(), expression.isNegated());
    }

    private StackValue generateExpressionMatch(StackValue expressionToMatch, KtExpression subjectExpression, KotlinType subjectKotlinType, KtExpression patternExpression) {
        if (expressionToMatch != null) {
            return generateEquals(
                    subjectExpression, patternExpression, KtTokens.EQEQ, expressionToMatch, subjectKotlinType,
                    bindingContext.get(BindingContext.PRIMITIVE_NUMERIC_COMPARISON_INFO, patternExpression)
            );
        }
        else {
            return gen(patternExpression);
        }
    }

    private StackValue generateIsCheck(StackValue expressionToMatch, KtTypeReference typeReference, boolean negated) {
        KotlinType kotlinType = bindingContext.get(TYPE, typeReference);
        markStartLineNumber(typeReference);
        StackValue konstue = generateIsCheck(expressionToMatch, kotlinType, false);
        return negated ? StackValue.not(konstue) : konstue;
    }

    private StackValue generateIsCheck(StackValue expressionToGen, KotlinType rhsKotlinType, boolean leaveExpressionOnStack) {
        KotlinType lhsKotlinType = expressionToGen.kotlinType;
        Type lhsBoxedType;
        if (lhsKotlinType != null && InlineClassesUtilsKt.isInlineClassType(lhsKotlinType)) {
            lhsBoxedType = typeMapper.mapTypeAsDeclaration(lhsKotlinType);
        }
        else {
            lhsBoxedType = OBJECT_TYPE;
        }
        return StackValue.operation(Type.BOOLEAN_TYPE, v -> {
            expressionToGen.put(lhsBoxedType, expressionToGen.kotlinType, v);
            if (leaveExpressionOnStack) {
                v.dup();
            }

            Type type = boxType(typeMapper.mapTypeAsDeclaration(rhsKotlinType));
            if (TypeUtils.isReifiedTypeParameter(rhsKotlinType)) {
                putReifiedOperationMarkerIfTypeIsReifiedParameter(this, rhsKotlinType, ReifiedTypeInliner.OperationKind.IS);
                v.instanceOf(type);
                return null;
            }

            CodegenUtilKt.generateIsCheck(v, rhsKotlinType, type);
            return null;
        });
    }

    @Override
    public void propagateChildReifiedTypeParametersUsages(@NotNull ReifiedTypeParametersUsages usages) {
        parentCodegen.getReifiedTypeParametersUsages().propagateChildUsagesWithinContext(
                usages,
                () -> context.getContextDescriptor().getTypeParameters().stream().filter(TypeParameterDescriptor::isReified).map(
                        it -> it.getName().asString()).collect(Collectors.toSet())
        );
    }

    @Override
    public StackValue visitWhenExpression(@NotNull KtWhenExpression expression, StackValue receiver) {
        return generateWhenExpression(expression, false);
    }

    public StackValue generateWhenExpression(KtWhenExpression expression, boolean isStatement) {
        Type resultType = isStatement ? Type.VOID_TYPE : expressionType(expression);
        KotlinType resultKotlinType = isStatement ? null : kotlinType(expression);

        return StackValue.operation(resultType, resultKotlinType, v -> {
            KtProperty subjectVariable = expression.getSubjectVariable();
            KtExpression subjectExpression = expression.getSubjectExpression();

            if (subjectVariable == null && subjectExpression == null) {
                v.nop();
            }

            SwitchCodegen switchCodegen = switchCodegenProvider.buildAppropriateSwitchCodegenIfPossible(
                    expression, isStatement, CodegenUtil.isExhaustive(bindingContext, expression, isStatement)
            );
            if (switchCodegen != null) {
                switchCodegen.generate();
                return null;
            }

            int subjectLocal;
            Type subjectType;
            KotlinType subjectKotlinType;
            VariableDescriptor subjectVariableDescriptor = null;
            if (subjectVariable != null) {
                subjectVariableDescriptor = bindingContext.get(BindingContext.VARIABLE, subjectVariable);
                assert subjectVariableDescriptor != null : "Unresolved subject variable: " + subjectVariable.getName();
                subjectKotlinType = subjectVariableDescriptor.getType();
                subjectType = asmType(subjectVariableDescriptor.getType());
                subjectLocal = myFrameMap.enter(subjectVariableDescriptor, subjectType);
                visitProperty(subjectVariable, null);
                tempVariables.put(subjectExpression, StackValue.local(subjectLocal, subjectType, subjectKotlinType));
            }
            else if (subjectExpression != null) {
                subjectKotlinType = kotlinType(subjectExpression);
                subjectType = expressionType(subjectExpression);
                subjectLocal = myFrameMap.enterTemp(subjectType);
                gen(subjectExpression, subjectType, subjectKotlinType);
                tempVariables.put(subjectExpression, StackValue.local(subjectLocal, subjectType, subjectKotlinType));
                v.store(subjectLocal, subjectType);
            }
            else {
                subjectLocal = -1;
                subjectType = Type.VOID_TYPE;
                subjectKotlinType = null;
            }

            Label begin = new Label();
            v.mark(begin);

            Label end = new Label();
            boolean hasElse = KtPsiUtil.checkWhenExpressionHasSingleElse(expression);

            Label nextCondition = null;
            for (KtWhenEntry whenEntry : expression.getEntries()) {
                if (nextCondition != null) {
                    v.mark(nextCondition);
                }
                nextCondition = new Label();
                FrameMap.Mark mark = myFrameMap.mark();
                Label thisEntry = new Label();
                if (!whenEntry.isElse()) {
                    KtWhenCondition[] conditions = whenEntry.getConditions();
                    for (int i = 0; i < conditions.length; i++) {
                        StackValue conditionValue = generateWhenCondition(subjectExpression, subjectType, subjectKotlinType, subjectLocal, conditions[i]);
                        BranchedValue.Companion.condJump(conditionValue, nextCondition, true, v);
                        if (i < conditions.length - 1) {
                            v.goTo(thisEntry);
                            v.mark(nextCondition);
                            nextCondition = new Label();
                        }
                    }
                }

                v.visitLabel(thisEntry);
                gen(whenEntry.getExpression(), resultType, resultKotlinType);
                mark.dropTo();
                if (!whenEntry.isElse()) {
                    v.goTo(end);
                }
            }
            if (!hasElse && nextCondition != null) {
                v.mark(nextCondition);
                putUnitInstanceOntoStackForNonExhaustiveWhen(expression, isStatement);
            }

            markLineNumber(expression, isStatement);
            v.mark(end);

            if (subjectVariableDescriptor != null) {
                myFrameMap.leave(subjectVariableDescriptor);
                v.visitLocalVariable(
                        subjectVariableDescriptor.getName().asString(), subjectType.getDescriptor(), null,
                        begin, end, subjectLocal
                );
            }
            else {
                myFrameMap.leaveTemp(subjectType);
            }
            tempVariables.remove(subjectExpression);
            return null;
        });
    }

    public void putUnitInstanceOntoStackForNonExhaustiveWhen(
            @NotNull KtWhenExpression whenExpression,
            boolean isStatement
    ) {
        if (CodegenUtil.isExhaustive(bindingContext, whenExpression, isStatement)) {
            // when() is supposed to be exhaustive
            genThrow(v, "kotlin/NoWhenBranchMatchedException", null);
        }
        else if (!isStatement) {
            // non-exhaustive when() with no else -> Unit must be expected
            StackValue.putUnitInstance(v);
        }
    }

    private StackValue generateWhenCondition(KtExpression subjectExpression, Type subjectType, KotlinType subjectKotlinType, int subjectLocal, KtWhenCondition condition) {
        if (condition instanceof KtWhenConditionInRange) {
            KtWhenConditionInRange conditionInRange = (KtWhenConditionInRange) condition;
            return generateIn(StackValue.local(subjectLocal, subjectType, subjectKotlinType),
                              conditionInRange.getRangeExpression(),
                              conditionInRange.getOperationReference());
        }
        StackValue.Local match = subjectLocal == -1 ? null : StackValue.local(subjectLocal, subjectType, subjectKotlinType);
        if (condition instanceof KtWhenConditionIsPattern) {
            KtWhenConditionIsPattern patternCondition = (KtWhenConditionIsPattern) condition;
            return generateIsCheck(match, patternCondition.getTypeReference(), patternCondition.isNegated());
        }
        else if (condition instanceof KtWhenConditionWithExpression) {
            KtExpression patternExpression = ((KtWhenConditionWithExpression) condition).getExpression();
            return generateExpressionMatch(match, subjectExpression, subjectKotlinType, patternExpression);
        }
        else {
            throw new UnsupportedOperationException("unsupported kind of when condition");
        }
    }

    public Call makeFakeCall(ReceiverValue initializerAsReceiver) {
        KtSimpleNameExpression fake = new KtPsiFactory(state.getProject(), false).createSimpleName("fake");
        return CallMaker.makeCall(fake, initializerAsReceiver);
    }

    @Override
    public String toString() {
        return context.getContextDescriptor().toString();
    }

    @Override
    @NotNull
    public FrameMap getFrameMap() {
        return myFrameMap;
    }

    @NotNull
    public MethodContext getContext() {
        return context;
    }

    @Override
    @NotNull
    public NameGenerator getInlineNameGenerator() {
        NameGenerator nameGenerator = getParentCodegen().getInlineNameGenerator();
        Name name = context.getContextDescriptor().getName();
        String inlinedName = name.isSpecial() ? SPECIAL_TRANSFORMATION_NAME : name.asString();
        return nameGenerator.subGenerator(inlinedName + INLINE_CALL_TRANSFORMATION_SUFFIX);
    }

    public Type getReturnType() {
        return returnType;
    }

    public Stack<BlockStackElement> getBlockStackElements() {
        return new Stack<>(blockStackElements);
    }

    public void addBlockStackElementsForNonLocalReturns(@NotNull Stack<BlockStackElement> elements, int finallyDepth) {
        blockStackElements.addAll(elements);
        this.finallyDepth = finallyDepth;
    }

    private static class NonLocalReturnInfo {

        private final CallableDescriptor descriptor;

        private final String labelName;

        private NonLocalReturnInfo(@NotNull CallableDescriptor descriptor, @NotNull String name) {
            this.descriptor = descriptor;
            labelName = name;
        }

        private JvmKotlinType getJvmKotlinType(@NotNull KotlinTypeMapper typeMapper) {
            return new JvmKotlinType(typeMapper.mapReturnType(descriptor), descriptor.getReturnType());
        }
    }

    @NotNull
    private StackValue.Delegate delegatedVariableValue(
            @NotNull StackValue delegateValue,
            @NotNull StackValue metadataValue,
            @NotNull VariableDescriptorWithAccessors variableDescriptor,
            @NotNull KotlinTypeMapper typeMapper
    ) {
        return StackValue.localDelegate(typeMapper.mapType(variableDescriptor.getType()), delegateValue, metadataValue, variableDescriptor, this);
    }

    @NotNull
    @Override
    public InstructionAdapter getVisitor() {
        return v;
    }

    @NotNull
    @Override
    public TypeSystemCommonBackendContext getTypeSystem() {
        return typeSystem;
    }

    @Override
    public void consumeReifiedOperationMarker(@NotNull TypeParameterMarker typeParameter) {
        assert typeParameter instanceof TypeParameterDescriptor : "Type parameter should be a descriptor: " + typeParameter;
        TypeParameterDescriptor typeParameterDescriptor = (TypeParameterDescriptor) typeParameter;
        if (typeParameterDescriptor.getContainingDeclaration() != context.getContextDescriptor()) {
            parentCodegen.getReifiedTypeParametersUsages().addUsedReifiedParameter(typeParameterDescriptor.getName().asString());
        }
    }
}
