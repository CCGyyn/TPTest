package com.odm.tptest;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;
import android.util.Log;

public class TPTestView extends View {

    private static final int RESET_MSG = 100;
    private static final int TIMEOUT_MSG = 101;
    private static final long TIMEOUT = 15 * 1000L;

    static final float X_LEFT = 60;
    static float X_RIGHT = 235;
    static float X_MIDDLE = 0;
    static float Y_TOP = 5;
    static float Y_BOTTEM = 315;
    static float Y_MIDDLE = 0;
    static final float TOLERANCE = 60;
    static int TOTAL_POINTS_SQU_1 = 0;

    static int TOTAL_POINTS_SQU_2 = 0;
    static float TOTAL_POINTS_CRO = 0;

    private Path mPathHorizontal;
    private Path mPathVertical;
    private Path mPathCross;
    private Path mPathCurrentGre;
    private Path mPathCurrentRed;
    private Path[] subPaths;
    
    private Paint mPaintStandard;
    private Paint mPaintCurrentGre;
    private Paint mPaintCurrentRed;
    private Paint mPaintPercent;    
                       
    private PointF mFirstPoint;
    private PointF mLastPoint;    
    
    private int side = 0;
    private int mStep;
    private boolean isOnEdge = false;
    private boolean isHit = false;
    private boolean isIncrease = false;
    private boolean isDecrease = false;
    private boolean drawAble = false;
    private boolean isBegin = false;  
    private float hitCount;
    private float k1;
    private float k2;
    
    private DecimalFormat decimalFormat;
    private String mPercent;
    
    private Context mContext;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RESET_MSG:
                    mPathCurrentRed.reset();
                    mPathCurrentGre.reset();
                    mPercent = "";
                    invalidate();
                    break;
                case TIMEOUT_MSG:
                    Intent intent = new Intent();
                    intent.putExtra("result", true);
                    TouchPanelActivity activity = (TouchPanelActivity) mContext;
                    activity.feedbackResult(0);
                    //activity.feedbackResult(TestCase.STATE_FAIL);
                    break;
                default:
                    break;
            }

        }
    };

    private int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height","dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        Log.d("Navi height:" + height);
        return height;
    }

    private int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        Log.d("Status height:" + height);
        return height;
    }

    public TPTestView(Context context, int widthPix, int heightPix) {
        super(context);

        mContext = context;
        Y_TOP = TOLERANCE;
        Y_BOTTEM = heightPix - Y_TOP;
        X_RIGHT = widthPix - X_LEFT;
        X_MIDDLE = widthPix / 2;
        Y_MIDDLE = heightPix /2;

        //modified by caigaopeng for Q6801 ID1011505 begin
        /*TOTAL_POINTS_SQU_1 = (int) (X_RIGHT - X_LEFT);
        TOTAL_POINTS_SQU_2 = (int) (Y_BOTTEM - Y_TOP);*/
        TOTAL_POINTS_SQU_1 = widthPix - (int)X_LEFT / 2;
        TOTAL_POINTS_SQU_2 = heightPix - (int)Y_TOP / 2;
        //modified by caigaopeng for Q6801 ID1011505 end
        TOTAL_POINTS_CRO = (float) Math.sqrt(Math.pow(X_RIGHT - X_LEFT, 2)
                + Math.pow(Y_BOTTEM - Y_TOP, 2));

        k1 = (Y_BOTTEM - Y_TOP) / (X_RIGHT - X_LEFT);
        k2 = (Y_BOTTEM - Y_TOP) / (X_LEFT - X_RIGHT);
        
        decimalFormat = new DecimalFormat("#.#");
        
        mStep = 1;
        mPercent = "";
                     
        subPaths = new Path[8];
        subPaths[0] = new Path();
        subPaths[0].moveTo(X_LEFT, Y_TOP);
        subPaths[0].lineTo(X_RIGHT, Y_TOP);
        subPaths[1] = new Path();
        subPaths[1].moveTo(X_LEFT, Y_MIDDLE);
        subPaths[1].lineTo(X_RIGHT, Y_MIDDLE);
        subPaths[2] = new Path();
        subPaths[2].moveTo(X_LEFT, Y_BOTTEM);
        subPaths[2].lineTo(X_RIGHT, Y_BOTTEM);

        subPaths[3] = new Path();
        subPaths[3].moveTo(X_LEFT, Y_TOP - TOLERANCE);
        subPaths[3].lineTo(X_LEFT, Y_BOTTEM + TOLERANCE);
        subPaths[4] = new Path();
        subPaths[4].moveTo(X_MIDDLE, Y_TOP - TOLERANCE);
        subPaths[4].lineTo(X_MIDDLE, Y_BOTTEM + TOLERANCE);
        subPaths[5] = new Path();
        subPaths[5].moveTo(X_RIGHT, Y_TOP - TOLERANCE);
        subPaths[5].lineTo(X_RIGHT, Y_BOTTEM + TOLERANCE);

        subPaths[6] = new Path();
        subPaths[6].moveTo(X_LEFT, Y_TOP);
        subPaths[6].lineTo(X_RIGHT, Y_BOTTEM);
        subPaths[7] = new Path();
        subPaths[7].moveTo(X_RIGHT, Y_TOP);
        subPaths[7].lineTo(X_LEFT, Y_BOTTEM);
        
        mPathHorizontal = new Path();
        mPathHorizontal.addPath(subPaths[0]);
        mPathHorizontal.addPath(subPaths[1]);
        mPathHorizontal.addPath(subPaths[2]);

        mPathVertical = new Path();
        mPathVertical.addPath(subPaths[3]);
        mPathVertical.addPath(subPaths[4]);
        mPathVertical.addPath(subPaths[5]);

        mPathCross = new Path();
        mPathCross.addPath(subPaths[6]);
        mPathCross.addPath(subPaths[7]);
        
        mPathCurrentGre = new Path();
        mPathCurrentRed = new Path();
        
        mPaintStandard = new Paint();
        mPaintStandard.setColor(Color.BLUE);
        mPaintStandard.setStrokeWidth(TOLERANCE*2);
        mPaintStandard.setStyle(Style.STROKE);
        mPaintStandard.setDither(true);
        mPaintStandard.setStrokeCap(Cap.ROUND);
        mPaintStandard.setStrokeJoin(Join.ROUND);
        mPaintStandard.setAntiAlias(true);
        mPaintStandard.setAlpha(255);
        
        mPaintCurrentGre = new Paint();
        mPaintCurrentGre.setColor(Color.GREEN);
        mPaintCurrentGre.setStrokeWidth(TOLERANCE*4/3);
        mPaintCurrentGre.setStyle(Style.STROKE);
        mPaintCurrentGre.setDither(true);
        mPaintCurrentGre.setStrokeCap(Cap.ROUND);
        mPaintCurrentGre.setStrokeJoin(Join.ROUND);
               
        mPaintCurrentRed = new Paint();
        mPaintCurrentRed.setColor(Color.RED);
        mPaintCurrentRed.setStrokeWidth(5);
        mPaintCurrentRed.setStyle(Style.STROKE);
        mPaintCurrentRed.setDither(true);
        mPaintCurrentRed.setStrokeCap(Cap.ROUND);
        mPaintCurrentRed.setStrokeJoin(Join.ROUND);
        
        mPaintPercent = new Paint();
        mPaintPercent.setStrokeWidth(4);
        mPaintPercent.setTextSize(50);
        mPaintPercent.setColor(Color.GRAY);
        
        mFirstPoint = new PointF();
        mLastPoint = new PointF();
        handler.sendEmptyMessageDelayed(TIMEOUT_MSG, TIMEOUT);

    }   
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.d("OriPoint  X: "+ event.getX()+" Y: "+ event.getY());
        
        PointF point = new PointF();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handler.removeMessages(TIMEOUT_MSG);

                mPathCurrentRed.reset();
                
                handler.removeMessages(RESET_MSG);
                mPathCurrentGre.reset();
                hitCount = 0;
                side = -1;

                point = fixPoint(event.getX(), event.getY());
                isIncrease = false;
                isDecrease = false;

                mPathCurrentRed.moveTo(event.getX(), event.getY());
                                               
                if (isOnEdge) {
                    /*
                    * side:
                    * \     /
                    *  7   8
                    *   \ /
                    * */
                    if (mStep == 7 || mStep == 8) {
                        if (point.x <= 120 && point.y <= 200)
                            side = 1;
                        else if (point.x > 120 && point.y > 200)
                            side = 2;
                        else if (point.x > 120 && point.y < 200)
                            side = 3;
                        else if (point.x < 120 && point.y > 200)
                            side = 4;
                    } else if (mStep <= 6) {
                        /*
                        * side :
                        * --1--
                        * --2--
                        * --3--
                        *
                        * | | |
                        * 4 5 6
                        * | | |
                        * */
                        if ((point.x == X_LEFT || point.x == X_RIGHT || point.x == X_MIDDLE)
                                && (point.y == Y_TOP || point.y == Y_BOTTEM || point.y == Y_MIDDLE)) {
                            side = 0;
                        } else if (point.y == Y_TOP) {
                            side = 1;
                        } else if (point.y == Y_MIDDLE) {
                            side = 2;
                        } else if (point.y == Y_BOTTEM) {
                            side = 3;
                        }else if (point.y == X_LEFT) {
                            side = 4;
                        } else if (point.y == X_MIDDLE) {
                            side = 5;
                        } else if (point.y == X_RIGHT) {
                            side = 6;
                        }
                    }
                                        
                    drawAble = true;
                    isBegin = true;
                    mPathCurrentGre.moveTo(point.x, point.y);
                    mFirstPoint.set(point.x, point.y);
                    mLastPoint.set(point.x, point.y);
                    
                    hitPoint(point.x, point.y);
                } else{
                    drawAble = false;
                    isBegin = false;
                }
                
                break;
            case MotionEvent.ACTION_MOVE:
                point = fixPoint(event.getX(), event.getY());
                mPathCurrentRed.lineTo(event.getX(), event.getY());
                if (isOnEdge) {
                    if (mStep <= 6 && (side == -1 || side == 0)) {
                        if ((point.x == X_LEFT || point.x == X_RIGHT || point.x == X_MIDDLE)
                                && (point.y == Y_TOP || point.y == Y_BOTTEM || point.y == Y_MIDDLE)) {
                            side = 0;
                        } else if (point.y == Y_TOP) {
                            side = 1;
                        } else if (point.y == Y_MIDDLE) {
                            side = 2;
                        } else if (point.y == Y_BOTTEM) {
                            side = 3;
                        }else if (point.x == X_LEFT) {
                            side = 4;
                        } else if (point.x == X_MIDDLE) {
                            side = 5;
                        } else if (point.x == X_RIGHT) {
                            side = 6;
                        }
                    }
                }
                
                if (drawAble || !isBegin)
                    hitPoint(point.x, point.y);
                
                if (mStep <= 3) {
                    if (side > 0 && side <= 3) {
                        mPercent = decimalFormat
                                .format(((float) hitCount / (float) TOTAL_POINTS_SQU_1) * 100) + "%";
                    }
                } else if (mStep <= 6) {
                    if (side >= 3 && side <= 6) {
                        mPercent = decimalFormat
                                .format(((float) hitCount / (float) TOTAL_POINTS_SQU_2) * 100) + "%";
                    }
                } else {
                    mPercent = decimalFormat
                            .format(((float) hitCount / (float) TOTAL_POINTS_CRO) * 100) + "%";
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                handler.sendEmptyMessageDelayed(TIMEOUT_MSG, TIMEOUT);
                Log.d("MotionEvent.ACTION_UP Percent:  "+ mPercent);
                if (mStep <= 6) {
                    if ((side > 0 && mStep <= 3)
                            && ((float) hitCount / (float) TOTAL_POINTS_SQU_1) > 0.95) {
                        if (side == 1) {
                            mStep++;
                            subPaths[0].reset();
                        } else if (side == 2) {
                            mStep++;
                            subPaths[1].reset();
                        } else if (side == 3) {
                            mStep++;
                            subPaths[2].reset();
                        }
                        mPathHorizontal.reset();
                        mPathHorizontal.addPath(subPaths[0]);
                        mPathHorizontal.addPath(subPaths[1]);
                        mPathHorizontal.addPath(subPaths[2]);
                    } else if ((side > 0 && mStep <= 6)
                            && ((float) hitCount / (float) TOTAL_POINTS_SQU_2) >= 0.95) {
                        if (side == 4) {
                            mStep++;
                            subPaths[3].reset();
                        } else if (side == 5) {
                            mStep++;
                            subPaths[4].reset();
                        } else if (side == 6) {
                            mStep++;
                            subPaths[5].reset();
                        }
                        mPathVertical.reset();
                        mPathVertical.addPath(subPaths[3]);
                        mPathVertical.addPath(subPaths[4]);
                        mPathVertical.addPath(subPaths[5]);
                    }
                    handler.sendEmptyMessage(RESET_MSG);
                } else if (((float) hitCount / (float) TOTAL_POINTS_CRO) > 0.95) {
                    Log.d("mStep = "+mStep+"  side = "+side);
                    if (mStep == 7) {
                        if (side == 1 || side == 2) {
                            mStep++;
                            //bCrossLeft = true;
                            subPaths[6].reset();
                            mPathCross.reset();
                            mPathCross.addPath(subPaths[6]);
                            mPathCross.addPath(subPaths[7]);
                        } else if (side == 3 || side == 4) {
                            mStep++;
                            //bCrossRight = true;
                            subPaths[7].reset();
                            mPathCross.reset();
                            mPathCross.addPath(subPaths[6]);
                            mPathCross.addPath(subPaths[7]);
                        }
                    } else if (mStep == 8) {
                        handler.removeMessages(TIMEOUT_MSG);
                        Intent intent = new Intent();
                        intent.putExtra("result", true);
                        TouchPanelActivity activity = (TouchPanelActivity) mContext;
                        activity.feedbackResult(0);
                        //activity.feedbackResult(TestCase.STATE_PASS);
                    }
                    handler.sendEmptyMessage(RESET_MSG);
                } else
                    handler.sendEmptyMessageDelayed(RESET_MSG, 500);
                side = 0;
                break;
            default:
                break;
        }               
        return true;
    }
    
    protected PointF fixPoint(float x, float y) {
        isOnEdge = false;
        if (mStep <= 3) {
            if ( (side == 1 || side == 0 || side == -1) && y >= Y_TOP - TOLERANCE * 2/3 && y <= Y_TOP + TOLERANCE * 2/3) {
                y = Y_TOP;
                isOnEdge = true;
            } else if ((side == 2 || side == 0 || side == -1) && y >= Y_MIDDLE - TOLERANCE * 2/3 && y <= Y_MIDDLE + TOLERANCE * 2/3) {
                y = Y_MIDDLE;
                isOnEdge = true;
            } else if ((side == 3 || side == 0 || side == -1) && y >= Y_BOTTEM - TOLERANCE * 2/3 && y <= Y_BOTTEM + TOLERANCE * 2/3) {
                y = Y_BOTTEM;
                isOnEdge = true;
            }
            //deleted by caigaopeng for Q6801 ID1011505 begin
            /*if (x < X_LEFT || x > X_RIGHT || y < Y_TOP || y > Y_BOTTEM)
                isOnEdge = false;*/
            //deleted by caigaopeng for Q6801 ID1011505 end
        } else if (mStep <= 6) {

            if ((side == 4 || side == 0 || side == -1) && x >= X_LEFT - TOLERANCE * 2/3 && x <= X_LEFT + TOLERANCE * 2/3) {
                x = X_LEFT;
                isOnEdge = true;
            } else if ((side == 5 || side == 0 || side == -1) && x >= X_MIDDLE - TOLERANCE * 2/3 && x <= X_MIDDLE + TOLERANCE * 2/3) {
                x = X_MIDDLE;
                isOnEdge = true;
            } else if ((side == 6 || side == 0 || side == -1) && x >= X_RIGHT - TOLERANCE * 2/3 && x <= X_RIGHT + TOLERANCE * 2/3) {
                x = X_RIGHT;
                isOnEdge = true;
            }

            //deleted by caigaopeng for Q6801 ID1011505 begin
            /*if (x < X_LEFT || x > X_RIGHT || y < Y_TOP || y > Y_BOTTEM)
                isOnEdge = false;*/
            //deleted by caigaopeng for Q6801 ID1011505 end
        } else if (mStep == 7 || mStep == 8) {
            if (x < X_LEFT)
                x = X_LEFT;
            else if (x > X_RIGHT)
                x = X_RIGHT;

            float y1 = (x - X_LEFT) * k1 + Y_TOP;
            float y2 = (x - X_RIGHT) * k2 + Y_TOP;

            if (y1 < Y_TOP)
                y1 = Y_TOP;
            else if (y1 > Y_BOTTEM)
                y1 = Y_BOTTEM;
            if (y2 < Y_TOP)
                y2 = Y_TOP;
            else if (y2 > Y_BOTTEM)
                y2 = Y_BOTTEM;

            Log.d("side = "+side +" y1 = " +y1 +" y2 = "+y2);
            if (!subPaths[6].isEmpty() && y >= y1 - TOLERANCE -20 && y <= y1 + TOLERANCE+20 && side != 3 && side != 4) {
                y = y1;
                isOnEdge = true;
            } else if (!subPaths[7].isEmpty() && y >= y2 - TOLERANCE -20 && y <= y2 + TOLERANCE +20 && side != 1 && side !=2) {
                y = y2;
                isOnEdge = true;
            }
        }

        Log.d("FixPoint  X: " + x + " Y:" + y + " isOnEdge = "+isOnEdge + " mStep = "+mStep);
        return new PointF(x, y);
    }
    
    protected void hitPoint(float x, float y) {
        Log.d("side : " + side);
        Log.d("LastPoint : " + mLastPoint.x + "  " + mLastPoint.y);
        Log.d("CurrPoint : " + x + "  " + y);
        if (isOnEdge && (mStep <= 6)) {
            if (side >= 0 && !isBegin) {
                drawAble = true;
                isBegin = true;
                mPathCurrentGre.moveTo(x, y);
                mFirstPoint.set(x, y);
                mLastPoint.set(x, y);              
            } 
            if (side == 0){
                isHit = true;
                isIncrease = false;
                isDecrease = false;
            } else if ((!subPaths[0].isEmpty() && side == 1)
                            || (!subPaths[1].isEmpty() && side == 2)
                            || (!subPaths[2].isEmpty() && side == 3)) {
                if (x != mLastPoint.x && y == mLastPoint.y) {
                    if (!isIncrease && !isDecrease) {
                        if (x > mLastPoint.x) {
                            isIncrease = true;
                        } else {
                            isDecrease = true;
                        }
                    }
                    if (isIncrease && x < mLastPoint.x) {
                        return;
                    } else if (isDecrease && x > mLastPoint.x) {
                        return;
                    }
                    if (isHit){
                        hitCount += Math.abs(x - mLastPoint.x);
                        mPathCurrentGre.lineTo(x, y);                        
                    } else {
                        isHit = true;
                        isIncrease = false;
                        isDecrease = false;
                    }
                    mLastPoint.set(x, y);
                }               
            } else if ((!subPaths[3].isEmpty() && side == 4)
                                || (!subPaths[4].isEmpty() && side == 5)
                                || (!subPaths[5].isEmpty() && side == 6)) {
                if (y != mLastPoint.y && x == mLastPoint.x) {
                    if (!isIncrease && !isDecrease) {
                        if (y > mLastPoint.y) {
                            isIncrease = true;
                        } else {
                            isDecrease = true;
                        }
                    }
                    if (isIncrease && y < mLastPoint.y) {
                        return;
                    } else if (isDecrease && y > mLastPoint.y) {
                        return;
                    }
                    if (isHit){
                        hitCount += Math.abs(y - mLastPoint.y);
                        mPathCurrentGre.lineTo(x, y);
                    } else {
                        isHit = true;
                        isIncrease = false;
                        isDecrease = false;
                    }
                    mLastPoint.set(x, y);
                }
            }
        } else if (isOnEdge && (mStep == 7 || mStep == 8)) {
            if (side > 0) {
                if (!isBegin) {
                    drawAble = true;
                    isBegin = true;
                    mPathCurrentGre.moveTo(x, y);
                    mFirstPoint.set(x, y);
                    mLastPoint.set(x, y);
                }
                if (side == 1 && x > mLastPoint.x) {
                    if (isHit) {
                        hitCount += Math.sqrt(Math.pow(x - mLastPoint.x, 2)
                                + Math.pow(y - mLastPoint.y, 2));
                        mPathCurrentGre.lineTo(x, y);
                    }
                    isHit = true;
                    mLastPoint.set(x, y);
                } else if (side == 2 && x < mLastPoint.x) {
                    if (isHit) {
                        hitCount += Math.sqrt(Math.pow(x - mLastPoint.x, 2)
                                + Math.pow(y - mLastPoint.y, 2));
                        mPathCurrentGre.lineTo(x, y);
                    }
                    isHit = true;
                    mLastPoint.set(x, y);
                } else if (side == 3 && y > mLastPoint.y) {
                    if (isHit) {
                        hitCount += Math.sqrt(Math.pow(x - mLastPoint.x, 2)
                                + Math.pow(y - mLastPoint.y, 2));
                        mPathCurrentGre.lineTo(x, y);
                    }
                    isHit = true;
                    mLastPoint.set(x, y);
                } else if (side == 4 && y < mLastPoint.y) {
                    if (isHit) {
                        hitCount += Math.sqrt(Math.pow(x - mLastPoint.x, 2)
                                + Math.pow(y - mLastPoint.y, 2));
                        mPathCurrentGre.lineTo(x, y);
                    }
                    isHit = true;
                    mLastPoint.set(x, y);
                }
            } 
        } else {
            isHit = false;
            drawAble = false;
        }
        Log.d("Percent:  "+ mPercent);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mStep <= 3) {
            canvas.drawPath(mPathHorizontal, mPaintStandard);

            //canvas.drawText(mPercent, 80, 140, mPaintPercent);
            
        } else if (mStep <= 6) {
            canvas.drawPath(mPathVertical, mPaintStandard);

            //canvas.drawText(mPercent, 80, 140, mPaintPercent);
        } else if (mStep <= 8) {
            canvas.drawPath(mPathCross, mPaintStandard);

            //canvas.drawText(mPercent, 80, 120, mPaintPercent);
        }
                
        canvas.drawPath(mPathCurrentGre, mPaintCurrentGre);
        canvas.drawPath(mPathCurrentRed, mPaintCurrentRed);
               
    }
}
