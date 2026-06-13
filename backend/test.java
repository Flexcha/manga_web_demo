import java.io.File;
public class test {
    public static void main(String[] args) {
        System.out.println("PWD: " + new File(".").getAbsolutePath());
        System.out.println("frontend: " + new File("frontend").exists());
        System.out.println("../frontend: " + new File("../frontend").exists());
    }
}
