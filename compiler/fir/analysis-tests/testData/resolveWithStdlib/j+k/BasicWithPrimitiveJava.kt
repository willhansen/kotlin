// FILE: Some.java

public class Some {
    public boolean foo(int param) {
        return param > 0;
    }

    public String[] bar(int[] arr) {
        String[] result = new String[arr.length];
        int i = 0;
        for (int elem: arr) {
            result[i++] = elem;
        }
        return result;
    }
}

// FILE: jvm.kt

class A : Some() {
    fun test() {
        konst res1 = foo(1)
        konst res2 = foo(-1)
        konst res3 = bar(intArrayOf(0, 2, -2))
    }
}
