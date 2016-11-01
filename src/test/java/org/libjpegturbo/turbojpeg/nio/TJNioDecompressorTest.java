package org.libjpegturbo.turbojpeg.nio;

import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.bridj.TurbojpegLibrary.TJPF;

public class TJNioDecompressorTest {
    TJNioDecompressor decompressor;    
    
    @Before
    public void setup() throws Exception {
        this.decompressor = new TJNioDecompressor();
    }
    // Requires turbojpeg library to be integrated properly into maven
    @Ignore
    @Test
    public void testDecompress() throws Exception{
        InputStream is = getClass().getClassLoader().getResourceAsStream("ocean.jpg");
        final byte[] jpegImage = IOUtils.toByteArray(is);
        ByteBuffer source = ByteBuffer.allocateDirect(jpegImage.length).order(ByteOrder.nativeOrder());
        source.put(jpegImage);
        decompressor.setSourceImage(source, TJPF.TJPF_BGR);
        int width = decompressor.getWidth();
        assertEquals(720,width);
        int height = decompressor.getHeight();
        assertEquals(960,height);
        ByteBuffer buf = decompressor.decompress(TJ.FLAG_FASTDCT);
        assertEquals(720 * 960 * 3,buf.limit());
        final byte[] uncompressedImage = new byte[buf.limit()];
        buf.get(uncompressedImage);
        assertEquals(519088164,Arrays.hashCode(uncompressedImage));

    }

}
