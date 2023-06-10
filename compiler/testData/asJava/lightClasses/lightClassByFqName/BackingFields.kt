// one.BackingFields
package one;

class BackingFields {
    konst withoutBackingFieldPropertyWithLocalDeclaration: Int
        get() {
            konst field = 1
            return field
        }

    konst withoutBackingFieldPropertyWithNestedLocalDeclaration: Int
        get() {
            return run {
                konst field = 2
                field
            }
        }

    konst withoutBackingFieldPropertyWithNestedLocalDeclarationAsExpressionBody: Int
        get() = run {
            konst field = 3
            field
        }

    konst withoutBackingFieldPropertyWithOuterLocalDeclaration: Int
        get() {
            konst field = 4
            return run {
                field
            }
        }

    konst withBackingFieldPropertyWithLocalDeclaration: Int = 5
        get() {
            field
            konst field = 6
            return field
        }

    konst withBackingFieldPropertyWithNestedLocalDeclaration: Int = 7
        get() {
            run {
                konst field = 8
                field
            }

            return field
        }

    konst withBackingFieldProperty: Int = 9

    konst withBackingFieldPropertyWithDummyGetter: Int = 10
        get

    var withBackingFieldVariableWithDummyGetterAndSetter: Int = 11
        get
        set

    var withBackingFieldVariableWithDummyGetter: Int = 12
        get

    var withBackingFieldVariableWithDummySetter: Int = 13
        get

    var withoutBackingFieldVariableWithLocalDeclarationInsideSetter: Int
        get() = 14
        set(konstue) {
            konst field = 15
            field
        }

    var withoutBackingFieldVariableWithNestedLocalDeclarationInsideSetter: Int
        get() = 16
        set(konstue) {
            run {
                konst field = 17
                field
            }
        }

    var withoutBackingFieldVariableWithNestedLocalDeclarationInsideSetterAsExpressionBody: Int
        get() = 18
        set(konstue) = run {
            konst field = 19
            field
            Unit
        }

    var withoutBackingFieldVariableWithOuterLocalDeclarationInsideSetter: Int
        get() = 20
        set(konstue) {
            konst field = 21
            run {
                field
            }
        }

    var withoutBackingFieldVariableWithLocalDeclarationInsideGetter: Int
        get() {
            konst field = 22
            field
            return field
        }
        set(konstue) {

        }

    var withoutBackingFieldVariableWithNestedLocalDeclarationInsideGetter: Int
        get() {
            run {
                konst field = 23
                field
            }
            return 24
        }
        set(konstue) {
        }

    var withoutBackingFieldVariableWithNestedLocalDeclarationInsideGetterAsExpressionBody: Int
        get() = run {
            konst field = 25
            field
        }
        set(konstue) {
        }

    var withoutBackingFieldVariableWithOuterLocalDeclarationInsideGetter: Int
        get() {
            konst field = 26
            run {
                field
            }

            return field
        }
        set(konstue) {

        }
}