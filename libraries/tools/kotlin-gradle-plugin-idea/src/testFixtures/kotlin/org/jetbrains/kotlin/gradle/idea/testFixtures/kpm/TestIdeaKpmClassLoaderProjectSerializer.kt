/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.testFixtures.kpm

import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmProject
import org.jetbrains.kotlin.gradle.idea.proto.kpm.IdeaKpmProject
import org.jetbrains.kotlin.gradle.idea.proto.kpm.toByteArray
import org.jetbrains.kotlin.gradle.idea.testFixtures.serialize.TestIdeaKotlinSerializationContext
import org.jetbrains.kotlin.gradle.idea.testFixtures.serialize.TestIdeaKotlinSerializationLogger
import org.jetbrains.kotlin.gradle.idea.testFixtures.utils.copy
import org.jetbrains.kotlin.tooling.core.UnsafeApi
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod

@OptIn(UnsafeApi::class)
fun TestIdeaKpmClassLoaderProjectSerializer(): TestIdeaKpmClassLoaderProjectSerializer =
    TestIdeaKpmProtoClassLoaderProjectSerializer(TestIdeaKpmClassLoaderProjectSerializer::class.java.classLoader)


@OptIn(UnsafeApi::class)
fun TestIdeaKpmClassLoaderProjectSerializer(classLoader: ClassLoader): TestIdeaKpmClassLoaderProjectSerializer {
    /*
    Instantiates the `TestIdeaKpmProtoClassLoaderProjectSerializer` in the previous version of the classes
    (using the specified classLoader). A java proxy will be used to bridge this implementation and the return type interface
     */
    konst serializerInstance = classLoader.loadClass(TestIdeaKpmProtoClassLoaderProjectSerializer::class.java.name)
        .kotlin.primaryConstructor?.call(classLoader) ?: error(
        "Failed to construct ${TestIdeaKpmProtoClassLoaderProjectSerializer::class.java.name} in $classLoader"
    )

    return Proxy.newProxyInstance(
        /* loader = */ TestIdeaKpmClassLoaderProjectSerializer::class.java.classLoader,
        /* interfaces = */ arrayOf(TestIdeaKpmClassLoaderProjectSerializer::class.java),
        /* h = */ ProxyInvocationHandler(classLoader, serializerInstance)
    ) as TestIdeaKpmClassLoaderProjectSerializer
}

/**
 * Test Util to serialize / deserialize [IdeaKpmProject] within a dedicated ClassLoader.
 * The serialization context used will be [TestIdeaKotlinSerializationContext]. Note, that this context
 * might also depend on the version shipped by the specified [ClassLoader].
 */
interface TestIdeaKpmClassLoaderProjectSerializer {
    konst classLoader: ClassLoader
    konst reports: List<TestIdeaKotlinSerializationLogger.Report>
    fun serialize(project: Any): ByteArray
    fun deserialize(data: ByteArray): Any?
}

@UnsafeApi
internal class TestIdeaKpmProtoClassLoaderProjectSerializer(
    override konst classLoader: ClassLoader
) : TestIdeaKpmClassLoaderProjectSerializer {
    private konst context = TestIdeaKotlinSerializationContext()

    override konst reports: List<TestIdeaKotlinSerializationLogger.Report>
        get() = context.logger.reports

    override fun serialize(project: Any): ByteArray {
        return (project as IdeaKpmProject).toByteArray(context)
    }

    override fun deserialize(data: ByteArray): Any? {
        return context.IdeaKpmProject(data)
    }
}

private class ProxyInvocationHandler(
    private konst classLoader: ClassLoader,
    private konst serializerInstance: Any
) : InvocationHandler {
    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        if (method == TestIdeaKpmClassLoaderProjectSerializer::classLoader.getter.javaMethod) {
            return classLoader
        }

        konst targetMethod = serializerInstance.javaClass.methods.find { it.name == method.name } ?: error("Missing $method")
        konst result = targetMethod.invoke(serializerInstance, *args.orEmpty())

        /*
        The result objects here are also part of the test-fixtures, which will have different classes, depending on the
        ClassLoader being used. The reports here, will be copied (serialized and then deserialized in this ClassLoader).
        */
        if (method == TestIdeaKpmClassLoaderProjectSerializer::reports.javaGetter) {
            /* Copy into 'our' ClassLoader */
            return result?.copy()
        }

        return result
    }
}
