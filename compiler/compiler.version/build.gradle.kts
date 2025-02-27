import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    java
    id("jps-compatible")
}

// This module does not apply Kotlin plugin, so we are setting toolchain via
// java extension
configureJavaOnlyToolchain(JdkMajorVersion.JDK_1_8)

konst kotlinVersion: String by rootProject.extra

dependencies {
    compileOnly("org.jetbrains:annotations:13.0")
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

tasks.named<ProcessResources>("processResources") {
    konst kotlinVersionLocal = kotlinVersion
    inputs.property("compilerVersion", kotlinVersionLocal)
    filesMatching("META-INF/compiler.version") {
        filter<ReplaceTokens>("tokens" to mapOf("snapshot" to kotlinVersionLocal))
    }
}
