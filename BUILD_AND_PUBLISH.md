# APN Selector - Guida alla Compilazione e Pubblicazione

## Prerequisiti

- **Android Studio** Hedgehog (2023.1.1) o successivo
- **JDK 17** (incluso in Android Studio)
- **Android SDK** con API 35 installata
- **Gradle** 8.11.1 (scaricato automaticamente dal wrapper)

---

## Compilazione

### Da Android Studio

1. Apri Android Studio
2. **File > Open** e seleziona la cartella `apnselector/`
3. Attendi il sync di Gradle (automatico)
4. **Build > Make Project** (`Ctrl+F9`)

### Da riga di comando

```bash
cd apnselector/

# Debug build
./gradlew assembleDebug

# Release build (richiede signing config, vedi sotto)
./gradlew assembleRelease

# L'APK generato si trova in:
# app/build/outputs/apk/debug/app-debug.apk
# app/build/outputs/apk/release/app-release.apk
```

### Installazione su dispositivo

```bash
# Debug (direttamente, con USB debugging attivo)
./gradlew installDebug

# Oppure con adb
adb install app/build/outputs/apk/debug/app-debug.apk
```

> **Nota:** L'app non ha una launcher activity. Dopo l'installazione, tieni premuto sulla home screen > Widget > cerca "APN Selector" per aggiungere i widget.

---

## Configurazione Signing per Release

Per pubblicare su Google Play serve un keystore firmato.

### 1. Creare il keystore

```bash
keytool -genkey -v \
  -keystore apnselector-release.jks \
  -keyalg RSA -keysize 2048 \
  -validity 10000 \
  -alias apnselector
```

Inserisci una password sicura e i dati richiesti. **Conserva il keystore e le password in un luogo sicuro** - se li perdi non potrai piu' aggiornare l'app.

### 2. Configurare il signing in Gradle

Crea il file `apnselector/keystore.properties` (NON committarlo nel repo):

```properties
storeFile=../apnselector-release.jks
storePassword=LA_TUA_PASSWORD
keyAlias=apnselector
keyPassword=LA_TUA_PASSWORD
```

Aggiungi al file `app/build.gradle.kts`:

```kotlin
import java.util.Properties
import java.io.FileInputStream

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) load(FileInputStream(file))
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... rest of release config
        }
    }
}
```

### 3. Generare AAB (Android App Bundle) per il Play Store

```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

Google Play richiede il formato **AAB** (non APK) per le nuove app.

---

## Pubblicazione su Google Play Store

### 1. Registrazione sviluppatore

1. Vai su [Google Play Console](https://play.google.com/console)
2. Registrati come sviluppatore (costo una tantum: **$25 USD**)
3. Completa la verifica dell'identita'

### 2. Creare l'app

1. Nella Play Console, clicca **"Crea app"**
2. Compila:
   - **Nome app**: APN Selector
   - **Lingua predefinita**: Italiano (o Inglese)
   - **App o gioco**: App
   - **Gratuita o a pagamento**: Gratuita
3. Accetta le policy

### 3. Compilare la scheda dello Store

#### Sezione "App principale"

- **Titolo**: APN Selector
- **Descrizione breve** (max 80 caratteri):
  > Widget per monitorare gli APN attivi del dispositivo
- **Descrizione completa** (max 4000 caratteri):
  > APN Selector e' un'app widget-only che ti permette di vedere tutti gli APN configurati sul tuo dispositivo direttamente dalla home screen, senza dover navigare nelle impostazioni di sistema.
  >
  > Caratteristiche:
  > - Widget Lista (4x2): mostra tutti gli APN con quello attivo evidenziato
  > - Widget Singolo (1x1): monitora un singolo APN con icona colorata
  > - Aggiornamento automatico al cambio di carrier
  > - Tap per aprire direttamente le impostazioni APN di sistema
  > - Nessun dato raccolto, nessuna connessione a internet

#### Sezione "Elementi grafici"

Prepara:
- **Icona app**: 512x512 PNG
- **Feature graphic**: 1024x500 PNG
- **Screenshot**: almeno 2 screenshot (telefono), consigliati anche per tablet
  - Screenshot del widget lista sulla home screen
  - Screenshot del widget singolo
  - Screenshot della configurazione widget

### 4. Content rating

1. Vai in **Policy > Content rating**
2. Compila il questionario IARC
3. L'app ricevera' rating "Tutti" (nessun contenuto sensibile)

### 5. Pricing & distribution

- Seleziona i paesi di distribuzione
- Dichiara che l'app non contiene ads
- Completa la Data Safety section:
  - **L'app raccoglie dati?** No
  - **L'app condivide dati?** No
  - L'app richiede `READ_PHONE_STATE` solo per leggere gli APN localmente

### 6. Caricamento e rilascio

1. Vai in **Release > Production** (o usa prima "Internal testing" per test)
2. Clicca **"Crea nuova release"**
3. Carica il file `app-release.aab`
4. Aggiungi le **note di rilascio**:
   > Versione iniziale - Widget per monitorare APN attivi
5. Clicca **"Rivedi rilascio"** poi **"Avvia rollout in produzione"**

### 7. Revisione Google

- La prima revisione richiede tipicamente **1-7 giorni**
- Controlla lo stato nella Play Console
- Se viene rifiutata, leggi il motivo e correggi

---

## Testing consigliato prima della pubblicazione

### Internal Testing (consigliato)

1. Nella Play Console vai in **Release > Testing > Internal testing**
2. Crea una traccia e carica l'AAB
3. Aggiungi i tester tramite email
4. I tester ricevono un link per installare l'app

### Checklist pre-pubblicazione

- [ ] Il widget lista mostra gli APN correttamente
- [ ] L'APN attivo e' evidenziato
- [ ] Tap sul widget apre le impostazioni APN
- [ ] Il widget singolo mostra icona check/cross corretta
- [ ] La configurazione colore funziona
- [ ] Il widget si aggiorna al cambio di carrier
- [ ] L'app funziona senza SIM (mostra messaggio "No APN found")
- [ ] Il permesso READ_PHONE_STATE viene richiesto correttamente
- [ ] Testato su Android 8 (API 26) e Android 14/15 (API 34/35)

---

## Aggiornamenti futuri

Per rilasciare un aggiornamento:

1. Incrementa `versionCode` e `versionName` in `app/build.gradle.kts`
2. Genera un nuovo AAB: `./gradlew bundleRelease`
3. Carica nella Play Console in **Release > Production > Crea nuova release**
4. Aggiungi note di rilascio e pubblica

---

## Struttura dell'app

```
L'app non ha launcher activity - e' composta solo da widget.
Dopo l'installazione, aggiungi i widget dalla home screen:

  Tieni premuto sulla home > Widget > APN Selector

  - "APN List" (4x2): lista completa APN
  - "APN Monitor" (1x1): singolo APN con colore
```
