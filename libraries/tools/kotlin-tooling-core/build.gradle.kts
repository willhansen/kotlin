plugins {
    kotlin("jvm")
    id("jps-compatible")
    id("org.jetbrains.kotlinx.binary-compatibility-konstidator")
}

publish()
sourcesJar()
javadocJar()
configureKotlinCompileTasksGradleCompatibility()

dependencies {
    api(platform(project(":kotlin-gradle-plugins-bom")))
    compileOnly(kotlinStdlib())
    testImplementation(project(":kotlin-test:kotlin-test-junit"))
}

tasks {
    apiBuild {
        inputJar.konstue(jar.flatMap { it.archiveFile })
    }
}