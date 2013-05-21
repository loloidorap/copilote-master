package com.valohyd.copilotemaster.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.utils.Chronometer;

/**
 * Classe representant le fragment du chronometre
 * 
 * @author parodi
 * 
 */
public class ChronoFragment extends SherlockFragment {

	private View mainView;
	private Button partielButton, startButton, stopButton; // Boutons chronos
	private Chronometer chrono; // Chronometre
	private ListView partielList; // Liste des temps partiels
	private long timeWhenStopped = 0; // Stocke un temps pour le resume du
										// chrono
	private boolean firstTime = true; // Au start du chrono

	ArrayList<String> partielValues = new ArrayList<String>(); // Les partiels
	ArrayAdapter<String> listAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.chrono_layout, container, false);

		// CHRONOMETER
		chrono = (Chronometer) mainView.findViewById(R.id.chrono);

		// LIST
		partielList = (ListView) mainView.findViewById(R.id.partielList);
		listAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, partielValues);
		partielList.setAdapter(listAdapter);
		// BUTTONS
		partielButton = (Button) mainView.findViewById(R.id.partielButton);
		partielButton.setEnabled(false); // Desactivation des partiels si chrono
											// eteint

		startButton = (Button) mainView.findViewById(R.id.startChronoButton);
		stopButton = (Button) mainView.findViewById(R.id.stopChronoButton);

		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				partielButton.setEnabled(true); // activation des partiels
				// Si on doit reprendre le chrono
				if (startButton.getText().equals("Resume")) {
					chrono.setBase(SystemClock.elapsedRealtime()
							+ timeWhenStopped); // On reprend ou on s'etait
												// arrêté
				}
				// Si on declenche pour la premiere fois le chrono
				if (firstTime) {
					chrono.setBase(SystemClock.elapsedRealtime()); // On remet à
																	// 0
				}
				chrono.start();
				startButton.setEnabled(false); // On ne peux plus rappuyer sur
												// start
				stopButton.setText("Stop");
				firstTime = false;
			}
		});
		stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				partielButton.setEnabled(false); // desactivation des partiels
				// Reset du chrono
				if (stopButton.getText().equals("Reset")) {
					chrono.stop(); // On stop le chrono
					chrono.setBase(SystemClock.elapsedRealtime()); // On RAZ le
																	// chrono
					startButton.setText("Start"); // On remet start
					startButton.setEnabled(true); // On reactive le bouton start
					partielValues.clear(); // On vide la liste des partiels
					listAdapter.notifyDataSetChanged(); // On notifie le
														// changement des
														// données
				}
				// Sinon c'est qu'on pause le chrono
				else {
					timeWhenStopped = chrono.getBase()
							- SystemClock.elapsedRealtime(); // On stocke le
																// temps
					chrono.stop(); // On stoppe le chrono
					startButton.setEnabled(true);
					startButton.setText("Resume");
					stopButton.setText("Reset");
				}
			}
		});

		partielButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Ajout d'un temps partiel
				listAdapter.add(chrono.getText().toString());
				listAdapter.notifyDataSetChanged();
				partielList.setSelection(partielList.getCount() - 1); // On
																		// descend
																		// la
																		// liste
																		// au
																		// dernier
																		// element
			}
		});
		return mainView;
	}
}
