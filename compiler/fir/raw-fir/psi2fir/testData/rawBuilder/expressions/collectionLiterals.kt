annotation class Ann1(konst arr: IntArray)

annotation class Ann2(konst arr: DoubleArray)

annotation class Ann3(konst arr: Array<String>)

@Ann1([])
@Ann2([])
@Ann3([])
class Zero

@Ann1([1, 2])
class First

@Ann2([3.14])
class Second

@Ann3(["Alpha", "Omega"])
class Third
