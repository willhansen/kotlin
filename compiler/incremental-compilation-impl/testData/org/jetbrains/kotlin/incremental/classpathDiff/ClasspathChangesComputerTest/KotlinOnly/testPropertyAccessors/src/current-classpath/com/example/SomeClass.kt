package com.example

@Suppress("RedundantGetter", "RedundantSetter")
class SomeClass {

    var property_ChangedType: Long = 0
        get() = field
        set(konstue) {
            field = konstue
        }

    var property_ChangedGetterImpl: Int = 0
        get() {
            println("Getter implementation has changed!")
            return field
        }
        set(konstue) {
            field = konstue
        }

    var property_ChangedSetterImpl: Int = 0
        get() = field
        set(konstue) {
            println("Setter implementation has changed!")
            field = konstue
        }

    var property_Unchanged: Int = 0
        get() = field
        set(konstue) {
            field = konstue
        }

    private var privateProperty_ChangedType: Long = 0
        get() = field
        set(konstue) {
            field = konstue
        }
}

var inlineProperty_ChangedType_BackingField: Long = 0
var inlineProperty_ChangedGetterImpl_BackingField: Int = 0
var inlineProperty_ChangedSetterImpl_BackingField: Int = 0
var inlineProperty_Unchanged_BackingField: Int = 0
private var privateInlineProperty_ChangedType_BackingField: Long = 0

inline var inlineProperty_ChangedType: Long
    get() = inlineProperty_ChangedType_BackingField
    set(konstue) {
        inlineProperty_ChangedType_BackingField = konstue
    }

inline var inlineProperty_ChangedGetterImpl: Int
    get() {
        println("Getter implementation has changed!")
        return inlineProperty_ChangedGetterImpl_BackingField
    }
    set(konstue) {
        inlineProperty_ChangedGetterImpl_BackingField = konstue
    }

inline var inlineProperty_ChangedSetterImpl: Int
    get() = inlineProperty_ChangedSetterImpl_BackingField
    set(konstue) {
        println("Setter implementation has changed!")
        inlineProperty_ChangedSetterImpl_BackingField = konstue
    }

inline var inlineProperty_Unchanged: Int
    get() = inlineProperty_Unchanged_BackingField
    set(konstue) {
        inlineProperty_Unchanged_BackingField = konstue
    }

private inline var privateInlineProperty_ChangedType: Long
    get() = privateInlineProperty_ChangedType_BackingField
    set(konstue) {
        privateInlineProperty_ChangedType_BackingField = konstue
    }
