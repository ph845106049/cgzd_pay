package com.cgzd.pay.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;

import java.awt.*;
import java.io.ByteArrayOutputStream;

/**
 * @author wangning
 * @date 2021/11/30
 * @description 生成二维码并转换为base64
 */
public class QRCodeUtil {

    /**
     * 生成二维码
     *
     * @param text 二维码内容
     * @param width  宽度
     * @param height  高度
     * @return
     */
    public static String getQRCodeBase64(String text, int width, int height) {
        QrConfig config = new QrConfig(width, height);
        // 设置边距，既二维码和背景之间的边距
        config.setMargin(1);
        // 设置前景色，既二维码颜色
        config.setForeColor(Color.BLACK);
        // 设置背景色
        config.setBackColor(Color.WHITE);
        // 生成二维码到文件，也可以到流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        QrCodeUtil.generate(text, config, ImgUtil.IMAGE_TYPE_PNG, outputStream);
        byte[] pngData = outputStream.toByteArray();
        // 这个前缀，可前端加，可后端加，不加的话，不能识别为图片
        return "data:image/png;base64," + Base64.encode(pngData);
    }

    /**
     * 生成二维码
     *
     * @param text 二维码内容
     * @param config 自定义配置
     * @return
     */
    public static String getQRCodeBase64(String text, QrConfig config) {
        // 生成二维码到文件，也可以到流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        QrCodeUtil.generate(text, config, ImgUtil.IMAGE_TYPE_JPG, outputStream);
        byte[] pngData = outputStream.toByteArray();
        return "data:image/png;base64," + Base64.encode(pngData);
    }
}
