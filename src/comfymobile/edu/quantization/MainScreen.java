package comfymobile.edu.quantization;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.*;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainScreen extends Activity implements SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback, Camera.PreviewCallback, Camera.AutoFocusCallback
{
    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private SurfaceView preview;
    private Button shotBtn;
    private ImageView iv;
    private SeekBar seekBarLvl;

    private int lvl = 2;
    int color[] = new int[lvl];



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // если хотим, чтобы приложение постоянно имело портретную ориентацию
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // если хотим, чтобы приложение было полноэкранным
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // и без заголовка
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        // наше SurfaceView имеет имя SurfaceView01
        preview = (SurfaceView) findViewById(R.id.SurfaceView01);

        surfaceHolder = preview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // кнопка имеет имя Button01
        shotBtn = (Button) findViewById(R.id.Button01);
        shotBtn.setText("Shot");
        shotBtn.setOnClickListener(this);
        for (int i = 0 ; i < lvl ; i++){
            color[i] = (int)(i*256./lvl);
        }
        initUI();
    }

   void initUI(){
       iv = (ImageView) findViewById(R.id.imageView);

       seekBarLvl = (SeekBar) findViewById(R.id.seekBarLvl);
       seekBarLvl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           @Override
           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

           }

           @Override
           public void onStartTrackingTouch(SeekBar seekBar) {
               //To change body of implemented methods use File | Settings | File Templates.
           }

           @Override
           public void onStopTrackingTouch(SeekBar seekBar) {
               lvl = seekBar.getProgress()+2;

               color = new int[lvl];
               for (int i = 0 ; i < lvl ; i++){
                   color[i] = (int)(i*256./lvl);
               }

               Toast toast = Toast.makeText(getApplicationContext(),
                       "Color count = "+ String.valueOf(lvl), Toast.LENGTH_SHORT);
               toast.show();
           }
       });
       paint.setColorFilter(filter);

   }

    @Override
    protected void onResume()
    {
        super.onResume();
        camera = Camera.open();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (camera != null)
        {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        try
        {
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(this);
            Camera.Parameters parameters = camera.getParameters();
            Size size = parameters.getPreviewSize();
            w =  size.width;
            h = size.height;
            bm = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bm);
            this.size = w*h;
            this.offset = this.size;
            pixels= new int[this.size];
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Size previewSize = camera.getParameters().getPreviewSize();
        float aspect = (float) previewSize.width / previewSize.height;

        int previewSurfaceWidth = preview.getWidth();
        int previewSurfaceHeight = preview.getHeight();

        LayoutParams lp = preview.getLayoutParams();

        // здесь корректируем размер отображаемого preview, чтобы не было искажений

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
            // портретный вид
            camera.setDisplayOrientation(90);
            lp.height = previewSurfaceHeight;
            lp.width = (int) (previewSurfaceHeight / aspect);
            ;
        }
        else
        {
            // ландшафтный
            camera.setDisplayOrientation(0);
            lp.width = previewSurfaceWidth;
            lp.height = (int) (previewSurfaceWidth / aspect);
        }

        preview.setLayoutParams(lp);
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }

    @Override
    public void onClick(View v)
    {
        if (v == shotBtn)
        {
            // либо делаем снимок непосредственно здесь
            // 	либо включаем обработчик автофокуса

            //camera.takePicture(null, null, null, this);
            camera.autoFocus(this);
        }

    }

    @Override
    public void onPictureTaken(byte[] paramArrayOfByte, Camera paramCamera)
    {
        // сохраняем полученные jpg в папке /sdcard/CameraExample/
        // имя файла - System.currentTimeMillis()

        try
        {
            File saveDir = new File("/sdcard/CameraExample/");

            if (!saveDir.exists())
            {
                saveDir.mkdirs();
            }

            FileOutputStream os = new FileOutputStream(String.format("/sdcard/CameraExample/%d.jpg", System.currentTimeMillis()));
            os.write(paramArrayOfByte);
            os.close();
        }
        catch (Exception e)
        {
        }

        // после того, как снимок сделан, показ превью отключается. необходимо включить его
        paramCamera.startPreview();
    }

    @Override
    public void onAutoFocus(boolean paramBoolean, Camera paramCamera)
    {
        if (paramBoolean)
        {
            // если удалось сфокусироваться, делаем снимок
            paramCamera.takePicture(null, null, null, this);
        }
    }

    @Override
    public void onPreviewFrame(byte[] paramArrayOfByte, Camera paramCamera)
    {
        uploadImage(paramArrayOfByte);
    }

    int w;
    int h;
    Bitmap bm;
    Canvas canvas;
    ColorMatrixColorFilter filter=new ColorMatrixColorFilter(new float[]{
            1f, 0.0f, 0f, 0, 0f,
            0f, 1f, 0.f, 0, 0f,
            0f, 0.f, 1f, 0, 0,
            0, 0, 0, 1, 0
    });
    Paint paint = new Paint();

    private void uploadImage(byte[] imageData) {
        // the bitmap we want to fill with the image
        int[] pixels = convertYUV420_NV21toRGB8888(imageData);
        canvas.drawBitmap(pixels,0,w,0f,0f,w,h,false,paint);
        iv.setImageBitmap(bm);
    }

    int size;
    int offset;
    int[] pixels;

    private int[] convertYUV420_NV21toRGB8888(byte [] data) {
        int u, v, y1, y2, y3, y4;
        // i percorre os Y and the final pixels
        // k percorre os pixles U e V
        for(int i=0, k=0; i < size; i+=2, k+=2) {
            y1 = data[i  ]&0xff;
            y2 = data[i+1]&0xff;
            y3 = data[w+i  ]&0xff;
            y4 = data[w+i+1]&0xff;

            u = data[offset+k  ]&0xff;
            v = data[offset+k+1]&0xff;
            u = u-128;
            v = v-128;

            pixels[i  ] = convertYUVtoRGB(y1, u, v);
            pixels[i+1] = convertYUVtoRGB(y2, u, v);
            pixels[w+i  ] = convertYUVtoRGB(y3, u, v);
            pixels[w+i+1] = convertYUVtoRGB(y4, u, v);

            if (i!=0 && (i+2)%w==0)
                i+=w;
        }

        return pixels;
    }

    private  int convertYUVtoRGB(int y, int u, int v) {
        int r,g,b;

        r = y + (int)1.402f*v;
        g = y - (int)(0.344f*u +0.714f*v);
        b = y + (int)1.772f*u;
        boolean rflag = true,gflag = true,bflag = true;
        for (int i = 0; i < lvl-1 && (rflag || gflag || bflag) ; i++){
            if( r < color[i+1]  && rflag ){
               r = color[i];
               rflag  = false;
            }
            if( g < color[i+1]  && gflag ){
                g = color[i];
                gflag  = false;
            }
            if( b < color[i+1]  && bflag ){
                b = color[i];
                bflag  = false;
            }
        }
        r = r >= color[lvl-1] ?  255: r;
        g = g >= color[lvl-1] ?  255: g;
        b = b >= color[lvl-1] ?  255: b;
        return 0xff000000 | (b<<16) | (g<<8) | r;
    }

}