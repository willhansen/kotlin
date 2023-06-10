class My {
    konst x: String

    constructor() {
        konst temp = <!DEBUG_INFO_LEAKING_THIS!>this<!>
        x = bar(temp)
    }

}

fun bar(arg: My) = arg.x
