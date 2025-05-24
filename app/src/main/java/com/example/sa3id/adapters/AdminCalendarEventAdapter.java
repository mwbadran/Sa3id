package com.example.sa3id.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sa3id.R;
import com.example.sa3id.models.CalendarEvent;

import java.util.ArrayList;
import java.util.List;

public class AdminCalendarEventAdapter extends RecyclerView.Adapter<AdminCalendarEventAdapter.EventViewHolder> {
    
    public interface EventActionListener {
        void onEditEvent(CalendarEvent event);
        void onDeleteEvent(CalendarEvent event);
    }
    
    private List<CalendarEvent> events;
    private EventActionListener listener;
    
    public AdminCalendarEventAdapter(EventActionListener listener) {
        this.events = new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_calendar_event, parent, false);
        return new EventViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(events.get(position));
    }
    
    @Override
    public int getItemCount() {
        return events.size();
    }
    
    public void setEvents(List<CalendarEvent> events) {
        this.events = events;
        notifyDataSetChanged();
    }
    
    public List<CalendarEvent> getEvents() {
        return new ArrayList<>(events);
    }
    
    public void filterEvents(String type, String startDate, String endDate) {
        List<CalendarEvent> filteredList = new ArrayList<>();
        
        for (CalendarEvent event : events) {
            boolean typeMatch = type == null || type.isEmpty() || event.getType().equals(type);
            boolean dateMatch = true;
            
            // Add date range filtering logic if needed
            
            if (typeMatch && dateMatch) {
                filteredList.add(event);
            }
        }
        
        setEvents(filteredList);
    }
    
    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView textEventTitle;
        private final TextView textEventDate;
        private final TextView textEventType;
        private final Button buttonEdit;
        private final Button buttonDelete;
        
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textEventTitle = itemView.findViewById(R.id.textEventTitle);
            textEventDate = itemView.findViewById(R.id.textEventDate);
            textEventType = itemView.findViewById(R.id.textEventType);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
        
        public void bind(CalendarEvent event) {
            textEventTitle.setText(event.getTitle());
            textEventDate.setText(event.getDate());
            
            String eventTypeText = "مناسبة";
            if (event.getType().equals("holiday")) {
                eventTypeText = "عطلة";
            }
            textEventType.setText(eventTypeText);
            
            buttonEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditEvent(event);
                }
            });
            
            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteEvent(event);
                }
            });
        }
    }
} 