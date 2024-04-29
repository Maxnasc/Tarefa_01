package com.example.tarefa_01;

import static java.util.Currency.getInstance;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.calculos.CryptoUtils;
import com.example.calculos.Region;
import com.example.calculos.RestrictedRegion;
import com.example.calculos.SubRegion;
import com.example.calculos.Utils;
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

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Semaphore semaforo;
    private Coordinates coordenadas;
    private LatLng posicao;
    private Coordinates bufferCoordenadas;
    private Handler handler;
    private TextView texViewLatitude;
    private TextView texViewLongitude;
    private TextView textCountRegion;
    private Button botaoRegion;
    private Button botaoAtualizarBD;
    private GoogleMap myMap;
    private FirebaseFirestore db;
    private Queue<Region> filaCoordenadas;
    private Queue<Region> dadosDB = new LinkedList<>();
    private Utils utils = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FirebaseApp.initializeApp(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa os TextViews após inflar o layout
        texViewLatitude = findViewById(R.id.textViewLatitude);
        texViewLongitude = findViewById(R.id.textViewLongitude);
        textCountRegion = findViewById(R.id.controleRegioes);
        botaoRegion = findViewById(R.id.buttonAdicionaRegiao);
        botaoAtualizarBD = findViewById(R.id.atualizarBancoDeDados);

        //Inicia o bufferCoordenadas
        bufferCoordenadas = new Coordinates();
        bufferCoordenadas.setLatitude(0);
        bufferCoordenadas.setLongitude(0);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        // Configura os listeners de clique para o botão
        botaoRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bloqueia a execução até que o semáforo seja liberado
                semaforo.take();

                // Obtém as coordenadas atuais
                semaforo.requestRegion();
                Log.i("Requisicao", "Pediu pra gravar dnv");

                // Libera o semáforo
                try {
                    semaforo.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        botaoAtualizarBD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordDB();
            }
        });

        // Instancianado variáveis
        Localizacao localizacao = new Localizacao(1000, MainActivity.this);
        RegionManager addRegionManager = new RegionManager(1000, MainActivity.this);
        semaforo = new Semaphore();

        // Iniciando as Threads
        Thread t1 = new Thread(localizacao);
        Thread t2 = new Thread(addRegionManager);
        t1.start();
        t2.start();

        // Inicializa o Handler para agendar a execução da função showMap() periodicamente
        handler = new Handler();

        // Chamada do método onCreate
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicia o agendamento da execução da função showMap()
        scheduleShowMap();
    }

    // Método para agendar a execução da função showMap() em intervalos regulares
    private void scheduleShowMap() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // Chama a função showMap() para atualizar o mapa
                    showMap();
                    // Faz a consulta no banco para atualizar o app
                    consultaBanco();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Agenda a próxima execução da função showMap() após um intervalo de tempo (por exemplo, 5 segundos)
                handler.postDelayed(this, 5000); // 5000 milissegundos = 5 segundos
            }
        }, 0); // Executa a primeira chamada imediatamente
    }

    public void showMap() throws InterruptedException {

        // Bloqueia a execução até que o semáforo seja liberado
        semaforo.take();

        // Obtém as coordenadas atuais
        coordenadas = semaforo.get_coordenadas();
        posicao = new LatLng(coordenadas.getLatitude(), coordenadas.getLongitude());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Atualiza os TextViews com as coordenadas
        String textoLatitude = "Latitude: " + coordenadas.getLatitude();
        String textoLongitude = "Longitude: " + coordenadas.getLongitude();
        texViewLatitude.setText(textoLatitude);
        texViewLongitude.setText(textoLongitude);
        textCountRegion.setText("Regiões adicionadas: " + semaforo.getNumberRegionsOnQueue());
        bufferCoordenadas = coordenadas;

        // Libera o semáforo
        semaforo.release();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        myMap = googleMap;
        if (posicao != null) {
            myMap.clear();
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(posicao, 18);
            myMap.addMarker(new MarkerOptions().position(posicao).title("Posição atual"));
            myMap.moveCamera(CameraUpdateFactory.newLatLng(posicao));
            myMap.getUiSettings().setZoomControlsEnabled(true);
            myMap.getUiSettings().setCompassEnabled(true);
            myMap.animateCamera(yourLocation);
        }
    }

    private void recordDB() {
        new Thread(() -> {
            try {
                semaforo.take();
                boolean adicionouAlgo = false;
                filaCoordenadas = semaforo.getFilaCoordenadas();
                consultaBanco();

                if (!filaCoordenadas.isEmpty()) {
                for (Region regiaoLocal : filaCoordenadas) {
                    if (!dadosDB.isEmpty()) {
                        if (regiaoLocal.verifyDistance(dadosDB)) {
                            db.collection("Regioes").document(regiaoLocal.getNome()).set(regiaoLocal.getAllDataForDatabase());
                            adicionouAlgo = true;
                        } else {
                            showMessage("Coordenada dentro do raio mínimo");
                        }
                    } else {
                        // Adiciona primeiro termo
                        db.collection("Regioes").document(regiaoLocal.getNome()).set(regiaoLocal.getAllDataForDatabase());
                        adicionouAlgo = true;
                    }
                }} else {
                    showMessage("Fila de regiões vazia");
                }

                if (adicionouAlgo) {
                    showMessage("Dado(s) adicionado(s) no banco de dados");
                } else {
                    showMessage("Falha ao adicionar o(s) dados ao banco");
                }

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

//    private double distanceToNearestCoordinate(Region regiaoDoBanco, Region regiaoLocal) {
//        double distancia = regiaoLocal.calcularDistancia(regiaoDoBanco.getPosixLatitude(), regiaoDoBanco.getPosixLongitude(), regiaoLocal.getPosixLatitude(), regiaoLocal.getPosixLongitude());
//        return distancia;
//    }

    private void consultaBanco() {
        Queue<Region> dados = new LinkedList<>();
        new Thread(() -> {
            db.collection("Regioes").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.i("recordDB", "Dados lidos com sucesso");
                    for (DocumentSnapshot document : task.getResult()) {
                        Region objeto = null;
                        Map<String, Object> data = document.getData();
                        if (data != null) {
                            // Verificar o tipo do objeto
                            if (data.containsKey("mainRegion")) {
                                if (data.containsKey("Restricted")) {
                                    //objeto = new RestrictedRegion((String) data.get("nome"), (Double) data.get("posixLatitude"), (Double) data.get("posixLongitude"), (int) data.get("user"), (long) data.get("timestamp"), (Region) data.get("mainRegion"));
                                    objeto = document.toObject(RestrictedRegion.class);
                                } else {
                                    //objeto = new SubRegion((String) data.get("nome"), (Double) data.get("posixLatitude"), (Double) data.get("posixLongitude"), (int) data.get("user"), (long) data.get("timestamp"), (Region) data.get("mainRegion"));
                                    objeto = document.toObject(SubRegion.class);
                                }
                            } else {
                                //objeto = new Region((String) data.get("nome"), (Double) data.get("posixLatitude"), (Double) data.get("posixLongitude"), (int) data.get("user"), (long) data.get("timestamp"));
                                objeto = document.toObject(Region.class);
                            }
                        }
                        if (objeto != null) {
                            dados.add(objeto);
                        }
                    }
                    this.dadosDB = dados;
                } else {
                    Log.w("Firebase", "Erro ao obter documentos.", task.getException());
                }
            });
        }).start();
    }

    private void showMessage(final String message) {
        (MainActivity.this).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
