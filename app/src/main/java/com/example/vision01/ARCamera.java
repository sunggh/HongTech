package com.example.vision01;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.media.Image;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vision01.common.helpers.CameraPermissionHelper;
import com.example.vision01.common.helpers.DepthSettings;
import com.example.vision01.common.helpers.DisplayRotationHelper;
import com.example.vision01.common.helpers.InstantPlacementSettings;
import com.example.vision01.common.helpers.SnackbarHelper;
import com.example.vision01.common.helpers.TapHelper;
import com.example.vision01.common.helpers.TrackingStateHelper;
import com.example.vision01.common.hongtech.Framebuffer;
import com.example.vision01.common.hongtech.GLError;
import com.example.vision01.common.hongtech.Mesh;
import com.example.vision01.common.hongtech.RenderingHelper;
import com.example.vision01.common.hongtech.Shader;
import com.example.vision01.common.hongtech.Texture;
import com.example.vision01.common.hongtech.VertexBuffer;
import com.example.vision01.common.hongtech.arcore.BackgroundRenderer;
import com.example.vision01.common.hongtech.arcore.PlaneRenderer;
import com.example.vision01.common.hongtech.arcore.SpecularCubemapFilter;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.InstantPlacementPoint;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import android.view.MotionEvent;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.example.vision01.FindForm.AR_Mode;

public class ARCamera extends AppCompatActivity implements RenderingHelper.Renderer{

    private RenderingHelper render;
    private GLSurfaceView surfaceView;
    private boolean installRequested;

    private PlaneRenderer planeRenderer;
    private BackgroundRenderer backgroundRenderer;
    private Framebuffer virtualSceneFramebuffer;
    private boolean hasSetTextureNames = false;
    private DisplayRotationHelper displayRotationHelper;
    private Texture dfgTexture;
    private SpecularCubemapFilter cubemapFilter;

    private static final int CUBEMAP_RESOLUTION = 16;
    private static final int CUBEMAP_NUMBER_OF_IMPORTANCE_SAMPLES = 32;

    private VertexBuffer pointCloudVertexBuffer;
    private Mesh pointCloudMesh;
    private Shader pointCloudShader;

    private Mesh virtualObjectMesh;
    private Shader virtualObjectShader;
    private final ArrayList<Anchor> anchors = new ArrayList<>();
    private final ArrayList<Anchor> ScanAncors = new ArrayList<>();
    private final DepthSettings depthSettings = new DepthSettings();
    private boolean[] depthSettingsMenuDialogCheckboxes = new boolean[2];

    private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);

    private static final String SEARCHING_PLANE_MESSAGE = "블루투스를 찾는 중입니다...";
    private static final String WAITING_FOR_TAP_MESSAGE = "블루투스를 찾는 중입니다2...";

    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();

    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16]; // view x model
    private final float[] modelViewProjectionMatrix = new float[16]; // projection x view x model
    private final float[] sphericalHarmonicsCoefficients = new float[9 * 3];
    private final float[] viewInverseMatrix = new float[16];
    private final float[] worldLightDirection = {0.0f, 0.0f, 0.0f, 0.0f};
    private final float[] viewLightDirection = new float[4]; // view x world light direction

    private long lastPointCloudTimestamp = 0;

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100f;

    private final InstantPlacementSettings instantPlacementSettings = new InstantPlacementSettings();
    private boolean[] instantPlacementSettingsMenuDialogCheckboxes = new boolean[1];

    private static final float APPROXIMATE_DISTANCE_METERS = 2.0f;
    private TextView txt;
    private TextView txt2;
    private TapHelper tapHelper;
    protected static final int NUM_VERTICES = 3;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r_camera);
        surfaceView = findViewById(R.id.surfaceview);

        displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

        installRequested = false;

        render = new RenderingHelper(surfaceView, this, getAssets());

        depthSettings.onCreate(this);

        instantPlacementSettings.onCreate(this);

        tapHelper = new TapHelper(/*context=*/ this);
        surfaceView.setOnTouchListener(tapHelper);

        txt= (TextView)findViewById(R.id.textView5);// AR화면에서 텍스트 출력하기 위한 텍스트뷰
        txt2= (TextView)findViewById(R.id.textView6);
        //깜빡이는 애니메이션
        Animation anim;
        anim = new AlphaAnimation(0.0f,1.0f);
        anim.setDuration(300);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(100);
        txt.startAnimation(anim);
        txt2.startAnimation(anim);



        GLSurfaceView glview = (GLSurfaceView)findViewById(R.id.surfaceview2);
        glview.setEGLConfigChooser(false);
        glview.setEGLContextClientVersion(2);
        glview.setZOrderOnTop(true);//배경투명하게 하기위함
        glview.setEGLConfigChooser(8,8,8,8,16,0);//배경투명하게 하기위함
        glview.getHolder().setFormat(PixelFormat.RGBA_8888); //배경투명하게 하기위함
        glview.getHolder().setFormat(PixelFormat.TRANSLUCENT);//배경투명하게 하기위함

        glview.setRenderer(new GLSurfaceView.Renderer() {

            private int programObject;
            private FloatBuffer vertexBuffer;

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                gl.glClearColor(0f, 0f, 0f, 0f);//배경투명하게 하기위함
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                GLES20.glViewport(0, 0, width, height);
                init();
            }


            @Override
            public void onDrawFrame(GL10 gl) {
                float x = 0.1f*(float) Math.sin(System.currentTimeMillis()/1000.0);
                float[] vVertices = new float[] // 삼각형 그리기
                        {0.0f, 0.5f, 0.0f,
                                0.0f, -0.5f, 0.0f,
                                0.5f, 0.0f, 0.0f};


                vertexBuffer.rewind();
                vertexBuffer.put(vVertices);
                vertexBuffer.rewind();


                // Use the program object
                GLES20.glUseProgram(programObject);
                int handle = GLES20.glGetUniformLocation(programObject, "uColor");
                float r = (float) (0.5f+Math.sin(System.currentTimeMillis()/1000.0));
                float g = (float) (0.5f+Math.sin(System.currentTimeMillis()/300.0));
                GLES20.glUniform4f(handle, r, g,0,1);

                // Load the vertex data
                GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
                GLES20.glEnableVertexAttribArray(0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

                gl.glClearColor(0f, 0f, 0f, 0f); // 배경투명하게
            }

            public void GLText(GL10 gl) {
                Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
                Canvas canvas = new Canvas(bitmap);
                bitmap.eraseColor(0);

                Paint paint = new Paint();
                paint.setTextSize(18);
                paint.setAntiAlias(true);
                paint.setARGB(0xff, 255, 0, 255);
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setTextScaleX(0.5f);

                canvas.drawColor(Color.BLUE);
                canvas.drawText("testGLText", 10.f, 15.f, paint);

                GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
            }

            private void error(String s) {
                Log.e("GLTEST", s);
            }

            private int loadShader(int shaderType, String source) {
                if (shaderType != GLES20.GL_FRAGMENT_SHADER && shaderType != GLES20.GL_VERTEX_SHADER) {
                    throw new RuntimeException("Illegal shader type");
                }

                int shader = GLES20.glCreateShader(shaderType);
                if (shader != 0) {
                    GLES20.glShaderSource(shader, source);
                    GLES20.glCompileShader(shader);
                    int[] compiled = new int[1];
                    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
                    if (compiled[0] == 0) {
                        error("Could not compile shader :");
                        error(GLES20.glGetShaderInfoLog(shader));
                        GLES20.glDeleteShader(shader);
                        shader = 0;
                        throw new RuntimeException("Shader Syntax / compilation error");
                    }
                }
                return shader;
            }

            private void init() {
                String vShaderStr = "attribute vec4 vPosition; \n" +
                        "void main() \n" + "{ \n" +
                        " gl_Position = vPosition; \n" +
                        "} \n";
                String fShaderStr = "precision mediump float; \n" +
                        "uniform vec4 uColor;" +
                        "void main() \n" +
                        "{ \n" +
                        " gl_FragColor = uColor; \n" +
                        "} \n";

                ByteBuffer vbb = ByteBuffer.allocateDirect(NUM_VERTICES*3*4);
                vbb.order(ByteOrder.nativeOrder());
                vertexBuffer = vbb.asFloatBuffer();

                int vertexShader;
                int fragmentShader;

                // Load the vertex/fragment shaders
                vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vShaderStr);
                fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fShaderStr);

                // Create the program object
                programObject = GLES20.glCreateProgram();
                if (programObject == 0)
                    return;

                GLES20.glAttachShader(programObject, vertexShader);
                GLES20.glAttachShader(programObject, fragmentShader);
                // Bind vPosition to attribute 0
                GLES20.glBindAttribLocation(programObject, 0, "vPosition");
                // Link the program
                GLES20.glLinkProgram(programObject);
                int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(programObject, GLES20.GL_LINK_STATUS, linkStatus, 0);

                if (linkStatus[0] != GLES20.GL_TRUE) {
                    error("Could not link program: ");
                    error(GLES20.glGetProgramInfoLog(programObject));
                    GLES20.glDeleteProgram(programObject);
                    programObject = 0;
                }
            }
        });
    }



    public void onSurfaceChanged(RenderingHelper render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        virtualSceneFramebuffer.resize(width, height);
    }

    @Override
    public void onSurfaceCreated(RenderingHelper render) {
        try {
            planeRenderer = new PlaneRenderer(render);
            backgroundRenderer = new BackgroundRenderer(render);
            virtualSceneFramebuffer = new Framebuffer(render, /*width=*/ 1, /*height=*/ 1);
            cubemapFilter =
                    new SpecularCubemapFilter(
                            render, CUBEMAP_RESOLUTION, CUBEMAP_NUMBER_OF_IMPORTANCE_SAMPLES);
            // Load DFG lookup table for environmental lighting
            dfgTexture =
                    new Texture(
                            render,
                            Texture.Target.TEXTURE_2D,
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            /*useMipmaps=*/ false);
            // The dfg.raw file is a raw half-float texture with two channels.
            final int dfgResolution = 64;
            final int dfgChannels = 2;
            final int halfFloatSize = 2;
            ByteBuffer buffer =
                    ByteBuffer.allocateDirect(dfgResolution * dfgResolution * dfgChannels * halfFloatSize);
            try (InputStream is = getAssets().open("models/dfg.raw")) {
                is.read(buffer.array());
            }
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, dfgTexture.getTextureId());
            GLError.maybeThrowGLException("Failed to bind DFG texture", "glBindTexture");
            GLES30.glTexImage2D(
                    GLES30.GL_TEXTURE_2D,
                    /*level=*/ 0,
                    GLES30.GL_RG16F,
                    /*width=*/ dfgResolution,
                    /*height=*/ dfgResolution,
                    /*border=*/ 0,
                    GLES30.GL_RG,
                    GLES30.GL_HALF_FLOAT,
                    buffer);
            GLError.maybeThrowGLException("Failed to populate DFG texture", "glTexImage2D");

            Texture virtualObjectAlbedoTexture =
                    Texture.createFromAsset(
                            render,
                            "models/ball_color.png",
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            Texture.ColorFormat.SRGB);
            Texture virtualObjectPbrTexture =
                    Texture.createFromAsset(
                            render,
                            "models/ball_color.png",
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            Texture.ColorFormat.LINEAR);
            virtualObjectMesh = Mesh.createFromAsset(render, "models/ball.obj");
            virtualObjectShader =
                    Shader.createFromAssets(
                            render,
                            "shaders/environmental_hdr.vert",
                            "shaders/environmental_hdr.frag",
                            /*defines=*/ new HashMap<String, String>() {
                                {
                                    put(
                                            "NUMBER_OF_MIPMAP_LEVELS",
                                            Integer.toString(cubemapFilter.getNumberOfMipmapLevels()));
                                }
                            })
                            .setTexture("u_AlbedoTexture", virtualObjectAlbedoTexture)
                            .setTexture("u_RoughnessMetallicAmbientOcclusionTexture", virtualObjectPbrTexture)
                            .setTexture("u_Cubemap", cubemapFilter.getFilteredCubemapTexture())
                            .setTexture("u_DfgTexture", dfgTexture);
        } catch (IOException e) {
            //Log.e(TAG, "Failed to read a required asset file", e);
            //messageSnackbarHelper.showError(this, "Failed to read a required asset file: " + e);
        }
    }

    private boolean hasTrackingPlane() {
        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                return true;
            }
        }
        return false;
    }

    private void updateLightEstimation(LightEstimate lightEstimate, float[] viewMatrix) {
        if (lightEstimate.getState() != LightEstimate.State.VALID) {
            virtualObjectShader.setBool("u_LightEstimateIsValid", false);
            return;
        }
        virtualObjectShader.setBool("u_LightEstimateIsValid", true);

        Matrix.invertM(viewInverseMatrix, 0, viewMatrix, 0);
        virtualObjectShader.setMat4("u_ViewInverse", viewInverseMatrix);

        updateMainLight(
                lightEstimate.getEnvironmentalHdrMainLightDirection(),
                lightEstimate.getEnvironmentalHdrMainLightIntensity(),
                viewMatrix);
        updateSphericalHarmonicsCoefficients(
                lightEstimate.getEnvironmentalHdrAmbientSphericalHarmonics());
        cubemapFilter.update(lightEstimate.acquireEnvironmentalHdrCubeMap());
    }

    private void updateMainLight(float[] direction, float[] intensity, float[] viewMatrix) {
        // We need the direction in a vec4 with 0.0 as the final component to transform it to view space
        worldLightDirection[0] = direction[0];
        worldLightDirection[1] = direction[1];
        worldLightDirection[2] = direction[2];
        Matrix.multiplyMV(viewLightDirection, 0, viewMatrix, 0, worldLightDirection, 0);
        virtualObjectShader.setVec4("u_ViewLightDirection", viewLightDirection);
        virtualObjectShader.setVec3("u_LightIntensity", intensity);
    }

    private void updateSphericalHarmonicsCoefficients(float[] coefficients) {
        // Pre-multiply the spherical harmonics coefficients before passing them to the shader. The
        // constants in sphericalHarmonicFactors were derived from three terms:
        //
        // 1. The normalized spherical harmonics basis functions (y_lm)
        //
        // 2. The lambertian diffuse BRDF factor (1/pi)
        //
        // 3. A <cos> convolution. This is done to so that the resulting function outputs the irradiance
        // of all incoming light over a hemisphere for a given surface normal, which is what the shader
        // (environmental_hdr.frag) expects.
        //
        // You can read more details about the math here:
        // https://google.github.io/filament/Filament.html#annex/sphericalharmonics

        if (coefficients.length != 9 * 3) {
            throw new IllegalArgumentException(
                    "The given coefficients array must be of length 27 (3 components per 9 coefficients");
        }

        // Apply each factor to every component of each coefficient
        for (int i = 0; i < 9 * 3; ++i) {
            sphericalHarmonicsCoefficients[i] = coefficients[i] * sphericalHarmonicFactors[i / 3];
        }
        virtualObjectShader.setVec3Array(
                "u_SphericalHarmonicsCoefficients", sphericalHarmonicsCoefficients);
    }

    private static final float[] sphericalHarmonicFactors = {
            0.282095f,
            -0.325735f,
            0.325735f,
            -0.325735f,
            0.273137f,
            -0.273137f,
            0.078848f,
            -0.273137f,
            0.136569f,
    };
    public boolean round =false , firston=false;
    @Override
    public void onDrawFrame(RenderingHelper render) {
        if (session == null) {
            return;
        }
        // Texture names should only be set once on a GL thread unless they change. This is done during
        // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
        // initialized during the execution of onSurfaceCreated.
        if (!hasSetTextureNames) {
            session.setCameraTextureNames(
                    new int[] {backgroundRenderer.getCameraColorTexture().getTextureId()});
            hasSetTextureNames = true;
        }

        // -- Update per-frame state

        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        // Obtain the current frame from ARSession. When the configuration is set to
        // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
        // camera framerate.
        Frame frame;
        try {
            frame = session.update();
        } catch (CameraNotAvailableException e) {
            //Log.e(TAG, "Camera not available during onDrawFrame", e);
            //messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
            return;
        }
        Camera camera = frame.getCamera();
        // Update BackgroundRenderer state to match the depth settings.
        try {
            backgroundRenderer.setUseDepthVisualization(
                    render, depthSettings.depthColorVisualizationEnabled());
            backgroundRenderer.setUseOcclusion(render, depthSettings.useDepthForOcclusion());
        } catch (IOException e) {
            //Log.e(TAG, "Failed to read a required asset file", e);
            //messageSnackbarHelper.showError(this, "Failed to read a required asset file: " + e);
            return;
        }
        // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
        // used to draw the background camera image.
        backgroundRenderer.updateDisplayGeometry(frame);
        if (camera.getTrackingState() == TrackingState.TRACKING
                && (depthSettings.useDepthForOcclusion()
                || depthSettings.depthColorVisualizationEnabled())) {
            try (Image depthImage = frame.acquireDepthImage()) {
                backgroundRenderer.updateCameraDepthTexture(depthImage);
            } catch (NotYetAvailableException e) {
                // This normally means that depth data is not available yet. This is normal so we will not
                // spam the logcat with this.
            }
        }

        if(FindForm.Mode== FindForm.CUR_MODE.PROGRESS) {
            finish();
            return;
        }

        if(AR_Mode == FindForm.AR_MODE.SEARCHING|| AR_Mode == FindForm.AR_MODE.SEARCHED || AR_Mode == FindForm.AR_MODE.SEARCH_FINISH) {
            handleTap(frame, camera);
        }



        // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
        trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

        // Show a message based on whether tracking has failed, if planes are detected, and if the user
        // has placed any objects.
        String message = null;
        if (camera.getTrackingState() == TrackingState.PAUSED) {
            if (camera.getTrackingFailureReason() == TrackingFailureReason.NONE) {
                message = SEARCHING_PLANE_MESSAGE;
            } else {
                message = TrackingStateHelper.getTrackingFailureReasonString(camera);
            }
        } else {
            if(AR_Mode == FindForm.AR_MODE.NONE) {
                message = "최적화 중입니다. 잠시만 기다려주세요.";
            } else if(AR_Mode == FindForm.AR_MODE.SEARCHING || AR_Mode == FindForm.AR_MODE.SEARCHED){
                txt2.setText("오른쪽으로 화면을");
                txt.setText("천천히 돌려주세요!!");
            }else if (AR_Mode == FindForm.AR_MODE.FINISH) {
                txt2.setText("화면에 보이는 AR");
                txt.setText("방향으로 이동 하세요!!");
            }
        }


        if (message == null) {
            messageSnackbarHelper.hide(this);
        } else {
            messageSnackbarHelper.showMessage(this, message);
        }

        // -- Draw background

        if (frame.getTimestamp() != 0) {
            // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
            // drawing possible leftover data from previous sessions if the texture is reused.
            backgroundRenderer.drawBackground(render);
        }

        // If not tracking, don't draw 3D objects.
        if (camera.getTrackingState() == TrackingState.PAUSED) {
            return;
        }

        // -- Draw non-occluded virtual objects (planes, point cloud)

        // Get projection matrix.
        camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);

        // Get camera matrix and draw.
        camera.getViewMatrix(viewMatrix, 0);

        // Update lighting parameters in the shader
        updateLightEstimation(frame.getLightEstimate(), viewMatrix);

        // Visualize anchors created by touch.
        render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f);
        if(AR_Mode == FindForm.AR_MODE.FINISH) {
            for (Anchor anchor : anchors) {
            /*if (anchor.getTrackingState() != TrackingState.TRACKING) { 우리에게 필요한건 트레킹이 아님
                continue;
            }*/

                // Get the current pose of an Anchor in world space. The Anchor pose is updated
                // during calls to session.update() as ARCore refines its estimate of the world.
                anchor.getPose().toMatrix(modelMatrix, 0);
                // Calculate model/view/projection matrices
                Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

                // Update shader properties and draw
                virtualObjectShader.setMat4("u_ModelView", modelViewMatrix);
                virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
                render.draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer);
            }
        }
        // Compose the virtual scene with the background.
        backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR);


    }
    private Boolean[] pose = {false,false,false,false,false}; // 0 = -2 1 = -1  2 = 0  3 = 1 4 = 2
    // Handle only one tap per frame, as taps are usually low frequency compared to frame rate.
    private void handleTap(Frame frame, Camera camera) {
        //Pose cameraPose = frame.getCamera().getDisplayOrientedPose();
        List<HitResult> hitResultList;
        hitResultList =
                frame.hitTestInstantPlacement((float) 500, (float)1000,2);
        for (HitResult hit : hitResultList) {
            ScanAncors.add(hit.createAnchor());
            switch ((int)hit.createAnchor().getPose().tx()){
                case -2 :
                    pose[0]=true;
                    break;
                case -1:
                    pose[1]=true;
                    break;
                case 0:
                    pose[2]=true;
                    break;
                case 1:
                    pose[3]=true;
                    break;
                case 2:
                    pose[4]=true;
                    break;
            }
            if(AR_Mode==FindForm.AR_MODE.SEARCH_FINISH) {

                anchors.clear();

                anchors.add(hit.createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 300, (float)800,(float) 5.2);
                anchors.add(hitResultList.get(0).createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 380, (float)800,(float) 5.2);
                anchors.add(hitResultList.get(0).createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 480, (float)800,(float) 5.8);
                anchors.add(hitResultList.get(0).createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 580, (float)500,(float) 5.9);
                anchors.add(hitResultList.get(0).createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 680, (float)400,(float) 5);
                anchors.add(hitResultList.get(0).createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 780, (float)700,(float) 5.5);
                anchors.add(hitResultList.get(0).createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 880, (float)800,(float) 5.7);
                anchors.add(hitResultList.get(0).createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 980, (float)900,(float) 5.2);
                anchors.add(hitResultList.get(0).createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 350, (float)1000,(float)5.5);
                anchors.add(hitResultList.get(0).createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 400, (float)1300,(float)5);
                anchors.add(hitResultList.get(0).createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 450, (float)1200,(float)6);
                anchors.add(hitResultList.get(0).createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 550, (float)700,(float) 5.5);
                anchors.add(hitResultList.get(0).createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 650, (float)800,(float)5.8);
                anchors.add(hitResultList.get(0).createAnchor());
                hitResultList =
                        frame.hitTestInstantPlacement((float) 750, (float)900,(float)5);
                anchors.add(hitResultList.get(0).createAnchor());

                AR_Mode=FindForm.AR_MODE.SEARCHING;
            }

            if(pose[0]&&pose[1]&&pose[2]&&pose[3]&&pose[4]
                    &&((int)(hit.createAnchor().getPose().tx()*10)==(int)(ScanAncors.get(0).getPose().tx()*10))) { //&&(hit.createAnchor().getPose().tx()==ScanAncors.get(0).getPose().tx())
                ScanAncors.clear();
                AR_Mode=FindForm.AR_MODE.FINISH;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (session != null) {
            // Explicitly close ARCore Session to release native resources.
            // Review the API reference for important considerations before calling close() in apps with
            // more complicated lifecycle requirements:
            // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
            session.close();
            session = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }

                // Create the session.
                session = new Session(/* context= */ this);
            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                //messageSnackbarHelper.showError(this, message);
                //Log.e(TAG, "Exception creating session", exception);
                return;
            }
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            configureSession();
            // To record a live camera session for later playback, call
            // `session.startRecording(recorderConfig)` at anytime. To playback a previously recorded AR
            // session instead of using the live camera feed, call
            // `session.setPlaybackDataset(playbackDatasetPath)` before calling `session.resume()`. To
            // learn more about recording and playback, see:
            // https://developers.google.com/ar/develop/java/recording-and-playback
            session.resume();
        } catch (CameraNotAvailableException e) {
            messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
            session = null;
            return;
        }

        surfaceView.onResume();
        displayRotationHelper.onResume();
    }

    private void configureSession() {
        Config config = session.getConfig();
        config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        } else {
            config.setDepthMode(Config.DepthMode.DISABLED);
        }
        /*if (instantPlacementSettings.isInstantPlacementEnabled()) {
            config.setInstantPlacementMode(Config.InstantPlacementMode.LOCAL_Y_UP);
        } else {
            config.setInstantPlacementMode(Config.InstantPlacementMode.DISABLED);
        }*/
        config.setInstantPlacementMode(Config.InstantPlacementMode.LOCAL_Y_UP);
        session.configure(config);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            // Use toast instead of snackbar here since the activity will exit.
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }
}