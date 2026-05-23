package com.example.maptracker;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapsActivity extends FragmentActivity {

    private MapView carteVue;

    private Marker marqueurActuel;

    private static final double NIVEAU_ZOOM = 15.0;

    private static final int CODE_DEMANDE_PERMISSION = 200;

    private LocationManager gestionnaireLocalisation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_main);

        carteVue = findViewById(R.id.carte_osm);

        configurerCarte();

        gestionnaireLocalisation =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean autorisationAccordee =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        if (autorisationAccordee) {
            demarrerSuiviPosition();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODE_DEMANDE_PERMISSION
            );
        }
    }

    private void configurerCarte() {
        carteVue.setTileSource(TileSourceFactory.MAPNIK);

        carteVue.setMultiTouchControls(true);

        carteVue.getZoomController().setVisibility(
                org.osmdroid.views.CustomZoomButtonsController.Visibility.ALWAYS
        );

        GeoPoint coordDepart = new GeoPoint(31.6295, -7.9811);
        carteVue.getController().setZoom(NIVEAU_ZOOM);
        carteVue.getController().setCenter(coordDepart);

        marqueurActuel = new Marker(carteVue);
        marqueurActuel.setPosition(coordDepart);
        marqueurActuel.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marqueurActuel.setTitle("Position de départ");
        carteVue.getOverlays().add(marqueurActuel);

        Toast.makeText(this, "Carte chargée ✓", Toast.LENGTH_SHORT).show();
    }

    private void demarrerSuiviPosition() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        gestionnaireLocalisation.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000,
                0,
                ecouteurPosition
        );
    }

    private final LocationListener ecouteurPosition = new LocationListener() {

        @Override
        public void onLocationChanged(Location nouvellePosition) {
            double latitudeRecue  = nouvellePosition.getLatitude();
            double longitudeRecue = nouvellePosition.getLongitude();

            Toast.makeText(
                    getApplicationContext(),
                    "📍 " + latitudeRecue + " / " + longitudeRecue,
                    Toast.LENGTH_SHORT
            ).show();

            GeoPoint nouvelleCoord = new GeoPoint(latitudeRecue, longitudeRecue);

            if (marqueurActuel == null) {
                marqueurActuel = new Marker(carteVue);
                marqueurActuel.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marqueurActuel.setTitle("Position actuelle");
                carteVue.getOverlays().add(marqueurActuel);
            }

            marqueurActuel.setPosition(nouvelleCoord);

            carteVue.getController().animateTo(nouvelleCoord);
            carteVue.getController().setZoom(NIVEAU_ZOOM);

            carteVue.invalidate();
        }

        @Override
        public void onStatusChanged(String fournisseur, int etat, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String fournisseur) {
            Toast.makeText(getApplicationContext(),
                    "Localisation activée ✓", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String fournisseur) {
            afficherDialogueGpsDesactive();
        }
    };

    private void afficherDialogueGpsDesactive() {
        AlertDialog.Builder constructeurDialog = new AlertDialog.Builder(this);
        constructeurDialog
                .setMessage("La localisation semble désactivée. Voulez-vous l'activer ?")
                .setCancelable(false)
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        ));
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        constructeurDialog.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int codeRequete,
                                           String[] listePermissions,
                                           int[] tableauResultats) {
        super.onRequestPermissionsResult(codeRequete, listePermissions, tableauResultats);

        if (codeRequete == CODE_DEMANDE_PERMISSION) {
            boolean permissionOK = tableauResultats.length > 0
                    && tableauResultats[0] == PackageManager.PERMISSION_GRANTED;

            if (permissionOK) {
                Toast.makeText(this,
                        "Permission accordée ✓", Toast.LENGTH_SHORT).show();
                demarrerSuiviPosition();
            } else {
                Toast.makeText(this,
                        "Permission refusée — localisation impossible.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        carteVue.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        carteVue.onPause();
        gestionnaireLocalisation.removeUpdates(ecouteurPosition);
    }
}