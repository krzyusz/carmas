package com.example.krzysiek.carmas;



import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.Button;
import android.content.Intent;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;


public class MainActivity extends AppCompatActivity implements GPSCallback{

    private Button but1;
    private Button but2;
    private Button but3;
    private Button but4;
    private BTService serialPort; //************************************************
    boolean mBounded;//************************************************
    private BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("98:5D:AD:1F:73:46");//************************************************
    public String kod = "";
    public String currentLayout = "";
    RelativeLayout rellay1, rellay2,rellay4;
    public int layoutId = 0;
    // 1 - login layout
    // 2 - register layout
    // 0 - main menu
    //
    Handler handler = new Handler();
    Handler handler1 = new Handler();
    Handler buttonHandler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            rellay1.setVisibility(View.VISIBLE);
            rellay2.setVisibility(View.VISIBLE);

        }
    };

    Runnable runnable1 = new Runnable() {
        @Override
        public void run() {


            if(getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                ImageView v = (ImageView) findViewById(R.id.imgView_logohorizontal);
                if(v!=null){
                    v.setVisibility(View.GONE);
                }

            }


            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    rellay4.setVisibility(View.VISIBLE);
                }
            },400);
        }
    };



    private View.OnClickListener but1list = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            button1Clicked();
        }
    };

    private View.OnClickListener but2list = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            button2Clicked();
        }
    };

    private View.OnClickListener but3list = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            button3Clicked();
        }
    };

    private View.OnClickListener but4list = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            button4Clicked();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CarmasSettings.checkSettings(this);

       // Log.e("asdasdasd",String.valueOf(layoutId));
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        RouteFile.resetRouteFiles(getApplicationContext());
        CarmasFunctions.checkFiles(getApplicationContext());
        initialiseElements();
        changeLayout(this,"","");





        /*if(CarmasSettings.isLogged(this)){
            createDirectory();
            Toast.makeText(this,"zalogowany jako "+CarmasSettings.login,Toast.LENGTH_SHORT).show();
            linearLayout.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }*/







        Intent mIntent = new Intent(this, BTService.class);//************************************************
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);//************************************************

        CarmasSettings.checkSettings(this);
    }

    ServiceConnection mConnection = new ServiceConnection() {//********************************************************************************
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "BTService odłączony", Toast.LENGTH_SHORT).show();
            mBounded = false;
            serialPort = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                mBounded = true;
                BTService.LocalBinder mLocalBinder = (BTService.LocalBinder) service;
                serialPort = mLocalBinder.getBTServiceInstance();
                serialPort.connect(device);
            }catch(Exception e){}
        }
    };//******************************************************************************************************************************************



    private void button1Clicked(){
        serialPort.send("1");
        but1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bgpressed));
        buttonHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                but1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bg));
                Intent a = new Intent(getApplicationContext(),SensorActivity2.class);
                startActivity(a);
                serialPort.send("1");
            }
        },150);
    }

    private void button2Clicked(){
        but2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bgpressed));
        buttonHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                but2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bg));
                Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
                startActivity(intent);
            }
        },150);

    }

    private void button3Clicked(){
        but3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bgpressed));
        buttonHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                but3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bg));
                Intent g = new Intent(getApplicationContext(),GalleryActivity.class);
                startActivity(g);
            }
        },150);

    }

    private void button4Clicked(){
        but4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bgpressed));
        Intent settings = new Intent(this,SettingsActivity.class);
        startActivity(settings);
        finish();
        buttonHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                but4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bg));
            }
        },150);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(gpsManager!=null){
            gpsManager.stopListening();
            gpsManager.setGPSCallback(null);
            gpsManager = null;
        }
        super.onDestroy();
        //serialPort.send("0");
        unbindService(mConnection);
    }

    @Override
    public void onBackPressed() {
        if(layoutId == 2){
            changeLayout(getApplicationContext(),"","back");
        }else if(layoutId == 0) {
            super.onBackPressed();
        }else{
            super.onBackPressed();
        }


    }

    public boolean createDirectory(){
        try {

            File f = new File(this.getExternalFilesDir(null).getAbsolutePath() + "/" + CarmasSettings.kod);
            if(f.exists()){
                File f2 = new File(this.getExternalFilesDir(null).getAbsolutePath() + "/" + CarmasSettings.kod + "/trasy");
                if(f2.exists()){
                    return true;
                }else{
                    boolean b = f2.mkdir();
                    return b;
                }
            }else{
                boolean bool = f.mkdir();
                File f2 = new File(this.getExternalFilesDir(null).getAbsolutePath() + "/" + CarmasSettings.kod + "/trasy");
                if(f2.exists()){
                    return true;
                }else{
                    boolean b = f2.mkdir();
                    return b;
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private void changeLayout(Context context,String tag,String s){
        final Context appContext = context;
        if(!CarmasSettings.isLogged(appContext)){
            setContentView(R.layout.activity_mainlogin);
            setLayout("login_layout");
            layoutId = 1;
            rellay1 = (RelativeLayout) findViewById(R.id.rellay1);
            rellay2 = (RelativeLayout) findViewById(R.id.rellay2);
            if(s.contains("back")||s.contains("logged")){
                handler.postDelayed(runnable, 500); // --------animacja loga
            }else{
                handler.postDelayed(runnable, 2000); // ----animacja loga
            }


            if(tag.equals("Zły login lub hasło")){

            }
            loginLayoutV();

        }else{
            setContentView(R.layout.activity_buttons);
            rellay4 = (RelativeLayout) findViewById(R.id.rellay4);

            handler1.postDelayed(runnable1, 500); // --------animacja loga
            setLayout("main_menu_layout");
            layoutId = 0;
            but1 = (Button) findViewById(R.id.button1);
            but1.setOnClickListener(but1list);
            but2 = (Button) findViewById(R.id.button2);
            but2.setOnClickListener(but2list);
            but3 = (Button) findViewById(R.id.button3);
            but3.setOnClickListener(but3list);
            but4 = (Button) findViewById(R.id.button4);
            but4.setOnClickListener(but4list);

            createDirectory();
        }
    }



    public void setLayout(String s){
        this.currentLayout = s;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(layoutId == 1){
            EditText loginInput = (EditText) findViewById(R.id.loginInput);
            EditText passwordInput = (EditText) findViewById(R.id.passwordInput);
            final String login = loginInput.getText().toString();
            final String password = passwordInput.getText().toString();
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setContentView(R.layout.activity_mainlogin);
                rellay1 = (RelativeLayout) findViewById(R.id.rellay1);
                rellay2 = (RelativeLayout) findViewById(R.id.rellay2);
                handler.postDelayed(runnable, 100);
                loginInput = (EditText) findViewById(R.id.loginInput);
                passwordInput = (EditText) findViewById(R.id.passwordInput);
                loginInput.setText(login);
                passwordInput.setText(password);
                loginLayoutV();

            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
                setContentView(R.layout.activity_mainlogin);
                rellay1 = (RelativeLayout) findViewById(R.id.rellay1);
                rellay2 = (RelativeLayout) findViewById(R.id.rellay2);
                handler.postDelayed(runnable, 100);
                loginInput = (EditText) findViewById(R.id.loginInput);
                passwordInput = (EditText) findViewById(R.id.passwordInput);
                loginInput.setText(login);
                passwordInput.setText(password);
                loginLayoutV();
            }
        }else if(layoutId == 2){

            String registerLogin = ((EditText) findViewById(R.id.registerLoginInput)).getText().toString();
            String registerEmail = ((EditText) findViewById(R.id.registerEmailInput)).getText().toString();
            String registerPass1 = ((EditText) findViewById(R.id.registerPass1Input)).getText().toString();
            String registerPass2 = ((EditText) findViewById(R.id.registerPass2Input)).getText().toString();

            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setContentView(R.layout.activity_mainregister);
                final RelativeLayout rellay3 = (RelativeLayout) findViewById(R.id.rellay3);
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        rellay3.setVisibility(View.VISIBLE);

                    }
                };
                handler.postDelayed(runnable, 100);
                ((EditText) findViewById(R.id.registerLoginInput)).setText(registerLogin);
                ((EditText) findViewById(R.id.registerEmailInput)).setText(registerEmail);
                ((EditText) findViewById(R.id.registerPass1Input)).setText(registerPass1);
                ((EditText) findViewById(R.id.registerPass2Input)).setText(registerPass2);
                registerLayoutV();

            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
                setContentView(R.layout.activity_mainregister);
                final RelativeLayout rellay3 = (RelativeLayout) findViewById(R.id.rellay3);
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        rellay3.setVisibility(View.VISIBLE);

                    }
                };
                handler.postDelayed(runnable, 100);
                ((EditText) findViewById(R.id.registerLoginInput)).setText(registerLogin);
                ((EditText) findViewById(R.id.registerEmailInput)).setText(registerEmail);
                ((EditText) findViewById(R.id.registerPass1Input)).setText(registerPass1);
                ((EditText) findViewById(R.id.registerPass2Input)).setText(registerPass2);
                registerLayoutV();
            }
        }


    }

    public void loginLayoutV(){
        final Context appContext = getApplicationContext();
        final Button loginButton = (Button) findViewById(R.id.loginButton);
        final EditText loginInput = (EditText) findViewById(R.id.loginInput);
        final EditText passwordInput = (EditText) findViewById(R.id.passwordInput);
        final Button registerButton =(Button) findViewById(R.id.register);
        final Button infoButton = (Button) findViewById(R.id.info);
        final LinearLayout linLay = (LinearLayout) findViewById(R.id.linlay1);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_mainregister);
                setLayout("register_layout");
                layoutId = 2;
                final RelativeLayout rellay3;
                rellay3 = (RelativeLayout) findViewById(R.id.rellay3);
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        rellay3.setVisibility(View.VISIBLE);

                    }
                };

                handler.postDelayed(runnable, 1000); // -----animacja loga
                registerLayoutV();
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CarmasSettings.showAlertDialog(MainActivity.this,"Informacja","CARMAS jest projektem mającym na celu wspieranie kierowcy podczas jazdy samochodem. Na projekt składa się aplikacja mobilna współpracująca z układem elektronicznym. Głównymi funkcjami projektu są: czujniki parkowania, wideorejestrator oraz detekcja wypadku. Najważniejszą cechą projektu jest wszechstronność - działa on na każdym telefonie z systemem Android oraz instalacja jest możliwa w każdym aucie. ","ok");
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bgpressed));
                buttonHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loginButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bg));
                    }
                },150);
                linLay.requestFocus();
                String login = loginInput.getText().toString();
                String password = passwordInput.getText().toString();

                Handler handler = new Handler();
                if(login.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Wypełnij wszystkie pola ",Toast.LENGTH_SHORT).show();
                    loginInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bgred));
                    loginInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View view, boolean hasFocus) {
                            if (hasFocus) {
                                loginInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bg));
                            }
                        }
                    });
                    passwordInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bgred));
                    passwordInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View view, boolean hasFocus) {
                            if (hasFocus) {
                                passwordInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bg));
                            }
                        }
                    });
                }else{

                    AsyncLogin asyncLogin = new AsyncLogin(getApplicationContext(),"login");
                    asyncLogin.execute(login,password);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getCurrentFocus().clearFocus();
                            if(CarmasSettings.tag.contains("Zły login lub hasło")){

                                Log.e("asdasd",CarmasSettings.tag);
                                loginInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bgred));
                                loginInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                    @Override
                                    public void onFocusChange(View view, boolean hasFocus) {
                                        if (hasFocus) {
                                            loginInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bg));
                                        }
                                    }
                                });
                                passwordInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bgred));
                                passwordInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                    @Override
                                    public void onFocusChange(View view, boolean hasFocus) {
                                        if (hasFocus) {
                                            passwordInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bg));
                                        }
                                    }
                                });
                            }else if(CarmasSettings.tag.contains("Problem z połączeniem")) {
                                //
                                //
                                //              PROBLEM Z POŁĄCZENIEM
                                //
                                //
                                //
                            }else{
                                loginInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bggreen));
                                passwordInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bggreen));
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        changeLayout(appContext,CarmasSettings.tag,"logged");
                                    }
                                }, 400);

                            }

                        }
                    }, 500);

                }

            }
        });


    }

    public void registerLayoutV(){
        final Context appContext = getApplicationContext();


        final LinearLayout linLayReg = (LinearLayout) findViewById(R.id.linlay3);
        final Button registerButton = (Button) findViewById(R.id.registerButton);
        final EditText registerLoginInput = (EditText) findViewById(R.id.registerLoginInput);
        final EditText registerEmailInput = (EditText) findViewById(R.id.registerEmailInput);
        final EditText registerPass1Input = (EditText) findViewById(R.id.registerPass1Input);
        final EditText registerPass2Input = (EditText) findViewById(R.id.registerPass2Input);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bgpressed));
                buttonHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        registerButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bg));
                    }
                },150);

                linLayReg.requestFocus();
                String loginInput = registerLoginInput.getText().toString();
                String emailInput = registerEmailInput.getText().toString();
                String pass1Input = registerPass1Input.getText().toString();
                String pass2Input = registerPass2Input.getText().toString();
                if(loginInput.isEmpty()||emailInput.isEmpty()||pass1Input.isEmpty()||pass2Input.isEmpty()){
                    if (loginInput.isEmpty()) {
                        registerLoginInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bgred));
                        registerLoginInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean hasFocus) {
                                if (hasFocus) {
                                    registerLoginInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bg));
                                }
                            }
                        });
                    }
                    if (emailInput.isEmpty()) {
                        registerEmailInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bgred));
                        registerEmailInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean hasFocus) {
                                if (hasFocus) {
                                    registerEmailInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bg));
                                }
                            }
                        });
                    }
                    if (pass1Input.isEmpty()) {
                        registerPass1Input.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bgred));
                        registerPass1Input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean hasFocus) {
                                if (hasFocus) {
                                    registerPass1Input.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bg));
                                }
                            }
                        });
                    }
                    if (pass2Input.isEmpty()) {
                        registerPass2Input.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bgred));
                        registerPass2Input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean hasFocus) {
                                if (hasFocus) {
                                    registerPass2Input.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bg));
                                }
                            }
                        });
                    }
                    Toast.makeText(appContext,  "wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
                }else{
                    if(registerPass1Input.getText().toString().equals(registerPass2Input.getText().toString())){
                        AsyncLogin asyncRegister = new AsyncLogin(getApplicationContext(),"register");
                        asyncRegister.execute(registerLoginInput.getText().toString(),registerEmailInput.getText().toString(),registerPass1Input.getText().toString());
                       // Log.e("asdasd","asyncRegister started");
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(CarmasSettings.tag.contains("Zarejestrowano")){
                                    registerLoginInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bggreen));
                                    registerEmailInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bggreen));
                                    registerPass1Input.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bggreen));
                                    registerPass2Input.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bggreen));
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            changeLayout(appContext,CarmasSettings.tag,"logged");
                                        }
                                    }, 400);

                                }else if(CarmasSettings.tag.contains("E-mail")){
                                    registerEmailInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bgred));
                                    registerEmailInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                        @Override
                                        public void onFocusChange(View view, boolean hasFocus) {
                                            if (hasFocus) {
                                                registerEmailInput.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bg));
                                            }
                                        }
                                    });
                                }
                            }
                        }, 300);

                    }else{
                        Toast.makeText(getApplicationContext(),  "hasła nie są takie same", Toast.LENGTH_SHORT).show();

                        registerPass2Input.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bgred));
                        registerPass2Input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean hasFocus) {
                                if (hasFocus) {
                                    registerPass2Input.setBackground(ContextCompat.getDrawable(appContext, R.drawable.et_bg));
                                }
                            }
                        });
                    }
                }

            }
        });
    }

    private GPSManager gpsManager = null;
    private double lat,lon,speed;
    private double convertSpeed(double speed) {
        return ((speed * 3600) * 0.001);
    }

    private double roundDecimal(double value, final int decimalPlace) {
        BigDecimal bd = new BigDecimal(value);

        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        value = bd.doubleValue();

        return value;
    }

    private void initialiseElements() {
        gpsManager = new GPSManager(getApplicationContext());
        gpsManager.startListening(getApplicationContext());
        gpsManager.setGPSCallback(this);
    }

    @Override
    public void onGPSUpdate(Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();
        speed = location.getSpeed();
        Double speedKM=(roundDecimal(convertSpeed(speed), 2));
        Log.e("gpsListener","lat:"+lat+" lon:"+lon+" speed:"+speedKM);
        //EmergencySMS.saveLocation(getApplicationContext(),CarmasSettings.saveRouteFile,lat,lon,speedKM);

    }




}
