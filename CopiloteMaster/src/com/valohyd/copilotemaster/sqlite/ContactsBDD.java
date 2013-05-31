package com.valohyd.copilotemaster.sqlite;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.valohyd.copilotemaster.models.Contact;

public class ContactsBDD {

	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "contacts.db";

	private static final String TABLE_CONTACTS = "table_contacts";
	private static final String COL_ID = "ID";
	private static final int NUM_COL_ID = 0;
	private static final String COL_NAME = "Name";
	private static final int NUM_COL_NAME = 1;
	private static final String COL_PHONE = "Phone";
	private static final int NUM_COL_PHONE = 2;

	private SQLiteDatabase bdd;

	private ContactsSQLite maBaseSQLite;

	public ContactsBDD(Context context) {
		// On cr�er la BDD et sa table
		maBaseSQLite = new ContactsSQLite(context, NOM_BDD, null, VERSION_BDD);
	}

	public void open() {
		// on ouvre la BDD en �criture
		bdd = maBaseSQLite.getWritableDatabase();
	}

	public void close() {
		// on ferme l'acc�s � la BDD
		bdd.close();
	}

	public SQLiteDatabase getBDD() {
		return bdd;
	}

	public long insertContact(Contact contact) {
		// Cr�ation d'un ContentValues (fonctionne comme une HashMap)
		ContentValues values = new ContentValues();
		// on lui ajoute une valeur associ� � une cl� (qui est le nom de la
		// colonne dans laquelle on veut mettre la valeur)
		values.put(COL_NAME, contact.getName());
		values.put(COL_PHONE, contact.getNumber());
		// on ins�re l'objet dans la BDD via le ContentValues
		return bdd.insert(TABLE_CONTACTS, null, values);
	}

	public int updateContact(int id, Contact contact) {
		// La mise � jour d'un contact dans la BDD fonctionne plus ou moins
		// comme
		// une insertion
		// il faut simple pr�ciser quel contact on doit mettre � jour gr�ce �
		// l'ID
		ContentValues values = new ContentValues();
		values.put(COL_NAME, contact.getName());
		values.put(COL_PHONE, contact.getNumber());
		return bdd.update(TABLE_CONTACTS, values, COL_ID + " = " + id, null);
	}

	public int removeContactWithID(int id) {
		// Suppression d'un contact de la BDD gr�ce � l'ID
		String where = COL_ID + "= " + "?";
		String whereArgs[] = { "" + id };
		return bdd.delete(TABLE_CONTACTS, where, whereArgs);
	}

	public int removeContactWithPhone(String phone) {
		// Suppression d'un contact de la BDD gr�ce � l'ID
		String where = COL_PHONE + "= " + "?";
		String whereArgs[] = { phone };
		return bdd.delete(TABLE_CONTACTS, where, whereArgs);
	}

	public Contact getContactWithName(String titre) {
		// R�cup�re dans un Cursor les valeur correspondant � un contact contenu
		// dans la BDD (ici on s�lectionne le contact gr�ce � son nom)
		Cursor c = bdd.query(TABLE_CONTACTS, new String[] { COL_ID, COL_NAME,
				COL_PHONE }, COL_PHONE + " LIKE \"" + titre + "\"", null, null,
				null, null);
		return cursorToContact(c);
	}

	/**
	 * On recup�re tous les contacts de la base
	 * 
	 * @return liste des contacts
	 */
	public ArrayList<Contact> getAllContacts() {
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		Cursor cursor = bdd.rawQuery("select * from " + TABLE_CONTACTS, null);
		if (cursor.moveToFirst()) {

			while (cursor.isAfterLast() == false) {
				Contact c = new Contact();
				c.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
				c.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
				c.setNumber(cursor.getString(cursor.getColumnIndex(COL_PHONE)));

				contacts.add(c);
				cursor.moveToNext();
			}
		}
		return contacts;
	}

	// Cette m�thode permet de convertir un cursor en un contact
	private Contact cursorToContact(Cursor c) {
		// si aucun �l�ment n'a �t� retourn� dans la requ�te, on renvoie null
		if (c.getCount() == 0)
			return null;

		// Sinon on se place sur le premier �l�ment
		c.moveToFirst();
		// On cr�� un contact
		Contact contact = new Contact();
		// on lui affecte toutes les infos gr�ce aux infos contenues dans le
		// Cursor
		contact.setId(c.getInt(NUM_COL_ID));
		contact.setName(c.getString(NUM_COL_NAME));
		contact.setNumber(c.getString(NUM_COL_PHONE));
		// On ferme le cursor
		c.close();

		// On retourne le contact
		return contact;
	}
}