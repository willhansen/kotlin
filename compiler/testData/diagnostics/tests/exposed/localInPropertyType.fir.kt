class My<T>(konst konstue: T)

open class Base

konst <!EXPOSED_PROPERTY_TYPE!>inkonstid1<!> = run {
    class Local
    My(Local())
}

konst inkonstid2 = My(object {})

konst inkonstid3 = My(object : Base() {})

konst <!EXPOSED_PROPERTY_TYPE!>inkonstid4<!> = run {
    class Local
    My(My(Local()))
}

konst <!EXPOSED_PROPERTY_TYPE!>inkonstid5<!> = run {
    fun inkonstid5a() = run {
        class Local
        Local()
    }
    My(inkonstid5a())
}

// Valid: effectively Any
konst konstid1 = object {}

// Valid: effectively Base
konst konstid2 = object : Base() {}

// Valid: explicit type argument
konst konstid3 = My<Base>(object : Base() {})

// Valid: explicit type specified
konst konstid4 : My<Base> = My(object : Base() {})

// Valid: local class denotable in local scope
konst konstid5 = run {
    class Local
    fun konstid5a() = My(Local())
    My<Any>(konstid5a())
}

// Valid: local class denotable in local scope
konst konstid6 = run {
    class Local
    fun konstid6a() = run {
        fun konstid6b() = My(Local())
        konstid6b()
    }
    My<Any>(konstid6a())
}

// Valid: effectively My<Any>
konst konstid7 = run {
    class Local
    My<My<*>>(My(Local()))
}