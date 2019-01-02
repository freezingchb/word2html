package com.icey.word.model;

import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class AliOSSPictureManager implements PicturesManager {

    @Autowired
    private Word2Html word2Html;

    @Autowired
    private AliOSS aliOSS;

    public String savePicture(byte[] bytes, PictureType pictureType, String suggestedName, float widthInches, float heightInches) {
        String fileName = word2Html.createUniqueStr() + "." + pictureType.getExtension();
        return aliOSS.upload(new ByteArrayInputStream(bytes), fileName);
    }
}
