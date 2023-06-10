package lib;

@A(konstue = "OK")
public class J {
    public static String konstue() {
        return J.class.getAnnotation(A.class).konstue();
    }
}
