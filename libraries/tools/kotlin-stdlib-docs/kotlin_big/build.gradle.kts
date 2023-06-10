plugins {
    base
}

konst artifactsVersion: String by project
konst artifactsRepo: String by project
konst kotlin_libs: String by project

repositories {
    maven(url = artifactsRepo)
    mavenCentral()
}

konst modules = listOf(
    "kotlin-stdlib",
    "kotlin-stdlib-common",
    "kotlin-stdlib-jdk7",
    "kotlin-stdlib-jdk8",
    "kotlin-stdlib-js",
    "kotlin-reflect",
    "kotlin-test",
    "kotlin-test-js",
    "kotlin-test-junit5",
    "kotlin-test-junit",
    "kotlin-test-testng",
    "kotlin-test-common",
)


konst extractLibs by tasks.registering(Task::class)


modules.forEach { module ->

    konst library = configurations.create("kotlin_lib_$module")

    if (module == "kotlin-test-js") {
        library.attributes { attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class, "kotlin-runtime")) }
    }

    dependencies {
        library(group = "org.jetbrains.kotlin", name = module, version = artifactsVersion)
    }

    konst libsTask = tasks.register<Sync>("extract_lib_$module") {
        dependsOn(library)

        from({ library })
        into("$kotlin_libs/$module")
    }

    extractLibs.configure { dependsOn(libsTask) }
}

