package org.libjpegturbo.turbojpeg;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Test;

public class TJCompressorTest {
    TJCompressor compressor;    
    
    @Before
    public void setup() throws Exception {
        this.compressor = new TJCompressor();
    }
    @Test
    public void testCompress() throws Exception{
        BufferedImage img = ImageIO.read(getClass().getClassLoader().getResource("ocean.jpg"));
        int [] widthHolder = new int[1];
        int [] heightHolder = new int[1];
        int pixelFormat = TJ.PF_BGR;
        byte[] srcImage = TJBench.loadImage(img, widthHolder, heightHolder, pixelFormat);
        compressor.setJPEGQuality(50);
        compressor.setSubsamp(TJ.SAMP_420);
        compressor.setSourceImage(srcImage, 0, 0, widthHolder[0], 0, heightHolder[0], pixelFormat);
        byte[] destImage = compressor.compress(0);
        try (FileOutputStream fos = new FileOutputStream("ocean-tjcompressor.jpg")) {
            fos.write(destImage, 0, compressor.getCompressedSize());
        }

    }

}
