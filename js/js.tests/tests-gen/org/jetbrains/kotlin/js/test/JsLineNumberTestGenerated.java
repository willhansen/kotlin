/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.GenerateJsTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("js/js.translator/testData/lineNumbers")
@TestDataPath("$PROJECT_ROOT")
public class JsLineNumberTestGenerated extends AbstractJsLineNumberTest {
    @Test
    public void testAllFilesPresentInLineNumbers() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("js/js.translator/testData/lineNumbers"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS, true);
    }

    @Test
    @TestMetadata("andAndWithSideEffect.kt")
    public void testAndAndWithSideEffect() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/andAndWithSideEffect.kt");
    }

    @Test
    @TestMetadata("backingField.kt")
    public void testBackingField() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/backingField.kt");
    }

    @Test
    @TestMetadata("catch.kt")
    public void testCatch() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/catch.kt");
    }

    @Test
    @TestMetadata("chainedCall.kt")
    public void testChainedCall() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/chainedCall.kt");
    }

    @Test
    @TestMetadata("classCapturingLocals.kt")
    public void testClassCapturingLocals() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/classCapturingLocals.kt");
    }

    @Test
    @TestMetadata("closure.kt")
    public void testClosure() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/closure.kt");
    }

    @Test
    @TestMetadata("complexExpressionAsDefaultArgument.kt")
    public void testComplexExpressionAsDefaultArgument() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/complexExpressionAsDefaultArgument.kt");
    }

    @Test
    @TestMetadata("conditionalDecomposed.kt")
    public void testConditionalDecomposed() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/conditionalDecomposed.kt");
    }

    @Test
    @TestMetadata("coroutine.kt")
    public void testCoroutine() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/coroutine.kt");
    }

    @Test
    @TestMetadata("coroutineNullAssertion.kt")
    public void testCoroutineNullAssertion() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/coroutineNullAssertion.kt");
    }

    @Test
    @TestMetadata("dataClass.kt")
    public void testDataClass() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/dataClass.kt");
    }

    @Test
    @TestMetadata("delegateMemberVal.kt")
    public void testDelegateMemberVal() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/delegateMemberVal.kt");
    }

    @Test
    @TestMetadata("delegatedProperty.kt")
    public void testDelegatedProperty() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/delegatedProperty.kt");
    }

    @Test
    @TestMetadata("delegation.kt")
    public void testDelegation() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/delegation.kt");
    }

    @Test
    @TestMetadata("destructuring.kt")
    public void testDestructuring() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/destructuring.kt");
    }

    @Test
    @TestMetadata("destructuringInline.kt")
    public void testDestructuringInline() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/destructuringInline.kt");
    }

    @Test
    @TestMetadata("doWhileWithComplexCondition.kt")
    public void testDoWhileWithComplexCondition() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/doWhileWithComplexCondition.kt");
    }

    @Test
    @TestMetadata("elvis.kt")
    public void testElvis() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/elvis.kt");
    }

    @Test
    @TestMetadata("enumCompanionObject.kt")
    public void testEnumCompanionObject() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/enumCompanionObject.kt");
    }

    @Test
    @TestMetadata("enumObject.kt")
    public void testEnumObject() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/enumObject.kt");
    }

    @Test
    @TestMetadata("expressionAsFunctionBody.kt")
    public void testExpressionAsFunctionBody() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/expressionAsFunctionBody.kt");
    }

    @Test
    @TestMetadata("for.kt")
    public void testFor() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/for.kt");
    }

    @Test
    @TestMetadata("increment.kt")
    public void testIncrement() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/increment.kt");
    }

    @Test
    @TestMetadata("inlineArguments.kt")
    public void testInlineArguments() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/inlineArguments.kt");
    }

    @Test
    @TestMetadata("inlineLocalVarsRef.kt")
    public void testInlineLocalVarsRef() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/inlineLocalVarsRef.kt");
    }

    @Test
    @TestMetadata("inlineReturn.kt")
    public void testInlineReturn() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/inlineReturn.kt");
    }

    @Test
    @TestMetadata("inlining.kt")
    public void testInlining() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/inlining.kt");
    }

    @Test
    @TestMetadata("inliningWithLambda.kt")
    public void testInliningWithLambda() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/inliningWithLambda.kt");
    }

    @Test
    @TestMetadata("innerClass.kt")
    public void testInnerClass() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/innerClass.kt");
    }

    @Test
    @TestMetadata("isOperator.kt")
    public void testIsOperator() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/isOperator.kt");
    }

    @Test
    @TestMetadata("jsCode.kt")
    public void testJsCode() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/jsCode.kt");
    }

    @Test
    @TestMetadata("lambdaWithClosure.kt")
    public void testLambdaWithClosure() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/lambdaWithClosure.kt");
    }

    @Test
    @TestMetadata("lastExpressionInInlineLambda.kt")
    public void testLastExpressionInInlineLambda() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/lastExpressionInInlineLambda.kt");
    }

    @Test
    @TestMetadata("literals.kt")
    public void testLiterals() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/literals.kt");
    }

    @Test
    @TestMetadata("longLiteral.kt")
    public void testLongLiteral() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/longLiteral.kt");
    }

    @Test
    @TestMetadata("memberFunWithDefaultParam.kt")
    public void testMemberFunWithDefaultParam() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/memberFunWithDefaultParam.kt");
    }

    @Test
    @TestMetadata("multipleReferences.kt")
    public void testMultipleReferences() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/multipleReferences.kt");
    }

    @Test
    @TestMetadata("objectInstanceFunction.kt")
    public void testObjectInstanceFunction() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/objectInstanceFunction.kt");
    }

    @Test
    @TestMetadata("optionalArgs.kt")
    public void testOptionalArgs() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/optionalArgs.kt");
    }

    @Test
    @TestMetadata("propertyWithoutInitializer.kt")
    public void testPropertyWithoutInitializer() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/propertyWithoutInitializer.kt");
    }

    @Test
    @TestMetadata("simple.kt")
    public void testSimple() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/simple.kt");
    }

    @Test
    @TestMetadata("stringLiteral.kt")
    public void testStringLiteral() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/stringLiteral.kt");
    }

    @Test
    @TestMetadata("syntheticCodeInConstructors.kt")
    public void testSyntheticCodeInConstructors() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/syntheticCodeInConstructors.kt");
    }

    @Test
    @TestMetadata("syntheticCodeInEnums.kt")
    public void testSyntheticCodeInEnums() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/syntheticCodeInEnums.kt");
    }

    @Test
    @TestMetadata("konstParameter.kt")
    public void testValParameter() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/konstParameter.kt");
    }

    @Test
    @TestMetadata("whenEntryWithMultipleConditions.kt")
    public void testWhenEntryWithMultipleConditions() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/whenEntryWithMultipleConditions.kt");
    }

    @Test
    @TestMetadata("whenEntryWithMultipleConditionsNonOptimized.kt")
    public void testWhenEntryWithMultipleConditionsNonOptimized() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/whenEntryWithMultipleConditionsNonOptimized.kt");
    }

    @Test
    @TestMetadata("whenIn.kt")
    public void testWhenIn() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/whenIn.kt");
    }

    @Test
    @TestMetadata("whenIs.kt")
    public void testWhenIs() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/whenIs.kt");
    }

    @Test
    @TestMetadata("whileWithComplexCondition.kt")
    public void testWhileWithComplexCondition() throws Exception {
        runTest("js/js.translator/testData/lineNumbers/whileWithComplexCondition.kt");
    }

    @Nested
    @TestMetadata("js/js.translator/testData/lineNumbers/inlineMultiModule")
    @TestDataPath("$PROJECT_ROOT")
    public class InlineMultiModule {
        @Test
        public void testAllFilesPresentInInlineMultiModule() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("js/js.translator/testData/lineNumbers/inlineMultiModule"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS, true);
        }

        @Test
        @TestMetadata("simple.kt")
        public void testSimple() throws Exception {
            runTest("js/js.translator/testData/lineNumbers/inlineMultiModule/simple.kt");
        }
    }
}
