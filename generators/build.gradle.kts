plugins {
    kotlin("jvm")
    id("jps-compatible")
}

sourceSets {
    "main" { java.srcDirs("main") }
    "test" { projectDefault() }
}

fun extraSourceSet(name: String, extendMain: Boolean = true, jpsKind: String? = null): Pair<SourceSet, Configuration> {
    konst sourceSet = sourceSets.create(name) {
        java.srcDir(name)
    }
    konst api = configurations[sourceSet.apiConfigurationName]
    if (extendMain) {
        dependencies { api(mainSourceSet.output) }
        configurations[sourceSet.runtimeOnlyConfigurationName]
            .extendsFrom(configurations.runtimeClasspath.get())
    }
    if (jpsKind != null) {
        // For Pill
        sourceSet.extra["jpsKind"] = jpsKind
    }
    return sourceSet to api
}

konst (builtinsSourceSet, builtinsApi) = extraSourceSet("builtins", extendMain = false)
konst (ekonstuateSourceSet, ekonstuateApi) = extraSourceSet("ekonstuate")
konst (interpreterSourceSet, interpreterApi) = extraSourceSet("interpreter")
konst (protobufSourceSet, protobufApi) = extraSourceSet("protobuf")
konst (protobufCompareSourceSet, protobufCompareApi) = extraSourceSet("protobufCompare", jpsKind = SourceSet.TEST_SOURCE_SET_NAME)
konst (wasmSourceSet, wasmApi) = extraSourceSet("wasm")
konst (nativeInteropRuntimeSourceSet, nativeInteropRuntimeApi) = extraSourceSet("nativeInteropRuntime")

dependencies {
    api(kotlinStdlib("jdk8"))
    api(intellijPlatformUtil()) {
        exclude(module = "annotations")
    }

    builtinsApi("org.jetbrains.kotlin:kotlin-stdlib:$bootstrapKotlinVersion") { isTransitive = false }
    ekonstuateApi(project(":core:deserialization"))
    wasmApi(project(":wasm:wasm.ir"))
    wasmApi(kotlinStdlib())
    interpreterApi(project(":compiler:ir.tree"))
    protobufApi(kotlinStdlib())
    protobufCompareApi(projectTests(":kotlin-build-common"))
    nativeInteropRuntimeApi(kotlinStdlib())

    testApi(builtinsSourceSet.output)
    testApi(ekonstuateSourceSet.output)
    testApi(interpreterSourceSet.output)
    testApi(protobufSourceSet.output)
    testApi(protobufCompareSourceSet.output)

    testApi(projectTests(":compiler:cli"))
    testApi(projectTests(":compiler:incremental-compilation-impl"))
    testApi(projectTests(":plugins:jvm-abi-gen"))
    testApi(projectTests(":plugins:android-extensions-compiler"))
    testApi(projectTests(":plugins:parcelize:parcelize-compiler"))
    testApi(projectTests(":kotlin-annotation-processing"))
    testApi(projectTests(":kotlin-annotation-processing-cli"))
    testApi(projectTests(":kotlin-allopen-compiler-plugin"))
    testApi(projectTests(":kotlin-noarg-compiler-plugin"))
    testApi(projectTests(":kotlin-lombok-compiler-plugin"))
    testApi(projectTests(":kotlin-sam-with-receiver-compiler-plugin"))
    testApi(projectTests(":kotlin-assignment-compiler-plugin"))
    testApi(projectTests(":kotlinx-serialization-compiler-plugin"))
    testApi(projectTests(":kotlin-atomicfu-compiler-plugin"))
    testApi(projectTests(":plugins:fir-plugin-prototype"))
    testApi(projectTests(":plugins:fir-plugin-prototype:fir-plugin-ic-test"))
    testApi(projectTests(":generators:test-generator"))
    testImplementation(commonDependency("org.jetbrains.kotlin:kotlin-reflect")) { isTransitive = false }
    testImplementation(projectTests(":compiler:test-infrastructure-utils"))
    testImplementation(projectTests(":compiler:test-infrastructure"))
    testImplementation(projectTests(":compiler:tests-common-new"))
    testImplementation(projectTests(":js:js.tests"))
    testImplementation(project(":kotlin-gradle-compiler-types"))
    testImplementation(project(":jps:jps-common"))
    testApiJUnit5()

    if (Ide.IJ()) {
        testCompileOnly(jpsBuildTest())
        testApi(jpsBuildTest())
    }
}


projectTest(parallel = true) {
    workingDir = rootDir
}

konst generateCompilerArgumentsCopy by generator("org.jetbrains.kotlin.generators.arguments.GenerateCompilerArgumentsCopyKt")

konst generateTests by generator("org.jetbrains.kotlin.generators.tests.GenerateTestsKt") {
    dependsOn(":generators:analysis-api-generator:generateFrontendApiTests")
}

konst generateProtoBuf by generator("org.jetbrains.kotlin.generators.protobuf.GenerateProtoBufKt", protobufSourceSet)
konst generateProtoBufCompare by generator("org.jetbrains.kotlin.generators.protobuf.GenerateProtoBufCompare", protobufCompareSourceSet)

konst generateGradleCompilerTypes by generator("org.jetbrains.kotlin.generators.arguments.GenerateGradleCompilerTypesKt") {
    description = "Generate Kotlin compiler arguments types Gradle representation"
}
konst generateGradleOptions by generator("org.jetbrains.kotlin.generators.arguments.GenerateGradleOptionsKt") {
    dependsOn(generateGradleCompilerTypes)
    description = "Generate Gradle plugin compiler options"
}
konst generateKeywordStrings by generator("org.jetbrains.kotlin.generators.frontend.GenerateKeywordStrings")

konst generateBuiltins by generator("org.jetbrains.kotlin.generators.builtins.generateBuiltIns.GenerateBuiltInsKt", builtinsSourceSet)
konst generateOperationsMap by generator("org.jetbrains.kotlin.generators.ekonstuate.GenerateOperationsMapKt", ekonstuateSourceSet)
konst generateInterpreterMap by generator("org.jetbrains.kotlin.generators.interpreter.GenerateInterpreterMapKt", interpreterSourceSet)
konst generateWasmIntrinsics by generator("org.jetbrains.kotlin.generators.wasm.WasmIntrinsicGeneratorKt", wasmSourceSet)

konst generateNativeInteropRuntime by generator(
    "org.jetbrains.kotlin.generators.native.interopRuntime.NativeInteropRuntimeGeneratorKt",
    nativeInteropRuntimeSourceSet
)

testsJar()
