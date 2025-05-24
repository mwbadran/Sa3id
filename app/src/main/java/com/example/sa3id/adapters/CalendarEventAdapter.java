package com.example.sa3id.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sa3id.R;
import com.example.sa3id.models.Exam;
import com.example.sa3id.models.CalendarEvent;

import java.util.ArrayList;
import java.util.List;

public class CalendarEventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_EXAM = 0;
    private static final int TYPE_EVENT = 1;
    
    private List<Object> items;
    private String extraTimeType;

    public CalendarEventAdapter(String extraTimeType) {
        this.items = new ArrayList<>();
        this.extraTimeType = extraTimeType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_EXAM) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam, parent, false);
            return new ExamViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_event, parent, false);
            return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ExamViewHolder) {
            ((ExamViewHolder) holder).bind((Exam) items.get(position), extraTimeType);
        } else if (holder instanceof EventViewHolder) {
            ((EventViewHolder) holder).bind((CalendarEvent) items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Exam) {
            return TYPE_EXAM;
        } else {
            return TYPE_EVENT;
        }
    }

    public void setItems(List<Object> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setExtraTimeType(String extraTimeType) {
        this.extraTimeType = extraTimeType;
        notifyDataSetChanged();
    }

    public void clearItems() {
        this.items.clear();
        notifyDataSetChanged();
    }

    public void addItem(Object item) {
        this.items.add(item);
        notifyItemInserted(items.size() - 1);
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

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventTitleText;
        private final TextView eventTypeText;
        private final TextView eventDateText;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitleText = itemView.findViewById(R.id.textEventTitle);
            eventTypeText = itemView.findViewById(R.id.textEventType);
            eventDateText = itemView.findViewById(R.id.textEventDate);
        }

        public void bind(CalendarEvent event) {
            eventTitleText.setText(event.getTitle());
            eventTypeText.setText(event.getType());
            eventDateText.setText(event.getDate());
        }
    }
} 