/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.test.generators.GenerateCompilerTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("compiler/testData/resolvedCalls")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class ResolvedCallsTestGenerated extends AbstractResolvedCallsTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
    }

    public void testAllFilesPresentInResolvedCalls() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls"), Pattern.compile("^(.+)\\.kt$"), null, true, "enhancedSignatures");
    }

    @TestMetadata("explicitReceiverIsDispatchReceiver.kt")
    public void testExplicitReceiverIsDispatchReceiver() throws Exception {
        runTest("compiler/testData/resolvedCalls/explicitReceiverIsDispatchReceiver.kt");
    }

    @TestMetadata("explicitReceiverIsExtensionReceiver.kt")
    public void testExplicitReceiverIsExtensionReceiver() throws Exception {
        runTest("compiler/testData/resolvedCalls/explicitReceiverIsExtensionReceiver.kt");
    }

    @TestMetadata("hasBothDispatchAndExtensionReceivers.kt")
    public void testHasBothDispatchAndExtensionReceivers() throws Exception {
        runTest("compiler/testData/resolvedCalls/hasBothDispatchAndExtensionReceivers.kt");
    }

    @TestMetadata("hasBothDispatchAndExtensionReceiversWithoutExplicitReceiver.kt")
    public void testHasBothDispatchAndExtensionReceiversWithoutExplicitReceiver() throws Exception {
        runTest("compiler/testData/resolvedCalls/hasBothDispatchAndExtensionReceiversWithoutExplicitReceiver.kt");
    }

    @TestMetadata("implicitReceiverIsDispatchReceiver.kt")
    public void testImplicitReceiverIsDispatchReceiver() throws Exception {
        runTest("compiler/testData/resolvedCalls/implicitReceiverIsDispatchReceiver.kt");
    }

    @TestMetadata("implicitReceiverIsExtensionReceiver.kt")
    public void testImplicitReceiverIsExtensionReceiver() throws Exception {
        runTest("compiler/testData/resolvedCalls/implicitReceiverIsExtensionReceiver.kt");
    }

    @TestMetadata("impliedThisNoExplicitReceiver.kt")
    public void testImpliedThisNoExplicitReceiver() throws Exception {
        runTest("compiler/testData/resolvedCalls/impliedThisNoExplicitReceiver.kt");
    }

    @TestMetadata("simpleCall.kt")
    public void testSimpleCall() throws Exception {
        runTest("compiler/testData/resolvedCalls/simpleCall.kt");
    }

    @TestMetadata("compiler/testData/resolvedCalls/arguments")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class Arguments extends AbstractResolvedCallsTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        public void testAllFilesPresentInArguments() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/arguments"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("compiler/testData/resolvedCalls/arguments/functionLiterals")
        @TestDataPath("$PROJECT_ROOT")
        @RunWith(JUnit3RunnerWithInners.class)
        public static class FunctionLiterals extends AbstractResolvedCallsTest {
            private void runTest(String testDataFilePath) throws Exception {
                KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
            }

            public void testAllFilesPresentInFunctionLiterals() throws Exception {
                KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/arguments/functionLiterals"), Pattern.compile("^(.+)\\.kt$"), null, true);
            }

            @TestMetadata("chainedLambdas.kt")
            public void testChainedLambdas() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/functionLiterals/chainedLambdas.kt");
            }

            @TestMetadata("notInferredLambdaReturnType.kt")
            public void testNotInferredLambdaReturnType() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/functionLiterals/notInferredLambdaReturnType.kt");
            }

            @TestMetadata("notInferredLambdaType.kt")
            public void testNotInferredLambdaType() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/functionLiterals/notInferredLambdaType.kt");
            }

            @TestMetadata("simpleGenericLambda.kt")
            public void testSimpleGenericLambda() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/functionLiterals/simpleGenericLambda.kt");
            }

            @TestMetadata("simpleLambda.kt")
            public void testSimpleLambda() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/functionLiterals/simpleLambda.kt");
            }

            @TestMetadata("unmappedLambda.kt")
            public void testUnmappedLambda() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/functionLiterals/unmappedLambda.kt");
            }
        }

        @TestMetadata("compiler/testData/resolvedCalls/arguments/genericCalls")
        @TestDataPath("$PROJECT_ROOT")
        @RunWith(JUnit3RunnerWithInners.class)
        public static class GenericCalls extends AbstractResolvedCallsTest {
            private void runTest(String testDataFilePath) throws Exception {
                KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
            }

            public void testAllFilesPresentInGenericCalls() throws Exception {
                KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/arguments/genericCalls"), Pattern.compile("^(.+)\\.kt$"), null, true);
            }

            @TestMetadata("inferredParameter.kt")
            public void testInferredParameter() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/genericCalls/inferredParameter.kt");
            }

            @TestMetadata("simpleGeneric.kt")
            public void testSimpleGeneric() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/genericCalls/simpleGeneric.kt");
            }

            @TestMetadata("uninferredParameter.kt")
            public void testUninferredParameter() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/genericCalls/uninferredParameter.kt");
            }

            @TestMetadata("uninferredParameterTypeMismatch.kt")
            public void testUninferredParameterTypeMismatch() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/genericCalls/uninferredParameterTypeMismatch.kt");
            }
        }

        @TestMetadata("compiler/testData/resolvedCalls/arguments/namedArguments")
        @TestDataPath("$PROJECT_ROOT")
        @RunWith(JUnit3RunnerWithInners.class)
        public static class NamedArguments extends AbstractResolvedCallsTest {
            private void runTest(String testDataFilePath) throws Exception {
                KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
            }

            public void testAllFilesPresentInNamedArguments() throws Exception {
                KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/arguments/namedArguments"), Pattern.compile("^(.+)\\.kt$"), null, true);
            }

            @TestMetadata("positionedAfterNamed.kt")
            public void testPositionedAfterNamed() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/namedArguments/positionedAfterNamed.kt");
            }

            @TestMetadata("shiftedArgsMatch.kt")
            public void testShiftedArgsMatch() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/namedArguments/shiftedArgsMatch.kt");
            }
        }

        @TestMetadata("compiler/testData/resolvedCalls/arguments/oneArgument")
        @TestDataPath("$PROJECT_ROOT")
        @RunWith(JUnit3RunnerWithInners.class)
        public static class OneArgument extends AbstractResolvedCallsTest {
            private void runTest(String testDataFilePath) throws Exception {
                KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
            }

            public void testAllFilesPresentInOneArgument() throws Exception {
                KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/arguments/oneArgument"), Pattern.compile("^(.+)\\.kt$"), null, true);
            }

            @TestMetadata("argumentHasNoType.kt")
            public void testArgumentHasNoType() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/oneArgument/argumentHasNoType.kt");
            }

            @TestMetadata("simpleMatch.kt")
            public void testSimpleMatch() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/oneArgument/simpleMatch.kt");
            }

            @TestMetadata("typeMismatch.kt")
            public void testTypeMismatch() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/oneArgument/typeMismatch.kt");
            }

            @TestMetadata("unmappedArgument.kt")
            public void testUnmappedArgument() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/oneArgument/unmappedArgument.kt");
            }
        }

        @TestMetadata("compiler/testData/resolvedCalls/arguments/realExamples")
        @TestDataPath("$PROJECT_ROOT")
        @RunWith(JUnit3RunnerWithInners.class)
        public static class RealExamples extends AbstractResolvedCallsTest {
            private void runTest(String testDataFilePath) throws Exception {
                KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
            }

            public void testAllFilesPresentInRealExamples() throws Exception {
                KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/arguments/realExamples"), Pattern.compile("^(.+)\\.kt$"), null, true);
            }

            @TestMetadata("emptyList.kt")
            public void testEmptyList() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/realExamples/emptyList.kt");
            }

            @TestMetadata("emptyMutableList.kt")
            public void testEmptyMutableList() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/realExamples/emptyMutableList.kt");
            }
        }

        @TestMetadata("compiler/testData/resolvedCalls/arguments/severalCandidates")
        @TestDataPath("$PROJECT_ROOT")
        @RunWith(JUnit3RunnerWithInners.class)
        public static class SeveralCandidates extends AbstractResolvedCallsTest {
            private void runTest(String testDataFilePath) throws Exception {
                KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
            }

            public void testAllFilesPresentInSeveralCandidates() throws Exception {
                KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/arguments/severalCandidates"), Pattern.compile("^(.+)\\.kt$"), null, true);
            }

            @TestMetadata("mostSpecific.kt")
            public void testMostSpecific() throws Exception {
                runTest("compiler/testData/resolvedCalls/arguments/severalCandidates/mostSpecific.kt");
            }
        }
    }

    @TestMetadata("compiler/testData/resolvedCalls/differentCallElements")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class DifferentCallElements extends AbstractResolvedCallsTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        public void testAllFilesPresentInDifferentCallElements() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/differentCallElements"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("annotationCall.kt")
        public void testAnnotationCall() throws Exception {
            runTest("compiler/testData/resolvedCalls/differentCallElements/annotationCall.kt");
        }

        @TestMetadata("delegatorToSuperCall.kt")
        public void testDelegatorToSuperCall() throws Exception {
            runTest("compiler/testData/resolvedCalls/differentCallElements/delegatorToSuperCall.kt");
        }

        @TestMetadata("simpleArrayAccess.kt")
        public void testSimpleArrayAccess() throws Exception {
            runTest("compiler/testData/resolvedCalls/differentCallElements/simpleArrayAccess.kt");
        }
    }

    @TestMetadata("compiler/testData/resolvedCalls/dynamic")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class Dynamic extends AbstractResolvedCallsTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        public void testAllFilesPresentInDynamic() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/dynamic"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("explicitReceiverIsDispatchReceiver.kt")
        public void testExplicitReceiverIsDispatchReceiver() throws Exception {
            runTest("compiler/testData/resolvedCalls/dynamic/explicitReceiverIsDispatchReceiver.kt");
        }

        @TestMetadata("explicitReceiverIsExtensionReceiver.kt")
        public void testExplicitReceiverIsExtensionReceiver() throws Exception {
            runTest("compiler/testData/resolvedCalls/dynamic/explicitReceiverIsExtensionReceiver.kt");
        }

        @TestMetadata("hasBothDispatchAndExtensionReceivers.kt")
        public void testHasBothDispatchAndExtensionReceivers() throws Exception {
            runTest("compiler/testData/resolvedCalls/dynamic/hasBothDispatchAndExtensionReceivers.kt");
        }

        @TestMetadata("hasBothDispatchAndExtensionReceiversWithoutExplicitReceiver.kt")
        public void testHasBothDispatchAndExtensionReceiversWithoutExplicitReceiver() throws Exception {
            runTest("compiler/testData/resolvedCalls/dynamic/hasBothDispatchAndExtensionReceiversWithoutExplicitReceiver.kt");
        }

        @TestMetadata("implicitReceiverIsDispatchReceiver.kt")
        public void testImplicitReceiverIsDispatchReceiver() throws Exception {
            runTest("compiler/testData/resolvedCalls/dynamic/implicitReceiverIsDispatchReceiver.kt");
        }

        @TestMetadata("implicitReceiverIsExtensionReceiver.kt")
        public void testImplicitReceiverIsExtensionReceiver() throws Exception {
            runTest("compiler/testData/resolvedCalls/dynamic/implicitReceiverIsExtensionReceiver.kt");
        }
    }

    @TestMetadata("compiler/testData/resolvedCalls/functionTypes")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class FunctionTypes extends AbstractResolvedCallsTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        public void testAllFilesPresentInFunctionTypes() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/functionTypes"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("invokeForExtensionFunctionType.kt")
        public void testInvokeForExtensionFunctionType() throws Exception {
            runTest("compiler/testData/resolvedCalls/functionTypes/invokeForExtensionFunctionType.kt");
        }

        @TestMetadata("invokeForFunctionType.kt")
        public void testInvokeForFunctionType() throws Exception {
            runTest("compiler/testData/resolvedCalls/functionTypes/invokeForFunctionType.kt");
        }

        @TestMetadata("konstOfExtensionFunctionType.kt")
        public void testValOfExtensionFunctionType() throws Exception {
            runTest("compiler/testData/resolvedCalls/functionTypes/konstOfExtensionFunctionType.kt");
        }

        @TestMetadata("konstOfExtensionFunctionTypeInvoke.kt")
        public void testValOfExtensionFunctionTypeInvoke() throws Exception {
            runTest("compiler/testData/resolvedCalls/functionTypes/konstOfExtensionFunctionTypeInvoke.kt");
        }

        @TestMetadata("konstOfFunctionType.kt")
        public void testValOfFunctionType() throws Exception {
            runTest("compiler/testData/resolvedCalls/functionTypes/konstOfFunctionType.kt");
        }

        @TestMetadata("konstOfFunctionTypeInvoke.kt")
        public void testValOfFunctionTypeInvoke() throws Exception {
            runTest("compiler/testData/resolvedCalls/functionTypes/konstOfFunctionTypeInvoke.kt");
        }
    }

    @TestMetadata("compiler/testData/resolvedCalls/invoke")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class Invoke extends AbstractResolvedCallsTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        public void testAllFilesPresentInInvoke() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/invoke"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("bothReceivers.kt")
        public void testBothReceivers() throws Exception {
            runTest("compiler/testData/resolvedCalls/invoke/bothReceivers.kt");
        }

        @TestMetadata("dispatchReceiverAsReceiverForInvoke.kt")
        public void testDispatchReceiverAsReceiverForInvoke() throws Exception {
            runTest("compiler/testData/resolvedCalls/invoke/dispatchReceiverAsReceiverForInvoke.kt");
        }

        @TestMetadata("extensionReceiverAsReceiverForInvoke.kt")
        public void testExtensionReceiverAsReceiverForInvoke() throws Exception {
            runTest("compiler/testData/resolvedCalls/invoke/extensionReceiverAsReceiverForInvoke.kt");
        }

        @TestMetadata("implicitReceiverForInvoke.kt")
        public void testImplicitReceiverForInvoke() throws Exception {
            runTest("compiler/testData/resolvedCalls/invoke/implicitReceiverForInvoke.kt");
        }

        @TestMetadata("invokeOnClassObject1.kt")
        public void testInvokeOnClassObject1() throws Exception {
            runTest("compiler/testData/resolvedCalls/invoke/invokeOnClassObject1.kt");
        }

        @TestMetadata("invokeOnClassObject2.kt")
        public void testInvokeOnClassObject2() throws Exception {
            runTest("compiler/testData/resolvedCalls/invoke/invokeOnClassObject2.kt");
        }

        @TestMetadata("invokeOnEnumEntry1.kt")
        public void testInvokeOnEnumEntry1() throws Exception {
            runTest("compiler/testData/resolvedCalls/invoke/invokeOnEnumEntry1.kt");
        }

        @TestMetadata("invokeOnEnumEntry2.kt")
        public void testInvokeOnEnumEntry2() throws Exception {
            runTest("compiler/testData/resolvedCalls/invoke/invokeOnEnumEntry2.kt");
        }

        @TestMetadata("invokeOnObject1.kt")
        public void testInvokeOnObject1() throws Exception {
            runTest("compiler/testData/resolvedCalls/invoke/invokeOnObject1.kt");
        }

        @TestMetadata("invokeOnObject2.kt")
        public void testInvokeOnObject2() throws Exception {
            runTest("compiler/testData/resolvedCalls/invoke/invokeOnObject2.kt");
        }
    }

    @TestMetadata("compiler/testData/resolvedCalls/objectsAndClassObjects")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class ObjectsAndClassObjects extends AbstractResolvedCallsTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        public void testAllFilesPresentInObjectsAndClassObjects() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/objectsAndClassObjects"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("classObject.kt")
        public void testClassObject() throws Exception {
            runTest("compiler/testData/resolvedCalls/objectsAndClassObjects/classObject.kt");
        }

        @TestMetadata("kt5308IntRangeConstant.kt")
        public void testKt5308IntRangeConstant() throws Exception {
            runTest("compiler/testData/resolvedCalls/objectsAndClassObjects/kt5308IntRangeConstant.kt");
        }

        @TestMetadata("object.kt")
        public void testObject() throws Exception {
            runTest("compiler/testData/resolvedCalls/objectsAndClassObjects/object.kt");
        }
    }

    @TestMetadata("compiler/testData/resolvedCalls/realExamples")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class RealExamples extends AbstractResolvedCallsTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        public void testAllFilesPresentInRealExamples() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/realExamples"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("stringPlusInBuilders.kt")
        public void testStringPlusInBuilders() throws Exception {
            runTest("compiler/testData/resolvedCalls/realExamples/stringPlusInBuilders.kt");
        }
    }

    @TestMetadata("compiler/testData/resolvedCalls/resolve")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class Resolve extends AbstractResolvedCallsTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        public void testAllFilesPresentInResolve() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/resolve"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("mostSpecificUninferredParam.kt")
        public void testMostSpecificUninferredParam() throws Exception {
            runTest("compiler/testData/resolvedCalls/resolve/mostSpecificUninferredParam.kt");
        }

        @TestMetadata("mostSpecificWithLambda.kt")
        public void testMostSpecificWithLambda() throws Exception {
            runTest("compiler/testData/resolvedCalls/resolve/mostSpecificWithLambda.kt");
        }
    }

    @TestMetadata("compiler/testData/resolvedCalls/secondaryConstructors")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class SecondaryConstructors extends AbstractResolvedCallsTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        public void testAllFilesPresentInSecondaryConstructors() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/secondaryConstructors"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("classWithGenerics.kt")
        public void testClassWithGenerics() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/classWithGenerics.kt");
        }

        @TestMetadata("classWithGenerics2.kt")
        public void testClassWithGenerics2() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/classWithGenerics2.kt");
        }

        @TestMetadata("classWithGenerics3.kt")
        public void testClassWithGenerics3() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/classWithGenerics3.kt");
        }

        @TestMetadata("explicitPrimaryArgs.kt")
        public void testExplicitPrimaryArgs() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/explicitPrimaryArgs.kt");
        }

        @TestMetadata("explicitPrimaryCallSecondary.kt")
        public void testExplicitPrimaryCallSecondary() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/explicitPrimaryCallSecondary.kt");
        }

        @TestMetadata("explicitPrimaryNoArgs.kt")
        public void testExplicitPrimaryNoArgs() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/explicitPrimaryNoArgs.kt");
        }

        @TestMetadata("implicitPrimary.kt")
        public void testImplicitPrimary() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/implicitPrimary.kt");
        }

        @TestMetadata("overload1.kt")
        public void testOverload1() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/overload1.kt");
        }

        @TestMetadata("overload2.kt")
        public void testOverload2() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/overload2.kt");
        }

        @TestMetadata("overload3.kt")
        public void testOverload3() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/overload3.kt");
        }

        @TestMetadata("overloadDefault.kt")
        public void testOverloadDefault() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/overloadDefault.kt");
        }

        @TestMetadata("overloadNamed.kt")
        public void testOverloadNamed() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/overloadNamed.kt");
        }

        @TestMetadata("simple.kt")
        public void testSimple() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/simple.kt");
        }

        @TestMetadata("varargs.kt")
        public void testVarargs() throws Exception {
            runTest("compiler/testData/resolvedCalls/secondaryConstructors/varargs.kt");
        }
    }

    @TestMetadata("compiler/testData/resolvedCalls/thisOrSuper")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class ThisOrSuper extends AbstractResolvedCallsTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        public void testAllFilesPresentInThisOrSuper() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/resolvedCalls/thisOrSuper"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("labeledSuper.kt")
        public void testLabeledSuper() throws Exception {
            runTest("compiler/testData/resolvedCalls/thisOrSuper/labeledSuper.kt");
        }

        @TestMetadata("labeledThis.kt")
        public void testLabeledThis() throws Exception {
            runTest("compiler/testData/resolvedCalls/thisOrSuper/labeledThis.kt");
        }

        @TestMetadata("simpleSuper.kt")
        public void testSimpleSuper() throws Exception {
            runTest("compiler/testData/resolvedCalls/thisOrSuper/simpleSuper.kt");
        }

        @TestMetadata("simpleThis.kt")
        public void testSimpleThis() throws Exception {
            runTest("compiler/testData/resolvedCalls/thisOrSuper/simpleThis.kt");
        }

        @TestMetadata("thisInExtensionFunction.kt")
        public void testThisInExtensionFunction() throws Exception {
            runTest("compiler/testData/resolvedCalls/thisOrSuper/thisInExtensionFunction.kt");
        }
    }
}
