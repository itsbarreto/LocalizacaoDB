package priceranking.app.italobarreto.localizacaodb.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Field;

import priceranking.app.italobarreto.localizacaodb.R;
import priceranking.app.italobarreto.localizacaodb.factories.LocalFactory;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mercadoReferencia = dbRef.child("mercados");

    private FirebaseAuth firebaseAuth;
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
                showMessageOKCancel("Precisamos da sua localizacao para sabermos em qual mercado voce esta",
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
        pesquisaLugarAtual(Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null));

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
                StringBuffer lugares =  new StringBuffer("Probabilidade acima de 0.5");
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    if(placeLikelihood.getLikelihood() > 0.5){
                        lugares.append(LocalFactory.montaStringLugar(placeLikelihood.getPlace(),placeLikelihood.getLikelihood()));
                    }
                }
                tvLugar.setText(lugares.toString());
                likelyPlaces.release();
            }
        });
    }


    private void salvaMercado(){
        /*
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
        m.setLatitude(12.23);
        m.setLongitue(34.23);
        m.setNmMercado("Carrefour");
        m.setUrlImg("https://fontmeme.com/images/Carrefour-Logo.jpg");
        mercadoReferencia.child("abc").setValue(m);
        */

    }




    /**
     * Atribuicao dos elementos da tela as variaveis.
     */
    private void configuraElementos(){
        tvUsuario = (TextView) findViewById(R.id.id_nm_usu);
        tvLugar = (TextView) findViewById(R.id.id_tx_local);
    }


    /**
     * Pede para logar caso nao esteja logado.
     */
    private void verificacaoDeLogin(){
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() == null) {
            Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(myIntent);

        }

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        tvLugar.setText("Erro para pegar o lugar\n" + connectionResult.getErrorMessage());
    }
}
