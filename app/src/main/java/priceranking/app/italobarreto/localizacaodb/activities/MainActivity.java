package priceranking.app.italobarreto.localizacaodb.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import priceranking.app.italobarreto.localizacaodb.R;
import priceranking.app.italobarreto.localizacaodb.factories.LocalFactory;
import priceranking.app.italobarreto.localizacaodb.pojo.MercadoPojo;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mercadoReferencia = dbRef.child("mercados");

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private GoogleApiClient mGoogleApiClient;


    private TextView tvUsuario;
    private TextView tvLugar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        verificacaoDeLogin();
        configuraElementos();


        //Texto com o nome do usuario
        tvUsuario.setText(firebaseAuth.getCurrentUser().getEmail());

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        localizacaoAtual();


    }




    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;


    private void localizacaoAtual() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showMessageOKCancel(getString(R.string.str_confirm_lclz),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_CODE_ASK_PERMISSIONS);

                            }
                        });
                return;
            }
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Verifica se o GPS está ativo
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // Caso não esteja ativo abre um novo diálogo com as configurações para
        // realizar se ativamento
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            showMessageOKCancel(getString(R.string.str_confirm_lclz),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Roda a pesqisa de lugar com um timer de 2 segundos.
                            new Timer().schedule(
                                        new TimerTask() {
                                            @Override
                                            public void run() {
                                                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                                    pesquisaLugarAtual(Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null));
                                                }
                                            }
                                        }
                                        , 2000);
                            }
                    });
        }
        else{
            pesquisaLugarAtual(Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null));
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void pesquisaLugarAtual(PendingResult<PlaceLikelihoodBuffer> result) {

        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                StringBuffer lugares = new StringBuffer("Todos os lugares");
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    lugares.append(LocalFactory.montaStringLugar(placeLikelihood.getPlace(), placeLikelihood.getLikelihood()));
                }
                lugares.append("\n\n------------------------------\nMercados\n------------------------------\n");
                ArrayList<Place> lugaresProvaveis = mercadosPossiveis(likelyPlaces);
                for (Place p : lugaresProvaveis) {
                    lugares.append(LocalFactory.montaStringLugar(p));
                }
                tvLugar.setText(lugares.toString());
                likelyPlaces.release();
            }
        });
    }

    /**
     * Percorre os lugares e verifica qual eh o mais provavel de o cliente estar.
     * Passos:
     * 01 - Cria <code>ArrayList</code> com os lugares.
     * 02 - Ordena a lista de forma decrescente.
     * 03 - Filtra por tipo de lugar.
     *
     * @param likelyPlaces
     * @return : mercado mais provavel
     */
    private ArrayList<Place> mercadosPossiveis(PlaceLikelihoodBuffer likelyPlaces) {

        ArrayList<PlaceLikelihood> lugaresOrdenadosProb = new ArrayList<>();
        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
            lugaresOrdenadosProb.add(placeLikelihood);
        }
        Collections.sort(lugaresOrdenadosProb, new Comparator<PlaceLikelihood>() {
            @Override
            public int compare(PlaceLikelihood p1, PlaceLikelihood p2) {
                return p1.getLikelihood() < p2.getLikelihood() ? -1 : (p1.getLikelihood() > p2.getLikelihood() ? +1 : 0);
            }
        });

        ArrayList<Place> lugares = new ArrayList<>();
        for (PlaceLikelihood p : lugaresOrdenadosProb) {

            if (p.getLikelihood() == 0.0 && !lugares.isEmpty()) {
                break;
            }
            if (p.getPlace().getPlaceTypes().contains(Place.TYPE_ESTABLISHMENT)) {
                if (p.getPlace().getPlaceTypes().contains(Place.TYPE_GROCERY_OR_SUPERMARKET)) {
                    lugares.add(p.getPlace());
                    if (p.getLikelihood() > 0.85) {
                        break;
                    }

                } else if (p.getPlace().getPlaceTypes().contains(Place.TYPE_STORE)) {
                    lugares.add(p.getPlace());
                }
            }

        }
        return lugares;
    }



    private void salvaMercado(Place lugar, String descricao) {

        mercadoReferencia.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("FIREBASE", dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FIREBASE_ERRO", databaseError.getMessage());
            }
        });
        MercadoPojo m = new MercadoPojo();
        m.setLatitude(lugar.getLatLng().latitude);
        m.setLongitue(lugar.getLatLng().longitude);
        m.setNmMercado(lugar.getName().toString());
        m.setUrlImg(descricao);
        m.setUsuIdReg(firebaseUser.getUid());
        m.setMsDateIncl(new Date().getTime());
        mercadoReferencia.child(lugar.getId()).setValue(m);


    }


    /**
     * Atribuicao dos elementos da tela as variaveis.
     */
    private void configuraElementos() {
        tvUsuario = (TextView) findViewById(R.id.id_nm_usu);
        tvLugar = (TextView) findViewById(R.id.id_tx_local);
    }


    /**
     * Pede para logar caso nao esteja logado.
     */
    private void verificacaoDeLogin() {
        firebaseAuth = FirebaseAuth.getInstance();

        if ((firebaseUser = firebaseAuth.getCurrentUser()) == null) {
            Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(myIntent);

        }


    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        tvLugar.setText("Erro para pegar o lugar\n" + connectionResult.getErrorMessage());
    }
}
