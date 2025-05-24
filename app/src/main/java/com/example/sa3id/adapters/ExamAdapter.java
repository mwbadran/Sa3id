package com.example.sa3id.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sa3id.R;
import com.example.sa3id.models.Exam;
import java.util.ArrayList;
import java.util.List;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {
    private List<Exam> exams;
    private String extraTimeType;

    public ExamAdapter(String extraTimeType) {
        this.exams = new ArrayList<>();
        this.extraTimeType = extraTimeType;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Exam exam = exams.get(position);
        holder.bind(exam, extraTimeType);
    }

    @Override
    public int getItemCount() {
        return exams.size();
    }

    public void setExams(List<Exam> exams) {
        this.exams = exams;
        notifyDataSetChanged();
    }

    public void setExtraTimeType(String extraTimeType) {
        this.extraTimeType = extraTimeType;
        notifyDataSetChanged();
    }

    public List<Exam> getExams() {
        return exams;
    }

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        private final TextView examNameText;
        private final TextView examDateText;
        private final TextView examTimeText;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            examNameText = itemView.findViewById(R.id.textExamName);
            examDateText = itemView.findViewById(R.id.textExamDate);
            examTimeText = itemView.findViewById(R.id.textExamTime);
        }

        public void bind(Exam exam, String extraTimeType) {
            examNameText.setText(exam.getExamName());
            examDateText.setText(exam.getDate());
            String timeText = String.format("Start: %s | End: %s",
                exam.getStartHour(),
                exam.getEndTimeByExtraTime(extraTimeType));
            examTimeText.setText(timeText);
        }
    }
} 