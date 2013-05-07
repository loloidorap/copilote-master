package com.valohyd.copilotemaster.utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.valohyd.copilotemaster.R;

public class MultiSelectionAdapter<T> extends BaseAdapter {

	Context mContext;

	LayoutInflater mInflater;

	ArrayList<T> mList;
	ArrayList<T> mList2;

	SparseBooleanArray mSparseBooleanArray;

	public MultiSelectionAdapter(Context context, HashMap<T, T> list) {
		this.mContext = context;

		mInflater = LayoutInflater.from(mContext);

		mSparseBooleanArray = new SparseBooleanArray();

		mList = new ArrayList<T>();
		mList2 = new ArrayList<T>();

		this.mList = new ArrayList<T>(list.keySet());
		this.mList2 = new ArrayList<T>(list.values());

	}

	public void setList(HashMap<T, T> list) {
		this.mList = new ArrayList<T>(list.keySet());
		this.mList2 = new ArrayList<T>(list.values());
		this.mSparseBooleanArray = new SparseBooleanArray();
	}

	public ArrayList<T> getCheckedItems() {

		ArrayList<T> mTempArry = new ArrayList<T>();

		for (int i = 0; i < mList.size(); i++) {

			if (mSparseBooleanArray.get(i)) {

				mTempArry.add(mList.get(i));

			}

		}

		return mTempArry;

	}

	@Override
	public int getCount() {
		return mList.size();

	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);

	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.contact_row, null);

		}

		TextView tvTitle = (TextView) convertView
				.findViewById(R.id.contactName);

		tvTitle.setText(mList.get(position).toString());

		CheckBox mCheckBox = (CheckBox) convertView
				.findViewById(R.id.chkEnable);

		mCheckBox.setTag(position);

		mCheckBox.setChecked(mSparseBooleanArray.get(position));

		mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);

		ImageButton mCallButton = (ImageButton) convertView
				.findViewById(R.id.callContactButton);
		mCallButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String uri = "tel:" + mList2.get(position).toString().trim();
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse(uri));
				mContext.startActivity(intent);
			}
		});

		ImageButton mSmsButton = (ImageButton) convertView
				.findViewById(R.id.smsContactButton);
		mSmsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent smsIntent = new Intent(Intent.ACTION_VIEW);
				smsIntent.setType("vnd.android-dir/mms-sms");
				smsIntent.putExtra("address", mList2.get(position).toString()
						.trim());
				// smsIntent.putExtra("sms_body","Body of Message");
				mContext.startActivity(smsIntent);
			}
		});

		return convertView;

	}

	OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
		}

	};

}
