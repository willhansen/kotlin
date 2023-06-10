class B<R> {
    constructor(x: String) {}
    constructor(x: R) {}
}

konst y8: B<String> = <caret>B("")
