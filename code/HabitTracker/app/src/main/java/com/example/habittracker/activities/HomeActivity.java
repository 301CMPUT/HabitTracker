package com.example.habittracker.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.habittracker.DatabaseManager;
import com.example.habittracker.Habit;

import com.example.habittracker.NavBarManager;
import com.example.habittracker.R;
import com.example.habittracker.activities.eventlist.EventListActivity;
import com.example.habittracker.utils.CustomHabitList;
import com.example.habittracker.utils.DateConverter;
import com.example.habittracker.utils.SharedInfo;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * This activity, HomeActivity displays the "home" aka, the daily view of activities
 */
public class HomeActivity extends AppCompatActivity {

    private ArrayList<Habit> habitList = new ArrayList<>();
    private ListView list = null;
    private ArrayAdapter<Habit> habitAdapter;

    /**
     * Populates the screen's interactables (nav bar click, button clicks, etc..)
     * @param savedInstanceState    {@code Bundle}  the bundle info used to create
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        NavBarManager nav = new NavBarManager(this,findViewById(R.id.bottom_navigation));

        list = findViewById(R.id.sharing_list_view);

        this.habitAdapter = new CustomHabitList(this, habitList);
        this.list.setAdapter(habitAdapter);

        this.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Performs an action (open habit view details) when click on habit
             * @param adapter   {@code AdapterView<?> } the adapter view related to this list
             * @param v         {@code View}            the view clicked
             * @param position  {@code int}             the index of the item clicked
             * @param id        {@code long}            the id
             */
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),HabitViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("habit", (Serializable) list.getItemAtPosition(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        Button event_list_button = findViewById(R.id.follow_button);
        event_list_button.setOnClickListener(new View.OnClickListener() {

            /**
             * Performs an action (takes user to event list ivew) when milestone button is pressed.
             * @param view  {@code View} the view that was pressed
             */
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EventListActivity.class);
                startActivity(intent);
            }
        });

        // snapshot
        DatabaseManager
                .get()
                .getHabitsColRef(SharedInfo.getInstance().getCurrentUser().getUsername())
                .addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    /**
                     * Populates daily habit view when visiting this activity again or changes occur
                     * (or really, an event occurs)
                     * @param value {@code QuerySnapshot}               the snapshot result
                     * @param error {@code FirebaseFirestoreException}  in case of error, result.
                     */
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        // Clear the old list
                        habitList.clear();
                        repopulate(value, error);
                        habitAdapter.notifyDataSetChanged();
                    }
                }
        );
    }

    /**
     * Repopulates the activity that lists all habits belonging to a user
     * @param value {QuerySnapshot}                 the snapshot value
     * @param error {FirebaseFirestoreException}    error, if any
     */
    private void repopulate (QuerySnapshot value, FirebaseFirestoreException error) {
        String [] attributes = {"reason", "dateStarted", "whatDays", "progress", "display"};

        if (error == null) {
            for (QueryDocumentSnapshot doc : value) {
                ArrayList<String> weekDays = (ArrayList<String>) doc.get(attributes[2]);

                // check if the habit should be performed in today's day of the week
                if ( (weekDays != null) && (weekDays.contains(DateConverter.getCurrentWeekDay())) ) {

                    // check if the habit started today or before today.
                    ArrayList<Long> dateTest = (ArrayList<Long>) doc.get(attributes[1]);
                    Date startDate = DateConverter.arrayListToDate(dateTest);
                    Date today = new Date();

                    if (today.after(startDate)) {
                        String habitTitle = doc.getId();
                        String displayTitle = (String) doc.getData().get(attributes[4]);
                        String habitReason = (String) doc.getData().get(attributes[0]);

                        habitList.add(
                                new Habit(
                                        habitTitle,
                                        displayTitle,
                                        habitReason,
                                        startDate,
                                        weekDays
                                ));
                    }
                }
            }
        }
    }
    }