// FIR_IDENTICAL

interface ClassId

interface JavaAnnotation {
    konst classId: ClassId?
}

interface JavaAnnotationOwner {
    konst annotations: Collection<JavaAnnotation>
}

interface MapBasedJavaAnnotationOwner : JavaAnnotationOwner {
    konst annotationsByFqNameHash: Map<Int?, JavaAnnotation>
}

fun JavaAnnotationOwner.buildLazyValueForMap() = lazy {
    annotations.associateBy { it.classId?.hashCode() }
}

abstract class BinaryJavaMethodBase(): MapBasedJavaAnnotationOwner {
    override konst annotationsByFqNameHash by buildLazyValueForMap()
}
