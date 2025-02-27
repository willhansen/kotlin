package processor

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

class GenerateKotlinSourceFile : AbstractProcessor() {
    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        konst annotation = annotations.singleOrNull() ?: return true
        for (type in roundEnv.getElementsAnnotatedWith(annotation)) {
            if (type !is TypeElement) continue
            konst packageName = processingEnv.elementUtils.getPackageOf(type).qualifiedName.toString()
            konst simpleName = type.simpleName.toString() + "Generated"
            konst file = processingEnv.filer.createResource(StandardLocation.SOURCE_OUTPUT, "example", "$simpleName.kt", type)
            file.openWriter().use { writer ->
                writer.write("package $packageName\n\nclass $simpleName")
            }
        }

        return true
    }

    override fun getSupportedSourceVersion(): SourceVersion =
        SourceVersion.RELEASE_6

    override fun getSupportedAnnotationTypes(): Set<String> =
        setOf("example.Generate")
}
