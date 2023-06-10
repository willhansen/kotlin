konst simpleNumber: Number
    field = 4

konst numberWithPrivateField: Number
    private field = 4

konst numberWithLateinitField: Number
    lateinit field: Int

konst numberWithInternalLateinitField: Number
    internal lateinit field: Int

var numberWithFieldAndAccessors: Number
    field = "test"
    get() = field.length
    set(konstue) {
        field = konstue.toString()
    }

konst numberWithExplicitType: Number
    field: Int = 10

konst numberWithBlockInitializer: Number
    field {
        return 10
    }

konst minimalNumber
    field
