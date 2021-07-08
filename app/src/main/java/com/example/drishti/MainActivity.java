package com.example.drishti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.drishti.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private PaintView paintView;
  public  Bitmap mainBitmap;
  ActivityMainBinding binding;
    private TextToSpeech mTTs;
    EditText input;
  String text="";
    public int rotationDegree = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



       mTTs=new TextToSpeech(this,this);

        paintView = (PaintView) findViewById(R.id.paintView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
       mainBitmap= paintView.init(metrics);

binding.ButtonResult.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        InputImage image = InputImage.fromBitmap(mainBitmap,rotationDegree);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {

                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    String blockText = block.getText();
                                    Point[] blockCornerPoints = block.getCornerPoints();
                                    Rect blockFrame = block.getBoundingBox();
                                    for (Text.Line line : block.getLines()) {
                                        String lineText = line.getText();
                                        Point[] lineCornerPoints = line.getCornerPoints();
                                        Rect lineFrame = line.getBoundingBox();
                                        for (Text.Element element : line.getElements()) {
                                            String elementText = element.getText();
                                            Point[] elementCornerPoints = element.getCornerPoints();
                                            Rect elementFrame = element.getBoundingBox();
                                        text=elementText;
                                        }
                                    }
                                }



                                AlertDialog.Builder d=new AlertDialog.Builder(MainActivity.this);
                                        d.setCancelable(false);
                                        d.create();

                                if(text.isEmpty()==true) {
                                    d.setTitle("Sorry No Text Detected.Please Redraw!!");

                                     d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialog, int which) {
                                             dialog.dismiss();
                                         }
                                     });

                                }
                                else {
                                    d.setTitle("Detected Text is :");
                                    final EditText input=new EditText(MainActivity.this);
                                    input.setText(text);
                                    d.setView(input);

                                    d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    d.setNeutralButton("Speech", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                          speak();

                                        }
                                    });


                                }


                                 d.show();


                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this,"Text cann't be detected",Toast.LENGTH_SHORT).show();
                                    }
                                });

    }
});

   binding.clear.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View v) {
           paintView.clear();
           text="";
       }
   });



        binding.blur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.blur();
            }
        });

        binding.normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.normal();
            }
        });


    }





    @Override
    protected void onDestroy(){
        if(mTTs!=null)
        {
            mTTs.stop();
            mTTs.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if(status== TextToSpeech.SUCCESS){
            int result=mTTs.setLanguage(Locale.getDefault());
            mTTs.setSpeechRate(status);
            mTTs.setPitch(status);
            if(result==TextToSpeech.LANG_MISSING_DATA||result==TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("mtts","Language not detected");
            }
            else
            {
                speak();
            }
        }
        else
        {
            Log.e("TextToSpeech","Intialization Failed");
        }
    }

    private void speak()
    {
        mTTs.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
    }
}
