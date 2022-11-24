package com.darkbeast0106.peoplerestclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.darkbeast0106.peoplerestclient.databinding.ActivityMainBinding;
import com.darkbeast0106.peoplerestclient.databinding.PersonListItemBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private String base_url = "https://retoolapi.dev/cRJhEP/people";

    private class RequestTask extends AsyncTask<Void, Void, Response> {
        private String requestUrl;
        private String requestMethod;
        private String requestBody;

        public RequestTask(String requestUrl) {
            this.requestUrl = requestUrl;
            this.requestMethod = "GET";
        }

        public RequestTask(String requestUrl, String requestMethod) {
            this.requestUrl = requestUrl;
            this.requestMethod = requestMethod;
        }

        public RequestTask(String requestUrl, String requestMethod, String requestBody) {
            this.requestUrl = requestUrl;
            this.requestMethod = requestMethod;
            this.requestBody = requestBody;
        }

        @Override
        protected Response doInBackground(Void... voids) {
            Response response = null;
            try {
                switch (requestMethod) {
                    case "GET":
                        response = RequestHandler.get(requestUrl);
                        break;
                    case "POST":
                        response = RequestHandler.post(requestUrl, requestBody);
                        break;
                    case "PUT":
                        response = RequestHandler.put(requestUrl, requestBody);
                        break;
                    case "DELETE":
                        response = RequestHandler.delete(requestUrl);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);
            binding.progressBar.setVisibility(View.GONE);
            if (response == null) {
                Toast.makeText(MainActivity.this, R.string.unable_to_connect, Toast.LENGTH_SHORT).show();
                return;
            }
            if (response.getResponseCode() >= 400) {
                Toast.makeText(MainActivity.this, response.getContent(), Toast.LENGTH_SHORT).show();
                return;
            }
            switch (requestMethod){
                case "GET":
                    String content = response.getContent();
                    Gson converter = new Gson();
                    List<Person> people = Arrays.asList(converter.fromJson(content, Person[].class));
                    Log.d("JSON fromJSON: ", people.get(0) + " " + people.get(0).getId());
                    PeopleAdapter adapter = new PeopleAdapter(people);
                    binding.peopleListView.setAdapter(adapter);
                    break;
                default:
                    if (response.getResponseCode() >= 200 && response.getResponseCode() < 300) {
                        cancelForm();
                        RequestTask task = new RequestTask(base_url);
                        task.execute();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.setPerson(new Person(0, "", "", 0));
        setContentView(binding.getRoot());
        addListeners();
        RequestTask task = new RequestTask(base_url);
        task.execute();
    }

    private void addListeners() {
        binding.submitButton.setOnClickListener(view -> {
            try {
                String json = createJsonFromFormdata();
                //Log.d("JSON toJson: ", json);
                RequestTask task = new RequestTask(base_url, "POST", json);
                task.execute();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.updateButton.setOnClickListener(view -> {
            try {
                String json = createJsonFromFormdata();
                String url = base_url + "/" + binding.getPerson().getId();
                RequestTask task = new RequestTask(url, "PUT", json);
                task.execute();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.cancelButton.setOnClickListener(view -> {
            cancelForm();
        });

        binding.createButton.setOnClickListener(view -> {
            binding.personForm.setVisibility(View.VISIBLE);
            binding.submitButton.setVisibility(View.VISIBLE);
            binding.updateButton.setVisibility(View.GONE);
            binding.createButton.setVisibility(View.GONE);
        });
    }

    private String createJsonFromFormdata() {
        String name = binding.getPerson().getName().trim();
        String email = binding.getPerson().getEmail().trim();
        if (name.isEmpty()){
            throw new IllegalArgumentException("Name is required");
        }
        if (email.isEmpty()){
            throw new IllegalArgumentException("Email is required");
        }
        Person person = binding.getPerson();
        Gson converter = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return converter.toJson(person);
    }

    private void cancelForm() {
        binding.setPerson(new Person(0, "", "", 0));
        binding.personForm.setVisibility(View.GONE);
        binding.createButton.setVisibility(View.VISIBLE);
    }


    private class PeopleAdapter extends ArrayAdapter<Person> {
        private List<Person> people;

        public PeopleAdapter(List<Person> objects) {
            super(MainActivity.this, R.layout.person_list_item, objects);
            people = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            PersonListItemBinding listItemBinding = PersonListItemBinding.inflate(getLayoutInflater());
            Person actualPerson = people.get(position);
            listItemBinding.setPerson(actualPerson);

            listItemBinding.update.setOnClickListener(v -> {
                binding.setPerson(actualPerson);

                binding.personForm.setVisibility(View.VISIBLE);
                binding.submitButton.setVisibility(View.GONE);
                binding.updateButton.setVisibility(View.VISIBLE);
                binding.createButton.setVisibility(View.GONE);
            });
            listItemBinding.delete.setOnClickListener(v -> {
                String url = base_url + "/" + actualPerson.getId();
                RequestTask task = new RequestTask(url, "DELETE");
                task.execute();
            });
            return listItemBinding.getRoot().getRootView();
        }
    }
}