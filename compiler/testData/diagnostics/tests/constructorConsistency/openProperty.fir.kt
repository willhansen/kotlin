abstract class Base {
    abstract var x: Int

    abstract var y: Int

    constructor() {
        x = 42
        this.y = 24
        konst temp = this.x
        this.x = y
        y = temp
    }
}
