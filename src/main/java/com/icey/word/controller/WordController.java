package com.icey.word.controller;

import com.icey.word.model.AliOSSPictureManager;
import com.icey.word.model.Image;
import com.icey.word.model.Word2Html;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
    public Map<String, Object> upload(@RequestParam("doc") MultipartFile doc) {
        Map<String, Object> result = new HashMap<>();
        try {
            String fileName = doc.getOriginalFilename();
            if (StringUtils.isBlank(fileName))
                throw new Exception("请上传文件");

            String html;
            if (fileName.endsWith("doc")) {
                html = word2Html.doc(doc, aliOSSPictureManager);
            } else if(fileName.endsWith("docx")) {
                html = word2Html.docx(doc, tempPath);
            } else {
                throw new Exception("文档格式仅支持Doc和Docx");
            }

            result.put("status", true);
            result.put("html", html);
        } catch (Throwable ex) {
            result.put("status", false);
            result.put("html", ex.getMessage());
        }
        return result;
    }

    @RequestMapping({"/show"})
    public void show(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html; charset=UTF-8");
        response.setContentType("image/jpeg");
        String file = request.getParameter("file");
        FileInputStream fis = new FileInputStream("/tmp/" + file);
        OutputStream os = response.getOutputStream();
        int count;
        byte[] buffer = new byte[1024 * 1024];
        while ((count = fis.read(buffer)) != -1)
            os.write(buffer, 0, count);
        os.flush();
        fis.close();
        os.close();
    }

    @RequestMapping({"/test"})
    public void test() throws Exception {
        String a = "xxx";
        // a = image.uploadAliOSS("C:/Users/icey/Desktop/test/566-373 - 副本.png");
        // String a = JSONObject.toJSONString(word2Html.uploadImage("C:/Users/icey/Desktop/pictest"));
        // String[] search = {"bc", "12", "qw"};
        // String[] replace = {"b1c1", "1122", "q3w4"};
        // String a = StringUtils.replaceEach("abcdefg123qwer", search, replace);
        System.out.println(a);
    }
}
