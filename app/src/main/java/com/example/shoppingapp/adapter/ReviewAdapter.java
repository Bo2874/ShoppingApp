package com.example.shoppingapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingapp.R;
import com.example.shoppingapp.database.entity.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private final List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvUserName.setText(review.getUserName());
        holder.tvDate.setText(review.getDate());
        holder.tvComment.setText(review.getComment());
        holder.ratingBar.setRating(review.getRating());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvDate, tvComment;
        RatingBar ratingBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvReviewUserName);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
            tvComment = itemView.findViewById(R.id.tvReviewComment);
            ratingBar = itemView.findViewById(R.id.rbReviewItem);
        }
    }
}
