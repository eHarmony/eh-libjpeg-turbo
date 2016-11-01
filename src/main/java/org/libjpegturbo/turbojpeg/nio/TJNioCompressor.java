/*
 * Copyright (C)2011-2015 D. R. Commander.  All Rights Reserved.
 * Copyright (C)2015 Viktor Szathmáry.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the libjpeg-turbo Project nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS",
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.libjpegturbo.turbojpeg.nio;

import java.io.Closeable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.bridj.CLong;
import org.bridj.Pointer;
import org.bridj.Pointer.StringType;
import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJException;
import org.libjpegturbo.turbojpeg.YUVImage;
import org.libjpegturbo.turbojpeg.bridj.TurbojpegLibrary;

/**
 * TurboJPEG compressor
 */
public class TJNioCompressor implements Closeable {

    private static final String NO_ASSOC_ERROR = "No source image is associated with this instance";
    private ByteBuffer srcBuf = null;
    private int srcWidth = 0;
    private int srcHeight = 0;
    private int srcPitch = 0;
    private TurbojpegLibrary.TJPF srcPixelFormat = null;
    private TurbojpegLibrary.TJSAMP subsamp = TurbojpegLibrary.TJSAMP.TJSAMP_444;
    private int jpegQuality = -1;
    private int compressedSize = 0;
    Pointer<Byte> pointerToSrcBuf;
    Pointer<Pointer<Byte>> pointerPointerToDstBuf;
    ByteBuffer dstBuf;
    final Pointer<CLong> pointerToCompressedSize;
    final Pointer<?> pointerToCompressor;
    
    public TJNioCompressor() throws TJException {
        this.pointerToCompressor = TurbojpegLibrary.tjInitCompress();
        this.pointerPointerToDstBuf = (Pointer<Pointer<Byte>>) Pointer.NULL;
        this.pointerToCompressedSize = Pointer.allocateCLong();
        byte z = (byte)0;
        this.pointerToSrcBuf = Pointer.pointerToBytes(z,z,z,z,z,z,z,z,z,z,z,z,z,z,z,z);

    }


    /**
     * Associate an uncompressed RGB, grayscale, or CMYK source image with this compressor instance.
     *
     * @param srcImage
     *            image buffer containing RGB, grayscale, or CMYK pixels to be compressed or encoded. This buffer is not
     *            modified.
     *
     * @param width
     *            width (in pixels) of the region in the source image from which the JPEG or YUV image should be
     *            compressed/encoded
     *
     * @param pitch
     *            bytes per line of the source image. Normally, this should be
     *            <code>width * TJ.pixelSize(pixelFormat)</code> if the source image is unpadded, but you can use this
     *            parameter to, for instance, specify that the scanlines in the source image are padded to a 4-byte
     *            boundary or to compress/encode a JPEG or YUV image from a region of a larger source image. You can
     *            also be clever and use this parameter to skip lines, etc. Setting this parameter to 0 is the
     *            equivalent of setting it to <code>width * TJ.pixelSize(pixelFormat)</code>.
     *
     * @param height
     *            height (in pixels) of the region in the source image from which the JPEG or YUV image should be
     *            compressed/encoded
     *
     * @param pixelFormat
     *            pixel format of the source image (one of {@link TJ#PF_RGB TJ.PF_*})
     */
    public void setSourceImage(ByteBuffer srcImageBuffer, int width, int pitch, int height, TurbojpegLibrary.TJPF pixelFormat, TurbojpegLibrary.TJSAMP subsamp)
            throws TJException {
        if (srcImageBuffer == null || width < 1 || height < 1 || pitch < 0 || pixelFormat == null)
            throw new IllegalArgumentException("Invalid argument in setSourceImage()");
        this.srcBuf = srcImageBuffer;
        this.srcWidth = width;
        if (pitch == 0)
            this.srcPitch = width * TJ.getPixelSize((int)pixelFormat.value);
        else
            this.srcPitch = pitch;
        this.srcHeight = height;
        this.srcPixelFormat = pixelFormat;
        this.subsamp = subsamp;

        this.pointerToSrcBuf = Pointer.pointerToBytes(srcBuf);

        int requiredSize = (int) TurbojpegLibrary.tjBufSize(width, height, (int)subsamp.value());
        if (dstBuf == null || dstBuf.capacity() < requiredSize) {
            dstBuf = ByteBuffer.allocateDirect(requiredSize)
                    .order(ByteOrder.nativeOrder());
            pointerPointerToDstBuf = Pointer.pointerToPointer(Pointer.pointerToBytes(dstBuf));
        } else {
            dstBuf.rewind();
        }
        


    }


    /**
     * Set the JPEG image quality level for subsequent compress operations.
     *
     * @param quality
     *            the new JPEG image quality level (1 to 100, 1 = worst, 100 = best)
     */
    public void setJPEGQuality(int quality) {
        if (quality < 1 || quality > 100)
            throw new IllegalArgumentException("Invalid argument in setJPEGQuality()");
        jpegQuality = quality;
    }

    /**
     * Compress the uncompressed source image associated with this compressor instance and output a JPEG image to the
     * given destination buffer.
     *
     * @param dstBuf
     *            buffer that will receive the JPEG image. Use {@link TJ#bufSize} to determine the maximum size for this
     *            buffer based on the source image's width and height and the desired level of chrominance subsampling.
     *
     * @param flags
     *            the bitwise OR of one or more of {@link TJ#FLAG_BOTTOMUP TJ.FLAG_*}
     */
    public ByteBuffer compress(int flags) throws TJException {
        if (flags < 0)
            throw new IllegalArgumentException("Invalid argument in compress()");
        if (srcBuf == null)
            throw new IllegalStateException(NO_ASSOC_ERROR);
        if (jpegQuality < 0)
            throw new IllegalStateException("JPEG Quality not set");

        
        pointerToCompressedSize.set(CLong.valueOf(0));
            final int errorCode = TurbojpegLibrary.tjCompress2(pointerToCompressor, // handle
                    pointerToSrcBuf, // srcBuf
                    this.srcWidth, // width
                    this.srcPitch, // pitch
                    this.srcHeight, // height
                    (int)this.srcPixelFormat.value(), // pixelFormat

                    pointerPointerToDstBuf, // jpegBuf
                    pointerToCompressedSize, //jpegSize
                    (int)this.subsamp.value(), // jpegSubsamp
                    this.jpegQuality, // jpegquality
                    TurbojpegLibrary.TJFLAG_NOREALLOC | flags); // flags, do not reallocate destination buffer
            if (errorCode > 0) {
                throw new TJException("Could not compress due to errorCode " + errorCode + ", " + TurbojpegLibrary.tjGetErrorStr().getString(StringType.C)) ;
            }
            compressedSize = (int)pointerToCompressedSize.getCLong();
            dstBuf.rewind();
            dstBuf.limit(compressedSize);
//            ByteBuffer dstBuf = pointerPointerToDstBuf.getPointerAtIndex(0).getByteBuffer(compressedSize);
//            dstBuf.limit(compressedSize);
            return dstBuf;
    }




    /**
     * Returns the size of the image (in bytes) generated by the most recent compress operation.
     *
     * @return the size of the image (in bytes) generated by the most recent compress operation.
     */
    public int getCompressedSize() {
        return compressedSize;
    }

    /**
     * Free the native structures associated with this compressor instance.
     */
    @Override
    public void close() throws TJException {
        if (this.pointerToCompressor != null) {
            TurbojpegLibrary.tjDestroy(pointerToCompressor);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } catch (TJException e) {
        } finally {
            super.finalize();
        }
    };


    private void checkSourceImage() {
        if (srcWidth < 1 || srcHeight < 1)
            throw new IllegalStateException(NO_ASSOC_ERROR);
    }

}
