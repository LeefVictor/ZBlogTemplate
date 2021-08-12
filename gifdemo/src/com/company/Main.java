package com.company;

import com.company.image.GifBuilder;
import com.company.image.SplitSprites;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        SplitSprites ss = new SplitSprites("D:\\temp\\sprites.png", "D:\\temp\\gif\\split").scanByCol().split().save();
        new GifBuilder(ss.getImagesAfterSplit(), true, 10, "D:\\temp\\gif\\test.gif").build();
    }
}
