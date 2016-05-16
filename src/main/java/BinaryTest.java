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
import java.lang.reflect.Method;
import java.security.SecureClassLoader;
import java.util.Base64;

//------------- Paste the code ABOVE THIS LINE into the HEAD section of your HackerRank test's code template ------------//

//------------- Paste the code BELOW THIS LINE into the TAIL section of your HackerRank test's code template ------------//

/**
* The {@link BinaryTest} class reads a Base64-encoded Java {@code .class} file from {@link System#in} and loads and executes
* the class defined by this file. The loaded class must have a {@code main} method.
* Class files can be encoded using the standard {@code base64} UNIX command.
*
* @author Mirko Raner, all rights reserved
**/
public class BinaryTest
{
    /**
    * Reads a Base64-encoded Java {@code .class} file from {@link System#in} and loads and executes it.
    * The input consists of the fully qualified class name, a line-feed character (LF; ASCII 10) and the Base64-encoded
    * class file (MIME encoding per RFC 2045).
    * @param arguments the command-line arguments (not evaluated)
    * @throws Exception if the code caused an {@link Error} or some other non-{@link Exception} {@link Throwable}
    * (all regular {@link Exception}s will be caught by default and reported to {@link System#out})
    **/
    public static void main(String... arguments) throws Exception
    {
        try
        {
            String name = null;
            ByteArrayOutputStream contents = new ByteArrayOutputStream();
            int data;
            while ((data = System.in.read()) != -1)
            {
                if (name == null && data == 10)
                {
                    name = new String(contents.toByteArray());
                    contents.reset();
                }
                if (data != 10)
                {
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
            String[] noArguments = {};
            main.invoke(null, (Object)noArguments);
        }
        catch (Exception exception)
        {
            exception.printStackTrace(System.out);
        }
    }
}
