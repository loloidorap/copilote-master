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
	private long timeWhenStopped = 0;

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
				if (startButton.getText().equals("Resume")) {
					chrono.setBase(SystemClock.elapsedRealtime()
							+ timeWhenStopped);
				}
				chrono.start();
				startButton.setEnabled(false);
				stopButton.setText("Stop");

			}
		});
		stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (stopButton.getText().equals("Reset")) {
					chrono.stop();
					chrono.setBase(SystemClock.elapsedRealtime());
					startButton.setText("Start");
					startButton.setEnabled(true);
					partielValues.clear();
					listAdapter.notifyDataSetChanged();
				} else {
					timeWhenStopped = chrono.getBase()
							- SystemClock.elapsedRealtime();
					chrono.stop();
					startButton.setEnabled(true);
					startButton.setText("Resume");
					stopButton.setText("Reset");
				}
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
