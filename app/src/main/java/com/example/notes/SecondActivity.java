package com.example.notes;

import static android.content.Context.MODE_PRIVATE;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    private Button New;
    private ListView lv;

    private List<Note> notes = new ArrayList<>();
    private List<Note> filteredNotes = new ArrayList<>();
    private ArrayAdapter<Note> adapter;
    private ActivityResultLauncher<Intent> addNoteLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Activity result launcher
        addNoteLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                                String title = result.getData().getStringExtra("NOTE_TITLE");
                                String body = result.getData().getStringExtra("NOTE_BODY");
                                int index = result.getData().getIntExtra("NOTE_INDEX", -1);

                                if (title != null) {
                                    if (index == -1) {
                                        notes.add(new Note(title, body));
                                    } else {
                                        notes.get(index).setTitle(title);
                                        notes.get(index).setBody(body);
                                    }

                                    filteredNotes.clear();
                                    filteredNotes.addAll(notes);
                                    adapter.notifyDataSetChanged();
                                    saveNotes();
                                }
                            }
                        });

        New = findViewById(R.id.button);
        lv = findViewById(R.id.taskList);
        registerForContextMenu(lv);

        SearchView s = findViewById(R.id.search);

        int searchTextId = getResources()
                .getIdentifier("android:id/search_src_text", null, null);

        TextView searchText = s.findViewById(searchTextId);
        searchText.setTextColor(Color.WHITE);
        searchText.setHintTextColor(Color.parseColor("#BB86FC"));


        s.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filteredNotes.clear();

                if (newText.isEmpty()) {
                    filteredNotes.addAll(notes);
                } else {
                    for (Note note : notes) {
                        if (note.getTitle().toLowerCase().contains(newText.toLowerCase()) ||
                                note.getBody().toLowerCase().contains(newText.toLowerCase())) {

                            filteredNotes.add(note);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                return true;
            }
        });

        //shows only title via Note.toString()
        adapter = new ArrayAdapter<>(
                this,
                R.layout.item_note,
                R.id.text1,
                filteredNotes
        );

        lv.setAdapter(adapter);

        loadNotes();
        filteredNotes.clear();
        filteredNotes.addAll(notes);
        adapter.notifyDataSetChanged();


        lv.setOnItemClickListener((parent, view, position, id) -> {
            Note clickedNote = filteredNotes.get(position);
            int realIndex = notes.indexOf(clickedNote);

            Intent intent = new Intent(SecondActivity.this, ThirdActivity.class);
            intent.putExtra("NOTE_TITLE", clickedNote.getTitle());
            intent.putExtra("NOTE_BODY", clickedNote.getBody());
            intent.putExtra("NOTE_INDEX", realIndex);

            addNoteLauncher.launch(intent);
        });


        New.setOnClickListener(v -> {
            Intent intent = new Intent(SecondActivity.this, ThirdActivity.class);
            addNoteLauncher.launch(intent);
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.note_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        Toast.makeText(this, "Context menu clicked", Toast.LENGTH_SHORT).show();


        int position = info.position;

        Note selectedNote = filteredNotes.get(position);
        int realIndex = notes.indexOf(selectedNote);

        if (item.getItemId() == R.id.menu_edit) {

            Intent intent = new Intent(SecondActivity.this, ThirdActivity.class);

            intent.putExtra("NOTE_TITLE", selectedNote.getTitle());
            intent.putExtra("NOTE_BODY", selectedNote.getBody());
            intent.putExtra("NOTE_INDEX", realIndex);

            addNoteLauncher.launch(intent);
        }
        else if (item.getItemId() == R.id.menu_delete) {

            notes.remove(realIndex);
            filteredNotes.remove(position);

            adapter.notifyDataSetChanged();
            saveNotes();

            return true;
        }

        return super.onContextItemSelected(item);
    }


    private void saveNotes() {
        SharedPreferences prefs = getSharedPreferences("notes_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(notes);

        editor.putString("notes_list", json);
        editor.apply();
    }

    private void loadNotes() {
        SharedPreferences prefs = getSharedPreferences("notes_prefs", MODE_PRIVATE);
        String json = prefs.getString("notes_list", null);

        if (json != null) {

            Gson gson = new Gson();
            Type type = new TypeToken<List<Note>>() {}.getType();
            notes.clear();
            notes.addAll(gson.fromJson(json, type));
        }
    }
}
