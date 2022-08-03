These are the main differences to the official version:

# Trailing Slash

In Spark, the path `/users/admin` is not the same as `/users/admin/`.

If you want the trailing slash to be optional, you will need to write 
your path as: `/users/admin/?`, which will match (`admin/` or `admin`).

| Path     | Example 1:<br/>/file.html | Example 2<br/>/dir/ |
|----------|---------------------------|---------------------|
| /:page   | Match                     | No Match            |
| /:page/  | No Match                  | Match               |
| /:page/? | Match                     | Match               |

**NOTE**: Remember that in Spark opening slash is optional, for this
reason, `""` and `"/?"` are aliases of `"/"`.

# Compression

The official version will compress your output using GZip if you add
the header `Content-Encoding` to `GZip` (only if the browser supports it).
However, it doesn't include the `Content-Length` header, which the browser 
uses to monitor the download process. It is also not possible to set the 
length header before Spark compress it as the compression happens after
you return your output. Also, it is not possible to compress it yourself
as Spark will find the `Content-Encoding` header and will try to compress
your output again.

For that reason, we added a way to control what does Spark should do, by
specifying: `response.compress` value:

## Let Spark do it for you

```java
import spark.Response.Compression;
import static spark.Spark.*;

public class GZipTest {
    public static void main(String[] arg) {
        get("/download.txt", (request, response) -> {
            response.compression = Compression.GZIP_COMPRESS;
            return "TEXT CONTENT";
        });
    }
}
```

By using `GZIP_COMPRESS` you are letting Spark know that it should
compress the output using `GZip` and it should add the `Content-Encoding`
header (very similar to the Official version behaviour). 

**NOTE** : By only setting the `Content-Encoding` header, this version
won't compress your output.

## DIY

```java
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import spark.Response.Compression;
import static spark.Spark.*;

public class GZipTest {
    public static void main(String[] arg) {
        get("/download.txt", (request, response) -> {
            response.compression = Compression.GZIP_COMPRESSED; // <-- We use COMPRESSED instead
            byte[] bytes = gzipFile("/some/file.txt");
            response.header("Content-Length", bytes.length);
            return bytes;
        });
    }

    /**
     * Compress file
     */
    public byte[] gzipFile(String source_filepath) {
        byte[] buffer = new byte[1024];
        long size = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gzipOuputStream = new GZIPOutputStream(baos);
            FileInputStream fileInput = new FileInputStream(source_filepath);
            int bytes_read;
            while ((bytes_read = fileInput.read(buffer)) > 0) {
                gzipOuputStream.write(buffer, 0, bytes_read);
                size += bytes_read;
            }
            fileInput.close();
            gzipOuputStream.finish();
            gzipOuputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return baos.toByteArray();
    }
}
```

This time, by using `GZIP_COMPRESSED` (instead of `GZIP_COMPRESS` - note the tense -), we are letting Spark know that we are handling the compression. Because
of that, we can set the `Content-Length` header now. 

**NOTE** : Be aware that in this last example, we are compressing the file and keeping
the result in memory before sending it to Spark, which means that very large size files
may take all your application memory capacity. One way to be able to get the size of
very large files is by calculating the size by compressing the stream first (ignoring the output),
get the size, and proceed as usual (this means it will be compressed twice, wasting processing resources).
Other way is to compress the input and stored it as a file (not ideal if your input is dynamic).

## Brotli compression

Additional to `GZip`, this version includes [Brotli](https://en.wikipedia.org/wiki/Brotli) compression
which is [widely supported](https://caniuse.com/brotli) by modern browsers.

In order to use this compression you will need first to add the 
[com.nixxcode.jvmbrotli](https://mvnrepository.com/artifact/com.nixxcode.jvmbrotli) dependency 
in your project (Gradle, Maven, etc). 

**NOTE**: If you are using Gradle, you may need to also include the native library according to your
system architecture.

Then, you can activate it using `response.compression = Compression.BROTLI_COMPRESS` or 
`response.compression = Compression.BROTLI_COMPRESSED` (similar to `GZip`).

**NOTE**: By default, this version will use `response.compression = Compression.AUTO`, which means,
if the browser supports `Brotli`, and you have the dependency in your project, will compress it
using `Brotli`, otherwise, will use `GZip` (if the browser supports it).

# Mime Types

In this version (Unofficial), we have added many more common mime types into Spark. Additionally,
we added a way to modify or add new types if needed:

```java
import spark.staticfiles.MimeType;

import static spark.Spark.*;

public class MimeTest {
    static {
        // You can add a custom mapping like this:
        MimeType.mappings.put("jsonld", "application/ld+json");
        // Or override them:
        MimeType.mappings.put("bin", "binary/octet-stream");
    }
}
```

# Regex Paths

You can now use regular expressions to define paths for example:

**NOTE:** To define a regex path, they need to start with `~/`. Ending slash is optional.

```java
import static spark.Spark.*;

public class RegexPaths {

    public static void main(String[] args) {
        // Example: /hello-world, /hello-earth
        get("~/^\\/hello-.*.html", (request, response) -> "Hello World!");
        // You can define groups:
        get("~/([^.]+)\\.page$/", (request, response) -> String.format("%s Page", request.params(1)));
        // You can define groups by name:
        // Example: /1723-my-page-name.ext
        get("~/^\\/(?<id>\\d+)-(?<name>[^.]+)\\.(?<ext>.*)$/", (request, response) -> 
            String.format("[%d] ID : %s Page (ext: %s)",
                Integer.parseInt(request.params("id")), //As it matches the regex, it is safe
                request.params("name"),
                request.params("ext")
        ));
    }
    
}
```

# HTTP2
