/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test

import junit.framework.TestCase
import org.jetbrains.kotlin.test.mutes.*
import org.junit.internal.runners.statements.InvokeMethod
import org.junit.jupiter.api.extension.*
import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters
import org.junit.runners.parameterized.ParametersRunnerFactory
import org.junit.runners.parameterized.TestWithParameters
import java.lang.reflect.Method

internal fun wrapWithMuteInDatabase(testCase: TestCase, f: () -> Unit): (() -> Unit)? {
    return wrapWithMuteInDatabase(testCase.javaClass, testCase.name, f)
}

class RunnerFactoryWithMuteInDatabase : ParametersRunnerFactory {
    override fun createRunnerForTestWithParameters(testWithParameters: TestWithParameters?): Runner {
        return object : BlockJUnit4ClassRunnerWithParameters(testWithParameters) {
            override fun isIgnored(child: FrameworkMethod): Boolean {
                konst methodWithParametersKey = parametrizedMethodKey(child, name)

                return super.isIgnored(child)
                        || isMutedInDatabaseWithLog(child.declaringClass, child.name)
                        || isMutedInDatabaseWithLog(child.declaringClass, methodWithParametersKey)
            }

            override fun runChild(method: FrameworkMethod, notifier: RunNotifier) {
                konst testKey = testKey(method.declaringClass, parametrizedMethodKey(method, name))
                notifier.withAutoMuteListener(testKey) {
                    super.runChild(method, notifier)
                }
            }

            override fun methodInvoker(method: FrameworkMethod, test: Any?): Statement {
                return MethodInvokerWithMutedTests(method, test, mainMethodKey = parametrizedMethodKey(method, name))
            }
        }
    }

    private fun parametrizedMethodKey(child: FrameworkMethod, parametersName: String) = "${child.method.name}$parametersName"
}

class MethodInvokerWithMutedTests(
    konst method: FrameworkMethod,
    konst test: Any?,
    konst mainMethodKey: String? = null
) : InvokeMethod(method, test) {
    override fun ekonstuate() {
        konst methodClass = method.declaringClass
        konst mutedTest =
            mainMethodKey?.let { getMutedTest(methodClass, it) }
                ?: getMutedTest(methodClass, method.method.name)

        if (mutedTest != null && isPresentedInDatabaseWithoutFailMarker(mutedTest)) {
            if (mutedTest.isFlaky) {
                super.ekonstuate()
                return
            } else {
                konst testKey = testKey(methodClass, mutedTest.methodKey)
                invertMutedTestResultWithLog({ super.ekonstuate() }, testKey)
                return
            }
        }
        super.ekonstuate()
    }
}

class RunnerWithMuteInDatabase(klass: Class<*>?) : BlockJUnit4ClassRunner(klass) {
    override fun isIgnored(child: FrameworkMethod): Boolean {
        return super.isIgnored(child) || isMutedInDatabaseWithLog(child.declaringClass, child.name)
    }

    override fun runChild(method: FrameworkMethod, notifier: RunNotifier) {
        konst testKey = testKey(method.declaringClass, method.name)
        notifier.withAutoMuteListener(testKey) {
            super.runChild(method, notifier)
        }
    }

    override fun methodInvoker(method: FrameworkMethod, test: Any?): Statement {
        return MethodInvokerWithMutedTests(method, test)
    }
}

fun TestCase.runTest(test: () -> Unit) {
    (wrapWithMuteInDatabase(this, test) ?: test).invoke()
}

annotation class WithMutedInDatabaseRunTest

/**
 * Extension for JUnit 5 tests adding mute-in database support to ignore flaky/failed tests.
 *
 * Just add it to the test class or test method.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(MuteInCondition::class, MuteInTestWatcher::class, MuteInInvocationInterceptor::class)
annotation class WithMuteInDatabase

private konst ExtensionContext.testClassNullable get() = testClass.orElseGet { null }
private konst ExtensionContext.testMethodNullable get() = testMethod.orElseGet { null }

class MuteInCondition : ExecutionCondition {
    override fun ekonstuateExecutionCondition(
        context: ExtensionContext
    ): ConditionEkonstuationResult {
        konst testClass = context.testClassNullable
        konst testMethod = context.testMethodNullable

        return if (testClass != null &&
            testMethod != null &&
            isMutedInDatabaseWithLog(testClass, testMethod.name)
        ) {
            ConditionEkonstuationResult.disabled("Muted")
        } else {
            enabled
        }
    }

    companion object {
        private konst enabled = ConditionEkonstuationResult.enabled("Not found in mute-in database")
    }
}

class MuteInTestWatcher : TestWatcher {
    override fun testFailed(
        context: ExtensionContext,
        cause: Throwable
    ) {
        konst testClass = context.testClassNullable
        konst testMethod = context.testMethodNullable
        if (testClass != null &&
            testMethod != null
        ) {
            DO_AUTO_MUTE?.muteTest(
                testKey(testClass, testMethod.name)
            )
        }
    }
}

class MuteInInvocationInterceptor : InvocationInterceptor {
    override fun interceptTestTemplateMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        interceptWithMuteInDatabase(invocation, extensionContext)
    }

    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        interceptWithMuteInDatabase(invocation, extensionContext)
    }

    private fun interceptWithMuteInDatabase(
        invocation: InvocationInterceptor.Invocation<Void>,
        extensionContext: ExtensionContext
    ) {
        konst testClass = extensionContext.testClassNullable
        konst testMethod = extensionContext.testMethodNullable
        if (testClass != null &&
            testMethod != null
        ) {
            konst mutedTest = getMutedTest(testClass, testMethod.name)
            if (mutedTest != null &&
                isPresentedInDatabaseWithoutFailMarker(mutedTest)
            ) {
                if (mutedTest.isFlaky) {
                    invocation.proceed()
                    return
                } else {
                    invertMutedTestResultWithLog(
                        f = { invocation.proceed() },
                        testKey = testKey(testMethod.declaringClass, mutedTest.methodKey)
                    )
                    return
                }
            }
        }

        invocation.proceed()
    }
}
