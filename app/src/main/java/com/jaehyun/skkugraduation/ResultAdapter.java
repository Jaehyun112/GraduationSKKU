package com.jaehyun.skkugraduation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jaehyun.skkugraduation.CameraTensorflow.GuideItem;
import com.jaehyun.skkugraduation.Utils.DpToPxConverter;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private Context context;

    private DpToPxConverter dpToPxConverter;
    private int animateSize = 0;

    private ItemClick itemClick;

    public interface ItemClick {
        public void onClick(View view, int position);
    }

    public void setItemClick(ResultAdapter.ItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public ResultAdapter(Context context) {
        this.context = context;
        dpToPxConverter = new DpToPxConverter(context);
        //inspectItem = new InspectItem(context);
    }

    private ResultAdapter.ItemFinish itemFinish;

    public interface ItemFinish {
        public void onFinish();
    }

    public void setItemFinish(ResultAdapter.ItemFinish itemFinish) {
        this.itemFinish = itemFinish;
    }

    public ArrayList<String> titles = new ArrayList<>();


    private int containerWidth = 0;
    private int itemSize = 0;

    @NonNull
    @Override
    public ResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.main_activity_result_item, viewGroup, false);
        ResultAdapter.ViewHolder view = new ResultAdapter.ViewHolder(v);

        containerWidth = viewGroup.getMeasuredWidth();
        itemSize = (containerWidth/2)-(dpToPxConverter.convert(10)*4);

        resourceInit();

        return view;
    }

    private void resourceInit() {
        animateSize = dpToPxConverter.convert(5);
    }

    @Override
    public void onBindViewHolder(@NonNull final ResultAdapter.ViewHolder viewHolder, int position) {
        ViewGroup.LayoutParams layoutParams = viewHolder.imgContainer.getLayoutParams();
        layoutParams.width = itemSize;
        layoutParams.height = itemSize;
        viewHolder.imgContainer.setLayoutParams(layoutParams);

        Glide.with(context).load(GuideItem.cropBitmapList.get(position)).into(viewHolder.imgView).waitForLayout();
        viewHolder.titleView.setText(titles.get(position));
        if(position %2 == 0){
            viewHolder.imgContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemClick != null);
                    itemClick.onClick(v,viewHolder.getAdapterPosition());
                }
            });
        }else{
            viewHolder.xImage.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return titles.size();
        //return GuideItem.cropBitmapList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgView;
        private TextView titleView;
        private ImageView xImage;

        private RelativeLayout imgContainer;

        public ViewHolder(@NonNull View view) {
            super(view);

            imgContainer = view.findViewById(R.id.main_activity_result_item_image_container);
            imgView = view.findViewById(R.id.mian_activity_result_item_image);
            titleView = view.findViewById(R.id.mian_activity_result_item_title);
            xImage = view.findViewById(R.id.main_activity_result_item_x);
        }
    }
}