package com.diegogomez.climate;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    EditText cityBox;
    Button updateButton;
    ImageButton searchButton;
    public TextView temperaturaView, descripcion, termica, humedad, viento;
    public TextView cityName, fecha, information;
    ImageView icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        fecha.setText(mostrarFecha());
        getUbicacionActual();

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(cityBox.getText().toString()))
                cityBox.setError("Busca una ciudad");
            else
                get();
        });

        searchButton.setOnClickListener(v -> getUbicacionActual());

        final InformationDialog informationDialog = new InformationDialog(MainActivity.this);
        information.setOnClickListener(v -> informationDialog.loadingAlertDialog());
    }

    public void init() {
        cityBox = findViewById(R.id.searchCityBar);
        searchButton = findViewById(R.id.searchAction);
        temperaturaView = findViewById(R.id.tempWeather);
        descripcion = findViewById(R.id.descriptionWeather);
        updateButton = findViewById(R.id.updater);
        termica = findViewById(R.id.termicaValor);
        humedad = findViewById(R.id.humedadValor);
        viento = findViewById(R.id.vientoVelocidad);
        cityName = findViewById(R.id.cityResult);
        icon = findViewById(R.id.iconWeather);
        fecha = findViewById(R.id.fecha);
        information = findViewById(R.id.infoBusqueda);
    }

    // Metodo para busqueda por medio del nombre de la ciudad.
    public void get() {
        String ciudad = cityBox.getText().toString();
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + ciudad + "&lang=es&appid=a384f8e199f04d1d9dcf528e4fd9c38c&units=metric";
        Toast.makeText(MainActivity.this, "Buscando...", Toast.LENGTH_LONG).show();

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null, response -> {
            try {
                JSONObject object = response.getJSONObject("main");
                JSONObject sys = response.getJSONObject("sys");
                JSONObject dataViento = response.getJSONObject("wind");
                JSONArray arreglo = response.getJSONArray("weather");

                getTemperatura(object);
                getDescription(arreglo);
                iconPicker(arreglo);
                getTermica(object);
                getHumedad(object);
                getPais(sys, response);
                getViento(dataViento);

            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            if (error.networkResponse != null) {
                cityBox.setError("Ciudad no encontrada");
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            } else if (error instanceof TimeoutError) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            } else if (error instanceof ServerError) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            } else if (error instanceof NoConnectionError) {
                Toast.makeText(MainActivity.this, "Revisa tu conexión a Internet", Toast.LENGTH_SHORT).show();
            } else if (error instanceof NetworkError) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            } else if (error instanceof ParseError) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        queue.add(request);
    }

    // Metodo de busqueda por latitud y longitud.
    public void getUbicacionActual() {
        LocationManager locationManager;

        // Aviso al usuario.
        Toast.makeText(MainActivity.this, "Accediendo a su ubicación. Aguarde...", Toast.LENGTH_LONG).show();

        // Conectar al servicio de GPS.
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        // SE VERIFICA QUE SE TENGAN LOS PERMISOS DE UBICACION.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }

        locationManager.requestLocationUpdates(
                locationManager.GPS_PROVIDER,
                10,
                1,
                location -> get(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()))
        );

    }

    public void get(String latitud, String longitud) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat="+latitud+"&lon=" +longitud+"&lang=es&appid=a384f8e199f04d1d9dcf528e4fd9c38c&units=metric";

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null, response -> {
            try {
                JSONObject object = response.getJSONObject("main");
                JSONObject sys = response.getJSONObject("sys");
                JSONObject dataViento = response.getJSONObject("wind");
                JSONArray arreglo = response.getJSONArray("weather");

                getTemperatura(object);
                getDescription(arreglo);
                iconPicker(arreglo);
                getTermica(object);
                getHumedad(object);
                getPais(sys, response);
                getViento(dataViento);

            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            if (error.networkResponse != null) {
                Toast.makeText(MainActivity.this, "No se pudo acceder a su ubicacion :(", Toast.LENGTH_SHORT).show();
            } else if (error instanceof TimeoutError) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            } else if (error instanceof ServerError) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            } else if (error instanceof NoConnectionError) {
                Toast.makeText(MainActivity.this, "Revisa tu conexión a Internet", Toast.LENGTH_SHORT).show();
            } else if (error instanceof NetworkError) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            } else if (error instanceof ParseError) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        queue.add(request);
    }

    private void getTemperatura(JSONObject main) {
        try {
            temperaturaView.setText(String.format("%s°C", main.getString("temp")));
        } catch (JSONException e) {
            temperaturaView.setText("???");
        }
    }

    private void getDescription(JSONArray weather) {
        try {
            descripcion.setText(weather.getJSONObject(0).getString("description"));
        } catch (JSONException e) {
            descripcion.setText("???");
        }
    }

    private void iconPicker(JSONArray weather) {
        try {
            String iconSelect = weather.getJSONObject(0).getString("main");
            switch (iconSelect) {
                case "Clouds":
                case "Mist":
                    icon.setImageDrawable(getDrawable(R.drawable.cloud));
                    break;
                case "Rain":
                    icon.setImageDrawable(getDrawable(R.drawable.cloud_drizzle));
                    break;
                case "Thunderstorm":
                    icon.setImageDrawable(getDrawable(R.drawable.cloud_lightning));
                    break;
                default:
                    icon.setImageDrawable(getDrawable(R.drawable.sun));
                    break;
            }
        } catch (JSONException e) {
            Toast.makeText(MainActivity.this, "No se pudo encontrar un icono para el clima", Toast.LENGTH_SHORT).show();
        }
    }

    private void getTermica(JSONObject main) {
        try {
            termica.setText(String.format("%s°C", main.getString("feels_like")));
        } catch (JSONException e) {
            termica.setText("???");
        }
    }

    private void getHumedad(JSONObject main) {
        try {
            humedad.setText(String.format("%s%%", main.getString("humidity")));
        } catch (JSONException e) {
            humedad.setText("???");
        }
    }

    private void getPais(JSONObject sys, JSONObject response) {
        try {
            String country = sys.getString("country");
            String city = response.getString("name");
            cityName.setText(String.format("%s, %s", city, country));
        } catch (JSONException e) {
            cityName.setText("???, ???");
        }
    }

    private void getViento(JSONObject dataViento) {
        try {
            viento.setText(String.format("%s Km/h", dataViento.getString("speed")));
        } catch (JSONException e) {
            viento.setText("???");
        }
    }

    private String mostrarFecha() {
        long ahora = System.currentTimeMillis();
        Date fecha = new Date(ahora);
        DateFormat df = new SimpleDateFormat("dd/MM/yy");

        return df.format(fecha);
    }

}