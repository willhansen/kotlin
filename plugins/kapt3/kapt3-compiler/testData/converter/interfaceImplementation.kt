interface Named {
    konst name: String?
}

class Product2 : Named {
    override var name: String? = null

    constructor(otherName: String) {
        this.name = otherName
    }
}
