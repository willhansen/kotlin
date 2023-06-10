// FIR_IDENTICAL

inline fun <reified T : Any> classRefFun() = T::class

inline fun <reified T : Any> Any.classRefExtFun() = T::class

inline konst <reified T : Any> T.classRefExtVal
    get() = T::class


class Host {
    inline fun <reified TF : Any> classRefGenericMemberFun() = TF::class

    inline fun <reified TF : Any> Any.classRefGenericMemberExtFun() = TF::class

    inline konst <reified TV : Any> TV.classRefGenericMemberExtVal
        get() = TV::class
}

