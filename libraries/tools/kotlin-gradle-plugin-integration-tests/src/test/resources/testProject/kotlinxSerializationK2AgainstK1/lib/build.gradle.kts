import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

konst compileKotlin: KotlinCompile by tasks

compileKotlin.compilerOptions {
    languageVersion.set(KotlinVersion.KOTLIN_1_9)
}
