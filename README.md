# DJI Cloud API Demo - Full Local Setup and Run Guide

This workspace contains the two projects that must run together for the DJI Cloud API demo:

- `Cloud-API-Demo-Web` - frontend UI built with Vite + Vue + TypeScript
- `DJI-Cloud-API-Demo` - backend API built with Spring Boot

This README is intended to be the single runbook for the next time you need to start the full stack from scratch.

## 0. Alignment with DJI official guide

Reference guide used as baseline:

- https://developer.dji.com/doc/cloud-api-tutorial/en/quick-start/source-code-deployment-steps.html

Official sequence in the DJI guide:

1. Register as DJI developer
2. Generate App License
3. Build and start front-end
4. Build and start back-end
5. Initialize database
6. Login in Pilot 2 / web

Practical local mapping in this repository:

1. Developer account + `appId`, `appKey`, `appLicense` are already configured in project files.
2. MQTT broker in DJI docs is often EMQX; this local setup uses Mosquitto (works for this stack).
3. Database engine in DJI docs is MySQL; this local setup uses MariaDB 12.2 (MySQL-compatible).
4. Startup order in this README prioritizes infrastructure first (DB/Redis/MQTT), then backend, then frontend, for more stable local boot.

## 1. What must be running

For the project to work end to end, these components must be available at the same time:

- Frontend on `8080`
- Backend on `6789`
- MQTT broker on `1883`
- MQTT over WebSocket on `8083`
- MariaDB / MySQL on `3306`
- Redis on `6379`

## 2. Folder layout

- `Cloud-API-Demo-Web` - frontend application
- `DJI-Cloud-API-Demo` - backend application
- `DJI-Cloud-API-Demo/sql/cloud_sample.sql` - database schema and seed data
- `mosquitto-ws.conf` - standalone Mosquitto WebSocket listener config for port `8083`

## 3. Verified local configuration used in this repo

The current setup in this repo is aligned to this LAN IP:

- `192.168.1.174`

The two files that must match your current machine IP are:

### Frontend config

File:

- `Cloud-API-Demo-Web/src/api/http/config.ts`

Expected values:

- `baseURL: 'http://<YOUR_PC_IP>:6789/'`
- `websocketURL: 'ws://<YOUR_PC_IP>:6789/api/v1/ws'`

### Backend config

File:

- `DJI-Cloud-API-Demo/sample/src/main/resources/application.yml`

Expected values:

- `mqtt.BASIC.host: <YOUR_PC_IP>`
- `mqtt.DRC.host: <YOUR_PC_IP>`
- `mqtt.BASIC.port: 8083`
- `mqtt.DRC.port: 8083`

If your PC IP changes, update both files before starting the stack.

## 4. First-time prerequisites

Install or make available these tools/services:

- Node.js + npm
- Java 11
- Redis
- Mosquitto
- MariaDB or MySQL-compatible server

Notes from the verified local setup:

- Node.js was already installed and worked correctly.
- Java 11 was already installed and worked correctly.
- Redis service was already installed and running on `6379`.
- Mosquitto service was already installed and running on `1883`.
- MariaDB 12.2 was installed locally and works as the MySQL-compatible database.
- Maven was not available on `PATH` during verification, so a fallback backend launch path is documented below.

## 5. One-time database setup

If the database is not already initialized, do this once.

### 5.1 Start MariaDB / MySQL on port 3306

On the verified machine, MariaDB was started manually with:

```powershell
& 'C:\Program Files\MariaDB 12.2\bin\mariadbd.exe' --defaults-file='C:\Program Files\MariaDB 12.2\data\my.ini' --console
```

If you have a registered Windows service instead, starting that service is also fine.

### 5.2 Import the schema

Run:

```powershell
Get-Content 'e:\DJI-DroneRepos\DJI-Cloud-API-Demo\sql\cloud_sample.sql' | & 'C:\Program Files\MariaDB 12.2\bin\mysql.exe' -uroot
```

To verify the database exists:

```powershell
& 'C:\Program Files\MariaDB 12.2\bin\mysql.exe' -uroot -e "SHOW DATABASES LIKE 'cloud_sample';"
```

Expected result:

- `cloud_sample`

### 5.3 MariaDB 12 collation compatibility fix (important)

If you run this project on MariaDB 12+, apply this fix once after importing the schema:

```powershell
Get-Content 'e:\DJI-DroneRepos\DJI-Cloud-API-Demo\sql\compat_mariadb12_collation_fix.sql' | & 'C:\Program Files\MariaDB 12.2\bin\mysql.exe' -uroot
```

Why this is needed:

- MariaDB 12 defaults to `utf8mb4_uca1400_ai_ci` session/database collation.
- The original sample schema uses older `utf8mb3_general_ci` in some firmware columns.
- Mixed collations can break firmware queries with errors like `Illegal mix of collations ... for operation '='`.

## 6. One-time frontend setup

From `Cloud-API-Demo-Web`:

```powershell
Set-Location 'e:\DJI-DroneRepos\Cloud-API-Demo-Web'
npm install
```

If `node_modules` is already present, you do not need to repeat this unless dependencies change.

## 7. One-time backend build setup

### Preferred path

If Maven is available on your machine, use the normal build:

```powershell
Set-Location 'e:\DJI-DroneRepos\DJI-Cloud-API-Demo'
mvn -pl sample -am -DskipTests compile
```

This is the cleanest path on a fresh machine.

### Fallback path if Maven is not available

The verified workspace already contains compiled backend classes and local Maven cache entries, so the backend can still be launched without `mvn` if these already exist:

- `DJI-Cloud-API-Demo/sample/target/classes`
- `DJI-Cloud-API-Demo/sample/sample/target/runtime-classpath.txt`
- local `.m2` cache under the current user profile

This fallback is useful for this machine now, but on a truly clean machine you should still install Maven and run the normal compile first.

## 8. Exact startup order

Always start the stack in this order:

1. MariaDB / MySQL
2. Redis
3. Mosquitto on `1883`
4. Mosquitto WebSocket listener on `8083`
5. Backend on `6789`
6. Frontend on `8080`

Pre-flight checks before startup:

1. Confirm LAN IP did not change.
2. Confirm frontend and backend config files still reference the same LAN IP.
3. Confirm required ports are free (`8080`, `8083`, `6789`) or intentionally occupied by your previous run.

## 9. Exact commands to run everything

Use separate terminals for each long-running process.

### 9.1 Start MariaDB

Verified command:

```powershell
& 'C:\Program Files\MariaDB 12.2\bin\mariadbd.exe' --defaults-file='C:\Program Files\MariaDB 12.2\data\my.ini' --console
```

### 9.2 Start Redis

If Redis is installed as a Windows service and already configured, make sure it is running.

Check:

```powershell
Get-NetTCPConnection -State Listen -LocalPort 6379
```

### 9.3 Start Mosquitto on port 1883

If Mosquitto is installed as a Windows service and already configured, make sure it is running.

Check:

```powershell
Get-NetTCPConnection -State Listen -LocalPort 1883
```

### 9.4 Start Mosquitto WebSocket listener on port 8083

Run:

```powershell
& 'C:\Program Files\Mosquitto\mosquitto.exe' -v -c 'e:\DJI-DroneRepos\mosquitto-ws.conf'
```

This uses the checked-in config file:

```conf
listener 8083
protocol websockets
allow_anonymous true
```

### 9.5 Start backend on port 6789

#### Preferred backend start if Maven is available

```powershell
Set-Location 'e:\DJI-DroneRepos\DJI-Cloud-API-Demo'
mvn -pl sample -am -DskipTests compile
Set-Location 'e:\DJI-DroneRepos\DJI-Cloud-API-Demo\sample'
mvn spring-boot:run
```

#### Verified fallback backend start without Maven

This command was verified to work in the current workspace:

```powershell
Set-Location 'e:\DJI-DroneRepos\DJI-Cloud-API-Demo\sample'
$cp = (Get-Content 'sample\target\runtime-classpath.txt' -Raw).Trim()
java -cp "target\classes;$cp" com.dji.sample.CloudApiSampleApplication
```

Important:

- Do not use `target\runtime-classpath.txt` for this fallback.
- Use `sample\target\runtime-classpath.txt`.
- The top-level `target\runtime-classpath.txt` was incomplete during verification and missed required web and MyBatis dependencies.

### 9.6 Start frontend on port 8080

Use the direct Vite command below:

```powershell
Set-Location 'e:\DJI-DroneRepos\Cloud-API-Demo-Web'
.\node_modules\.bin\vite.cmd --host 0.0.0.0 --port 8080 --strictPort
```

Important:

- This is the verified command.
- `npm run serve -- --host 0.0.0.0 --port 8080` did not behave correctly in this environment and Vite fell back to a different port.
- Use the direct `vite.cmd` command when you need port `8080` reliably.

## 10. How to verify the full stack is up

Run:

```powershell
Get-NetTCPConnection -State Listen -LocalPort 8080,8083,1883,3306,6379,6789 -ErrorAction SilentlyContinue |
	Select-Object LocalAddress, LocalPort, OwningProcess, State |
	Sort-Object LocalPort |
	Format-Table -AutoSize
```

Expected active ports:

- `1883`
- `3306`
- `6379`
- `6789`
- `8080`
- `8083`

Quick health checks (recommended):

```powershell
Test-NetConnection -ComputerName localhost -Port 8080
Test-NetConnection -ComputerName localhost -Port 6789
Test-NetConnection -ComputerName localhost -Port 8083
```

Expected for all:

- `TcpTestSucceeded : True`

You can also verify the backend port is reachable with:

```powershell
Test-NetConnection -ComputerName localhost -Port 6789
```

Expected:

- `TcpTestSucceeded : True`

## 11. URLs to use after startup

### Local browser

- Frontend: `http://localhost:8080`
- Backend: `http://localhost:6789`

### RC / Pilot 2

Use:

- `http://<YOUR_PC_IP>:8080/pilot-login`

Example for the current verified machine:

- `http://192.168.1.174:8080/pilot-login`

Do not use `6789` as the H5 login page URL.

## 12. Minimal restart procedure for next time

If the repo is already configured and dependencies are already present, the shortest reliable restart is:

1. Start MariaDB
2. Ensure Redis is running
3. Ensure Mosquitto `1883` is running
4. Start WebSocket Mosquitto on `8083`
5. Start backend with either Maven or the verified fallback command
6. Start frontend with direct `vite.cmd`
7. Open `http://localhost:8080`

## 13. Common problems and exact fixes

### Frontend comes up on the wrong port

Problem:

- Vite starts on `3000` or `3001` instead of `8080`

Fix:

- Use this command instead of `npm run serve`:

```powershell
.\node_modules\.bin\vite.cmd --host 0.0.0.0 --port 8080 --strictPort
```

### Backend fails because classes are missing from the classpath

Problem:

- Starting from `target\runtime-classpath.txt` can miss required dependencies such as Spring MVC and MyBatis Plus.

Fix:

- Use `sample\target\runtime-classpath.txt` instead.

### Backend says port 6789 is already in use

Problem:

- Another backend instance is already running.

Fix:

- Stop the old backend process first, or reuse the existing running instance.

Useful command to identify the owner process:

```powershell
Get-NetTCPConnection -State Listen -LocalPort 6789 | Select-Object LocalAddress, LocalPort, OwningProcess
Get-Process -Id <OwningProcess>
```

### RC cannot open the login page

Check:

- frontend is on `8080`
- RC and PC are on the same LAN
- you used `http://<YOUR_PC_IP>:8080/pilot-login`
- Windows firewall allows at least `8080`, `6789`, and `8083`

### MQTT cloud connection stays disconnected

Check:

- backend on `6789`
- WebSocket broker on `8083`
- frontend/backend IP settings match your current LAN IP

### Drone is connected but not visible in workspace

If backend logs show `Workspace ID does not exist`:

1. Check that the gateway/aircraft rows in `manage_device` have a valid `workspace_id`.
2. Check that the same `workspace_id` exists in `manage_workspace`.
3. Rebind the device from Pilot2 cloud page if needed, then refresh the web page.

Quick SQL check:

```sql
USE cloud_sample;
SELECT workspace_id, workspace_name FROM manage_workspace;
SELECT device_sn, workspace_id, bound_status FROM manage_device WHERE device_sn IN ('<GATEWAY_SN>', '<DRONE_SN>');
```

If MQTT WS broker on `8083` fails to start, verify no duplicate process already occupies `8083`.

## 16. Clean restart from zero terminals (recommended daily flow)

Use this sequence when all terminals were closed and you want the fastest reproducible startup:

1. Open terminal A and start MariaDB.
2. Open terminal B and start Mosquitto WS on `8083`.
3. Ensure Redis and Mosquitto `1883` services are running.
4. Open terminal C and start backend (`6789`).
5. Open terminal D and start frontend (`8080`).
6. Run port verification command from section 10.
7. Open `http://localhost:8080` and optionally `http://<LAN_IP>:8080/pilot-login`.

This flow was re-validated after closing all terminals.

## 14. How to stop everything

Stop the long-running terminals that were started for:

- MariaDB
- Mosquitto WebSocket on `8083`
- backend
- frontend

Redis and the base Mosquitto broker may be Windows services on your machine, so stop them only if you intentionally want them down.

## 15. Current verified working state

This workspace was verified with all required ports active at the same time:

- `1883`
- `3306`
- `6379`
- `6789`
- `8080`
- `8083`

That means the project is now runnable locally with the commands documented above.

## 17. Jurnal complet al incidentului camera livestream (detaliat)

Aceasta sectiune documenteaza integral incidentul tratat in aceasta conversatie, cu focus pe camera/livestream: ce simptome au aparut, ce s-a verificat, ce s-a schimbat in cod, de ce functioneaza acum si care este mecanismul exact de functionare.

### 17.1 Simptome initiale observate

Primul simptom raportat a fost eroarea backend de tip Jackson:

- `Cannot deserialize value of type java.lang.String from Object value (token JsonToken.START_OBJECT)`

Ulterior, dupa restartul backend-ului, au aparut simptome succesive in zona de livestream:

1. In UI, la Play: `Please check whether the live stream service is normal.`
2. In frontend: conexiuni catre endpointuri SRS vechi (`:1985`) cu `ERR_CONNECTION_REFUSED`.
3. Dupa migrarea pe WHIP/WHEP: eroare frontend `invalid WHEP url .../whep`.
4. Dupa acceptarea URL-ului: `POST .../whep 404 (Not Found)` intermitent.

Concluzie rapida: nu a fost un singur bug, ci un lant de probleme:

- incompatibilitate de deserializare,
- lipsa server livestream activ,
- incompatibilitate format URL intre player si MediaMTX,
- apoi race condition publish/play.

### 17.2 Cauza reala pentru eroarea de deserializare

In fluxul live, unele payload-uri DJI au inceput sa vina in forma obiect (JSON object), unde codul local se astepta la string simplu.

Zonele impactate:

1. `video_id` (acceptat cand string, dar uneori trimis/propagat ca structura).
2. output RTSP din raspuns (uneori string, alteori obiect cu camp `url`).

Fixul a fost introdus pe backend in codul Java:

- [DJI-Cloud-API-Demo/cloud-sdk/src/main/java/com/dji/sdk/cloudapi/device/VideoId.java](DJI-Cloud-API-Demo/cloud-sdk/src/main/java/com/dji/sdk/cloudapi/device/VideoId.java)
- [DJI-Cloud-API-Demo/sample/src/main/java/com/dji/sample/manage/service/impl/LiveStreamServiceImpl.java](DJI-Cloud-API-Demo/sample/src/main/java/com/dji/sample/manage/service/impl/LiveStreamServiceImpl.java)

Implementare:

1. `VideoId` a fost facut tolerant la input de tip string sau obiect.
2. `LiveStreamServiceImpl` a fost facut tolerant la output RTSP de tip string sau map.

### 17.3 Compatibilitate Java 11

In timpul patch-ului, unele modificari foloseau sintaxa Java mai noua (pattern matching `instanceof`), dar mediul local este Java 11.

S-a refacut codul la stil Java 11 (cast explicit), apoi s-a recompilat.

Impact:

1. Build functional pe mediul local.
2. Fara regresii de sintaxa in runtime.

### 17.4 De ce apare codul 13010 (`Please check whether the live stream service is normal`)

Acest mesaj nu insemna ca endpointul HTTP backend este mort. El venea din codurile de eroare livestream ale platformei, cand backend-ul cere start stream, dar infrastructura de stream nu este disponibila/corecta.

La verificare s-a observat:

1. Nu era server de streaming activ pe porturile clasice.
2. In acel moment, backend-ul putea raspunde API, dar nu avea unde sa publice stream-ul.

### 17.5 Schimbarea arhitecturii locale: MediaMTX in loc de SRS local

Pentru a obtine setup local rapid si stabil, s-a instalat MediaMTX si s-a folosit fluxul WebRTC standard:

1. Publish prin WHIP.
2. Play prin WHEP.

Motiv:

1. Frontend-ul avea deja optiune WEBRTC (`url_type = 4`).
2. Backend-ul avea mapare pentru WHIP (`UrlTypeEnum.WHIP = 4`).
3. MediaMTX suporta nativ WHIP/WHEP pe HTTP.

### 17.6 Modificari backend pentru URL-uri MediaMTX

Au fost facute modificari in:

- [DJI-Cloud-API-Demo/sample/src/main/java/com/dji/sample/manage/service/impl/LiveStreamServiceImpl.java](DJI-Cloud-API-Demo/sample/src/main/java/com/dji/sample/manage/service/impl/LiveStreamServiceImpl.java)
- [DJI-Cloud-API-Demo/sample/src/main/resources/application.yml](DJI-Cloud-API-Demo/sample/src/main/resources/application.yml)

Schimbari functionale:

1. URL de publish WHIP se construieste in format MediaMTX:
	- `http://<ip>:8889/<droneSn>-<payloadIndex>/whip`
2. URL de playback returnat frontend-ului se construieste din acesta:
	- `http://<ip>:8889/<droneSn>-<payloadIndex>/whep`
3. `livestream.url.whip.url` a fost setat la baza MediaMTX locala:
	- `http://192.168.1.174:8889`

Acest model elimina dependenta de endpointurile vechi SRS pe `:1985`.

### 17.7 De ce se vedea in continuare `:1985` in unele teste

In unele rulari, fluxul de UI era inca pe RTMP (sau pe cod frontend care genera URL de forma SRS), de aceea se vedeau request-uri catre `1985`.

S-a setat implicit WEBRTC in componenta de live:

- [Cloud-API-Demo-Web/src/components/livestream-others.vue](Cloud-API-Demo-Web/src/components/livestream-others.vue)

Important:

1. `rtmpURL` din config ramane util doar cand selectezi explicit RTMP.
2. Pentru fluxul curent recomandat (WEBRTC), `rtmpURL` nu influenteaza play-ul.

### 17.8 Eroarea `invalid WHEP url .../whep` si cauza exacta

Aceasta eroare a fost produsa de validarea stricta din SDK-ul frontend (fisier vendor SRS), care accepta doar pattern-uri cu slash final (`/whep/`) sau varianta `whip-play`.

MediaMTX returna URL valid standard in forma fara slash final (`.../whep`), iar validarea locala il respingea incorect.

Fix aplicat:

- [Cloud-API-Demo-Web/src/vendors/srs.sdk.js](Cloud-API-Demo-Web/src/vendors/srs.sdk.js)

Schimbare:

1. Validarea accepta si URL-uri terminate in `/whep` fara slash final.

### 17.9 Eroarea `POST .../whep 404` dupa fixul de validare

Dupa ce URL-ul a devenit acceptat, a ramas un 404 intermitent.

Analiza logurilor MediaMTX a aratat clar cauza:

1. Sesiunea de reader (WHEP/play) pornea uneori putin mai repede.
2. Publisher-ul (WHIP) devenea online imediat dupa.
3. In acel interval scurt, WHEP primea 404 (`no stream is available on path`).

Aceasta este o conditie de cursa (race condition), nu un config gresit de host/port.

Fix aplicat in UI:

- [Cloud-API-Demo-Web/src/components/livestream-others.vue](Cloud-API-Demo-Web/src/components/livestream-others.vue)

Comportament nou:

1. Retry automat la play WebRTC pentru erori tranzitorii (404/400).
2. 8 incercari, delay 1.2s intre incercari.
3. Mesaj de success cand stream-ul se conecteaza.
4. Mesaj de eroare explicit doar daca se depaseste bucla de retry.

### 17.10 De ce acum functioneaza fluxul camera

Fluxul final functional este urmatorul:

1. Utilizatorul apasa Play in modul WEBRTC.
2. Frontend cheama `live/streams/start` cu `url_type = 4`.
3. Backend construieste URL WHIP pentru path-ul camerei/payload-ului.
4. DJI RC/Pilot2 publica streamul catre MediaMTX pe WHIP.
5. Backend returneaza frontend-ului URL WHEP pe acelasi path.
6. Frontend initiaza play WHEP.
7. Daca stream-ul nu e online in acel moment, retry-ul acopera fereastra de startup.
8. Cand path-ul devine online, player-ul preia feed-ul video.

Pe scurt: functioneaza deoarece URL-urile, protocoalele si timpii de startup sunt acum aliniati intre backend, frontend, MediaMTX si RC.

### 17.11 Observatii operationale importante

1. Mesajul `The camera has started live streaming` (613003) poate aparea daca stream-ul era deja pornit; nu este neaparat eroare fatala.
2. Daca apare comportament inconsistent, apasa mai intai Stop, apoi Start.
3. Pentru depanare rapida verifica logul MediaMTX: el arata imediat daca sesiunea publica, daca path-ul devine online si daca reader-ul intra prea devreme.
4. Eroarea AMap key este separata (map), nu blocheaza livestream.
5. Endpointurile STS/OSS pot da erori daca `oss.enable` este false; aceasta problema este independenta de camera live.

### 17.12 Rezumat practic al root cause + fix

Root cause compus:

1. Deserializare stricta backend la structuri noi.
2. Lipsa server local de streaming in etapa initiala.
3. Incompatibilitate de validare URL in frontend vendor SDK.
4. Race condition la startup publish/play.

Fix compus:

1. Toleranta backend la payload/object shape.
2. MediaMTX instalat si folosit pe WHIP/WHEP.
3. Generare URL-uri backend in format MediaMTX.
4. Relaxare validare WHEP in SDK frontend.
5. Retry automat la play WebRTC.

Rezultat:

1. Camera porneste live din RC/Pilot2.
2. Frontend poate prelua feed-ul pe WHEP fara sa cada la prima fereastra de 404.
3. Fluxul este stabil pentru uz local in LAN.
