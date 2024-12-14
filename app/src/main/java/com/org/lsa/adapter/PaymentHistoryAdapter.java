package com.org.lsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.org.lsa.R;

import java.util.List;
import java.util.Map;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder> {
    private final List<Map<String, Object>> paymentHistoryList;

    public PaymentHistoryAdapter(List<Map<String, Object>> paymentHistoryList) {
        this.paymentHistoryList = paymentHistoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.payment_history_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> record = paymentHistoryList.get(position);
        holder.tvDatePaid.setText(record.get("DatePaid").toString().split("T")[0]);
        holder.tvMonthsCovered.setText(record.get("Months_Covered").toString().split("\\.")[0]);
        holder.tvAmountPaid.setText(record.get("AmountPaid").toString());
        holder.tvPaymentDetails.setText(record.get("PaymentDetails").toString());
    }

    @Override
    public int getItemCount() {
        return paymentHistoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDatePaid, tvMonthsCovered, tvAmountPaid, tvPaymentDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDatePaid = itemView.findViewById(R.id.tvDatePaid);
            tvMonthsCovered = itemView.findViewById(R.id.tvMonthsCovered);
            tvAmountPaid = itemView.findViewById(R.id.tvAmountPaid);
            tvPaymentDetails = itemView.findViewById(R.id.tvPaymentDetails);
        }
    }
}

