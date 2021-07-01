package com.jesen.cod.jetpackvideo.ui.publish;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.UseCase;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCaptureConfig;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.ActivityCaptureBinding;
import com.jesen.cod.jetpackvideo.ui.view.RecordView;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.jetpackvideo.utils.ToastUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CaptureActivity extends AppCompatActivity {

    private static File PARENT_FILE_PATH;
    public static final String PREVIEW_RESULT_FILE_PATH = "file_path";
    public static final String PREVIEW_RESULT_FILE_WIDTH = "file_width";
    public static final String PREVIEW_RESULT_FILE_HEIGHT = "file_height";
    public static final String PREVIEW_RESULT_FILE_TYPE = "file_TYPE";

    public static final int REQ_CODE_TO_CAPTURE = 1003;

    private ActivityCaptureBinding mBinding;
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};

    private static final int PERMISSION_CODE = 1001;
    private ArrayList<String> deniedPermissions = new ArrayList<>();

    private CameraX.LensFacing mLensFacing;
    // 摄像头旋转角度
    private int cameraRotation = Surface.ROTATION_0;
    // 摄像头分辨率
    private Size resolution;
    // 宽高比
    private Rational rational;
    private Preview preview;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;

    private TextureView mTextureView;

    private boolean takingPicture;
    private String outputFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_capture);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_capture);

        // 后置摄像头
        mLensFacing = CameraX.LensFacing.BACK;
        resolution = new Size(1280, 720);
        rational = new Rational(9, 16);

        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE);

        PARENT_FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        Og.d("CaptureActivity, PARENT_FILE_PATH: "+PARENT_FILE_PATH);

        mBinding.recordView.setOnRecordListener(new RecordView.OnRecordListener() {
            @Override
            public void onClick() {
                takingPicture = true;
                File file = new File(PARENT_FILE_PATH, System.currentTimeMillis() + ".jpeg");
                mBinding.captureTips.setVisibility(View.VISIBLE);
                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull @NotNull File file) {
                        onFileSaved(file);
                    }

                    @Override
                    public void onError(@NonNull @NotNull ImageCapture.UseCaseError useCaseError, @NonNull @NotNull String message, @Nullable @org.jetbrains.annotations.Nullable Throwable cause) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show(CaptureActivity.this, useCaseError.toString());
                            }
                        });
                    }
                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onLongClick() {
                takingPicture = false;
                File file = new File(PARENT_FILE_PATH, System.currentTimeMillis() + ".mp4");
                videoCapture.startRecording(file, new VideoCapture.OnVideoSavedListener() {
                    @Override
                    public void onVideoSaved(File file) {
                        onFileSaved(file);
                    }

                    @Override
                    public void onError(VideoCapture.UseCaseError useCaseError, String message, @Nullable @org.jetbrains.annotations.Nullable Throwable cause) {
                        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show(CaptureActivity.this, useCaseError.toString());
                            }
                        });
                    }
                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onFinish() {
                videoCapture.stopRecording();
            }
        });

    }

    private void onFileSaved(File file) {
        outputFilePath = file.getAbsolutePath();
        Og.d("CaptureActivity, onFileSaved, outputFilePath: " + outputFilePath);
        String mimeType = takingPicture ? "image/jpeg" : "video/mp4";
        MediaScannerConnection.scanFile(this, new String[]{outputFilePath}, new String[]{mimeType}, null);
        PreviewActivity.startActivityForResult(this, outputFilePath, !takingPicture, "完成");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            deniedPermissions.clear();
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int result = grantResults[i];
                if (result != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permission);
                }
            }
            if (deniedPermissions.isEmpty()) {
                bindCameraX();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.capture_permission_message))
                        .setNegativeButton("不", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                CaptureActivity.this.finish();
                            }
                        })
                        .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String[] denied = new String[deniedPermissions.size()];
                                ActivityCompat.requestPermissions(CaptureActivity.this, denied, PERMISSION_CODE);
                            }
                        }).create().show();
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void bindCameraX() {
        CameraX.unbindAll();

        //查询一下当前要使用的设备摄像头(比如后置摄像头)是否存在
        boolean hasAvailableCameraId = false;
        try {
            hasAvailableCameraId = CameraX.hasCameraWithLensFacing(mLensFacing);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }

        if (!hasAvailableCameraId) {
            ToastUtil.showOnUI(this,"无可用的设备cameraId!,请检查设备的相机是否被占用");
            finish();
            return;
        }

        //查询一下是否存在可用的cameraId.形式如：后置："0"，前置："1"
        String cameraIdForLensFacing = null;
        try {
            cameraIdForLensFacing = CameraX.getCameraFactory().cameraIdForLensFacing(mLensFacing);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(cameraIdForLensFacing)) {
            ToastUtil.showOnUI(this,"无可用的设备cameraId!,请检查设备的相机是否被占用");
            finish();
            return;
        }

        PreviewConfig config = new PreviewConfig.Builder()
                // 后置摄像头
                .setLensFacing(mLensFacing)
                // 摄像头旋转角度
                .setTargetRotation(cameraRotation)
                // 分辨率
                .setTargetResolution(resolution)
                // 宽高比
                .setTargetAspectRatio(rational)
                .build();
        preview = new Preview(config);

        imageCapture = new ImageCapture(
                new ImageCaptureConfig.Builder()
                        .setTargetAspectRatio(rational)
                        .setTargetResolution(resolution)
                        .setLensFacing(mLensFacing)
                        .setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
                        .setTargetRotation(cameraRotation)
                        .build());


        videoCapture = new VideoCapture(
                new VideoCaptureConfig.Builder()
                        .setTargetRotation(cameraRotation)
                        .setTargetResolution(resolution)
                        .setTargetAspectRatio(rational)
                        .setLensFacing(mLensFacing)
                        .setBitRate(3 * 1024 * 1024)
                        .setVideoFrameRate(25)
                        .build()
        );

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                // textView必须从布局中remove再add,才能正常渲染画面
                mTextureView = mBinding.textureView;
                ViewGroup parent = (ViewGroup) mTextureView.getParent();
                parent.removeView(mTextureView);

                parent.addView(mTextureView, 0);
                mTextureView.setSurfaceTexture(output.getSurfaceTexture());

            }
        });

        //上面配置的都是我们期望的分辨率
        List<UseCase> newUseList = new ArrayList<>();
        newUseList.add(preview);
        newUseList.add(imageCapture);
        newUseList.add(videoCapture);
        //下面我们要查询一下 当前设备它所支持的分辨率有哪些，然后再更新一下 所配置的几个usecase
        Map<UseCase, Size> resolutions = CameraX.getSurfaceManager().getSuggestedResolutions(cameraIdForLensFacing, null, newUseList);
        Iterator<Map.Entry<UseCase, Size>> iterator = resolutions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UseCase, Size> next = iterator.next();
            UseCase useCase = next.getKey();
            Size value = next.getValue();
            Og.d("CaptureActivity, support size:"+value.getWidth()+"*"+value.getHeight());
            Map<String, Size> update = new HashMap<>();
            update.put(cameraIdForLensFacing, value);
            useCase.updateSuggestedResolution(update);

        }
        CameraX.bindToLifecycle(this, preview, imageCapture, videoCapture);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PreviewActivity.REQ_PREVIEW_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent();
            intent.putExtra(PREVIEW_RESULT_FILE_PATH, outputFilePath);
            // 设备竖屏，宽高需要互换，横屏不需要
            intent.putExtra(PREVIEW_RESULT_FILE_WIDTH, resolution.getHeight());
            intent.putExtra(PREVIEW_RESULT_FILE_HEIGHT, resolution.getWidth());
            intent.putExtra(PREVIEW_RESULT_FILE_TYPE, !takingPicture);
            setResult(RESULT_OK,intent);
            finish();
        }
    }

    public static void startActivityForResult(Activity activity) {
        Intent intent = new Intent(activity, CaptureActivity.class);
        activity.startActivityForResult(intent, REQ_CODE_TO_CAPTURE);
    }

    @Override
    protected void onDestroy() {
        CameraX.unbindAll();
        super.onDestroy();

    }
}