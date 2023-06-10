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
        x = 3
        m = 6

        // Writing properties using setters is dangerous
        y = 4
        this.z = 5
    }

    konst w = "6"
}