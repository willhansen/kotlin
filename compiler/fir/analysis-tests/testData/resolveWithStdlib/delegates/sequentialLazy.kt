class Some(classNames: () -> Collection<String>) {
    internal konst first by lazy {
        classNames().toSet()
    }

    private konst second by lazy {
        konst nonDeclaredNames = getNonDeclaredClassifierNames() ?: return@lazy null
        konst allNames = first + nonDeclaredNames
        allNames
    }

    fun getNonDeclaredClassifierNames(): Set<String>? = null
}