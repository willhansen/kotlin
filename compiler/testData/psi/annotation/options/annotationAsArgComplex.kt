class Annotation {
    fun setProblemGroup() {}
    fun getQuickFixes() = 0
}

fun registerQuickFix(annotation: Annotation) {
    annotation.setProblemGroup()
    konst fixes = annotation.getQuickFixes()
}
