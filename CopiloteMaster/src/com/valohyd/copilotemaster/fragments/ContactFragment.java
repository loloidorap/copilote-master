package com.valohyd.copilotemaster.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.models.Contact;
import com.valohyd.copilotemaster.sqlite.ContactsBDD;
import com.valohyd.copilotemaster.utils.AnalyticsManager;
import com.valohyd.copilotemaster.utils.MultiSelectionAdapter;

/**
 * Classe representant les contacts rapides
 * 
 * @author parodi
 * 
 */
public class ContactFragment extends SherlockFragment {

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
		
		AnalyticsManager.trackScreen(getActivity(),
				AnalyticsManager.KEY_PAGE_CONTACTS);
		AnalyticsManager.dispatch();
		
		mainView = inflater.inflate(R.layout.contact_layout, container, false);

		// BDD
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
	 * permet de dire de redessiner le menu
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		getActivity().supportInvalidateOptionsMenu();
	}

	/**
	 * Initialise la liste des contacts avec les preferences
	 */
	private void initContacts() {
		bdd.open();
		contacts = bdd.getAllContacts();
		bdd.close();
		if (contacts.isEmpty())
			mainView.findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
	}

	/**
	 * Supprime un contact des preferences
	 * 
	 * @param contact
	 */
	private void removeContact(Contact contact) {
		if (contacts.contains(contact)) {
			bdd.open();
			int res = bdd.removeContactWithPhone(contact.getNumber());
			if (res == 1)
				contacts.remove(contact);
			else {
				Toast.makeText(getActivity(),
						R.string.erreur_suppression_contact, Toast.LENGTH_SHORT)
						.show();
			}
			bdd.close();
			if (contacts.isEmpty())
				mainView.findViewById(R.id.no_contacts).setVisibility(
						View.VISIBLE);
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
			Toast.makeText(getActivity(), R.string.contact_existant,
					Toast.LENGTH_SHORT).show();
		} else {
			Log.d("ADD", c.toString());
			bdd.open();
			bdd.insertContact(c);
			contacts.add(c);
			bdd.close();
		}
		mainView.findViewById(R.id.no_contacts).setVisibility(View.GONE);
	}

	/**
	 * Actualise l'adapter
	 */
	private void refreshAdapter() {
		mAdapter.setList(contacts);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * Lecture du répertoire du tel
	 */
	public void readcontact() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
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
				String[] projection = new String[] {
						ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
						ContactsContract.CommonDataKinds.Phone.NUMBER };

				Cursor people = getActivity().getContentResolver().query(
						contactData, projection, null, null, null);

				int indexName = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
				int indexNumber = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

				people.moveToFirst();
				do {
					String name = people.getString(indexName);
					String number = people.getString(indexNumber);
					addContact(name, number); // On l'ajoute
					refreshAdapter(); // On refresh
				} while (people.moveToNext());
			}
			break;
		}
	}
}
