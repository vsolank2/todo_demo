package com.swatiitsolutions.tododemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.swatiitsolutions.tododemo.API.APIClient;
import com.swatiitsolutions.tododemo.API.ApiInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements TodoAdapter.OnDoneClickListener, TodoAdapter.OnCloseClickListener {
    ApiInterface apiInterface;
    List<ClsToDo> toDoList = new ArrayList<>();
    TodoAdapter adapter;
    RecyclerView rvTodo;
    DatabaseHandler db;
    TextView etTask;
    Button btnSave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("ToDo Demo");

        btnSave = findViewById(R.id.btnSave);
        etTask = findViewById(R.id.etTask);
        apiInterface = APIClient.getClient().create(ApiInterface.class);
        rvTodo = findViewById(R.id.rvTodo);
        rvTodo.setLayoutManager(new LinearLayoutManager(this));
        db= new DatabaseHandler(this);
        AsyncTaskExample asyncTask=new AsyncTaskExample();
        asyncTask.execute();

        setAdapter();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.InsertData(etTask.getText().toString(),"false");
                setAdapter();
            }
        });

    }

    @Override
    public void onDoneItemClick(TodoDB item) {
        item.setCompleted("true");
        db.updateContact(item);
        setAdapter();
    }

    @Override
    public void onCloseItemClick(TodoDB item) {
        db.deleteContact(item);
        setAdapter();
    }


    private class AsyncTaskExample extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                Call<List<ClsToDo>> call = apiInterface.getTodoList();

                call.enqueue(new Callback<List<ClsToDo>>() {
                    @Override
                    public void onResponse(Call<List<ClsToDo>> call, Response<List<ClsToDo>> response) {
                        toDoList = response.body();
                        if(toDoList != null){
                            Log.d("Check", "onResponse: "+new Gson().toJson(toDoList));


                            for(int i=0; i<toDoList.size();i++){
                                db.InsertData(toDoList.get(i).getTitle(),toDoList.get(i).getCompleted().toString());
                            }

                            List<TodoDB> todoDBS = db.getAllContacts();
                            Collections.sort(todoDBS, new Comparator<TodoDB>() {
                                @Override
                                public int compare(TodoDB o1, TodoDB o2) {
                                    return o1.getTitle().compareTo(o2.getTitle());
                                }
                            });
                            Log.d("Check", "onResponse: todoDBS "+new Gson().toJson(todoDBS));
                            TodoDB[] todoArray = new TodoDB[todoDBS.size()];
                            todoArray = todoDBS.toArray(todoArray);
                            adapter = new TodoAdapter(todoArray,MainActivity.this,MainActivity.this,MainActivity.this);
                            rvTodo.setAdapter(adapter);

                        }else {
                            Log.d("Check", "onResponse: "+response.message());
                            Log.d("Check", "onResponse: "+response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ClsToDo>> call, Throwable t) {
                        Log.d("Check", "onResponse: "+t.getMessage());

                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String str) {
            super.onPostExecute(str);
           setAdapter();
        }
    }

    private void setAdapter() {

        List<TodoDB> todoDBS = db.getAllContacts();
        Collections.sort(todoDBS, new Comparator<TodoDB>() {
            @Override
            public int compare(TodoDB o1, TodoDB o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        Log.d("Check", "onResponse: todoDBS "+new Gson().toJson(todoDBS));
        TodoDB[] todoArray = new TodoDB[todoDBS.size()];
        todoArray = todoDBS.toArray(todoArray);
        adapter = new TodoAdapter(todoArray,MainActivity.this,MainActivity.this,MainActivity.this);
        rvTodo.setAdapter(adapter);
    }
}