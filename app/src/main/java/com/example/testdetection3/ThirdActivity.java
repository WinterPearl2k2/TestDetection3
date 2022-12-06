//package com.example.testdetection3;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.CameraX;
//import androidx.camera.core.ImageAnalysis;
//import androidx.camera.core.ImageProxy;
//import androidx.camera.core.Preview;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.core.content.ContextCompat;
//import androidx.databinding.DataBindingUtil;
//import androidx.lifecycle.LifecycleOwner;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.content.DialogInterface;
//import android.content.pm.PackageManager;
//import android.content.res.AssetFileDescriptor;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.graphics.PorterDuff;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.os.Bundle;
//import android.text.InputType;
//import android.util.Log;
//import android.util.Pair;
//import android.util.Size;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//
//import com.example.testdetection3.databinding.ActivityMainBinding;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.android.gms.vision.face.FaceDetector;
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.firebase.ml.vision.FirebaseVision;
//import com.google.firebase.ml.vision.common.FirebaseVisionImage;
//import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
//import com.google.firebase.ml.vision.face.FirebaseVisionFace;
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
//
//import org.tensorflow.lite.Interpreter;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.MappedByteBuffer;
//import java.nio.channels.FileChannel;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//public class MainActivity extends AppCompatActivity {
//    ActivityMainBinding binding;
//    FirebaseVisionImage firebaseVisionImage;
//    boolean start=true,flipX=false, flag = false;
//    String [] PERMISSIONS = new String[] {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);//Load model
//        try {
//            tfLite=new Interpreter(loadModelFile(MainActivity.this,modelFile));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if(checkPermission()) {
//            binding.previewView.post(this::initCamera);
//        }
//        binding.imageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                addFace();
//            }
//        });
//        addImage();
//    }
//
//    int img[] = new int[] {R.drawable.img_son_tung, R.drawable.img_chipu};
//    String name[] = new String[] {"Sơn Tùng", "Chi pu"};
//
//    private void addImage() {
//        for(int i = 0; i < img.length; i++) {
//            firebaseVisionImage = FirebaseVisionImage.fromBitmap(BitmapFactory.decodeResource(getResources(),
//                    img[i]));
//            Log.e("AAA", "1");
//            flag = true;
//            bundleImage(firebaseVisionImage, flag, name[i]);
//            flag = false;
//        }
//    }
//
//    private void addFace() {
//        {
//
//            start=false;
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("Enter Name");
//
//            // Set up the input
//            final EditText input = new EditText(this);
//
//            input.setInputType(InputType.TYPE_CLASS_TEXT );
//            builder.setView(input);
//
//            // Set up the buttons
//            builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    //Toast.makeText(context, input.getText().toString(), Toast.LENGTH_SHORT).show();
//
//                    //Create and Initialize new object with Face embeddings and Name.
//                    SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
//                            "0", "", -1f);
//                    result.setExtra(embeedings);
//                    Log.e("AAA", result + "");
//                    registered.put( input.getText().toString(),result);
//                    start=true;
//
//                }
//            });
//            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    start=true;
//                    dialog.cancel();
//                }
//            });
//
//            builder.show();
//        }
//    }
//
//    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
//        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
//        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
//        FileChannel fileChannel = inputStream.getChannel();
//        long startOffset = fileDescriptor.getStartOffset();
//        long declaredLength = fileDescriptor.getDeclaredLength();
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
//    }
//    private HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>(); //saved Faces
//    CameraSelector cameraSelector;
//    FaceDetector detector;
//    ProcessCameraProvider cameraProvider;
//
//    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
//    int cam_face=CameraSelector.LENS_FACING_BACK; //Default Back Camera
//    private void initCamera() {
//        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
//        cameraProviderFuture.addListener(() -> {
//            try {
//                cameraProvider = cameraProviderFuture.get();
//
//                Preview preview = new Preview.Builder()
//                        .setTargetResolution(new Size(binding.previewView.getWidth(), binding.previewView.getHeight()))
//                        .build();
//
//                cameraSelector = new CameraSelector.Builder()
//                        .requireLensFacing(cam_face)
//                        .build();
//
//                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
//                ImageAnalysis imageAnalysis =
//                        new ImageAnalysis.Builder()
//                                .setTargetResolution(new Size(binding.previewView.getWidth(), binding.previewView.getHeight()))
//                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
//                                .build();
//
//                Executor executor = Executors.newSingleThreadExecutor();
//                imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
//                    @SuppressLint("UnsafeOptInUsageError")
//                    @Override
//                    public void analyze(@NonNull ImageProxy imageProxy) {
//                        if(imageProxy == null || imageProxy.getImage() == null)
//                            return;
//
//                        initDraw();
//                        initDetector();
//
//                        int rotation = degressToFirebaseRotation(imageProxy.getImageInfo().getRotationDegrees());
//                        firebaseVisionImage = FirebaseVisionImage.fromMediaImage(imageProxy.getImage(), rotation);
//                        bundleImage(firebaseVisionImage, false, "");
//                    }
//                });
//                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
//            } catch (ExecutionException | InterruptedException e) {
//                // No errors need to be handled for this in Future.
//                // This should never be reached.
//            }
//        }, ContextCompat.getMainExecutor(this));
//
//    }
//    private void bundleImage(FirebaseVisionImage firebaseVisionImage, boolean b, String s) {
//        FirebaseVisionFaceDetectorOptions detectorOptions = new FirebaseVisionFaceDetectorOptions.Builder()
//                .enableTracking().build();
//        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(detectorOptions);
//        Task<List<FirebaseVisionFace>> result =
//                detector.detectInImage(firebaseVisionImage)
//                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
//                            @Override
//                            public void onSuccess(List<FirebaseVisionFace> faces) {
//                                if(faces.size()!=0) {
//                                    FirebaseVisionFace face = faces.get(0); //Get first face from detected faces
////                                                    System.out.println(face);
//
////                                            //mediaImage to Bitmap
////                                            Bitmap frame_bmp = toBitmap(mediaImage);
////
////                                            int rot = imageProxy.getImageInfo().getRotationDegrees();
////
////                                            //Adjust orientation of Face
////                                            Bitmap frame_bmp1 = rotateBitmap(frame_bmp, rot, false, false);
//
//
//
//                                    //Get bounding box of face
//                                    RectF boundingBox = new RectF(face.getBoundingBox());
//
//                                    //Crop out bounding box from whole Bitmap(image)
//                                    Bitmap cropped_face = getCropBitmapByCPU(firebaseVisionImage.getBitmap(), boundingBox);
//
////                                            if(flipX)
////                                                cropped_face = rotateBitmap(cropped_face, 0, flipX, false);
//                                    //Scale the acquired Face to 112*112 which is required input for model
//                                    Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);
//
//                                    if(start) {
//                                        recognizeImage(scaled, b, s); //Send scaled bitmap to create face embeddings.
////                                                    System.out.println(boundingBox);
//                                    }
//
//                                }
//                                else
//                                {
//                                    if(registered.isEmpty()) {
//                                        binding.textView.setText("Name ???");
//                                        binding.imageView.setVisibility(View.VISIBLE);
//                                    } else {
//                                        binding.textView.setText("I can't see");
//                                        binding.imageView.setVisibility(View.INVISIBLE);
//                                    }
//                                }
//                            }
//                        });
//    }
////    private void initCamera() {
////        CameraX.unbindAll();
////        PreviewConfig config = new PreviewConfig.Builder()
//////                .setTargetResolution(new Size(binding.previewView.getWidth(), binding.previewView.getHeight()))
////                .setTargetResolution(new Size(binding.previewView.getWidth(), binding.previewView.getHeight()))
////                .setLensFacing(CameraX.LensFacing.BACK)
////                .build();
////        Preview preview = new Preview(config);
////        preview.setOnPreviewOutputUpdateListener(output -> {
////            ViewGroup group = (ViewGroup) binding.previewView.getParent();
////            group.removeView(binding.previewView);
////            group.addView(binding.previewView, 0);
////            binding.previewView.setSurfaceTexture(output.getSurfaceTexture());
////        });
////
////        ImageAnalysisConfig analysisConfig = new ImageAnalysisConfig.Builder()
////                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
////                .setTargetResolution(new Size(binding.previewView.getWidth(), binding.previewView.getHeight()))
////                .setLensFacing(CameraX.LensFacing.BACK)
////                .build();
////        Executor executor = Executors.newSingleThreadExecutor();
////        ImageAnalysis imageAnalysis = new ImageAnalysis(analysisConfig);
////        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
////            @Override
////            public void analyze(ImageProxy image, int rotationDegrees) {
////                if(image == null || image.getImage() == null)
////                    return;
////
////                initDraw();
////                initDetector();
////
////                int rotation = degressToFirebaseRotation(rotationDegrees);
////                firebaseVisionImage = FirebaseVisionImage.fromMediaImage(image.getImage(), rotation);
////                bundleImage(firebaseVisionImage, false, "");
////            }
////        });
////        CameraX.bindToLifecycle(this, preview, imageAnalysis);
////    }
////
////    private void bundleImage(FirebaseVisionImage firebaseVisionImage, boolean b, String s) {
////        FirebaseVisionFaceDetectorOptions detectorOptions = new FirebaseVisionFaceDetectorOptions.Builder()
////                .enableTracking().build();
////        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(detectorOptions);
////        Task<List<FirebaseVisionFace>> result =
////                detector.detectInImage(firebaseVisionImage)
////                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
////                            @Override
////                            public void onSuccess(List<FirebaseVisionFace> faces) {
////                                if(faces.size()!=0) {
////                                    FirebaseVisionFace face = faces.get(0); //Get first face from detected faces
//////                                                    System.out.println(face);
////
//////                                            //mediaImage to Bitmap
//////                                            Bitmap frame_bmp = toBitmap(mediaImage);
//////
//////                                            int rot = imageProxy.getImageInfo().getRotationDegrees();
//////
//////                                            //Adjust orientation of Face
//////                                            Bitmap frame_bmp1 = rotateBitmap(frame_bmp, rot, false, false);
////
////
////
////                                    //Get bounding box of face
////                                    RectF boundingBox = new RectF(face.getBoundingBox());
////
////                                    //Crop out bounding box from whole Bitmap(image)
////                                    Bitmap cropped_face = getCropBitmapByCPU(firebaseVisionImage.getBitmap(), boundingBox);
////
//////                                            if(flipX)
//////                                                cropped_face = rotateBitmap(cropped_face, 0, flipX, false);
////                                    //Scale the acquired Face to 112*112 which is required input for model
////                                    Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);
////
////                                    if(start) {
////                                        recognizeImage(scaled, b, s); //Send scaled bitmap to create face embeddings.
//////                                                    System.out.println(boundingBox);
////                                    }
////
////                                }
////                                else
////                                {
////                                    if(registered.isEmpty()) {
////                                        binding.textView.setText("Name ???");
////                                        binding.imageView.setVisibility(View.VISIBLE);
////                                    } else {
////                                        binding.textView.setText("I can't see");
////                                        binding.imageView.setVisibility(View.INVISIBLE);
////                                    }
////                                }
////                            }
////                        });
////    }
//
//    int[] intValues;
//    Interpreter tfLite;
//    int inputSize=112;  //Input size for model
//    boolean isModelQuantized=false;
//    float[][] embeedings;
//    float IMAGE_MEAN = 128.0f;
//    float IMAGE_STD = 128.0f;
//    int OUTPUT_SIZE=192; //Output size of model
//    private static int SELECT_PICTURE = 1;
//    private static final int MY_CAMERA_REQUEST_CODE = 100;
//    String modelFile="mobile_face_net.tflite"; //model name
//    float distance= 1.0f;
//    private void recognizeImage(Bitmap bitmap, boolean b, String s) {
//        binding.imageView.setImageBitmap(bitmap);
//        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);
//
//        imgData.order(ByteOrder.nativeOrder());
//
//        intValues = new int[inputSize * inputSize];
//
//        //get pixel values from Bitmap to normalize
//        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//
//        imgData.rewind();
//
//        for (int i = 0; i < inputSize; ++i) {
//            for (int j = 0; j < inputSize; ++j) {
//                int pixelValue = intValues[i * inputSize + j];
//                if (isModelQuantized) {
//                    // Quantized model
//                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
//                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
//                    imgData.put((byte) (pixelValue & 0xFF));
//                } else { // Float model
//                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
//                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
//                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
//
//                }
//            }
//        }
//        //imgData is input to our model
//        Object[] inputArray = {imgData};
//
//        Map<Integer, Object> outputMap = new HashMap<>();
//
//
//        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable
//        outputMap.put(0, embeedings);
//        if(b) {
//            SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
//                    "0", "", -1f);
//            result.setExtra(embeedings);
//            registered.put( s,result);
//            start=true;
//        }
//
//        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model
//
//
//
//        float distance_local = Float.MAX_VALUE;
//        String id = "0";
//        String label = "?";
//
//        //Compare new face with saved Faces.
//        if (registered.size() > 0) {
//
//            final List<Pair<String, Float>> nearest = findNearest(embeedings[0]);//Find 2 closest matching face
//
//            if (nearest.get(0) != null) {
//
//                final String name = nearest.get(0).first; //get name and distance of closest matching face
//                // label = name;
//                distance_local = nearest.get(0).second;
////                if (developerMode)
////                {
////                if(distance_local<distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
////                    binding.textView.setText("Nearest: "+name +"\nDist: "+ String.format("%.3f",distance_local)+"\n2nd Nearest: "+nearest.get(1).first +"\nDist: "+ String.format("%.3f",nearest.get(1).second));
////                else
////                    binding.textView.setText("Unknown "+"\nDist: "+String.format("%.3f",distance_local)+"\nNearest: "+name +"\nDist: "+ String.format("%.3f",distance_local)+"\n2nd Nearest: "+nearest.get(1).first +"\nDist: "+ String.format("%.3f",nearest.get(1).second));
//
////                    System.out.println("nearest: " + name + " - distance: " + distance_local);
////                }
////                else
////                {
//
//                if(distance_local<distance) { //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
//                    binding.imageView.setVisibility(View.VISIBLE);
//                    binding.textView.setText(name);
//                } else {
//                    binding.textView.setText("I don't know him/her");
////                    System.out.println("nearest: " + name + " - distance: " + distance_local);
//                }
//            }
//        }
//
//
////            final int numDetectionsOutput = 1;
////            final ArrayList<SimilarityClassifier.Recognition> recognitions = new ArrayList<>(numDetectionsOutput);
////            SimilarityClassifier.Recognition rec = new SimilarityClassifier.Recognition(
////                    id,
////                    label,
////                    distance);
////
////            recognitions.add( rec );
//
//    }//Compare Faces by distance between face embeddings
//    private List<Pair<String, Float>> findNearest(float[] emb) {
//        List<Pair<String, Float>> neighbour_list = new ArrayList<Pair<String, Float>>();
//        Pair<String, Float> ret = null; //to get closest match
//        Pair<String, Float> prev_ret = null; //to get second closest match
//        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet())
//        {
//
//            final String name = entry.getKey();
//            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];
//
//            float distance = 0;
//            for (int i = 0; i < emb.length; i++) {
//                float diff = emb[i] - knownEmb[i];
//                distance += diff*diff;
//            }
//            distance = (float) Math.sqrt(distance);
//            if (ret == null || distance < ret.second) {
//                prev_ret=ret;
//                ret = new Pair<>(name, distance);
//            }
//        }
//        if(prev_ret==null) prev_ret=ret;
//        neighbour_list.add(ret);
//        neighbour_list.add(prev_ret);
//
//        return neighbour_list;
//
//    }
//
//    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
//        int width = bm.getWidth();
//        int height = bm.getHeight();
//        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
//        // CREATE A MATRIX FOR THE MANIPULATION
//        Matrix matrix = new Matrix();
//        // RESIZE THE BIT MAP
//        matrix.postScale(scaleWidth, scaleHeight);
//
//        // "RECREATE" THE NEW BITMAP
//        Bitmap resizedBitmap = Bitmap.createBitmap(
//                bm, 0, 0, width, height, matrix, false);
//        bm.recycle();
//        return resizedBitmap;
//    }
//
//    private Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
//        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
//                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
//        Canvas cavas = new Canvas(resultBitmap);
//
//        // draw background
//        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
//        paint.setColor(Color.WHITE);
//        cavas.drawRect(
//                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
//                paint);
//
//        Matrix matrix = new Matrix();
//        matrix.postTranslate(-cropRectF.left, -cropRectF.top);
//
//        cavas.drawBitmap(source, matrix, paint);
//
////        if (source != null && !source.isRecycled()) {
////            source.recycle();
////        }
//
//        return resultBitmap;
//    }
//
//    private boolean checkPermission() {
//        for(String permisson : PERMISSIONS) {
//            if(ContextCompat.checkSelfPermission(this, permisson) != PackageManager.PERMISSION_GRANTED)
//                return false;
//        }
//        return true;
//    }
//
//    Bitmap bitmap;
//    Canvas canvas;
//    Paint paint;
//    float widthScalerFactor = 1.0f, heightScalerFactor = 1.0f;
//    private void initDraw() {
//        bitmap = Bitmap.createBitmap(binding.previewView.getWidth(), binding.previewView.getHeight(), Bitmap.Config.ARGB_8888);
//        canvas = new Canvas(bitmap);
//        paint = new Paint();
//        paint.setColor(Color.GREEN);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(5f);
//        //Hệ số tỷ lệ = hỉnh ảnh lớn hơn / hình ảnh nhỏ hơn
//        widthScalerFactor = canvas.getWidth() / (firebaseVisionImage.getBitmap().getWidth() * 1.0f);
//        heightScalerFactor = canvas.getHeight() / (firebaseVisionImage.getBitmap().getHeight() * 1.0f);
//    }
//
//    private void initDetector() {
//        FirebaseVisionFaceDetectorOptions detectorOptions = new FirebaseVisionFaceDetectorOptions.Builder()
//                .enableTracking().build();
//        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(detectorOptions);
//        detector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
//            @Override
//            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
//                if(!firebaseVisionFaces.isEmpty()) {
//                    binding.trackingImgView.setVisibility(View.VISIBLE);
//                    processFaces(firebaseVisionFaces);
//                } else {
//                    binding.trackingImgView.setVisibility(View.INVISIBLE);
//                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.e("AAA", "Fail");
//            }
//        });
//    }
//
//    private void processFaces(List<FirebaseVisionFace> firebaseVisionFaces) {
//        for(FirebaseVisionFace face : firebaseVisionFaces) {
//            Rect box = new Rect((int) translateX(face.getBoundingBox().left),
//                    (int) translateY(face.getBoundingBox().top),
//                    (int) translateX(face.getBoundingBox().right),
//                    (int) translateY(face.getBoundingBox().bottom));
//            canvas.drawRect(box, paint);
//        }
//        binding.trackingImgView.setImageBitmap(bitmap);
//    }
//
//    private float translateX(int x) {
//        float scaledX = x * widthScalerFactor;
//        return scaledX;
//    }
//
//    private float translateY(int y) {
//        float scaledY = y * heightScalerFactor;
//        return scaledY;
//    }
//
//    private int degressToFirebaseRotation(int rotationDegrees) {
//        switch (rotationDegrees) {
//            case 0:
//                return FirebaseVisionImageMetadata.ROTATION_0;
//            case 90:
//                return FirebaseVisionImageMetadata.ROTATION_90;
//            case 180:
//                return FirebaseVisionImageMetadata.ROTATION_180;
//            case 270:
//                return FirebaseVisionImageMetadata.ROTATION_270;
//            default:
//                throw new IllegalArgumentException("error");
//        }
//    }
//}