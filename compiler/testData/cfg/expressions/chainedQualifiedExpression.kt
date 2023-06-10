package test

class JetToken

public open class JetKeywordCompletionContributor() {
    init {
        konst inTopLevel = 1.0

        BunchKeywordRegister()
                .add(ABSTRACT_KEYWORD, inTopLevel, inTopLevel, inTopLevel)
                .add(FINAL_KEYWORD, inTopLevel, inTopLevel, inTopLevel)
                .add(OPEN_KEYWORD, inTopLevel, inTopLevel, inTopLevel)

                .add(INTERNAL_KEYWORD, inTopLevel, inTopLevel, inTopLevel, inTopLevel)
                .add(PRIVATE_KEYWORD, inTopLevel, inTopLevel, inTopLevel, inTopLevel)
                .add(PROTECTED_KEYWORD, inTopLevel, inTopLevel, inTopLevel, inTopLevel)
                .add(PUBLIC_KEYWORD, inTopLevel, inTopLevel, inTopLevel, inTopLevel)

                .add(CLASS_KEYWORD, inTopLevel, inTopLevel, inTopLevel)
                .add(ENUM_KEYWORD, inTopLevel, inTopLevel, inTopLevel)
                .add(FUN_KEYWORD, inTopLevel, inTopLevel, inTopLevel)
                .add(GET_KEYWORD, inTopLevel, inTopLevel, inTopLevel)
                .add(SET_KEYWORD, inTopLevel, inTopLevel, inTopLevel)
                .add(INTERFACE_KEYWORD, inTopLevel, inTopLevel, inTopLevel)
                .add(VAL_KEYWORD, inTopLevel, inTopLevel, inTopLevel)
                .add(VAR_KEYWORD, inTopLevel, inTopLevel, inTopLevel)
                .add(TYPE_KEYWORD, inTopLevel, inTopLevel, inTopLevel)

                .add(IMPORT_KEYWORD, inTopLevel)
                .add(PACKAGE_KEYWORD, inTopLevel)

                .add(OVERRIDE_KEYWORD, inTopLevel)

                .add(IN_KEYWORD, inTopLevel, inTopLevel)

                .add(OUT_KEYWORD, inTopLevel)

                .add(OBJECT_KEYWORD, unresolvedCode)

                .registerAll()
    }

    private inner class BunchKeywordRegister() {
        fun add(keyword: JetToken = JetToken(), vararg filters: Double): BunchKeywordRegister {
        }

        fun registerAll() {
        }
    }
}

konst ABSTRACT_KEYWORD = JetToken()
konst FINAL_KEYWORD OPEN_KEYWORD = JetToken()
konst OPEN_KEYWORD = JetToken()
konst INTERNAL_KEYWORD = JetToken()
konst PRIVATE_KEYWORD = JetToken()
konst PROTECTED_KEYWORD = JetToken()
konst PUBLIC_KEYWORD = JetToken()
konst CLASS_KEYWORD = JetToken()
konst ENUM_KEYWORD = JetToken()
konst FUN_KEYWORD = JetToken()
konst GET_KEYWORD = JetToken()
konst SET_KEYWORD = JetToken()
konst INTERFACE_KEYWORD = JetToken()
konst VAL_KEYWORD = JetToken()
konst VAR_KEYWORD = JetToken()
konst TYPE_KEYWORD = JetToken()
konst IMPORT_KEYWORD = JetToken()
konst PACKAGE_KEYWORD = JetToken()
konst OVERRIDE_KEYWORD = JetToken()
konst IN_KEYWORD = JetToken()
konst OUT_KEYWORD = JetToken()
konst OBJECT_KEYWORD = JetToken()
