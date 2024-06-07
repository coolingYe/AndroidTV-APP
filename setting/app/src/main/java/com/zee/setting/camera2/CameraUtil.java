package com.zee.setting.camera2;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.util.Log;
import android.util.Range;
import android.util.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraUtil {
    private static final String TAG = "Camera2Helper";
    private static final int resolutionIndex = 0;      // 控制预览分辨率
    public static Size[] resolutions = new Size[]{
            new Size(1920, 1080),//1.7
            new Size(1280, 960),//1.3
            new Size(1280, 720),//1.7
            new Size(960, 540),//1.7
            new Size(640, 480),//1.3
            new Size(480, 270),//1.7
            new Size(384, 216),//1.7
            new Size(320, 240),//1.3
            new Size(160, 120),//1.3
    };

    public static Size getBestSupportedSize(Size[] sizeArray, int viewWidth, final int viewHeight) {
        List<Size> sizeWidthEqual = new ArrayList<>();
        Log.d(TAG, "viewWidth=" + viewWidth + ", viewHeight=" + viewHeight);

        boolean isSmallToBig = false;
        if (sizeArray.length >= 2) {
            if (sizeArray[0].getWidth() <= sizeArray[1].getWidth() && sizeArray[0].getHeight() <= sizeArray[1].getHeight()) {
                //从小到大
                isSmallToBig = true;
            }
        }

        //先查找preview中是否存在与预览view相同宽高的尺寸
        Size size = null;

        if ((viewWidth * 1.0f / viewHeight) == (1920 * 1.0f / 1080)) {
            for (int i = 0; i < sizeArray.length; i++) {
                if (isSmallToBig) {
                    size = sizeArray[sizeArray.length - 1 - i];
                } else {
                    size = sizeArray[i];
                }

                if (size.getWidth() == 1920 && size.getHeight() == 1080) {
                    return size;
                }
            }
        }

        for (int i = 0; i < sizeArray.length; i++) {
            if (isSmallToBig) {
                size = sizeArray[sizeArray.length - 1 - i];
            } else {
                size = sizeArray[i];
            }
            Log.d(TAG, "size.getWidth()=" + size.getWidth() + ", size.getHeight()=" + size.getHeight());
            //选择更高倍率相机尺寸
            if (size.getWidth() > viewWidth && size.getHeight() > viewHeight) {
                float multipleWidth = size.getWidth() * 1.0f / viewWidth;
                float multipleHeight = size.getHeight() * 1.0f / viewHeight;
                if (multipleWidth == multipleHeight) {
                    return size;
                }
            }
            //优先选择大小相等的尺寸
            if ((size.getWidth() == viewWidth) && (size.getHeight() == viewHeight)) {
                return size;
            }
            //再次选择能够满足我们遮罩层的的容差的
            if (size.getWidth() == viewWidth) {
                sizeWidthEqual.add(size);
            }
        }
        if (sizeWidthEqual.size() > 0) {
            //找一个分辨率最大的尺寸
            Collections.sort(sizeWidthEqual, new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    return Math.abs(o1.getHeight() - viewHeight) - Math.abs(o2.getHeight() - viewHeight);//相机宽度由小到大排序
                }
            });
            for (Size sizeWidth : sizeWidthEqual) {
                Log.d(TAG, "sizeWidthEqual  size.getWidth()=" + sizeWidth.getWidth() + ", size.getHeight()=" + sizeWidth.getHeight());
                //优先选择大小相等的尺寸
            }
            //图片宽度和屏幕高度越接近 像素越好
            return sizeWidthEqual.get(0);
        } else {
            //以上都不满足自己设置一个条件选择一个相机尺寸展示就可以了。理论上都是被淘汰四五年前的低端机
            return sizeArray[0];
        }
    }

    public static Size getBestSupportedSize(List<Size> sizes, int viewWidth, final int viewHeight) {
        Size[] tempSizes = sizes.toArray(new Size[0]);
        Arrays.sort(tempSizes, new Comparator<Size>() {
            @Override
            public int compare(Size o1, Size o2) {
                if (o1.getWidth() > o2.getWidth()) {
                    return -1;
                } else if (o1.getWidth() == o2.getWidth()) {
                    return o1.getHeight() > o2.getHeight() ? -1 : 1;
                } else {
                    return 1;
                }
            }
        });
        sizes = Arrays.asList(tempSizes);
        boolean support480P = false;
        boolean support1080P = false;
        for (Size size : sizes) {
            Log.e(TAG, "size.getWidth()=" + size.getWidth() + ", size.getHeight()=" + size.getHeight());
            if (size.getHeight() == 480 && size.getWidth() == 640) {
                support480P = true;
            }
            if (size.getHeight() == 1080 && size.getWidth() == 1920) {
                support1080P = true;
            }
        }
        if (!support480P && !support1080P) {
            Log.e(TAG, "error! not support sizes of what we set");
        } else if (!support1080P) {
            Log.e(TAG, "error! only not support 1920x1080");
        } else if (!support480P) {
            Log.e(TAG, "error! only not support 640x480");
        }
        int screenHeight = viewHeight;
        int screenWidth = viewWidth;
        if (screenWidth < 640) {
            Log.e(TAG, "error! screen resolution < 640x480");
        } else if (screenWidth < 1920) {
            Log.e(TAG, "error! screen resolution < 1920x1080");
        }
        return resolutions[resolutionIndex];
    }

    public static Range<Integer> getTargetFpsRange(CameraCharacteristics cameraCharacteristics) {
        Range<Integer>[] ranges = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        if (ranges != null) {
            Range<Integer> rangeMax30 = null;
            Range<Integer> range15_30 = null;
            Range<Integer> rangeMax10_30 = null;
            for (Range<Integer> range: ranges) {
                if (range.getLower() == 15 && range.getUpper() == 30) {
                    return range;
                } else if (range.getLower() >= 15 && range.getUpper() == 30) {
                    rangeMax30 = range;
                } else if (range.getLower() >= 15 && range.getUpper() < 30) {
                    range15_30 = range;
                } else if (range.getLower() >= 10 && range.getUpper() <= 30) {
                    rangeMax10_30 = range;
                }
            }

            if (rangeMax30 != null) return rangeMax30;
            if (range15_30 != null) return range15_30;
            if (rangeMax10_30 != null) return rangeMax10_30;
        }

        return null;
    }


    public static int getCameraNum(Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            if (cameraIds != null) {
                return cameraIds.length;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String[] getCameraIds(Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            return cameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isUsbCameraDevice(UsbDevice usbDevice) {
        if (usbDevice == null || usbDevice.getProductName() == null) {
            return false;
        }
        boolean isUsbCamera = false;
        int interfaceCount = usbDevice.getInterfaceCount();
        for (int interIndex = 0; interIndex < interfaceCount; interIndex++) {
            UsbInterface usbInterface = usbDevice.getInterface(interIndex);
            if ((usbInterface.getName() == null || usbDevice.getProductName().equals(usbInterface.getName()))
                    && usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_VIDEO) {
                isUsbCamera = true;
                break;
            }
        }
        return isUsbCamera;
    }

    public static boolean isUsbStorageDevice(UsbDevice usbDevice) {
        if (usbDevice == null || usbDevice.getProductName() == null) {
            return false;
        }
        boolean isUsbStorage = false;
        int interfaceCount = usbDevice.getInterfaceCount();
        for (int interIndex = 0; interIndex < interfaceCount; interIndex++) {
            UsbInterface usbInterface = usbDevice.getInterface(interIndex);
            if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_MASS_STORAGE) {
                isUsbStorage = true;
                break;
            }
        }
        return isUsbStorage;
    }
}
