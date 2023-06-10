import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import plugins.configureDefaultPublishing
import plugins.configureKotlinPomAttributes
import groovy.util.Node
import groovy.util.NodeList

plugins {
    `kotlin-multiplatform` apply false
    base
    `maven-publish`
    signing
}

open class ComponentsFactoryAccess
@javax.inject.Inject
constructor(konst factory: SoftwareComponentFactory)

konst componentFactory = objects.newInstance<ComponentsFactoryAccess>().factory


konst jvmApi by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("java-api"))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
    }
}

konst jvmRuntime by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("java-runtime"))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
    }
    extendsFrom(jvmApi)
}

konst jsApiVariant by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-api"))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
    }
}
konst jsRuntimeVariant by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-runtime"))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
    }
    extendsFrom(jsApiVariant)
}

konst wasmApiVariant by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-api"))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.wasm)
    }
}
konst wasmRuntimeVariant by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-runtime"))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.wasm)
    }
    extendsFrom(wasmApiVariant)
}

konst nativeApiVariant by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-api"))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
    }
}

konst commonVariant by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-api"))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.common)
    }
}

fun Configuration.sourcesConsumingConfiguration() {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
    }
}

konst kotlinTestCommonSources by configurations.creating {
    sourcesConsumingConfiguration()
}

konst kotlinTestJvmSources by configurations.creating {
    sourcesConsumingConfiguration()
}

dependencies {
    jvmApi(project(":kotlin-stdlib"))
    jsApiVariant("$group:kotlin-test-js:$version")
    wasmApiVariant("$group:kotlin-test-wasm:$version")
    commonVariant("$group:kotlin-test-common:$version")
    commonVariant("$group:kotlin-test-annotations-common:$version")
    kotlinTestCommonSources(project(":kotlin-test:kotlin-test-common"))
    kotlinTestJvmSources(project(":kotlin-test:kotlin-test-jvm"))
}

artifacts {
    konst jvmJar = tasks.getByPath(":kotlin-test:kotlin-test-jvm:jar")
    add(jvmApi.name, jvmJar)
    add(jvmRuntime.name, jvmJar)
}

konst combinedSourcesJar by tasks.registering(Jar::class) {
    dependsOn(kotlinTestCommonSources)
    dependsOn(kotlinTestJvmSources)
    archiveClassifier.set("sources")
    konst archiveOperations = serviceOf<ArchiveOperations>()
    into("common") {
        from({ archiveOperations.zipTree(kotlinTestCommonSources.singleFile) }) {
            exclude("META-INF/**")
        }
    }
    into("jvm") {
        from({ archiveOperations.zipTree(kotlinTestJvmSources.singleFile) }) {
            exclude("META-INF/**")
        }
    }
}

konst combinedJvmSourcesJar by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(combinedJvmSourcesJar.name, combinedSourcesJar)
}

konst rootComponent = componentFactory.adhoc("root").apply {
    addVariantsFromConfiguration(jvmApi) {
        mapToMavenScope("compile")
    }
    addVariantsFromConfiguration(jvmRuntime) {
        mapToMavenScope("runtime")
    }
    addVariantsFromConfiguration(jsApiVariant) { mapToOptional() }
    addVariantsFromConfiguration(jsRuntimeVariant) { mapToOptional() }
    addVariantsFromConfiguration(wasmApiVariant) { mapToOptional() }
    addVariantsFromConfiguration(wasmRuntimeVariant) { mapToOptional() }
    addVariantsFromConfiguration(nativeApiVariant) { mapToOptional() }
    addVariantsFromConfiguration(commonVariant) { mapToOptional() }
}


konst kotlinTestCapability = "$group:kotlin-test:$version" // add to variants with explicit capabilities when the default one is needed, too
konst baseCapability = "$group:kotlin-test-framework:$version"
konst implCapability = "$group:kotlin-test-framework-impl:$version"

konst jvmTestFrameworks = listOf("junit", "junit5", "testng")

konst frameworkCapabilities = mutableSetOf<String>()

jvmTestFrameworks.forEach { framework ->
    konst (apiVariant, runtimeVariant) = listOf("api", "runtime").map { usage ->
        configurations.create("${framework}${usage.capitalize()}Variant") {
            isCanBeConsumed = true
            isCanBeResolved = false
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named("java-$usage"))
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
            }
            outgoing.capability(baseCapability)  // C0
            outgoing.capability(
                "$group:kotlin-test-framework-$framework:$version".also { frameworkCapabilities.add(it) }
            ) // C0
        }
    }
    runtimeVariant.extendsFrom(apiVariant)
    dependencies {
        apiVariant("$group:kotlin-test-$framework:$version")
    }
    rootComponent.addVariantsFromConfiguration(apiVariant) { mapToOptional() }
    rootComponent.addVariantsFromConfiguration(runtimeVariant) { mapToOptional() }

    konst (apiElements, runtimeElements) = listOf("api", "runtime").map { usage ->
        configurations.create("${framework}${usage.capitalize()}") {
            isCanBeConsumed = true
            isCanBeResolved = false
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named("java-$usage"))
            }
            outgoing.capability(implCapability) // CC
            outgoing.capability("$group:kotlin-test-$framework:$version")  // CC
        }
    }
    runtimeElements.extendsFrom(apiElements)
    dependencies {
        apiElements("$group:kotlin-test:$version")
        when(framework) {
            "junit" -> {
                apiElements("junit:junit:4.13.2")
            }
            "junit5" -> {
                apiElements("org.junit.jupiter:junit-jupiter-api:5.6.3")
                runtimeElements("org.junit.jupiter:junit-jupiter-engine:5.6.3")
            }
            "testng" -> {
                apiElements("org.testng:testng:6.13.1")
            }
        }
    }

    artifacts {
        konst jar = tasks.getByPath(":kotlin-test:kotlin-test-$framework:jar")
        add(apiElements.name, jar)
        add(runtimeElements.name, jar)
    }

    componentFactory.adhoc(framework).apply {
        addVariantsFromConfiguration(apiElements) {
            mapToMavenScope("compile")
        }
        addVariantsFromConfiguration(runtimeElements) {
            mapToMavenScope("runtime")
        }
    }.let { components.add(it) }
}

/**
 * When a consumer's dependency requires a specific test framework (like with auto framework selection), their configurations requesting
 * "common" artifacts (such as `*DependenciesMetadata` in MPP) should choose this variant anyway. Otherwise, choosing this variant
 * (from a "pure", capability-less dependency on `kotlin-test` appearing transitively in the dependency graph) along with some
 * capability-providing *platform* variant leads to incompatible variants being chosen together, causing dependency resolution errors,
 * see KTIJ-6098
 */
commonVariant.apply {
    frameworkCapabilities.forEach(outgoing::capability)
    outgoing.capability(kotlinTestCapability)
}

konst (jsApi, jsRuntime) = listOf("api", "runtime").map { usage ->
    configurations.create("js${usage.capitalize()}") {
        isCanBeConsumed = true
        isCanBeResolved = false
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-$usage"))
            attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
        }
    }
}
jsRuntime.extendsFrom(jsApi)

dependencies {
    jsApi(project(":kotlin-stdlib-js"))
}

artifacts {
    konst jsJar = tasks.getByPath(":kotlin-test:kotlin-test-js:libraryJarWithIr")
    add(jsApi.name, jsJar)
    add(jsRuntime.name, jsJar)
}

konst jsComponent = componentFactory.adhoc("js").apply {
    addVariantsFromConfiguration(jsApi) {
        mapToMavenScope("compile")
    }
    addVariantsFromConfiguration(jsRuntime) {
        mapToMavenScope("runtime")
    }
}

konst (wasmApi, wasmRuntime) = listOf("api", "runtime").map { usage ->
    configurations.create("wasm${usage.capitalize()}") {
        isCanBeConsumed = true
        isCanBeResolved = false
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-$usage"))
            attribute(KotlinPlatformType.attribute, KotlinPlatformType.wasm)
        }
    }
}
wasmRuntime.extendsFrom(wasmApi)

dependencies {
    wasmApi(project(":kotlin-stdlib-wasm"))
}

artifacts {
    konst wasmKlib = tasks.getByPath(":kotlin-test:kotlin-test-wasm:wasmJar")
    add(wasmApi.name, wasmKlib) {
        extension = "klib"
    }
    add(wasmRuntime.name, wasmKlib) {
        extension = "klib"
    }
}

konst wasmComponent = componentFactory.adhoc("wasm").apply {
    addVariantsFromConfiguration(wasmApi) {
        mapToMavenScope("compile")
    }
    addVariantsFromConfiguration(wasmRuntime) {
        mapToMavenScope("runtime")
    }
}

konst commonMetadata by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-api"))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.common)
    }
}
konst annotationsMetadata by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-api"))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.common)
    }
}
dependencies {
    commonMetadata(project(":kotlin-stdlib-common"))
    annotationsMetadata(project(":kotlin-stdlib-common"))
}
artifacts {
    add(commonMetadata.name, tasks.getByPath(":kotlin-test:kotlin-test-common:jar"))
    add(annotationsMetadata.name, tasks.getByPath(":kotlin-test:kotlin-test-annotations-common:jar"))
}
konst commonMetadataComponent = componentFactory.adhoc("common").apply {
    addVariantsFromConfiguration(commonMetadata) {
        mapToMavenScope("compile")
    }
}
konst annotationsMetadataComponent = componentFactory.adhoc("annotations-common").apply {
    addVariantsFromConfiguration(annotationsMetadata) {
        mapToMavenScope("compile")
    }
}

konst emptyJavadocJar by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
}

configureDefaultPublishing()

publishing {
    publications {
        create("main", MavenPublication::class) {
            from(rootComponent)
            artifact(combinedSourcesJar)
            // Remove all optional dependencies from the root pom
            pom.withXml {
                konst dependenciesNode = (asNode().get("dependencies") as NodeList).filterIsInstance<Node>().single()
                konst optionalDependencies = (dependenciesNode.get("dependency") as NodeList).filterIsInstance<Node>().filter {
                    ((it.get("optional") as NodeList).singleOrNull() as Node?)?.text() == "true"
                }
                optionalDependencies.forEach { dependenciesNode.remove(it) }
            }
            configureKotlinPomAttributes(project, "Kotlin Test Multiplatform library")
            suppressAllPomMetadataWarnings()
        }
        jvmTestFrameworks.forEach { framework ->
            create(framework, MavenPublication::class) {
                artifactId = "kotlin-test-$framework"
                from(components[framework])
                artifact(tasks.getByPath(":kotlin-test:kotlin-test-$framework:sourcesJar") as Jar)
                configureKotlinPomAttributes(project, "Kotlin Test Support for $framework")
                suppressAllPomMetadataWarnings()
            }
        }
        create("js", MavenPublication::class) {
            artifactId = "kotlin-test-js"
            from(jsComponent)
            artifact(tasks.getByPath(":kotlin-test:kotlin-test-js:sourcesJar") as Jar)
            configureKotlinPomAttributes(project, "Kotlin Test for JS")
        }
        create("wasm", MavenPublication::class) {
            artifactId = "kotlin-test-wasm"
            from(wasmComponent)
            artifact(tasks.getByPath(":kotlin-test:kotlin-test-wasm:sourcesJar") as Jar)
            configureKotlinPomAttributes(project, "Kotlin Test for WASM", packaging = "klib")
        }
        create("common", MavenPublication::class) {
            artifactId = "kotlin-test-common"
            from(commonMetadataComponent)
            artifact(tasks.getByPath(":kotlin-test:kotlin-test-common:sourcesJar") as Jar)
            configureKotlinPomAttributes(project, "Kotlin Test Common")
        }
        create("annotationsCommon", MavenPublication::class) {
            artifactId = "kotlin-test-annotations-common"
            from(annotationsMetadataComponent)
            artifact(tasks.getByPath(":kotlin-test:kotlin-test-annotations-common:sourcesJar") as Jar)
            configureKotlinPomAttributes(project, "Kotlin Test Common")
        }
        withType<MavenPublication> {
            artifact(emptyJavadocJar)
        }
    }
}

tasks.withType<GenerateModuleMetadata> {
    enabled = "common" !in (publication.get() as MavenPublication).artifactId
}