// !LANGUAGE: +InlineClasses +UseGetterNameForPropertyAnnotationsMethodOnJvm

interface IFoo {
    fun overridingFun()
    fun String.overridingExtFun()

    konst overridingVal: Int
    var overridingVar: Int
    konst String.overridingExtVal: Int
    var String.overridingExtVar: Int
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class A

@Target(AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.BINARY)
annotation class AGet

@Target(AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
annotation class ASet

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class ASetParam

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class AReceiver

inline class Z(@get:AGet konst x: Int) : IFoo {

    constructor(y: Long) : this(y.toInt())

    @A override fun overridingFun() {}
    @A override fun @receiver:AReceiver String.overridingExtFun() {}

    @A @get:AGet
    override konst overridingVal: Int
        get() = x

    @Suppress("RESERVED_VAR_PROPERTY_OF_VALUE_CLASS")
    @A @get:AGet @set:ASet @setparam:ASetParam
    override var overridingVar: Int
        get() = x
        set(v) {}

    @A @get:AGet
    override konst @receiver:AReceiver String.overridingExtVal: Int
        get() = x

    @Suppress("RESERVED_VAR_PROPERTY_OF_VALUE_CLASS")
    @A @get:AGet @set:ASet @setparam:ASetParam
    override var @receiver:AReceiver String.overridingExtVar: Int
        get() = x
        set(v) {}

    @A override fun toString(): String = x.toString()

    @A fun nonOverridingFun() {}

    @A fun @receiver:AReceiver String.nonOverridingExtFun() {}

    @A @get:AGet
    konst nonOverridingVal: Int get() = x

    @Suppress("RESERVED_VAR_PROPERTY_OF_VALUE_CLASS")
    @A @get:AGet @set:ASet @setparam:ASetParam
    var nonOverridingVar: Int
        get() = x
        set(v) {}

    @A @get:AGet
    konst @receiver:AReceiver String.nonOverridingExtVal: Int
        get() = x

    @Suppress("RESERVED_VAR_PROPERTY_OF_VALUE_CLASS")
    @A @get:AGet @set:ASet @setparam:ASetParam
    var @receiver:AReceiver String.nonOverridingExtVar: Int
        get() = x
        set(v) {}
}
