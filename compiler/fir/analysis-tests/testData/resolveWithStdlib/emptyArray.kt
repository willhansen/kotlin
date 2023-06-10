konst x: Array<String> = emptyArray()

konst y: Array<String>
    get() = emptyArray()

interface My

konst z: Array<out My>
    get() = emptyArray()