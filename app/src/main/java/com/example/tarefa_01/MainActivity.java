package com.example.tarefa_01;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.calculos.CryptoUtils;
import com.example.calculos.Region;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    private static final long UPDATE_INTERVAL_MS = 5000;

    private Semaphore semaforo;
    private Coordinates coordenadas;
    private LatLng posicao;
    private Coordinates bufferCoordenadas;
    private Handler handler;
    private TextView texViewLatitude;
    private TextView texViewLongitude;
    private TextView textCountRegion;
    private GoogleMap myMap;
    private FirebaseFirestore db;
    private Queue<Region> filaCoordenadas;
    private Queue<Region> dadosDB = new LinkedList<>();
    private final CryptoUtils encriptador = new CryptoUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFirebase();
        initializeViews();
        initializeHandlers();

        getPermissions();

        initializeThreads();

        setWindowInsets();
        scheduleShowMap();
    }

    private void initializeFirebase() {
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        texViewLatitude = findViewById(R.id.textViewLatitude);
        texViewLongitude = findViewById(R.id.textViewLongitude);
        textCountRegion = findViewById(R.id.controleRegioes);
        Button botaoRegion = findViewById(R.id.buttonAdicionaRegiao);
        Button botaoAtualizarBD = findViewById(R.id.atualizarBancoDeDados);

        bufferCoordenadas = new Coordinates();
        bufferCoordenadas.setLatitude(0);
        bufferCoordenadas.setLongitude(0);

        botaoRegion.setOnClickListener(v -> handleAddRegionButtonClick());
        botaoAtualizarBD.setOnClickListener(v -> recordDB());
    }

    private void initializeHandlers() {
        handler = new Handler(Looper.getMainLooper());
        semaforo = new Semaphore();
    }

    private void initializeThreads() {
        Localizacao localizacao = new Localizacao(1000, this);
        RegionManager addRegionManager = new RegionManager(1000, this);
        new Thread(localizacao).start();
        new Thread(addRegionManager).start();
    }

    private void setWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void getPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void scheduleShowMap() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    showMap();
                    consultaBanco();
                } catch (InterruptedException e) {
                    Log.i("MapError", String.valueOf(e));
                }
                handler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        }, 0);
    }

    public void showMap() throws InterruptedException {
        semaforo.take();
        coordenadas = semaforo.get_coordenadas();
        posicao = new LatLng(coordenadas.getLatitude(), coordenadas.getLongitude());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        updateCoordinatesText();
        bufferCoordenadas = coordenadas;
        semaforo.release();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        if (posicao != null) {
            updateMap();
        }
    }

    private void updateMap() {
        myMap.clear();
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(posicao, 18);
        myMap.addMarker(new MarkerOptions().position(posicao).title("Posição atual"));
        myMap.moveCamera(CameraUpdateFactory.newLatLng(posicao));
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setCompassEnabled(true);
        myMap.animateCamera(yourLocation);
    }

    private void handleAddRegionButtonClick() {
        new Thread(() -> {
            try {
                semaforo.take();
                semaforo.requestRegion();
                Log.i("Requisicao", "Pediu pra gravar dnv");
                semaforo.release();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void recordDB() {
        new Thread(() -> {
            try {
                semaforo.take();
                filaCoordenadas = semaforo.getFilaCoordenadas();
                consultaBanco();

                boolean adicionouAlgo = processRegions();

                showMessage(adicionouAlgo ? "Dado(s) adicionado(s) no banco de dados" : "Falha ao adicionar o(s) dados ao banco");

                filaCoordenadas.clear();
                semaforo.setFilaCoordenadas(filaCoordenadas);
                semaforo.setNumberRegionsOnQueue(0);
                semaforo.release();
            } catch (Exception e) {
                Log.i("ERRO", String.valueOf(e));
                throw new RuntimeException(e);
            }
        }).start();
    }

    private boolean processRegions() {
        boolean adicionouAlgo = false;
        if (!filaCoordenadas.isEmpty()) {
            for (Region regiaoLocal : filaCoordenadas) {
                if (shouldAddRegionToDatabase(regiaoLocal)) {
                    adicionouAlgo = true;
                }
            }
        } else {
            showMessage("Fila de regiões vazia");
        }
        return adicionouAlgo;
    }

    private boolean shouldAddRegionToDatabase(Region regiaoLocal) {
        try {
            if (!dadosDB.isEmpty()) {
                if (regiaoLocal.verifyDistance(dadosDB)) {
                    addRegionToDatabase(regiaoLocal);
                    return true;
                } else {
                    showMessage("Coordenada dentro do raio mínimo");
                }
            } else {
                addRegionToDatabase(regiaoLocal);
                return true;
            }
        } catch (Exception e) {
            Log.e("FirestoreError", "Erro ao adicionar documento: ", e);
        }
        return false;
    }

    private void addRegionToDatabase(Region regiaoLocal) {
        Map<String, Object> data = jsonObjectToMap(regiaoLocal.getDadoEncriptado());
        db.collection("Regioes").document(regiaoLocal.getNome()).set(data);
    }

    private void consultaBanco() {
        db.collection("Regioes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i("recordDB", "Dados lidos com sucesso");
                Queue<Region> dados = new LinkedList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    Region objeto = decryptRegionData(document.getData());
                    if (objeto != null) {
                        dados.add(objeto);
                    }
                }
                dadosDB = dados;
            } else {
                Log.w("Firebase", "Erro ao obter documentos.", task.getException());
            }
        });
    }

    private Region decryptRegionData(Map<String, Object> data) {
        if (data == null) return null;

        if (data.containsKey("mainRegion")) {
            if (data.containsKey("Restricted")) {
                return encriptador.decryptRestrictedRegion(data);
            } else {
                return encriptador.decryptSubRegion(data);
            }
        } else {
            return encriptador.decryptRegion(data);
        }
    }

    private void updateCoordinatesText() {
        String textoLatitude = "Latitude: " + coordenadas.getLatitude();
        String textoLongitude = "Longitude: " + coordenadas.getLongitude();
        texViewLatitude.setText(textoLatitude);
        texViewLongitude.setText(textoLongitude);
        textCountRegion.setText("Regiões adicionadas: " + semaforo.getNumberRegionsOnQueue());
    }

    private void showMessage(final String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private Map<String, Object> jsonObjectToMap(JsonObject jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(jsonObject, type);
    }
}
