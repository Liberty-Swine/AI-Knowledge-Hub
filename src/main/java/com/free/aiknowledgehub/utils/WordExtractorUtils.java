package com.free.aiknowledgehub.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;

/**
 * @Description 获取word文本内容
 * @Author: Liberty-Swine
 * @Date 2026/4/6 18:47
 */
public class WordExtractorUtils {

    /**
     * 获取word文件内容
     * @param filePath
     * @return
     * @throws Exception
     */
    public static String getContent(String filePath) throws Exception {
        File file = new File(filePath);
        if (filePath.endsWith(".docx")) {
            return extractDocx(file).replaceAll("[\\r\\n]+", "");
        } else if (filePath.endsWith(".doc")) {
            return extractDoc(file).replaceAll("[\\r\\n]+", "");
        } else {
            throw new IllegalArgumentException("Unsupported file format");
        }
    }

    private static String extractDocx(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText().trim();
        }
    }

    private static String extractDoc(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument doc = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(doc)) {
            return extractor.getText().trim();
        }
    }

    public static JSONObject extractAsJson(String filePath) throws Exception {
        String text = getContent(filePath);
        JSONObject json = new JSONObject();
        json.put("content", text);
        json.put("filename", new File(filePath).getName());
        return json;
    }

    public static void main(String[] args) {
        try {
            String text = getContent("C:\\Users\\dush\\Desktop\\test.docx");
            System.out.println(text);

            JSONObject jsonObject = extractAsJson("C:\\Users\\dush\\Desktop\\test.docx");
            System.out.println(jsonObject);
            //移除所有的空格换行
            String cleanText = text.replaceAll("[\\r\\n]+", "");
            // 将文本传递给 DeepSeek 或其他处理逻辑
            // deepseek.process(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
