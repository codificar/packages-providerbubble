package br.com.codificar.providerbubble;

import br.com.codificar.providerbubble.R;
import br.com.codificar.providerbubble.MainActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.graphics.BitmapFactory;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.content.DialogInterface;
import android.net.Uri;

import android.media.MediaPlayer;

public class BubbleService extends Service {
    public static final String APP_NAME = "Prestação de Serviços"; // TODO isso deve vir externo
    public static final String REACT_CLASS = "BubbleService";
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
    }

    private Notification prepareStartForeground(Intent intent, int flags, int startId) {

        prepareStart(intent, flags, startId);

        String title = getString(R.string.app_name);
        String text = getString(R.string.display_over_other_apps);
        String ticker = getString(R.string.display_over_other_apps_message);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel("provider_channel",
                "Provider channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Provider channel");
            if(notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "provider_channel");

        builder
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
            .setContentTitle(text)
            .setContentText(ticker)
            .setTicker(ticker)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(ticker))
            .setWhen(System.currentTimeMillis());

        Intent startIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getApplicationContext().getPackageName()));
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1000, startIntent, 0);
        builder.setContentIntent(contentIntent);
        return builder.build();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        activateWakeLock();
		    if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(getApplicationContext())) {
            BubbleServiceBridgeModule.emitShowOverAppsAlert();
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
        android.app.AlertDialog.Builder showBubbleDialog;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            showBubbleDialog = new android.app.AlertDialog.Builder(getApplicationContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            showBubbleDialog = new android.app.AlertDialog.Builder(getApplicationContext());
        }

        showBubbleDialog.setTitle(getString(R.string.over_other_apps_title));
        showBubbleDialog.setMessage(getString(R.string.over_other_apps_message));

        showBubbleDialog.setPositiveButton("yes",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package: com.br.motoristaprivado.prestador"));
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

        chatHead = new ImageView(this);

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

        new CountDownTimer(500, 5) {
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) chatHead.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;
                if(windowManager != null)
                    windowManager.updateViewLayout(chatHead, mParams);
            }

            public void onFinish() {
                mParams.x = 0;
                if(windowManager != null)
                windowManager.updateViewLayout(chatHead, mParams);
            }
        }.start();
    }

    private void moveToRight(final int x_cord_now) {
        new CountDownTimer(500, 5) {
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) chatHead.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;
                if(windowManager != null)
                    windowManager.updateViewLayout(chatHead, mParams);
            }

            public void onFinish() {
                mParams.x = szWindow.x - chatHead.getWidth();
                if(windowManager != null)
                    windowManager.updateViewLayout(chatHead, mParams);
            }
        }.start();
    }

    private double bounceValue(long step, long scale) {
        return scale * Math.exp(-0.055 * step) * Math.cos(0.08 * step);
    }

    private int getStatusBarHeight() {
        return (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
    }

    private void chathead_click() {
        /*
         * If Activity is not visible, bubble is clickable.
         */
        if(!MainActivity.isActivityVisible()){
            goToActivity(MainActivity.class);
        }
    }

    private void chathead_longclick() {
        Log.d(TAG, "Into Bubble.chathead_longclick() ");
    }

    private void goToActivity(Class c) {
        if (minVersion())  {
            Intent mainIntent = new Intent(this, c);
            PendingIntent mainPIntent = PendingIntent.getActivity(this, 0,
                    mainIntent, 0);
            mainIntent.setAction("android.intent.action.MAIN");
            mainIntent.addCategory("android.intent.category.LAUNCHER");
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            try {
                mainPIntent.send();
            } catch (PendingIntent.CanceledException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        else{
            Intent intent = new Intent(this, c);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
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
        int imageVector = R.drawable.app_bubble_default;

        if(onRide)
            imageVector =  R.drawable.app_bubble_service;

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
        if((android.os.Build.VERSION.SDK_INT < 23) ||
            (android.os.Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(getApplicationContext()))){
                startForeground(NOTIFICATION_ID, prepareStartForeground(intent, flags, startId)
            );
        }
        else {
            prepareStart(intent, flags, startId);
        }

        if (startId == START_STICKY) {
            // handleStart();
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
                if(windowManager != null) windowManager.removeView(chatHead);
            } catch (Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public static void stopRequestBubble(Context c, long delay) {
        if(c != null) {
            onRide = false;
            Log.i(TAG, "Stop Request Bubble");
            Intent intent = new Intent(c, BubbleService.class);
            intent.putExtra("stopRequest", delay);
            c.startService(intent);
            Log.i(TAG, "Stopped Request Bubble");
        }
    }

    public static void startRequestBubble(Context c, long duration) {
        if(c != null) {
            onRide = true;
            Log.i(TAG, "Start Request Bubble");
            Intent intent = new Intent(c, BubbleService.class);
            intent.putExtra("startRequest", duration);
            c.startService(intent);
            Log.i(TAG, "Started Request Bubble");
        }
    }

    public static void startServiceBubble(Context c) {
        if(c != null) {
            ctx = c;
            Log.i(TAG, "Start Service Bubble");
            Intent intent = new Intent(c, BubbleService.class);
            c.startService(intent);
            Log.i(TAG, "Started Service Bubble");
        }
    }

    public static void stopServiceBubble(Context c) {
        if(c != null) {
            onRide = false;
            Log.i(TAG, "Stop Service Bubble");
            c.stopService(new Intent(c, BubbleService.class));
            Log.i(TAG, "Stopped Service Bubble");
        }
    }

    public static void stopTimeControlBubble(Context c) {
        if(c != null) {
            Log.i(TAG, "Stop Time Control Bubble");
            Intent intent = new Intent(c, BubbleService.class);
            intent.putExtra("stopTimeControl", true);
            c.startService(intent);
            Log.i(TAG, "Stopped Time Control Bubble");
        }
    }

    public static boolean minVersion(){
        Log.e(TAG, "Device: "+ Build.VERSION.SDK_INT);
        Log.e(TAG, "Min: "+ Build.VERSION_CODES.LOLLIPOP);
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }
}
