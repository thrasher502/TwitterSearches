package com.example.twittersearches;

import java.util.Collections;
import java.util.ArrayList;
import android.app.ListActivity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;

public class MainActivity extends ListActivity {

    enum TouchFlag {CLEAR, KEEP}
    protected final static String SEARCHES = "searches";
    protected EditText queryEditText,tagEditText;
    protected ImageButton saveButton;
    protected ArrayList<String> tags;
    protected SharedPreferences savedSearches;
    protected ArrayAdapter<String> adapter;

    TouchFlag flag = TouchFlag.CLEAR;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queryEditText = (EditText) findViewById(R.id.queryEditText);
        tagEditText = (EditText)findViewById(R.id.tagEditText);
        saveButton = (ImageButton)findViewById(R.id.saveButton);
        savedSearches = getSharedPreferences(SEARCHES,MODE_PRIVATE);
        tags = new ArrayList<String>(savedSearches.getAll().keySet());
        Collections.sort(tags);
        adapter = new ArrayAdapter<String>(this,R.layout.list_item,tags);
        setListAdapter(adapter);

        //Listeners:

        queryEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()&& flag==TouchFlag.CLEAR)
                    setEditTexts("","");



                    return false;
            }
        });

        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tag = ((TextView)view).getText().toString();
                String urlString = getString(R.string.searchURL).toString() + Uri.encode(savedSearches.getString(tag,""),"UTF-8");
                Intent searchIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(urlString));
                startActivity(searchIntent);
            }
        });

        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String TAG = ((TextView)view).getText().toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getString(R.string.shareEditDeleteTitle,TAG));
                builder.setItems(R.array.longClickItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Switch declares what each item of the alert dialog does:
                        switch (which){
                            case 0:
                                shareSearch(TAG);
                                break;
                            case 1:
                                flag = TouchFlag.KEEP;
                                editSearch(TAG);
                                break;
                            case 2:
                                deleteSearch(TAG);


                                break;


                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();

                return true;
            }
        });
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (queryEditText.getText().length()>0 && tagEditText.getText().length()>0){
                    addSearch(tagEditText.getText().toString(),queryEditText.getText().toString());
                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(tagEditText.getWindowToken(),0);
                    clearEditTexts();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(getString(R.string.noTextAlert));
                    builder.setPositiveButton(getString(R.string.ok),null);
                    builder.create().show();


                }

            }
        });


    }

    //Methods used
    private void addSearch(String tag, String query){
        SharedPreferences.Editor editor = savedSearches.edit();
        editor.putString(tag,query);
        editor.apply();
        adapter.notifyDataSetChanged();
        if (!tags.contains(tag)){
            tags.add(tag);
            Collections.sort(tags);
        }

    }

    private void shareSearch(String tag){
        String urlString = getString(R.string.searchURL)+ Uri.encode(savedSearches.getString(tag,""),"UTF-8");
        Intent shareSearch = new Intent ();
        shareSearch.setAction(Intent.ACTION_SEND);
        shareSearch.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareSubject));
        shareSearch.putExtra(Intent.EXTRA_TEXT,getString(R.string.shareText,urlString));
        shareSearch.setType("text/plain");
        startActivity(Intent.createChooser(shareSearch,getString(R.string.shareSearch)));
    }

    private void editSearch(String tag){

        setEditTexts(tag,savedSearches.getString(tag,""));
    }

    private void deleteSearch(final String TAG){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirmDelete,TAG));
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = savedSearches.edit();
                tags.remove(TAG);
                editor.remove(TAG);
                editor.apply();
                adapter.notifyDataSetChanged();
                clearEditTexts();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
}

    private void setEditTexts(String tag, String query){
        tagEditText.setText(tag);
        queryEditText.setText(query);
        flag = TouchFlag.KEEP;
    }
    private void clearEditTexts(){
        setEditTexts(getString(R.string.tagText),getString(R.string.queryText));;
        flag=TouchFlag.CLEAR;

    }


}
