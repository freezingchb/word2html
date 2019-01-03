package com.icey.word.model;

import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class AliOSSPictureManager implements PicturesManager {

    @Value(value = "${image.tempPath}")
    private String tempPath;

    @Autowired
    private Image image;

    public String savePicture(byte[] bytes, PictureType pictureType, String suggestedName, float widthInches, float heightInches) {
        try {
            String filePath = tempPath + "/" + image.createUniqueStr() + "." + pictureType.getExtension();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            image.widenImage(inputStream, filePath);
            inputStream.close();

            String url = image.uploadAliOSS(filePath);
            image.deleteFile(new File(filePath)); // 删除文件
            return url;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return "";
        }
    }
}
