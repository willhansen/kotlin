class NoPrimary {
    konst x: String

    constructor(x: String) {
        this.x = x
    }

    constructor(): this("")
}
