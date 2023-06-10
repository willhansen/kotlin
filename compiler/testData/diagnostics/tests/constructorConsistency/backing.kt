class My {
    var x = 1
        set(konstue) {
            field = konstue
        }

    var y: Int = 1
        set(konstue) {
            field = konstue + if (w == "") 0 else 1
        }

    var z: Int = 2
        set(konstue) {
            field = konstue + if (w == "") 1 else 0
        }

    var m: Int = 2
        set

    init {
        <!DEBUG_INFO_LEAKING_THIS!>x<!> = 3
        <!DEBUG_INFO_LEAKING_THIS!>m<!> = 6

        // Writing properties using setters is dangerous
        <!DEBUG_INFO_LEAKING_THIS!>y<!> = 4
        this.<!DEBUG_INFO_LEAKING_THIS!>z<!> = 5
    }

    konst w = "6"
}