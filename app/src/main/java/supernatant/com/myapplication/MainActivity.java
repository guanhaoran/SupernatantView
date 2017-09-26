package supernatant.com.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView viewById;
    private TextView viewById1;
    private TextView viewById2;
    private SupernatantView supernatantView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        viewById = (TextView) findViewById(R.id.btn_click);
        viewById1 = (TextView) findViewById(R.id.btn_click1);
        viewById2 = (TextView) findViewById(R.id.btn_click2);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"点击",Toast.LENGTH_LONG).show();
            }
        });
        supernatantView = new SupernatantView(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        supernatantView.setShowView(viewById, viewById1, viewById2).
                setShowShape(Contans.GUIDE_RECT, Contans.GUIDE_CIRCLE,Contans.GUIDE_ROUND_RECT).
                setHintText("我是测试文字1111", "我是测试文字2222").
                show();
    }
}
