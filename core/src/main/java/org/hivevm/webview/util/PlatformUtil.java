/**
 * MIT License
 * 
 * Copyright (c) 2025 HiveVM
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.hivevm.webview.util;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.jetbrains.annotations.NotNull;

class PlatformUtil {

  static void writeInputStreamToOutputStream(@NotNull InputStream source,
      @NotNull OutputStream dest) throws IOException {
    byte[] buffer = new byte[1024];
    int read = 0;

    while ((read = source.read(buffer)) != -1) {
      dest.write(buffer, 0, read);
    }

    dest.flush();

    source.close();
    dest.close();
  }

  static byte[] readInputStreamBytes(@NotNull InputStream source) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    writeInputStreamToOutputStream(source, out);

    source.close();

    return out.toByteArray();
  }

  static String readInputStreamString(@NotNull InputStream source, @NotNull Charset sourceCharset)
      throws IOException {
    byte[] bytes = readInputStreamBytes(source);

    return new String(bytes, sourceCharset);
  }

  static int getWordSize() {
    // Sources:
    // https://www.oracle.com/java/technologies/hotspotfaq.html#64bit_detection:~:text=When%20writing%20Java%20code%2C%20how%20do%20I%20distinguish%20between%2032%20and%2064%2Dbit%20operation%3F
    // https://stackoverflow.com/a/808314
    // https://www.ibm.com/docs/en/sdk-java-technology/8?topic=dja-determining-whether-your-application-is-running-32-bit-31-bit-z-64-bit-jvm

    String SADM = System.getProperty("sun.arch.data.model");
    if ((SADM != null) && !SADM.equals("unknown")) {
      return Integer.parseInt(SADM);
    }

    String CIVBM = System.getProperty("com.ibm.vm.bitmode");
    if (CIVBM != null) {
      return Integer.parseInt(CIVBM);
    }

    String vmName = System.getProperty("java.vm.name");
    if (vmName.contains("64-bit")) {
      return 64;
    } else if (vmName.contains("32-bit")) {
      return 32;
    }

    return -1;
  }

}
