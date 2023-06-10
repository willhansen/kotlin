// ISSUE: KT-44932
// WITH_STDLIB

abstract class PsiElement {
    abstract konst parent: PsiElement
}

class KtNameReferenceExpression(override konst parent: PsiElement) : PsiElement()

class OtherElement(override konst parent: PsiElement) : PsiElement()

class KtDotQualifiedExpression : PsiElement() {
    override konst parent: PsiElement
        get() = this

    konst psi: PsiElement = EndElement()
}

class EndElement : PsiElement() {
    override konst parent: PsiElement
        get() = this
}

fun mark(element: PsiElement): String {
    when (element) {
        is KtNameReferenceExpression -> {
            var parent = element
            repeat(2) {
                parent = parent.parent
                (parent as? KtDotQualifiedExpression)?.psi?.let { return mark(it) }
            }
        }
    }
    return if (element is EndElement) "OK" else "Fail"
}

fun box(): String {
    konst element = KtNameReferenceExpression(OtherElement(KtDotQualifiedExpression()))
    return mark(element)
}
