package com.pwt.videoschannel.adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pwt.videoschannel.R;
import com.pwt.videoschannel.database.prefs.SharedPref;
import com.pwt.videoschannel.models.Category;
import com.pwt.videoschannel.utils.Constant;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;
import java.util.Random;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.ViewHolder> {

    private List<Category> items;

    Context context;
    private OnItemClickListener mOnItemClickListener;
    SharedPref sharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view, Category obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterCategory(Context context, List<Category> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView categoryName;
        public TextView txtAlphabet;
        public ImageView imgAlphabet;
        public ImageView imgCategory;
        public LinearLayout lytParent;

        public ViewHolder(View v) {
            super(v);
            categoryName = v.findViewById(R.id.txt_label_name);
            txtAlphabet = v.findViewById(R.id.txt_alphabet);
            imgAlphabet = v.findViewById(R.id.img_alphabet);
            imgCategory = v.findViewById(R.id.img_category);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (sharedPref.getCategoryViewType() == Constant.CATEGORY_LIST) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_list, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_grid, parent, false);
        }
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Category c = items.get(position);

        holder.categoryName.setText(c.term);
        holder.txtAlphabet.setText(c.term.substring(0, 1));

        int[] colorArr = {R.color.red, R.color.pink, R.color.purple, R.color.deep_purple, R.color.indigo, R.color.blue, R.color.cyan, R.color.teal, R.color.green, R.color.lime, R.color.orange, R.color.brown, R.color.gray, R.color.blue_gray, R.color.black};
        int rnd = new Random().nextInt(colorArr.length);
        holder.imgAlphabet.setImageResource(colorArr[rnd]);

        if (!c.image.equals("")) {
            Glide.with(context)
                    .load(c.image.replace(" ", "%20"))
                    .transition(withCrossFade())
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_button_transparent)
                    .centerCrop()
                    .into(holder.imgCategory);
            holder.imgCategory.setVisibility(View.VISIBLE);
        } else {
            holder.imgCategory.setVisibility(View.GONE);
        }

        holder.lytParent.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, c, position);
            }
        });
    }

    public void setListData(List<Category> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void resetListData() {
        this.items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}