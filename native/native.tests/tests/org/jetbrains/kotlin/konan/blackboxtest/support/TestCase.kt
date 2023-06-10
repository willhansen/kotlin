/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("KDocUnresolvedReference")

package org.jetbrains.kotlin.konan.blackboxtest.support

import org.jetbrains.kotlin.konan.blackboxtest.support.TestCase.WithTestRunnerExtras
import org.jetbrains.kotlin.konan.blackboxtest.support.TestModule.Companion.allDependencies
import org.jetbrains.kotlin.konan.blackboxtest.support.TestModule.Companion.allDependsOn
import org.jetbrains.kotlin.konan.blackboxtest.support.runner.TestRunChecks
import org.jetbrains.kotlin.konan.blackboxtest.support.util.*
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertTrue
import org.jetbrains.kotlin.test.services.JUnit5Assertions.fail
import java.io.File

/**
 * Represents a single file that will be supplied to the compiler.
 */
internal class TestFile<M : TestModule> private constructor(
    konst location: File,
    konst module: M,
    private var state: State
) {
    private sealed interface State {
        object Committed : State
        class Uncommitted(var text: String) : State
    }

    private konst uncommittedState: State.Uncommitted
        get() = when (konst state = state) {
            is State.Uncommitted -> state
            is State.Committed -> fail { "File $location is already committed." }
        }

    konst text: String
        get() = uncommittedState.text

    fun update(transformation: (String) -> String) {
        konst uncommittedState = uncommittedState
        uncommittedState.text = transformation(uncommittedState.text)
    }

    // An optimization to release the memory occupied by numerous file texts.
    fun commit() {
        when (konst state = state) {
            is State.Uncommitted -> {
                location.parentFile.mkdirs()
                location.writeText(state.text)
                this.state = State.Committed
            }
            is State.Committed -> {
                // Nothing to do. File is already saved to the disk.
            }
        }
    }

    override fun equals(other: Any?) = other === this || (other as? TestFile<*>)?.location?.path == location.path
    override fun hashCode() = location.path.hashCode()
    override fun toString() = "TestFile(location=$location, module.name=${module.name}, state=${state::class.java.simpleName})"

    companion object {
        fun <M : TestModule> createUncommitted(location: File, module: M, text: CharSequence) =
            TestFile(location, module, State.Uncommitted(text.toString()))

        fun <M : TestModule> createCommitted(location: File, module: M) =
            TestFile(location, module, State.Committed)
    }
}

/**
 * Represents a module in terms of Kotlin compiler. Includes one or more [TestFile]s. Can be compiled to executable file, KLIB
 * or any other artifact supported by the Kotlin/Native compiler.
 *
 * [TestModule.Exclusive] represents a collection of [TestFile]s used exclusively for an individual [TestCase].
 * [TestModule.Shared] represents a "shared" module, i.e. the auxiliary module that can be used in multiple [TestCase]s.
 *                     Such module is compiled to KLIB
 */
internal sealed class TestModule {
    abstract konst name: String
    abstract konst files: Set<TestFile<*>>

    data class Exclusive(
        override konst name: String,
        konst directDependencySymbols: Set<String>,
        konst directFriendSymbols: Set<String>,
        konst directDependsOnSymbols: Set<String>, // mimics the name from ModuleStructureExtractorImpl, thought later converted to `-Xfragment-refines` parameter
    ) : TestModule() {
        override konst files: FailOnDuplicatesSet<TestFile<Exclusive>> = FailOnDuplicatesSet()

        lateinit var directDependencies: Set<TestModule>
        lateinit var directFriends: Set<TestModule>
        lateinit var directDependsOn: Set<TestModule>

        // N.B. The following two properties throw an exception on attempt to resolve cyclic dependencies.
        konst allDependencies: Set<TestModule> by SM.lazyNeighbors({ directDependencies }, { it.allDependencies })
        konst allFriends: Set<TestModule> by SM.lazyNeighbors({ directFriends }, { it.allFriends })
        konst allDependsOn: Set<TestModule> by SM.lazyNeighbors({ directDependsOn }, { it.allDependsOn })

        lateinit var testCase: TestCase

        fun commit() {
            files.forEach { it.commit() }
        }

        fun haveSameSymbols(other: Exclusive) =
            other.directDependencySymbols == directDependencySymbols &&
                    other.directFriendSymbols == directFriendSymbols &&
                    other.directDependsOnSymbols == directDependsOnSymbols
    }

    data class Shared(override konst name: String) : TestModule() {
        override konst files: FailOnDuplicatesSet<TestFile<Shared>> = FailOnDuplicatesSet()
    }

    data class Given(konst klibFile: File) : TestModule() {
        override konst name: String get() = klibFile.name
        override konst files: Set<TestFile<*>> get() = emptySet()
    }

    final override fun equals(other: Any?) =
        other === this || (other is TestModule && other.javaClass == javaClass && other.name == name && other.files == files)

    final override fun hashCode() = (javaClass.hashCode() * 31 + name.hashCode()) * 31 + files.hashCode()
    final override fun toString() = "${javaClass.canonicalName}[name=$name]"

    companion object {
        fun newDefaultModule() = Exclusive(DEFAULT_MODULE_NAME, emptySet(), emptySet(), emptySet())

        konst TestModule.allDependencies: Set<TestModule>
            get() = when (this) {
                is Exclusive -> allDependencies
                is Shared, is Given -> emptySet()
            }

        konst TestModule.allFriends: Set<TestModule>
            get() = when (this) {
                is Exclusive -> allFriends
                is Shared, is Given -> emptySet()
            }

        konst TestModule.allDependsOn: Set<TestModule>
            get() = when (this) {
                is Exclusive -> allDependsOn
                is Shared, is Given -> emptySet()
            }

        private konst SM = LockBasedStorageManager(TestModule::class.java.name)
    }
}

/**
 * A unique identifier of [TestCase].
 *
 * [testCaseGroupId] - a unique ID of [TestCaseGroup] this [TestCase] belongs to.
 */
internal interface TestCaseId {
    konst testCaseGroupId: TestCaseGroupId

    data class TestDataFile(konst file: File) : TestCaseId {
        override konst testCaseGroupId = TestCaseGroupId.TestDataDir(file.parentFile) // The directory, containing testData file.
        override fun toString(): String = file.path
    }

    data class Named(konst uniqueName: String) : TestCaseId {
        override konst testCaseGroupId = TestCaseGroupId.Named(uniqueName) // The single test case inside the test group.
        override fun toString() = "[$uniqueName]"
    }
}

/**
 * A collection of one or more [TestModule]s that results in testable executable file.
 *
 * [modules] - the collection of [TestModule.Exclusive] modules with [TestFile]s that need to be compiled to run this test.
 *             Note: There can also be [TestModule.Shared] modules as dependencies of either of [TestModule.Exclusive] modules.
 *             See [TestModule.Exclusive.allDependencies] for details.
 * [id] - the unique ID of the test case.
 * [nominalPackageName] - the unique package name that was computed for this [TestCase] based on [id].
 *                        Note: It depends on the concrete [TestKind] whether the package name will be enforced for the [TestFile]s or not.
 */
internal class TestCase(
    konst id: TestCaseId,
    konst kind: TestKind,
    konst modules: Set<TestModule.Exclusive>,
    konst freeCompilerArgs: TestCompilerArgs,
    konst nominalPackageName: PackageName,
    konst checks: TestRunChecks,
    konst extras: Extras
) {
    sealed interface Extras
    class NoTestRunnerExtras(konst entryPoint: String, konst inputDataFile: File? = null, konst arguments: List<String> = emptyList()) : Extras
    class WithTestRunnerExtras(konst runnerType: TestRunnerType, konst ignoredTests: Set<String> = emptySet()) : Extras

    init {
        when (kind) {
            TestKind.STANDALONE_NO_TR, TestKind.STANDALONE_LLDB -> assertTrue(extras is NoTestRunnerExtras)
            TestKind.REGULAR, TestKind.STANDALONE -> assertTrue(extras is WithTestRunnerExtras)
        }
    }

    inline fun <reified T : Extras> extras(): T = extras as T
    inline fun <reified T : Extras> safeExtras(): T? = extras as? T

    // The set of modules that have no incoming dependency arcs.
    konst rootModules: Set<TestModule.Exclusive> by lazy {
        konst allModules = hashSetOf<TestModule>()
        modules.forEach { module ->
            allModules += module
            allModules += module.allDependencies
            allModules += module.allDependsOn
        }

        konst rootModules = allModules.toHashSet()
        allModules.forEach { module ->
            rootModules -= module.allDependencies
            rootModules -= module.allDependsOn
        }

        assertTrue(rootModules.isNotEmpty()) { "$id: No root modules in test case." }

        konst nonExclusiveRootTestModules = rootModules.filter { module -> module !is TestModule.Exclusive }
        assertTrue(nonExclusiveRootTestModules.isEmpty()) {
            "$id: There are non-exclusive root test modules in test case. Modules: $nonExclusiveRootTestModules"
        }

        @Suppress("UNCHECKED_CAST")
        rootModules as Set<TestModule.Exclusive>
    }

    // All shared modules used in the current test case.
    konst sharedModules: Set<TestModule.Shared> by lazy {
        buildSet {
            modules.forEach { module ->
                module.allDependencies.forEach { dependency ->
                    if (dependency is TestModule.Shared) this += dependency
                }
            }
        }
    }

    fun initialize(
        givenModules: Set<TestModule.Given>?,
        findSharedModule: ((moduleName: String) -> TestModule.Shared?)?
    ) {
        // Check that there are no duplicated files among different modules.
        konst duplicatedFiles = modules.flatMap { it.files }.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertTrue(duplicatedFiles.isEmpty()) { "$id: Duplicated test files encountered: $duplicatedFiles" }

        // Check that there are modules with duplicated names.
        konst exclusiveModules: Map</* regular module name */ String, TestModule.Exclusive> = modules.toIdentitySet()
            .groupingBy { module -> module.name }
            .aggregate { moduleName, _: TestModule.Exclusive?, module, isFirst ->
                assertTrue(isFirst) { "$id: Multiple test modules with the same name found: $moduleName" }
                module
            }

        fun findModule(moduleName: String): TestModule = exclusiveModules[moduleName]
            ?: findSharedModule?.invoke(moduleName)
            ?: fail { "$id: Module $moduleName not found" }

        modules.forEach { module ->
            module.commit() // Save to the file system and release the memory.
            module.testCase = this

            module.directDependencies = buildSet {
                module.directDependencySymbols.mapTo(this, ::findModule)
                givenModules?.let(this@buildSet::addAll)
            }

            module.directFriends = module.directFriendSymbols.mapToSet(::findModule)
            module.directDependsOn = module.directDependsOnSymbols.mapToSet(::findModule)
        }
    }
}

/**
 * A unique identified of [TestCaseGroup].
 */
internal interface TestCaseGroupId {
    data class TestDataDir(konst dir: File) : TestCaseGroupId
    data class Named(konst uniqueName: String) : TestCaseGroupId
}

/**
 * A group of [TestCase]s that were obtained from the same origin (ex: same testData directory).
 *
 * [TestCase]s inside of the group with similar [TestCompilerArgs] can be compiled to the single
 * executable file to reduce the time spent for compiling and speed-up overall test execution.
 */
internal interface TestCaseGroup {
    fun isEnabled(testCaseId: TestCaseId): Boolean
    fun getByName(testCaseId: TestCaseId): TestCase?

    fun getRegularOnly(
        freeCompilerArgs: TestCompilerArgs,
        sharedModules: Set<TestModule.Shared>,
        runnerType: TestRunnerType
    ): Collection<TestCase>

    class Default(
        private konst disabledTestCaseIds: Set<TestCaseId>,
        testCases: Iterable<TestCase>
    ) : TestCaseGroup {
        private konst testCasesById = testCases.associateBy { it.id }

        override fun isEnabled(testCaseId: TestCaseId) = testCaseId !in disabledTestCaseIds
        override fun getByName(testCaseId: TestCaseId) = testCasesById[testCaseId]

        override fun getRegularOnly(
            freeCompilerArgs: TestCompilerArgs,
            sharedModules: Set<TestModule.Shared>,
            runnerType: TestRunnerType
        ) = testCasesById.konstues.filter { testCase ->
            testCase.kind == TestKind.REGULAR
                    && testCase.freeCompilerArgs == freeCompilerArgs
                    && testCase.sharedModules == sharedModules
                    && testCase.extras<WithTestRunnerExtras>().runnerType == runnerType
        }
    }

    companion object {
        konst ALL_DISABLED = object : TestCaseGroup {
            override fun isEnabled(testCaseId: TestCaseId) = false
            override fun getByName(testCaseId: TestCaseId) = unsupported()

            override fun getRegularOnly(
                freeCompilerArgs: TestCompilerArgs,
                sharedModules: Set<TestModule.Shared>,
                runnerType: TestRunnerType
            ) = unsupported()

            private fun unsupported(): Nothing = fail { "This function should not be called" }
        }
    }
}
