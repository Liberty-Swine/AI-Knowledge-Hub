package com.free.aiknowledgehub.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description 获取pdf文件内容
 * @Author: Liberty-Swine
 * @Date 2026/4/6 18:56
 */
public class PdfExtractorUtils {
    // ====================== 智能配置中心 ======================
    public static class Config {
        // 基础配置
        private boolean extractNativeText = true;    // 提取原生文本
        private boolean extractImages = false;       // 导出图片文件
        private boolean ocrEnabled = false;          // 启用OCR识别
        private String outputDir = "./output";       // 输出目录

        // OCR 高级配置
        private String ocrLanguage = "eng";          // 识别语言
        private int ocrDpi = 300;                    // 图像分辨率
        private boolean ocrPreprocess = true;        // 图像预处理
        private float ocrConfidence = 70.0f;         // 置信度阈值(%)

        // 混合模式配置
        private boolean hybridMode = true;           // 智能混合模式
        private int textLengthThreshold = 150;       // 触发OCR的文本长度阈值

        public Config enableOCR(boolean enable) {
            this.ocrEnabled = enable;
            if (enable) {
                ensureDirExists(outputDir);
            }
            return this;
        }

        public Config setHybridThreshold(int threshold) {
            this.textLengthThreshold = Math.max(50, threshold);
            return this;
        }

        private void ensureDirExists(String path) {
            try {
                Files.createDirectories(Paths.get(path));
            } catch (IOException e) {
                throw new RuntimeException("创建目录失败: " + path);
            }
        }


        public Config extractNativeText(boolean enable) {
            this.extractNativeText = enable;
            return this;
        }

        public Config extractImages(boolean enable) {
            this.extractImages = enable;
            return this;
        }

        public Config ocrLanguage(String lang) {
            this.ocrLanguage = lang;
            return this;
        }
    }

    // ====================== 核心处理方法 ======================
    public static OcrResult process(File pdfFile, Config config) throws Exception {
        OcrResult result = new OcrResult();

        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            // 阶段1: 原生文本提取
            if (config.extractNativeText) {
                String nativeText = extractNativeText(doc);
                result.nativeText = nativeText;

                // 智能触发OCR的条件
                boolean needOcr = config.hybridMode &&
                        (nativeText.trim().length() < config.textLengthThreshold);

                if (needOcr) {
                    config.ocrEnabled = true;
                }
            }

//            // 阶段2: OCR处理
//            if (config.ocrEnabled) {
//                result.ocrText = processOCR(doc, config);
//                if (config.extractImages) {
//                    extractRawImages(doc, config.outputDir, result);
//                }
//            }
        }
        return result;
    }

    // ====================== OCR处理管道 ======================
//    private static String processOCR(PDDocument doc, Config config) throws Exception {
//        ITesseract tesseract = new Tesseract();
//        tesseract.setDatapath("/usr/share/tesseract-ocr/tessdata"); // 修改为实际路径
//        tesseract.setLanguage(config.ocrLanguage);
//        tesseract.setTessVariable("user_defined_dpi", String.valueOf(config.ocrDpi));
//
//        StringBuilder ocrResult = new StringBuilder();
//        int pageNum = 0;
//
//        for (PDPage page : doc.getPages()) {
//            pageNum++;
//            PDResources resources = page.getResources();
//
//            int imgIndex = 0;
//            for (COSName name : resources.getXObjectNames()) {
//                if (resources.isImageXObject(name)) {
//                    PDImageXObject image = (PDImageXObject) resources.getXObject(name);
//                    BufferedImage bufferedImage = image.getImage();
//
//                    // 图像预处理流水线
//                    if (config.ocrPreprocess) {
//                        bufferedImage = preprocessImage(bufferedImage, config.ocrDpi);
//                    }
//
//                    // 执行OCR并过滤结果
//                    String text = tesseract.doOCR(bufferedImage);
//                    text = filterByConfidence(text, tesseract, config.ocrConfidence);
//
//                    ocrResult.append(String.format("\n[Page %d - Image %d]\n",
//                            pageNum, ++imgIndex))
//                            .append(text);
//                }
//            }
//        }
//        return ocrResult.toString();
//    }

    // ====================== 图像优化算法 ======================
    private static BufferedImage preprocessImage(BufferedImage src, int dpi) {
        // 1. 自适应二值化
        BufferedImage grayscale = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        grayscale.getGraphics().drawImage(src, 0, 0, null);

        // 2. 对比度增强（CLAHE算法简化版）
        RescaleOp rescaleOp = new RescaleOp(1.2f, 15, null);
        rescaleOp.filter(grayscale, grayscale);

        // 3. 基于DPI的缩放
        if (dpi < 300) {
            double scale = 300.0 / dpi;
            int newWidth = (int)(grayscale.getWidth() * scale);
            int newHeight = (int)(grayscale.getHeight() * scale);
            Image scaled = grayscale.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage scaledBI = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
            scaledBI.getGraphics().drawImage(scaled, 0, 0, null);
            return scaledBI;
        }
        return grayscale;
    }

    // ====================== 置信度过滤机制 ======================
//    private static String filterByConfidence(String text, ITesseract tesseract, float threshold) {
//        // 注意：需使用tesseract 5.x以上版本获取置信度信息
//        return text; // 实际开发中需解析置信度数据
//    }

    // ====================== 辅助方法 ======================
    private static String extractNativeText(PDDocument doc) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        return stripper.getText(doc);
    }

    private static void extractRawImages(PDDocument doc, String outputDir, OcrResult result) throws IOException {
        int imgCount = 0;
        for (PDPage page : doc.getPages()) {
            PDResources resources = page.getResources();
            for (COSName name : resources.getXObjectNames()) {
                if (resources.isImageXObject(name)) {
                    PDImageXObject image = (PDImageXObject) resources.getXObject(name);
                    String filename = outputDir + "/img_" + (++imgCount) + ".png";
                    ImageIO.write(image.getImage(), "PNG", new File(filename));
                    result.imagePaths.add(filename);
                }
            }
        }
    }

    // ====================== 结果封装对象 ======================
    public static class OcrResult {
        public String nativeText = "";
        public String ocrText = "";
        public java.util.List<String> imagePaths = new ArrayList<>();

        //        public String getCombinedText() {
//            return "[Native Text]\n" + nativeText + "\n\n[OCR Text]\n" + ocrText;
//        }
        public String getCombinedText() {
            return nativeText;
        }
    }

    // ====================== 使用示例 ======================
    public static void main(String[] args) {

        try {
            String content = getContent("C:\\Users\\dush\\Desktop\\test.pdf");
            System.err.println(content.length());
            System.err.println(content);
        } catch (Exception e) {
            System.err.println("处理失败: " + e.getMessage());
        }
    }

    /**
     * 根据pdf文件地址，获取文件内容，纯文本
     * @param filePath
     * @return
     */
    public static String getContent(String filePath){
        Config config = new Config()
                .extractNativeText(true)
                .ocrLanguage("chi_sim+eng");
        try {
            File file = new File(filePath);
            OcrResult result = process(file, config);
            String combinedText = result.getCombinedText();
            return trimContent(combinedText);
        } catch (Exception e) {
            throw new RuntimeException("获取文本失败: " + e.getMessage());
        }
    }

    /**
     * 进行格式化
     * @param content
     * @return
     */
    public static String trimContent(String content){
        String cleanedText = content
                .replaceAll("-\\n", "")     // 合并断行
                .replaceAll("\\s{2,}", " ") // 压缩连续空格
                .replaceAll("(?m)^\\d+$", "") // 删除页码行
                .trim();
        return cleanedText;
    }

    /**
     * 分块分段
     * @param content
     * @return
     */
    public static java.util.List<String> formatContent(String content){
        String cleanedText=trimContent(content);
        int maxChunkSize = 5000; // 按 5000 字符分块
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < cleanedText.length(); i += maxChunkSize) {
            int end = Math.min(i + maxChunkSize, cleanedText.length());
            chunks.add(cleanedText.substring(i, end));
        }
        return chunks;
    }
}
