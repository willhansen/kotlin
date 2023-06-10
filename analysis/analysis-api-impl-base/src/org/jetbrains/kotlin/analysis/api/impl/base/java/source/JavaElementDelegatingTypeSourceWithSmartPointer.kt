package org.jetbrains.kotlin.analysis.api.impl.base.java.source

import com.intellij.psi.*
import org.jetbrains.kotlin.load.java.structure.impl.source.JavaElementSourceFactory
import org.jetbrains.kotlin.load.java.structure.impl.source.JavaElementTypeSource

internal abstract class JavaElementDelegatingTypeSourceWithSmartPointer<PSI : PsiElement, TYPE : PsiType> : JavaElementTypeSource<TYPE>() {
    abstract konst psiPointer: SmartPsiElementPointer<out PSI>
    abstract fun getType(psi: PSI): TYPE

    override konst type: TYPE
        get() {
            konst psi = psiPointer.element
                ?: error("Cannot restore $psiPointer")
            return getType(psi)
        }
}

internal class JavaElementDelegatingVariableReturnTypeSourceWithSmartPointer<TYPE : PsiType>(
    override konst psiPointer: SmartPsiElementPointer<out PsiVariable>,
    override konst factory: JavaElementSourceFactory,
) : JavaElementDelegatingTypeSourceWithSmartPointer<PsiVariable, TYPE>() {

    override fun getType(psi: PsiVariable): TYPE {
        @Suppress("UNCHECKED_CAST")
        return psi.type as TYPE
    }
}

internal class JavaElementDelegatingMethodReturnTypeSourceWithSmartPointer<TYPE : PsiType>(
    override konst psiPointer: SmartPsiElementPointer<out PsiMethod>,
    override konst factory: JavaElementSourceFactory,
) : JavaElementDelegatingTypeSourceWithSmartPointer<PsiMethod, TYPE>() {

    override fun getType(psi: PsiMethod): TYPE {
        @Suppress("UNCHECKED_CAST")
        return psi.returnType as TYPE
    }
}
internal class JavaElementDelegatingExpressionTypeSourceWithSmartPointer<TYPE : PsiType>(
    override konst psiPointer: SmartPsiElementPointer<out PsiExpression>,
    override konst factory: JavaElementSourceFactory,
) : JavaElementDelegatingTypeSourceWithSmartPointer<PsiExpression, TYPE>() {

    override fun getType(psi: PsiExpression): TYPE {
        @Suppress("UNCHECKED_CAST")
        return psi.type as TYPE
    }
}


internal class JavaElementDelegatingTypeParameterBoundTypeSourceWithSmartPointer<TYPE : PsiType>(
    override konst psiPointer: SmartPsiElementPointer<out PsiTypeParameter>,
    private konst boundIndex: Int,
    override konst factory: JavaElementSourceFactory,
) : JavaElementDelegatingTypeSourceWithSmartPointer<PsiTypeParameter, TYPE>() {

    override fun getType(psi: PsiTypeParameter): TYPE {
        @Suppress("UNCHECKED_CAST")
        return psi.bounds[boundIndex] as TYPE
    }
}
