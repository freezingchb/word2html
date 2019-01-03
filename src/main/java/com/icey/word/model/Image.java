package com.icey.word.model;

import com.aliyun.oss.OSSClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class Image {

    @Value(value = "${aliYun.OSS.endpoint}")
    private String endpoint;

    @Value(value = "${aliYun.OSS.accessKeyId}")
    private String accessKeyId;

    @Value(value = "${aliYun.OSS.accessKeySecret}")
    private String accessKeySecret;

    @Value(value = "${aliYun.OSS.bucketName}")
    private String bucketName;

    /**
     * 图片最大宽度
     */
    private int imageMaxWidth = 550;

    /**
     * 上传文件至阿里云OSS
     *
     * @param filePath 文件绝对地址
     * @return
     */
    public String uploadAliOSS(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists())
            return "";

        // FileUtils.copyFile(file, new File("/tmp/oss" + file.getName()));
        // return "http://127.0.0.1:8088/doc/show?file=oss" + file.getName();
        InputStream inputStream = new FileInputStream(file);
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, file.getName(), inputStream);
        ossClient.shutdown();
        inputStream.close();

        StringBuffer sb = new StringBuffer();
        sb.append("http://");
        sb.append(bucketName);
        sb.append(".");
        sb.append(StringUtils.substring(endpoint, 7));
        sb.append("/");
        sb.append(file.getName());
        return sb.toString();
    }

    /**
     * 将生成的图片传到阿里云
     *
     * @param imageFolderPath 图片目录地址
     * @return
     * @throws IOException
     */
    public Map<String, String[]> uploadImage(String imageFolderPath) throws IOException {
        List<String> origin = new ArrayList<>();
        List<String> current = new ArrayList<>();

        File file = new File(imageFolderPath);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length != 0) {
                for (File item : files) {
                    if (item.isFile()) {
                        // 本地图片处理
                        String absolutePath = item.getAbsolutePath();
                        InputStream inputStream = new FileInputStream(item);
                        this.widenImage(inputStream, absolutePath); // 改变图片宽度
                        origin.add(absolutePath);
                        inputStream.close();

                        // OSS图片
                        current.add(this.uploadAliOSS(absolutePath));
                    }
                }
            }
        }

        Map<String, String[]> map = new HashMap<>();
        String[] origins = origin.toArray(new String[origin.size()]);
        String[] currents = current.toArray(new String[current.size()]);
        map.put("origin", origins);
        map.put("current", currents);
        return map;
    }

    /**
     * 等比改变图片宽度
     *
     * @param inputStream 图片输入流
     * @param imagePath 目标图片绝对地址
     */
    public void widenImage(InputStream inputStream, String imagePath) {
        try {
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            String fileSuffix = StringUtils.substring(imagePath, imagePath.lastIndexOf(".") + 1);

            // 设定新宽高
            if (new File(imagePath).exists() && width <= this.imageMaxWidth)
                return;
            int newWidth = width <= this.imageMaxWidth ? width : this.imageMaxWidth;
            int newHeight = newWidth * height / width;

            // 构建图片流
            BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            newImage.getGraphics().drawImage(bufferedImage, 0, 0, newWidth, newHeight, null);

            // 输出流
            OutputStream outStream = new FileOutputStream(imagePath);
            BufferedOutputStream outputStream = new BufferedOutputStream(outStream);
            ImageIO.write(newImage, fileSuffix, outputStream);

            outStream.close();
            outputStream.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * 删除文件或目录
     *
     * @param file
     * @return
     */
    public boolean deleteFile(File file) {
        if (!file.exists())
            return false;

        if (file.isFile()) {
            return file.delete();
        } else {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File fileItem : files) {
                    deleteFile(fileItem);
                }
            }
        }
        return file.delete();
    }

    /**
     * 产生唯一的随机数字符串
     *
     * @return
     */
    public String createUniqueStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(new SimpleDateFormat("yyMMddHHmmss").format(new Date()));

        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            sb.append(Integer.toString(random.nextInt(10)));
        }
        return sb.toString();
    }
}
