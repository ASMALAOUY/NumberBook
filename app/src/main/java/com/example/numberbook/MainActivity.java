package com.example.numberbook;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnLoadContacts, btnSyncContacts, btnSearch;
    private EditText etKeyword;
    private RecyclerView recyclerViewContacts;
    private ContactAdapter adapter;
    private List<Contact> contactList = new ArrayList<>();
    private List<Contact> originalContactList = new ArrayList<>();
    private ContactApi contactApi;
    private FloatingActionButton fabAddContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLoadContacts = findViewById(R.id.btnLoadContacts);
        btnSyncContacts = findViewById(R.id.btnSyncContacts);
        btnSearch = findViewById(R.id.btnSearch);
        etKeyword = findViewById(R.id.etKeyword);
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        fabAddContact = findViewById(R.id.fabAddContact);

        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(contactList);
        recyclerViewContacts.setAdapter(adapter);

        contactApi = RetrofitClient.getClient().create(ContactApi.class);

        btnLoadContacts.setOnClickListener(v -> checkPermissionAndLoadContacts());
        btnSyncContacts.setOnClickListener(v -> syncContactsToServer());
        btnSearch.setOnClickListener(v -> searchContactsLocal());

        // Bouton flottant pour ajouter un contact
        if (fabAddContact != null) {
            fabAddContact.setOnClickListener(v -> showAddContactDialog());
        }

        // Test de connexion API au démarrage
        testApiConnection();
    }

    // ── Permission Lecture ──────────────────────────────────────────────────────────
    private void checkPermissionAndLoadContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private final androidx.activity.result.ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadContacts();
                } else {
                    Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
                }
            });

    // ── Chargement des contacts ──────────────────────────────────────────────
    private void loadContacts() {
        contactList.clear();
        originalContactList.clear();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission non accordée", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER},
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String phone = cursor.getString(1);
                phone = phone.replaceAll("[\\s\\-\\+\\(\\)]", "");
                contactList.add(new Contact(name, phone));
            }
            cursor.close();

            originalContactList.addAll(contactList);
            adapter.updateData(contactList);

            TextView tvContactCount = findViewById(R.id.tvContactCount);
            tvContactCount.setText("CONTACTS (" + contactList.size() + ")");

            // Mettre à jour le subtitle de la toolbar
            com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setSubtitle(contactList.size() + " contact(s) chargé(s)");

            Toast.makeText(this, contactList.size() + " contacts chargés", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Erreur lors du chargement", Toast.LENGTH_SHORT).show();
        }
    }

    // ── RECHERCHE LOCALE ──────────────────────────────────────────────────────────
    private void searchContactsLocal() {
        String keyword = etKeyword.getText().toString().trim().toLowerCase();

        if (keyword.isEmpty()) {
            adapter.updateData(originalContactList);
            Toast.makeText(this, "Affichage de tous les contacts", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Contact> filteredList = new ArrayList<>();
        for (Contact contact : originalContactList) {
            if (contact.getName().toLowerCase().contains(keyword) ||
                    contact.getPhone().contains(keyword)) {
                filteredList.add(contact);
            }
        }

        adapter.updateData(filteredList);

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Aucun contact trouvé pour: " + keyword, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, filteredList.size() + " contact(s) trouvé(s)", Toast.LENGTH_SHORT).show();
        }
    }

    // ── AJOUTER UN CONTACT MANUELLEMENT ──────────────────────────────────────────
    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter un contact");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        final EditText inputName = new EditText(this);
        inputName.setHint("Nom complet");
        inputName.setPadding(0, 10, 0, 10);
        layout.addView(inputName);

        final EditText inputPhone = new EditText(this);
        inputPhone.setHint("Numéro de téléphone");
        inputPhone.setPadding(0, 20, 0, 10);
        layout.addView(inputPhone);

        TextView spacer = new TextView(this);
        spacer.setHeight(20);
        layout.addView(spacer);

        builder.setView(layout);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();

            if (!name.isEmpty() && !phone.isEmpty()) {
                addContactToDevice(name, phone);
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Ajouter un contact directement dans le carnet d'adresses
    private void addContactToDevice(String name, String phone) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestWritePermissionLauncher.launch(Manifest.permission.WRITE_CONTACTS);
            return;
        }

        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build());

            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());

            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

            Toast.makeText(this, "Contact '" + name + "' ajouté avec succès", Toast.LENGTH_LONG).show();
            loadContacts();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Permission pour écrire les contacts
    private final androidx.activity.result.ActivityResultLauncher<String> requestWritePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Permission accordée, réessayez", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
                }
            });

    // ── SYNCHRONISATION VERS SERVEUR ────────────────────────────────────────
    private void syncContactsToServer() {
        if (originalContactList.isEmpty()) {
            Toast.makeText(this, "Aucun contact à synchroniser", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Synchronisation de " + originalContactList.size() + " contacts...", Toast.LENGTH_SHORT).show();

        for (Contact contact : originalContactList) {
            contactApi.insertContact(contact).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse> call,
                                       @NonNull Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("MainActivity", "Contact synchronisé: " + contact.getName());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                    Log.e("MainActivity", "Erreur réseau: " + t.getMessage());
                }
            });
        }
        Toast.makeText(this, "Synchronisation terminée", Toast.LENGTH_SHORT).show();
    }

    // ── TEST CONNEXION API ────────────────────────────────────────────────
    private void testApiConnection() {
        contactApi.getAllContacts().enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                if (response.isSuccessful()) {
                    int count = response.body() != null ? response.body().size() : 0;
                    Log.d("MainActivity", "API OK: " + count + " contacts sur serveur");
                } else {
                    Log.e("MainActivity", "Erreur API: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                Log.e("MainActivity", "Connexion échouée: " + t.getMessage());
            }
        });
    }
}