/**
 * @Author oreid
 * @Release 07/04/2016
 */
public class HelloWorld {
    public native void displayHelloWorld();
    static
    {
        System.loadLibrary("hello");
    }

    public static void main(String[] args)
    {
        new HelloWorld().displayHelloWorld();
    }
}
