import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.security.SecureClassLoader;
import java.util.Base64;

public class Hackload {

    public static void main(String... arguments) throws Exception {

        String name = null;
        ByteArrayOutputStream contents = new ByteArrayOutputStream();
        int data;
        while ((data = System.in.read()) != -1) {
            if (name == null && data == 10) {
                name = new String(contents.toByteArray());
                System.err.println();
                System.err.println("Name is " + name);
                contents.reset();
            }
            if (data != 10) {
                contents.write(data);
            }
        }
        Base64.Decoder base64 = Base64.getMimeDecoder();
        byte[] bytes = contents.toByteArray();
        byte[] bytecode = base64.decode(bytes);
        final String outerName = name;
        ClassLoader classLoader = new SecureClassLoader()
        {
            @Override
            protected Class<?> findClass(final String className) throws ClassNotFoundException
            {
                return className.equals(outerName)? super.defineClass(className, bytecode, 0, bytecode.length):super.findClass(className);
            }
        };
        Class<?> testClass = classLoader.loadClass(name);
        Method main = testClass.getDeclaredMethod("main", String[].class);
        try {
            String[] noArguments = {};
            main.invoke(null, (Object)noArguments);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
