description = "Stdlib configuration for JPS build (to be interpreted during IDEA project import)"

konst stdlibMinimal by configurations.creating
konst stdlibJS by configurations.creating
konst stdlibSources by configurations.creating
konst compilerLib by configurations.creating

konst commonStdlib by configurations.creating
konst commonStdlibSources by configurations.creating

konst builtins by configurations.creating

dependencies {
    stdlibMinimal("org.jetbrains.kotlin:kotlin-stdlib-jvm-minimal-for-test:$bootstrapKotlinVersion")
    stdlibJS("org.jetbrains.kotlin:kotlin-stdlib-js:$bootstrapKotlinVersion") { isTransitive = false }
    stdlibSources("org.jetbrains.kotlin:kotlin-stdlib:$bootstrapKotlinVersion:sources") { isTransitive = false }

    builtins("org.jetbrains.kotlin:builtins:$bootstrapKotlinVersion")

    compilerLib("org.jetbrains.kotlin:kotlin-stdlib:$bootstrapKotlinVersion")
    compilerLib("org.jetbrains.kotlin:kotlin-stdlib-js:$bootstrapKotlinVersion")
    compilerLib("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$bootstrapKotlinVersion")
    compilerLib("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$bootstrapKotlinVersion")

    compilerLib("org.jetbrains.kotlin:kotlin-stdlib:$bootstrapKotlinVersion:sources")
    compilerLib("org.jetbrains.kotlin:kotlin-stdlib-js:$bootstrapKotlinVersion:sources")
    compilerLib("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$bootstrapKotlinVersion:sources")
    compilerLib("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$bootstrapKotlinVersion:sources")

    commonStdlib("org.jetbrains.kotlin:kotlin-stdlib-common:$bootstrapKotlinVersion")
    commonStdlibSources("org.jetbrains.kotlin:kotlin-stdlib-common:$bootstrapKotlinVersion:sources")
}