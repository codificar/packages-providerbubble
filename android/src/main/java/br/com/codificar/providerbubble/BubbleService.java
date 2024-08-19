package br.com.codificar.providerbubble;

// import br.com.codificar.providerbubble.R;
//import br.com.codificar.providerbubble.MainActivity;

import android.R;

import android.os.IBinder;
import android.os.PowerManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;

import androidx.annotation.Nullable;

import androidx.core.app.NotificationCompat;

import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.DialogInterface;

import android.provider.Settings;

import android.util.Log;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import android.widget.ImageView;

import android.net.Uri;

import android.media.MediaPlayer;

import android.content.pm.ServiceInfo;

public class BubbleService extends Service {
    public static final String APP_NAME = "Prestação de Serviços"; // TODO isso deve vir externo
    public static final String REACT_CLASS = "RNProviderBubble";
    private NotificationManager mNotificationManager;

    public static final String FOREGROUND = "br.com.codificar.providerbubble.BubbleService";
    private static int NOTIFICATION_ID = 3313;
    private static int PERMISSION_OVERLAY_SCREEN = 78;
    private WindowManager windowManager;
    private static ImageView chatHead;
    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin;
    private Point szWindow = new Point();
    private boolean isLeft = true;
    private String sMsg = "";
    private static final String TAG = "Bubble";
    private static boolean onRide = false ;
    public static Activity currentActivity ; // TODO isso aqui funcionando.

    Handler timeControlHandler = new Handler();
    Runnable timeControlRunnable = new Runnable() {
        @Override
        public void run() {
            stopRequest(0);
        }
    };
    private static Context ctx;
    private PowerManager.WakeLock mWakeLock = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void prepareStart(Intent intent, int flags, int startId) {
        try {
            if (intent != null) {
                Bundle bd = intent.getExtras();

                if (bd != null) {
                    sMsg = bd.getString("EXTRA_MSG");
                    if (bd.containsKey("startRequest")) {
                        startRequest(bd.getLong("startRequest"));
                    }
                    if (bd.containsKey("stopRequest")) {
                        long delay = bd.getLong("stopRequest");
                        Log.i(TAG, "stopRequest - Delay: "+delay);
                        stopRequest(delay);
                    }
                    if(bd.containsKey("stopTimeControl")){
                        stopTimeControl();
                    }
                }
                if (sMsg != null && sMsg.length() > 0) {
                    if (startId == Service.START_STICKY) {
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                showMsg(sMsg);
                            }
                        }, 300);

                    } else {
                        showMsg(sMsg);
                    }

                }
            }
            if (startId == START_STICKY) {
                handleStart();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Notification prepareStartForeground(Intent intent, int flags, int startId) {

        prepareStart(intent, flags, startId);

        Context context = getApplicationContext();
        String title = context.getResources().getString(context.getResources().getIdentifier("app_name", "string", context.getPackageName()));
        String text = context.getResources().getString(context.getResources().getIdentifier("display_over_other_apps", "string", context.getPackageName()));
        String ticker = context.getResources().getString(context.getResources().getIdentifier("display_over_other_apps_message", "string", context.getPackageName()));

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("provider_channel",
                    "Provider channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Provider channel");
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "provider_channel");

        builder
            .setSmallIcon(context.getResources().getIdentifier("ic_launcher", "mipmap", context.getPackageName())) //TODO MIPMAP
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), context.getResources().getIdentifier("ic_launcher", "mipmap", context.getPackageName()))) //TODO MIPMAP
            .setContentTitle(text)
            .setContentText(ticker)
            .setTicker(ticker)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(ticker))
            .setWhen(System.currentTimeMillis());

        Intent startIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getApplicationContext().getPackageName()));

        // Aqui a mudança para usar FLAG_IMMUTABLE
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1000, startIntent, PendingIntent.FLAG_IMMUTABLE);
        
        builder.setContentIntent(contentIntent);
        return builder.build();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        activateWakeLock();
        if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(getApplicationContext())) {
            RNProviderBubbleModule.emitShowOverAppsAlert();
            return;
        }
    }

    private void activateWakeLock(){
        PowerManager pm = (PowerManager) getApplicationContext()
                .getSystemService(Context.POWER_SERVICE);

        if (pm != null) {
            mWakeLock = pm.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                            | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE, "WakeLock");
        }
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    public void openOverAppsAlert() {
        try {
            android.app.AlertDialog.Builder showBubbleDialog;
            Context context = getApplicationContext();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                showBubbleDialog = new android.app.AlertDialog.Builder(getApplicationContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
            } else {
                showBubbleDialog = new android.app.AlertDialog.Builder(getApplicationContext());
            }

            showBubbleDialog.setTitle(context.getResources().getString(context.getResources().getIdentifier("over_other_apps_title", "string", context.getPackageName())));
            showBubbleDialog.setMessage( context.getResources().getString(context.getResources().getIdentifier("over_other_apps_message", "string", context.getPackageName())));

            showBubbleDialog.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse( "package: " + context.getPackageName() )); // TODO esse treco aqui
                        getApplicationContext().startActivity(intent);
                    }
                });

            showBubbleDialog.setNegativeButton("no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
                });

            showBubbleDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleStart() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams paramRemove = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        paramRemove.gravity = Gravity.TOP | Gravity.LEFT;

        chatHead = new ImageView(getApplicationContext());

        setImageVector();


        windowManager.getDefaultDisplay().getSize(szWindow);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        if ((android.os.Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(getBaseContext())) || (android.os.Build.VERSION.SDK_INT < 23)) {
            if(windowManager != null) windowManager.addView(chatHead, params);
        }

        chatHead.setOnTouchListener(new View.OnTouchListener() {
            long time_start = 0, time_end = 0;
            boolean isLongclick = false, inBounded = false;
            int remove_img_width = 0, remove_img_height = 0;

            Handler handler_longClick = new Handler();

            Runnable runnable_longClick = new Runnable() {

                @Override
                public void run() {
                    Log.d(TAG, "Into runnable_longClick");

                    isLongclick = true;
                    chathead_longclick();
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) chatHead.getLayoutParams();

                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();
                int x_cord_Destination, y_cord_Destination;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        time_start = System.currentTimeMillis();

                        x_init_cord = x_cord;
                        y_init_cord = y_cord;

                        x_init_margin = layoutParams.x;
                        y_init_margin = layoutParams.y;

                        myHandler.removeCallbacks(myRunnable);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;

                        if (isLongclick) {
                            int x_bound_left = szWindow.x / 2 - (int) (remove_img_width * 1.5);
                            int x_bound_right = szWindow.x / 2 + (int) (remove_img_width * 1.5);
                            int y_bound_top = szWindow.y - (int) (remove_img_height * 1.5);

                            if ((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top) {
                                inBounded = true;
                                windowManager.updateViewLayout(chatHead, layoutParams);
                                break;
                            } else {
                                inBounded = false;
                            }

                        }

                        layoutParams.x = x_cord_Destination;
                        layoutParams.y = y_cord_Destination;

                        windowManager.updateViewLayout(chatHead, layoutParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        isLongclick = false;
                        handler_longClick.removeCallbacks(runnable_longClick);

                        if (inBounded) {
                            stopService(new Intent(BubbleService.this, BubbleService.class));
                            inBounded = false;
                            break;
                        }

                        int x_diff = x_cord - x_init_cord;
                        int y_diff = y_cord - y_init_cord;

                        if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                            time_end = System.currentTimeMillis();
                            if ((time_end - time_start) < 300) {
                                chathead_click();
                            }
                        }

                        y_cord_Destination = y_init_margin + y_diff;

                        int BarHeight = getStatusBarHeight();
                        if (y_cord_Destination < 0) {
                            y_cord_Destination = 0;
                        } else if (y_cord_Destination + (chatHead.getHeight() + BarHeight) > szWindow.y) {
                            y_cord_Destination = szWindow.y - (chatHead.getHeight() + BarHeight);
                        }
                        layoutParams.y = y_cord_Destination;

                        inBounded = false;
                        resetPosition(x_cord);

                        break;
                    default:
                        Log.d(TAG, TAG + ".setOnTouchListener  -> event.getAction() : default");
                        break;
                }
                return true;
            }
        });

        WindowManager.LayoutParams paramsTxt = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        paramsTxt.gravity = Gravity.TOP | Gravity.LEFT;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try{
            if(windowManager == null) {
                windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            }

            if((windowManager != null) && (chatHead != null) && (newConfig != null) && (szWindow != null)) {
                windowManager.getDefaultDisplay().getSize(szWindow);

                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) chatHead.getLayoutParams();
                if(layoutParams != null){
                    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        if (layoutParams.y + (chatHead.getHeight() + getStatusBarHeight()) > szWindow.y) {
                            layoutParams.y = szWindow.y - (chatHead.getHeight() + getStatusBarHeight());
                            windowManager.updateViewLayout(chatHead, layoutParams);
                        }

                        if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                            resetPosition(szWindow.x);
                        }
                    }
                    else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        if (layoutParams.x > szWindow.x) {
                            resetPosition(szWindow.x);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void resetPosition(int x_cord_now) {
        if (x_cord_now <= szWindow.x / 2) {
            isLeft = true;
            moveToLeft(x_cord_now);

        } else {
            isLeft = false;
            moveToRight(x_cord_now);

        }

    }

    private void moveToLeft(final int x_cord_now) {
        final int x = szWindow.x - x_cord_now;

        new CountDownTimer(50, 5) {
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) chatHead.getLayoutParams();

            public void onTick(long t) {
                try {
                    long step = (500 - t) / 5;
                    if(windowManager != null)
                        windowManager.updateViewLayout(chatHead, mParams);
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
            }

            public void onFinish() {
                try {
                    mParams.x = 0;
                    if(windowManager != null)
                        windowManager.updateViewLayout(chatHead, mParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void moveToRight(final int x_cord_now) {
        new CountDownTimer(50, 5) {
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) chatHead.getLayoutParams();

            public void onTick(long t) {
                try {
                    long step = (500 - t) / 5;
                    if(windowManager != null)
                        windowManager.updateViewLayout(chatHead, mParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void onFinish() {
                try {
                    mParams.x = szWindow.x - chatHead.getWidth();
                    if(windowManager != null)
                        windowManager.updateViewLayout(chatHead, mParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

//    private double bounceValue(long step, long scale) {
//        return scale * Math.exp(-0.055 * step) * Math.cos(0.08 * step);
//    }

    private int getStatusBarHeight() {
        return (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
    }

    /*
    * Go to activity with package name on application context.
    */
    private void chathead_click() {

        try {
            // Verifica se a versão mínima é atendida
            if (minVersion()) {
                // Cria um Intent para a atividade atual
                Intent mainIntent = new Intent(this, BubbleService.currentActivity.getClass());
                
                // Atualiza o PendingIntent para usar FLAG_IMMUTABLE
                PendingIntent mainPIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                        mainIntent, PendingIntent.FLAG_IMMUTABLE);
                
                // Configura o Intent para abrir a atividade principal
                mainIntent.setAction("android.intent.action.MAIN");
                mainIntent.addCategory("android.intent.category.LAUNCHER");
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                // Envia o PendingIntent
                mainPIntent.send();

            } else {
                // Caso a versão mínima não seja atendida, inicia a atividade de forma padrão
                Intent intent = new Intent(this, BubbleService.currentActivity.getClass());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean minVersion(){
        Log.e(TAG, "Device: "+ Build.VERSION.SDK_INT);
        Log.e(TAG, "Min: "+ Build.VERSION_CODES.LOLLIPOP);
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    private void chathead_longclick() {
        Log.d(TAG, "Into Bubble.chathead_longclick() ");
    }

    private void showMsg(String sMsg) {
        if (chatHead != null) {
            Log.d(TAG, TAG + ".showMsg -> sMsg=" + sMsg);
            myHandler.removeCallbacks(myRunnable);
            setImageVector();
            myHandler.postDelayed(myRunnable, 4000);
        }
    }

    public void startRequest(final long durationSecs) {
        long durationMillis = durationSecs*1000;
        setImageVector();

        startTimeControl(durationMillis);
        chathead_click();
    }

    public void startTimeControl(long durationMillis){
        //Stop Control Time:
        stopTimeControl();
        //Start control time:
        timeControlHandler.postDelayed(timeControlRunnable, durationMillis+1500);
    }

    public void stopTimeControl(){
        //Stop Control Time:
        timeControlHandler.removeCallbacks(timeControlRunnable);
    }

    public void stopRequest(long delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setImageVector();
            }
        }, delay);
        Log.i(TAG, "old icon");
    }

    public void setImageVector(){
        Context context = getApplicationContext();
        // int imageVector = R.drawable.app_bubble_default;
        int imageVector = context.getResources().getIdentifier("app_bubble_default", "drawable", context.getPackageName());

        if(imageVector == 0){
            imageVector = context.getResources().getIdentifier("bubble_default", "drawable", context.getPackageName());
            if(imageVector == 0){
                imageVector = context.getResources().getIdentifier("library_bubble_default", "drawable", context.getPackageName());
                Log.d(TAG,"bubble_default images not found , using library default id == 0");
            }
        }

        // int imageVector = R.drawable.app_bubble_services;
        if(onRide){
            imageVector = context.getResources().getIdentifier("app_bubble_service", "drawable", context.getPackageName());
            if(imageVector == 0){
                imageVector = context.getResources().getIdentifier("bubble_service", "drawable", context.getPackageName());
                if(imageVector == 0){
                    imageVector = context.getResources().getIdentifier("library_bubble_service", "drawable", context.getPackageName());
                    Log.d(TAG,"bubble_service images not found , using library service id == 0");
                }
            }
        }

        if(chatHead!=null && imageVector!=0) {
            chatHead.setImageResource(imageVector);
        }
    }

    Handler myHandler = new Handler();
    Runnable myRunnable = new Runnable() {

        @Override
        public void run() {
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ((Build.VERSION.SDK_INT < 23) ||
            (Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(getApplicationContext()))) {
            
            // Verifica se o Android é Q (10) ou superior para especificar o tipo de serviço em primeiro plano
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    prepareStartForeground(intent, flags, startId),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST  // Altere este tipo conforme necessário
                );
            } else {
                startForeground(
                    NOTIFICATION_ID,
                    prepareStartForeground(intent, flags, startId)
                );
            }
        } else {
            prepareStart(intent, flags, startId);
        }

        if (startId == START_STICKY) {
            return super.onStartCommand(intent, flags, startId);
        } else {
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // force get windowManager
        if(windowManager == null) {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }

        if(mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        if ((android.os.Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(getBaseContext())) || (android.os.Build.VERSION.SDK_INT < 23)) {
            try{

                if(windowManager != null) {
                    //windowManager.removeViewImmediate();
                    windowManager.removeViewImmediate(chatHead);
                }
            } catch (Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public static void stopRequestBubble(Context c, long delay) {
        try {
            if(c != null) {
                onRide = false;
                Log.i(TAG, "Stop Request Bubble");
                Intent intent = new Intent(c, BubbleService.class);
                intent.putExtra("stopRequest", delay);
                c.startService(intent);
                Log.i(TAG, "Stopped Request Bubble");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startRequestBubble(Context c, long duration) {
        try {
            if(c != null) {
                onRide = true;
                Log.i(TAG, "Start Request Bubble");
                Intent intent = new Intent(c, BubbleService.class);
                intent.putExtra("startRequest", duration);
                c.startService(intent);
                Log.i(TAG, "Started Request Bubble");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
