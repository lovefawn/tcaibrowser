package org.tcai.reader.models;

import java.util.ArrayList;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.tcai.reader.R;

public class ReaderItemAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<String> lstItems;
	private TextView tvItem;

	public ReaderItemAdapter(Context mContext, ArrayList<String> list) {
		this.context = mContext;
		lstItems = list;
	}

	@Override
	public int getCount() {
		return lstItems.size();
	}

	@Override
	public Object getItem(int position) {
		return lstItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void exchange(int startPosition, int endPosition) {
		Object endObject = getItem(endPosition);
		Object startObject = getItem(startPosition);
		lstItems.add(startPosition, (String) endObject);
		lstItems.remove(startPosition + 1);
		lstItems.add(endPosition, (String) startObject);
		lstItems.remove(endPosition + 1);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.item, null);
		tvItem = (TextView) convertView.findViewById(R.id.tv_feed);
		if (lstItems.get(position) == null) {
			tvItem.setText("+");
			tvItem.setTextSize(20);
			tvItem.setBackgroundResource(R.drawable.red);
			tvItem.setGravity(Gravity.CENTER);
			tvItem.setPadding(0, 0, 0, 0);
		} else if (lstItems.get(position).equals("none")) {
			tvItem.setText("");
			tvItem.setBackgroundDrawable(null);
		} else {
			tvItem.setText( lstItems.get(position));
			tvItem.setBackgroundResource(R.drawable.browser_thumbnail);
			tvItem.getBackground().setAlpha(180);
		}
		return convertView;
	}

}
