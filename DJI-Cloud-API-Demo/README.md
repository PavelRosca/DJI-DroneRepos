# DJI Cloud API Demo (Backend) - Ghid Complet

## 1. Context important

Acest proiect este backend-ul demo pentru integrare DJI Cloud API.

Proiectul este "discontinued" de catre DJI, ceea ce inseamna:

- fara mentenanta activa
- posibile incompatibilitati cu device-uri noi
- necesita patch-uri locale pentru a functiona in prezent

Acest README explica arhitectura, setup-ul local si toate remediile aplicate in sesiunea curenta pentru RC Pro 2 + Matrice 400.

## 2. Rolul backend-ului

Backend-ul gestioneaza:

- autentificare web/pilot
- workspace/device management
- persistenta in MySQL
- cache/session in Redis
- integrare MQTT pentru status/comenzi device
- API-uri consumate de frontend-ul Cloud-API-Demo-Web

Module importante:

- cloud-sdk (logica SDK/integration layer)
- sample (aplicatia Spring Boot care ruleaza local)

## 3. Dependinte runtime necesare

Pentru pornire completa locala:

- Java 11
- Maven 3.9.x
- MySQL (port 3306)
- Redis (port 6379)
- Mosquitto MQTT
: listener MQTT 1883
: listener WebSocket MQTT 8083

Port backend:

- 6789

## 4. Configurare backend

Fisier central:

- sample/src/main/resources/application.yml

In sesiunea curenta au fost aliniate:

- appId / appKey / appLicense
- host-uri LAN pentru comunicare RC si MQTT
- valori locale pentru startup stabil

## 5. Flux functional end-to-end (simplificat)

1. RC/Pilot deschide pagina H5 din frontend.
2. Frontend face login catre backend.
3. Backend returneaza config (inclusiv mqtt_addr).
4. Pilot2 creeaza client MQTT pe WS (8083).
5. Device trimite topicuri status (inclusiv online/status reply).
6. Backend proceseaza mesajele si actualizeaza starea device/workspace.

## 6. Ce s-a reparat in aceasta sesiune (foarte important)

### 6.1 Compatibilitate device-uri noi (Matrice 400 si necunoscute)

Problema initiala:

- backend pica la deserializare pe type necunoscut (ex. 103, ulterior 174)

Fixuri aplicate:

- DeviceTypeEnum:
: adaugat M400(103)
: adaugat UNKNOWN(-1)
: fallback in find() pentru tipuri necunoscute
- DeviceSubTypeEnum:
: fallback in find() la ZERO pentru subType necunoscut
- DeviceEnum:
: adaugat mapare M400
: adaugat helper findOrNull pentru mapari necunoscute
- SDKManager:
: fallback defensiv la clasificare gateway cand enum-ul nu are modelul nou
- DeviceServiceImpl:
: folosire lookup tolerant pentru model key in conversii topo

Rezultat:

- backend nu mai crapa pe device type nou/necunoscut

### 6.2 Fix downstream DB (nickname NOT NULL)

Problema:

- insert in manage_device esua cand dictionarul nu avea metadata pentru model nou

Fix:

- in SDKDeviceService s-a adaugat fallback nickname (de ex. device SN) cand metadata lipseste

Rezultat:

- inregistrarea device nu mai pica pe constrangerea nickname

### 6.3 Build/runtime consistency

Pentru ca sample sa foloseasca patch-urile cloud-sdk locale:

- cloud-sdk a fost instalat local in Maven repository
- backend restartat pe artefactul actualizat

## 7. Pornire backend

Din radacina repo-ului DJI-Cloud-API-Demo:

1. mvn -pl sample -am -DskipTests compile
2. cd sample
3. mvn spring-boot:run

Verificari rapide:

- http://localhost:6789 (service up)
- endpointurile protejate pot raspunde 401 fara token, ceea ce este normal

## 8. Legatura cu frontend-ul

Frontend-ul (Cloud-API-Demo-Web) trebuie sa fie pornit separat pe 8080.

In setup-ul folosit aici:

- RC intra pe frontend: http://192.168.1.174:8080/pilot-login
- frontend comunica cu backend-ul pe 6789
- mqtt_addr intors catre pilot este pe WS 8083

## 9. Troubleshooting orientat pe problema reala

### 9.1 "Device not found" desi RC se logheaza

Verifica in ordine:

- backend ruleaza fara exceptii de deserializare
- broker WS 8083 este accesibil
- workspace binding este complet
- device apare in endpointurile de device bound

### 9.2 Cloud disconnect in Pilot Home

- confirma mqtt_addr valid la login pilot
- confirma topic flow online/status reply
- confirma ca frontend-ul activ este instanta corecta (nu alta aplicatie pe 8080)

### 9.3 "Debug tool subscribe topic" din tutorial DJI

"Debug tool" inseamna un client MQTT extern (ex. MQTTX) folosit pentru diagnostic topicuri.

Nu este obligatoriu pentru functionarea de baza, dar este util pentru debugging.

Topicuri mentionate frecvent in tutorial:

- sys/product/+/status
- sys/product/+/status_reply

## 10. Nota de productie

Acest cod este demo educativ, nu platforma productie.

Pentru productie sunt necesare:

- securizare endpointuri/tokenuri
- TLS/HTTPS
- credential management
- observability, retry strategy, audit si hardening complet

## 11. Referinte

- DJI Developer Cloud API Tutorial:
: https://developer.dji.com/doc/cloud-api-tutorial/cn/
- Licenta proiect:
: MIT (conform fisierelor originale)

