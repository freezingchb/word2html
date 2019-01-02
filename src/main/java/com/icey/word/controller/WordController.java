package com.icey.word.controller;

import com.icey.word.model.AliOSSPictureManager;
import com.icey.word.model.Word2Html;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 因为文件路径的问题，docx图片只有在linux上能正常替换
 */
@RestController
@RequestMapping({"/doc"})
public class WordController {

    @Value(value = "${image.tempPath}")
    private String tempPath;

    @Autowired
    private AliOSSPictureManager aliOSSPictureManager;

    @Autowired
    private Word2Html word2Html;

    @RequestMapping({"/upload"})
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            String fileName = file.getOriginalFilename();
            if (StringUtils.isBlank(fileName))
                throw new Exception("请上传文件");

            String html;
            if (fileName.endsWith("doc")) {
                html = word2Html.doc(file, aliOSSPictureManager);
            } else if(fileName.endsWith("docx")) {
                html = word2Html.docx(file, tempPath);
            } else {
                throw new Exception("文档格式仅支持Doc和Docx");
            }

            result.put("status", true);
            result.put("message", html);
        } catch (Throwable ex) {
            result.put("status", false);
            result.put("message", ex.getMessage());
        }
        return result;
    }

    @RequestMapping({"/test"})
    public void test() throws Exception {
        String a = "xxx";
        // System.out.println(new Word2Html().deleteFile(new File("C:/Users/icey/Desktop/test")));
        // a = new com.icey.word.model.AliOSS().upload(new java.io.FileInputStream("C:/Users/icey/Desktop/1812291735051498/word/media/image1.png"), "1812291735064579image1.png");
        // String a = JSONObject.toJSONString(word2Html.uploadImage("C:/Users/icey/Desktop/pictest"));
        // String[] search = {"bc", "12", "qw"};
        // String[] replace = {"b1c1", "1122", "q3w4"};
        // String a = StringUtils.replaceEach("abcdefg123qwer", search, replace);
        System.out.println(a);
    }
}
