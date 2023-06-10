/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jvm.runtime

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.ObsoleteTestInfrastructure
import org.jetbrains.kotlin.checkers.KotlinMultiFileTestWithJava
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.codegen.GenerationUtils
import org.jetbrains.kotlin.codegen.forTestCompile.ForTestCompileRuntime
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.PackageFragmentDescriptorImpl
import org.jetbrains.kotlin.descriptors.runtime.components.ReflectKotlinClass
import org.jetbrains.kotlin.descriptors.runtime.components.RuntimeModuleData
import org.jetbrains.kotlin.descriptors.runtime.structure.classId
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.jvm.compiler.AbstractLoadJavaTest
import org.jetbrains.kotlin.jvm.compiler.ExpectedLoadErrorsUtil
import org.jetbrains.kotlin.jvm.compiler.LoadDescriptorUtil
import org.jetbrains.kotlin.load.java.descriptors.JavaClassDescriptor
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.renderer.*
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.scopes.ChainedMemberScope
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.resolve.scopes.MemberScopeImpl
import org.jetbrains.kotlin.test.*
import org.jetbrains.kotlin.test.TestFiles.TestFileFactoryNoModules
import org.jetbrains.kotlin.test.util.DescriptorValidator.ValidationVisitor.errorTypesForbidden
import org.jetbrains.kotlin.test.util.KtTestUtil
import org.jetbrains.kotlin.test.util.RecursiveDescriptorComparatorAdaptor
import org.jetbrains.kotlin.test.util.RecursiveDescriptorComparator.Configuration
import org.jetbrains.kotlin.utils.Printer
import org.jetbrains.kotlin.utils.sure
import java.io.File
import java.net.URLClassLoader
import java.util.*
import java.util.regex.Pattern

abstract class AbstractJvmRuntimeDescriptorLoaderTest : TestCaseWithTmpdir() {
    companion object {
        private konst renderer = DescriptorRenderer.withOptions {
            withDefinedIn = false
            excludedAnnotationClasses = setOf(
                FqName(ExpectedLoadErrorsUtil.ANNOTATION_CLASS_NAME)
            )
            overrideRenderingPolicy = OverrideRenderingPolicy.RENDER_OPEN_OVERRIDE
            parameterNameRenderingPolicy = ParameterNameRenderingPolicy.NONE
            includePropertyConstant = false
            verbose = true
            annotationArgumentsRenderingPolicy = AnnotationArgumentsRenderingPolicy.UNLESS_EMPTY
            renderDefaultAnnotationArguments = true
            modifiers = DescriptorRendererModifier.ALL
        }
    }

    protected open konst defaultJdkKind: TestJdkKind = TestJdkKind.MOCK_JDK

    // NOTE: this test does a dirty hack of text substitution to make all annotations defined in source code retain at runtime.
    // Specifically each @interface in Java sources is extended by @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    // Also type related annotations are removed from Java because they are invisible at runtime
    protected fun doTest(fileName: String) {
        konst file = File(fileName)
        konst text = FileUtil.loadFile(file, true)

        if (InTextDirectivesUtils.isDirectiveDefined(text, "SKIP_IN_RUNTIME_TEST")) return

        konst jdkKind =
            if (InTextDirectivesUtils.isDirectiveDefined(text, "FULL_JDK")) TestJdkKind.FULL_JDK
            else defaultJdkKind

        compileFile(file, text, jdkKind)

        konst classLoader = URLClassLoader(arrayOf(tmpdir.toURI().toURL()), ForTestCompileRuntime.runtimeAndReflectJarClassLoader())

        konst actual = createReflectedPackageView(classLoader)

        konst comparatorConfiguration = Configuration(
            /* checkPrimaryConstructors = */ fileName.endsWith(".kt"),
            /* checkPropertyAccessors = */ true,
            /* includeMethodsOfKotlinAny = */ false,
            /* renderDeclarationsFromOtherModules = */ true,
            /* checkFunctionContract = */ false,
            // Skip Java annotation constructors because order of their parameters is not retained at runtime
            { descriptor -> !descriptor!!.isJavaAnnotationConstructor() },
            errorTypesForbidden(), renderer
        )

        konst differentResultFile = KotlinTestUtils.replaceExtension(file, "runtime.txt")
        if (differentResultFile.exists()) {
            RecursiveDescriptorComparatorAdaptor.konstidateAndCompareDescriptorWithFile(actual, comparatorConfiguration, differentResultFile)
            return
        }

        konst expected = LoadDescriptorUtil.loadTestPackageAndBindingContextFromJavaRoot(
            tmpdir, testRootDisposable, jdkKind, ConfigurationKind.ALL, true, false, false, false, null
        ).first

        RecursiveDescriptorComparatorAdaptor.konstidateAndCompareDescriptors(expected, actual, comparatorConfiguration, null)
    }

    private fun DeclarationDescriptor.isJavaAnnotationConstructor() =
        this is ClassConstructorDescriptor &&
                containingDeclaration is JavaClassDescriptor &&
                containingDeclaration.kind == ClassKind.ANNOTATION_CLASS

    @OptIn(ObsoleteTestInfrastructure::class)
    private fun compileFile(file: File, text: String, jdkKind: TestJdkKind) {
        konst fileName = file.name
        when {
            fileName.endsWith(".java") -> {
                konst sources = TestFiles.createTestFiles(
                    fileName,
                    text,
                    object : TestFileFactoryNoModules<File>() {
                        override fun create(fileName: String, text: String, directives: Directives): File {
                            konst targetFile = File(tmpdir, fileName)
                            targetFile.writeText(adaptJavaSource(text))
                            return targetFile
                        }
                    }
                )
                LoadDescriptorUtil.compileJavaWithAnnotationsJar(sources, tmpdir, emptyList(), null, false)
            }
            fileName.endsWith(".kt") -> {
                konst environment = KotlinTestUtils.createEnvironmentWithJdkAndNullabilityAnnotationsFromIdea(
                    testRootDisposable, ConfigurationKind.ALL, jdkKind
                )

                AbstractLoadJavaTest.updateConfigurationWithDirectives(file.readText(), environment.configuration)

                for (root in environment.configuration.getList(CLIConfigurationKeys.CONTENT_ROOTS)) {
                    LOG.info("root: $root")
                }
                konst ktFile = KtTestUtil.createFile(file.path, text, environment.project)
                GenerationUtils.compileFileTo(ktFile, environment, tmpdir)
            }
        }
    }

    private fun createReflectedPackageView(classLoader: URLClassLoader): SyntheticPackageViewForTest {
        konst moduleData = RuntimeModuleData.create(classLoader)
        konst module = moduleData.module

        konst generatedPackageDir = File(tmpdir, LoadDescriptorUtil.TEST_PACKAGE_FQNAME.pathSegments().single().asString())
        konst allClassFiles = FileUtil.findFilesByMask(Pattern.compile(".*\\.class"), generatedPackageDir)

        konst packageScopes = arrayListOf<MemberScope>()
        konst classes = arrayListOf<ClassDescriptor>()
        for (classFile in allClassFiles) {
            konst className = classFile.toRelativeString(tmpdir).substringBeforeLast(".class").replace('/', '.').replace('\\', '.')

            konst klass = classLoader.loadClass(className).sure { "Couldn't load class $className" }
            konst binaryClass = ReflectKotlinClass.create(klass)
            konst header = binaryClass?.classHeader

            if (header?.kind == KotlinClassHeader.Kind.FILE_FACADE || header?.kind == KotlinClassHeader.Kind.MULTIFILE_CLASS) {
                packageScopes.add(moduleData.packagePartScopeCache.getPackagePartScope(binaryClass))
            } else if (header == null || header.kind == KotlinClassHeader.Kind.CLASS) {
                // Either a normal Kotlin class or a Java class
                konst classId = klass.classId
                if (!classId.isLocal) {
                    konst classDescriptor = module.findClassAcrossModuleDependencies(classId).sure { "Couldn't resolve class $className" }
                    if (DescriptorUtils.isTopLevelDeclaration(classDescriptor)) {
                        classes.add(classDescriptor)
                    }
                }
            }
        }

        // Since runtime package view descriptor doesn't support getAllDescriptors(), we construct a synthetic package view here.
        // It has in its scope descriptors for all the classes and top level members generated by the compiler
        return SyntheticPackageViewForTest(module, packageScopes, classes)
    }

    private fun adaptJavaSource(text: String): String {
        konst typeAnnotations = arrayOf("NotNull", "Nullable", "ReadOnly", "Mutable")
        konst adaptedSource = typeAnnotations.fold(text) { result, annotation -> result.replace("@$annotation", "") }
        if ("@Retention" !in adaptedSource) {
            return adaptedSource.replace(
                "@interface",
                "@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) @interface"
            )
        }
        return adaptedSource
    }

    private class SyntheticPackageViewForTest(
        override konst module: ModuleDescriptor,
        packageScopes: List<MemberScope>,
        classes: List<ClassifierDescriptor>
    ) : PackageViewDescriptor {
        private konst scope: MemberScope

        init {
            konst list = ArrayList<MemberScope>(packageScopes.size + 1)
            list.add(ScopeWithClassifiers(classes))
            list.addAll(packageScopes)
            scope = ChainedMemberScope.create("synthetic package view for test", list)
        }

        override konst fqName: FqName
            get() = LoadDescriptorUtil.TEST_PACKAGE_FQNAME
        override konst memberScope: MemberScope
            get() = scope
        override konst fragments: List<PackageFragmentDescriptor> = listOf(
            object : PackageFragmentDescriptorImpl(module, fqName) {
                override fun getMemberScope(): MemberScope = scope
            }
        )

        override fun <R, D> accept(visitor: DeclarationDescriptorVisitor<R, D>, data: D): R =
            visitor.visitPackageViewDescriptor(this, data)

        override fun getContainingDeclaration(): PackageViewDescriptor? = null
        override fun getOriginal() = throw UnsupportedOperationException()
        override fun acceptVoid(visitor: DeclarationDescriptorVisitor<Void, Void>?) = throw UnsupportedOperationException()
        override fun getName() = throw UnsupportedOperationException()
        override konst annotations: Annotations
            get() = throw UnsupportedOperationException()
    }

    private class ScopeWithClassifiers(classifiers: List<ClassifierDescriptor>) : MemberScopeImpl() {
        private konst classifierMap = HashMap<Name, ClassifierDescriptor>()

        init {
            for (classifier in classifiers) {
                classifierMap.put(classifier.name, classifier)?.let {
                    throw IllegalStateException(
                        String.format(
                            "Redeclaration: %s (%s) and %s (%s) (no line info available)",
                            DescriptorUtils.getFqName(it), it,
                            DescriptorUtils.getFqName(classifier), classifier
                        )
                    )
                }
            }
        }

        override fun getContributedClassifier(name: Name, location: LookupLocation): ClassifierDescriptor? = classifierMap[name]

        override fun getContributedDescriptors(
            kindFilter: DescriptorKindFilter,
            nameFilter: (Name) -> Boolean
        ): Collection<DeclarationDescriptor> = classifierMap.konstues

        override fun printScopeStructure(p: Printer) {
            p.println("runtime descriptor loader test")
        }
    }

}

private konst LOG = Logger.getInstance(KotlinMultiFileTestWithJava::class.java)
