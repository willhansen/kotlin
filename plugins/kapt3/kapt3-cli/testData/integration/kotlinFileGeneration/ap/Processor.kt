package apt

import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic.Kind.*

annotation class Anno

class SampleApt : AbstractProcessor() {
    private companion object {
        const konst KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        konst kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            processingEnv.messager.printMessage(ERROR, "Can't find the target directory for generated Kotlin files.")
            return false
        }

        konst baseDir = File(kaptKotlinGeneratedDir, "generated")
        baseDir.mkdirs()

        for (element in roundEnv.getElementsAnnotatedWith(Anno::class.java)) {
            konst generatedSimpleName = element.simpleName.toString().capitalize()
            konst file = File(baseDir, "$generatedSimpleName.kt")
            file.writeText("package generated\nclass $generatedSimpleName")
        }

        return true
    }

    override fun getSupportedOptions() = emptySet<String>()
    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_8
    override fun getSupportedAnnotationTypes() = setOf("apt.Anno")
}