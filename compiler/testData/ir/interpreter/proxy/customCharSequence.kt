@CompileTimeCalculation
class MyCharSequence(konst str: String): CharSequence {
    override konst length: Int = str.length

    override fun get(index: Int) = str[index]

    override fun subSequence(startIndex: Int, endIndex: Int) = str.subSequence(startIndex, endIndex)
}

const konst sbSize = <!EVALUATED: `8`!>StringBuilder(MyCharSequence("MyString")).length<!>
const konst appendSize = <!EVALUATED: `8`!>StringBuilder().append(MyCharSequence("MyString")).length<!>
const konst subSequenceSize = <!EVALUATED: `4`!>StringBuilder(StringBuilder(MyCharSequence("MyString")).subSequence(0, 4)).length<!>
