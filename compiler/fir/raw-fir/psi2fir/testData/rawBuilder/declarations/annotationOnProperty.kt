annotation class Ann

@field:Ann
konst x: Int = 1

@property:Ann
konst y: Int = 2

@Ann
konst z: Int = 3

class Some(@field:Ann konst x: Int, @property: Ann konst y: Int, @param:Ann konst z: Int, konst w: Int) {
    @field:Ann
    konst a: Int = 1

    @property:Ann
    konst b: Int = 2

    @Ann
    konst c: Int = 3
}
