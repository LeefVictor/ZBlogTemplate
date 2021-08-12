package com.company.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;

public class SplitSprites {

    private BufferedImage bi;
    private String outputDir;
    private int width;
    //分割图片中最大的图片宽度
    private int subImgMaxWidth;
    private int height;
    private List<BufferedImage> imagesAfterSplit = new ArrayList<>();

    private Map<Integer, Img> allImgs = new HashMap<>();

    public SplitSprites(String spritesImgPath, String outputDir) throws IOException {
        this.bi = ImageIO.read(new File(spritesImgPath));
        this.outputDir = outputDir;
        this.width = bi.getWidth();
        this.height = bi.getHeight();
    }

    public List<BufferedImage> getImagesAfterSplit() {
        return imagesAfterSplit;
    }

    //按列扫描， 只要不出现整列都是空白的， 认为是一张图。 记录起始和结束坐标， 得到图片大小并记录
    public SplitSprites scanByCol() throws IOException {
        int currentImage = 0;
        //是否在扫描着一副命中的图片
        boolean in = false;

        for (int i = 0; i < width; i++) {
            boolean scanPx = false;
            for (int i1 = 0; i1 < height; i1++) {
                int rgb = bi.getRGB(i, i1);
                scanPx = scanPx || (rgb != -1);
                if (scanPx) {
                    break;
                }
            }

            //兼容， 避免刚好最后一个像素点就是结束, 当最后一个x坐标的像素列扫描后scanPx仍为true， 设置其为false， 表示结束了
            if (i == width - 1) {
                if (scanPx) {
                    scanPx = false;
                }
            }

            //表示前在扫描一个图片的，但是这次整列像素都是空白， 证明这幅图结束了。
            if (in && !scanPx) {
                in = false;
                allImgs.get(currentImage).endX = i;
                //计算分割图片的最大宽度
                int temp = allImgs.get(currentImage).endX - allImgs.get(currentImage).startX;
                if (temp > subImgMaxWidth) {
                    subImgMaxWidth = temp;
                }
            }

            //未在扫描进行时， 扫描到一个非透明像素点，证明开始扫描下一幅图片了。
            if (!in && scanPx) {
                in = true;
                currentImage += 1;
                allImgs.put(currentImage, new Img());
                allImgs.get(currentImage).startX = i;
            }
        }

        System.out.println("扫描到" + allImgs.size() + "张图片");
        return this;
    }

    public SplitSprites split() throws IOException {
        //以所有图片中最大宽度的图片， 并以此为标准进行保存
        for (Entry<Integer, Img> entry : allImgs.entrySet()) {
            Img img = entry.getValue();
            BufferedImage splitImg = expand(bi.getSubimage(img.startX, 0, img.endX - img.startX, height));
            //ImageIO.write(splitImg, "png", new File(outputDir + File.separator + entry.getKey() + ".png"));
            imagesAfterSplit.add(splitImg);
        }
        return this;
    }

    public SplitSprites save() throws IOException {
        for (int i = 0; i < imagesAfterSplit.size(); i++) {
            BufferedImage bufferedImage = imagesAfterSplit.get(i);
            ImageIO.write(bufferedImage, "png", new File(outputDir + File.separator + i + ".png"));
        }
        return this;
    }

    private BufferedImage expand(BufferedImage image) {
        int currentWidth = image.getWidth();
        if (subImgMaxWidth == currentWidth) {
            return image;
        }
        //为了保持动作处于整个图片中心， 所以是同时往两部扩展
        int start = (subImgMaxWidth - currentWidth) / 2;

        BufferedImage newImage = new BufferedImage(subImgMaxWidth, image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        // 初始化的像素点设为透明
        for (int i = 0; i < newImage.getWidth(); i++) {
            for (int i1 = 0; i1 < newImage.getHeight(); i1++) {
                newImage.setRGB(i, i1, -1);
            }
        }

        //将旧的图片流写入新的图片流
        Graphics2D graphics2D = newImage.createGraphics();
        graphics2D.drawImage(image, null, start, 0);
        graphics2D.dispose();

        return newImage;
    }

    public static void main(String[] args) throws IOException {
        SplitSprites ss = new SplitSprites("D:\\temp\\sprites.png", "D:\\temp\\gif\\split").scanByCol().split().save();
        new GifBuilder(ss.getImagesAfterSplit(), true, 10, "D:\\temp\\gif\\test.gif").build();
    }


    static class Img {

        private int startX;
        private int endX;
    }
}
