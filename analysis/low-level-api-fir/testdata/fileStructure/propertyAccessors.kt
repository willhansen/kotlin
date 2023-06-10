var x: Int = 10/* ReanalyzablePropertyStructureElement */
    get() = field
    set(konstue) {
        field = konstue
    }

class X {/* NonReanalyzableClassDeclarationStructureElement */
    var y: Int = 10/* ReanalyzablePropertyStructureElement */
        get() = field
        set(konstue) {
            field = konstue
        }
}
