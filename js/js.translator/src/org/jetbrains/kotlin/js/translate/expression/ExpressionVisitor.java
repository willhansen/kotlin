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

package org.jetbrains.kotlin.js.translate.expression;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.builtins.KotlinBuiltIns;
import org.jetbrains.kotlin.builtins.functions.FunctionClassDescriptor;
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind;
import org.jetbrains.kotlin.config.LanguageVersion;
import org.jetbrains.kotlin.descriptors.*;
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor;
import org.jetbrains.kotlin.descriptors.annotations.KotlinRetention;
import org.jetbrains.kotlin.js.backend.ast.*;
import org.jetbrains.kotlin.js.backend.ast.metadata.MetadataProperties;
import org.jetbrains.kotlin.js.backend.ast.metadata.SpecialFunction;
import org.jetbrains.kotlin.js.naming.NameSuggestion;
import org.jetbrains.kotlin.js.translate.context.TranslationContext;
import org.jetbrains.kotlin.js.translate.declaration.ClassTranslator;
import org.jetbrains.kotlin.js.translate.declaration.PropertyTranslatorKt;
import org.jetbrains.kotlin.js.translate.general.Translation;
import org.jetbrains.kotlin.js.translate.general.TranslatorVisitor;
import org.jetbrains.kotlin.js.translate.operation.BinaryOperationTranslator;
import org.jetbrains.kotlin.js.translate.operation.UnaryOperationTranslator;
import org.jetbrains.kotlin.js.translate.reference.*;
import org.jetbrains.kotlin.js.translate.utils.BindingUtils;
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils;
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils;
import org.jetbrains.kotlin.js.translate.utils.UtilsKt;
import org.jetbrains.kotlin.js.translate.utils.mutator.CoercionMutator;
import org.jetbrains.kotlin.js.translate.utils.mutator.LastExpressionMutator;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.psi.psiUtil.PsiUtilsKt;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.BindingContextUtils;
import org.jetbrains.kotlin.resolve.DescriptorUtils;
import org.jetbrains.kotlin.resolve.bindingContextUtil.BindingContextUtilsKt;
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall;
import org.jetbrains.kotlin.resolve.constants.CompileTimeConstant;
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ConstantExpressionEkonstuator;
import org.jetbrains.kotlin.resolve.descriptorUtil.DescriptorUtilsKt;
import org.jetbrains.kotlin.resolve.inline.InlineUtil;
import org.jetbrains.kotlin.types.KotlinType;
import org.jetbrains.kotlin.types.expressions.DoubleColonLHS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.jetbrains.kotlin.descriptors.FindClassInModuleKt.findClassAcrossModuleDependencies;
import static org.jetbrains.kotlin.js.translate.context.Namer.GET_KCLASS_FROM_EXPRESSION;
import static org.jetbrains.kotlin.js.translate.context.Namer.getCapturedVarAccessor;
import static org.jetbrains.kotlin.js.translate.general.Translation.translateAsExpression;
import static org.jetbrains.kotlin.js.translate.utils.BindingUtils.*;
import static org.jetbrains.kotlin.js.translate.utils.ErrorReportingUtils.message;
import static org.jetbrains.kotlin.js.translate.utils.JsAstUtils.*;
import static org.jetbrains.kotlin.js.translate.utils.JsDescriptorUtils.getReceiverParameterForDeclaration;
import static org.jetbrains.kotlin.js.translate.utils.TranslationUtils.translateInitializerForProperty;
import static org.jetbrains.kotlin.resolve.BindingContext.*;
import static org.jetbrains.kotlin.resolve.calls.util.CallUtilKt.getResolvedCallWithAssert;
import static org.jetbrains.kotlin.resolve.descriptorUtil.DescriptorUtilsKt.getAnnotationClass;
import static org.jetbrains.kotlin.types.expressions.ExpressionTypingUtils.isFunctionExpression;
import static org.jetbrains.kotlin.types.expressions.ExpressionTypingUtils.isFunctionLiteral;

public final class ExpressionVisitor extends TranslatorVisitor<JsNode> {
    private static final FqName primitiveClassesFqName = new FqName("kotlin.reflect.js.internal.PrimitiveClasses");

    @Override
    protected JsNode emptyResult(@NotNull TranslationContext context) {
        return new JsNullLiteral();
    }

    @Override
    @NotNull
    public JsNode visitConstantExpression(@NotNull KtConstantExpression expression, @NotNull TranslationContext context) {
        return translateConstantExpression(expression, context).source(expression);
    }

    @NotNull
    private static JsNode translateConstantExpression(@NotNull KtConstantExpression expression, @NotNull TranslationContext context) {
        CompileTimeConstant<?> compileTimeValue = ConstantExpressionEkonstuator.getConstant(expression, context.bindingContext());
        assert compileTimeValue != null : message(expression, "Expression is not compile time konstue: " + expression.getText() + " ");

        JsNode result = Translation.translateConstant(compileTimeValue, expression, context);
        if (result == null) {
            throw new AssertionError(message(expression, "Unsupported constant expression: " + expression.getText() + " "));
        }

        return result;
    }

    @Override
    @NotNull
    public JsNode visitBlockExpression(@NotNull KtBlockExpression ktBlockExpression, @NotNull TranslationContext context) {
        List<KtExpression> statements = ktBlockExpression.getStatements();
        JsBlock jsBlock = new JsBlock();
        for (KtExpression statement : statements) {
            JsNode jsNode = Translation.translateExpression(statement, context, jsBlock);
            JsStatement jsStatement = convertToStatement(jsNode);
            if (!JsAstUtils.isEmptyStatement(jsStatement)) {
                jsBlock.getStatements().add(jsStatement);
            }
        }
        if (statements.isEmpty()) {
            ClassDescriptor unitClass = context.getCurrentModule().getBuiltIns().getUnit();
            jsBlock.getStatements().add(JsAstUtils.asSyntheticStatement(
                    ReferenceTranslator.translateAsValueReference(unitClass, context)));
        }
        return jsBlock;
    }

    @Override
    public JsNode visitDestructuringDeclaration(@NotNull KtDestructuringDeclaration multiDeclaration, @NotNull TranslationContext context) {
        KtExpression ktInitializer = multiDeclaration.getInitializer();
        assert ktInitializer != null : "Initializer for multi declaration must be not null";
        JsExpression initializer = Translation.translateAsExpression(ktInitializer, context);
        JsName parameterName = JsScope.declareTemporary();
        JsVars tempVarDeclaration = JsAstUtils.newVar(parameterName, initializer);
        MetadataProperties.setSynthetic(tempVarDeclaration, true);
        context.addStatementToCurrentBlock(tempVarDeclaration);
        return DestructuringDeclarationTranslator.translate(multiDeclaration, JsAstUtils.pureFqn(parameterName, null), context);
    }

    @Override
    @NotNull
    public JsNode visitReturnExpression(@NotNull KtReturnExpression ktReturnExpression, @NotNull TranslationContext context) {
        KtExpression returned = ktReturnExpression.getReturnedExpression();

        // TODO: add related descriptor to context and use it here
        KtDeclarationWithBody parent = PsiTreeUtil.getParentOfType(ktReturnExpression, KtDeclarationWithBody.class);
        if (parent instanceof KtSecondaryConstructor) {
            ClassDescriptor classDescriptor = context.getClassDescriptor();
            assert classDescriptor != null : "Missing class descriptor in context while translating constructor: " +
                    PsiUtilsKt.getTextWithLocation(ktReturnExpression);
            JsExpression ref = ReferenceTranslator.translateAsValueReference(classDescriptor.getThisAsReceiverParameter(), context);
            return new JsReturn(ref.source(ktReturnExpression));
        }

        FunctionDescriptor returnTarget = getNonLocalReturnTarget(ktReturnExpression, context);

        JsReturn jsReturn;
        if (returned == null) {
            JsExpression returnExpression = null;
            if (returnTarget != null && KotlinBuiltIns.mayReturnNonUnitValue(returnTarget)) {
                ClassDescriptor unit = context.getCurrentModule().getBuiltIns().getUnit();
                returnExpression = ReferenceTranslator.translateAsValueReference(unit, context);
            }
            jsReturn = new JsReturn(returnExpression);
        }
        else {
            JsExpression jsReturnExpression = translateAsExpression(returned, context);

            KotlinType returnedType = context.bindingContext().getType(returned);
            assert returnedType != null : "Resolved return expression is expected to have type: " +
                                          PsiUtilsKt.getTextWithLocation(ktReturnExpression);

            CallableDescriptor returnTargetOrCurrentFunction = returnTarget;
            if (returnTargetOrCurrentFunction == null) {
                returnTargetOrCurrentFunction = (CallableDescriptor) context.getDeclarationDescriptor();
            }
            if (returnTargetOrCurrentFunction != null) {
                jsReturnExpression = TranslationUtils.coerce(context, jsReturnExpression,
                                                             TranslationUtils.getReturnTypeForCoercion(returnTargetOrCurrentFunction));
            }

            jsReturn = new JsReturn(jsReturnExpression);
        }

        MetadataProperties.setReturnTarget(jsReturn, returnTarget);

        return jsReturn.source(ktReturnExpression);
    }

    @Nullable
    private static FunctionDescriptor getNonLocalReturnTarget(
            @NotNull KtReturnExpression expression,
            @NotNull TranslationContext context
    ) {
        DeclarationDescriptor descriptor = context.getDeclarationDescriptor();
        assert descriptor instanceof CallableMemberDescriptor : "Return expression can only be inside callable declaration: " +
                                                                PsiUtilsKt.getTextWithLocation(expression);
        KtSimpleNameExpression target = expression.getTargetLabel();

        //call inside lambda
        if (isFunctionLiteral(descriptor) || isFunctionExpression(descriptor)) {
            if (target == null) {
                if (isFunctionLiteral(descriptor)) {
                    return BindingContextUtils.getContainingFunctionSkipFunctionLiterals(descriptor, true).getFirst();
                }
            }
            else {
                PsiElement element = context.bindingContext().get(LABEL_TARGET, target);
                descriptor = context.bindingContext().get(DECLARATION_TO_DESCRIPTOR, element);
            }
        }

        assert descriptor == null || descriptor instanceof FunctionDescriptor :
                "Function descriptor expected to be target of return label: " + PsiUtilsKt.getTextWithLocation(expression);
        return (FunctionDescriptor) descriptor;
    }

    @Override
    @NotNull
    public JsNode visitParenthesizedExpression(@NotNull KtParenthesizedExpression expression,
            @NotNull TranslationContext context) {
        KtExpression expressionInside = expression.getExpression();
        if (expressionInside != null) {
            return Translation.translateExpression(expressionInside, context);
        }
        return JsEmpty.INSTANCE;
    }

    @Override
    @NotNull
    public JsNode visitBinaryExpression(@NotNull KtBinaryExpression expression,
            @NotNull TranslationContext context) {
        return BinaryOperationTranslator.translate(expression, context);
    }

    @Override
    @NotNull
    // assume it is a local variable declaration
    public JsNode visitProperty(@NotNull KtProperty expression, @NotNull TranslationContext context) {
        VariableDescriptor descriptor = BindingContextUtils.getNotNull(context.bindingContext(), BindingContext.VARIABLE, expression);

        JsExpression initializer = translateInitializerForProperty(expression, context);

        KtExpression delegateExpression = expression.getDelegateExpression();
        JsName name = context.getNameForDescriptor(descriptor);
        if (delegateExpression != null) {
            initializer = PropertyTranslatorKt.translateDelegateOrInitializerExpression(context, expression);
            assert initializer != null : "Initializer must be non-null for property with delegate";
        }
        else if (context.isBoxedLocalCapturedInClosure(descriptor)) {
            JsNameRef alias = getCapturedVarAccessor(name.makeRef());
            initializer = JsAstUtils.wrapValue(alias, initializer == null ? new JsNullLiteral() : initializer);
        }

        return newVar(name, initializer).source(expression);
    }

    @Override
    @NotNull
    public JsNode visitCallableReferenceExpression(@NotNull KtCallableReferenceExpression expression, @NotNull TranslationContext context) {
        return CallableReferenceTranslator.INSTANCE.translate(expression, context);
    }

    @Override
    public JsNode visitClassLiteralExpression(
            @NotNull KtClassLiteralExpression expression, TranslationContext context
    ) {
        KtExpression receiverExpression = expression.getReceiverExpression();
        assert receiverExpression != null : "Class literal expression should have a left-hand side";

        DoubleColonLHS lhs = context.bindingContext().get(DOUBLE_COLON_LHS, receiverExpression);
        assert lhs != null : "Class literal expression should have LHS resolved";

        ClassifierDescriptor descriptor = lhs.getType().getConstructor().getDeclarationDescriptor();

        if (lhs instanceof DoubleColonLHS.Expression && !((DoubleColonLHS.Expression) lhs).isObjectQualifier()) {
            JsExpression receiver = translateAsExpression(receiverExpression, context);
            receiver = TranslationUtils.coerce(context, receiver, context.getCurrentModule().getBuiltIns().getAnyType());
            if (isPrimitiveClassLiteral(lhs.getType())) {
                JsExpression primitiveExpression = getPrimitiveClass(context, descriptor);
                if (primitiveExpression != null) {
                    return JsAstUtils.newSequence(Arrays.asList(receiver, primitiveExpression));
                }
            }
            return new JsInvocation(context.namer().kotlin(GET_KCLASS_FROM_EXPRESSION), receiver);
        }

        return getObjectKClass(context, descriptor);
    }

    @NotNull
    public static JsExpression getObjectKClass(@NotNull TranslationContext context, @Nullable ClassifierDescriptor descriptor) {
        JsExpression primitiveExpression = getPrimitiveClass(context, descriptor);
        if (primitiveExpression != null) return primitiveExpression;

        // getKClass should be imported as intrinsic when used outside of inline context, otherwise bootstrap fails.
        // Inside an inline function it should however be marked as SpecialFunction to support T::class when T -> Int (KT-32215)
        JsName kClassName = context.getNameForIntrinsic(SpecialFunction.GET_KCLASS.getSuggestedName());
        MetadataProperties.setSpecialFunction(kClassName, SpecialFunction.GET_KCLASS);

        return new JsInvocation(kClassName.makeRef(), UtilsKt.getReferenceToJsClass(descriptor, context));
    }

    @Nullable
    public static JsExpression getPrimitiveClass(@NotNull TranslationContext context, @Nullable ClassifierDescriptor classifierDescriptor) {
        if (!context.getConfig().isAtLeast(LanguageVersion.KOTLIN_1_2) || findPrimitiveClassesObject(context) == null) return null;
        if (!(classifierDescriptor instanceof ClassDescriptor)) return null;
        ClassDescriptor descriptor = (ClassDescriptor) classifierDescriptor;

        FqName fqName = DescriptorUtilsKt.getFqNameSafe(descriptor);
        switch (fqName.asString()) {
            case "kotlin.Boolean":
            case "kotlin.Byte":
            case "kotlin.Short":
            case "kotlin.Int":
            case "kotlin.Float":
            case "kotlin.Double":
            case "kotlin.String":
            case "kotlin.Array":
            case "kotlin.Any":
            case "kotlin.Throwable":
            case "kotlin.Number":
            case "kotlin.Nothing":
            case "kotlin.BooleanArray":
            case "kotlin.CharArray":
            case "kotlin.ByteArray":
            case "kotlin.ShortArray":
            case "kotlin.IntArray":
            case "kotlin.LongArray":
            case "kotlin.FloatArray":
            case "kotlin.DoubleArray":
                return getKotlinPrimitiveClassRef(context, StringUtil.decapitalize(fqName.shortName().asString()) + "Class");

            default: {
                if (descriptor instanceof FunctionClassDescriptor) {
                    FunctionClassDescriptor functionClassDescriptor = (FunctionClassDescriptor) descriptor;
                    if (functionClassDescriptor.getFunctionTypeKind() == FunctionTypeKind.Function.INSTANCE) {
                        ClassDescriptor primitivesObject = findPrimitiveClassesObject(context);
                        assert primitivesObject != null;
                        FunctionDescriptor function = DescriptorUtils.getFunctionByName(
                                primitivesObject.getUnsubstitutedMemberScope(), Name.identifier("functionClass"));
                        JsExpression functionRef = pureFqn(context.getInlineableInnerNameForDescriptor(function), null);
                        return new JsInvocation(functionRef, new JsIntLiteral(functionClassDescriptor.getArity()));
                    }
                }
                break;
            }
        }
        return null;
    }

    @NotNull
    private static JsExpression getKotlinPrimitiveClassRef(@NotNull TranslationContext context, @NotNull String name) {
        ClassDescriptor primitivesObject = findPrimitiveClassesObject(context);
        assert primitivesObject != null;
        PropertyDescriptor property = DescriptorUtils.getPropertyByName(
                primitivesObject.getUnsubstitutedMemberScope(), Name.identifier(name));
        return pureFqn(context.getInlineableInnerNameForDescriptor(property), null);
    }

    private static boolean isPrimitiveClassLiteral(@NotNull KotlinType type) {
        return KotlinBuiltIns.isPrimitiveType(type) || KotlinBuiltIns.isArray(type) || KotlinBuiltIns.isPrimitiveArray(type);
    }

    @Nullable
    private static ClassDescriptor findPrimitiveClassesObject(@NotNull TranslationContext context) {
        return findClassAcrossModuleDependencies(context.getCurrentModule(), ClassId.topLevel(primitiveClassesFqName));
    }

    @Override
    @NotNull
    public JsNode visitCallExpression(
            @NotNull KtCallExpression expression,
            @NotNull TranslationContext context
    ) {
        return CallExpressionTranslator.translate(expression, null, context).source(expression);
    }

    @Override
    @NotNull
    public JsNode visitIfExpression(@NotNull KtIfExpression expression, @NotNull TranslationContext context) {
        assert expression.getCondition() != null : "condition should not ne null: " + expression.getText();
        JsExpression testExpression = Translation.translateAsExpression(expression.getCondition(), context);
        KotlinType type = context.bindingContext().getType(expression);

        boolean isKotlinExpression = BindingContextUtilsKt.isUsedAsExpression(expression, context.bindingContext());

        KtExpression thenExpression = expression.getThen();
        KtExpression elseExpression = expression.getElse();

        JsStatement thenStatement =
                thenExpression != null ? Translation.translateAsStatementAndMergeInBlockIfNeeded(thenExpression, context) : null;
        JsStatement elseStatement =
                elseExpression != null ? Translation.translateAsStatementAndMergeInBlockIfNeeded(elseExpression, context) : null;

        if (type != null) {
            if (thenStatement != null) {
                thenStatement = LastExpressionMutator.mutateLastExpression(thenStatement, new CoercionMutator(type, context));
            }
            if (elseStatement != null) {
                elseStatement = LastExpressionMutator.mutateLastExpression(elseStatement, new CoercionMutator(type, context));
            }
        }

        if (isKotlinExpression) {
            JsExpression jsThenExpression = JsAstUtils.extractExpressionFromStatement(thenStatement);
            JsExpression jsElseExpression = JsAstUtils.extractExpressionFromStatement(elseStatement);
            boolean canBeJsExpression = jsThenExpression != null && jsElseExpression != null;
            if (canBeJsExpression) {
                return new JsConditional(testExpression, jsThenExpression, jsElseExpression).source(expression);
            }
        }
        if (thenStatement == null) {
            thenStatement = JsEmpty.INSTANCE;
        }
        JsIf ifStatement = new JsIf(testExpression, thenStatement, elseStatement);
        return ifStatement.source(expression);
    }

    @Override
    @NotNull
    public JsExpression visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression,
            @NotNull TranslationContext context) {
        return ReferenceTranslator.translateSimpleName(expression, context).source(expression);
    }

    @Override
    @NotNull
    public JsNode visitWhileExpression(@NotNull KtWhileExpression expression, @NotNull TranslationContext context) {
        return LoopTranslator.createWhile(false, expression, context);
    }

    @Override
    @NotNull
    public JsNode visitDoWhileExpression(@NotNull KtDoWhileExpression expression, @NotNull TranslationContext context) {
        return LoopTranslator.createWhile(true, expression, context);
    }

    @Override
    @NotNull
    public JsNode visitStringTemplateExpression(@NotNull KtStringTemplateExpression expression, @NotNull TranslationContext context) {
        JsStringLiteral stringLiteral = resolveAsStringConstant(expression, context);
        if (stringLiteral != null) {
            return stringLiteral.source(expression);
        }
        return resolveAsTemplate(expression, context).source(expression);
    }

    @NotNull
    private static JsNode resolveAsTemplate(@NotNull KtStringTemplateExpression expression,
            @NotNull TranslationContext context) {
        return StringTemplateTranslator.translate(expression, context);
    }

    @Nullable
    private static JsStringLiteral resolveAsStringConstant(@NotNull KtExpression expression,
            @NotNull TranslationContext context) {
        Object konstue = getCompileTimeValue(context.bindingContext(), expression);
        if (konstue == null) {
            return null;
        }
        assert konstue instanceof String : "Compile time constant template should be a String constant.";
        String constantString = (String) konstue;
        return new JsStringLiteral(constantString);
    }

    @Override
    @NotNull
    public JsNode visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression, @NotNull TranslationContext context) {
        return QualifiedExpressionTranslator.translateQualifiedExpression(expression, context);
    }

    @Override
    public JsNode visitLabeledExpression(@NotNull KtLabeledExpression expression, @NotNull TranslationContext context) {
        KtExpression baseExpression = expression.getBaseExpression();
        assert baseExpression != null;

        if (BindingContextUtilsKt.isUsedAsExpression(expression, context.bindingContext())) {
            return Translation.translateAsExpression(baseExpression, context).source(expression);
        }

        JsScope scope = context.scope();
        assert scope instanceof JsFunctionScope: "Labeled statement is unexpected outside of function scope";
        JsFunctionScope functionScope = (JsFunctionScope) scope;

        String labelIdent = getReferencedName(expression.getTargetLabel());

        JsName labelName = functionScope.enterLabel(labelIdent, NameSuggestion.sanitizeName(labelIdent));
        JsStatement baseStatement = Translation.translateAsStatement(baseExpression, context);
        functionScope.exitLabel();

        return new JsLabel(labelName, baseStatement).source(expression);
    }

    @Override
    @NotNull
    public JsNode visitPrefixExpression(
            @NotNull KtPrefixExpression expression,
            @NotNull TranslationContext context
    ) {
        return UnaryOperationTranslator.translate(expression, context).source(expression);
    }

    @Override
    @NotNull
    public JsNode visitPostfixExpression(@NotNull KtPostfixExpression expression,
            @NotNull TranslationContext context) {
        return UnaryOperationTranslator.translate(expression, context).source(expression);
    }

    @Override
    @NotNull
    public JsNode visitIsExpression(@NotNull KtIsExpression expression,
            @NotNull TranslationContext context) {
        return Translation.patternTranslator(context).translateIsExpression(expression).source(expression);
    }

    @Override
    @NotNull
    public JsNode visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression,
            @NotNull TranslationContext context) {
        return QualifiedExpressionTranslator.translateQualifiedExpression(expression, context).source(expression);
    }

    @Override
    @Nullable
    public JsNode visitWhenExpression(@NotNull KtWhenExpression expression,
            @NotNull TranslationContext context) {
        return WhenTranslator.translate(expression, context);
    }

    @Override
    @NotNull
    public JsNode visitBinaryWithTypeRHSExpression(
            @NotNull KtBinaryExpressionWithTypeRHS expression,
            @NotNull TranslationContext context
    ) {
        JsExpression jsExpression;

        if (PatternTranslator.isCastExpression(expression)) {
            jsExpression = PatternTranslator.newInstance(context).translateCastExpression(expression);
        }
        else {
            jsExpression = Translation.translateAsExpression(expression.getLeft(), context);
        }

        return jsExpression.source(expression);
    }

    private static String getReferencedName(KtSimpleNameExpression expression) {
        return expression.getReferencedName()
                .replaceAll("^@", "")
                .replaceAll("(?:^`(.*)`$)", "$1");
    }

    private static JsNameRef getTargetLabel(KtExpressionWithLabel expression, TranslationContext context) {
        KtSimpleNameExpression labelElement = expression.getTargetLabel();
        if (labelElement == null) {
            return null;
        }

        String labelIdent = getReferencedName(labelElement);
        JsScope scope = context.scope();
        assert scope instanceof JsFunctionScope: "Labeled statement is unexpected outside of function scope";
        JsName labelName = ((JsFunctionScope) scope).findLabel(labelIdent);
        assert labelName != null;
        return labelName.makeRef();
    }

    @Override
    @NotNull
    public JsNode visitBreakExpression(@NotNull KtBreakExpression expression, @NotNull TranslationContext context) {
        return new JsBreak(getTargetLabel(expression, context)).source(expression);
    }

    @Override
    @NotNull
    public JsNode visitContinueExpression(@NotNull KtContinueExpression expression, @NotNull TranslationContext context) {
        return new JsContinue(getTargetLabel(expression, context)).source(expression);
    }

    @Override
    @NotNull
    public JsNode visitLambdaExpression(@NotNull KtLambdaExpression expression, @NotNull TranslationContext context) {
        return new LiteralFunctionTranslator(context).translate(expression.getFunctionLiteral()).source(expression);
    }

    @Override
    @NotNull
    public JsNode visitNamedFunction(@NotNull KtNamedFunction expression, @NotNull TranslationContext context) {
        JsExpression alias = new LiteralFunctionTranslator(context).translate(expression);

        FunctionDescriptor descriptor = getFunctionDescriptor(context.bindingContext(), expression);
        JsNameRef nameRef = (JsNameRef) ReferenceTranslator.translateAsValueReference(descriptor, context);
        assert nameRef.getName() != null;
        if (InlineUtil.isInline(descriptor)) {
            MetadataProperties.setStaticRef(nameRef.getName(), alias);
        }

        boolean isExpression = BindingContextUtilsKt.isUsedAsExpression(expression, context.bindingContext());
        JsNode result = isExpression ? alias : JsAstUtils.newVar(nameRef.getName(), alias);

        return result.source(expression);
    }

    @Override
    @NotNull
    public JsNode visitThisExpression(@NotNull KtThisExpression expression, @NotNull TranslationContext context) {
        DeclarationDescriptor thisExpression =
                getDescriptorForReferenceExpression(context.bindingContext(), expression.getInstanceReference());
        assert thisExpression != null : "This expression must reference a descriptor: " + expression.getText();

        return context.getDispatchReceiver(getReceiverParameterForDeclaration(thisExpression)).source(expression);
    }

    @Override
    @NotNull
    public JsNode visitArrayAccessExpression(@NotNull KtArrayAccessExpression expression,
            @NotNull TranslationContext context) {
        return AccessTranslationUtils.translateAsGet(expression, context);
    }

    @Override
    @NotNull
    public JsNode visitSuperExpression(@NotNull KtSuperExpression expression, @NotNull TranslationContext context) {
        ResolvedCall<? extends CallableDescriptor> resolvedCall = getResolvedCallWithAssert(expression, context.bindingContext());
        return context.getDispatchReceiver((ReceiverParameterDescriptor) resolvedCall.getResultingDescriptor());
    }

    @Override
    @NotNull
    public JsNode visitForExpression(@NotNull KtForExpression expression,
            @NotNull TranslationContext context) {
        return LoopTranslator.translateForExpression(expression, context).source(expression);
    }

    @Override
    @NotNull
    public JsNode visitTryExpression(
            @NotNull KtTryExpression expression,
            @NotNull TranslationContext context
    ) {
        return new TryTranslator(expression, context).translate();
    }

    @Override
    @NotNull
    public JsNode visitThrowExpression(@NotNull KtThrowExpression expression,
            @NotNull TranslationContext context) {
        KtExpression thrownExpression = expression.getThrownExpression();
        assert thrownExpression != null : "Thrown expression must not be null";
        return new JsThrow(translateAsExpression(thrownExpression, context)).source(expression);
    }

    @Override
    @NotNull
    public JsNode visitObjectLiteralExpression(@NotNull KtObjectLiteralExpression expression, @NotNull TranslationContext context) {
        ClassDescriptor descriptor = BindingUtils.getClassDescriptor(context.bindingContext(), expression.getObjectDeclaration());
        translateClassOrObject(expression.getObjectDeclaration(), descriptor, context);

        JsExpression constructor = ReferenceTranslator.translateAsTypeReference(descriptor, context);
        List<DeclarationDescriptor> closure = context.getClassOrConstructorClosure(descriptor);
        List<JsExpression> closureArgs = new ArrayList<>();
        if (closure != null) {
            for (DeclarationDescriptor capturedValue : closure) {
                closureArgs.add(context.getArgumentForClosureConstructor(capturedValue));
                if (capturedValue instanceof TypeParameterDescriptor) {
                    closureArgs.add(context.getTypeArgumentForClosureConstructor((TypeParameterDescriptor) capturedValue));
                }
            }
        }

        // In case of object expressions like this:
        //   object : SuperClass(A, B, ...)
        // we may capture local variables in expressions A, B, etc. We don't want to generate local fields for these variables.
        // Our ClassTranslator is capable of such thing, but case of object expression is a little special.
        // Consider the following:
        //
        //   class A(konst x: Int) {
        //      fun foo() { object : A(x) }
        //
        // By calling A(x) super constructor we capture `this` explicitly. However, we can't tell which `A::this` we are mentioning,
        // either `this` of an object literal or `this` of enclosing `class A`.
        // Frontend treats it as `this` of enclosing class declaration, therefore it expects backend to generate
        // super call in scope of `fun foo()` rather than define inner scope for object's constructor.
        // Thus we generate this call here rather than relying on ClassTranslator.
        ResolvedCall<FunctionDescriptor> superCall = BindingUtils.getSuperCall(context.bindingContext(),
                                                                               expression.getObjectDeclaration());
        if (superCall != null) {
            closureArgs.addAll(CallArgumentTranslator.translate(superCall, null, context).getTranslateArguments());
        }

        return new JsNew(constructor, closureArgs);
    }

    @Override
    public JsNode visitAnnotatedExpression(@NotNull KtAnnotatedExpression expression, TranslationContext context) {
        for (KtAnnotationEntry entry : expression.getAnnotationEntries()) {
            AnnotationDescriptor descriptor = context.bindingContext().get(BindingContext.ANNOTATION, entry);
            if (descriptor == null) continue;

            ClassifierDescriptor classifierDescriptor = getAnnotationClass(descriptor);
            if (classifierDescriptor == null) continue;

            KotlinRetention retention = DescriptorUtilsKt.getAnnotationRetention(classifierDescriptor);

            if (retention == KotlinRetention.SOURCE) {
                KtExpression baseExpression = expression.getBaseExpression();
                if (baseExpression == null) continue;

                return baseExpression.accept(this, context);
            }
        }

        return super.visitAnnotatedExpression(expression, context);
    }

    @Override
    public JsNode visitClass(@NotNull KtClass klass, TranslationContext context) {
        ClassDescriptor descriptor = BindingUtils.getClassDescriptor(context.bindingContext(), klass);
        translateClassOrObject(klass, descriptor, context);
        return JsEmpty.INSTANCE;
    }

    @Override
    public JsNode visitTypeAlias(@NotNull KtTypeAlias typeAlias, TranslationContext data) {
        // Resolved by front-end, not used by backend
        return JsEmpty.INSTANCE;
    }

    private static void translateClassOrObject(
            @NotNull KtClassOrObject declaration,
            @NotNull ClassDescriptor descriptor,
            @NotNull TranslationContext context
    ) {
        TranslationContext classContext = context.innerWithUsageTracker(descriptor);
        ClassTranslator.translate(declaration, classContext);
    }
}
