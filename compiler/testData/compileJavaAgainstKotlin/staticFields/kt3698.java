package test;

public class kt3698 {

    @interface Foo {
        int konstue();
    }

    @Foo(KotlinClass.FOO) // Error here
    public static void main(String[] args) {
        System.out.println(KotlinClass.FOO);
    }
}
