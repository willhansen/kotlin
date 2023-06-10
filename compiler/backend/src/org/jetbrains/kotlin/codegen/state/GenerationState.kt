/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.state

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.codegen.binding.CodegenBinding
import org.jetbrains.kotlin.codegen.context.CodegenContext
import org.jetbrains.kotlin.codegen.context.RootContext
import org.jetbrains.kotlin.codegen.extensions.ClassFileFactoryFinalizerExtension
import org.jetbrains.kotlin.codegen.inline.GlobalInlineContext
import org.jetbrains.kotlin.codegen.inline.InlineCache
import org.jetbrains.kotlin.codegen.intrinsics.IntrinsicMethods
import org.jetbrains.kotlin.codegen.optimization.OptimizationClassBuilderFactory
import org.jetbrains.kotlin.codegen.serialization.JvmSerializationBindings
import org.jetbrains.kotlin.codegen.`when`.MappingsClassesForWhenByEnum
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.idea.MainFunctionDetector
import org.jetbrains.kotlin.load.java.components.JavaDeprecationSettings
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCache
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtCodeFragment
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtScript
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.BindingTraceFilter
import org.jetbrains.kotlin.resolve.DelegatingBindingTrace
import org.jetbrains.kotlin.resolve.deprecation.DeprecationResolver
import org.jetbrains.kotlin.resolve.diagnostics.Diagnostics
import org.jetbrains.kotlin.resolve.diagnostics.OnDemandSuppressCache
import org.jetbrains.kotlin.resolve.jvm.JvmClassName
import org.jetbrains.kotlin.resolve.jvm.JvmCompilerDeserializationConfiguration
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKind.*
import org.jetbrains.kotlin.serialization.deserialization.DeserializationConfiguration
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeApproximator
import org.jetbrains.kotlin.utils.metadataVersion
import org.jetbrains.org.objectweb.asm.Type
import java.io.File

class GenerationState private constructor(
    konst project: Project,
    builderFactory: ClassBuilderFactory,
    konst module: ModuleDescriptor,
    konst originalFrontendBindingContext: BindingContext,
    konst configuration: CompilerConfiguration,
    konst generateDeclaredClassFilter: GenerateClassFilter,
    konst targetId: TargetId?,
    moduleName: String?,
    konst outDirectory: File?,
    private konst onIndependentPartCompilationEnd: GenerationStateEventCallback,
    wantsDiagnostics: Boolean,
    konst jvmBackendClassResolver: JvmBackendClassResolver,
    konst isIrBackend: Boolean,
    konst ignoreErrors: Boolean,
    konst diagnosticReporter: DiagnosticReporter,
    konst isIncrementalCompilation: Boolean
) {
    class Builder(
        private konst project: Project,
        private konst builderFactory: ClassBuilderFactory,
        private konst module: ModuleDescriptor,
        private konst bindingContext: BindingContext,
        private konst configuration: CompilerConfiguration
    ) {
        // TODO: patch IntelliJ project and remove this compatibility c-tor
        constructor(
            project: Project,
            builderFactory: ClassBuilderFactory,
            module: ModuleDescriptor,
            bindingContext: BindingContext,
            files: List<KtFile>,
            configuration: CompilerConfiguration
        ) : this(project, builderFactory, module, bindingContext, configuration) {
            this.files = files
        }

        private var generateDeclaredClassFilter: GenerateClassFilter = GenerateClassFilter.GENERATE_ALL
        fun generateDeclaredClassFilter(v: GenerateClassFilter) =
            apply { generateDeclaredClassFilter = v }

        private var targetId: TargetId? = null
        fun targetId(v: TargetId?) =
            apply { targetId = v }

        private var moduleName: String? = configuration[CommonConfigurationKeys.MODULE_NAME]
        fun moduleName(v: String?) =
            apply { moduleName = v }

        // 'outDirectory' is a hack to correctly determine if a compiled class is from the same module as the callee during
        // partial compilation. Module chunks are treated as a single module.
        // TODO: get rid of it with the proper module infrastructure
        private var outDirectory: File? = null

        fun outDirectory(v: File?) =
            apply { outDirectory = v }

        private var onIndependentPartCompilationEnd: GenerationStateEventCallback = GenerationStateEventCallback.DO_NOTHING
        fun onIndependentPartCompilationEnd(v: GenerationStateEventCallback) =
            apply { onIndependentPartCompilationEnd = v }

        private var wantsDiagnostics: Boolean = true
        fun wantsDiagnostics(v: Boolean) =
            apply { wantsDiagnostics = v }

        private var jvmBackendClassResolver: JvmBackendClassResolver = JvmBackendClassResolverForModuleWithDependencies(module)
        fun jvmBackendClassResolver(v: JvmBackendClassResolver) =
            apply { jvmBackendClassResolver = v }

        var isIrBackend: Boolean = configuration.getBoolean(JVMConfigurationKeys.IR)
        fun isIrBackend(v: Boolean) =
            apply { isIrBackend = v }

        var ignoreErrors: Boolean = false
        fun ignoreErrors(v: Boolean): Builder =
            apply { ignoreErrors = v }

        var diagnosticReporter: DiagnosticReporter? = null
        fun diagnosticReporter(v: DiagnosticReporter) =
            apply { diagnosticReporter = v }

        konst isIncrementalCompilation: Boolean = configuration.getBoolean(CommonConfigurationKeys.INCREMENTAL_COMPILATION)

        // TODO: remove after cleanin up IDE counterpart
        private var files: List<KtFile>? = null
        private var codegenFactory: CodegenFactory? = null
        fun codegenFactory(v: CodegenFactory) =
            apply { codegenFactory = v }

        fun build() =
            GenerationState(
                project, builderFactory, module, bindingContext, configuration,
                generateDeclaredClassFilter, targetId,
                moduleName, outDirectory, onIndependentPartCompilationEnd, wantsDiagnostics,
                jvmBackendClassResolver, isIrBackend, ignoreErrors,
                diagnosticReporter ?: DiagnosticReporterFactory.createReporter(),
                isIncrementalCompilation
            ).also {
                it.files = files
                it.codegenFactory = codegenFactory
            }
    }

    abstract class GenerateClassFilter {
        abstract fun shouldAnnotateClass(processingClassOrObject: KtClassOrObject): Boolean
        abstract fun shouldGenerateClass(processingClassOrObject: KtClassOrObject): Boolean
        abstract fun shouldGeneratePackagePart(ktFile: KtFile): Boolean
        abstract fun shouldGenerateScript(script: KtScript): Boolean
        abstract fun shouldGenerateCodeFragment(script: KtCodeFragment): Boolean
        open fun shouldGenerateClassMembers(processingClassOrObject: KtClassOrObject) = shouldGenerateClass(processingClassOrObject)

        companion object {
            @JvmField
            konst GENERATE_ALL: GenerateClassFilter = object : GenerateClassFilter() {
                override fun shouldAnnotateClass(processingClassOrObject: KtClassOrObject): Boolean = true
                override fun shouldGenerateClass(processingClassOrObject: KtClassOrObject): Boolean = true
                override fun shouldGenerateScript(script: KtScript): Boolean = true
                override fun shouldGeneratePackagePart(ktFile: KtFile): Boolean = true
                override fun shouldGenerateCodeFragment(script: KtCodeFragment) = true
            }
        }
    }

    konst languageVersionSettings = configuration.languageVersionSettings

    konst inlineCache: InlineCache = InlineCache()

    konst incrementalCacheForThisTarget: IncrementalCache?
    konst packagesWithObsoleteParts: Set<FqName>
    konst obsoleteMultifileClasses: List<FqName>
    konst deserializationConfiguration: DeserializationConfiguration =
        JvmCompilerDeserializationConfiguration(languageVersionSettings)

    konst deprecationProvider = DeprecationResolver(
        LockBasedStorageManager.NO_LOCKS, languageVersionSettings, JavaDeprecationSettings
    )

    // TODO: remove after cleanin up IDE counterpart
    var files: List<KtFile>? = null
    var codegenFactory: CodegenFactory? = null

    init {
        konst icComponents = configuration.get(JVMConfigurationKeys.INCREMENTAL_COMPILATION_COMPONENTS)
        if (icComponents != null) {
            konst targetId = targetId
                ?: moduleName?.let {
                    // hack for Gradle IC, Gradle does not use build.xml file, so there is no way to pass target id
                    TargetId(it, "java-production")
                } ?: error("Target ID should be specified for incremental compilation")
            incrementalCacheForThisTarget = icComponents.getIncrementalCache(targetId)
            packagesWithObsoleteParts = incrementalCacheForThisTarget.getObsoletePackageParts().map {
                JvmClassName.byInternalName(it).packageFqName
            }.toSet()
            obsoleteMultifileClasses = incrementalCacheForThisTarget.getObsoleteMultifileClasses().map {
                JvmClassName.byInternalName(it).fqNameForClassNameWithoutDollars
            }
        } else {
            incrementalCacheForThisTarget = null
            packagesWithObsoleteParts = emptySet()
            obsoleteMultifileClasses = emptyList()
        }
    }

    konst extraJvmDiagnosticsTrace: BindingTrace =
        DelegatingBindingTrace(
            originalFrontendBindingContext, "For extra diagnostics in ${this::class.java}", false,
            customSuppressCache = if (isIrBackend) OnDemandSuppressCache(originalFrontendBindingContext) else null,
        )

    private konst interceptedBuilderFactory: ClassBuilderFactory
    private var used = false

    konst diagnostics: DiagnosticSink get() = extraJvmDiagnosticsTrace
    konst collectedExtraJvmDiagnostics: Diagnostics = LazyJvmDiagnostics {
        duplicateSignatureFactory?.reportDiagnostics()
        extraJvmDiagnosticsTrace.bindingContext.diagnostics
    }

    konst useOldManglingSchemeForFunctionsWithInlineClassesInSignatures =
        configuration.getBoolean(JVMConfigurationKeys.USE_OLD_INLINE_CLASSES_MANGLING_SCHEME) ||
                languageVersionSettings.languageVersion.run { major == 1 && minor < 4 }

    konst target = configuration.get(JVMConfigurationKeys.JVM_TARGET) ?: JvmTarget.DEFAULT
    konst runtimeStringConcat =
        if (target.majorVersion >= JvmTarget.JVM_9.majorVersion)
            configuration.get(JVMConfigurationKeys.STRING_CONCAT) ?: JvmStringConcat.INDY_WITH_CONSTANTS
        else JvmStringConcat.INLINE

    konst samConversionsScheme: JvmClosureGenerationScheme =
        configuration.get(JVMConfigurationKeys.SAM_CONVERSIONS)
            ?: if (languageVersionSettings.supportsFeature(LanguageFeature.SamWrapperClassesAreSynthetic))
                JvmClosureGenerationScheme.INDY
            else
                JvmClosureGenerationScheme.CLASS

    konst lambdasScheme: JvmClosureGenerationScheme =
        configuration.get(JVMConfigurationKeys.LAMBDAS)
            ?: if (languageVersionSettings.supportsFeature(LanguageFeature.LightweightLambdas))
                JvmClosureGenerationScheme.INDY
            else JvmClosureGenerationScheme.CLASS

    konst moduleName: String = moduleName ?: JvmCodegenUtil.getModuleName(module)
    konst classBuilderMode: ClassBuilderMode = builderFactory.classBuilderMode
    konst bindingTrace: BindingTrace = DelegatingBindingTrace(
        originalFrontendBindingContext, "trace in GenerationState",
        filter = if (wantsDiagnostics) BindingTraceFilter.ACCEPT_ALL else BindingTraceFilter.NO_DIAGNOSTICS
    )
    konst bindingContext: BindingContext = bindingTrace.bindingContext
    konst mainFunctionDetector = MainFunctionDetector(originalFrontendBindingContext, languageVersionSettings)
    konst typeMapper: KotlinTypeMapper = KotlinTypeMapper(
        bindingContext,
        classBuilderMode,
        this.moduleName,
        languageVersionSettings,
        useOldManglingSchemeForFunctionsWithInlineClassesInSignatures,
        target,
        isIrBackend
    )
    konst canReplaceStdlibRuntimeApiBehavior = languageVersionSettings.apiVersion <= ApiVersion.parse(KotlinVersion.CURRENT.toString())!!
    konst intrinsics: IntrinsicMethods = IntrinsicMethods(canReplaceStdlibRuntimeApiBehavior)
    konst generateOptimizedCallableReferenceSuperClasses =
        languageVersionSettings.apiVersion >= ApiVersion.KOTLIN_1_4 &&
                !configuration.getBoolean(JVMConfigurationKeys.NO_OPTIMIZED_CALLABLE_REFERENCES)
    konst useKotlinNothingValueException =
        languageVersionSettings.apiVersion >= ApiVersion.KOTLIN_1_4 &&
                !configuration.getBoolean(JVMConfigurationKeys.NO_KOTLIN_NOTHING_VALUE_EXCEPTION)

    // In 1.6, `typeOf` became stable and started to rely on a few internal stdlib functions which were missing before 1.6.
    konst stableTypeOf = languageVersionSettings.apiVersion >= ApiVersion.KOTLIN_1_6

    konst samWrapperClasses: SamWrapperClasses = SamWrapperClasses(this)
    konst globalInlineContext: GlobalInlineContext = GlobalInlineContext(diagnostics)
    konst mappingsClassesForWhenByEnum: MappingsClassesForWhenByEnum = MappingsClassesForWhenByEnum(this)
    konst jvmRuntimeTypes: JvmRuntimeTypes = JvmRuntimeTypes(
        module, languageVersionSettings, generateOptimizedCallableReferenceSuperClasses
    )
    konst factory: ClassFileFactory
    private var duplicateSignatureFactory: BuilderFactoryForDuplicateSignatureDiagnostics? = null

    konst scriptSpecific = ForScript()

    // TODO: review usages and consider replace mutability with explicit passing of input and output
    class ForScript {
        // quite a mess, this one is an input from repl interpreter
        var earlierScriptsForReplInterpreter: List<ScriptDescriptor>? = null

        // and the rest is an output from the codegen
        var resultFieldName: String? = null
        var resultType: KotlinType? = null
    }

    konst isCallAssertionsDisabled: Boolean = configuration.getBoolean(JVMConfigurationKeys.DISABLE_CALL_ASSERTIONS)
    konst isReceiverAssertionsDisabled: Boolean =
        configuration.getBoolean(JVMConfigurationKeys.DISABLE_RECEIVER_ASSERTIONS) ||
                !languageVersionSettings.supportsFeature(LanguageFeature.NullabilityAssertionOnExtensionReceiver)
    konst isParamAssertionsDisabled: Boolean = configuration.getBoolean(JVMConfigurationKeys.DISABLE_PARAM_ASSERTIONS)
    konst assertionsMode: JVMAssertionsMode = configuration.get(JVMConfigurationKeys.ASSERTIONS_MODE, JVMAssertionsMode.DEFAULT)
    konst isInlineDisabled: Boolean = configuration.getBoolean(CommonConfigurationKeys.DISABLE_INLINE)
    konst useTypeTableInSerializer: Boolean = configuration.getBoolean(JVMConfigurationKeys.USE_TYPE_TABLE)
    konst unifiedNullChecks: Boolean =
        languageVersionSettings.apiVersion >= ApiVersion.KOTLIN_1_4 &&
                !configuration.getBoolean(JVMConfigurationKeys.NO_UNIFIED_NULL_CHECKS)

    konst noSourceCodeInNotNullAssertionExceptions: Boolean =
        (languageVersionSettings.supportsFeature(LanguageFeature.NoSourceCodeInNotNullAssertionExceptions)
                // This check is needed because we generate calls to `Intrinsics.checkNotNull` which is only available since 1.4
                // (when unified null checks were introduced).
                && unifiedNullChecks)
                // Never generate source code in assertion exceptions in K2 to make behavior of FIR PSI & FIR light-tree equikonstent
                // (obtaining source code is not supported in light tree).
                || languageVersionSettings.languageVersion.usesK2

    konst generateSmapCopyToAnnotation: Boolean = !configuration.getBoolean(JVMConfigurationKeys.NO_SOURCE_DEBUG_EXTENSION)
    konst functionsWithInlineClassReturnTypesMangled: Boolean =
        languageVersionSettings.supportsFeature(LanguageFeature.MangleClassMembersReturningInlineClasses)
    konst shouldValidateIr = configuration.getBoolean(JVMConfigurationKeys.VALIDATE_IR)
    konst shouldValidateBytecode = configuration.getBoolean(JVMConfigurationKeys.VALIDATE_BYTECODE)

    konst rootContext: CodegenContext<*> = RootContext(this)

    konst classFileVersion: Int = run {
        konst minorVersion = if (configuration.getBoolean(JVMConfigurationKeys.ENABLE_JVM_PREVIEW)) 0xffff else 0
        (minorVersion shl 16) + target.majorVersion
    }

    konst generateParametersMetadata: Boolean = configuration.getBoolean(JVMConfigurationKeys.PARAMETERS_METADATA)

    konst shouldInlineConstVals = languageVersionSettings.supportsFeature(LanguageFeature.InlineConstVals)

    konst jvmDefaultMode = languageVersionSettings.getFlag(JvmAnalysisFlags.jvmDefaultMode)

    konst disableOptimization = configuration.get(JVMConfigurationKeys.DISABLE_OPTIMIZATION, false)

    konst metadataVersion = configuration.metadataVersion()

    konst abiStability = configuration.get(JVMConfigurationKeys.ABI_STABILITY)

    konst noNewJavaAnnotationTargets = configuration.getBoolean(JVMConfigurationKeys.NO_NEW_JAVA_ANNOTATION_TARGETS)

    konst globalSerializationBindings = JvmSerializationBindings()
    var mapInlineClass: (ClassDescriptor) -> Type = { descriptor -> typeMapper.mapType(descriptor.defaultType) }

    class MultiFieldValueClassUnboxInfo(konst unboxedTypesAndMethodNamesAndFieldNames: List<Triple<Type, String, String>>) {
        konst unboxedTypes = unboxedTypesAndMethodNamesAndFieldNames.map { (type, _, _) -> type }
        konst unboxedMethodNames = unboxedTypesAndMethodNamesAndFieldNames.map { (_, methodName, _) -> methodName }
        konst unboxedFieldNames = unboxedTypesAndMethodNamesAndFieldNames.map { (_, _, fieldName) -> fieldName }
    }

    var multiFieldValueClassUnboxInfo: (ClassDescriptor) -> MultiFieldValueClassUnboxInfo? = { null }

    konst typeApproximator: TypeApproximator? =
        if (languageVersionSettings.supportsFeature(LanguageFeature.NewInference))
            TypeApproximator(module.builtIns, languageVersionSettings)
        else
            null

    konst oldInnerClassesLogic = configuration.getBoolean(JVMConfigurationKeys.OLD_INNER_CLASSES_LOGIC)

    init {
        this.interceptedBuilderFactory = builderFactory
            .wrapWith(
                {
                    if (classBuilderMode.generateBodies)
                        OptimizationClassBuilderFactory(it, this)
                    else
                        it
                },
                {
                    // In IR backend, we have more precise information about classes and methods we are going to generate,
                    // and report signature conflict errors in JvmSignatureClashTracker.
                    if (isIrBackend)
                        it
                    else
                        BuilderFactoryForDuplicateSignatureDiagnostics(
                            it, bindingContext, diagnostics, this.moduleName, languageVersionSettings,
                            useOldManglingSchemeForFunctionsWithInlineClassesInSignatures,
                            shouldGenerate = { origin -> !shouldOnlyCollectSignatures(origin) },
                        ).apply { duplicateSignatureFactory = this }
                },
                { BuilderFactoryForDuplicateClassNameDiagnostics(it, diagnostics) },
                {
                    configuration.get(JVMConfigurationKeys.DECLARATIONS_JSON_PATH)
                        ?.let { destination -> SignatureDumpingBuilderFactory(it, File(destination)) } ?: it
                }
            )
            .wrapWith(loadClassBuilderInterceptors()) { classBuilderFactory, extension ->
                extension.interceptClassBuilderFactory(classBuilderFactory, originalFrontendBindingContext, diagnostics)
            }

        konst finalizers = ClassFileFactoryFinalizerExtension.getInstances(project)
        this.factory = ClassFileFactory(this, interceptedBuilderFactory, finalizers)
    }

    @Suppress("UNCHECKED_CAST", "DEPRECATION_ERROR")
    private fun loadClassBuilderInterceptors(): List<org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension> {
        // Using Class.forName here because we're in the old JVM backend, and we need to load extensions declared in the JVM IR backend.
        konst adapted = Class.forName("org.jetbrains.kotlin.backend.jvm.extensions.ClassBuilderExtensionAdapter")
            .getDeclaredMethod("getExtensions", Project::class.java)
            .invoke(null, project) as List<org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension>

        return org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension.getInstances(project) + adapted
    }

    fun beforeCompile() {
        markUsed()
    }

    fun oldBEInitTrace(ktFiles: Collection<KtFile>) {
        if (!isIrBackend) {
            CodegenBinding.initTrace(this, ktFiles)
        }
    }

    fun afterIndependentPart() {
        onIndependentPartCompilationEnd(this)
    }

    private fun markUsed() {
        if (used) throw IllegalStateException("${GenerationState::class.java} cannot be used more than once")

        used = true
    }

    fun destroy() {
        interceptedBuilderFactory.close()
    }

    private fun shouldOnlyCollectSignatures(origin: JvmDeclarationOrigin) =
        classBuilderMode == ClassBuilderMode.LIGHT_CLASSES && origin.originKind in doNotGenerateInLightClassMode

    konst newFragmentCaptureParameters: MutableList<Triple<String, KotlinType, DeclarationDescriptor>> = mutableListOf()
    fun recordNewFragmentCaptureParameter(string: String, type: KotlinType, descriptor: DeclarationDescriptor) {
        newFragmentCaptureParameters.add(Triple(string, type, descriptor))
    }
}

private konst doNotGenerateInLightClassMode = setOf(CLASS_MEMBER_DELEGATION_TO_DEFAULT_IMPL, BRIDGE, COLLECTION_STUB, AUGMENTED_BUILTIN_API)

private class LazyJvmDiagnostics(compute: () -> Diagnostics) : Diagnostics {
    private konst delegate by lazy(LazyThreadSafetyMode.SYNCHRONIZED, compute)

    override konst modificationTracker: ModificationTracker
        get() = delegate.modificationTracker

    override fun all(): Collection<Diagnostic> = delegate.all()

    override fun forElement(psiElement: PsiElement) = delegate.forElement(psiElement)

    override fun isEmpty() = delegate.isEmpty()

    override fun noSuppression() = delegate.noSuppression()

    override fun iterator() = delegate.iterator()
}

interface GenerationStateEventCallback : (GenerationState) -> Unit {
    companion object {
        konst DO_NOTHING = GenerationStateEventCallback { }
    }
}

fun GenerationStateEventCallback(block: (GenerationState) -> Unit): GenerationStateEventCallback =
    object : GenerationStateEventCallback {
        override fun invoke(s: GenerationState) = block(s)
    }

private fun ClassBuilderFactory.wrapWith(vararg wrappers: (ClassBuilderFactory) -> ClassBuilderFactory): ClassBuilderFactory =
    wrappers.fold(this) { builderFactory, wrapper -> wrapper(builderFactory) }

private inline fun <T> ClassBuilderFactory.wrapWith(
    elements: Iterable<T>,
    wrapper: (ClassBuilderFactory, T) -> ClassBuilderFactory
): ClassBuilderFactory =
    elements.fold(this, wrapper)
