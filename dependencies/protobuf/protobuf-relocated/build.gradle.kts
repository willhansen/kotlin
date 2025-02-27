import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.File

plugins {
    `java-base`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "4.0.3" apply false
}

repositories {
    mavenCentral()
}

konst baseProtobuf by configurations.creating
konst baseProtobufSources by configurations.creating

konst protobufVersion: String by rootProject.extra
konst protobufJarPrefix = "protobuf-$protobufVersion"

konst renamedSources = "$buildDir/renamedSrc/"
konst outputJarsPath = "$buildDir/libs"

dependencies {
    baseProtobuf("com.google.protobuf:protobuf-java:$protobufVersion")
    baseProtobufSources("com.google.protobuf:protobuf-java:$protobufVersion:sources")
}

konst prepare = tasks.register<ShadowJar>("prepare") {
    destinationDirectory.set(File(outputJarsPath))
    archiveVersion.set(protobufVersion)
    archiveClassifier.set("")
    from(
        provider {
            baseProtobuf.files.find { it.name.startsWith("protobuf-java") }?.canonicalPath
        }
    )

    relocate("com.google.protobuf", "org.jetbrains.kotlin.protobuf" ) {
        exclude("META-INF/maven/com.google.protobuf/protobuf-java/pom.properties")
    }
}

artifacts.add("default", prepare)

konst relocateSources = task<Copy>("relocateSources") {
    from(
        provider {
            zipTree(baseProtobufSources.files.find { it.name.startsWith("protobuf-java") && it.name.endsWith("-sources.jar") }
                        ?: throw GradleException("sources jar not found among ${baseProtobufSources.files}"))
        }
    )

    into(renamedSources)

    filter { it.replace("com.google.protobuf", "org.jetbrains.kotlin.protobuf") }
}

konst prepareSources = task<Jar>("prepareSources") {
    destinationDirectory.set(File(outputJarsPath))
    archiveVersion.set(protobufVersion)
    archiveClassifier.set("sources")
    from(relocateSources)
}

artifacts.add("default", prepareSources)

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(prepare)
            artifact(prepareSources)
        }
    }

    repositories {
        maven {
            url = uri("${rootProject.buildDir}/internal/repo")
        }
        maven {
            name = "kotlinSpace"
            url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-dependencies")
            credentials(org.gradle.api.artifacts.repositories.PasswordCredentials::class)
        }
    }
}
