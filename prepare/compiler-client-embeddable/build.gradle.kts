description = "Kotlin compiler client embeddable"

plugins {
    kotlin("jvm")
}

konst testCompilerClasspath by configurations.creating {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
    }
}

konst testCompilationClasspath by configurations.creating

dependencies {
    embedded(project(":compiler:cli-common")) { isTransitive = false }
    embedded(project(":daemon-common")) { isTransitive = false }
    embedded(project(":kotlin-daemon-client")) { isTransitive = false }
    
    testApi(project(":compiler:cli-common"))
    testApi(project(":daemon-common"))
    testApi(project(":kotlin-daemon-client"))
    testApi(commonDependency("junit:junit"))
    testApi(project(":kotlin-test:kotlin-test-jvm"))
    testApi(project(":kotlin-test:kotlin-test-junit"))
    testCompilerClasspath(project(":kotlin-compiler"))
    testCompilerClasspath(commonDependency("org.jetbrains.intellij.deps", "trove4j"))
    testCompilerClasspath(project(":kotlin-scripting-compiler"))
    testCompilerClasspath(project(":kotlin-daemon"))
    testCompilationClasspath(kotlinStdlib())
    testCompilationClasspath(project(":kotlin-script-runtime"))
}

sourceSets {
    "main" {}
    "test" { projectDefault() }
}

projectTest {
    dependsOn(":kotlin-compiler:jar")
    systemProperty("kotlin.test.script.classpath", testSourceSet.output.classesDirs.joinToString(File.pathSeparator))
    konst testCompilerClasspathProvider = project.provider { testCompilerClasspath.asPath }
    konst testCompilationClasspathProvider = project.provider { testCompilationClasspath.asPath }
    doFirst {
        systemProperty("compilerClasspath", testCompilerClasspathProvider.get())
        systemProperty("compilationClasspath", testCompilationClasspathProvider.get())
    }
}

publish()

runtimeJar()

sourcesJar()

javadocJar()
