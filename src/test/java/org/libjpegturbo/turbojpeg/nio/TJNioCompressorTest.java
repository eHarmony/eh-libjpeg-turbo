package org.libjpegturbo.turbojpeg.nio;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJBench;
import org.libjpegturbo.turbojpeg.TJCompressor;
import org.libjpegturbo.turbojpeg.bridj.TurbojpegLibrary.TJPF;
import org.libjpegturbo.turbojpeg.bridj.TurbojpegLibrary.TJSAMP;

public class TJNioCompressorTest {

    TJNioCompressor compressor;    
    
    @Before
    public void setup() throws Exception {
        this.compressor = new TJNioCompressor();
    }
    // Requires turbojpeg library to be integrated properly into maven
    @Ignore
    @Test
    public void testCompressBGR() throws Exception{
        BufferedImage img = ImageIO.read(getClass().getClassLoader().getResource("ocean.jpg"));
        int [] widthHolder = new int[1];
        int [] heightHolder = new int[1];
        int pixelFormat = TJ.PF_BGR;
        byte[] srcImage = TJBench.loadImage(img, widthHolder, heightHolder, pixelFormat);
        ByteBuffer srcImageBuffer = ByteBuffer.allocateDirect(srcImage.length);
        srcImageBuffer.put(srcImage);
        srcImageBuffer.rewind();
        compressor.setJPEGQuality(50);
        compressor.setSourceImage(srcImageBuffer, widthHolder[0], 0, heightHolder[0], TJPF.TJPF_BGR, TJSAMP.TJSAMP_420);
        ByteBuffer destImage = compressor.compress(0);
        assertEquals(53961,destImage.limit());
        // write out the file for visual inspection
        
        try (FileOutputStream fos = new FileOutputStream(new File("ocean-TJNioCompressor-bgr.jpg"),false)) {                       
              FileChannel channel = fos.getChannel();
              channel.write(destImage);
          } catch (Exception e) {
              e.printStackTrace();
          }
        

    }
    // Requires turbojpeg library to be integrated properly into maven
    @Ignore
    @Test
    public void testCompressRGB() throws Exception{
        BufferedImage img = ImageIO.read(getClass().getClassLoader().getResource("ocean.jpg"));
        int [] widthHolder = new int[1];
        int [] heightHolder = new int[1];
        int pixelFormat = TJ.PF_RGB;
        byte[] srcImage = TJBench.loadImage(img, widthHolder, heightHolder, pixelFormat);
        compressor.setJPEGQuality(50);
        ByteBuffer srcImageBuffer = ByteBuffer.allocateDirect(srcImage.length);
        srcImageBuffer.put(srcImage);
        srcImageBuffer.rewind();
        compressor.setSourceImage(srcImageBuffer, widthHolder[0], 0, heightHolder[0], TJPF.TJPF_RGB, TJSAMP.TJSAMP_444);
        ByteBuffer destImage = compressor.compress(0);
        assertEquals(61878,destImage.limit());
        // write out the file for visual inspection
        
        try (FileOutputStream fos = new FileOutputStream(new File("ocean-TJNioCompressor-rgb.jpg"),false)) {                       
              FileChannel channel = fos.getChannel();
              channel.write(destImage);
          } catch (Exception e) {
              e.printStackTrace();
          }
    }
    // Requires turbojpeg library to be integrated properly into maven
    @Ignore
    @Test
    public void testCompressBGRBig() throws Exception{
        BufferedImage img = ImageIO.read(getClass().getClassLoader().getResource("jungle.jpg"));
        int [] widthHolder = new int[1];
        int [] heightHolder = new int[1];
        int pixelFormat = TJ.PF_BGRA;
        byte[] srcImage = TJBench.loadImage(img, widthHolder, heightHolder, pixelFormat);
        compressor.setJPEGQuality(50);
        ByteBuffer srcImageBuffer = ByteBuffer.allocateDirect(srcImage.length);
        srcImageBuffer.put(srcImage);
        srcImageBuffer.rewind();
        compressor.setSourceImage(srcImageBuffer, widthHolder[0], 0, heightHolder[0], TJPF.TJPF_BGRA, TJSAMP.TJSAMP_422);
        ByteBuffer destImage = compressor.compress(0);
        assertEquals(1036269,destImage.limit());
        // write out the file for visual inspection
        try (FileOutputStream fos = new FileOutputStream(new File("jungle-TJNioCompressor-bgra.jpg"),false)) {                       
              FileChannel channel = fos.getChannel();
              channel.write(destImage);
          } catch (Exception e) {
              e.printStackTrace();
          }
    }
    

}
