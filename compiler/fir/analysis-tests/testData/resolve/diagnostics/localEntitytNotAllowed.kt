object A {
    object B {
        object C
    }

    interface X

    konst a = object : Any() {
        <!LOCAL_OBJECT_NOT_ALLOWED!>object D<!> {
            <!LOCAL_OBJECT_NOT_ALLOWED!>object G<!>
            <!LOCAL_INTERFACE_NOT_ALLOWED, NESTED_CLASS_NOT_ALLOWED!>interface Z<!>
        }

        <!LOCAL_INTERFACE_NOT_ALLOWED!>interface Y<!>
    }

    fun b() {
        <!LOCAL_OBJECT_NOT_ALLOWED!>object E<!> {
            <!LOCAL_OBJECT_NOT_ALLOWED!>object F<!>
            <!LOCAL_INTERFACE_NOT_ALLOWED, NESTED_CLASS_NOT_ALLOWED!>interface M<!>
        }

        <!LOCAL_INTERFACE_NOT_ALLOWED!>interface N<!>

        konst c = object : Any() {
            konst t = "test"

            <!LOCAL_INTERFACE_NOT_ALLOWED!>interface U<!>
        }
    }
}
