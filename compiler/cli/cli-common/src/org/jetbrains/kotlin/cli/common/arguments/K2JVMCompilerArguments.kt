/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common.arguments

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.*

class K2JVMCompilerArguments : CommonCompilerArguments() {
    companion object {
        @JvmStatic
        private konst serialVersionUID = 0L
    }

    @Argument(konstue = "-d", konstueDescription = "<directory|jar>", description = "Destination for generated class files")
    var destination: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-classpath",
        shortName = "-cp",
        konstueDescription = "<path>",
        description = "List of directories and JAR/ZIP archives to search for user class files"
    )
    var classpath: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(konstue = "-include-runtime", description = "Include Kotlin runtime into the resulting JAR")
    var includeRuntime = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-jdk-home",
        konstueDescription = "<path>",
        description = "Include a custom JDK from the specified location into the classpath instead of the default JAVA_HOME"
    )
    var jdkHome: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @GradleOption(
        konstue = DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @Argument(konstue = "-no-jdk", description = "Don't automatically include the Java runtime into the classpath")
    var noJdk = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-no-stdlib",
        description = "Don't automatically include the Kotlin/JVM stdlib and Kotlin reflection into the classpath"
    )
    var noStdlib = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-no-reflect", description = "Don't automatically include Kotlin reflection into the classpath")
    var noReflect = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-expression",
        shortName = "-e",
        description = "Ekonstuate the given string as a Kotlin script"
    )
    var expression: String? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-script-templates",
        konstueDescription = "<fully qualified class name[,]>",
        description = "Script definition template classes"
    )
    var scriptTemplates: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @GradleOption(
        konstue = DefaultValue.STRING_NULL_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @Argument(konstue = "-module-name", konstueDescription = "<name>", description = "Name of the generated .kotlin_module file")
    var moduleName: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @GradleOption(
        konstue = DefaultValue.JVM_TARGET_VERSIONS,
        gradleInputType = GradleInputTypes.INPUT,
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @Argument(
        konstue = "-jvm-target",
        konstueDescription = "<version>",
        description = "Target version of the generated JVM bytecode (${JvmTarget.SUPPORTED_VERSIONS_DESCRIPTION}), default is 1.8",
    )
    var jvmTarget: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @GradleOption(
        konstue = DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @Argument(konstue = "-java-parameters", description = "Generate metadata for Java 1.8 reflection on method parameters")
    var javaParameters = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    // Advanced options

    @Argument(konstue = "-Xuse-old-backend", description = "Use the old JVM backend")
    var useOldBackend = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xallow-unstable-dependencies",
        description = "Do not report errors on classes in dependencies, which were compiled by an unstable version of the Kotlin compiler"
    )
    var allowUnstableDependencies = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xabi-stability",
        konstueDescription = "{stable|unstable}",
        description = "When using unstable compiler features such as FIR, use 'stable' to mark generated class files as stable\n" +
                "to prevent diagnostics from stable compilers at the call site.\n" +
                "When using the JVM IR backend, conversely, use 'unstable' to mark generated class files as unstable\n" +
                "to force diagnostics to be reported."
    )
    var abiStability: String? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xir-do-not-clear-binding-context",
        description = "When using the IR backend, do not clear BindingContext between psi2ir and lowerings"
    )
    var doNotClearBindingContext = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xbackend-threads",
        konstueDescription = "<N>",
        description = "When using the IR backend, run lowerings by file in N parallel threads.\n" +
                "0 means use a thread per processor core.\n" +
                "Default konstue is 1"
    )
    var backendThreads: String = "1"
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xmodule-path", konstueDescription = "<path>", description = "Paths where to find Java 9+ modules")
    var javaModulePath: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-Xadd-modules",
        konstueDescription = "<module[,]>",
        description = "Root modules to resolve in addition to the initial modules,\n" +
                "or all modules on the module path if <module> is ALL-MODULE-PATH"
    )
    var additionalJavaModules: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xno-call-assertions", description = "Don't generate not-null assertions for arguments of platform types")
    var noCallAssertions = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xno-receiver-assertions",
        description = "Don't generate not-null assertion for extension receiver arguments of platform types"
    )
    var noReceiverAssertions = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xno-param-assertions",
        description = "Don't generate not-null assertions on parameters of methods accessible from Java"
    )
    var noParamAssertions = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xno-optimize", description = "Disable optimizations")
    var noOptimize = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xassertions", konstueDescription = "{always-enable|always-disable|jvm|legacy}",
        description = "Assert calls behaviour\n" +
                "-Xassertions=always-enable:  enable, ignore jvm assertion settings;\n" +
                "-Xassertions=always-disable: disable, ignore jvm assertion settings;\n" +
                "-Xassertions=jvm:            enable, depend on jvm assertion settings;\n" +
                "-Xassertions=legacy:         calculate condition on each call, check depends on jvm assertion settings in the kotlin package;\n" +
                "default: legacy"
    )
    var assertionsMode: String? = JVMAssertionsMode.DEFAULT.description
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) JVMAssertionsMode.DEFAULT.description else konstue
        }

    @Argument(
        konstue = "-Xbuild-file",
        deprecatedName = "-module",
        konstueDescription = "<path>",
        description = "Path to the .xml build file to compile"
    )
    var buildFile: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(konstue = "-Xmultifile-parts-inherit", description = "Compile multifile classes as a hierarchy of parts and facade")
    var inheritMultifileParts = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xuse-type-table", description = "Use type table in metadata serialization")
    var useTypeTable = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xuse-old-class-files-reading",
        description = "Use old class files reading implementation. This may slow down the build and cause problems with Groovy interop.\n" +
                "Should be used in case of problems with the new implementation"
    )
    var useOldClassFilesReading = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xuse-fast-jar-file-system",
        description = "Use fast implementation on Jar FS. This may speed up compilation time, but currently it's an experimental mode"
    )
    var useFastJarFileSystem = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xdump-declarations-to",
        konstueDescription = "<path>",
        description = "Path to JSON file to dump Java to Kotlin declaration mappings"
    )
    var declarationsOutputPath: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-Xsuppress-missing-builtins-error",
        description = "Suppress the \"cannot access built-in declaration\" error (useful with -no-stdlib)"
    )
    var suppressMissingBuiltinsError = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xscript-resolver-environment",
        konstueDescription = "<key=konstue[,]>",
        description = "Script resolver environment in key-konstue pairs (the konstue could be quoted and escaped)"
    )
    var scriptResolverEnvironment: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    // Javac options
    @Argument(konstue = "-Xuse-javac", description = "Use javac for Java source and class files analysis")
    var useJavac = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xcompile-java", description = "Reuse javac analysis and compile Java source files")
    var compileJava = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xjavac-arguments",
        konstueDescription = "<option[,]>",
        description = "Java compiler arguments"
    )
    var javacArguments: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }


    @Argument(
        konstue = "-Xjava-source-roots",
        konstueDescription = "<path>",
        description = "Paths to directories with Java source files"
    )
    var javaSourceRoots: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xjava-package-prefix",
        description = "Package prefix for Java files"
    )
    var javaPackagePrefix: String? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xjsr305",
        deprecatedName = "-Xjsr305-annotations",
        konstueDescription = "{ignore/strict/warn}" +
                "|under-migration:{ignore/strict/warn}" +
                "|@<fq.name>:{ignore/strict/warn}",
        description = "Specify behavior for JSR-305 nullability annotations:\n" +
                "-Xjsr305={ignore/strict/warn}                   globally (all non-@UnderMigration annotations)\n" +
                "-Xjsr305=under-migration:{ignore/strict/warn}   all @UnderMigration annotations\n" +
                "-Xjsr305=@<fq.name>:{ignore/strict/warn}        annotation with the given fully qualified class name\n" +
                "Modes:\n" +
                "  * ignore\n" +
                "  * strict (experimental; treat as other supported nullability annotations)\n" +
                "  * warn (report a warning)"
    )
    var jsr305: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xnullability-annotations",
        konstueDescription = "@<fq.name>:{ignore/strict/warn}",
        description = "Specify behavior for specific Java nullability annotations (provided with fully qualified package name)\n" +
                "Modes:\n" +
                "  * ignore\n" +
                "  * strict\n" +
                "  * warn (report a warning)"
    )
    var nullabilityAnnotations: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xsupport-compatqual-checker-framework-annotations",
        konstueDescription = "enable|disable",
        description = "Specify behavior for Checker Framework compatqual annotations (NullableDecl/NonNullDecl).\n" +
                "Default konstue is 'enable'"
    )
    var supportCompatqualCheckerFrameworkAnnotations: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-Xjspecify-annotations",
        konstueDescription = "ignore|strict|warn",
        description = "Specify behavior for jspecify annotations.\n" +
                "Default konstue is 'warn'"
    )
    var jspecifyAnnotations: String? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xjvm-default",
        konstueDescription = "{all|all-compatibility|disable}",
        description = """Emit JVM default methods for interface declarations with bodies. Default is 'disable'.
-Xjvm-default=all                Generate JVM default methods for all interface declarations with bodies in the module.
                                 Do not generate DefaultImpls stubs for interface declarations with bodies, which are generated by default
                                 in the 'disable' mode. If interface inherits a method with body from an interface compiled in the 'disable'
                                 mode and doesn't override it, then a DefaultImpls stub will be generated for it.
                                 BREAKS BINARY COMPATIBILITY if some client code relies on the presence of DefaultImpls classes.
                                 Note that if interface delegation is used, all interface methods are delegated.
-Xjvm-default=all-compatibility  In addition to the 'all' mode, generate compatibility stubs in the DefaultImpls classes.
                                 Compatibility stubs could be useful for library and runtime authors to keep backward binary compatibility
                                 for existing clients compiled against previous library versions.
                                 'all' and 'all-compatibility' modes are changing the library ABI surface that will be used by clients after
                                 the recompilation of the library. In that sense, clients might be incompatible with previous library
                                 versions. This usually means that proper library versioning is required, e.g. major version increase in SemVer.
                                 In case of inheritance from a Kotlin interface compiled in 'all' or 'all-compatibility' modes, DefaultImpls
                                 compatibility stubs will invoke the default method of the interface with standard JVM runtime resolution semantics.
                                 Perform additional compatibility checks for classes inheriting generic interfaces where in some cases
                                 additional implicit method with specialized signatures was generated in the 'disable' mode:
                                 unlike in the 'disable' mode, the compiler will report an error if such method is not overridden explicitly
                                 and the class is not annotated with @JvmDefaultWithoutCompatibility (see KT-39603 for more details).
-Xjvm-default=disable            Default behavior. Do not generate JVM default methods."""
    )
    var jvmDefault: String = JvmDefaultMode.DEFAULT.description
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xdefault-script-extension",
        konstueDescription = "<script filename extension>",
        description = "Compile expressions and unrecognized scripts passed with the -script argument as scripts with given filename extension"
    )
    var defaultScriptExtension: String? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xdisable-standard-script", description = "Disable standard kotlin script support")
    var disableStandardScript = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xgenerate-strict-metadata-version",
        description = "Generate metadata with strict version semantics (see kdoc on Metadata.extraInt)"
    )
    var strictMetadataVersionSemantics = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xsanitize-parentheses",
        description = "Transform '(' and ')' in method names to some other character sequence.\n" +
                "This mode can BREAK BINARY COMPATIBILITY and is only supposed to be used to workaround\n" +
                "problems with parentheses in identifiers on certain platforms"
    )
    var sanitizeParentheses = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xfriend-paths",
        konstueDescription = "<path>",
        description = "Paths to output directories for friend modules (whose internals should be visible)"
    )
    var friendPaths: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xallow-no-source-files",
        description = "Allow no source files"
    )
    var allowNoSourceFiles = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xemit-jvm-type-annotations",
        description = "Emit JVM type annotations in bytecode"
    )
    var emitJvmTypeAnnotations = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xstring-concat",
        konstueDescription = "{indy-with-constants|indy|inline}",
        description = """Select code generation scheme for string concatenation.
-Xstring-concat=indy-with-constants   Concatenate strings using `invokedynamic` `makeConcatWithConstants`. Requires `-jvm-target 9` or greater.
-Xstring-concat=indy                Concatenate strings using `invokedynamic` `makeConcat`. Requires `-jvm-target 9` or greater.
-Xstring-concat=inline              Concatenate strings using `StringBuilder`
default: `indy-with-constants` for JVM target 9 or greater, `inline` otherwise"""

    )
    var stringConcat: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-Xjdk-release",
        konstueDescription = "<version>",
        description = """Compile against the specified JDK API version, similarly to javac's `-release`. Requires JDK 9 or newer.
Supported versions depend on the used JDK; for JDK 17+ supported versions are ${JvmTarget.SUPPORTED_VERSIONS_DESCRIPTION}.
Also sets `-jvm-target` konstue equal to the selected JDK version"""
    )
    var jdkRelease: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }


    @Argument(
        konstue = "-Xsam-conversions",
        konstueDescription = "{class|indy}",
        description = """Select code generation scheme for SAM conversions.
-Xsam-conversions=indy              Generate SAM conversions using `invokedynamic` with `LambdaMetafactory.metafactory`. Requires `-jvm-target 1.8` or greater.
-Xsam-conversions=class             Generate SAM conversions as explicit classes"""
    )
    var samConversions: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-Xlambdas",
        konstueDescription = "{class|indy}",
        description = """Select code generation scheme for lambdas.
-Xlambdas=indy                      Generate lambdas using `invokedynamic` with `LambdaMetafactory.metafactory`. Requires `-jvm-target 1.8` or greater.
                                    Lambda objects created using `LambdaMetafactory.metafactory` will have different `toString()`.
-Xlambdas=class                     Generate lambdas as explicit classes"""
    )
    var lambdas: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-Xklib",
        konstueDescription = "<path>",
        description = "Paths to cross-platform libraries in .klib format"
    )
    var klibLibraries: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-Xno-optimized-callable-references",
        description = "Do not use optimized callable reference superclasses available from 1.4"
    )
    var noOptimizedCallableReferences = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xno-kotlin-nothing-konstue-exception",
        description = "Do not use KotlinNothingValueException available since 1.4"
    )
    var noKotlinNothingValueException = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xno-reset-jar-timestamps",
        description = "Do not reset jar entry timestamps to a fixed date"
    )
    var noResetJarTimestamps = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xno-unified-null-checks",
        description = "Use pre-1.4 exception types in null checks instead of java.lang.NPE. See KT-22275 for more details"
    )
    var noUnifiedNullChecks = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xno-source-debug-extension",
        description = "Do not generate @kotlin.jvm.internal.SourceDebugExtension annotation on a class with the copy of SMAP"
    )
    var noSourceDebugExtension = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xprofile",
        konstueDescription = "<profilerPath:command:outputDir>",
        description = "Debug option: Run compiler with async profiler and save snapshots to `outputDir`; `command` is passed to async-profiler on start.\n" +
                "`profilerPath` is a path to libasyncProfiler.so; async-profiler.jar should be on the compiler classpath.\n" +
                "If it's not on the classpath, the compiler will attempt to load async-profiler.jar from the containing directory of profilerPath.\n" +
                "Example: -Xprofile=<PATH_TO_ASYNC_PROFILER>/async-profiler/build/libasyncProfiler.so:event=cpu,interkonst=1ms,threads,start:<SNAPSHOT_DIR_PATH>"
    )
    var profileCompilerCommand: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-Xrepeat",
        konstueDescription = "<number>",
        description = "Debug option: Repeats modules compilation <number> times"
    )
    var repeatCompileModules: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-Xuse-14-inline-classes-mangling-scheme",
        description = "Use 1.4 inline classes mangling scheme instead of 1.4.30 one"
    )
    var useOldInlineClassesManglingScheme = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xjvm-enable-preview",
        description = "Allow using features from Java language that are in preview phase.\n" +
                "Works as `--enable-preview` in Java. All class files are marked as preview-generated thus it won't be possible to use them in release environment"
    )
    var enableJvmPreview = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xsuppress-deprecated-jvm-target-warning",
        description = "Suppress deprecation warning about deprecated JVM target versions.\n" +
                "This option has no effect and will be deleted in a future version."
    )
    var suppressDeprecatedJvmTargetWarning = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xtype-enhancement-improvements-strict-mode",
        description = "Enable strict mode for some improvements in the type enhancement for loaded Java types based on nullability annotations,\n" +
                "including freshly supported reading of the type use annotations from class files.\n" +
                "See KT-45671 for more details"
    )
    var typeEnhancementImprovementsInStrictMode = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xserialize-ir",
        konstueDescription = "{none|inline|all}",
        description = "Save IR to metadata (EXPERIMENTAL)"
    )
    var serializeIr: String = "none"
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xkonstidate-ir",
        description = "Validate IR before and after lowering"
    )
    var konstidateIr = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xkonstidate-bytecode",
        description = "Validate generated JVM bytecode before and after optimizations"
    )
    var konstidateBytecode = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xenhance-type-parameter-types-to-def-not-null",
        description = "Enhance not null annotated type parameter's types to definitely not null types (@NotNull T => T & Any)"
    )
    var enhanceTypeParameterTypesToDefNotNull = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xlink-via-signatures",
        description = "Link JVM IR symbols via signatures, instead of descriptors.\n" +
                "This mode is slower, but can be useful in troubleshooting problems with the JVM IR backend"
    )
    var linkViaSignatures = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xdebug",
        description = "Enable debug mode for compilation.\n" +
                "Currently this includes spilling all variables in a suspending context regardless their liveness."
    )
    var enableDebugMode = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xno-new-java-annotation-targets",
        description = "Do not generate Java 1.8+ targets for Kotlin annotation classes"
    )
    var noNewJavaAnnotationTargets = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xuse-old-innerclasses-logic",
        description = "Use old logic for generation of InnerClasses attributes.\n" +
                "This option is deprecated and will be deleted in future versions."
    )
    var oldInnerClassesLogic = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xkonstue-classes",
        description = "Enable experimental konstue classes"
    )
    var konstueClasses = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xir-inliner",
        description = "Inline functions using IR inliner instead of bytecode inliner"
    )
    var enableIrInliner: Boolean = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }


    override fun configureAnalysisFlags(collector: MessageCollector, languageVersion: LanguageVersion): MutableMap<AnalysisFlag<*>, Any> {
        konst result = super.configureAnalysisFlags(collector, languageVersion)
        result[JvmAnalysisFlags.strictMetadataVersionSemantics] = strictMetadataVersionSemantics
        result[JvmAnalysisFlags.javaTypeEnhancementState] = JavaTypeEnhancementStateParser(collector, languageVersion.toKotlinVersion())
            .parse(jsr305, supportCompatqualCheckerFrameworkAnnotations, jspecifyAnnotations, nullabilityAnnotations)
        result[AnalysisFlags.ignoreDataFlowInAssert] = JVMAssertionsMode.fromString(assertionsMode) != JVMAssertionsMode.LEGACY
        JvmDefaultMode.fromStringOrNull(jvmDefault)?.let {
            result[JvmAnalysisFlags.jvmDefaultMode] = it
        } ?: collector.report(
            CompilerMessageSeverity.ERROR,
            "Unknown -Xjvm-default mode: $jvmDefault, supported modes: ${
                JvmDefaultMode.konstues().mapNotNull { mode ->
                    mode.description.takeIf { JvmDefaultMode.fromStringOrNull(it) != null }
                }
            }"
        )
        result[JvmAnalysisFlags.inheritMultifileParts] = inheritMultifileParts
        result[JvmAnalysisFlags.sanitizeParentheses] = sanitizeParentheses
        result[JvmAnalysisFlags.suppressMissingBuiltinsError] = suppressMissingBuiltinsError
        result[JvmAnalysisFlags.enableJvmPreview] = enableJvmPreview
        result[AnalysisFlags.allowUnstableDependencies] = allowUnstableDependencies || useK2 || languageVersion.usesK2
        result[JvmAnalysisFlags.disableUltraLightClasses] = disableUltraLightClasses
        result[JvmAnalysisFlags.useIR] = !useOldBackend
        return result
    }

    override fun configureLanguageFeatures(collector: MessageCollector): MutableMap<LanguageFeature, LanguageFeature.State> {
        konst result = super.configureLanguageFeatures(collector)
        if (typeEnhancementImprovementsInStrictMode) {
            result[LanguageFeature.TypeEnhancementImprovementsInStrictMode] = LanguageFeature.State.ENABLED
        }
        if (enhanceTypeParameterTypesToDefNotNull) {
            result[LanguageFeature.ProhibitUsingNullableTypeParameterAgainstNotNullAnnotated] = LanguageFeature.State.ENABLED
        }
        if (JvmDefaultMode.fromStringOrNull(jvmDefault)?.forAllMethodsWithBody == true) {
            result[LanguageFeature.ForbidSuperDelegationToAbstractFakeOverride] = LanguageFeature.State.ENABLED
            result[LanguageFeature.AbstractClassMemberNotImplementedWithIntermediateAbstractClass] = LanguageFeature.State.ENABLED
        }
        if (konstueClasses) {
            result[LanguageFeature.ValueClasses] = LanguageFeature.State.ENABLED
        }
        return result
    }

    override fun defaultLanguageVersion(collector: MessageCollector): LanguageVersion =
        if (useOldBackend) {
            if (!suppressVersionWarnings) {
                collector.report(
                    CompilerMessageSeverity.STRONG_WARNING,
                    "Language version is automatically inferred to ${LanguageVersion.KOTLIN_1_5.versionString} when using " +
                            "the old JVM backend. Consider specifying -language-version explicitly, or remove -Xuse-old-backend"
                )
            }
            LanguageVersion.KOTLIN_1_5
        } else super.defaultLanguageVersion(collector)

    override fun checkPlatformSpecificSettings(languageVersionSettings: LanguageVersionSettings, collector: MessageCollector) {
        if (useOldBackend && languageVersionSettings.languageVersion >= LanguageVersion.KOTLIN_1_6) {
            collector.report(
                CompilerMessageSeverity.ERROR,
                "Old JVM backend does not support language version 1.6 or above. " +
                        "Please use language version 1.5 or below, or remove -Xuse-old-backend"
            )
        }
        if (oldInnerClassesLogic) {
            collector.report(
                CompilerMessageSeverity.WARNING,
                "The -Xuse-old-innerclasses-logic option is deprecated and will be deleted in future versions."
            )
        }
    }

    override fun copyOf(): Freezable = copyK2JVMCompilerArguments(this, K2JVMCompilerArguments())
}
