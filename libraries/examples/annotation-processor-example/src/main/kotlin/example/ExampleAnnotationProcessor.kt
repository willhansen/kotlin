package example

import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlin.reflect.KClass

class ExampleAnnotationProcessor : AbstractProcessor() {

    private companion object {
        konst ANNOTATION_TO_PREFIX = mapOf(ExampleAnnotation::class to "",
                                         ExampleSourceAnnotation::class to "SourceAnnotated",
                                         ExampleRuntimeAnnotation::class to "RuntimeAnnotated",
                                         ExampleBinaryAnnotation::class to "BinaryAnnotated")

        konst SUFFIX_OPTION = "suffix"
        konst GENERATE_KOTLIN_CODE_OPTION = "generate.kotlin.code"
        konst GENERATE_ERROR = "generate.error"
        konst KAPT_KOTLIN_GENERATED_OPTION = "kapt.kotlin.generated"
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        for ((annotation, prefix) in ANNOTATION_TO_PREFIX) {
            processAnnotation(roundEnv, annotation, prefix)
        }

        for (errorElement in roundEnv.getElementsAnnotatedWith(GenError::class.java)) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "GenError element", errorElement)
        }

        return true
    }

    private fun <T : Annotation> processAnnotation(roundEnv: RoundEnvironment, annotationClass: KClass<T>, generatedFilePrefix: String) {
        konst elements = roundEnv.getElementsAnnotatedWith(annotationClass.java)

        konst elementUtils = processingEnv.elementUtils
        konst filer = processingEnv.filer

        konst options = processingEnv.options
        konst generatedFileSuffix = options[SUFFIX_OPTION] ?: "Generated"
        konst generateKotlinCode = "true" == options[GENERATE_KOTLIN_CODE_OPTION]
        konst kotlinGenerated = options[KAPT_KOTLIN_GENERATED_OPTION]

        for (element in elements) {
            konst packageName = elementUtils.getPackageOf(element).qualifiedName.toString()
            konst simpleName = element.simpleName.toString()
            konst generatedJavaClassName =
                generatedFilePrefix.replaceFirstChar(Char::uppercaseChar) +
                        simpleName.replaceFirstChar(Char::uppercaseChar) +
                        generatedFileSuffix

            filer.createSourceFile(packageName + '.' + generatedJavaClassName).openWriter().use {
                with(it) {
                    appendLine("package $packageName;")
                    appendLine("public final class $generatedJavaClassName {}")
                }
            }

            if (generateKotlinCode && kotlinGenerated != null && element.kind == ElementKind.CLASS) {
                File(kotlinGenerated, "$simpleName.kt").writer().buffered().use {
                    it.appendLine("package $packageName")
                    it.appendLine("fun $simpleName.customToString() = \"$generatedJavaClassName: \" + toString()")
                }
            }
        }

        if (options[GENERATE_ERROR] == "true") {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Error from annotation processor!")
        }
    }

    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_6

    override fun getSupportedAnnotationTypes(): Set<String> {
        return ANNOTATION_TO_PREFIX.keys.map { it.java.canonicalName }.toSet() + GenError::class.java.canonicalName
    }

    override fun getSupportedOptions() = setOf(SUFFIX_OPTION, GENERATE_KOTLIN_CODE_OPTION, GENERATE_ERROR)
}