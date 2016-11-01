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

import java.awt.image.*;
import java.nio.*;
import java.io.*;

import org.bridj.CLong;
import org.bridj.Pointer;
import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJException;
import org.libjpegturbo.turbojpeg.bridj.TurbojpegLibrary;
import org.libjpegturbo.turbojpeg.bridj.TurbojpegLibrary.TJCS;
import org.libjpegturbo.turbojpeg.bridj.TurbojpegLibrary.TJPF;
import org.libjpegturbo.turbojpeg.bridj.TurbojpegLibrary.TJSAMP;

/**
 * TurboJPEG decompressor
 */
public class TJNioDecompressor implements Closeable {

    private static final String NO_ASSOC_ERROR = "No JPEG image is associated with this instance";
    protected int jpegWidth = 0;
    protected int jpegHeight = 0;
    protected int jpegSubsamp = -1;
    protected int jpegColorspace = -1;
    Pointer<Byte> pointerToSrcBuf;
    Pointer<Byte> pointerToDstBuf;
    ByteBuffer dstBuf;
    long jpegSize;
    final Pointer<?> pointerToDecompressor;
    final private Pointer<Integer> pointerToWidth;
    final private Pointer<Integer> pointerToHeight;
    final private Pointer<Integer> pointerToJpegSubsamp;
    final private Pointer<Integer> pointerToJpegColorspace;
    private TJPF pixelFormat;

    /**
     * Create a TurboJPEG decompresssor instance.
     */
    public TJNioDecompressor() throws TJException {
        this.pointerToDecompressor = TurbojpegLibrary.tjInitDecompress();
        this.pointerToDstBuf = (Pointer<Byte>) Pointer.NULL;
        byte z = (byte)0;
        this.pointerToSrcBuf = (Pointer<Byte>) Pointer.NULL;
        this.pointerToHeight = Pointer.allocateInt();
        this.pointerToWidth = Pointer.allocateInt();
        this.pointerToJpegSubsamp = Pointer.allocateInt();
        this.pointerToJpegColorspace = Pointer.allocateInt();
   
    }




    /**
     * Associate the JPEG image of length <code>imageSize</code> bytes stored in <code>jpegImage</code> with this
     * decompressor instance. This image will be used as the source image for subsequent decompress operations.
     *
     * @param jpegImage
     *            JPEG image buffer. This buffer is not modified.
     *
     * @param imageSize
     *            size of the JPEG image (in bytes)
     */
    public void setSourceImage(ByteBuffer jpegImage, TJPF pixelFormat) throws TJException {
        
        if (jpegImage == null || jpegImage.limit() < 1)
            throw new IllegalArgumentException("Invalid argument in setSourceImage()");
        this.pointerToSrcBuf = Pointer.pointerToBytes(jpegImage);
        this.pixelFormat = pixelFormat;
        jpegSize = (long)jpegImage.limit();
        TurbojpegLibrary.tjDecompressHeader3(pointerToDecompressor, pointerToSrcBuf, jpegSize, pointerToWidth, pointerToHeight, pointerToJpegSubsamp, pointerToJpegColorspace);

        int pixelSize = TJ.getPixelSize((int)pixelFormat.value());
        int requiredSize = getWidth() * getHeight() * pixelSize;
        if (this.dstBuf == null || this.dstBuf.capacity() < requiredSize) {
            this.dstBuf = ByteBuffer.allocateDirect(requiredSize)
                    .order(ByteOrder.nativeOrder());
        } else {
            this.dstBuf.rewind();
        }
        pointerToDstBuf = Pointer.pointerToBytes(this.dstBuf);

    }



    /**
     * Returns the width of the source image (JPEG or YUV) associated with this decompressor instance.
     *
     * @return the width of the source image (JPEG or YUV) associated with this decompressor instance.
     */
    public int getWidth() {
        return pointerToWidth.getInt();
    }

    /**
     * Returns the height of the source image associated with this decompressor instance.
     *
     * @return the height of the source image associated with this decompressor instance.
     */
    public int getHeight() {
        return pointerToHeight.getInt();
    }

    /**
     * Returns the level of chrominance subsampling used in the source image (JPEG or YUV) associated with this
     * decompressor instance. See {@link TJ#SAMP_444 TJ.SAMP_*}.
     *
     * @return the level of chrominance subsampling used in the source image (JPEG or YUV) associated with this
     *         decompressor instance.
     */
    public TJSAMP getSubsamp() {
        return (TJSAMP) TJSAMP.fromValue(pointerToJpegSubsamp.getInt());
    }

    /**
     * Returns the colorspace used in the source image (JPEG or YUV) associated with this decompressor instance. See
     * {@link TJ#CS_RGB TJ.CS_*}. If the source image is YUV, then this always returns {@link TJ#CS_YCbCr}.
     *
     * @return the colorspace used in the source image (JPEG or YUV) associated with this decompressor instance.
     */
    public TJCS getColorspace() {
        return (TJCS) TJCS.fromValue(pointerToJpegColorspace.getInt());
    }

    /**
     * Returns the destination image buffer associated with this decompressor instance.
     *
     * @return the destination image buffer associated with this decompressor instance.
     */
    public ByteBuffer getDstBuf() {
        return this.dstBuf;
    }


    /**
     * Decompress the JPEG source image or decode the YUV source image associated with this decompressor instance and
     * output a grayscale, RGB, or CMYK image to the given destination buffer.
     *
     * @param dstBuf
     *            buffer that will receive the decompressed/decoded image. If the source image is a JPEG image, then
     *            this buffer should normally be <code>pitch * scaledHeight</code> bytes in size, where
     *            <code>scaledHeight</code> can be determined by calling <code>
     * scalingFactor.{@link TJScalingFactor#getScaled getScaled}(jpegHeight)
     * </code> with one of the scaling factors returned from {@link TJ#getScalingFactors} or by calling
     *            {@link #getScaledHeight}. If the source image is a YUV image, then this buffer should normally be
     *            <code>pitch * height</code> bytes in size, where <code>height</code> is the height of the YUV image.
     *            However, the buffer may also be larger than the dimensions of the source image, in which case the
     *            <code>x</code>, <code>y</code>, and <code>pitch</code> parameters can be used to specify the region
     *            into which the source image should be decompressed/decoded.
     *
     * @param x
     *            x offset (in pixels) of the region in the destination image into which the source image should be
     *            decompressed/decoded
     *
     * @param y
     *            y offset (in pixels) of the region in the destination image into which the source image should be
     *            decompressed/decoded
     *
     * @param desiredWidth
     *            If the source image is a JPEG image, then this specifies the desired width (in pixels) of the
     *            decompressed image (or image region.) If the desired destination image dimensions are different than
     *            the source image dimensions, then TurboJPEG will use scaling in the JPEG decompressor to generate the
     *            largest possible image that will fit within the desired dimensions. Setting this to 0 is the same as
     *            setting it to the width of the JPEG image (in other words, the width will not be considered when
     *            determining the scaled image size.) This parameter is ignored if the source image is a YUV image.
     *
     * @param pitch
     *            bytes per line of the destination image. Normally, this should be set to
     *            <code>scaledWidth * TJ.pixelSize(pixelFormat)</code> if the destination image is unpadded, but you can
     *            use this to, for instance, pad each line of the destination image to a 4-byte boundary or to
     *            decompress/decode the source image into a region of a larger image. NOTE: if the source image is a
     *            JPEG image, then <code>scaledWidth</code> can be determined by calling <code>
     * scalingFactor.{@link TJScalingFactor#getScaled getScaled}(jpegWidth)
     * </code> or by calling {@link #getScaledWidth}. If the source image is a YUV image, then <code>scaledWidth</code>
     *            is the width of the YUV image. Setting this parameter to 0 is the equivalent of setting it to
     *            <code>scaledWidth * TJ.pixelSize(pixelFormat)</code>.
     *
     * @param desiredHeight
     *            If the source image is a JPEG image, then this specifies the desired height (in pixels) of the
     *            decompressed image (or image region.) If the desired destination image dimensions are different than
     *            the source image dimensions, then TurboJPEG will use scaling in the JPEG decompressor to generate the
     *            largest possible image that will fit within the desired dimensions. Setting this to 0 is the same as
     *            setting it to the height of the JPEG image (in other words, the height will not be considered when
     *            determining the scaled image size.) This parameter is ignored if the source image is a YUV image.
     *
     * @param pixelFormat
     *            pixel format of the decompressed/decoded image (one of {@link TJ#PF_RGB TJ.PF_*})
     *
     * @param flags
     *            the bitwise OR of one or more of {@link TJ#FLAG_BOTTOMUP TJ.FLAG_*}

     * @return ByteBuffer with the decompressed image
     */
    public ByteBuffer decompress(int flags) throws TJException {
        TurbojpegLibrary.tjDecompress2(pointerToDecompressor, pointerToSrcBuf, jpegSize, pointerToDstBuf, getWidth(), 0, getHeight(), (int) pixelFormat.value(), flags);
        dstBuf.rewind();
        int pixelSize = TJ.getPixelSize((int)pixelFormat.value());
        int newLimit = getWidth() * getHeight() * pixelSize;
        dstBuf.limit(newLimit);
        
        return dstBuf;
    }



    /**
     * Free the native structures associated with this decompressor instance.
     */
    @Override
    public void close() throws TJException {
        if (this.pointerToDecompressor != null) {
            TurbojpegLibrary.tjDestroy(pointerToDecompressor);
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



}
