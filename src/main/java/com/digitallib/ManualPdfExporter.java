package com.digitallib;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates a PDF from a Markdown file.
 *
 * Usage:
 *   java -cp <jar> com.digitallib.ManualPdfExporter <input.md> <output.pdf>
 *
 * Images in the Markdown are resolved relative to the input file's directory.
 * If no arguments are given, defaults to reading INSTALACAO.md from the working directory.
 */
public class ManualPdfExporter {

    public static void main(String[] args) throws Exception {
        Path inputMd  = args.length > 0 ? Paths.get(args[0]) : Paths.get("INSTALACAO.md");
        Path outputPdf = args.length > 1 ? Paths.get(args[1]) : Paths.get("instalacao-gama.pdf");
        generate(inputMd, outputPdf);
        System.out.println("PDF gerado com sucesso: " + outputPdf.toAbsolutePath());
    }

    /**
     * Generates a PDF from a Markdown file on the filesystem.
     * Images referenced in the Markdown are resolved relative to the markdown file's parent directory.
     */
    public static void generate(Path inputMd, Path outputPdf) throws Exception {
        if (!Files.exists(inputMd)) {
            throw new FileNotFoundException("Arquivo Markdown não encontrado: " + inputMd.toAbsolutePath());
        }

        String markdown = Files.readString(inputMd, StandardCharsets.UTF_8);
        Path imagesBaseDir = inputMd.toAbsolutePath().getParent();

        // Convert Markdown → HTML
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        String body = renderer.render(parser.parse(markdown));

        // Embed images as base64 so the PDF renderer doesn't need filesystem access
        body = embedImages(body, imagesBaseDir);

        String xhtml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
                    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <html xmlns="http://www.w3.org/1999/xhtml">
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        @page { margin: 2cm; }
                        body { font-family: Arial, sans-serif; font-size: 11pt;
                               color: #111; line-height: 1.6; }
                        h1 { font-size: 20pt; color: #2c3e50;
                             border-bottom: 2pt solid #2c3e50; padding-bottom: 4pt; }
                        h2 { font-size: 15pt; color: #34495e;
                             border-bottom: 1pt solid #aaa; padding-bottom: 3pt; margin-top: 18pt; }
                        h3 { font-size: 12pt; color: #555; margin-top: 12pt; }
                        table { border-collapse: collapse; width: 100%%; margin: 8pt 0; }
                        th, td { border: 1pt solid #bbb; padding: 4pt 8pt; }
                        th { background-color: #f0f0f0; }
                        code { background: #f4f4f4; padding: 1pt 3pt;
                               font-family: Courier New, monospace; font-size: 10pt; }
                        pre  { background: #f4f4f4; padding: 6pt; font-size: 10pt;
                               font-family: Courier New, monospace; }
                        hr   { border: none; border-top: 1pt solid #ccc; margin: 12pt 0; }
                        img  { max-width: 100%%; display: block; margin: 8pt 0; }
                    </style>
                </head>
                <body>%s</body>
                </html>
                """.formatted(body);

        if (outputPdf.getParent() != null) {
            Files.createDirectories(outputPdf.getParent());
        }
        try (OutputStream os = new FileOutputStream(outputPdf.toFile())) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(xhtml, outputPdf.getParent().toUri().toString());
            builder.toStream(os);
            builder.run();
        }
    }

    /** Replaces img src attributes with inline base64 data URIs resolved from imagesBaseDir. */
    private static String embedImages(String html, Path imagesBaseDir) {
        Pattern pattern = Pattern.compile("src=[\"'](?!data:)([^\"']+)[\"']");
        Matcher matcher = pattern.matcher(html);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String src = matcher.group(1);
            Path imgPath = imagesBaseDir.resolve(src).normalize();
            String dataUri = toBase64DataUri(imgPath);
            if (dataUri != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement("src=\"" + dataUri + "\""));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
                System.err.println("Imagem não encontrada: " + imgPath);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String toBase64DataUri(Path imgPath) {
        if (!Files.exists(imgPath)) return null;
        try {
            byte[] bytes = Files.readAllBytes(imgPath);
            String name = imgPath.getFileName().toString().toLowerCase();
            String mime = name.endsWith(".jpg") || name.endsWith(".jpeg") ? "image/jpeg"
                        : name.endsWith(".gif")                           ? "image/gif"
                        : name.endsWith(".svg")                           ? "image/svg+xml"
                        : "image/png";
            return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            System.err.println("Erro ao ler imagem: " + imgPath + " — " + e.getMessage());
            return null;
        }
    }
}
