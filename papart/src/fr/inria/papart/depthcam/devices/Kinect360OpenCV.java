/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.depthcam.devices;

import fr.inria.papart.depthcam.PixelOffset;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import java.nio.ByteBuffer;
import java.util.Arrays;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import processing.core.PApplet;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class Kinect360OpenCV extends KinectDepthAnalysis {

    public IplImage validPointsIpl;
    public byte[] validPointsRaw;

    public Kinect360OpenCV(PApplet parent, KinectDevice kinect) {
       super(parent, kinect);
       init();
    }

    private void init() {
        validPointsIpl = IplImage.create(cvSize(kinectDevice().depthWidth(),
                kinectDevice().depthHeight()),
                IPL_DEPTH_8U, 3);
        validPointsRaw = new byte[kinectDevice().depthSize() * 3];
    }

    public IplImage update(IplImage depth, IplImage color) {
        return update(depth, color, 1);
    }

    public IplImage update(IplImage depth, IplImage color, int skip) {

        updateRawDepth(depth);
        updateRawColor(color);
        clearImageBuffer();
        computeDepthAndDo(1, new setImageData());
        updateImageBuffer();
        return validPointsIpl;
    }

    private void clearImageBuffer() {
        ByteBuffer outputBuff = validPointsIpl.getByteBuffer();
        outputBuff.get(validPointsRaw);
        Arrays.fill(validPointsRaw, (byte) 0);
    }

    private void updateImageBuffer() {
        ByteBuffer outputBuff = validPointsIpl.getByteBuffer();
        outputBuff = (ByteBuffer) outputBuff.rewind();
        outputBuff.put(validPointsRaw);
    }

    class setImageData implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            depthData.validPointsMask[px.offset] = true;
            int outputOffset = px.offset * 3;
            int colorOffset = kinectDevice.findColorOffset(p) * 3;
            validPointsRaw[outputOffset + 2] = colorRaw[colorOffset + 2];
            validPointsRaw[outputOffset + 1] = colorRaw[colorOffset + 1];
            validPointsRaw[outputOffset + 0] = colorRaw[colorOffset + 0];
        }
    }

    @Deprecated
    public IplImage getDepthColorIpl() {
        return validPointsIpl;
    }

    public IplImage getColouredDepthImage() {
        return validPointsIpl;
    }

}
