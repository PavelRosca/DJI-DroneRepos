# Cloud API Demo Web (Frontend) - Ghid Complet

## 1. Context important

Acest proiect este demo-ul frontend pentru ecosistemul DJI Cloud API.

Proiectul original este marcat de DJI ca "discontinued" (fara mentenanta activa), deci:

- nu este productie-ready
- poate avea dependinte invechite
- necesita ajustari locale pentru a rula pe sisteme moderne

Acest README documenteaza ce inseamna proiectul, cum se porneste corect local si ce a fost reparat in sesiunea curenta pentru integrarea cu backend-ul si cu RC Pro 2 + Matrice 400.

## 2. Ce este acest frontend

Aplicatia web ofera interfata pentru:

- autentificare utilizatori web/pilot
- management workspace/device
- map, media, wayline, cloud modules
- fluxul "pilot-login" folosit de RC Pilot 2 in varianta H5

Stack tehnic principal:

- Vue + TypeScript
- Vite (dev server)
- MQTT over WebSocket (prin backend config)

## 3. Relatia cu backend-ul

Frontend-ul din acest folder comunica cu backend-ul din folderul:

- DJI-Cloud-API-Demo

Flux simplificat:

1. RC/Pilot intra pe pagina de login pilot din frontend.
2. Frontend face request-uri catre backend pentru auth/workspace/device.
3. Backend foloseste MySQL + Redis + MQTT broker (Mosquitto in setup-ul local).
4. Status device vine prin topicuri MQTT, iar backend-ul actualizeaza starea in platforma.

## 4. Configurari cheie (frontend)

Fisier principal de configurare runtime:

- src/api/http/config.ts

In sesiunea curenta au fost aliniate valori pentru test local LAN:

- APP ID / APP Key / App License din contul DJI Developer
- host-uri API si WebSocket pentru comunicare cu backend-ul local
- compatibilitate pentru fluxul RC Pilot 2 pe URL H5

## 5. URL corect pentru RC Pilot 2

Pentru setup-ul facut in aceasta sesiune, RC trebuie sa deschida:

- http://192.168.1.174:8080/pilot-login

Observatii:

- Nu se foloseste direct 6789 pentru ecranul de login H5 din RC in acest flux.
- 6789 ramane API backend.
- RC si PC trebuie sa fie in acelasi LAN.

## 6. Pornire frontend

Din acest folder:

1. npm install
2. npm run serve

Port asteptat:

- 8080

Verificare rapida:

- http://localhost:8080
- http://localhost:8080/pilot-login

## 7. Ce s-a reparat in aceasta sesiune (frontend)

### 7.1 Dependinte si instalare

- s-au remediat blocaje de instalare cauzate de registry/lock-uri vechi
- s-a curatat setup-ul de dependinte pentru pornire stabila locala

### 7.2 Config Vite si pornire stabila

- s-au facut ajustari de configurare pentru a porni serverul local consistent
- s-au eliminat situatii in care multiple procese node se calcau pe acelasi port

### 7.3 TypeScript

Eroare rezolvata:

- Cannot find type definition file for 'vite/client'

Fix aplicat:

- eliminat compilerOptions.types explicit din tsconfig.json
- pastrat referinta standard in src/vite-env.d.ts

## 8. Ce inseamna modulele din pilot-home

In UI-ul pilot, modulul "Cloud" este legat de componenta "Thing" (MQTT init/state).

Daca Cloud ramane "disconnect":

- de regula frontend-ul/backend-ul nu sunt aliniate
- sau sesiunea din RC este stale
- sau fluxul de status MQTT nu a finalizat inca bind/online

## 9. Troubleshooting rapid

### 9.1 "Webpage not available" pe RC

- verifica frontend pornit pe 8080
- verifica URL complet cu http
- verifica firewall pentru 8080
- verifica LAN comun RC/PC

### 9.2 Cloud ramane disconnect

- confirma backend online pe 6789
- confirma broker MQTT WS pe 8083
- relogin pe pilot-login
- uninstall/install modul Cloud o singura data, apoi asteapta

### 9.3 Agora WEB_SECURITY_RESTRICT in vconsole

- apare pe HTTP ne-securizat
- nu blocheaza neaparat binding cloud/device
- afecteaza mai ales fluxurile de livestream Agora

## 10. Componente necesare in paralel

Pentru functionare end-to-end trebuie sa fie sus simultan:

- frontend (acest proiect)
- backend (DJI-Cloud-API-Demo)
- MySQL
- Redis
- Mosquitto (1883 + 8083 WebSocket)

## 11. Nota de siguranta

Acest demo este util pentru invatare si prototipare, nu pentru productie directa.

Pentru productie:

- hardening securitate
- autentificare/secret management
- TLS/HTTPS
- audit complet al topicurilor si endpointurilor

## 12. Licenta si surse

- Licenta proiect: MIT (conform proiectului original)
- Documentatie DJI Cloud API: https://developer.dji.com/doc/cloud-api-tutorial/cn/
