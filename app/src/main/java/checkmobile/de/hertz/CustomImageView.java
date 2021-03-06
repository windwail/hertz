package checkmobile.de.hertz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.pixplicity.sharp.OnSvgElementListener;
import com.pixplicity.sharp.Sharp;
import com.pixplicity.sharp.SvgParseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by icetusk on 19.05.16.
 */
public class CustomImageView extends ImageView{

    private String TAG = this.getClass().getName();

    private Sharp mSvg;

    private Path p;

    private CustomImageView imageView;

    private Point touched = new Point();

    private String selectedId;

    private ScaleGestureDetector sgd;

    private Float scale = 1f;

    private Matrix matrix = new Matrix();

    private Map<String, PathInfo> cache = new HashMap<>();

    private GestureDetector gestureDetector;

    private float maxX;

    private float maxY;

    private int initialWidth;

    private int initialHeight;

    Rect rect;

    private GestureDetector GD ;

    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.e(TAG, "Scrolled "+distanceX+ " " + distanceY);
            setPan(-distanceX, -distanceY);
            return true;
        }


    };

    public CustomImageView(Context context) {
        super(context);
        init(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());

    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mSvg = Sharp.loadResource(getResources(), R.raw.car);

        mSvg.setOnElementListener(new OnSvgElementListener() {
            @Override
            public void onSvgStart(@NonNull Canvas canvas,
                                   @Nullable RectF bounds) {

            }

            @Override
            public void onSvgEnd(@NonNull Canvas canvas,
                                 @Nullable RectF bounds) {

                rect = new Rect((int)bounds.left, (int)bounds.right, (int)bounds.top, (int)bounds.bottom);

                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(10);
                paint.setColor(Color.WHITE);

            }

            @Override
            public <T> T onSvgElement(@Nullable String id,
                                      @NonNull T element,
                                      @Nullable RectF elementBounds,
                                      @NonNull Canvas canvas,
                                      @Nullable RectF canvasBounds,
                                      @Nullable Paint paint) {
                p = (Path) element;
                if (p instanceof Path) {

                    RectF rectF = new RectF();
                    Region region = new Region();
                    if (!cache.containsKey(id)) {
                        p.computeBounds(rectF, true);

                        if(rectF.right > maxX) {
                            maxX = rectF.right;
                        }

                        if(rectF.bottom > maxY) {
                            maxY = rectF.bottom;
                        }

                        region.setPath(p, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
                        PathMeasure pm = new PathMeasure(p, false);
                        cache.put(id, new PathInfo(region, rectF, pm, p));
                    }
                }
                return element;
            }

            @Override
            public <T> void onSvgElementDrawn(@Nullable String id,
                                              @NonNull T element,
                                              @NonNull Canvas canvas,
                                              @Nullable Paint paint) {


            }

        });

        try {
            mSvg.getSharpPicture();
        } catch (Throwable e) {
            //e.printStackTrace();
            //throw new RuntimeException(e);
        }

        GD = new GestureDetector(getContext(), mGestureListener);



        Log.e(TAG, "maxX "+getWidth());

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                initialWidth = getMeasuredWidth();
                initialHeight = getMeasuredHeight();

                scale =  (float)(getWidth() / maxX - 0.08); // Scale on X
                scale = Math.min(scale, (float)(getHeight() / maxY - 0.08)); // Or by Y

                setScale(scale);

                return true;
            }
        });



    }

    public void setScale(float scale) {
        this.scale = scale;
        this.matrix.setScale(scale,scale);

        for(String id: cache.keySet()) {
            PathInfo pi = cache.get(id);
            pi.zoomedPath = new Path(pi.path);
            pi.zoomedPath.transform(matrix);
            pi.recalculate();
        }

        invalidate();
    }

    public void setPan(float x, float y) {
        this.matrix.postTranslate(x,y);

        for(String id: cache.keySet()) {
            PathInfo pi = cache.get(id);
            pi.zoomedPath = new Path(pi.path);
            pi.zoomedPath.transform(matrix);
            pi.recalculate();
        }

        invalidate();
    }




    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            //Log.d("s Tap", "s at: ");
            return true;
        }
        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            //Log.d("Double Tap", "Tapped at: (" + x + "," + y + ")");

            return true;
        }
    }


    boolean firstTouch = false;
    long time = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == event.ACTION_DOWN) {
            if (firstTouch && (System.currentTimeMillis() - time) <= 300) {
                //do stuff here for double tap
                Log.e("** DOUBLE TAP**", " second tap ");


                /*
                for(String id: cache.keySet()) {
                    PathInfo pi = cache.get(id);

                    if(pi.region.contains((int)event.getX(), (int)event.getY())) {
                        pi.selected = !pi.selected;
                    }
                }

                invalidate();
                */

                firstTouch = false;

            } else {
                firstTouch = true;
                time = System.currentTimeMillis();
                Log.e("** SINGLE  TAP**", " First Tap time  " + time);

                for(String id: cache.keySet()) {
                    PathInfo pi = cache.get(id);

                    if(pi.region.contains((int)event.getX(), (int)event.getY())) {

                        // Deselect others.
                        for(PathInfo ip: cache.values()) {
                            ip.selected = false;
                        }

                        pi.selected = !pi.selected;
                    }
                }

                invalidate();



                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(touched != null) {
            Paint p = new Paint();
            p.setStrokeWidth(4f);
            p.setColor(Color.WHITE);
            canvas.drawPoint(touched.x, touched.y, p);
        }

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);

        for(String id: cache.keySet()) {
            PathInfo pi = cache.get(id);
            canvas.drawPath(pi.zoomedPath, paint);

            if(pi.selected) {
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(pi.zoomedPath, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.GRAY);
                canvas.drawPath(pi.zoomedPath, paint);
                paint.setColor(Color.WHITE);

            }
        }


    }

    public static class PathInfo {
        public Region region;
        public RectF rectf;
        public PathMeasure pathMeasure;
        public Path path;
        public Path zoomedPath;
        public boolean selected;

        public PathInfo(Region region, RectF rectf, PathMeasure pathMeasure, Path path) {
            this.region = region;
            this.rectf = rectf;
            this.pathMeasure = pathMeasure;
            this.path = path;
            this.zoomedPath = path;
        }

        public void recalculate() {
            rectf = new RectF();
            zoomedPath.computeBounds(rectf, true);
            region = new Region();
            region.setPath(zoomedPath, new Region((int) rectf.left, (int) rectf.top, (int) rectf.right, (int) rectf.bottom));
        }
    }

}
