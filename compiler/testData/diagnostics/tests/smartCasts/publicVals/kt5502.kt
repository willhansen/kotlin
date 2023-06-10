// NB: should work after KT-5907 / KT-4450 fix

konst currentTimeMillis = 1234L

public class Foo(protected konst maxParsingTimeInMillis: Long?) {

    var parsingStartTimeStamp = 0L

    protected fun checkForParsingTimeout(): Boolean {
        if (maxParsingTimeInMillis == null)
            return true
        if (currentTimeMillis - parsingStartTimeStamp > <!DEBUG_INFO_SMARTCAST!>maxParsingTimeInMillis<!>)
            return false
        return true
    }
}