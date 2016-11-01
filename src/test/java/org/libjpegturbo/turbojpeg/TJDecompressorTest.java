package org.libjpegturbo.turbojpeg;

import java.awt.image.BufferedImage;
import static org.junit.Assert.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class TJDecompressorTest {
    TJDecompressor decompressor;    
    
    @Before
    public void setup() throws Exception {
        this.decompressor = new TJDecompressor();
    }
    @Test
    public void testDecompress() throws Exception{
        InputStream is = getClass().getClassLoader().getResourceAsStream("ocean.jpg");
        final byte[] jpegImage = IOUtils.toByteArray(is);
        
        decompressor.setSourceImage(jpegImage, jpegImage.length);
        int width = decompressor.getWidth();
        assertEquals(720,width);
        int height = decompressor.getHeight();
        assertEquals(960,height);
        final byte[] uncompressedImage = new byte[width * height * 3];
        decompressor.decompress(uncompressedImage, 0, 0, width, 0, height, TJ.PF_BGR, TJ.FLAG_FASTDCT);
        assertEquals(519088164,Arrays.hashCode(uncompressedImage));

    }

}
