package com.example.numberbook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<Contact> contacts;

    public ContactAdapter(List<Contact> contacts) {
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);

        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhone());

        // Génération des initiales
        String name = contact.getName() != null ? contact.getName().trim() : "?";
        String initials;

        if (!name.isEmpty()) {
            int spaceIndex = name.indexOf(" ");
            if (spaceIndex > 0 && spaceIndex < name.length() - 1) {
                initials = String.valueOf(name.charAt(0)).toUpperCase()
                        + String.valueOf(name.charAt(spaceIndex + 1)).toUpperCase();
            } else {
                initials = String.valueOf(name.charAt(0)).toUpperCase();
            }
        } else {
            initials = "?";
        }

        holder.tvAvatar.setText(initials);
    }

    @Override
    public int getItemCount() {
        return contacts != null ? contacts.size() : 0;
    }

    public void updateData(List<Contact> newContacts) {
        this.contacts = newContacts;
        notifyDataSetChanged();
    }

    // Make this class public instead of static (or keep static but make it public)
    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAvatar;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
        }
    }
}