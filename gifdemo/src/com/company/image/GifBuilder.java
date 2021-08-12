package com.company.image;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

public class GifBuilder {

    private List<BufferedImage> images;

    private String outputFilePath;

    private boolean loop;

    private int delayTime;

    public GifBuilder(List<BufferedImage> images, boolean loop, int circleTime, String outputFilePath) {
        this.images = images;
        this.loop = loop;
        this.delayTime = circleTime;
        this.outputFilePath = outputFilePath;
    }

    public void build() throws IOException {
        ImageOutputStream iioStream =
                ImageIO.createImageOutputStream(new FileOutputStream(outputFilePath));
        ImageWriter writer =
                ImageIO.getImageWritersByMIMEType("image/gif").next();
        writer.setOutput(iioStream);

        writer.prepareWriteSequence(null);
        for (int i = 0; i < images.size(); i++) {
            BufferedImage frame = images.get(i);
            ImageTypeSpecifier type =
                    ImageTypeSpecifier.createFromRenderedImage(frame);
            IIOMetadata metadata = writer.getDefaultImageMetadata(type, null);
            configureRootMetadata(metadata);
            writer.writeToSequence(new IIOImage(frame, null, metadata), null);
        }

        writer.endWriteSequence();
        writer.dispose();
    }

    //设置每帧的属性
    private void configureRootMetadata(IIOMetadata metadata) throws IIOInvalidTreeException {
        String metaFormatName = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delayTime)); //设置帧的速率
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");

        int loopContinuously = loop ? 0 : 1;
        child.setUserObject(new byte[]{0x1, (byte) (loopContinuously & 0xFF), (byte) ((loopContinuously >> 8) & 0xFF)});
        appExtensionsNode.appendChild(child);

        metadata.setFromTree(metaFormatName, root);
    }

    private IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return (node);
    }
}
