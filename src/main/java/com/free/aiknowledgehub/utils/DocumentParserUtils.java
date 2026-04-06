package com.free.aiknowledgehub.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.DocumentParser;
import java.io.InputStream;
import java.util.List;

/**
 * @Description
 * @Author: Liberty-Swine
 * @Date 2026/4/6 19:20
 */
@Component
public class DocumentParserUtils {

    /**
     * 解析文件流，提取文本内容
     * @param inputStream 文件输入流
     * @param contentType 文件类型（如 application/pdf、application/vnd.openxmlformats-officedocument.wordprocessingml.document）
     * @return 提取后的纯文本
     * @throws Exception 解析异常
     */
    public String parse(InputStream inputStream, String contentType) throws Exception {
        // 根据文件类型分发解析
        if (contentType == null) {
            throw new IllegalArgumentException("文件类型不能为空");
        }

        return switch (contentType) {
            case "application/pdf" -> parsePdf(inputStream);
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> parseWord(inputStream);
            case "text/plain", "text/markdown" -> parseTxt(inputStream);
            default -> throw new UnsupportedOperationException("不支持的文件类型: " + contentType);
        };
    }

    /**
     * 解析PDF
     * @param inputStream
     * @return
     * @throws Exception
     */
    private String parsePdf(InputStream inputStream) throws Exception {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 解析Word
     * @param inputStream
     * @return
     * @throws Exception
     */
    private String parseWord(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph para : paragraphs) {
                sb.append(para.getText()).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * 解析TXT/MD
     * @param inputStream
     * @return
     * @throws Exception
     */
    private String parseTxt(InputStream inputStream) throws Exception {
        return new String(inputStream.readAllBytes());
    }
}
