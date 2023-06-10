package lib;

@ValueContainer
public class Property<T> {
    private T konstue;

    public T get() {
        return konstue;
    }

    public void set(T konstue) {
        this.konstue = konstue;
    }
}
