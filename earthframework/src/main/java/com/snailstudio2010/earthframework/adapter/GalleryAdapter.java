package com.snailstudio2010.earthframework.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snailstudio2010.earthframework.R;
import com.snailstudio2010.earthframework.entity.ArticlePoint;
import com.snailstudio2010.earthframework.gallery.GalleryView;
import com.snailstudio2010.libutils.ArrayUtils;
import com.snailstudio2010.libutils.DisplayUtils;
import com.snailstudio2010.libutils.NotNull;

import java.util.List;

public class GalleryAdapter extends PagerAdapter {

    private Context mContext;
    private ViewPager mVp;
    private List<ArticlePoint> mList;
    private LayoutInflater mInflater;

    private View[] mViews;
    private Vibrator mVibrator;

    private GalleryView.OnGalleryListener mOnGalleryListener;

    public GalleryAdapter(Context context, ViewPager vp, List<ArticlePoint> list) {
        this.mContext = context;
        this.mVp = vp;
        this.mList = list;
        mInflater = LayoutInflater.from(context);
        mViews = new View[list.size()];
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setOnItemSelectListener(GalleryView.OnGalleryListener listener) {
        mOnGalleryListener = listener;
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(@NotNull View view, @NotNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
        container.removeView((View) object);
    }

    @NotNull
    @Override
    public Object instantiateItem(@NotNull ViewGroup container, final int position) {
        ArticlePoint item = mList.get(position);
        View view = mInflater.inflate(R.layout.gallery_item, null);
        View rlContainer = view.findViewById(R.id.rl_container);
        rlContainer.setOnClickListener(v -> {
            if (mOnGalleryListener != null)
                mOnGalleryListener.onGalleryItemClick(position, mList.get(position));
        });
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(item.info);
//        TextView tvLocation = view.findViewById(R.id.tv_location);
//        tvLocation.setText(item.getFields().getLocation());
        ImageView ivClose = view.findViewById(R.id.iv_close);
        ivClose.setVisibility(position == mVp.getCurrentItem() ? View.VISIBLE : View.GONE);
        ivClose.setOnClickListener(v -> {
            if (mOnGalleryListener != null) mOnGalleryListener.onGalleryClose();
        });
        ImageView ivAvatar = view.findViewById(R.id.iv_avatar);
        if (!ArrayUtils.isEmpty(mList) && mList.size() > 1) {
            LinearLayout.LayoutParams rll = (LinearLayout.LayoutParams) ivAvatar.getLayoutParams();
            rll.width = LinearLayout.LayoutParams.MATCH_PARENT;
            rll.height = (int) DisplayUtils.dip2px(mContext, 152);
            ivAvatar.setLayoutParams(rll);
        }
//        if (!TextUtils.isEmpty(item.getPhoto())) {
//            // "http://cdn.national-space.com/Fhz22vfh7EhUO3igOggwdn2UZqu-"
//            ImageUtils.loadImage(mContext, item.getPhoto(), ivAvatar, R.mipmap.toolbar, true);
//        } else {
//            new Handler().postDelayed(() ->
//                    ImageUtils.loadImage(mContext, item.getPhoto(),
//                            ivAvatar, R.mipmap.toolbar, true), 100);
//        }

        view.setTag(position);
        container.addView(view);
        mViews[position] = view;
        return view;
    }

    public void onPageSelected(int position) {
        mVibrator.vibrate(50);
        if (mViews == null) return;

        for (int i = 0; i < mViews.length; i++) {
            if (mViews[i] == null) continue;
            ImageView ivClose = mViews[i].findViewById(R.id.iv_close);
            ivClose.setVisibility(i == position ? View.VISIBLE : View.GONE);
        }
    }

    public void notifyDataSetChanged() {
        mViews = new View[mList.size()];
        super.notifyDataSetChanged();
    }
}