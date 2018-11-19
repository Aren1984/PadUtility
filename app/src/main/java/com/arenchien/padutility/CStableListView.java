package com.arenchien.padutility;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ArenChien on 2018/10/8.
 */

public class CStableListView {
    ListView m_kListView;
    Context m_kContext;
    int m_nListViewResourceId;

    CStableListView( Activity kActivity, int nResourceId ) {
        m_kContext = kActivity;
        m_kListView = ( ListView ) kActivity.findViewById( nResourceId );
        m_nListViewResourceId = nResourceId;
    }

    public void setOnItemClickListener(@Nullable AdapterView.OnItemClickListener listener) {
        m_kListView.setOnItemClickListener( listener );
    }

    public String GetText( int nIndex ) {
        return ( (StableArrayAdapter) m_kListView.getAdapter() ).GetKey( nIndex );
    }

    public int GetCount() {
        return ( (StableArrayAdapter) m_kListView.getAdapter() ).GetItemCount();
    }

    public int GetSelectedIndex() {
        return ( (StableArrayAdapter) m_kListView.getAdapter() ).getSelectItem();
    }

    public void SetSelectedIndex( int nSelected, boolean bNotifyDataChanged ) {
        StableArrayAdapter adapter = (StableArrayAdapter) m_kListView.getAdapter();
        adapter.setSelectItem( nSelected );

        if ( bNotifyDataChanged) {
            adapter.notifyDataSetInvalidated();
        }
    }

    public void SetData( ArrayList<String> kList, ArrayList<String> kKeyList ) {
        StableArrayAdapter adapter = new StableArrayAdapter( m_kContext, android.R.layout.simple_list_item_1, kList, kKeyList );
        m_kListView.setAdapter( adapter );
        m_kListView.setSelection( 0 );
        adapter.setSelectItem( 0 );
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
        HashMap<Integer, String> mKeyMap = new HashMap<Integer, String>();
        private int nSelectItem = -1;

        public StableArrayAdapter( Context context, int textViewResourceId, List<String> objects, List<String> kKeyList ) {
            super( context, textViewResourceId, objects );
            for ( int i = 0; i < objects.size(); ++i ) {
                mIdMap.put( objects.get( i ), i );
            }
            for ( int i = 0; i < objects.size(); ++i ) {
                mKeyMap.put( i, kKeyList.get( i ) );
            }
        }

        public void setSelectItem( int nSelectItem ) {
            this.nSelectItem = nSelectItem;
        }

        public int getSelectItem() {
            return this.nSelectItem;
        }

        public String GetKey( int nItemIndex ) {
            return mKeyMap.get( nItemIndex );
        }

        public int GetItemCount() {
            return mIdMap.size();
        }

        @Override
        public long getItemId( int position ) {
            String item = getItem( position );
            if ( mIdMap.containsKey( item ) ) {
                return mIdMap.get( item );
            }
            else {
                return 0;
            }
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            final View renderer = super.getView( position, convertView, parent );
            if ( position == nSelectItem ) {
                renderer.setBackgroundResource( android.R.color.darker_gray );
            } else {
                renderer.setBackgroundResource( android.R.color.white );
            }
            return renderer;
        }

    }
}