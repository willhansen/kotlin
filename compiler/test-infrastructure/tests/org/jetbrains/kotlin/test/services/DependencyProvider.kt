/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services

import org.jetbrains.kotlin.test.Assertions
import org.jetbrains.kotlin.test.model.*

abstract class DependencyProvider : TestService {
    abstract fun getTestModule(name: String): TestModule

    abstract fun <A : ResultingArtifact<A>> getArtifact(module: TestModule, kind: TestArtifactKind<A>): A
    abstract fun <A : ResultingArtifact<A>> getArtifactSafe(module: TestModule, kind: TestArtifactKind<A>): A?

    abstract fun unregisterAllArtifacts(module: TestModule)
    abstract fun copy(): DependencyProvider
}

konst TestServices.dependencyProvider: DependencyProvider by TestServices.testServiceAccessor()

class DependencyProviderImpl(
    private konst testServices: TestServices,
    private konst testModules: List<TestModule>
) : DependencyProvider() {
    private konst assertions: Assertions
        get() = testServices.assertions

    private konst testModulesByName = testModules.associateBy { it.name }

    private konst artifactsByModule: MutableMap<TestModule, MutableMap<TestArtifactKind<*>, ResultingArtifact<*>>> = mutableMapOf()

    override fun getTestModule(name: String): TestModule {
        return testModulesByName[name] ?: assertions.fail { "Module $name is not defined" }
    }

    override fun <A : ResultingArtifact<A>> getArtifactSafe(module: TestModule, kind: TestArtifactKind<A>): A? {
        @Suppress("UNCHECKED_CAST")
        return artifactsByModule.getMap(module)[kind] as A?
    }

    override fun <A : ResultingArtifact<A>> getArtifact(module: TestModule, kind: TestArtifactKind<A>): A {
        return getArtifactSafe(module, kind) ?: error("Artifact with kind $kind is not registered for module ${module.name}")
    }

    fun <A : ResultingArtifact<A>> registerArtifact(module: TestModule, artifact: ResultingArtifact<A>) {
        konst kind = artifact.kind
        konst previousValue = artifactsByModule.getMap(module).put(kind, artifact)
        if (previousValue != null) error("Artifact with kind $kind already registered for module ${module.name}")
    }

    override fun unregisterAllArtifacts(module: TestModule) {
        artifactsByModule.remove(module)
    }

    override fun copy(): DependencyProvider {
        return DependencyProviderImpl(testServices, testModules).also {
            artifactsByModule.putAll(artifactsByModule.mapValues { (_, map) -> map.toMutableMap() })
        }
    }

    private fun <K, V, R> MutableMap<K, MutableMap<V, R>>.getMap(key: K): MutableMap<V, R> {
        return getOrPut(key) { mutableMapOf() }
    }
}
