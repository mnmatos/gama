package com.digitallib;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelpController {

    private static final Logger logger = LogManager.getLogger(HelpController.class);

    public static void openInBrowser() {
        try {
            String markdown;
            try (InputStream is = HelpController.class.getResourceAsStream("/help/manual.md")) {
                if (is == null) {
                    logger.error("Manual não encontrado no classpath: /help/manual.md");
                    return;
                }
                markdown = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            MutableDataSet options = new MutableDataSet();
            options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
            Parser parser = Parser.builder(options).build();
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();
            String body = renderer.render(parser.parse(markdown));

            body = embedImagesAsBase64(body);

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8"/>
                        <style>
                            body { font-family: Segoe UI, Arial, sans-serif; font-size: 14px;
                                   margin: 20px 30px; color: #222; line-height: 1.6; }
                            h1 { color: #2c3e50; border-bottom: 2px solid #2c3e50; padding-bottom: 6px; }
                            h2 { color: #34495e; border-bottom: 1px solid #ccc; padding-bottom: 4px; margin-top: 30px; }
                            h3 { color: #555; margin-top: 20px; }
                            table { border-collapse: collapse; width: 100%%; margin: 10px 0; }
                            th, td { border: 1px solid #ccc; padding: 6px 12px; text-align: left; }
                            th { background-color: #f0f0f0; }
                            code { background: #f4f4f4; padding: 2px 5px; border-radius: 3px; font-family: Consolas, monospace; }
                            pre  { background: #f4f4f4; padding: 10px; border-radius: 4px; overflow-x: auto; }
                            img  { max-width: 100%%; height: auto; border: 1px solid #ddd;
                                   border-radius: 4px; margin: 8px 0; display: block; }
                            hr   { border: none; border-top: 1px solid #ddd; margin: 20px 0; }
                        </style>
                    </head>
                    <body>%s</body>
                    </html>
                    """.formatted(body);

            Path tmpFile = Files.createTempFile("gama-manual-", ".html");
            tmpFile.toFile().deleteOnExit();
            Files.writeString(tmpFile, html, StandardCharsets.UTF_8);

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(tmpFile.toUri());
            } else {
                // Fallback: try to open with the OS shell command
                new ProcessBuilder("cmd", "/c", "start", "", tmpFile.toAbsolutePath().toString())
                        .start();
            }

        } catch (Exception e) {
            logger.error("Erro ao abrir manual de ajuda no navegador", e);
        }
    }

    private static String embedImagesAsBase64(String html) {
        Pattern pattern = Pattern.compile("src=[\"'](?!data:)([^\"']+)[\"']");
        Matcher matcher = pattern.matcher(html);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String src = URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8);
            String filename = src.contains("/") ? src.substring(src.lastIndexOf('/') + 1) : src;
            String resourcePath = "/help/images/" + filename;
            String dataUri = toBase64DataUri(resourcePath);
            if (dataUri != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement("src=\"" + dataUri + "\""));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
                logger.warn("Imagem não encontrada: {}", resourcePath);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String toBase64DataUri(String resourcePath) {
        try (InputStream in = HelpController.class.getResourceAsStream(resourcePath)) {
            if (in == null) return null;
            byte[] bytes = in.readAllBytes();
            String ext = resourcePath.substring(resourcePath.lastIndexOf('.') + 1).toLowerCase();
            String mime = switch (ext) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "gif"         -> "image/gif";
                case "svg"         -> "image/svg+xml";
                default            -> "image/png";
            };
            return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            logger.error("Erro ao converter imagem para base64: {}", resourcePath, e);
            return null;
        }
    }
}

