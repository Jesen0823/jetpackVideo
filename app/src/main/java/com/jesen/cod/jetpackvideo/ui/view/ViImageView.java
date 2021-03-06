package com.jesen.cod.jetpackvideo.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.jesen.cod.libcommon.utils.PixUtils;
import com.jesen.cod.libcommon.view.ViewHelper;

import org.jetbrains.annotations.NotNull;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ViImageView extends AppCompatImageView {
    public ViImageView(@NonNull Context context) {
        super(context);
    }

    public ViImageView(@NonNull Context context, @Nullable  AttributeSet attrs) {
        super(context, attrs);
    }

    public ViImageView(@NonNull Context context, @Nullable  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewHelper.setViewOutline(this, attrs,defStyleAttr,0);
    }

    public void setImageUrl(String imageUrl) {
        setImageUrl(this, imageUrl, false);
    }

    @BindingAdapter(value = {"image_url", "isCircle"})
    public static void setImageUrl(ViImageView view, String imageUrl, boolean isCircle) {
        view.setImageUrl(view, imageUrl, isCircle, 0);
    }

    @BindingAdapter(value = {"image_url", "isCircle", "radius"}, requireAll = false)
    public static void setImageUrl(ViImageView view, String imageUrl, boolean isCircle, int radius) {
        RequestBuilder<Drawable> builder = Glide.with(view).load(imageUrl);
        if (isCircle) {
            builder.transform(new CircleCrop());
        } else if (radius > 0) {
            builder.transform(new RoundedCornersTransformation(PixUtils.dp2px(radius), 0));
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams != null && layoutParams.width > 0 && layoutParams.height > 0) {
            builder.override(layoutParams.width, layoutParams.height);
        }
        builder.into(view);
    }

    public void bindData(String imageUrl, int widthPx, int heightPx, int marginLeft) {
        bindData(imageUrl, widthPx, heightPx, marginLeft, PixUtils.getScreenWidth(), PixUtils.getScreenWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    public void bindData(String imageUrl, int widthPx, int heightPx, int marginLeft, int maxWidth,
                         int maxHeight) {
        if (widthPx <= 0 || heightPx <= 0) {
            Glide.with(this).load(imageUrl).into(new Target<Drawable>() {
                @Override
                public void onLoadStarted(@Nullable  Drawable placeholder) {

                }

                @Override
                public void onLoadFailed(@Nullable  Drawable errorDrawable) {

                }

                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable  Transition<? super Drawable> transition) {
                    int height = resource.getIntrinsicHeight();
                    int width = resource.getIntrinsicWidth();
                    setSize(width, height, marginLeft, maxWidth, maxHeight);

                    setImageDrawable(resource);
                }

                @Override
                public void onLoadCleared(@Nullable  Drawable placeholder) {
                }

                @Override
                public void getSize(@NonNull SizeReadyCallback cb) {
                }

                @Override
                public void removeCallback(@NonNull SizeReadyCallback cb) {
                }

                @Override
                public void setRequest(@Nullable  Request request) {
                }

                @Nullable
                
                @Override
                public Request getRequest() {
                    return null;
                }

                @Override
                public void onStart() {
                }

                @Override
                public void onStop() {
                }

                @Override
                public void onDestroy() {
                }
            });
            return;
        }
        setSize(widthPx, heightPx, marginLeft, maxWidth, maxHeight);
        setImageUrl(this, imageUrl, false);
    }

    private void setSize(int width, int height, int marginLeft, int maxWidth, int maxHeight) {
        int finalWidth, finalHeight;
        if (width > height) {
            finalWidth = maxWidth;
            finalHeight = (int) (height / (width * 1.f / finalWidth));
        } else {
            finalHeight = maxHeight;
            finalWidth = (int) (width / (height * 1.f / finalHeight));
        }

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = finalWidth;
        params.height = finalHeight;
        if (params instanceof FrameLayout.LayoutParams) {
            ((FrameLayout.LayoutParams) params).leftMargin = height > width ? PixUtils.dp2px(marginLeft) : 0;
        } else if (params instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) params).leftMargin = height > width ? PixUtils.dp2px(marginLeft) : 0;
        }
        setLayoutParams(params);
    }

    @BindingAdapter(value = {"blur_url", "radius"})
    public static void setBlurImageUrl(ImageView imageView, String blurUrl, int radius) {
        Glide.with(imageView).load(blurUrl).override(radius)
                .transform(new BlurTransformation())
                .dontAnimate()
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imageView.setBackground(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable  Drawable placeholder) {

                    }
                });
    }
}
