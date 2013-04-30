package com.valohyd.copilotemaster.fragments;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.utils.Chronometer;

/**
 * Classe representant le fragment de pointage
 * 
 * @author parodi
 * 
 */
public class ChronoFragment extends Fragment {

	private View mainView;
	private Button partielButton, startButton, stopButton;
	private Chronometer chrono;
	private ListView partielList;

	ArrayList<String> partielValues = new ArrayList<String>();
	ArrayAdapter<String> listAdapter;

	public static final String ARG_SECTION_NUMBER = "section_number";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.chrono_layout, container, false);

		// LIST
		partielList = (ListView) mainView.findViewById(R.id.partielList);
		listAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, partielValues);
		partielList.setAdapter(listAdapter);
		// BUTTONS
		partielButton = (Button) mainView.findViewById(R.id.partielButton);
		startButton = (Button) mainView.findViewById(R.id.startChronoButton);
		stopButton = (Button) mainView.findViewById(R.id.stopChronoButton);

		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				partielValues.clear();
				listAdapter.notifyDataSetChanged();
				chrono.setBase(SystemClock.elapsedRealtime());
				chrono.start();
				startButton.setText("Reset");
			}
		});
		stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				chrono.stop();
				startButton.setText("Reset");
			}
		});

		partielButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				listAdapter.add(chrono.getText().toString());
				listAdapter.notifyDataSetChanged();
				partielList.setSelection(partielList.getCount() - 1);
			}
		});

		// CHRONOMETER
		chrono = (Chronometer) mainView.findViewById(R.id.chrono);
		return mainView;
	}
}
