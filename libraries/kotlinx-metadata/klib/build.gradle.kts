import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

description = "Kotlin Library (KLIB) metadata manipulation library"

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

group = "org.jetbrains.kotlinx"

konst deployVersion = findProperty("kotlinxMetadataKlibDeployVersion") as String?
version = deployVersion ?: "0.0.1-SNAPSHOT"

sourceSets {
    "main" { projectDefault() }
}

konst embedded by configurations
embedded.isTransitive = false
configurations.getByName("compileOnly").extendsFrom(embedded)
configurations.getByName("testApi").extendsFrom(embedded)

dependencies {
    api(kotlinStdlib())
    embedded(project(":kotlinx-metadata"))
    embedded(project(":core:compiler.common"))
    embedded(project(":core:metadata"))
    embedded(project(":core:deserialization"))
    embedded(project(":core:deserialization.common"))
    embedded(project(":compiler:serialization"))
    embedded(project(":kotlin-util-klib-metadata"))
    embedded(project(":kotlin-util-klib"))
    embedded(project(":kotlin-util-io"))
    embedded(protobufLite())
}

if (deployVersion != null) {
    publish()
}

runtimeJarWithRelocation {
    from(mainSourceSet.output)
    exclude("**/*.proto")
    relocate("org.jetbrains.kotlin", "kotlinx.metadata.internal")
}

sourcesJar()

javadocJar()
