@file:Suppress("HasPlatformType")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.binary-compatibility-konstidator")
}

kotlin {
    sourceSets.all {
        languageSettings.optIn("org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi")
    }
}

konst embedded by configurations.getting {
    isTransitive = false
    configurations.getByName("compileOnly").extendsFrom(this)
    configurations.getByName("testImplementation").extendsFrom(this)
}

dependencies {
    api(project(":kotlin-gradle-plugin-idea"))
    embedded("com.google.protobuf:protobuf-java:3.21.9")
    embedded("com.google.protobuf:protobuf-kotlin:3.21.9")
    testImplementation(project(":kotlin-test:kotlin-test-junit"))
    testImplementation(kotlin("reflect"))
    testImplementation(testFixtures(project(":kotlin-gradle-plugin-idea")))
}

configureKotlinCompileTasksGradleCompatibility()

sourceSets.main.configure {
    java.srcDir("src/generated/java")
    java.srcDir("src/generated/kotlin")
}

publish()

javadocJar()
sourcesJar()
runtimeJar(tasks.register<ShadowJar>("embeddable")) {
    from(mainSourceSet.output)
    exclude("**/*.proto")
    relocate("com.google.protobuf", "org.jetbrains.kotlin.gradle.idea.proto.com.google.protobuf")
}

/* Setup configuration for binary compatibility tests */
run {
    konst binaryValidationApiJar = tasks.register<Jar>("binaryValidationApiJar") {
        this.archiveBaseName.set(project.name + "-api")
        from(mainSourceSet.output)
    }

    apiValidation {
        ignoredPackages += "org.jetbrains.kotlin.gradle.idea.proto.generated"
        nonPublicMarkers += "org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi"
    }

    tasks {
        apiBuild {
            inputJar.konstue(binaryValidationApiJar.flatMap { it.archiveFile })
        }
    }
}

/* Setup protoc */
tasks.register<Exec>("protoc") {
    konst protoSources = file("src/main/proto")
    konst javaOutput = file("src/generated/java/")
    konst kotlinOutput = file("src/generated/kotlin/")

    inputs.dir(protoSources)
    outputs.dir(javaOutput)
    outputs.dir(kotlinOutput)

    doFirst {
        javaOutput.deleteRecursively()
        kotlinOutput.deleteRecursively()
        javaOutput.mkdirs()
        kotlinOutput.mkdirs()
    }

    workingDir(project.projectDir)

    commandLine(
        *arrayOf(
            "protoc",
            "-I=$protoSources",
            "--java_out=${javaOutput.absolutePath}",
            "--kotlin_out=${kotlinOutput.absolutePath}"
        ) + protoSources.listFiles().orEmpty()
            .filter { it.extension == "proto" }
            .map { it.path },
    )
}


/* Setup backwards compatibility tests */
run {
    konst compatibilityTestClasspath by configurations.creating {
        isCanBeResolved = true
        isCanBeConsumed = false
        attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
    }

    dependencies {
        compatibilityTestClasspath(project(":kotlin-gradle-plugin-idea-for-compatibility-tests"))
    }

    tasks.test {
        konst capturedCompatibilityTestClasspath: FileCollection = compatibilityTestClasspath
        dependsOn(capturedCompatibilityTestClasspath)
        inputs.files(capturedCompatibilityTestClasspath)
        doFirst {
            systemProperty("compatibilityTestClasspath", capturedCompatibilityTestClasspath.files.joinToString(";") { it.absolutePath })
        }
    }
}

