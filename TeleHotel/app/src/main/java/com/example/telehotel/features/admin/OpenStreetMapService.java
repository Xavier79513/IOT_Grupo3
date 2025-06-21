package com.example.telehotel.features.admin;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.example.telehotel.data.model.LatLng;
import com.example.telehotel.data.model.LugarHistorico;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*public class OpenStreetMapService {
    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/";
    private static final String OVERPASS_BASE_URL = "https://overpass-api.de/api/interpreter";

    // Geocodificación: Dirección -> Coordenadas
    public static void buscarCoordenadas(String direccion, OnLocationFoundListener listener) {
        new AsyncTask<String, Void, LocationResult>() {
            @Override
            protected LocationResult doInBackground(String... params) {
                try {
                    String query = URLEncoder.encode(params[0], "UTF-8");
                    String url = NOMINATIM_BASE_URL + "search?format=json&q=" + query + "&limit=1";

                    String response = makeHttpRequest(url);
                    JSONArray results = new JSONArray(response);

                    if (results.length() > 0) {
                        JSONObject location = results.getJSONObject(0);
                        double lat = location.getDouble("lat");
                        double lon = location.getDouble("lon");
                        String displayName = location.getString("display_name");

                        return new LocationResult(lat, lon, displayName, true);
                    }

                    return new LocationResult(0, 0, "No encontrado", false);

                } catch (Exception e) {
                    Log.e("OSMService", "Error en geocodificación", e);
                    return new LocationResult(0, 0, "Error: " + e.getMessage(), false);
                }
            }

            @Override
            protected void onPostExecute(LocationResult result) {
                listener.onLocationFound(result);
            }
        }.execute(direccion);
    }

    // Geocodificación inversa: Coordenadas -> Dirección
    public static void buscarDireccion(double lat, double lng, OnAddressFoundListener listener) {
        new AsyncTask<Double, Void, String>() {
            @Override
            protected String doInBackground(Double... params) {
                try {
                    String url = NOMINATIM_BASE_URL + "reverse?format=json&lat=" +
                            params[0] + "&lon=" + params[1];

                    String response = makeHttpRequest(url);
                    JSONObject result = new JSONObject(response);

                    return result.getString("display_name");

                } catch (Exception e) {
                    Log.e("OSMService", "Error en geocodificación inversa", e);
                    return "Coordenadas: " + params[0] + ", " + params[1];
                }
            }

            @Override
            protected void onPostExecute(String address) {
                listener.onAddressFound(address);
            }
        }.execute(lat, lng);
    }

    // Buscar lugares históricos cercanos usando Overpass API
    public static void buscarLugaresHistoricos(double lat, double lng, double radiusKm,
                                               OnHistoricalPlacesFoundListener listener) {
        new AsyncTask<Double, Void, List<LugarHistorico>>() {
            @Override
            protected List<LugarHistorico> doInBackground(Double... params) {
                List<LugarHistorico> lugares = new ArrayList<>();

                try {
                    // Query de Overpass para buscar lugares históricos
                    String overpassQuery = construirQueryOverpass(params[0], params[1], params[2]);

                    String response = makeHttpPostRequest(OVERPASS_BASE_URL, overpassQuery);
                    JSONObject result = new JSONObject(response);
                    JSONArray elements = result.getJSONArray("elements");

                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject element = elements.getJSONObject(i);

                        if (element.has("tags")) {
                            JSONObject tags = element.getJSONObject("tags");

                            String nombre = tags.optString("name", "Lugar sin nombre");
                            String descripcion = obtenerDescripcion(tags);
                            String tipoLugar = obtenerTipoLugar(tags);

                            double lugarLat = element.getDouble("lat");
                            double lugarLng = element.getDouble("lon");

                            // Calcular distancia
                            float distancia = calcularDistancia(params[0], params[1], lugarLat, lugarLng);

                            LugarHistorico lugar = new LugarHistorico(
                                    nombre, descripcion,
                                    new LatLng(lugarLat, lugarLng),
                                    distancia, tipoLugar
                            );

                            lugares.add(lugar);
                        }
                    }

                    // Ordenar por distancia
                    Collections.sort(lugares, (a, b) -> Float.compare(a.getDistancia(), b.getDistancia()));

                } catch (Exception e) {
                    Log.e("OSMService", "Error buscando lugares históricos", e);
                }

                return lugares;
            }

            @Override
            protected void onPostExecute(List<LugarHistorico> lugares) {
                listener.onHistoricalPlacesFound(lugares);
            }
        }.execute(lat, lng, radiusKm);
    }

    private static String construirQueryOverpass(double lat, double lng, double radiusKm) {
        int radiusMetros = (int) (radiusKm * 1000);

        return "[out:json][timeout:25];" +
                "(node[\"tourism\"~\"^(attraction|museum|artwork|memorial)$\"](around:" + radiusMetros + "," + lat + "," + lng + ");" +
                " node[\"historic\"](around:" + radiusMetros + "," + lat + "," + lng + ");" +
                " node[\"amenity\"=\"place_of_worship\"](around:" + radiusMetros + "," + lat + "," + lng + ");" +
                " way[\"tourism\"~\"^(attraction|museum|artwork|memorial)$\"](around:" + radiusMetros + "," + lat + "," + lng + ");" +
                " way[\"historic\"](around:" + radiusMetros + "," + lat + "," + lng + ");" +
                ");" +
                "out center;";
    }

    private static String obtenerDescripcion(JSONObject tags) throws JSONException {
        // Priorizar diferentes campos de descripción
        String[] camposDescripcion = {"description", "description:es", "inscription", "information"};

        for (String campo : camposDescripcion) {
            if (tags.has(campo)) {
                return tags.optString(campo);
            }
        }

        // Si no hay descripción, crear una basada en los tags
        StringBuilder desc = new StringBuilder();

        if (tags.has("historic")) {
            desc.append("Sitio histórico: ").append(tags.getString("historic"));
        } else if (tags.has("tourism")) {
            desc.append("Atracción turística: ").append(tags.getString("tourism"));
        } else if (tags.has("amenity")) {
            desc.append("Lugar de interés: ").append(tags.getString("amenity"));
        }

        return desc.length() > 0 ? desc.toString() : "Lugar de interés histórico";
    }

    private static String obtenerTipoLugar(JSONObject tags) throws JSONException {
        if (tags.has("historic")) {
            String historic = tags.getString("historic");
            switch (historic) {
                case "monument": return "Monumento";
                case "memorial": return "Memorial";
                case "building": return "Edificio histórico";
                case "castle": return "Castillo";
                case "church": return "Iglesia histórica";
                case "archaeological_site": return "Sitio arqueológico";
                default: return "Sitio histórico";
            }
        } else if (tags.has("tourism")) {
            String tourism = tags.getString("tourism");
            switch (tourism) {
                case "museum": return "Museo";
                case "attraction": return "Atracción turística";
                case "artwork": return "Arte público";
                case "memorial": return "Memorial";
                default: return "Lugar turístico";
            }
        } else if (tags.has("amenity") && tags.getString("amenity").equals("place_of_worship")) {
            return "Lugar de culto";
        }

        return "Lugar de interés";
    }

    private static float calcularDistancia(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0] / 1000; // Convertir a kilómetros
    }

    private static String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "HotelApp/1.0");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        connection.disconnect();

        return response.toString();
    }

    private static String makeHttpPostRequest(String urlString, String postData) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("User-Agent", "HotelApp/1.0");
        connection.setDoOutput(true);

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write("data=" + URLEncoder.encode(postData, "UTF-8"));
        writer.flush();
        writer.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        connection.disconnect();

        return response.toString();
    }

    // Interfaces para callbacks
    public interface OnLocationFoundListener {
        void onLocationFound(LocationResult result);
    }

    public interface OnAddressFoundListener {
        void onAddressFound(String address);
    }

    public interface OnHistoricalPlacesFoundListener {
        void onHistoricalPlacesFound(List<LugarHistorico> lugares);
    }

    // Clase para resultado de ubicación
    public static class LocationResult {
        public double latitude;
        public double longitude;
        public String address;
        public boolean success;

        public LocationResult(double lat, double lng, String addr, boolean success) {
            this.latitude = lat;
            this.longitude = lng;
            this.address = addr;
            this.success = success;
        }
    }
}*/
/*public class OpenStreetMapService {
    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/";
    private static final String OVERPASS_BASE_URL = "https://overpass-api.de/api/interpreter";

    // Geocodificación: Dirección -> Coordenadas
    public static void buscarCoordenadas(String direccion, OnLocationFoundListener listener) {
        new AsyncTask<String, Void, LocationResult>() {
            @Override
            protected LocationResult doInBackground(String... params) {
                try {
                    String query = URLEncoder.encode(params[0], "UTF-8");
                    String url = NOMINATIM_BASE_URL + "search?format=json&q=" + query + "&limit=1";

                    String response = makeHttpRequest(url);
                    JSONArray results = new JSONArray(response);

                    if (results.length() > 0) {
                        JSONObject location = results.getJSONObject(0);
                        double lat = location.getDouble("lat");
                        double lon = location.getDouble("lon");
                        String displayName = location.getString("display_name");

                        return new LocationResult(lat, lon, displayName, true);
                    }

                    return new LocationResult(0, 0, "No encontrado", false);

                } catch (Exception e) {
                    Log.e("OSMService", "Error en geocodificación", e);
                    return new LocationResult(0, 0, "Error: " + e.getMessage(), false);
                }
            }

            @Override
            protected void onPostExecute(LocationResult result) {
                listener.onLocationFound(result);
            }
        }.execute(direccion);
    }

    // Geocodificación inversa: Coordenadas -> Dirección
    public static void buscarDireccion(double lat, double lng, OnAddressFoundListener listener) {
        new AsyncTask<Double, Void, String>() {
            @Override
            protected String doInBackground(Double... params) {
                try {
                    String url = NOMINATIM_BASE_URL + "reverse?format=json&lat=" +
                            params[0] + "&lon=" + params[1];

                    String response = makeHttpRequest(url);
                    JSONObject result = new JSONObject(response);

                    return result.getString("display_name");

                } catch (Exception e) {
                    Log.e("OSMService", "Error en geocodificación inversa", e);
                    return "Coordenadas: " + params[0] + ", " + params[1];
                }
            }

            @Override
            protected void onPostExecute(String address) {
                listener.onAddressFound(address);
            }
        }.execute(lat, lng);
    }

    // Buscar lugares históricos cercanos usando Overpass API
    public static void buscarLugaresHistoricos(double lat, double lng, double radiusKm,
                                               OnHistoricalPlacesFoundListener listener) {
        new AsyncTask<Double, Void, List<LugarHistorico>>() {
            @Override
            protected List<LugarHistorico> doInBackground(Double... params) {
                List<LugarHistorico> lugares = new ArrayList<>();

                try {
                    // Query de Overpass para buscar lugares históricos
                    String overpassQuery = construirQueryOverpass(params[0], params[1], params[2]);

                    String response = makeHttpPostRequest(OVERPASS_BASE_URL, overpassQuery);
                    JSONObject result = new JSONObject(response);
                    JSONArray elements = result.getJSONArray("elements");

                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject element = elements.getJSONObject(i);

                        if (element.has("tags")) {
                            JSONObject tags = element.getJSONObject("tags");

                            String nombre = tags.optString("name", "");

                            // Filtrar lugares sin nombre o con nombres muy cortos
                            if (nombre.isEmpty() || nombre.length() < 3) {
                                continue;
                            }

                            String descripcion = obtenerDescripcion(tags);
                            String tipoLugar = obtenerTipoLugar(tags);

                            double lugarLat, lugarLng;

                            // Manejar diferentes tipos de elementos (node, way)
                            if (element.has("lat") && element.has("lon")) {
                                lugarLat = element.getDouble("lat");
                                lugarLng = element.getDouble("lon");
                            } else if (element.has("center")) {
                                JSONObject center = element.getJSONObject("center");
                                lugarLat = center.getDouble("lat");
                                lugarLng = center.getDouble("lon");
                            } else {
                                continue; // Saltar si no tiene coordenadas
                            }

                            // Calcular distancia real
                            float distancia = calcularDistancia(params[0], params[1], lugarLat, lugarLng);

                            // Filtrar lugares que estén realmente dentro del radio
                            if (distancia <= params[2]) {
                                LugarHistorico lugar = new LugarHistorico(
                                        nombre, descripcion,
                                        new LatLng(lugarLat, lugarLng),
                                        distancia, tipoLugar
                                );

                                lugares.add(lugar);
                            }
                        }
                    }

                    // Ordenar por distancia y limitar a 8 lugares
                    Collections.sort(lugares, (a, b) -> Float.compare(a.getDistancia(), b.getDistancia()));

                    // Limitar a máximo 8 lugares
                    if (lugares.size() > 8) {
                        lugares = lugares.subList(0, 8);
                    }

                } catch (Exception e) {
                    Log.e("OSMService", "Error buscando lugares históricos", e);
                }

                return lugares;
            }

            @Override
            protected void onPostExecute(List<LugarHistorico> lugares) {
                listener.onHistoricalPlacesFound(lugares);
            }
        }.execute(lat, lng, radiusKm);
    }

    private static String construirQueryOverpass(double lat, double lng, double radiusKm) {
        int radiusMetros = (int) (radiusKm * 1000);

        // Query más específica y limitada
        return "[out:json][timeout:15];" +
                "[bbox:" + (lat - 0.01) + "," + (lng - 0.01) + "," + (lat + 0.01) + "," + (lng + 0.01) + "];" +
                "(" +
                // Lugares históricos específicos
                "  node[\"historic\"~\"^(monument|memorial|building|castle|church|archaeological_site|ruins|fort)$\"]" +
                "    [\"name\"~\".+\"](around:" + radiusMetros + "," + lat + "," + lng + ");" +
                // Museos y atracciones turísticas
                "  node[\"tourism\"~\"^(museum|attraction|artwork)$\"]" +
                "    [\"name\"~\".+\"](around:" + radiusMetros + "," + lat + "," + lng + ");" +
                // Lugares de culto importantes
                "  node[\"amenity\"=\"place_of_worship\"]" +
                "    [\"historic\"~\".+\"][\"name\"~\".+\"](around:" + radiusMetros + "," + lat + "," + lng + ");" +
                // Ways (edificios, etc.)
                "  way[\"historic\"~\"^(monument|memorial|building|castle|church|archaeological_site)$\"]" +
                "    [\"name\"~\".+\"](around:" + radiusMetros + "," + lat + "," + lng + ");" +
                "  way[\"tourism\"~\"^(museum|attraction)$\"]" +
                "    [\"name\"~\".+\"](around:" + radiusMetros + "," + lat + "," + lng + ");" +
                ");" +
                "out center 20;"; // Limitar resultados a 20 para luego filtrar a 8
    }

    private static String obtenerDescripcion(JSONObject tags) throws JSONException {
        // Priorizar diferentes campos de descripción
        String[] camposDescripcion = {"description", "description:es", "inscription", "information"};

        for (String campo : camposDescripcion) {
            if (tags.has(campo)) {
                return tags.optString(campo);
            }
        }

        // Si no hay descripción, crear una basada en los tags
        StringBuilder desc = new StringBuilder();

        if (tags.has("historic")) {
            desc.append("Sitio histórico: ").append(tags.getString("historic"));
        } else if (tags.has("tourism")) {
            desc.append("Atracción turística: ").append(tags.getString("tourism"));
        } else if (tags.has("amenity")) {
            desc.append("Lugar de interés: ").append(tags.getString("amenity"));
        }

        return desc.length() > 0 ? desc.toString() : "Lugar de interés histórico";
    }

    private static String obtenerTipoLugar(JSONObject tags) throws JSONException {
        if (tags.has("historic")) {
            String historic = tags.getString("historic");
            switch (historic) {
                case "monument": return "Monumento";
                case "memorial": return "Memorial";
                case "building": return "Edificio histórico";
                case "castle": return "Castillo";
                case "church": return "Iglesia histórica";
                case "archaeological_site": return "Sitio arqueológico";
                default: return "Sitio histórico";
            }
        } else if (tags.has("tourism")) {
            String tourism = tags.getString("tourism");
            switch (tourism) {
                case "museum": return "Museo";
                case "attraction": return "Atracción turística";
                case "artwork": return "Arte público";
                case "memorial": return "Memorial";
                default: return "Lugar turístico";
            }
        } else if (tags.has("amenity") && tags.getString("amenity").equals("place_of_worship")) {
            return "Lugar de culto";
        }

        return "Lugar de interés";
    }

    private static float calcularDistancia(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0] / 1000; // Convertir a kilómetros
    }

    private static String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "HotelApp/1.0");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        connection.disconnect();

        return response.toString();
    }

    private static String makeHttpPostRequest(String urlString, String postData) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("User-Agent", "HotelApp/1.0");
        connection.setDoOutput(true);

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write("data=" + URLEncoder.encode(postData, "UTF-8"));
        writer.flush();
        writer.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        connection.disconnect();

        return response.toString();
    }

    // Interfaces para callbacks
    public interface OnLocationFoundListener {
        void onLocationFound(LocationResult result);
    }

    public interface OnAddressFoundListener {
        void onAddressFound(String address);
    }

    public interface OnHistoricalPlacesFoundListener {
        void onHistoricalPlacesFound(List<LugarHistorico> lugares);
    }

    // Clase para resultado de ubicación
    public static class LocationResult {
        public double latitude;
        public double longitude;
        public String address;
        public boolean success;

        public LocationResult(double lat, double lng, String addr, boolean success) {
            this.latitude = lat;
            this.longitude = lng;
            this.address = addr;
            this.success = success;
        }
    }
}*/
public class OpenStreetMapService {
    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/";
    private static final String OVERPASS_BASE_URL = "https://overpass-api.de/api/interpreter";

    // Geocodificación: Dirección -> Coordenadas
    public static void buscarCoordenadas(String direccion, OnLocationFoundListener listener) {
        new AsyncTask<String, Void, LocationResult>() {
            @Override
            protected LocationResult doInBackground(String... params) {
                try {
                    String query = URLEncoder.encode(params[0], "UTF-8");
                    String url = NOMINATIM_BASE_URL + "search?format=json&q=" + query + "&limit=1";

                    String response = makeHttpRequest(url);
                    JSONArray results = new JSONArray(response);

                    if (results.length() > 0) {
                        JSONObject location = results.getJSONObject(0);
                        double lat = location.getDouble("lat");
                        double lon = location.getDouble("lon");
                        String displayName = location.getString("display_name");

                        return new LocationResult(lat, lon, displayName, true);
                    }

                    return new LocationResult(0, 0, "No encontrado", false);

                } catch (Exception e) {
                    Log.e("OSMService", "Error en geocodificación", e);
                    return new LocationResult(0, 0, "Error: " + e.getMessage(), false);
                }
            }

            @Override
            protected void onPostExecute(LocationResult result) {
                listener.onLocationFound(result);
            }
        }.execute(direccion);
    }

    // Geocodificación inversa: Coordenadas -> Dirección
    public static void buscarDireccion(double lat, double lng, OnAddressFoundListener listener) {
        new AsyncTask<Double, Void, String>() {
            @Override
            protected String doInBackground(Double... params) {
                try {
                    String url = NOMINATIM_BASE_URL + "reverse?format=json&lat=" +
                            params[0] + "&lon=" + params[1];

                    String response = makeHttpRequest(url);
                    JSONObject result = new JSONObject(response);

                    return result.getString("display_name");

                } catch (Exception e) {
                    Log.e("OSMService", "Error en geocodificación inversa", e);
                    return "Coordenadas: " + params[0] + ", " + params[1];
                }
            }

            @Override
            protected void onPostExecute(String address) {
                listener.onAddressFound(address);
            }
        }.execute(lat, lng);
    }

    // Buscar lugares históricos cercanos usando Overpass API
    public static void buscarLugaresHistoricos(double lat, double lng, double radiusKm,
                                               OnHistoricalPlacesFoundListener listener) {
        new AsyncTask<Double, Void, List<LugarHistorico>>() {
            @Override
            protected List<LugarHistorico> doInBackground(Double... params) {
                List<LugarHistorico> lugares = new ArrayList<>();

                try {
                    // Primero intentar con query simplificada
                    String overpassQuery = construirQuerySimplificada(params[0], params[1], params[2]);

                    String response = makeHttpPostRequest(OVERPASS_BASE_URL, overpassQuery);
                    JSONObject result = new JSONObject(response);
                    JSONArray elements = result.getJSONArray("elements");

                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject element = elements.getJSONObject(i);

                        if (element.has("tags")) {
                            JSONObject tags = element.getJSONObject("tags");

                            String nombre = tags.optString("name", "");

                            // Filtrar lugares sin nombre o con nombres muy cortos
                            if (nombre.isEmpty() || nombre.length() < 3) {
                                continue;
                            }

                            String descripcion = obtenerDescripcion(tags);
                            String tipoLugar = obtenerTipoLugar(tags);

                            double lugarLat, lugarLng;

                            // Manejar diferentes tipos de elementos (node, way)
                            if (element.has("lat") && element.has("lon")) {
                                lugarLat = element.getDouble("lat");
                                lugarLng = element.getDouble("lon");
                            } else if (element.has("center")) {
                                JSONObject center = element.getJSONObject("center");
                                lugarLat = center.getDouble("lat");
                                lugarLng = center.getDouble("lon");
                            } else {
                                continue; // Saltar si no tiene coordenadas
                            }

                            // Calcular distancia real
                            float distancia = calcularDistancia(params[0], params[1], lugarLat, lugarLng);

                            // Filtrar lugares que estén realmente dentro del radio
                            if (distancia <= params[2]) {
                                LugarHistorico lugar = new LugarHistorico(
                                        nombre, descripcion,
                                        new LatLng(lugarLat, lugarLng),
                                        distancia, tipoLugar
                                );

                                lugares.add(lugar);
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.e("OSMService", "Error con Overpass API: " + e.getMessage());

                    // Fallback: usar datos predefinidos basados en la ubicación
                    lugares = obtenerLugaresPredefinidos(params[0], params[1], params[2]);
                }

                // Ordenar por distancia y limitar a 8 lugares
                Collections.sort(lugares, (a, b) -> Float.compare(a.getDistancia(), b.getDistancia()));

                // Limitar a máximo 8 lugares
                if (lugares.size() > 8) {
                    lugares = lugares.subList(0, 8);
                }

                return lugares;
            }

            @Override
            protected void onPostExecute(List<LugarHistorico> lugares) {
                listener.onHistoricalPlacesFound(lugares);
            }
        }.execute(lat, lng, radiusKm);
    }

    // Query más simple y confiable
    private static String construirQuerySimplificada(double lat, double lng, double radiusKm) {
        int radiusMetros = (int) (radiusKm * 1000);

        return "[out:json][timeout:10];" +
                "(" +
                "  node[\"historic\"][\"name\"](around:" + radiusMetros + "," + lat + "," + lng + ");" +
                "  node[\"tourism\"=\"museum\"][\"name\"](around:" + radiusMetros + "," + lat + "," + lng + ");" +
                "  node[\"tourism\"=\"attraction\"][\"name\"](around:" + radiusMetros + "," + lat + "," + lng + ");" +
                ");" +
                "out 15;";
    }

    // Fallback con lugares predefinidos para Lima, Perú
    private static List<LugarHistorico> obtenerLugaresPredefinidos(double lat, double lng, double radiusKm) {
        List<LugarHistorico> lugaresPredefinidos = new ArrayList<>();

        // Determinar ciudad basada en coordenadas (ejemplo para Lima)
        if (lat > -12.5 && lat < -11.5 && lng > -77.5 && lng < -76.5) {
            // Lima, Perú
            lugaresPredefinidos.add(new LugarHistorico(
                    "Plaza de Armas de Lima",
                    "Centro histórico de Lima, Patrimonio de la Humanidad",
                    new LatLng(-12.0464, -77.0297),
                    calcularDistancia(lat, lng, -12.0464, -77.0297),
                    "Plaza histórica"
            ));

            lugaresPredefinidos.add(new LugarHistorico(
                    "Catedral de Lima",
                    "Catedral metropolitana construida en 1535",
                    new LatLng(-12.0464, -77.0287),
                    calcularDistancia(lat, lng, -12.0464, -77.0287),
                    "Iglesia histórica"
            ));

            lugaresPredefinidos.add(new LugarHistorico(
                    "Palacio de Gobierno",
                    "Sede del poder ejecutivo del Perú",
                    new LatLng(-12.0463, -77.0280),
                    calcularDistancia(lat, lng, -12.0463, -77.0280),
                    "Palacio"
            ));

            lugaresPredefinidos.add(new LugarHistorico(
                    "Monasterio de San Francisco",
                    "Complejo religioso colonial con catacumbas",
                    new LatLng(-12.0456, -77.0290),
                    calcularDistancia(lat, lng, -12.0456, -77.0290),
                    "Monasterio"
            ));

            lugaresPredefinidos.add(new LugarHistorico(
                    "Casa de Aliaga",
                    "Casa colonial más antigua de América",
                    new LatLng(-12.0458, -77.0285),
                    calcularDistancia(lat, lng, -12.0458, -77.0285),
                    "Casa histórica"
            ));

            lugaresPredefinidos.add(new LugarHistorico(
                    "Museo Larco",
                    "Museo de arte precolombino",
                    new LatLng(-12.0692, -77.0707),
                    calcularDistancia(lat, lng, -12.0692, -77.0707),
                    "Museo"
            ));

            lugaresPredefinidos.add(new LugarHistorico(
                    "Huaca Pucllana",
                    "Sitio arqueológico preincaico",
                    new LatLng(-12.1086, -77.0447),
                    calcularDistancia(lat, lng, -12.1086, -77.0447),
                    "Sitio arqueológico"
            ));

            lugaresPredefinidos.add(new LugarHistorico(
                    "Barranco",
                    "Distrito bohemio con arquitectura republicana",
                    new LatLng(-12.1467, -77.0208),
                    calcularDistancia(lat, lng, -12.1467, -77.0208),
                    "Distrito histórico"
            ));
        }

        // Filtrar solo los que estén dentro del radio
        List<LugarHistorico> lugaresCercanos = new ArrayList<>();
        for (LugarHistorico lugar : lugaresPredefinidos) {
            if (lugar.getDistancia() <= radiusKm) {
                lugaresCercanos.add(lugar);
            }
        }

        return lugaresCercanos;
    }

    private static String obtenerDescripcion(JSONObject tags) throws JSONException {
        // Priorizar diferentes campos de descripción
        String[] camposDescripcion = {"description", "description:es", "inscription", "information"};

        for (String campo : camposDescripcion) {
            if (tags.has(campo)) {
                return tags.optString(campo);
            }
        }

        // Si no hay descripción, crear una basada en los tags
        StringBuilder desc = new StringBuilder();

        if (tags.has("historic")) {
            desc.append("Sitio histórico: ").append(tags.getString("historic"));
        } else if (tags.has("tourism")) {
            desc.append("Atracción turística: ").append(tags.getString("tourism"));
        } else if (tags.has("amenity")) {
            desc.append("Lugar de interés: ").append(tags.getString("amenity"));
        }

        return desc.length() > 0 ? desc.toString() : "Lugar de interés histórico";
    }

    private static String obtenerTipoLugar(JSONObject tags) throws JSONException {
        if (tags.has("historic")) {
            String historic = tags.getString("historic");
            switch (historic) {
                case "monument": return "Monumento";
                case "memorial": return "Memorial";
                case "building": return "Edificio histórico";
                case "castle": return "Castillo";
                case "church": return "Iglesia histórica";
                case "archaeological_site": return "Sitio arqueológico";
                default: return "Sitio histórico";
            }
        } else if (tags.has("tourism")) {
            String tourism = tags.getString("tourism");
            switch (tourism) {
                case "museum": return "Museo";
                case "attraction": return "Atracción turística";
                case "artwork": return "Arte público";
                case "memorial": return "Memorial";
                default: return "Lugar turístico";
            }
        } else if (tags.has("amenity") && tags.getString("amenity").equals("place_of_worship")) {
            return "Lugar de culto";
        }

        return "Lugar de interés";
    }

    private static float calcularDistancia(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0] / 1000; // Convertir a kilómetros
    }

    private static String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "HotelApp/1.0");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        connection.disconnect();

        return response.toString();
    }

    private static String makeHttpPostRequest(String urlString, String postData) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("User-Agent", "HotelApp/1.0");
            connection.setConnectTimeout(10000); // 10 segundos timeout
            connection.setReadTimeout(15000); // 15 segundos read timeout
            connection.setDoOutput(true);

            // Escribir datos POST
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write("data=" + URLEncoder.encode(postData, "UTF-8"));
            writer.flush();
            writer.close();

            // Verificar código de respuesta
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode + " - " + connection.getResponseMessage());
            }

            // Leer respuesta
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            return response.toString();

        } finally {
            connection.disconnect();
        }
    }

    // Interfaces para callbacks
    public interface OnLocationFoundListener {
        void onLocationFound(LocationResult result);
    }

    public interface OnAddressFoundListener {
        void onAddressFound(String address);
    }

    public interface OnHistoricalPlacesFoundListener {
        void onHistoricalPlacesFound(List<LugarHistorico> lugares);
    }

    // Clase para resultado de ubicación
    public static class LocationResult {
        public double latitude;
        public double longitude;
        public String address;
        public boolean success;

        public LocationResult(double lat, double lng, String addr, boolean success) {
            this.latitude = lat;
            this.longitude = lng;
            this.address = addr;
            this.success = success;
        }
    }
}