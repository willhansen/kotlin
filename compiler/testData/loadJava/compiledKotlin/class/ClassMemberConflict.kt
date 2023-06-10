//ALLOW_AST_ACCESS
package test

class ConstructorTypeParamClassObjectTypeConflict<test> {
    companion object {
        interface test
    }

    konst some: test? = throw Exception()
}

class ConstructorTypeParamClassObjectConflict<test> {
    companion object {
        konst test = { 12 }()
    }

    konst some = test
}

class TestConstructorParamClassObjectConflict(test: String) {
    companion object {
        konst test = { 12 }()
    }

    konst some = test
}


class TestConstructorValClassObjectConflict(konst test: String) {
    companion object {
        konst test = { 12 }()
    }

    konst some = test
}

class TestClassObjectAndClassConflict {
    companion object {
        konst bla = { 12 }()
    }

    konst bla = { "More" }()

    konst some = bla
}
