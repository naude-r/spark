/*
 * Copyright 2016 - Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.staticfiles;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import spark.resource.AbstractFileResolvingResource;

/**
 * Configures and holds mappings from file extensions to MIME types.
 */
public class MimeType {

    final static String CONTENT_TYPE = "Content-Type";

    private static volatile boolean guessingOn = true;

    public static final Map<String, String> mappings = new ConcurrentHashMap<>();

    static {
        mappings.put("7z", "application/x-7z-compressed");
        mappings.put("aac", "audio/aac");
        mappings.put("abw", "application/x-abiword");
        mappings.put("arc", "application/x-freearc");
        mappings.put("au", "audio/basic");
        mappings.put("avi", "video/msvideo,video/avi,video/x-msvideo");
        mappings.put("azw", "application/vnd.amazon.ebook");
        mappings.put("bin", "application/octet-stream");
        mappings.put("bmp", "image/bmp");
        mappings.put("bz", "application/x-bzip");
        mappings.put("bz2", "application/x-bzip2");
        mappings.put("cda", "application/x-cdf");
        mappings.put("csh", "application/x-csh");
        mappings.put("css", "text/css");
        mappings.put("csv", "text/csv");
        mappings.put("doc", "application/msword");
        mappings.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mappings.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        mappings.put("dtd", "application/xml-dtd");
        mappings.put("eot", "application/vnd.ms-fontobject");
        mappings.put("epub", "application/epub+zip");
        mappings.put("es", "application/ecmascript");
        mappings.put("exe", "application/octet-stream");
        mappings.put("gif", "image/gif");
        mappings.put("gz", "application/x-gzip");
        mappings.put("hqx", "application/mac-binhex40");
        mappings.put("htm", "text/html");
        mappings.put("html", "text/html");
        mappings.put("ico", "image/x-icon");
        mappings.put("ics", "text/calendar");
        mappings.put("jar", "application/java-archive");
        mappings.put("jpeg", "image/jpeg");
        mappings.put("jpg", "image/jpeg");
        mappings.put("js", "application/javascript");
        mappings.put("json", "application/json");
        mappings.put("jsonld", "application/ld+json");
        mappings.put("mid", "audio/midi");
        mappings.put("midi", "audio/x-midi");
        mappings.put("mjpeg", "video/x-motion-jpeg");
        mappings.put("mjs", "application/javascript");
        mappings.put("mp3", "audio/mpeg");
        mappings.put("mp4", "video/mp4");
        mappings.put("mpeg", "video/mpeg");
        mappings.put("mpkg", "application/vnd.apple.installer+xml");
        mappings.put("ndjson", "application/x-ndjson");
        mappings.put("odp", "application/vnd.oasis.opendocument.presentation");
        mappings.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
        mappings.put("odt", "application/vnd.oasis.opendocument.text");
        mappings.put("oga", "audio/ogg");
        mappings.put("ogg", "audio/vorbis,application/ogg");
        mappings.put("ogv", "video/ogg");
        mappings.put("ogx", "application/ogg");
        mappings.put("opus", "audio/opus");
        mappings.put("otf", "application/font-otf");
        mappings.put("pdf", "application/pdf");
        mappings.put("php", "application/x-httpd-php");
        mappings.put("pl", "application/x-perl");
        mappings.put("png", "image/png");
        mappings.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
        mappings.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        mappings.put("ppt", "application/vnd.ms-powerpointtd");
        mappings.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        mappings.put("ps", "application/postscript");
        mappings.put("qt", "video/quicktime");
        mappings.put("ra", "audio/x-pn-realaudio,audio/vnd.rn-realaudio");
        mappings.put("ram", "audio/x-pn-realaudio,audio/vnd.rn-realaudio");
        mappings.put("rar", "application/x-rar-compressed");
        mappings.put("rdf", "application/rdf,application/rdf+xml");
        mappings.put("rtf", "application/rtf");
        mappings.put("sgml", "text/sgml");
        mappings.put("sh", "application/x-sh");
        mappings.put("sit", "application/x-stuffit");
        mappings.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
        mappings.put("svg", "image/svg+xml");
        mappings.put("swf", "application/x-shockwave-flash");
        mappings.put("tar", "application/x-tar");
        mappings.put("tgz", "application/x-tar");
        mappings.put("tif", "image/tiff");
        mappings.put("tiff", "image/tiff");
        mappings.put("ts", "video/mp2t");
        mappings.put("tsv", "text/tab-separated-values");
        mappings.put("ttf", "application/font-ttf");
        mappings.put("txt", "text/plain");
        mappings.put("vsd", "application/vnd.visio");
        mappings.put("wav", "audio/wav,audio/x-wav");
        mappings.put("weba", "audio/webm");
        mappings.put("webm", "video/webm");
        mappings.put("webp", "image/webp");
        mappings.put("woff", "application/font-woff");
        mappings.put("woff2", "application/font-woff2");
        mappings.put("xhtml", "application/xhtml+xml");
        mappings.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
        mappings.put("xls", "application/vnd.ms-excel");
        mappings.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        mappings.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mappings.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        mappings.put("xml", "application/xml");
        mappings.put("xul", "application/vnd.mozilla.xul+xml");
        mappings.put("yaml", "text/yaml");
        mappings.put("zip", "application/zip,application/x-compressed-zip");
    }

    public static void register(String extension, String mimeType) {
        mappings.put(extension, mimeType);
    }

    public static void disableGuessing() {
        guessingOn = false;
    }

    public static String fromResource(AbstractFileResolvingResource resource) {
        String filename = Optional.ofNullable(resource.getFilename()).orElse("");
        return getMimeType(filename);
    }

    protected static String getMimeType(String filename) {
        String fileExtension = filename.replaceAll("^.*\\.(.*)$", "$1");
        return mappings.getOrDefault(fileExtension, "application/octet-stream");
    }

    protected static String fromPathInfo(String pathInfo) {
        return getMimeType(pathInfo);
    }

    protected static boolean shouldGuess() {
        return guessingOn;
    }
}
