package com.valohyd.copilotemaster.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.models.Contact;
import com.valohyd.copilotemaster.sqlite.ContactsBDD;
import com.valohyd.copilotemaster.utils.MultiSelectionAdapter;

/**
 * Classe representant les contacts rapides
 * 
 * @author parodi
 * 
 */
public class ContactFragment extends SherlockFragment {

	public static final String TAG_PREF_CONTACT = "contacts",
			TAG_NAME_PREF = "pref_file";; // Nom des prefs
	SharedPreferences sharedPrefs; // Prefs
	Editor edit; // Editeur des prefs

	ContactsBDD bdd;

	MultiSelectionAdapter mAdapter;

	ArrayList<Contact> contacts = new ArrayList<Contact>(); // Les contacts

	private final int PICK_CONTACT = 69; // Le request code pour choisir un
											// contact

	private Button addContactButton, removeContactsButton; // Boutons actions
	private ListView list; // Liste des contacts

	private View mainView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.contact_layout, container, false);

		// PREFERENCES
		sharedPrefs = getActivity().getSharedPreferences(TAG_NAME_PREF,
				Activity.MODE_PRIVATE);
		edit = sharedPrefs.edit();
		bdd = new ContactsBDD(getActivity());

		// INITIALISATION DES CONTACTS
		initContacts();

		list = (ListView) mainView.findViewById(R.id.contactList);
		mAdapter = new MultiSelectionAdapter(getActivity(), contacts);
		list.setAdapter(mAdapter);

		// AJOUT D'UN CONTACT
		addContactButton = (Button) mainView
				.findViewById(R.id.addContactButton);
		addContactButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// On va ajouter un contact du repertoire du tel
				readcontact();
			}
		});

		// SUPPRESSION D'UN CONTACT
		removeContactsButton = (Button) mainView
				.findViewById(R.id.removeContactButton);
		removeContactsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mAdapter != null) {
					// On supprime la selection
					ArrayList<Contact> mArraySelected = mAdapter
							.getCheckedItems();

					for (Contact selected : mArraySelected) {
						removeContact(selected);
					}
					refreshAdapter();
				}
			}
		});
		return mainView;
	}

	/**
	 * Initialise la liste des contacts avec les preferences
	 */
	private void initContacts() {
		bdd.open();
		contacts = bdd.getAllContacts();
		Log.d("INIT", contacts.toString());
		bdd.close();
		// ArrayList<String> contact_init = new ArrayList<String>(
		// sharedPrefs.getStringSet(TAG_PREF_CONTACT,
		// new HashSet<String>()));
		//
		// for (String s : contact_init) {
		// String[] contact = s.split(":");
		// contacts.add(contact[0] + ":" + contact[1]);
		// }
	}

	/**
	 * Supprime un contact des preferences
	 * 
	 * @param contact
	 */
	private void removeContact(Contact contact) {
		if (contacts.contains(contact)) {
			Log.d("REMOVE", contact.toString());
			bdd.open();
			bdd.removeContactWithID(contact.getId());
			contacts.remove(contact);
			bdd.close();
			// contacts.remove(contact);
			// savePreferences();
			// Toast.makeText(getActivity(), "Contact supprimé : " + contact,
			// Toast.LENGTH_SHORT).show();
			// Log.d("CONTACTS", contacts.toString());
		}
	}

	private boolean isContactExits(String name) {
		for (Contact c : contacts) {
			if (c.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Ajoute un contact aux preferences
	 * 
	 * @param name
	 * @param number
	 */
	private void addContact(String name, String number) {
		Contact c = new Contact(name, number);

		if (isContactExits(name)) {
			Toast.makeText(getActivity(), "Contact existant !",
					Toast.LENGTH_SHORT).show();
		} else {
			Log.d("ADD", c.toString());
			bdd.open();
			bdd.insertContact(c);
			contacts.add(c);
			bdd.close();
		}
		// if (contacts.contains(name + ":" + number)) {
		// Toast.makeText(getActivity(), "Contact existant !",
		// Toast.LENGTH_SHORT).show();
		// } else {
		// contacts.add(name + ":" + number);
		// savePreferences();
		// Toast.makeText(getActivity(), "Contact " + name + " ajouté !",
		// Toast.LENGTH_SHORT).show();
		// Log.d("CONTACTS", contacts.toString());
		// }
	}

	//
	// /**
	// * Sauvegarde les préférences
	// */
	// private void savePreferences() {
	// edit.putStringSet("contacts", new HashSet<String>(contacts));
	// edit.commit();
	// }

	/**
	 * Actualise l'adapter
	 */
	private void refreshAdapter() {
		Log.d("REFRESH", contacts.toString());
		mAdapter.setList(contacts);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * Lecture du répertoire du tel
	 */
	public void readcontact() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				Contacts.Phones.CONTENT_URI);
		startActivityForResult(intent, PICK_CONTACT);

	}

	/**
	 * En attente d'un resultat (ici attente du choix d'un contact depuis le
	 * repertoire)
	 */
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		// Si l'utilisateur vient du choix d'un contact
		case (PICK_CONTACT):
			// Si tout s'est bien déroulé
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = getActivity().managedQuery(contactData, null, null,
						null, null);
				if (c.moveToFirst()) {
					// On recupère les infos du contact
					String nom = c.getString(c
							.getColumnIndexOrThrow(People.NAME));
					String num = c.getString(c
							.getColumnIndexOrThrow(People.NUMBER));
					addContact(nom, num); // On l'ajoute
					refreshAdapter(); // On refresh
				}
			}
			break;
		}
	}
}
