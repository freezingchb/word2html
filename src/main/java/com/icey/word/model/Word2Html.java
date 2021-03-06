package com.icey.word.model;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.core.FileURIResolver;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

@Service
public class Word2Html {

    @Autowired
    private Image image;

    public String docx(MultipartFile file, String tempPath) throws Throwable {
        // 定义图片保存目录
        tempPath += "/" + image.createUniqueStr();

        // 生成 XWPFDocument
        InputStream inputStream = file.getInputStream();
        XWPFDocument document = new XWPFDocument(inputStream);

        // 准备 XHTML 选项 (设置 IURIResolver，把图片放到文件绝对路径下image/word/media文件夹)
        File imageFolderFile = new File(tempPath);
        XHTMLOptions options = XHTMLOptions.create().URIResolver(new FileURIResolver(imageFolderFile));
        options.setExtractor(new FileImageExtractor(imageFolderFile));
        options.setIgnoreStylesIfUnused(false);
        options.setFragment(true);

        // 将XWPFDocument 转换为 XHTML
        String htmlFile = tempPath + ".html";
        File htmlFileObj = new File(htmlFile);
        OutputStream outputStream = new FileOutputStream(htmlFileObj);
        XHTMLConverter.getInstance().convert(document, outputStream, options);

        // 文件转内容并把图片传到阿里云
        String htmlContent = this.getHtmlContent(htmlFile);
        Map<String, String[]> map = image.uploadImage(tempPath + "/word/media");

        inputStream.close();
        outputStream.close();

        // 删除目录
        image.deleteFile(htmlFileObj);
        image.deleteFile(imageFolderFile);

        return StringUtils.replaceEach(htmlContent, map.get("origin"), map.get("current"));
    }

    public String doc(MultipartFile file, PicturesManager picturesManager) throws Throwable {
        // 生成 HWPFDocument
        InputStream inputStream = file.getInputStream();
        HWPFDocument wordDocument = new HWPFDocument(inputStream);

        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        wordToHtmlConverter.setPicturesManager(picturesManager);
        wordToHtmlConverter.processDocument(wordDocument);
        Document htmlDocument = wordToHtmlConverter.getDocument();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DOMSource domSource = new DOMSource(htmlDocument);
        StreamResult streamResult = new StreamResult(outStream);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.METHOD, "html");
        serializer.transform(domSource, streamResult);
        outStream.close();
        inputStream.close();
        return new String(outStream.toByteArray());
    }

    /**
     * 获取html中内容
     *
     * @param filePath 文件绝对地址
     * @return
     * @throws IOException
     */
    private String getHtmlContent(String filePath) throws IOException {
        FileReader reader = new FileReader(new File(filePath));
        BufferedReader bufferedReader = new BufferedReader(reader);

        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = bufferedReader.readLine()) != null) {
            sb.append(s);
        }
        bufferedReader.close();
        return sb.toString();
    }
}
