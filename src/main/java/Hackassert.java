import java.io.*;
import java.net.URI;
import java.security.SecureClassLoader;
import java.text.MessageFormat;
import javax.tools.*;
import static java.util.Collections.singleton;
import static javax.tools.JavaFileObject.Kind.SOURCE;

public class Hackassert
{
    public static void main(String... arguments) throws Exception
    {
        String TEST_CLASS = "Test";
        String TEST_URI = "string:///" + TEST_CLASS;
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        MessageFormat template = new MessageFormat("public class Test implements Runnable '{' public void run() '{'\n{0}'}}'");
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        JavaFileManager fileManager = new ForwardingJavaFileManager<JavaFileManager>(javac.getStandardFileManager(null, null, null))
        {
            private ByteArrayOutputStream bytecode = new ByteArrayOutputStream();

            @Override
            public ClassLoader getClassLoader(final Location location)
            {
                return new SecureClassLoader()
                {
                    @Override
                    protected Class<?> findClass(final String className) throws ClassNotFoundException
                    {
                        return TEST_CLASS.equals(className)? super.defineClass(className, bytecode.toByteArray(), 0, bytecode.size()):super.findClass(className);
                    }
                };
            }

            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException
            {
                return new SimpleJavaFileObject(URI.create(TEST_URI + kind.extension), kind)
                {
                    @Override
                    public OutputStream openOutputStream() throws IOException
                    {
                        return bytecode;
                    }
                };
            }
        };
        int data;
        while ((data = System.in.read()) != -1)
        {
            content.write(data);
        }
        JavaFileObject source = new SimpleJavaFileObject(URI.create(TEST_URI + SOURCE.extension), SOURCE)
        {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors)
            {
                return template.format(new String[] {content.toString()});
            }
        };
        Boolean successful = javac.getTask(null, fileManager, null, null, null, singleton(source)).call();
        System.out.println(successful? "OK":"");
        ((Runnable)fileManager.getClassLoader(null).loadClass(TEST_CLASS).newInstance()).run();
    }
} 