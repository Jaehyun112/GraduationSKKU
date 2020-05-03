package com.jaehyun.skkugraduation.CameraTensorflow;

/*
 * Copyright 2017 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jaehyun.skkugraduation.CameraTensorflow.env.ImageUtils;
import com.jaehyun.skkugraduation.CameraTensorflow.env.Logger;
import com.jaehyun.skkugraduation.CameraTensorflow.tracking.MultiBoxTracker;
import com.jaehyun.skkugraduation.MainActivity;
import com.jaehyun.skkugraduation.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LegacyCameraConnectionFragment extends Fragment {
  private Camera camera;
  private static final Logger LOGGER = new Logger();
  private Camera.PreviewCallback imageListener;
  private Size desiredSize;

  /**
   * The layout identifier to inflate for this Fragment.
   */
  private int layout;

  public LegacyCameraConnectionFragment(
          final Camera.PreviewCallback imageListener, final int layout, final Size desiredSize) {
    this.imageListener = imageListener;
    this.layout = layout;
    this.desiredSize = desiredSize;
  }

  /**
   * Conversion from screen rotation to JPEG orientation.
   */
  private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

  static {
    ORIENTATIONS.append(Surface.ROTATION_0, 90);
    ORIENTATIONS.append(Surface.ROTATION_90, 0);
    ORIENTATIONS.append(Surface.ROTATION_180, 270);
    ORIENTATIONS.append(Surface.ROTATION_270, 180);
  }

  /**
   * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
   * {@link TextureView}.
   */
  private final TextureView.SurfaceTextureListener surfaceTextureListener =
      new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(
                final SurfaceTexture texture, final int width, final int height) {

          int index = getCameraId();
          camera = Camera.open(index);

          try {
            Camera.Parameters parameters = camera.getParameters();


            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes != null
                && focusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
              parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            }
            if (focusModes != null
                    && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
              parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            for(int i=0 ; i< focusModes.size() ; i++){
              parameters.setFocusMode(focusModes.get(i));
            }


            List<Camera.Size> cameraSizes = parameters.getSupportedPreviewSizes();
            Size[] sizes = new Size[cameraSizes.size()];
            int i = 0;
            for (Camera.Size size : cameraSizes) {
              sizes[i++] = new Size(size.width, size.height);
            }
            Size previewSize =
                CameraConnectionFragment.chooseOptimalSize(
                    sizes, desiredSize.getWidth(), desiredSize.getHeight());
            parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
            camera.setDisplayOrientation(90);
            camera.setParameters(parameters);
            camera.setPreviewTexture(texture);
          } catch (IOException exception) {
            camera.release();
          }

          camera.setPreviewCallbackWithBuffer(imageListener);
          Camera.Size s = camera.getParameters().getPreviewSize();
          camera.addCallbackBuffer(new byte[ImageUtils.getYUVByteSize(s.height, s.width)]);

          textureView.setAspectRatio(s.height, s.width);
          camera.startPreview();

          if(GuideItem.inpectLike == GuideItem.AUTO_INSPECT){
            nextGuideChange(0);
          }else if(GuideItem.inpectLike == GuideItem.SINGLE_INSPECT){
            nextGuideChange(GuideItem.currentStep);
          }
        }

        @Override
        public void onSurfaceTextureSizeChanged(
                final SurfaceTexture texture, final int width, final int height) {
          if(GuideItem.inpectLike == GuideItem.AUTO_INSPECT){
            nextGuideChange(0);
          }else if(GuideItem.inpectLike == GuideItem.SINGLE_INSPECT){
            nextGuideChange(GuideItem.currentStep);
          }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
          return true;
        }

        @Override
        public void onSurfaceTextureUpdated(final SurfaceTexture texture) {}
      };

  /**
   * An {@link AutoFitTextureView} for camera preview.
   */
  private AutoFitTextureView textureView;

  /**
   * An additional thread for running tasks that shouldn't block the UI.
   */
  private HandlerThread backgroundThread;

  @Override
  public View onCreateView(
          final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return inflater.inflate(layout, container, false);
  }

  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {
    textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
    detectedListnerHandler();

  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);


  }

  @Override
  public void onResume() {
    super.onResume();
    startBackgroundThread();
    if (textureView.isAvailable()) {
      camera.startPreview();
    } else {
      textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    /*
    if(GuideItem.inpectLike == GuideItem.AUTO_INSPECT){
      nextGuideChange(0);
    }else if(GuideItem.inpectLike == GuideItem.SINGLE_INSPECT){
      nextGuideChange(GuideItem.currentStep);
    }

     */

  }

  @Override
  public void onPause() {
    stopCamera();
    stopBackgroundThread();
    super.onPause();
  }

  /**
   * Starts a background thread and its {@link Handler}.
   */
  private void startBackgroundThread() {
    backgroundThread = new HandlerThread("CameraBackground");
    backgroundThread.start();
  }

  /**
   * Stops the background thread and its {@link Handler}.
   */
  private void stopBackgroundThread() {
    backgroundThread.quitSafely();
    try {
      backgroundThread.join();
      backgroundThread = null;
    } catch (final InterruptedException e) {
      LOGGER.e(e, "Exception!");
    }
  }

  protected void stopCamera() {
    if (camera != null) {
      camera.stopPreview();
      camera.setPreviewCallback(null);
      camera.release();
      camera = null;
    }
  }

  private int getCameraId() {
    CameraInfo ci = new CameraInfo();
    for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
      Camera.getCameraInfo(i, ci);
      if (ci.facing == CameraInfo.CAMERA_FACING_BACK)
        return i;
    }
    return -1; // No camera found
  }


  private View guide;
  private boolean isCaptured = false;
  private boolean isAfterImageStep2 = false;
  private int captureCount = 0;
  private ArrayList<RectF> locationHistory = new ArrayList<>();
  public void detectedListnerHandler(){
    guide = CameraActivity.guideView;
    MultiBoxTracker.setOnDetectedListner(new MultiBoxTracker.OnDetectedListner() {
      @Override
      public void onDtected(RectF rect) {

        RectF guideRect = new RectF(guide.getLeft(),guide.getTop()+center_y,guide.getRight(),guide.getBottom()+center_y);
        RectF location = new RectF();
        location = rect;

        int bandWidth = 200;
        if(location.left > guideRect.left & location.left < guideRect.left + bandWidth
                & location.right < guideRect.right & location.right > guideRect.right - bandWidth
                & location.bottom < guideRect.bottom & location.bottom > guideRect.bottom - bandWidth
                & location.top > guideRect.top & location.top < guideRect.top + bandWidth
        ){
          if(GuideItem.captureLike != GuideItem.CAPTURE_BUTTON) captureCount++;
          if(captureCount>25){
            if(!isCaptured){
              locationHistory.add(location);
              captureCount = 0;
              if(isAfterImageStep2){
                if(locationHistory.get(locationHistory.size()-1).centerX() != locationHistory.get(locationHistory.size()-2).centerX()
                        & locationHistory.get(locationHistory.size()-2).centerX() != locationHistory.get(locationHistory.size()-3).centerX()
                ){
                  captureStillPicture(location);
                  isCaptured= true;
                }
              }else{
                captureStillPicture(location);
                isCaptured= true;
              }
            }
          }
        }
      }
    });
  }

  private void captureStillPicture(final RectF rect) {
    camera.autoFocus(new Camera.AutoFocusCallback() {
      @Override
      public void onAutoFocus(boolean success, Camera camera) {
        if (success){
          capture(rect);
          captureEffect();
          camera.cancelAutoFocus();
        }else{
          camera.cancelAutoFocus();
          return;
        }
      }
    });

  }

  private void capture(RectF rect) {
    Bitmap bitmap = textureView.getBitmap();
    Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, (int)rect.left,(int) rect.top, (int)(rect.width()),(int)(rect.height()));

    if(GuideItem.inpectLike == GuideItem.AUTO_INSPECT){
      new Handler().postDelayed(new Runnable()
      {
        @Override
        public void run()
        {
          changeNextStep();
        }
      }, 1000);
      GuideItem.addCropBitmapList(croppedBitmap);
    }else if(GuideItem.inpectLike == GuideItem.SINGLE_INSPECT){
      GuideItem.setCropBitmapList(GuideItem.currentStep,croppedBitmap);
      finishStep();
    }
  }

  private void changeNextStep(){
    /*
    int current = GuideItem.currentStep;
    current++;
    GuideItem.currentStep = current;
    if(current == 9){
      finishStep();
    }else if(current == 8){
      captureBtnSetting();
      nextGuideChange(current);
    }else if(current == 6){
      LikeDialog likeDialog = new LikeDialog(getActivity());
      likeDialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
      likeDialog.show();
      int finalCurrent = current;
      likeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
          nextGuideChange(finalCurrent);
        }
      });
    }else{
      if(GuideItem.phoneLike == GuideItem.SAMSUNGnETC) {
        if(current == 7) {
          isAfterImageStep2 = true;
        }
      }
      else if (GuideItem.phoneLike == GuideItem.LG){
        if(current == 7) {
          changeNextStep();
          nextGuideChange(current);
          return;
        }
      }
      else if(GuideItem.phoneLike == GuideItem.IPHONE){
        if(current == 7) {
          isAfterImageStep2 = true;
        }
      }
      GuideItem.captureLike = GuideItem.CAPTURE_AUTO;
      nextGuideChange(current);
      RelativeLayout captureBtn = CameraActivity.captureButton;
      captureBtn.setVisibility(View.GONE);
    }

     */
  }

  private float center_y;
  private void nextGuideChange(int current){
    /*
    GuideItem.captureLike = GuideItem.CAPTURE_AUTO;
    if(current == 8){
      captureBtnSetting();
      CameraActivity.infoTextView.setText(GuideItem.guideInfos[current+1]);
    } else if(current == 7){
      if(GuideItem.phoneLike == GuideItem.IPHONE){
        CameraActivity.infoTextView.setText(GuideItem.guideInfos[current+1]);
      }else if(GuideItem.phoneLike == GuideItem.LG){
        CameraActivity.infoTextView.setText(GuideItem.guideInfos[current+2]);
        captureBtnSetting();
      }else{
        CameraActivity.infoTextView.setText(GuideItem.guideInfos[current]);
      }
    } else{
      CameraActivity.infoTextView.setText(GuideItem.guideInfos[current]);
    }

    RelativeLayout mGuideImageContainer = CameraActivity.guideImageContainer;
    ImageView mGuideImage = CameraActivity.guideImage;

    if(current == 1){
      mGuideImageContainer.setVisibility(View.VISIBLE);
      mGuideImage.setBackgroundResource(GuideItem.guideIcons[2]);
    }else if(current == 2){
      mGuideImageContainer.setVisibility(View.VISIBLE);
      mGuideImage.setBackgroundResource(GuideItem.guideIcons[0]);
    }else if(current == 4){
      mGuideImageContainer.setVisibility(View.VISIBLE);
      mGuideImage.setBackgroundResource(GuideItem.guideIcons[3]);
    }else if(current == 5){
      mGuideImageContainer.setVisibility(View.VISIBLE);
      mGuideImage.setBackgroundResource(GuideItem.guideIcons[1]);
    }else{
      mGuideImageContainer.setVisibility(View.GONE);
    }
    DpToPxConverter dpToPxConverter = new DpToPxConverter(getContext());
    ViewGroup.LayoutParams params = guide.getLayoutParams();
    params.width = dpToPxConverter.convert(GuideItem.guideWidts[current]);
    params.height = dpToPxConverter.convert(GuideItem.guideheights[current]);
    guide.setLayoutParams(params);
    guide.requestLayout();

    int halfPreviewHeight = textureView.getHeight()/2;
    center_y = textureView.getY() + textureView.getHeight()/2 - dpToPxConverter.convert(GuideItem.guideheights[current])/2;
    guide.setY(center_y);
    guide.requestLayout();

    new Handler().postDelayed(new Runnable()
    {
      @Override
      public void run()
      {
        isCaptured = false;
      }
    }, 2000);

     */
  }

  private void captureEffect(){
    /*
    Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
    vibrator.vibrate(100);
    MediaActionSound mediaActionSound = new MediaActionSound();
    mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
    View effectView = CameraActivity.captureEffect;
    effectView.setVisibility(View.VISIBLE);
    effectView.animate().alphaBy(0).alpha(1).setDuration(100).withEndAction(new Runnable() {
      @Override
      public void run() {
        effectView.animate().alpha(0).setDuration(100).withEndAction(new Runnable() {
          @Override
          public void run() {
            effectView.setVisibility(View.GONE);
          }
        }).start();
      }
    }).start();

    CameraActivity.infoTextView.animate().scaleX(0.1f).withEndAction(new Runnable() {
      @Override
      public void run() {
        CameraActivity.infoTextView.animate().scaleX(1).start();
      }
    }).start();

     */
  }
  private void captureBtnSetting(){
    /*
    Log.d("####","@@@@@");
    GuideItem.captureLike = GuideItem.CAPTURE_BUTTON;
    RelativeLayout captureBtn = CameraActivity.captureButton;
    captureBtn.setVisibility(View.VISIBLE);
    guide = CameraActivity.guideView;
    RectF guideRect = new RectF(guide.getX(),guide.getTop()+center_y , guide.getRight(),guide.getBottom()+center_y);
    captureBtn.setOnClickListener(v->{
      if(!isCaptured){
        captureStillPicture(guideRect);
        isCaptured= true;
      }
    });

     */
  }

  private void finishStep(){
    Intent intent = new Intent(getActivity(), MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);
    getActivity().finish();
    locationHistory.clear();
  }

}
