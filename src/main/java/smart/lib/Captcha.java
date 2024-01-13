package smart.lib;


import org.springframework.util.StringUtils;
import smart.lib.session.Session;
import smart.util.Helper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 验证码
 */
public class Captcha {
    // 验证码长度
    public final static long SIZE = 5;
    private static final char[] mapTable = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '0', '1',
            '2', '3', '4', '5', '6', '7',
            '8', '9'};

    /**
     * 删除会话中的验证码
     *
     * @param session 要删除的会话
     */
    public static void clear(Session session) {
        if (session == null) {
            return;
        }
        session.delete(StringUtils.uncapitalize(Captcha.class.getSimpleName()));
    }

    /**
     * 获取验证码(size: 90px * 32px)
     *
     * @return 验证码
     */
    public static CaptchaResult getImageCode() {
        int width = 90, height = 32;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 获取图形上下文
        Graphics g = image.getGraphics();
        //生成随机类
        Random random = new Random();
        // 矩形
        g.fillRect(0, 0, width, height);

        //设定字体
        g.setFont(new Font(null, Font.PLAIN, 24));

        //取随机产生的码
        StringBuilder phrase = new StringBuilder();
        //4代表4位验证码,如果要生成更多位的认证码,则加大数值
        for (int i = 0; i < Captcha.SIZE; ++i) {
            phrase.append(mapTable[(int) (mapTable.length * Math.random())]);
            // 将认证码显示到图象中
            g.setColor(getRandomColor());
            // 直接生成
            String str = phrase.substring(i, i + 1);
            // 设置随便码在背景图图片上的位置
            g.drawString(str, 16 * i + 6, 25);
        }
        // 干扰线
        for (int i = 0; i < 10; i++) {
            // 设置随机颜色
            g.setColor(getRandomColor());
            // 随机画线
            g.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }
        // 释放图形上下文
        g.dispose();
        return new CaptchaResult(image, phrase.toString());
    }

    /**
     * 获得随机颜色
     *
     * @return Color
     */
    static Color getRandomColor() {
        Random ran = new Random();
        return new Color(40 + ran.nextInt(180), 40 + ran.nextInt(180), 40 + ran.nextInt(180));
    }

    /**
     * @param image  验证码图片实例
     * @param phrase 验证码
     */
    public record CaptchaResult(BufferedImage image, String phrase) {
    }
}