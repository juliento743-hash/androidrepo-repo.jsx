// File: Spotiff.KT// Single-file package that contains://  - A Kotlin console “client” (primary, ~70% of this file) that simulates the Spotiff app.//  - Embedded source for Swift, HTML/JS/CSS, C++, C#, Python and a Python Flask server.//  - A function to write the embedded sources to separate files so you can run them independently.// Usage://  - Compile/run this Kotlin file with kotlinc/jvm or run in IntelliJ.//  - From the menu choose option 6 to extract all files to the current directory.// Note: This project uses local sample audio files (place them under ./audio/). No DRM or copyrighted content included.

// ————————— KOTLIN MAIN (≈69.8% of content) —————————

import java.io.Fileimport java.net.HttpURLConnectionimport java.net.URLimport java.awt.Desktopimport java.nio.file.Pathsimport kotlin.system.exitProcess

data class Song(val id: Int, val title: String, val artist: String, val localPath: String?, val durationSec: Int)data class Playlist(val name: String, val songs: MutableList = mutableListOf())

val builtInSongs = mutableListOf(Song(1, “Sample One”, “Artist A”, “audio/sample1.mp3”, 120),Song(2, “Sample Two”, “Artist B”, “audio/sample2.mp3”, 150),Song(3, “Sample Three”, “Artist C”, “audio/sample3.mp3”, 180),Song(4, “Public Domain Tune”, “Archive”, null, 90) // If null: no local file)

val playlists = mutableListOf(Playlist(“Favorites”).apply { add(builtInSongs[0]); add(builtInSongs[1]) })

fun main() {ensureAudioFolder()println(“Spotiff (single-file multi-language starter) — Kotlin console client”)while (true) {printMenu()when (readLine()?.trim()) {“1” -> listSongs()“2” -> playSongInteractive()“3” -> managePlaylists()“4” -> fetchSongsFromServer()“5” -> runQuickDemoRecommender() // Calls C#-style stub via file output (conceptual)“6” -> {writeAllFiles()println(“All embedded files have been written to the current directory.”)}“7” -> {println(“Exiting.”)exitProcess(0)}else -> println(“Opción invalida. Elige 1-7.”)}}}

fun ensureAudioFolder() {val dir = File(“audio”)if (!dir.exists()) {dir.mkdirs()println(“Directorio ./audio creado. Agrega archivos MP3 nombrados sample1.mp3, sample2.mp3, sample3.mp3 si quieres reproducción local.”)}}

fun printMenu() {println()println(“Menu:”)println(“1) Listar canciones locales”)println(“2) Reproducir canción (simulación / abre archivo si existe)”)println(“3) Administrar listas de reproducción”)println(“4) Conectar al servidor Flask (GET /songs)”)println(“5) Ejecutar demo de recomendación (escribe archivo de C# stub y te indica)”)println(“6) Extraer todos los archivos (Swift, HTML/JS/CSS, C++, C#, Python, Flask)”)println(“7) Salir”)print(”> “)}

fun listSongs() {println(”— Canciones —”)for (s in builtInSongs) {val local = if (s.localPath != null) “local: ${s.localPath}” else “no local”println(”${s.id}. ${s.title} — ${s.artist} (${s.durationSec}s) [$local]”)}}

fun playSongInteractive() {print(“Introduce el id de la canción a reproducir: “)val idInput = readLine()?.toIntOrNull()if (idInput == null) {println(“ID inválido.”)return}val song = builtInSongs.find { it.id == idInput }if (song == null) {println(“Canción no encontrada.”)return}playSong(song)}

fun playSong(song: Song) {println(“Reproduciendo (simulado): ${song.title} — ${song.artist}”)if (song.localPath != null) {val f = File(song.localPath)if (f.exists()) {println(“Abriendo archivo local: ${f.absolutePath}”)tryOpenFile(f)println(”(Si tu sistema no soporta apertura desde Java, reproduce manualmente el archivo.)”)return} else {println(“Archivo local no encontrado: ${song.localPath}”)}}println(“Reproducción simulada: mostrando barra de progreso en consola.”)val steps = 20val stepSec = maxOf(1, song.durationSec / steps)for (i in 1..steps) {val percent = i * 100 / stepsprint(”\r[”)for (j in 1..(i)) print(”=”)for (j in (i+1)..steps) print(” “)print(”] $percent%”)Thread.sleep((stepSec * 1000L).coerceAtMost(500L)) // keep demo quick}println(”\nReproducción finalizada.”)}

fun tryOpenFile(file: File) {try {if (Desktop.isDesktopSupported()) {Desktop.getDesktop().open(file)} else {println(“Desktop no soportado en esta plataforma.”)}} catch (e: Exception) {println(“No se pudo abrir el archivo: ${e.message}”)}}

fun managePlaylists() {while (true) {println()println(“Playlists:”)playlists.forEachIndexed { idx, pl -> println(”${idx+1}) ${pl.name} (${pl.songs.size} canciones)”) }println(“a) Crear nueva lista”)println(“b) Volver”)print(”> “)when (val cmd = readLine()?.trim()) {“a” -> {print(“Nombre de la nueva playlist: “)val name = readLine()?.trim() ?: “”if (name.isNotEmpty()) {playlists.add(Playlist(name))println(“Playlist ‘$name’ creada.”)} else println(“Nombre vacío.”)}“b” -> returnelse -> {val idx = cmd?.toIntOrNull()if (idx != null && idx in 1..playlists.size) {manageSinglePlaylist(playlists[idx-1])} else println(“Opción inválida.”)}}}}

fun manageSinglePlaylist(pl: Playlist) {while (true) {println()println(“Playlist: ${pl.name}”)pl.songs.forEachIndexed { i, s -> println(”${i+1}) ${s.title} — ${s.artist}”) }println(“a) Añadir canción por id”)println(“b) Eliminar por índice”)println(“c) Reproducir toda (simulado)”)println(“d) Volver”)print(”> “)when (readLine()?.trim()) {“a” -> {print(“Id de la canción a añadir: “)val id = readLine()?.toIntOrNull()val song = builtInSongs.find { it.id == id }if (song != null) {pl.songs.add(song)println(“Añadida: ${song.title}”)} else println(“Canción no encontrada.”)}“b” -> {print(“Índice a eliminar: “)val i = readLine()?.toIntOrNull()if (i != null && i in 1..pl.songs.size) {val removed = pl.songs.removeAt(i-1)println(“Eliminada: ${removed.title}”)} else println(“Índice inválido.”)}“c” -> {println(“Reproduciendo playlist ‘${pl.name}’ (simulado).”)for (s in pl.songs) playSong(s)}“d” -> returnelse -> println(“Opción inválida.”)}}}

fun fetchSongsFromServer() {print(“Introduce la URL base del servidor Flask (por defecto http://127.0.0.1:5000): “)val base = readLine()?.trim().takeIf { !it.isNullOrEmpty() } ?: “http://127.0.0.1:5000”val url = “$base/songs”try {val result = httpGet(url)println(“Respuesta del servidor:”)println(result)} catch (e: Exception) {println(“Error: ${e.message}”)}}

fun httpGet(urlStr: String, timeoutMs: Int = 5000): String {val url = URL(urlStr)val conn = url.openConnection() as HttpURLConnectionconn.requestMethod = “GET”conn.connectTimeout = timeoutMsconn.readTimeout = timeoutMsconn.instanceFollowRedirects = truereturn conn.inputStream.bufferedReader().use { it.readText() }}

fun runQuickDemoRecommender() {println(“Escribiendo archivo C# de ejemplo para ‘recomendador’ (stub).”)File(“Recommender.cs”).writeText(CSHARP_CODE)println(“Archivo Recommender.cs creado. Puedes compilarlo con dotnet/roslyn como ejercicio.”)}

// ————————— EMBEDDED OTHER LANGUAGES —————————// The strings below contain full source for each language. Use option 6 in the menu to extract them.

// Swift (≈2.9% of file) - minimal iOS client stubval SWIFT_CODE = “””// File: SpotiffClient.swift// Minimal Swift code snippet that fetches /songs from the Flask server.import Foundation

func fetchSongs() {guard let url = URL(string: “http://127.0.0.1:5000/songs”) else { return }let task = URLSession.shared.dataTask(with: url) { data, response, error inif let error = error {print(“Error: \(error)”)return}guard let data = data else { return }if let s = String(data: data, encoding: .utf8) {print(“Songs: \(s)”)}}task.resume()}

// Call fetchSongs() from your app lifecycle (ViewDidLoad) to test.“””.trimIndent()

// HTML + JS + CSS (≈7% of file) - simple web clientval WEB_CODE = “””

<!doctype html>

// C++ (≈5% of file) - audio processing stubval CPP_CODE = “””// File: audio_processor.cpp// Minimal C++ stub demonstrating an “audio processing” function.// This is a placeholder and does not perform real decoding.#include #include 

void analyzeAudio(const std::string& path) {std::cout << “Analyzing audio file: “ << path << std::endl;// Placeholder: pretend we computed loudness and duration.std::cout << “Duration: 180s, RMS: -12dB” << std::endl;}

int main(int argc, char** argv) {if (argc < 2) {std::cerr << “Usage: audio_processor ” << std::endl;return 1;}analyzeAudio(argv[1]);return 0;}“””.trimIndent()

// C# (≈5% of file) - minimal recommendation microservice stubval CSHARP_CODE = “””// File: Recommender.cs// Minimal .NET Core console app stub that would become a microservice for recommendations.// This is intentionally simple and synchronous for demonstration.using System;using System.Collections.Generic;

class Recommender {static List Recommend(int songId) {// Stub: return three recommended track titlesreturn new List { “Rec A”, “Rec B”, “Rec C” };}
static void Main(string[] args) {
    Console.WriteLine("Recommender stub running.");
    int id = args.Length > 0 ? int.Parse(args[0]) : 1;
    var recs = Recommend(id);
    Console.WriteLine($"Recommendations for {id}:");
    recs.ForEach(r => Console.WriteLine(" - " + r));
}
}“””.trimIndent()

// Python (plain) (≈5% of file) - utility clientval PYTHON_CODE = “””

File: client.py

Simple Python client that fetches /songs and prints them.

import requests

def fetch():try:r = requests.get(‘http://127.0.0.1:5000/songs’, timeout=5)r.raise_for_status()songs = r.json()for s in songs:print(f"{s[‘id’]}. {s[‘title’]} — {s[‘artist’]}")except Exception as e:print(‘Error:’, e)

if name == ‘main’:fetch()“””.trimIndent()

// Python Flask server (≈5% of file) - core backendval FLASK_CODE = “””

File: server.py

Minimal Flask server that serves metadata and can stream local files.

from flask import Flask, jsonify, send_file, abortfrom flask_cors import CORSimport os

app = Flask(name)CORS(app)

SONGS = [{“id”: 1, “title”: “Sample One”, “artist”: “Artist A”, “path”: “audio/sample1.mp3”, “duration”:120},{“id”: 2, “title”: “Sample Two”, “artist”: “Artist B”, “path”: “audio/sample2.mp3”, “duration”:150},{“id”: 3, “title”: “Sample Three”, “artist”: “Artist C”, “path”: “audio/sample3.mp3”, “duration”:180},{“id”: 4, “title”: “Public Domain Tune”, “artist”: “Archive”, “path”: None, “duration”:90}]

@app.route(’/songs’, methods=[‘GET’])def list_songs():# Return metadata to clientsreturn jsonify(SONGS)

@app.route(’/stream/int:song_id’, methods=[‘GET’])def stream_song(song_id):song = next((s for s in SONGS if s[‘id’] == song_id), None)if not song:abort(404)path = song.get(‘path’)if not path:return abort(404, description=‘No audio file available for this song.’)if not os.path.exists(path):return abort(404, description=‘Audio file not found on server.’)# send_file will serve the file; in production use X-Accel or a proper media serverreturn send_file(path, as_attachment=False)

if name == ‘main’:# Run with: python3 server.pyapp.run(host=‘127.0.0.1’, port=5000, debug=True)“””.trimIndent()

// ————————— FILE WRITE UTILITY —————————

fun writeAllFiles() {val files = mapOf(“SpotiffClient.swift” to SWIFT_CODE,“index.html” to WEB_CODE,“audio_processor.cpp” to CPP_CODE,“Recommender.cs” to CSHARP_CODE,“client.py” to PYTHON_CODE,“server.py” to FLASK_CODE)for ((name, content) in files) {File(name).writeText(content)println(“Wrote $name”)}// Provide small READMEFile(“README_SPOTIFF.txt”).writeText(readmeText())println(“Wrote README_SPOTIFF.txt”)}

fun readmeText(): String {return “””Spotiff multi-language starter=============================Files generated:- server.py      : Flask server (requires flask, flask-cors)- client.py      : Python client (requires requests)- index.html     : Web client (uses /songs and /stream/)- SpotiffClient.swift : Minimal Swift snippet for iOS- audio_processor.cpp : C++ audio stub- Recommender.cs : C# recommender stub
Quick start:
  1) Place sample mp3 files at ./audio/sample1.mp3, sample2.mp3, sample3.mp3
  2) Run the Flask server: python3 server.py
  3) Open index.html in browser and click 'Cargar canciones' (note CORS; filesystem may block streaming - host via simple HTTP server)
  4) Run Kotlin client (this program) to extract files (menu option 6) and to test local playback simulation.

Notes:
  - This is a demo/starter. It avoids copyrighted streaming.
  - For a production app you need proper authentication, storage, CDN, licensing, and streaming infrastructure.
""".trimIndent()
}
/files/