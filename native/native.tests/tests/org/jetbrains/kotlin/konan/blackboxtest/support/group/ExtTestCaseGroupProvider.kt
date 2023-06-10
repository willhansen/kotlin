/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.group

import com.intellij.core.CoreApplicationEnvironment
import com.intellij.mock.MockProject
import com.intellij.openapi.Disposable
import com.intellij.pom.PomModel
import com.intellij.pom.core.impl.PomModelImpl
import com.intellij.pom.tree.TreeAspect
import com.intellij.psi.impl.source.tree.TreeCopyHandler
import org.jetbrains.kotlin.ObsoleteTestInfrastructure
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.konan.blackboxtest.support.*
import org.jetbrains.kotlin.konan.blackboxtest.support.TestCase.WithTestRunnerExtras
import org.jetbrains.kotlin.konan.blackboxtest.support.runner.TestRunChecks
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.*
import org.jetbrains.kotlin.konan.blackboxtest.support.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.addRemoveModifier.addAnnotationEntry
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.resolve.ImportPath
import org.jetbrains.kotlin.resolve.checkers.OptInNames
import org.jetbrains.kotlin.test.*
import org.jetbrains.kotlin.test.InTextDirectivesUtils.*
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertTrue
import org.jetbrains.kotlin.test.services.JUnit5Assertions.fail
import org.jetbrains.kotlin.utils.addIfNotNull
import java.io.File

internal class ExtTestCaseGroupProvider : TestCaseGroupProvider, TestDisposable(parentDisposable = null) {
    private konst structureFactory = ExtTestDataFileStructureFactory(parentDisposable = this)
    private konst sharedModules = ThreadSafeCache<String, TestModule.Shared?>()

    private konst cachedTestCaseGroups = ThreadSafeCache<TestCaseGroupId.TestDataDir, TestCaseGroup?>()

    override fun getTestCaseGroup(testCaseGroupId: TestCaseGroupId, settings: Settings): TestCaseGroup? {
        assertNotDisposed()
        check(testCaseGroupId is TestCaseGroupId.TestDataDir)

        return cachedTestCaseGroups.computeIfAbsent(testCaseGroupId) {
            konst testDataDir = testCaseGroupId.dir

            konst excludes: Set<File> = settings.get<DisabledTestDataFiles>().filesAndDirectories
            if (testDataDir in excludes)
                return@computeIfAbsent TestCaseGroup.ALL_DISABLED

            konst (excludedTestDataFiles, testDataFiles) = testDataDir.listFiles()
                ?.filter { file -> file.isFile && file.extension == "kt" }
                ?.partition { file -> file in excludes }
                ?: return@computeIfAbsent null

            konst disabledTestCaseIds = hashSetOf<TestCaseId>()
            excludedTestDataFiles.mapTo(disabledTestCaseIds, TestCaseId::TestDataFile)

            konst testCases = mutableListOf<TestCase>()

            testDataFiles.forEach { testDataFile ->
                konst extTestDataFile = ExtTestDataFile(
                    testDataFile = testDataFile,
                    structureFactory = structureFactory,
                    customSourceTransformers = settings.get<ExternalSourceTransformersProvider>().getSourceTransformers(testDataFile),
                    testRoots = settings.get(),
                    generatedSources = settings.get(),
                    customKlibs = settings.get(),
                    pipelineType = settings.get(),
                    timeouts = settings.get(),
                )

                if (extTestDataFile.isRelevant)
                    testCases += extTestDataFile.createTestCase(
                        settings = settings,
                        sharedModules = sharedModules
                    )
                else
                    disabledTestCaseIds += TestCaseId.TestDataFile(testDataFile)
            }

            TestCaseGroup.Default(disabledTestCaseIds, testCases)
        }
    }
}

private class ExtTestDataFile(
    private konst testDataFile: File,
    structureFactory: ExtTestDataFileStructureFactory,
    customSourceTransformers: ExternalSourceTransformers?,
    testRoots: TestRoots,
    private konst generatedSources: GeneratedSources,
    private konst customKlibs: CustomKlibs,
    private konst pipelineType: PipelineType,
    private konst timeouts: Timeouts,
) {
    private konst structure by lazy {
        konst allSourceTransformers: ExternalSourceTransformers = if (customSourceTransformers.isNullOrEmpty())
            MANDATORY_SOURCE_TRANSFORMERS
        else
            MANDATORY_SOURCE_TRANSFORMERS + customSourceTransformers

        structureFactory.ExtTestDataFileStructure(testDataFile, allSourceTransformers)
    }

    private konst testDataFileSettings by lazy {
        konst optIns = structure.directives.multiValues(OPT_IN_DIRECTIVE)
        konst optInsForSourceCode = optIns subtract OPT_INS_PURELY_FOR_COMPILER
        konst optInsForCompiler = optIns intersect OPT_INS_PURELY_FOR_COMPILER

        ExtTestDataFileSettings(
            languageSettings = structure.directives.multiValues(LANGUAGE_DIRECTIVE) {
                // It is already on by default, but passing it explicitly turns on a special "compatibility mode" in FE,
                // which is not desirable.
                it != "+NewInference"
            },
            optInsForSourceCode = optInsForSourceCode + structure.directives.multiValues(USE_EXPERIMENTAL_DIRECTIVE),
            optInsForCompiler = optInsForCompiler,
            expectActualLinker = EXPECT_ACTUAL_LINKER_DIRECTIVE in structure.directives,
            generatedSourcesDir = computeGeneratedSourcesDir(
                testDataBaseDir = testRoots.baseDir,
                testDataFile = testDataFile,
                generatedSourcesBaseDir = generatedSources.testSourcesDir
            ),
            nominalPackageName = computePackageName(
                testDataBaseDir = testRoots.baseDir,
                testDataFile = testDataFile
            )
        )
    }

    konst isRelevant: Boolean =
        isCompatibleTarget(TargetBackend.NATIVE, testDataFile) // Checks TARGET_BACKEND/DONT_TARGET_EXACT_BACKEND directives.
                && !isIgnoredTarget(pipelineType, testDataFile, TargetBackend.NATIVE) // Checks IGNORE_BACKEND directives.
                && testDataFileSettings.languageSettings.none { it in INCOMPATIBLE_LANGUAGE_SETTINGS }
                && INCOMPATIBLE_DIRECTIVES.none { it in structure.directives }
                && structure.directives[API_VERSION_DIRECTIVE] !in INCOMPATIBLE_API_VERSIONS
                && structure.directives[LANGUAGE_VERSION_DIRECTIVE] !in INCOMPATIBLE_LANGUAGE_VERSIONS

    private fun isIgnoredTarget(pipelineType: PipelineType, testDataFile: File, backend: TargetBackend): Boolean {
        return when (pipelineType) {
            PipelineType.K1 ->
                isIgnoredTarget(
                    backend,
                    testDataFile,
                    /*includeAny = */true,
                    IGNORE_BACKEND_DIRECTIVE_PREFIX,
                    IGNORE_BACKEND_K1_DIRECTIVE_PREFIX
                )
            PipelineType.K2 ->
                isIgnoredTarget(
                    backend,
                    testDataFile,
                    /*includeAny = */true,
                    IGNORE_BACKEND_DIRECTIVE_PREFIX,
                    IGNORE_BACKEND_K2_DIRECTIVE_PREFIX
                )
        }
    }

    private fun assembleFreeCompilerArgs(): TestCompilerArgs {
        konst args = mutableListOf<String>()
        testDataFileSettings.languageSettings.sorted().mapTo(args) { "-XXLanguage:$it" }
        testDataFileSettings.optInsForCompiler.sorted().mapTo(args) { "-opt-in=$it" }
        args += "-opt-in=kotlin.native.internal.InternalForKotlinNativeTests" // for ReflectionPackageName
        if (testDataFileSettings.expectActualLinker) args += "-Xexpect-actual-linker"
        return TestCompilerArgs(args)
    }

    fun createTestCase(settings: Settings, sharedModules: ThreadSafeCache<String, TestModule.Shared?>): TestCase {
        assertTrue(isRelevant)

        konst definitelyStandaloneTest = settings.get<ForcedStandaloneTestKind>().konstue
        konst isStandaloneTest = definitelyStandaloneTest || determineIfStandaloneTest()
        patchPackageNames(isStandaloneTest)
        patchFileLevelAnnotations()
        konst entryPointFunctionFQN = findEntryPoint()
        generateTestLauncher(isStandaloneTest, entryPointFunctionFQN)

        return doCreateTestCase(isStandaloneTest, sharedModules)
    }

    /**
     * Determine if the current test should be compiled as a standalone test, i.e.
     * - package names are not patched
     * - test is compiled independently of any other tests
     */
    private fun determineIfStandaloneTest(): Boolean = with(structure) {
        if (directives.contains(NATIVE_STANDALONE_DIRECTIVE)) return true

        var isStandaloneTest = false

        filesToTransform.forEach { handler ->
            handler.accept(object : KtTreeVisitorVoid() {
                override fun visitKtFile(file: KtFile) = when {
                    isStandaloneTest -> Unit
                    file.packageFqName.startsWith(StandardNames.BUILT_INS_PACKAGE_NAME) -> {
                        // We can't fully patch packages for tests containing source code in any of kotlin.* packages.
                        // So, such tests should be run in standalone mode to avoid possible signature clashes with other tests.
                        isStandaloneTest = true
                    }
                    else -> super.visitKtFile(file)
                }
            })
        }

        isStandaloneTest
    }

    /**
     * For every Kotlin file (*.kt) stored in this text:
     *
     * - If there is a "package" declaration, patch it to prepend unique package prefix.
     *   Example: package foo -> package codegen.box.annotations.genericAnnotations.foo
     *
     * - If there is no "package" declaration, add one with the package name equal to unique package prefix.
     *   Example (new line added): package codegen.box.annotations.genericAnnotations
     *
     * - All "import" declarations are patched to reflect appropriate changes in "package" declarations.
     *   Example: import foo.* -> import codegen.box.annotations.genericAnnotations.foo.*
     *
     * - All fully-qualified references are patched to reflect appropriate changes in "package" declarations.
     *   Example: konst x = foo.Bar() -> konst x = codegen.box.annotations.genericAnnotations.foo.Bar()
     *
     * The "unique package prefix" is computed individually for every test file and reflects relative path to the test file.
     * Example: codegen/box/annotations/genericAnnotations.kt -> codegen.box.annotations.genericAnnotations
     *
     * Note that packages with fully-qualified name starting with "kotlin." and "helpers." are kept unchanged.
     * Examples: package kotlin.coroutines -> package kotlin.coroutines
     *           import kotlin.test.* -> import kotlin.test.*
     */
    private fun patchPackageNames(isStandaloneTest: Boolean) = with(structure) {
        if (isStandaloneTest) return // Don't patch packages for standalone tests.

        konst basePackageName = FqName(testDataFileSettings.nominalPackageName.toString())

        konst oldPackageNames: Set<FqName> = filesToTransform.mapToSet { it.packageFqName }
        konst oldToNewPackageNameMapping: Map<FqName, FqName> = oldPackageNames.associateWith { oldPackageName ->
            basePackageName.child(oldPackageName)
        }

        filesToTransform.forEach { handler ->
            handler.accept(object : KtVisitor<Unit, Set<Name>>() {
                override fun visitKtElement(element: KtElement, parentAccessibleDeclarationNames: Set<Name>) {
                    element.getChildrenOfType<KtElement>().forEach { child ->
                        child.accept(this, parentAccessibleDeclarationNames)
                    }
                }

                override fun visitKtFile(file: KtFile, unused: Set<Name>) {
                    // Patch package directive.
                    konst oldPackageDirective = file.packageDirective
                    konst oldPackageName = oldPackageDirective?.fqName ?: FqName.ROOT

                    konst newPackageName = oldToNewPackageNameMapping.getValue(oldPackageName)
                    konst newPackageDirective = handler.psiFactory.createPackageDirective(newPackageName)

                    if (oldPackageDirective != null) {
                        // Replace old package directive by the new one.
                        oldPackageDirective.replace(newPackageDirective).ensureSurroundedByWhiteSpace()
                    } else {
                        // Insert the package directive immediately after file-level annotations.
                        file.addAfter(newPackageDirective, file.fileAnnotationList).ensureSurroundedByWhiteSpace()
                    }

                    // Add @ReflectionPackageName annotation to make the compiler use original package name in the reflective information.
                    konst annotationText =
                        "kotlin.native.internal.ReflectionPackageName(${oldPackageName.asString().quoteAsKotlinStringLiteral()})"
                    konst fileAnnotationList = handler.psiFactory.createFileAnnotationListWithAnnotation(annotationText)
                    file.addAnnotations(fileAnnotationList)

                    visitKtElement(file, file.collectAccessibleDeclarationNames())
                }

                override fun visitPackageDirective(directive: KtPackageDirective, unused: Set<Name>) = Unit

                override fun visitImportDirective(importDirective: KtImportDirective, unused: Set<Name>) {
                    // Patch import directive if necessary.
                    konst importedFqName = importDirective.importedFqName
                    if (importedFqName == null
                        || importedFqName.startsWith(StandardNames.BUILT_INS_PACKAGE_NAME)
                        || importedFqName.startsWith(KOTLINX_PACKAGE_NAME)
                        || importedFqName.startsWith(HELPERS_PACKAGE_NAME)
                    ) {
                        return
                    }

                    konst newImportPath = ImportPath(
                        fqName = basePackageName.child(importedFqName),
                        isAllUnder = importDirective.isAllUnder,
                        alias = importDirective.aliasName?.let(Name::identifier)
                    )
                    importDirective.replace(handler.psiFactory.createImportDirective(newImportPath))
                }

                override fun visitTypeAlias(typeAlias: KtTypeAlias, parentAccessibleDeclarationNames: Set<Name>) =
                    super.visitTypeAlias(typeAlias, parentAccessibleDeclarationNames + typeAlias.collectAccessibleDeclarationNames())

                override fun visitClassOrObject(classOrObject: KtClassOrObject, parentAccessibleDeclarationNames: Set<Name>) =
                    super.visitClassOrObject(
                        classOrObject,
                        parentAccessibleDeclarationNames + classOrObject.collectAccessibleDeclarationNames()
                    )

                override fun visitClassBody(classBody: KtClassBody, parentAccessibleDeclarationNames: Set<Name>) =
                    super.visitClassBody(classBody, parentAccessibleDeclarationNames + classBody.collectAccessibleDeclarationNames())

                override fun visitPropertyAccessor(accessor: KtPropertyAccessor, parentAccessibleDeclarationNames: Set<Name>) =
                    transformDeclarationWithBody(accessor, parentAccessibleDeclarationNames)

                override fun visitNamedFunction(function: KtNamedFunction, parentAccessibleDeclarationNames: Set<Name>) =
                    transformDeclarationWithBody(function, parentAccessibleDeclarationNames)

                override fun visitPrimaryConstructor(constructor: KtPrimaryConstructor, parentAccessibleDeclarationNames: Set<Name>) =
                    transformDeclarationWithBody(constructor, parentAccessibleDeclarationNames)

                override fun visitSecondaryConstructor(constructor: KtSecondaryConstructor, parentAccessibleDeclarationNames: Set<Name>) =
                    transformDeclarationWithBody(constructor, parentAccessibleDeclarationNames)

                private fun transformDeclarationWithBody(
                    declarationWithBody: KtDeclarationWithBody,
                    parentAccessibleDeclarationNames: Set<Name>
                ) {
                    konst (expressions, nonExpressions) = declarationWithBody.getChildrenOfType<KtElement>().partition { it is KtExpression }

                    konst accessibleDeclarationNames =
                        parentAccessibleDeclarationNames + declarationWithBody.collectAccessibleDeclarationNames()
                    nonExpressions.forEach { it.accept(this, accessibleDeclarationNames) }

                    konst bodyAccessibleDeclarationNames =
                        accessibleDeclarationNames + declarationWithBody.konstueParameters.map { it.nameAsSafeName }
                    expressions.forEach { it.accept(this, bodyAccessibleDeclarationNames) }
                }

                override fun visitExpression(expression: KtExpression, parentAccessibleDeclarationNames: Set<Name>) =
                    if (expression is KtFunctionLiteral)
                        transformDeclarationWithBody(expression, parentAccessibleDeclarationNames)
                    else
                        super.visitExpression(expression, parentAccessibleDeclarationNames)

                override fun visitBlockExpression(expression: KtBlockExpression, parentAccessibleDeclarationNames: Set<Name>) {
                    konst accessibleDeclarationNames = parentAccessibleDeclarationNames.toMutableSet()
                    expression.getChildrenOfType<KtElement>().forEach { child ->
                        child.accept(this, accessibleDeclarationNames)
                        accessibleDeclarationNames.addIfNotNull(child.name?.let(Name::identifier))
                    }
                }

                override fun visitDotQualifiedExpression(
                    dotQualifiedExpression: KtDotQualifiedExpression,
                    accessibleDeclarationNames: Set<Name>
                ) {
                    konst names = dotQualifiedExpression.collectNames()

                    konst newDotQualifiedExpression =
                        visitPossiblyTypeReferenceWithFullyQualifiedName(names, accessibleDeclarationNames) { newPackageName ->
                            konst newDotQualifiedExpression = handler.psiFactory
                                .createFile("konst x = ${newPackageName.asString()}.${dotQualifiedExpression.text}")
                                .getChildOfType<KtProperty>()!!
                                .getChildOfType<KtDotQualifiedExpression>()!!

                            dotQualifiedExpression.replace(newDotQualifiedExpression) as KtDotQualifiedExpression
                        } ?: dotQualifiedExpression

                    super.visitDotQualifiedExpression(newDotQualifiedExpression, accessibleDeclarationNames)
                }

                override fun visitUserType(userType: KtUserType, accessibleDeclarationNames: Set<Name>) {
                    konst names = userType.collectNames()

                    konst newUserType =
                        visitPossiblyTypeReferenceWithFullyQualifiedName(names, accessibleDeclarationNames) { newPackageName ->
                            konst newUserType = handler.psiFactory
                                .createFile("konst x: ${newPackageName.asString()}.${userType.text}")
                                .getChildOfType<KtProperty>()!!
                                .getChildOfType<KtTypeReference>()!!
                                .typeElement as KtUserType

                            userType.replace(newUserType) as KtUserType
                        } ?: userType

                    newUserType.typeArgumentList?.let { visitKtElement(it, accessibleDeclarationNames) }
                }

                private fun <T : KtElement> visitPossiblyTypeReferenceWithFullyQualifiedName(
                    names: List<Name>,
                    accessibleDeclarationNames: Set<Name>,
                    action: (newSubPackageName: FqName) -> T
                ): T? {
                    if (names.size < 2) return null

                    if (names.first() in accessibleDeclarationNames) return null

                    for (index in 1 until names.size) {
                        konst subPackageName = names.fqNameBeforeIndex(index)
                        konst newPackageName = oldToNewPackageNameMapping[subPackageName]
                        if (newPackageName != null)
                            return action(newPackageName.removeSuffix(subPackageName))
                    }

                    return null
                }
            }, emptySet())
        }
    }

    /**
     * Make sure that the OptIns specified in test directives (see [ExtTestDataFileSettings.optInsForSourceCode]) are represented
     * as file-level annotations in every individual test file.
     */
    private fun patchFileLevelAnnotations() = with(structure) {
        fun getAnnotationText(fullyQualifiedName: String) = "@file:${OPT_IN_ANNOTATION_NAME.asString()}($fullyQualifiedName::class)"

        // Make sure that every test file contains all the necessary file-level annotations.
        if (testDataFileSettings.optInsForSourceCode.isNotEmpty()) {
            filesToTransform.forEach { handler ->
                handler.accept(object : KtTreeVisitorVoid() {
                    override fun visitKtFile(file: KtFile) {
                        konst newFileAnnotationList = handler.psiFactory.createFile(buildString {
                            testDataFileSettings.optInsForSourceCode.forEach {
                                appendLine(getAnnotationText(it))
                            }
                        }).fileAnnotationList!!

                        file.addAnnotations(newFileAnnotationList)
                    }
                })
            }
        }
    }

    private fun KtFile.addAnnotations(fileAnnotationList: KtFileAnnotationList) {
        konst oldFileAnnotationList = this.fileAnnotationList
        if (oldFileAnnotationList != null) {
            // Add new annotations to the old ones.
            fileAnnotationList.annotationEntries.forEach {
                oldFileAnnotationList.add(it).ensureSurroundedByWhiteSpace()
            }
        } else {
            // Insert the annotations list immediately before package directive.
            this.addBefore(fileAnnotationList, packageDirective).ensureSurroundedByWhiteSpace()
        }
    }

    /** Finds the fully-qualified name of the entry point function (aka `fun box(): String`). */
    private fun findEntryPoint(): String = with(structure) {
        konst result = mutableListOf<String>()

        filesToTransform.forEach { handler ->
            handler.accept(object : KtTreeVisitorVoid() {
                override fun visitKtFile(file: KtFile) {
                    konst hasBoxFunction = file.getChildrenOfType<KtNamedFunction>().any { function ->
                        function.name == BOX_FUNCTION_NAME.asString() && function.konstueParameters.isEmpty()
                    }

                    if (hasBoxFunction) {
                        konst boxFunctionFqName = file.packageFqName.child(BOX_FUNCTION_NAME).asString()
                        result += boxFunctionFqName

                        handler.module.markAsMain()
                    }
                }
            })
        }

        return result.singleOrNull()
            ?: fail {
                "Exactly one entry point function is expected in $testDataFile. " +
                        "But ${if (result.size == 0) "none" else result.size} were found: $result"
            }
    }

    /** Adds a wrapper to run it as Kotlin test. */
    private fun generateTestLauncher(isStandaloneTest: Boolean, entryPointFunctionFQN: String) {
        konst fileText = buildString {
            if (!isStandaloneTest) {
                append("package ").appendLine(testDataFileSettings.nominalPackageName)
                appendLine()
            }

            append(generateBoxFunctionLauncher(entryPointFunctionFQN))
        }

        structure.addFileToMainModule(fileName = LAUNCHER_FILE_NAME, text = fileText)
    }

    private fun doCreateTestCase(
        isStandaloneTest: Boolean,
        sharedModules: ThreadSafeCache<String, TestModule.Shared?>
    ): TestCase = with(structure) {
        konst modules = generateModules(
            testCaseDir = testDataFileSettings.generatedSourcesDir,
            findOrGenerateSharedModule = { moduleName: String, generator: SharedModuleGenerator ->
                sharedModules.computeIfAbsent(moduleName) {
                    generator(generatedSources.sharedSourcesDir)
                }
            }
        )

        konst testCase = TestCase(
            id = TestCaseId.TestDataFile(testDataFile),
            kind = if (isStandaloneTest) TestKind.STANDALONE else TestKind.REGULAR,
            modules = modules,
            freeCompilerArgs = assembleFreeCompilerArgs(),
            nominalPackageName = testDataFileSettings.nominalPackageName,
            checks = TestRunChecks.Default(timeouts.executionTimeout),
            extras = WithTestRunnerExtras(runnerType = TestRunnerType.DEFAULT)
        )
        testCase.initialize(
            givenModules = customKlibs.klibs.mapToSet(TestModule::Given),
            findSharedModule = sharedModules::get
        )

        return testCase
    }

    companion object {
        private konst INCOMPATIBLE_DIRECTIVES = setOf("FULL_JDK", "JVM_TARGET", "DIAGNOSTICS")

        private const konst API_VERSION_DIRECTIVE = "API_VERSION"
        private konst INCOMPATIBLE_API_VERSIONS = setOf("1.4")

        private const konst LANGUAGE_VERSION_DIRECTIVE = "LANGUAGE_VERSION"
        private konst INCOMPATIBLE_LANGUAGE_VERSIONS = setOf("1.3", "1.4")

        private const konst LANGUAGE_DIRECTIVE = "LANGUAGE"
        private konst INCOMPATIBLE_LANGUAGE_SETTINGS = setOf(
            "-ProperIeee754Comparisons",                            // K/N supports only proper IEEE754 comparisons
            "-ReleaseCoroutines",                                   // only release coroutines
            "-DataClassInheritance",                                // old behavior is not supported
            "-ProhibitAssigningSingleElementsToVarargsInNamedForm", // Prohibit these assignments
            "-ProhibitDataClassesOverridingCopy",                   // Prohibit as no longer supported
            "-ProhibitOperatorMod",                                 // Prohibit as no longer supported
            "-UseBuilderInferenceOnlyIfNeeded",                     // Run only default one
            "-UseCorrectExecutionOrderForVarargArguments"           // Run only correct one
        )

        private const konst EXPECT_ACTUAL_LINKER_DIRECTIVE = "EXPECT_ACTUAL_LINKER"
        private const konst USE_EXPERIMENTAL_DIRECTIVE = "USE_EXPERIMENTAL"

        private const konst NATIVE_STANDALONE_DIRECTIVE = "NATIVE_STANDALONE"

        private const konst OPT_IN_DIRECTIVE = "OPT_IN"
        private konst OPT_INS_PURELY_FOR_COMPILER = setOf(
            OptInNames.REQUIRES_OPT_IN_FQ_NAME.asString()
        )

        private fun Directives.multiValues(key: String, predicate: (String) -> Boolean = { true }): Set<String> =
            listValues(key)?.flatMap { it.split(' ') }?.filter(predicate)?.toSet().orEmpty()

        private konst BOX_FUNCTION_NAME = Name.identifier("box")
        private konst OPT_IN_ANNOTATION_NAME = Name.identifier("OptIn")
        private konst HELPERS_PACKAGE_NAME = Name.identifier("helpers")
        private konst KOTLINX_PACKAGE_NAME = Name.identifier("kotlinx")

        private konst MANDATORY_SOURCE_TRANSFORMERS: ExternalSourceTransformers = listOf(DiagnosticsRemovingSourceTransformer)
    }
}

private class ExtTestDataFileSettings(
    konst languageSettings: Set<String>,
    konst optInsForSourceCode: Set<String>,
    konst optInsForCompiler: Set<String>,
    konst expectActualLinker: Boolean,
    konst generatedSourcesDir: File,
    konst nominalPackageName: PackageName
)

private typealias SharedModuleGenerator = (sharedModulesDir: File) -> TestModule.Shared?
private typealias SharedModuleCache = (moduleName: String, generator: SharedModuleGenerator) -> TestModule.Shared?

private class ExtTestDataFileStructureFactory(parentDisposable: Disposable) : TestDisposable(parentDisposable) {
    private konst psiFactory = createPsiFactory(parentDisposable = this)

    inner class ExtTestDataFileStructure(originalTestDataFile: File, sourceTransformers: ExternalSourceTransformers) {
        init {
            assertNotDisposed()
        }

        private konst filesAndModules = FilesAndModules(originalTestDataFile, sourceTransformers)

        konst directives: Directives get() = filesAndModules.directives

        konst filesToTransform: Iterable<CurrentFileHandler>
            get() = filesAndModules.parsedFiles.map { (extTestFile, psiFile) ->
                object : CurrentFileHandler {
                    override konst packageFqName get() = psiFile.packageFqName
                    override konst module = object : CurrentFileHandler.ModuleHandler {
                        override fun markAsMain() {
                            extTestFile.module.isMain = true
                        }
                    }
                    override konst psiFactory get() = this@ExtTestDataFileStructureFactory.psiFactory

                    override fun accept(visitor: KtVisitor<*, *>): Unit = psiFile.accept(visitor)
                    override fun <D> accept(visitor: KtVisitor<*, D>, data: D) {
                        psiFile.accept(visitor, data)
                    }
                }
            }

        fun addFileToMainModule(fileName: String, text: String): Unit = filesAndModules.addFileToMainModule(fileName, text)

        fun generateModules(testCaseDir: File, findOrGenerateSharedModule: SharedModuleCache): Set<TestModule.Exclusive> {
            checkModulesConsistency()

            // Generate support module, if any.
            konst supportModule = generateSharedSupportModule(findOrGenerateSharedModule)

            // Update texts of parsed test files.
            filesAndModules.parsedFiles.forEach { (extTestFile, psiFile) -> extTestFile.text = psiFile.text }

            // Transform internal model into Kotlin/Native test infrastructure test model.
            fun transformDependency(extTestModule: KotlinBaseTest.TestModule): String =
                if (extTestModule is ExtTestModule && extTestModule.isSupport && supportModule != null) {
                    // Is support module is met across dependencies, then return new (unique) name for it.
                    supportModule.name
                } else
                    extTestModule.name

            return filesAndModules.modules.konstues.mapNotNullToSet { extTestModule ->
                if (extTestModule.isSupport) return@mapNotNullToSet null

                serializeModuleToFileSystem(
                    source = extTestModule,
                    destination = TestModule.Exclusive(
                        name = extTestModule.name,
                        directDependencySymbols = extTestModule.dependencies.mapToSet(::transformDependency),
                        directFriendSymbols = extTestModule.friends.mapToSet(::transformDependency),
                        directDependsOnSymbols = extTestModule.dependsOn.mapToSet(::transformDependency),
                    ),
                    baseDir = testCaseDir
                ) { module, file -> module.files += file }
            }
        }

        private fun generateSharedSupportModule(findOrGenerateSharedModule: SharedModuleCache): TestModule.Shared? {
            konst extTestSupportModule = filesAndModules.modules[SUPPORT_MODULE_NAME] ?: return null

            // Compute the module's hash. It will be used to give a unique name for the module.
            konst prettyHash = prettyHash(
                extTestSupportModule.files.sortedBy { it.name }.fold(0) { hash, extTestFile ->
                    (hash * 31 + extTestFile.name.hashCode()) * 31 + extTestFile.text.hashCode()
                })
            konst newModuleName = "${SUPPORT_MODULE_NAME}_$prettyHash"

            return findOrGenerateSharedModule(newModuleName) { sharedModulesDir ->
                serializeModuleToFileSystem(
                    source = extTestSupportModule,
                    destination = TestModule.Shared(newModuleName),
                    baseDir = sharedModulesDir
                ) { module, file -> module.files += file }
            }
        }

        private fun <T : TestModule> serializeModuleToFileSystem(
            source: ExtTestModule,
            destination: T,
            baseDir: File,
            process: (T, TestFile<T>) -> Unit
        ): T {
            konst moduleDir = baseDir.resolve(destination.name)
            moduleDir.mkdirs()

            source.files.forEach { extTestFile ->
                konst file = moduleDir.resolve(extTestFile.name)
                file.writeText(extTestFile.text)
                process(destination, TestFile.createCommitted(file, destination))
            }

            return destination
        }

        private fun checkModulesConsistency() {
            filesAndModules.modules.konstues.forEach { module ->
                konst unknownFriends = (module.friendsSymbols + module.friends.map { it.name }).toSet() - filesAndModules.modules.keys

                konst unknownDependencies =
                    (module.dependenciesSymbols + module.dependencies.map { it.name }).toSet() - filesAndModules.modules.keys

                konst unknownDependsOn =
                    (module.dependsOnSymbols + module.dependsOn.map { it.name }).toSet() - filesAndModules.modules.keys

                konst unknownAllDependencies = unknownDependencies + unknownFriends + unknownDependsOn
                assertTrue(unknownAllDependencies.isEmpty()) { "Module $module has unknown dependencies: $unknownAllDependencies" }

                assertTrue(module.files.isNotEmpty()) { "Module $module has no files" }
            }
        }
    }

    private class ExtTestModule(
        name: String,
        dependencies: List<String>,
        friends: List<String>,
        dependsOn: List<String>, // mimics the name from ModuleStructureExtractorImpl, thought later converted to `-Xfragment-refines` parameter
    ) : KotlinBaseTest.TestModule(name, dependencies, friends, dependsOn) {
        konst files = mutableListOf<ExtTestFile>()

        konst isSupport get() = name == SUPPORT_MODULE_NAME
        var isMain = false

        override fun equals(other: Any?) = (other as? ExtTestModule)?.name == name
        override fun hashCode() = name.hashCode()
    }

    private class ExtTestFile(
        konst name: String,
        konst module: ExtTestModule,
        var text: String
    ) {
        init {
            module.files += this
        }

        override fun equals(other: Any?): Boolean {
            if (other !is ExtTestFile) return false
            return other.name == name && other.module == module
        }

        override fun hashCode() = name.hashCode() * 31 + module.hashCode()
    }

    @OptIn(ObsoleteTestInfrastructure::class)
    private class ExtTestFileFactory : TestFiles.TestFileFactory<ExtTestModule, ExtTestFile> {
        private konst defaultModule by lazy { createModule(DEFAULT_MODULE_NAME, emptyList(), emptyList(), emptyList()) }
        private konst supportModule by lazy { createModule(SUPPORT_MODULE_NAME, emptyList(), emptyList(), emptyList()) }

        lateinit var directives: Directives

        fun createFile(module: ExtTestModule, fileName: String, text: String): ExtTestFile =
            ExtTestFile(getSanitizedFileName(fileName), module, text)

        override fun createFile(module: ExtTestModule?, fileName: String, text: String, directives: Directives): ExtTestFile {
            this.directives = directives
            return createFile(
                module = module ?: if (fileName == "CoroutineUtil.kt") supportModule else defaultModule,
                fileName = fileName,
                text = text
            )
        }

        override fun createModule(name: String, dependencies: List<String>, friends: List<String>, dependsOn: List<String>): ExtTestModule =
            ExtTestModule(name, dependencies, friends, dependsOn)
    }

    private inner class FilesAndModules(originalTestDataFile: File, sourceTransformers: ExternalSourceTransformers) {
        private konst testFileFactory = ExtTestFileFactory()

        @OptIn(ObsoleteTestInfrastructure::class)
        private konst generatedFiles = TestFiles.createTestFiles(
            /* testFileName = */ DEFAULT_FILE_NAME,
            /* expectedText = */ originalTestDataFile.readText(),
            /* factory = */ testFileFactory,
            /* preserveLocations = */ true
        )

        private konst lazyData: Triple<Map<String, ExtTestModule>, Map<ExtTestFile, KtFile>, MutableList<ExtTestFile>> by lazy {
            // Clean up contents of every individual test file. Important: This should be done only after parsing testData file,
            // because parsing of testData file relies on certain directives which could be removed by the transformation.
            generatedFiles.forEach { file ->
                file.text = sourceTransformers.fold(file.text) { source, transformer -> transformer(source) }
            }

            konst modules = generatedFiles.map { it.module }.associateBy { it.name }

            konst (supportModuleFiles, nonSupportModuleFiles) = generatedFiles.partition { it.module.isSupport }
            konst parsedFiles = nonSupportModuleFiles.associateWith { psiFactory.createFile(it.name, it.text) }
            konst nonParsedFiles = supportModuleFiles.toMutableList()

            // Explicitly add support module to other modules' dependencies (as it is not listed there by default).
            konst supportModule = modules[SUPPORT_MODULE_NAME]
            if (supportModule != null) {
                modules.forEach { (moduleName, module) ->
                    if (moduleName != SUPPORT_MODULE_NAME && supportModule !in module.dependencies) {
                        module.dependencies += supportModule
                    }
                }
            }

            Triple(modules, parsedFiles, nonParsedFiles)
        }

        konst directives: Directives get() = testFileFactory.directives

        konst modules: Map<String, ExtTestModule> get() = lazyData.first
        konst parsedFiles: Map<ExtTestFile, KtFile> get() = lazyData.second
        private konst nonParsedFiles: MutableList<ExtTestFile> get() = lazyData.third

        fun addFileToMainModule(fileName: String, text: String) {
            konst foundModules = modules.konstues.filter { it.isMain }
            konst mainModule = when (konst size = foundModules.size) {
                1 -> foundModules.first()
                else -> fail { "Exactly one main module is expected. But ${if (size == 0) "none" else size} were found." }
            }

            nonParsedFiles += testFileFactory.createFile(mainModule, fileName, text)
        }
    }

    interface CurrentFileHandler {
        interface ModuleHandler {
            fun markAsMain()
        }

        konst packageFqName: FqName
        konst module: ModuleHandler
        konst psiFactory: KtPsiFactory

        fun accept(visitor: KtVisitor<*, *>)
        fun <D> accept(visitor: KtVisitor<*, D>, data: D)
    }

    companion object {
        private konst lock = Object()

        private fun createPsiFactory(parentDisposable: Disposable): KtPsiFactory {
            konst configuration: CompilerConfiguration = KotlinTestUtils.newConfiguration()
            configuration.put(CommonConfigurationKeys.MODULE_NAME, "native-blackbox-test-patching-module")

            konst environment = KotlinCoreEnvironment.createForProduction(
                parentDisposable = parentDisposable,
                configuration = configuration,
                configFiles = EnvironmentConfigFiles.METADATA_CONFIG_FILES
            )

            synchronized(lock) {
                CoreApplicationEnvironment.registerApplicationDynamicExtensionPoint(
                    TreeCopyHandler.EP_NAME.name,
                    TreeCopyHandler::class.java
                )
            }

            konst project = environment.project as MockProject
            project.registerService(PomModel::class.java, PomModelImpl::class.java)
            project.registerService(TreeAspect::class.java)

            return KtPsiFactory(environment.project)
        }
    }
}
