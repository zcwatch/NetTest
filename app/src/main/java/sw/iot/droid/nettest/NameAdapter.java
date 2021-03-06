package sw.iot.droid.nettest;

/**
 * Created by GoldWatch on 6/21/17.
 */

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NameAdapter extends BaseAdapter {
    private String[] mNames;
    private String[] mValues;
    private Context mContext;
    private List<NameAddress> mList;

    public NameAdapter(Context pContext, List<NameAddress> pList) {
        this.mContext = pContext;
        this.mList = pList;
    }

    public NameAdapter(Context pContext, String[] pNames, String[] pValues) {
        this.mContext = pContext;
        this.mNames = pNames;
        this.mValues = pValues;
    }

    @Override
    public int getCount() {
        if (mList!=null)
            return mList.size();
        else
            return mNames.length;

    }

    @Override
    public Object getItem(int position) {
        if (mList!=null)
            return mList.get(position);
        else
            return mValues[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        LayoutInflater layout1=LayoutInflater.from(mContext);
        convertView=layout1.inflate(R.layout.item_custom, null);
        if(convertView!=null) {
            TextView view1=(TextView)convertView.findViewById(R.id.textView1);
            TextView view2=(TextView)convertView.findViewById(R.id.textView2);
            if (mList!=null) {
                view1.setText(mList.get(pos).getName());
                view2.setText(mList.get(pos).getAddress());
            } else {
                view1.setText(mNames[pos]);
                view2.setText(mValues[pos]);
            }
        }
        return convertView;
    }

}