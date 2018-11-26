package com.sdzshn3.android.news247.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.sdzshn3.android.news247.R;
import com.sdzshn3.android.news247.Retrofit.Results;
import com.squareup.picasso.Picasso;


import butterknife.BindView;
import butterknife.ButterKnife;


public class NewsFeedAdapter extends ListAdapter<Results, NewsFeedAdapter.ViewHolder> {

    private Context mContext;

    public NewsFeedAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Results> DIFF_CALLBACK = new DiffUtil.ItemCallback<Results>() {
        @Override
        public boolean areItemsTheSame(@NonNull Results news, @NonNull Results t1) {
            return news.getWebTitle().equals(t1.getWebTitle());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Results news, @NonNull Results t1) {
            return news.getWebTitle().equals(t1.getWebTitle()) &&
                    news.getSectionName().equals(t1.getSectionName()) &&
                    news.getWebUrl().equals(t1.getWebUrl()) &&
                    news.getWebPublicationDate().equals(t1.getWebPublicationDate());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        mContext = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Results currentNews = getItem(position);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        //showArticleImages boolean decides whether to show article images or not
        boolean showArticleImages = sharedPreferences.getBoolean(
                mContext.getString(R.string.show_article_images_key),
                Boolean.parseBoolean(mContext.getString(R.string.default_show_article_images)));

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        holder.shareButton.setBackground(VectorDrawableCompat.create(mContext.getResources(), R.drawable.ic_share, null));
        //Article URL sharing intent
        holder.shareButton.setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, currentNews.getWebUrl());
            sendIntent.setType("text/plain");
            mContext.startActivity(Intent.createChooser(sendIntent, mContext.getString(R.string.share_article_link_hint)));
        });

        String thumbnailUrl = currentNews.getFields().getThumbnail();
        if (thumbnailUrl != null && showArticleImages) {
            if (!thumbnailUrl.isEmpty()) {
                Picasso.get().load(thumbnailUrl).resize(272, 153).into(holder.thumbnailView);
            }
        } else {
            holder.thumbnailView.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.GONE);
        }
        String contributorImageUrl;
        try {
            contributorImageUrl = currentNews.getTags()[0].getBylineImageUrl();
        } catch (ArrayIndexOutOfBoundsException e) {
            contributorImageUrl = null;
        }

        if(contributorImageUrl != null){
            if(!contributorImageUrl.isEmpty()) {
                Picasso.get().load(contributorImageUrl).into(holder.contributorImage);
            }
            else {
                holder.contributorImage.setVisibility(View.GONE);
            }
        }else {
            holder.contributorImage.setVisibility(View.GONE);
        }

        Typeface semiBoldText = Typeface.createFromAsset(mContext.getAssets(), "GoogleSans-Medium.ttf");
        Typeface regularText = Typeface.createFromAsset(mContext.getAssets(), "GoogleSans-Regular.ttf");

        if(currentNews.getSectionName() != null) {
            holder.sectionView.setTypeface(regularText);
            holder.sectionView.setText(currentNews.getSectionName());
        }

        holder.titleView.setTypeface(semiBoldText);
        holder.titleView.setText(currentNews.getWebTitle());

        String authorName;
        try {
            authorName = currentNews.getTags()[0].getWebTitle();
        } catch (ArrayIndexOutOfBoundsException e) {
            authorName = null;
        }
        if (authorName == null) {
            holder.authorNameView.setVisibility(View.GONE);
            holder.separator.setVisibility(View.GONE);
        } else {
            if (!authorName.equals("")) {
                holder.authorNameView.setText(authorName);
            }
            holder.separator.setVisibility(View.VISIBLE);
        }

        if(currentNews.getWebPublicationDate() != null) {
            holder.publishedAtView.setText(currentNews.getWebPublicationDate().replace("T", " at ").replace("Z", " "));
        }
    }

    @Override
    public Results getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.thumbnailImageView)
        ImageView thumbnailView;
        @BindView(R.id.sectionTextView)
        TextView sectionView;
        @BindView(R.id.titleTextView)
        TextView titleView;
        @BindView(R.id.authorNameTextView)
        TextView authorNameView;
        @BindView(R.id.separator)
        TextView separator;
        @BindView(R.id.publishedAtTextView)
        TextView publishedAtView;
        @BindView(R.id.share_button)
        Button shareButton;
        @BindView(R.id.contributor_image)
        RoundedImageView contributorImage;
        @BindView(R.id.image_card_view)
        CardView cardView;

        ViewHolder(View listItemView) {
            super(listItemView);
            ButterKnife.bind(this, listItemView);
        }
    }


}
