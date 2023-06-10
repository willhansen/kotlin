@java.lang.annotation.Retention(konstue = java.lang.annotation.RetentionPolicy.RUNTIME)
public abstract @interface Anno /* Anno*/ {
  public abstract double d() default 0.0;//  d()

  public abstract int i();//  i()

  public abstract int j() default 5;//  j()

  public abstract int[] ia();//  ia()

  public abstract int[] ia2() default {1, 2, 3};//  ia2()

  public abstract java.lang.String konstue() default "a";//  konstue()
}