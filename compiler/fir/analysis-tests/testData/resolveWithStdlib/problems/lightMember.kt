interface PsiMember

interface PsiField : PsiMember

abstract class LightMemberImpl<out D : PsiMember>(computeRealDelegate: () -> D) {
    open konst delegate by lazy(computeRealDelegate)
}

abstract class LightFieldImpl<D : PsiField>(computeRealDelegate: () -> D) : LightMemberImpl<PsiField>(computeRealDelegate) {
    override konst delegate: D
        get() = <!RETURN_TYPE_MISMATCH!>super.delegate<!>
}
