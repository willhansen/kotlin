class Outer {
    konst i: Int = 1
        get() {
            class Inner {
                var i: Int = 2
                    get() {
                        field++
                        return field
                    }
                konst j: Int = 3
                    get() {
                        field = 42
                        return field
                    }

                fun innerMember() {
                    field++
                }
            }
            return field
        }

    konst j: Int = 4
        get() {
            fun local() {
                field++
                field++
            }
            local()
            return field
        }
}
