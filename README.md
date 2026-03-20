# DJI-DroneRepos - Ghid Root (Frontend + Backend)

Acest repository contine setup-ul local pentru demo DJI Cloud API, format din doua proiecte principale care trebuie rulate impreuna.

## Structura principala

- [Cloud-API-Demo-Web](Cloud-API-Demo-Web): frontend-ul web (Vite + Vue + TypeScript)
- [DJI-Cloud-API-Demo](DJI-Cloud-API-Demo): backend-ul (Spring Boot + cloud-sdk)

## Ce rol are fiecare componenta

### Frontend ([Cloud-API-Demo-Web](Cloud-API-Demo-Web))

- ofera UI pentru login, workspace, device, map, media, wayline
- expune pagina H5 pentru Pilot 2:
: `/pilot-login`
- face request-uri HTTP catre backend

### Backend ([DJI-Cloud-API-Demo](DJI-Cloud-API-Demo))

- gestioneaza autentificarea si API-urile de management
- proceseaza mesajele device prin MQTT
- persista datele in MySQL
- foloseste Redis pentru stare/cache

### Servicii infrastructura

- MySQL pe `3306`
- Redis pe `6379`
- Mosquitto MQTT pe `1883`
- Mosquitto MQTT WebSocket pe `8083`

## Cum comunica proiectele intre ele

Fluxul operational simplificat:

1. RC Pilot 2 deschide URL-ul de frontend: `http://<PC_LAN_IP>:8080/pilot-login`.
2. Frontend trimite login/API request catre backend pe `http://<PC_LAN_IP>:6789`.
3. Backend returneaza config, inclusiv `mqtt_addr` pentru Pilot2 (WS MQTT).
4. Pilot2 se conecteaza la broker pe `ws://<PC_LAN_IP>:8083/mqtt`.
5. Device-ul (ex. Matrice 400) publica topicuri status.
6. Backend proceseaza statusurile si actualizeaza workspace/device in UI.

## Porturi importante (toate trebuie sa fie active)

- `8080` - frontend
- `6789` - backend API
- `8083` - MQTT over WebSocket pentru Pilot2
- `1883` - MQTT clasic
- `3306` - MySQL
- `6379` - Redis

## Ordinea corecta de pornire

1. Porneste infrastructura: MySQL, Redis, Mosquitto (1883 + 8083).
2. Porneste backend-ul din [DJI-Cloud-API-Demo](DJI-Cloud-API-Demo).
3. Porneste frontend-ul din [Cloud-API-Demo-Web](Cloud-API-Demo-Web).
4. Verifica endpoint-uri locale:
: `http://localhost:8080`
: `http://localhost:6789`

## Comenzi uzuale

### Frontend

In [Cloud-API-Demo-Web](Cloud-API-Demo-Web):

1. `npm install`
2. `npm run serve`

### Backend

In [DJI-Cloud-API-Demo](DJI-Cloud-API-Demo):

1. `mvn -pl sample -am -DskipTests compile`
2. `cd sample`
3. `mvn spring-boot:run`

## URL-uri de folosit pe RC Pro 2

- Pilot login: `http://<PC_LAN_IP>:8080/pilot-login`
- Nu folosi direct 6789 ca pagina de login H5 in acest flux.

Exemplu local folosit in sesiune:

- `http://192.168.1.174:8080/pilot-login`

## Ce a fost reparat in aceasta sesiune

Rezumatul fixurilor majore deja aplicate:

1. Compatibilitate backend pentru device types noi (inclusiv M400) si fallback pentru tipuri necunoscute.
2. Evitare crash-uri de deserializare din cloud-sdk la valori noi de tip/subtip.
3. Fix insert DB pentru device-uri noi fara metadata completa (fallback nickname).
4. Aliniere config frontend/backend pentru comunicare pe LAN.
5. Corectie TypeScript frontend pentru `vite/client`.

Detalii complete:

- [Cloud-API-Demo-Web/README.md](Cloud-API-Demo-Web/README.md)
- [DJI-Cloud-API-Demo/README.md](DJI-Cloud-API-Demo/README.md)

## Troubleshooting rapid

### 1) RC vede "webpage not available"

- frontend nu ruleaza pe 8080 sau nu e accesibil din LAN
- URL gresit (lipsa `http://`)
- firewall blocheaza 8080

### 2) Cloud module ramane "disconnect"

- backend sau broker WS (8083) nu sunt active
- RC si PC nu sunt in acelasi LAN
- sesiune veche/corupta in Pilot2

### 3) Device "not found"

- binding incomplet in workspace
- topicurile status/status_reply nu au flux complet
- backend nu proceseaza corect topicurile daca infrastructura nu e completa

### 4) Firewall minim necesar pe Windows

Inbound allow pe:

- `6789` (backend)
- `8083` (MQTT WS)
- recomandat si `8080` (frontend)

## Important

Aceste proiecte sunt demo/discontinued. Sunt bune pentru test, integrare si invatare, nu pentru productie directa fara hardening major.
