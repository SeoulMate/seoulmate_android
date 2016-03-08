package co.seoulmate.android.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.List;
import java.util.Locale;
import co.seoulmate.android.app.R;
import co.seoulmate.android.app.adapters.CategoryAdapter;
import co.seoulmate.android.app.utils.CategoryUtils;
import co.seoulmate.android.app.utils.ModelUtils;

public class NewPostCatActivity extends AppCompatActivity {

    private static final String LOG = NewPostCatActivity.class.getSimpleName();
    private static final String KEY_SELECTED_TAG_INDEX = "selected_tag_index";

    private GridView mTagGrid;
    private View mSelectedTagView;
    private List<Integer> mTagsList;
    private boolean isBoard;
    private TextView chooseTag;
    private int mSelectedCategory = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post_cat);
        isBoard = getIntent().getBooleanExtra(ModelUtils.BOARD, false);
        if (savedInstanceState != null) {
            int savedAvatarIndex = savedInstanceState.getInt(KEY_SELECTED_TAG_INDEX);
            mSelectedCategory = savedAvatarIndex;
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView next = (TextView) toolbar.findViewById(R.id.nextStepQuestion);
        if(isBoard) {
            toolbar.setTitle(getString(R.string.write_board_post));
        }
        chooseTag = (TextView) findViewById(R.id.toolbar_choose_avatar);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if(isBoard) {
                    intent = new Intent(NewPostCatActivity.this, NewBoardPostActivity.class);
                    intent.putExtra(ModelUtils.BOARD, true);
                } else {
                    intent = new Intent(NewPostCatActivity.this, NewQuestionActivity.class);
                    intent.putExtra(ModelUtils.BOARD, false);
                }
                intent.putExtra(ModelUtils.CATNO, mSelectedCategory);
                startActivity(intent);
            }
        });
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_36dp);
        findViewById(android.R.id.content).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                setUpGridView();
            }
        });

    }
    private void setUpGridView() {
        mTagGrid = (GridView) findViewById(R.id.tags);
        mTagGrid.setAdapter(new CategoryAdapter(this,getResources().getStringArray(R.array.cat_array)));
        chooseTag.setText(String.format(Locale.US, getResources().getString(R.string.choose_category_selected),
                CategoryUtils.getName(mSelectedCategory)));
        mTagGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedTagView = view;
                mSelectedCategory = position;
                Log.d(LOG,"mSelectedCategory = " + mSelectedCategory);
                chooseTag.setText(String.format(Locale.US,getResources().getString(R.string.choose_category_selected),
                        CategoryUtils.getName(mSelectedCategory)));

            }
        });
        mTagGrid.setNumColumns(calculateSpanCount());
        mTagGrid.setItemChecked(mSelectedCategory, true);

    }

    /**
     * Calculates spans for tags dynamically.
     *
     * @return The recommended amount of columns.
     */
    private int calculateSpanCount() {
        int avatarSize = getResources().getDimensionPixelSize(R.dimen.size_fab);
        int avatarPadding = getResources().getDimensionPixelSize(R.dimen.spacing_double);
        int cols = mTagGrid.getWidth() / (avatarSize + avatarPadding);
        Log.d(LOG, "number of cols : " + cols);
        return 3;
    }



    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putInt(KEY_SELECTED_TAG_INDEX, mSelectedCategory);
    }

}
