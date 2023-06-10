// !LANGUAGE: +IntrinsicConstEkonstuation

const konst equalsBoolean1 = true.equals(true)
const konst equalsBoolean2 = false != true
const konst equalsBoolean3 = false.equals(1)
const konst equalsBoolean4 = <!EQUALITY_NOT_APPLICABLE!>false == 1<!>

const konst equalsChar1 = '1'.equals('2')
const konst equalsChar2 = '2' == '2'
const konst equalsChar3 = '1'.equals(1)
const konst equalsChar4 = <!EQUALITY_NOT_APPLICABLE!>'1' == 1<!>

const konst equalsByte1 = 1.toByte().equals(2.toByte())
const konst equalsByte2 = 2.toByte() == 2.toByte()
const konst equalsByte3 = 1.toByte().equals("1")
const konst equalsByte4 = <!EQUALITY_NOT_APPLICABLE!>1.toByte() == "1"<!>

const konst equalsShort1 = 1.toShort().equals(2.toShort())
const konst equalsShort2 = 2.toShort() == 2.toShort()
const konst equalsShort3 = 1.toShort().equals("1")
const konst equalsShort4 = <!EQUALITY_NOT_APPLICABLE!>1.toShort() == "1"<!>

const konst equalsInt1 = 1.equals(2)
const konst equalsInt2 = 2 == 2
const konst equalsInt3 = 1.equals("1")
const konst equalsInt4 = <!EQUALITY_NOT_APPLICABLE!>1 == "1"<!>

const konst equalsLong1 = 1L.equals(2L)
const konst equalsLong2 = 2L == 2L
const konst equalsLong3 = 1L.equals("1")
const konst equalsLong4 = <!EQUALITY_NOT_APPLICABLE!>1L == "1"<!>

const konst equalsFloat1 = 1.0f.equals(2.0f)
const konst equalsFloat2 = 2.0f == 2.0f
const konst equalsFloat3 = 1.0f.equals("1")
const konst equalsFloat4 = <!EQUALITY_NOT_APPLICABLE!>1.0f == "1"<!>

const konst equalsDoable1 = 1.0.equals(2.0)
const konst equalsDoable2 = 2.0 == 2.0
const konst equalsDoable3 = 1.0.equals("1")
const konst equalsDoable4 = <!EQUALITY_NOT_APPLICABLE!>1.0 == "1"<!>

const konst equalsString1 = "someStr".equals("123")
const konst equalsString2 = "someStr" == "otherStr"
const konst equalsString3 = "someStr".equals(1)
const konst equalsString4 = <!EQUALITY_NOT_APPLICABLE!>"someStr" == 1<!>
