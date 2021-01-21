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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;
import android.view.Display;

public class TPTestView extends View{

    static final float X_LEFT = 30;
    static float X_RIGHT = 235;
    static float Y_TOP = 5;
    static float Y_BOTTEM = 315;
    static final float TOLERANCE = X_LEFT * 2;
    static int TOTAL_POINTS_SQU_1 = 0;
    //static final int TOTAL_POINTS_SQU_2 = (int) (Y_BOTTEM - Y_TOP);
    //static final float TOTAL_POINTS_CRO = (float) Math.sqrt(Math.pow(X_RIGHT - X_LEFT, 2)
    //        + Math.pow(Y_BOTTEM - Y_TOP, 2));

    static int TOTAL_POINTS_SQU_2 = 0;
    static float TOTAL_POINTS_CRO = 0;

    private Path mPathSquare;
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
    private boolean drawAble = false;
    private boolean isBegin = false;  
    private float hitCount;
    private float k1;
    private float k2;
    
    private DecimalFormat decimalFormat;
    private String mPercent;
    
    private Context mContext;

    private int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height","dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        Log.v("ldong", "Navi height:" + height);
        return height;
    }

    private int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        Log.v("ldong", "Status height:" + height);
        return height;
    }

    public TPTestView(Context context, int widthPix, int heightPix) {
        super(context);

        mContext = context;
        Y_TOP = X_LEFT;
        Y_BOTTEM = heightPix - Y_TOP;
        X_RIGHT = widthPix - X_LEFT;

        TOTAL_POINTS_SQU_1 = (int) (X_RIGHT - X_LEFT);
        TOTAL_POINTS_SQU_2 = (int) (Y_BOTTEM - Y_TOP);
        TOTAL_POINTS_CRO = (float) Math.sqrt(Math.pow(X_RIGHT - X_LEFT, 2)
                + Math.pow(Y_BOTTEM - Y_TOP, 2));

        k1 = (Y_BOTTEM - Y_TOP) / (X_RIGHT - X_LEFT);
        k2 = (Y_BOTTEM - Y_TOP) / (X_LEFT - X_RIGHT);
        
        decimalFormat = new DecimalFormat("#.#");
        
        mStep = 1;
        mPercent = "";
                     
        subPaths = new Path[6];
        subPaths[0] = new Path();
        subPaths[0].moveTo(X_LEFT, Y_TOP);
        subPaths[0].lineTo(X_RIGHT, Y_TOP);
        subPaths[1] = new Path();
        subPaths[1].moveTo(X_RIGHT, Y_TOP);
        subPaths[1].lineTo(X_RIGHT, Y_BOTTEM);
        subPaths[2] = new Path();
        subPaths[2].moveTo(X_RIGHT, Y_BOTTEM);
        subPaths[2].lineTo(X_LEFT, Y_BOTTEM);
        subPaths[3] = new Path();
        subPaths[3].moveTo(X_LEFT, Y_BOTTEM);
        subPaths[3].lineTo(X_LEFT, Y_TOP);
        subPaths[4] = new Path();
        subPaths[4].moveTo(X_LEFT, Y_TOP);
        subPaths[4].lineTo(X_RIGHT, Y_BOTTEM);
        subPaths[5] = new Path();
        subPaths[5].moveTo(X_RIGHT, Y_TOP);
        subPaths[5].lineTo(X_LEFT, Y_BOTTEM);
        
        mPathSquare = new Path();
        mPathSquare.addPath(subPaths[0]);
        mPathSquare.addPath(subPaths[1]);
        mPathSquare.addPath(subPaths[2]);
        mPathSquare.addPath(subPaths[3]);
        
        mPathCross = new Path();
        mPathCross.addPath(subPaths[4]);
        mPathCross.addPath(subPaths[5]);
        
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

    }   
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.v("TPTestView", "OriPoint  X: "+ event.getX()+" Y: "+ event.getY());       
        
        PointF point = new PointF();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                mPathCurrentRed.reset();
                
                handler.removeMessages(0);
                mPathCurrentGre.reset();
                hitCount = 0;
                side = -1;

                point = fixPoint(event.getX(), event.getY());

                mPathCurrentRed.moveTo(event.getX(), event.getY());
                                               
                if (isOnEdge) {
                    if (mStep == 5 || mStep == 6) {
                        if (point.x <= 120 && point.y <= 200)
                            side = 1;
                        else if (point.x > 120 && point.y > 200)
                            side = 2;
                        else if (point.x > 120 && point.y < 200)
                            side = 3;
                        else if (point.x < 120 && point.y > 200)
                            side = 4;
                    } else {
                        if ((point.x == X_LEFT || point.x == X_RIGHT) && (point.y == Y_TOP || point.y == Y_BOTTEM))
                            side = 0;
                        else if (point.x <= 240 && point.y == Y_TOP)
                            side = 1;
                        else if (point.x > 240 && point.y == Y_TOP)
                            side = 2;
                        else if (point.x == X_RIGHT && point.y <= 400)
                            side = 3;
                        else if (point.x == X_RIGHT && point.y > 400)
                            side = 4;
                        else if (point.x <= 240 && point.y == Y_BOTTEM)
                            side = 6;
                        else if (point.x > 240 && point.y == Y_BOTTEM)
                            side = 5;
                        else if (point.x == X_LEFT && point.y <= 400)
                            side = 8;
                        else if (point.x == X_LEFT && point.y > 400)
                            side = 7;
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
                    if ((mStep == 1 || mStep == 2 || mStep == 3 || mStep == 4)
                            && (side == -1 || side == 0))
                        if ((point.x == X_LEFT || point.x == X_RIGHT)
                                && (point.y == Y_TOP || point.y == Y_BOTTEM))
                            side = 0;
                        else if (point.x <= 240 && point.y == Y_TOP)
                            side = 1;
                        else if (point.x > 240 && point.y == Y_TOP)
                            side = 2;
                        else if (point.x == X_RIGHT && point.y <= 400)
                            side = 3;
                        else if (point.x == X_RIGHT && point.y > 400)
                            side = 4;
                        else if (point.x <= 240 && point.y == Y_BOTTEM)
                            side = 6;
                        else if (point.x > 240 && point.y == Y_BOTTEM)
                            side = 5;
                        else if (point.x == X_LEFT && point.y <= 400)
                            side = 8;
                        else if (point.x == X_LEFT && point.y > 400)
                            side = 7;                    
                }
                
                if (drawAble || !isBegin)
                    hitPoint(point.x, point.y);
                
                if (mStep == 1 || mStep == 2 || mStep == 3 || mStep == 4) {
                    if (side == 1 || side == 2 || side == 5 || side == 6)
                        mPercent = decimalFormat
                                .format(((float) hitCount / (float) TOTAL_POINTS_SQU_1) * 100) + "%";
                    else if (side == 3 || side == 4 || side == 7 || side == 8)
                        mPercent = decimalFormat
                                .format(((float) hitCount / (float) TOTAL_POINTS_SQU_2) * 100) + "%";
                } else
                    mPercent = decimalFormat
                            .format(((float) hitCount / (float) TOTAL_POINTS_CRO) * 100) + "%";
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (mStep == 1 || mStep == 2 || mStep == 3 || mStep == 4) {
                    if ((side == 1 || side == 2 || side == 5 || side == 6)
                            && ((float) hitCount / (float) TOTAL_POINTS_SQU_1) > 0.95) {
                        if (side == 1 || side == 2) {
                            mStep++;
                            subPaths[0].reset();
                        } else if (side == 5 || side == 6) {
                            mStep++;
                            subPaths[2].reset();
                        }
                        mPathSquare.reset();
                        mPathSquare.addPath(subPaths[0]);
                        mPathSquare.addPath(subPaths[1]);
                        mPathSquare.addPath(subPaths[2]);
                        mPathSquare.addPath(subPaths[3]);
                    } else if ((side == 3 || side == 4 || side == 7 || side == 8)
                            && ((float) hitCount / (float) TOTAL_POINTS_SQU_2) > 0.95) {
                        if (side == 3 || side == 4) {
                            mStep++;
                            subPaths[1].reset();
                        } else if (side == 7 || side == 8) {
                            mStep++;
                            subPaths[3].reset();
                        }
                        mPathSquare.reset();
                        mPathSquare.addPath(subPaths[0]);
                        mPathSquare.addPath(subPaths[1]);
                        mPathSquare.addPath(subPaths[2]);
                        mPathSquare.addPath(subPaths[3]);
                    }
                    handler.sendEmptyMessage(0);                
                } else if (((float) hitCount / (float) TOTAL_POINTS_CRO) > 0.95) {
                    Log.i("TPTestView","mStep = "+mStep+"  side = "+side);
                    if (mStep == 5) {
                        if (side == 1 || side == 2) {
                            mStep++;
                            //bCrossLeft = true;
                            subPaths[4].reset();
                            mPathCross.reset();
                            mPathCross.addPath(subPaths[4]);
                            mPathCross.addPath(subPaths[5]);
                        } else if (side == 3 || side == 4) {
                            mStep++;
                            //bCrossRight = true;
                            subPaths[5].reset();
                            mPathCross.reset();
                            mPathCross.addPath(subPaths[4]);
                            mPathCross.addPath(subPaths[5]);
                        }
                    }else if (mStep == 6) {
                        Intent intent = new Intent();
                        intent.putExtra("result", true);
                        TouchPanelActivity activity = (TouchPanelActivity) mContext;
                        activity.setResult(1, intent);
                        activity.finish();
                    }
                    handler.sendEmptyMessage(0);
                } else
                    handler.sendEmptyMessageDelayed(0, 500);
                side = 0;
                break;
            default:
                break;
        }               
        return true;
    }
    
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPathCurrentRed.reset();
            mPathCurrentGre.reset();
            mPercent = "";
            invalidate();
        }       
    };
    
    protected PointF fixPoint(float x, float y) {
        isOnEdge = false;
        if (mStep == 1 || mStep == 2 || mStep == 3 || mStep == 4) {
            if (y >= Y_TOP - TOLERANCE && y <= Y_TOP + TOLERANCE && x >= X_LEFT - TOLERANCE && x <= X_LEFT + TOLERANCE) {
                if (side == 1 || side == 2)
                    y = Y_TOP;
                else if (side == 7  || side == 8)
                    x = X_LEFT;
                else {
                    x = X_LEFT;
                    y = Y_TOP;
                }
                isOnEdge = true;
            } else if (y >= Y_TOP - TOLERANCE && y <= Y_TOP + TOLERANCE && x >= X_RIGHT - TOLERANCE && x <= X_RIGHT + TOLERANCE) {
                if (side == 1 || side == 2)
                    y = Y_TOP;
                else if (side == 3 || side == 4)
                    x = X_RIGHT;
                else {
                    x = X_RIGHT;
                    y = Y_TOP;
                }
                isOnEdge = true;
            }else if (y >= Y_BOTTEM - TOLERANCE && y <= Y_BOTTEM + TOLERANCE && x >= X_RIGHT - TOLERANCE && x <= X_RIGHT + TOLERANCE) {
                if (side == 3 || side == 4)
                    x = X_RIGHT;
                else if (side == 5 || side == 6)
                    y = Y_BOTTEM;
                else {
                    x = X_RIGHT;
                    y = Y_BOTTEM;
                }
                isOnEdge = true;
            } else if (y >= Y_BOTTEM - TOLERANCE && y <= Y_BOTTEM + TOLERANCE && x >= X_LEFT - TOLERANCE && x <= X_LEFT + TOLERANCE) {
                if (side == 5 || side == 6)
                    y = Y_BOTTEM;
                else if (side == 7 || side == 8)
                    x = X_LEFT;
                else {
                    x = X_LEFT;
                    y = Y_BOTTEM;
                }
                isOnEdge = true;
            }else {
                if ((side == 1 || side == 2 || side == 0 || side == -1) && y >= Y_TOP - TOLERANCE && y <= Y_TOP || (y >= Y_TOP && y <= Y_TOP + TOLERANCE)) {
                    y = Y_TOP;
                    isOnEdge = true;
                } 
                if ((side == 7 || side == 8 || side == 0 || side == -1) && x >= X_LEFT - TOLERANCE && x <= X_LEFT || (x >= X_LEFT && x <= X_LEFT + TOLERANCE)) {
                    x = X_LEFT;
                    isOnEdge = true;
                }
                if ((side == 3 || side == 4 || side == 0 || side == -1) && x >= X_RIGHT - TOLERANCE && x <= X_RIGHT || (x >= X_RIGHT && x <= X_RIGHT + TOLERANCE)) {
                    x = X_RIGHT;
                    isOnEdge = true;
                }
                if ((side == 5 || side == 6 || side == 0 || side == -1) && y >= Y_BOTTEM - TOLERANCE && y <= Y_BOTTEM || (y >= Y_BOTTEM && y <= Y_BOTTEM + TOLERANCE)) {
                    y = Y_BOTTEM;
                    isOnEdge = true;
                }
            }            
            if (x < X_LEFT || x > X_RIGHT || y < Y_TOP || y > Y_BOTTEM)
                isOnEdge = false;
        } else if (mStep == 5 || mStep == 6) {
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

            Log.i("ldong", "side = "+side +" y1 = " +y1 +" y2 = "+y2);
            if (!subPaths[4].isEmpty() && y >= y1 - TOLERANCE -20 && y <= y1 + TOLERANCE+20 && side != 3 && side != 4) {
                y = y1;
                isOnEdge = true;
            } else if (!subPaths[5].isEmpty() && y >= y2 - TOLERANCE -20 && y <= y2 + TOLERANCE +20 && side != 1 && side !=2) {
                y = y2;
                isOnEdge = true;
            }
        }

        Log.i("TPTestView", "FixPoint  X: " + x + " Y:" + y + " isOnEdge = "+isOnEdge + " mStep = "+mStep);
        return new PointF(x, y);
    }
    
    protected void hitPoint(float x, float y) {
        Log.v("TPTestView", "side : " + side);
        Log.v("TPTestView", "LastPoint : " + mLastPoint.x + "  " + mLastPoint.y);
        Log.v("TPTestView", "CurrPoint : " + x + "  " + y);
        if (isOnEdge && (mStep == 1 || mStep == 2 || mStep == 3 || mStep == 4)) {
            if (side >= 0 && !isBegin) {
                drawAble = true;
                isBegin = true;
                mPathCurrentGre.moveTo(x, y);
                mFirstPoint.set(x, y);
                mLastPoint.set(x, y);              
            } 
            if (side == 0){
                isHit = true;
            } else if (!subPaths[0].isEmpty() && side == 1) {
                if (x >= mLastPoint.x && (y == mLastPoint.y || mLastPoint.x == X_LEFT)) {
                    if (isHit){
                        hitCount += Math.abs(x - mLastPoint.x);
                        mPathCurrentGre.lineTo(x, y);                        
                    } else {
                        isHit = true;                        
                    }
                    mLastPoint.set(x, y);
                }               
            } else if (!subPaths[0].isEmpty() && side == 2) {
                if (x <= mLastPoint.x && (y == mLastPoint.y || mLastPoint.x == X_RIGHT)) {
                    if (isHit){
                        hitCount += Math.abs(x - mLastPoint.x);
                        mPathCurrentGre.lineTo(x, y);
                    } else {
                        isHit = true;                        
                    }
                    mLastPoint.set(x, y);
                }               
            } else if (!subPaths[1].isEmpty() && side == 3) {
                if (y >= mLastPoint.y && (x == mLastPoint.x || mLastPoint.y == Y_TOP)) {
                    if (isHit){
                        hitCount += Math.abs(y - mLastPoint.y);
                        mPathCurrentGre.lineTo(x, y);
                    } else {
                        isHit = true;
                    }
                    mLastPoint.set(x, y);
                }
            } else if (!subPaths[1].isEmpty() && side == 4) {
                if (y <= mLastPoint.y && (x == mLastPoint.x || mLastPoint.y == Y_BOTTEM)) {
                    if (isHit){
                        hitCount += Math.abs(y - mLastPoint.y);
                        mPathCurrentGre.lineTo(x, y);
                    } else {
                        isHit = true;
                    }
                    mLastPoint.set(x, y);
                }
            } else if (!subPaths[2].isEmpty() && side == 5) {
                if (x <= mLastPoint.x && (y == mLastPoint.y || mLastPoint.x == X_RIGHT)) {
                    if (isHit){
                        hitCount += Math.abs(x - mLastPoint.x);
                        mPathCurrentGre.lineTo(x, y);
                    } else {
                        isHit = true;
                    }
                    mLastPoint.set(x, y);
                }
            } else if (!subPaths[2].isEmpty() && side == 6) {
                if (x >= mLastPoint.x && (y == mLastPoint.y || mLastPoint.x == X_LEFT)) {
                    if (isHit){
                        hitCount += Math.abs(x - mLastPoint.x);
                        mPathCurrentGre.lineTo(x, y);
                    } else {
                        isHit = true;
                    }
                    mLastPoint.set(x, y);
                }
            } else if (!subPaths[3].isEmpty() && side == 7) {
                if (y <= mLastPoint.y && (x == mLastPoint.x || mLastPoint.y == Y_BOTTEM)) {
                    if (isHit){
                        hitCount += Math.abs(y - mLastPoint.y);
                        mPathCurrentGre.lineTo(x, y);
                    } else {
                        isHit = true;
                    }
                    mLastPoint.set(x, y);
                }
            } else if (!subPaths[3].isEmpty() && side == 8) {
                if (y >= mLastPoint.y && (x == mLastPoint.x || mLastPoint.y == Y_TOP)) {
                    if (isHit){
                        hitCount += Math.abs(y - mLastPoint.y);
                        mPathCurrentGre.lineTo(x, y);
                    } else {
                        isHit = true;
                    }
                    mLastPoint.set(x, y);
                }
            } 
        } else if (isOnEdge && (mStep == 5 || mStep == 6)) {
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
        Log.v("TPTestView", "Percent:  "+ mPercent);
        Log.v("TPTestView","   ");
       
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
           
        if (mStep == 1 || mStep == 2 || mStep == 3 || mStep == 4) {
            canvas.drawPath(mPathSquare, mPaintStandard);

            canvas.drawText(mPercent, 80, 140, mPaintPercent);
            
        } else if (mStep == 5 || mStep == 6) {
            canvas.drawPath(mPathCross, mPaintStandard);

            canvas.drawText(mPercent, 80, 120, mPaintPercent);
        }
                
        canvas.drawPath(mPathCurrentGre, mPaintCurrentGre);
        canvas.drawPath(mPathCurrentRed, mPaintCurrentRed);
               
    }
}