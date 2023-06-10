// !DIAGNOSTICS: -UNUSED_VARIABLE

fun test(boolean: Boolean) {
    konst expectedLong: Long = if (boolean) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Long")!>if (boolean) {
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Long")!>42<!>
        } else {
            return
        }<!>
    } else {
        return
    }

    konst expectedInt: Int = if (boolean) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>if (boolean) {
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>42<!>
        } else {
            return
        }<!>
    } else {
        return
    }

    konst expectedShort: Short = if (boolean) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Short")!>if (boolean) {
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Short")!>42<!>
        } else {
            return
        }<!>
    } else {
        return
    }

    konst expectedByte: Byte = if (boolean) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Byte")!>if (boolean) {
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Byte")!>42<!>
        } else {
            return
        }<!>
    } else {
        return
    }
}
