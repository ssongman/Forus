package com.forus;

import android.content.ClipData;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ArrayList<String> arMeeting;
    public ArrayList<String> arMeetingKey;
    public ArrayAdapter<String> arAdapter;
    public ListView lvMeeting;
    private static final String TAG = "ForUs ListFragment";
    private DatabaseReference MeetingRef;
    private Query queryRef;

    String AuthUid        ;


    public ListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListFragment newInstance(String param1, String param2) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        AuthUid = ((MainActivity)getActivity()).getAuthUid();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        Button btnCreate = (Button) v.findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG,"Call Meeting Create");
                getActivity().getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, new CreateFragment()).commit();
            }
        });

        arMeeting = new ArrayList<String>();
        arMeetingKey = new ArrayList<String>();
        //arMeeting.add("김유신");
        //arMeeting.add("이순신");
        arAdapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_list_item_multiple_choice, arMeeting);
        lvMeeting = (ListView) v.findViewById(R.id.lvMeeting);
        lvMeeting.setAdapter(arAdapter);
        lvMeeting.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //list.setDivider( new ColorDrawable(Color.YELLOW));
        //list.setDividerHeight(5);
        lvMeeting.setOnItemClickListener( mItemClickListener);
        lvMeeting.setOnItemLongClickListener( mItemLongClickListener);

        // 모임정보 읽어오기 from DB
        MeetingRef = FirebaseDatabase.getInstance().getReference().child("Meeting");
        //MeetingRef = FirebaseDatabase.getInstance().getReference().child("Meeting").orderByChild("MtMembers").equalTo(AuthUid);
        //queryRef = MeetingRef.orderByChild("MtMembers").equalTo(AuthUid);
        //queryRef = MeetingRef.orderByChild("MtMembers").startAt(AuthUid).endAt(AuthUid);
        //queryRef = MeetingRef.child("MtMembers").child(AuthUid).orderByChild("PartYN").equalTo("Y");
        queryRef = MeetingRef.limitToFirst(10);    // 일단 10개만 읽는다.  나중에 변경하자.

        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                arMeeting.clear();
                arMeetingKey.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //Getting the data from snapshot
                    Meeting meeting = postSnapshot.getValue(Meeting.class);
                    String meeting_name = meeting.getMtName();
                    arMeeting.add(meeting_name);
                    arMeetingKey.add(postSnapshot.getKey());
                }
                arAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
         });

        return v;
    }

    AdapterView.OnItemLongClickListener mItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        // Long Click : 참여 fragment
        public boolean  onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Fragment fr = new PartFragment();
            Bundle args = new Bundle();
            args.putString("param1", arMeetingKey.get(position) );
            args.putString("param2", arMeeting.get(position) );
            fr.setArguments(args);

            // Create new fragment and transaction
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fr);
            transaction.addToBackStack(null);                  // and add the transaction to the back stack
            transaction.commit();

            return true;
        }
    };

    AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        // Click : Detail fragment
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Fragment fr = new DetailFragment();
            Bundle args = new Bundle();
            args.putString("param1", arMeetingKey.get(position) );
            args.putString("param2", arMeeting.get(position) );
            fr.setArguments(args);

            // Create new fragment and transaction
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fr);
            transaction.addToBackStack(null);                  // and add the transaction to the back stack
            transaction.commit();
        }

    };

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



    private void AlertMessage(String string)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("알림");
        builder.setMessage(string);
        builder.setPositiveButton("확인", null);
        builder.show();
    }

}