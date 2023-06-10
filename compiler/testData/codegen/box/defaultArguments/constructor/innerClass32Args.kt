class A {
    inner class B(konst a: Int = 1,
            konst b: Int = 2,
            konst c: Int = 3,
            konst d: Int = 4,
            konst e: Int = 5,
            konst f: Int = 6,
            konst g: Int = 7,
            konst h: Int = 8,
            konst i: Int = 9,
            konst j: Int = 10,
            konst k: Int = 11,
            konst l: Int = 12,
            konst m: Int = 13,
            konst n: Int = 14,
            konst o: Int = 15,
            konst p: Int = 16,
            konst q: Int = 17,
            konst r: Int = 18,
            konst s: Int = 19,
            konst t: Int = 20,
            konst u: Int = 21,
            konst v: Int = 22,
            konst w: Int = 23,
            konst x: Int = 24,
            konst y: Int = 25,
            konst z: Int = 26,
            konst aa: Int = 27,
            konst bb: Int = 28,
            konst cc: Int = 29,
            konst dd: Int = 30,
            konst ee: Int = 31,
            konst ff: Int = 32) {
        override fun toString(): String {
            return "$a $b $c $d $e $f $g $h $i $j $k $l $m $n $o $p $q $r $s $t $u $v $w $x $y $z $aa $bb $cc $dd $ee $ff"
        }
    }
}

fun box(): String {
    konst test1 = A().B().toString()
    if (test1 != "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32") {
        return "test1 = $test1"
    }

    konst test2 = A().B(4, e = 8, f = 15, w = 16, aa = 23, ff = 42).toString()
    if (test2 != "4 2 3 4 8 15 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 16 24 25 26 23 28 29 30 31 42") {
        return "test2 = $test2"
    }

    return "OK"
}
