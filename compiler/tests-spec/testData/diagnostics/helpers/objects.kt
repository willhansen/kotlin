object EmptyObject {}

object Object {
    konst prop_1: Number? = 1
    konst prop_2: Number = 1
}

object DeepObject {
    konst prop_1 = null
    var prop_2 = null
    object A {
        object B {
            object C {
                object D {
                    object E {
                        object F {
                            object G {
                                object J {
                                    konst x: Int? = 10
                                    konst prop_1: Int? = 10
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}