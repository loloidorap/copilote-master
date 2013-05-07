package com.valohyd.copilotemaster.fragments;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.Activity;
import android.app.Fragment;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.utils.MultiSelectionAdapter;

/**
 * Classe representant le fragment de pointage
 * 
 * @author parodi
 * 
 */
public class ContactFragment extends Fragment {

	public static final String TAG_PREF_CONTACT = "contacts";

	LinearLayout layoutButtons;

	SharedPreferences sharedPrefs;
	Editor edit;

	MultiSelectionAdapter<String> mAdapter;

	ArrayList<String> contacts = new ArrayList<String>();

	private final int PICK_CONTACT = 69;

	private TextView contactText;
	private Button addContactButton, removeContactsButton;
	private ListView list;

	private View mainView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.contact_layout, container, false);

		sharedPrefs = getActivity().getSharedPreferences("blop",
				Activity.MODE_PRIVATE);
		edit = sharedPrefs.edit();

		initContacts();

		layoutButtons = (LinearLayout) mainView
				.findViewById(R.id.buttonsContactLayout);

		contactText = (TextView) mainView.findViewById(R.id.textContact);

		list = (ListView) mainView.findViewById(R.id.contactList);
		mAdapter = new MultiSelectionAdapter<String>(getActivity(), contacts);
		list.setAdapter(mAdapter);

		addContactButton = (Button) mainView
				.findViewById(R.id.addContactButton);
		addContactButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				readcontact();
			}
		});

		removeContactsButton = (Button) mainView
				.findViewById(R.id.removeContactButton);
		removeContactsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mAdapter != null) {

					ArrayList<String> mArraySelected = mAdapter
							.getCheckedItems();

					for (String selected : mArraySelected) {
						removeContact(selected);
					}
					refreshAdapter();
				}
			}
		});

		return mainView;
	}

	private void initContacts() {
		ArrayList<String> contact_init = new ArrayList<String>(
				sharedPrefs.getStringSet(TAG_PREF_CONTACT, new HashSet<String>()));

		for (String s : contact_init) {
			String[] contact = s.split(":");
			contacts.add(contact[0]+":"+contact[1]);
		}
	}

	private void removeContact(String contact) {
		if (contacts.contains(contact)) {
			contacts.remove(contact);
			savePreferences();
			Toast.makeText(getActivity(), "Contact supprim� : " + contact,
					Toast.LENGTH_SHORT).show();
			Log.d("CONTACTS", contacts.toString());
		}
	}

	private void addContact(String name, String number) {
		if (contacts.contains(name+":"+number)) {
			Toast.makeText(getActivity(), "Contact existant !",
					Toast.LENGTH_SHORT).show();
		} else {
			contacts.add(name+":"+number);
			savePreferences();
			Toast.makeText(getActivity(), "Contact " + name + " ajout� !",
					Toast.LENGTH_SHORT).show();
			Log.d("CONTACTS", contacts.toString());
		}
	}

	private void savePreferences() {
		edit.putStringSet("contacts", new HashSet<String>(contacts));
		edit.commit();
	}

	private void refreshAdapter() {
		mAdapter.setList(contacts);
		mAdapter.notifyDataSetChanged();
	}

	public void readcontact() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				Contacts.Phones.CONTENT_URI);
		startActivityForResult(intent, PICK_CONTACT);

	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = getActivity().managedQuery(contactData, null, null,
						null, null);
				if (c.moveToFirst()) {
					String nom = c.getString(c
							.getColumnIndexOrThrow(People.NAME));
					String num = c.getString(c
							.getColumnIndexOrThrow(People.NUMBER));
					addContact(nom, num);
					refreshAdapter();
				}
			}
			break;
		}
	}
}
