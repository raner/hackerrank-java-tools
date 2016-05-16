//                                                                          //
// Copyright 2016 Mirko Raner                                               //
//                                                                          //
// Licensed under the Apache License, Version 2.0 (the "License");          //
// you may not use this file except in compliance with the License.         //
// You may obtain a copy of the License at                                  //
//                                                                          //
//     http://www.apache.org/licenses/LICENSE-2.0                           //
//                                                                          //
// Unless required by applicable law or agreed to in writing, software      //
// distributed under the License is distributed on an "AS IS" BASIS,        //
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. //
// See the License for the specific language governing permissions and      //
// limitations under the License.                                           //
//                                                                          //

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.security.SecureClassLoader;
import java.text.MessageFormat;
import java.util.concurrent.Callable;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import static java.util.Collections.singleton;
import static javax.tools.JavaFileObject.Kind.SOURCE;

//------------- Paste the code ABOVE THIS LINE into the HEAD section of your HackerRank test's code template ------------//

//------------- Paste the code BELOW THIS LINE into the TAIL section of your HackerRank test's code template ------------//

/**
* The {@link SourceTest} class reads a Java source snippet from {@link System#in}, compiles it into a {@code .class} file,
* and loads and executes it. As this mechanism relies on the {@link JavaCompiler}, a full JDK (as opposed to just a JRE)
* is required at runtime.
*
* @author Mirko Raner, all rights reserved
**/
public class SourceTest
{
    /**
    * Reads a Java source snippet from {@link System#in}, compiles it into a {@code .class} file, and loads and executes
    * it. The snippet must be valid code when pasted into the {@link Callable#call} method of a {@link Callable} of
    * {@link Void}. A terminal {@code return null;} statement is appended as part of the template; if the code needs to
    * return early (i.e., there are additional return points in the code) this must be done by returning {@code null}.
    * Compilation errors will be reported on {@link System#out}.
    *
    * @param arguments the command line arguments (not evaluated)
    * @throws Exception if a compilation or runtime exception occurred
    **/
    public static void main(String... arguments) throws Exception
    {
        String TEST_CLASS = "Test";
        String TEST_URI = "string:///" + TEST_CLASS;
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        MessageFormat template = new MessageFormat("public class Test implements java.util.concurrent.Callable<Void> '{' public Void call() throws Exception '{'\n{0}\nreturn null;'}}'");
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        JavaFileManager fileManager = new ForwardingJavaFileManager<JavaFileManager>(javac.getStandardFileManager(null, null, null))
        {
            ByteArrayOutputStream bytecode = new ByteArrayOutputStream();

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
        if (javac.getTask(new PrintWriter(System.out, true), fileManager, null, null, null, singleton(source)).call())
        {
            try
            {
                @SuppressWarnings("unchecked")
                Callable<Void> callable = (Callable<Void>)fileManager.getClassLoader(null).loadClass(TEST_CLASS).newInstance();
                callable.call();
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }
}
