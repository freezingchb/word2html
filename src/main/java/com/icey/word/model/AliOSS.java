package com.icey.word.model;


import com.aliyun.oss.OSSClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class AliOSS {

    @Value(value = "${aliYun.OSS.endpoint}")
    private String endpoint;

    @Value(value = "${aliYun.OSS.accessKeyId}")
    private String accessKeyId;

    @Value(value = "${aliYun.OSS.accessKeySecret}")
    private String accessKeySecret;

    @Value(value = "${aliYun.OSS.bucketName}")
    private String bucketName;

    /**
     * 上传文件至阿里云OSS
     *
     * @param input 文件流
     * @param objectName 最终展示的文件名
     * @return
     */
    public String upload(InputStream input, String objectName) {
//        return "http://drbd01.oss-cn-shanghai.aliyuncs.com/" + objectName;
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, objectName, input);
        ossClient.shutdown();

        StringBuffer sb = new StringBuffer();
        sb.append("http://");
        sb.append(bucketName);
        sb.append(".");
        sb.append(StringUtils.substring(endpoint, 7));
        sb.append("/");
        sb.append(objectName);
        return sb.toString();
    }
}
