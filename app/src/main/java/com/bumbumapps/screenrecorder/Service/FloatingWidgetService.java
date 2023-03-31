package com.bumbumapps.screenrecorder.Service;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumbumapps.screenrecorder.Activities.ActivityHome;
import com.bumbumapps.screenrecorder.Activities.ActivityMediaProjectionPermission;
import com.bumbumapps.screenrecorder.R;
import com.bumbumapps.screenrecorder.Utills.Constance;


public class FloatingWidgetService extends Service {

    private WindowManager mWindowManager;
    private Point szWindow = new Point();
    private View removeFloatingWidgetView;
    private View mFloatingWidgetView ,collapse_view, expanded_container, ll_screenshot,ll_homeview;
    // CircleMenuView circularmenuview;

    private ImageView remove_image_view;
    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin;
    private boolean isLeft = true;
    boolean isVisible = false;
    ImageView iv_screenshot, iv_record, iv_home,iv_collapsedview;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //init WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        getWindowManagerDefaultDisplay();

        //Init LayoutInflater
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        addRemoveView(inflater);
        addFloatingWidgetView(inflater);
        // addFloatingWidgetView();
        //implementClickListeners();
        implementTouchListenerToFloatingWidgetView();
        //  onFloatingWidgetClick();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        iv_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FloatingWidgetService.this, ActivityHome.class);

                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               startActivity(i);
                clickEventVisiblity();
              // Toast.makeText(getApplicationContext(), "new Home click", Toast.LENGTH_LONG).show();
            }
        });
        iv_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recordIntent = new Intent(FloatingWidgetService.this, ActivityHome.class);
                recordIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                recordIntent.setAction("record_intent");
                startActivity(recordIntent);

              /*  //Pause btn clicked
                Intent pauseIntent = new Intent(FloatingWidgetService.this, NotificationReceiver.class);
                pauseIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                pauseIntent.setAction("Pause_ACTION");
                sendBroadcast(pauseIntent);*/
                clickEventVisiblity();
               // Toast.makeText(getApplicationContext(), "second click", Toast.LENGTH_LONG).show();
            }
        });
        iv_screenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ScrennshotIntent = new Intent(FloatingWidgetService.this, ActivityMediaProjectionPermission.class);
                ScrennshotIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(ScrennshotIntent);
                clickEventVisiblity();
               // Toast.makeText(getApplicationContext(), "second click", Toast.LENGTH_LONG).show();
            }
        });

        //return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    /*  Add Remove View to Window Manager  */
    private View addRemoveView(LayoutInflater inflater) {
        //Inflate the removing view layout we created
        removeFloatingWidgetView = inflater.inflate(R.layout.remove_floating_widget_layout, null);

        //Add the view to the window.
        WindowManager.LayoutParams paramRemove;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            paramRemove = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            paramRemove = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        //Specify the view position
        paramRemove.gravity = Gravity.TOP | Gravity.LEFT;

        //Initially the Removing widget view is not visible, so set visibility to GONE
        removeFloatingWidgetView.setVisibility(View.GONE);
        remove_image_view = (ImageView) removeFloatingWidgetView.findViewById(R.id.remove_img);

        //Add the view to the window
        mWindowManager.addView(removeFloatingWidgetView, paramRemove);
        return remove_image_view;
    }

    public void clickEventVisiblity()
    {
        expanded_container.setVisibility(View.GONE);
        ll_screenshot.setVisibility(View.GONE);
        ll_homeview.setVisibility(View.GONE);
        iv_collapsedview.setImageResource(R.drawable.ic_recode);

    }

    //  Add Floating Widget View to Window Manager
    private void addFloatingWidgetView(LayoutInflater inflater) {
        //Inflate the floating view layout we created
        mFloatingWidgetView = inflater.inflate(R.layout.floating_widget_layout, null);

        //Add the view to the window.
        WindowManager.LayoutParams params;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }


        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;

        //Initially view will be added to top-left corner, you change x-y coordinates according to your need
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager.addView(mFloatingWidgetView, params);

        //find id of collapsed view layout
        collapse_view = mFloatingWidgetView.findViewById(R.id.collapse_view);

        //find id of the expanded view layout
        expanded_container = mFloatingWidgetView.findViewById(R.id.expanded_container);
        ll_screenshot = mFloatingWidgetView.findViewById(R.id.ll_screenshot);
        ll_homeview = mFloatingWidgetView.findViewById(R.id.ll_homeview);
        iv_screenshot = mFloatingWidgetView.findViewById(R.id.iv_screenshot);
        iv_record = mFloatingWidgetView.findViewById(R.id.iv_record);
        iv_home = mFloatingWidgetView.findViewById(R.id.iv_home);
        iv_collapsedview = mFloatingWidgetView.findViewById(R.id.iv_collapsedview);
    }

    /*  private void addFloatingWidgetView()
      {
          // Set up the large red button on the center right side
          // With custom button and content sizes and margins
          int redActionButtonSize = getResources().getDimensionPixelSize(R.dimen.red_action_button_size);
          int redActionButtonMargin = getResources().getDimensionPixelOffset(R.dimen.action_button_margin);
          int redActionButtonContentSize = getResources().getDimensionPixelSize(R.dimen.red_action_button_content_size);
          int redActionButtonContentMargin = getResources().getDimensionPixelSize(R.dimen.red_action_button_content_margin);
          int redActionMenuRadius = getResources().getDimensionPixelSize(R.dimen.red_action_menu_radius);
          int blueSubActionButtonSize = getResources().getDimensionPixelSize(R.dimen.blue_sub_action_button_size);
          int blueSubActionButtonContentMargin = getResources().getDimensionPixelSize(R.dimen.blue_sub_action_button_content_margin);
          ImageView icon = new ImageView(getApplicationContext()); // Create an icon
          // icon.setImageDrawable(getApplicationContext().getDrawable(R.drawable.icon));
          icon.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.icon));

          FloatingActionButton.LayoutParams starParams = new FloatingActionButton.LayoutParams(redActionButtonSize, redActionButtonSize);
          starParams.setMargins(redActionButtonMargin,
                  redActionButtonMargin,
                  redActionButtonMargin,
                  redActionButtonMargin);
          icon.setLayoutParams(starParams);

          FloatingActionButton.LayoutParams fabIconStarParams = new FloatingActionButton.LayoutParams(redActionButtonContentSize, redActionButtonContentSize);
          fabIconStarParams.setMargins(redActionButtonContentMargin,
                  redActionButtonContentMargin,
                  redActionButtonContentMargin,
                  redActionButtonContentMargin);

          FloatingActionButton actionButton = new FloatingActionButton.Builder(getActivity())
                  .setContentView(icon, fabIconStarParams)
                  .setPosition(FloatingActionButton.POSITION_LEFT_CENTER)
                  .build();
          // Set up customized SubActionButtons for the right center menu


          SubActionButton.Builder itemBuilder = new SubActionButton.Builder(getActivity());
          itemBuilder.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_action_blue_selector));

          FrameLayout.LayoutParams blueContentParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
          blueContentParams.setMargins(blueSubActionButtonContentMargin,
                  blueSubActionButtonContentMargin,
                  blueSubActionButtonContentMargin,
                  blueSubActionButtonContentMargin);
          itemBuilder.setLayoutParams(blueContentParams);
          // Set custom layout params
          FrameLayout.LayoutParams blueParams = new FrameLayout.LayoutParams(blueSubActionButtonSize, blueSubActionButtonSize);
          itemBuilder.setLayoutParams(blueParams);
          // repeat many times:
          ImageView itemIcon = new ImageView(getApplicationContext());
          itemIcon.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_home));
          itemIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
          SubActionButton button1 = itemBuilder.setContentView(itemIcon).build();


          ImageView itemIcon2 = new ImageView(getApplicationContext());
          itemIcon2.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_home));
          itemIcon2.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
          SubActionButton button2 = itemBuilder.setContentView(itemIcon2).build();

          ImageView itemIcon3 = new ImageView(getApplicationContext());
          itemIcon3.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_home));
          itemIcon3.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
          SubActionButton button3 = itemBuilder.setContentView(itemIcon3).build();

          ImageView itemIcon4 = new ImageView(getApplicationContext());
          itemIcon4.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_home));
          itemIcon4.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
          SubActionButton button4 = itemBuilder.setContentView(itemIcon4).build();


          FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(getActivity())
                  .addSubActionView(itemBuilder.setContentView(button1, blueContentParams).build())
                  .addSubActionView(itemBuilder.setContentView(button2, blueContentParams).build())
                  .addSubActionView(itemBuilder.setContentView(button3, blueContentParams).build())
                  .addSubActionView(itemBuilder.setContentView(button4, blueContentParams).build())
                  .setRadius(redActionMenuRadius)
                  .setStartAngle(70)
                  .setEndAngle(-70)
                  .attachTo(actionButton)
                  .build();
      }
  */
    private void getWindowManagerDefaultDisplay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
            mWindowManager.getDefaultDisplay().getSize(szWindow);
        else {
            int w = mWindowManager.getDefaultDisplay().getWidth();
            int h = mWindowManager.getDefaultDisplay().getHeight();
            szWindow.set(w, h);
        }
    }

    /*  Implement Touch Listener to Floating Widget Root View  */
    private void implementTouchListenerToFloatingWidgetView() {
        //Drag and move floating view using user's touch action.
        mFloatingWidgetView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {

            long time_start = 0, time_end = 0;

            boolean isLongClick = false;//variable to judge if user click long press
            boolean inBounded = false;//variable to judge if floating view is bounded to remove view
            int remove_img_width = 0, remove_img_height = 0;

            Handler handler_longClick = new Handler();
            Runnable runnable_longClick = new Runnable() {
                @Override
                public void run() {
                    //On Floating Widget Long Click

                    //Set isLongClick as true
                    isLongClick = true;

                    //Set remove widget view visibility to VISIBLE
                    removeFloatingWidgetView.setVisibility(View.VISIBLE);

                    onFloatingWidgetLongClick();
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //Get Floating widget view params
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

                //get the touch location coordinates
                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();

                int x_cord_Destination, y_cord_Destination;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        time_start = System.currentTimeMillis();

                        handler_longClick.postDelayed(runnable_longClick, 600);

                        remove_img_width = remove_image_view.getLayoutParams().width;
                        remove_img_height = remove_image_view.getLayoutParams().height;

                        x_init_cord = x_cord;
                        y_init_cord = y_cord;

                        //remember the initial position.
                        x_init_margin = layoutParams.x;
                        y_init_margin = layoutParams.y;

                        return true;
                    case MotionEvent.ACTION_UP:
                        isLongClick = false;
                        removeFloatingWidgetView.setVisibility(View.GONE);
                        remove_image_view.getLayoutParams().height = remove_img_height;
                        remove_image_view.getLayoutParams().width = remove_img_width;
                        handler_longClick.removeCallbacks(runnable_longClick);

                        //If user drag and drop the floating widget view into remove view then stop the service
                        if (inBounded) {
                            stopSelf();
                            Constance.isfloatingswitchEnabled=false;
                            inBounded = false;
                            break;
                        }


                        //Get the difference between initial coordinate and current coordinate
                        int x_diff = x_cord - x_init_cord;
                        int y_diff = y_cord - y_init_cord;

                        //The check for x_diff <5 && y_diff< 5 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                            time_end = System.currentTimeMillis();

                            //Also check the difference between start time and end time should be less than 300ms
                            if (( time_end - time_start ) < 300) {
                                onFloatingWidgetClick();
                            }


                        }

                        y_cord_Destination = y_init_margin + y_diff;

                        int barHeight = getStatusBarHeight();
                        if (y_cord_Destination < 0) {
                            y_cord_Destination = 0;
                        } else if (y_cord_Destination + ( mFloatingWidgetView.getHeight() + barHeight ) > szWindow.y) {
                            y_cord_Destination = szWindow.y - ( mFloatingWidgetView.getHeight() + barHeight );
                        }

                        layoutParams.y = y_cord_Destination;

                        inBounded = false;

                        //reset position if user drags the floating view
                        resetPosition(x_cord);

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;

                        //If user long click the floating view, update remove view
                        if (isLongClick) {
                            int x_bound_left = szWindow.x / 2 - (int) ( remove_img_width * 1.5 );
                            int x_bound_right = szWindow.x / 2 + (int) ( remove_img_width * 1.5 );
                            int y_bound_top = szWindow.y - (int) ( remove_img_height * 1.5 );

                            //If Floating view comes under Remove View update Window Manager
                            if (( x_cord >= x_bound_left && x_cord <= x_bound_right ) && y_cord >= y_bound_top) {
                                inBounded = true;

                                int x_cord_remove = (int) ( ( szWindow.x - ( remove_img_height * 1.5 ) ) / 2 );
                                int y_cord_remove = (int) ( szWindow.y - ( ( remove_img_width * 1.5 ) + getStatusBarHeight() ) );

                                if (remove_image_view.getLayoutParams().height == remove_img_height) {
                                    remove_image_view.getLayoutParams().height = (int) ( remove_img_height * 1.5 );
                                    remove_image_view.getLayoutParams().width = (int) ( remove_img_width * 1.5 );

                                    WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeFloatingWidgetView.getLayoutParams();
                                    param_remove.x = x_cord_remove;
                                    param_remove.y = y_cord_remove;

                                    mWindowManager.updateViewLayout(removeFloatingWidgetView, param_remove);
                                }

                                layoutParams.x = x_cord_remove + ( Math.abs(removeFloatingWidgetView.getWidth() - mFloatingWidgetView.getWidth()) ) / 2;
                                layoutParams.y = y_cord_remove + ( Math.abs(removeFloatingWidgetView.getHeight() - mFloatingWidgetView.getHeight()) ) / 2;

                                //Update the layout with new X & Y coordinate
                                mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                                break;
                            } else {
                                //If Floating window gets out of the Remove view update Remove view again
                                inBounded = false;
                                remove_image_view.getLayoutParams().height = remove_img_height;
                                remove_image_view.getLayoutParams().width = remove_img_width;
                              //  onFloatingWidgetClick();
                                clickEventVisiblity();
                            }

                        }


                        layoutParams.x = x_cord_Destination;
                        layoutParams.y = y_cord_Destination;

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                        return true;
                }
                return false;
            }
        });
    }

    /*  private void implementClickListeners() {
          mFloatingWidgetView.findViewById(R.id.close_floating_view).setOnClickListener(this);
          mFloatingWidgetView.findViewById(R.id.close_expanded_view).setOnClickListener(this);
          mFloatingWidgetView.findViewById(R.id.open_activity_button).setOnClickListener(this);
      }


      @Override
      public void onClick(View v) {
          switch (v.getId()) {
              case R.id.close_floating_view:
                  //close the service and remove the from from the window
                  stopSelf();
                  break;
              case R.id.close_expanded_view:
                  collapsedView.setVisibility(View.VISIBLE);
                  expandedView.setVisibility(View.GONE);
                  break;
              case R.id.open_activity_button:
                  //open the activity and stop service
                  Intent intent = new Intent(FloatingWidgetService.this, MainActivity.class);
                  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  startActivity(intent);

                  //close the service and remove view from the view hierarchy
                  stopSelf();
                  break;
          }
      }
  */
    /*  on Floating Widget Long Click, increase the size of remove view as it look like taking focus */
    private void onFloatingWidgetLongClick() {
        //Get remove Floating view params
        WindowManager.LayoutParams removeParams = (WindowManager.LayoutParams) removeFloatingWidgetView.getLayoutParams();

        //get x and y coordinates of remove view
        int x_cord = ( szWindow.x - removeFloatingWidgetView.getWidth() ) / 2;
        int y_cord = szWindow.y - ( removeFloatingWidgetView.getHeight() + getStatusBarHeight() );


        removeParams.x = x_cord;
        removeParams.y = y_cord;

        //Update Remove view params
        mWindowManager.updateViewLayout(removeFloatingWidgetView, removeParams);
    }

    /*  Reset position of Floating Widget view on dragging  */
    private void resetPosition(int x_cord_now) {
        if (x_cord_now <= szWindow.x / 2) {
            isLeft = true;
            moveToLeft(x_cord_now);
        } else {
            isLeft = false;
            moveToRight(x_cord_now);
        }

    }


    /*  Method to move the Floating widget view to Left  */
    private void moveToLeft(final int current_x_cord) {
        final int x = szWindow.x - current_x_cord;

        new CountDownTimer(500, 5) {
            //get params of Floating Widget view
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

            public void onTick(long t) {
                long step = ( 500 - t ) / 5;

                mParams.x = 0 - (int) ( current_x_cord * current_x_cord * step );

                //If you want bounce effect uncomment below line and comment above line
                // mParams.x = 0 - (int) (double) bounceValue(step, x);


                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }

            public void onFinish() {
                mParams.x = 0;

                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }
        }.start();
    }

    /*  Method to move the Floating widget view to Right  */
    private void moveToRight(final int current_x_cord) {

        new CountDownTimer(500, 5) {
            //get params of Floating Widget view
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

            public void onTick(long t) {
                long step = ( 500 - t ) / 5;

                mParams.x = (int) ( szWindow.x + ( current_x_cord * current_x_cord * step ) - mFloatingWidgetView.getWidth() );

                //If you want bounce effect uncomment below line and comment above line
                //  mParams.x = szWindow.x + (int) (double) bounceValue(step, x_cord_now) - mFloatingWidgetView.getWidth();

                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }

            public void onFinish() {
                mParams.x = szWindow.x - mFloatingWidgetView.getWidth();

                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }
        }.start();
    }

    /*  Get Bounce value if you want to make bounce effect to your Floating Widget */
    private double bounceValue(long step, long scale) {
        double value = scale * java.lang.Math.exp(-0.055 * step) * java.lang.Math.cos(0.08 * step);
        return value;
    }


    // Detect if the floating view is collapsed or expanded
    private boolean isViewCollapsed() {
        return mFloatingWidgetView == null || mFloatingWidgetView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }


    /*  return status bar height on basis of device display metrics  */
    private int getStatusBarHeight() {
        return (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
    }


    /*  Update Floating Widget view coordinates on Configuration change  */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        getWindowManagerDefaultDisplay();

        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {


            if (layoutParams.y + ( mFloatingWidgetView.getHeight() + getStatusBarHeight() ) > szWindow.y) {
                layoutParams.y = szWindow.y - ( mFloatingWidgetView.getHeight() + getStatusBarHeight() );
                mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
            }

            if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                resetPosition(szWindow.x);
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            if (layoutParams.x > szWindow.x) {
                resetPosition(szWindow.x);
            }

        }

    }

    /*  on Floating widget click show expanded view  */
    /*private void onFloatingWidgetClick() {
     *//*if (isViewCollapsed()) {*//*
            //When user clicks on the image view of the collapsed layout,
            //visibility of the collapsed layout will be changed to "View.GONE"
            //and expanded view will become visible.
           *//* collapsedView.setVisibility(View.GONE);
            expandedView.setVisibility(View.VISIBLE);*//*
            circularmenuview.setEventListener(new CircleMenuView.EventListener() {
                                                  @Override
                                                  public void onMenuOpenAnimationStart(@NonNull CircleMenuView view) {

                                                      super.onMenuOpenAnimationStart(view);
                                                  }

                                                  @Override
                                                  public void onMenuOpenAnimationEnd(@NonNull CircleMenuView view) {
                                                      super.onMenuOpenAnimationEnd(view);
                                                  }

                                                  @Override
                                                  public void onMenuCloseAnimationStart(@NonNull CircleMenuView view) {
                                                      super.onMenuCloseAnimationStart(view);
                                                  }

                                                  @Override
                                                  public void onMenuCloseAnimationEnd(@NonNull CircleMenuView view) {
                                                      super.onMenuCloseAnimationEnd(view);
                                                  }

                                                  @Override
                                                  public void onButtonClickAnimationStart(@NonNull CircleMenuView view, int buttonIndex) {
                                                      super.onButtonClickAnimationStart(view, buttonIndex);

                                                  }

                                                  @Override
                                                  public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int buttonIndex) {
                                                      super.onButtonClickAnimationEnd(view, buttonIndex);
                                                      if (buttonIndex == 0) {
                                                       *//*   Intent i = new Intent(context, AcitvityTwitter.class);
                                                          i.putExtra("socialmedianame", "facebook");

                                                          startActivity(i);*//*
                                                          Toast.makeText(getApplicationContext(),"clickmenuitem",Toast.LENGTH_LONG).show();
                                                      }
                                                      if (buttonIndex == 1) {
                                                         *//* Intent i = new Intent(context, AcitvityTwitter.class);
                                                          i.putExtra("socialmedianame", "instagram");
                                                          startActivity(i);*//*
                                                      }
                                                      if (buttonIndex == 2) {
                                                         *//* Intent i = new Intent(context, AcitvityTwitter.class);
                                                          i.putExtra("socialmedianame", "twitter");

                                                          startActivity(i);*//*
                                                      }
                                                      if (buttonIndex == 3) {
                                                       *//*   Intent i = new Intent(context, AcitvityTwitter.class);
                                                          i.putExtra("socialmedianame", "linkedin");
                                                          startActivity(i);*//*
                                                          //Toast.makeText(context,"clickmenuitem",Toast.LENGTH_LONG).show();
                                                      }
                                                      if (buttonIndex == 4) {
                                                          *//*Intent i = new Intent(context, AcitvityTwitter.class);
                                                          i.putExtra("socialmedianame", "youtube");
                                                          startActivity(i);*//*
                                                          // Toast.makeText(context,"clickmenuitem",Toast.LENGTH_LONG).show();
                                                      }
                                                  }

                                                  @Override
                                                  public boolean onButtonLongClick(@NonNull CircleMenuView view, int buttonIndex) {
                                                      return super.onButtonLongClick(view, buttonIndex);
                                                  }

                                                  @Override
                                                  public void onButtonLongClickAnimationStart(@NonNull CircleMenuView view, int buttonIndex) {
                                                      super.onButtonLongClickAnimationStart(view, buttonIndex);
                                                  }

                                                  @Override
                                                  public void onButtonLongClickAnimationEnd(@NonNull CircleMenuView view, int buttonIndex) {
                                                      super.onButtonLongClickAnimationEnd(view, buttonIndex);

                                                  }
                                              }
            );
       *//* }*//*
    }*/
    /*  on Floating widget click show expanded view  */
    private void onFloatingWidgetClick() {

        if (expanded_container.getVisibility() == View.VISIBLE) {
            expanded_container.setVisibility(View.GONE);
            ll_screenshot.setVisibility(View.GONE);
            ll_homeview.setVisibility(View.GONE);
            iv_collapsedview.setImageResource(R.drawable.ic_recode);
        } else {
            expanded_container.setVisibility(View.VISIBLE);
            ll_screenshot.setVisibility(View.VISIBLE);
            ll_homeview.setVisibility(View.VISIBLE);
            iv_collapsedview.setImageResource(R.drawable.ic_cancel);

        }
        // collapse_view.setVisibility(View.VISIBLE);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*  on destroy remove both view from window manager */

        if (mFloatingWidgetView != null)
            mWindowManager.removeView(mFloatingWidgetView);

        if (removeFloatingWidgetView != null)
            mWindowManager.removeView(removeFloatingWidgetView);

    }

}
