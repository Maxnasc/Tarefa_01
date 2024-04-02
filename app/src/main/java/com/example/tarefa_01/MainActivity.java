package com.example.tarefa_01;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
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
    private Queue<RegionObject> filaCoordenadas;

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
                // Solicita a adição de região para a classe Region através do semáforo
                // Bloqueia a execução até que o semáforo seja liberado
                semaforo.take();

                // Obtém as coordenadas atuais
                semaforo.requestRegion();

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
                // Solicita a adição de região para a classe Region através do semáforo
                // Bloqueia a execução até que o semáforo seja liberado
                semaforo.take();

                // Obtém as coordenadas atuais
                semaforo.requestRegion();
                filaCoordenadas = semaforo.getFilaCoordenadas();

                if (filaCoordenadas != null) {
                    if (!filaCoordenadas.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Dados enviados para o banco com sucesso", Toast.LENGTH_SHORT).show();

                        for (RegionObject coord : filaCoordenadas) {
                            // Montando objeto de rota
                            HashMap<String, String> dado = new HashMap<>();
                            dado.put("nome", coord.getNome());
                            dado.put("latitude", String.valueOf(coord.getPosixLatitude()));
                            dado.put("longitude", String.valueOf(coord.getPosixLongitude()));
                            dado.put("user", String.valueOf(coord.getUser()));
                            dado.put("timestamp", String.valueOf(coord.getTimestamp()));
                            // Publicando dados
                            db.collection("Regioes").document(coord.getNome()).set(dado).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(MainActivity.this, "Dados enviados para o banco com sucesso", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Falha ao enviar dados para o banco de dados", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        filaCoordenadas.clear();
                        semaforo.setFilaCoordenadas(filaCoordenadas);
                    } else {
                        Toast.makeText(MainActivity.this, "Impossível gravar no banco, fila vazia", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Impossível gravar no banco, fila vazia", Toast.LENGTH_SHORT).show();
                }

                // Libera o semáforo
                try {
                    semaforo.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Configura o listener de clique para o botão
        botaoAtualizarBD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Solicita a adição de região para a classe Region através do semáforo
                // Bloqueia a execução até que o semáforo seja liberado
                semaforo.take();

                // Obtém as coordenadas atuais
                semaforo.requestDataBase();

                // Libera o semáforo
                try {
                    semaforo.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Instancianado variáveis
        Localizacao localizacao = new Localizacao(1000, MainActivity.this);
        Region addRegion = new Region(1000, MainActivity.this);
        semaforo = new Semaphore();

        // Iniciando as Threads
        Thread t1 = new Thread(localizacao);
        Thread t2 = new Thread(addRegion);
        t1.start();
        t2.start();

        // Inicializa o Handler para agendar a execução da função showMap() periodicamente
        handler = new Handler();

        // Configuração da WebView
//        wv = findViewById(R.id.webv);
//        wv.getSettings().setJavaScriptEnabled(true);

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

        //Log.e("Localizacao", "Latitude: " + coordenadas.getLatitude() + " | Longitude: " + coordenadas.getLongitude());

        //if ((coordenadas.getLatitude() != bufferCoordenadas.getLatitude()) && (coordenadas.getLongitude() != bufferCoordenadas.getLatitude())) {
            // Exibe o mapa com as coordenadas atualizadas
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
        //}

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
}
