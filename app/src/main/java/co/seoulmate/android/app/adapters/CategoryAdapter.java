package co.seoulmate.android.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import co.seoulmate.android.app.R;
import co.seoulmate.android.app.utils.CategoryUtils;

/**
 * Created by hassanabid on 10/25/15.
 */
public class CategoryAdapter extends BaseAdapter{
    private String[] mCats;

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private int catPos;
    private int startPosition;
    private int endPosition;

    public CategoryAdapter(Context context, String[] cats) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mContext = context;
        mCats = cats;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(R.layout.cat_row_item, parent, false);
        }
        setCategories((TextView) convertView.findViewById(R.id.profileTagsTitle), mCats[position]);
        return convertView;
    }

    private void setCategories(TextView mIcon, String tag) {
//        mIcon.setImageResource(tag.getDrawableId());
        mIcon.setText(tag);
    }

    @Override
    public int getCount() {

        return mCats.length;
    }

    @Override
    public Object getItem(int position) {
        return mCats[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
