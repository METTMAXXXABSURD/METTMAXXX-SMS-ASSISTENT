# METTMAXXX SMS ASSISTENT V3.1 – Quellcode

Dies ist der Quellcode der Android-App **METTMAXXX SMS ASSISTENT V3.1**.

Die App ist ein kostenloses Hobbyprojekt, das mit KI-Unterstützung entwickelt wurde. Sie dient als alternative Bedienoberfläche für den **manuellen Versand einzelner SMS** über die normale SMS-Funktion des Android-Smartphones.

## Funktion

- Telefonnummer manuell eingeben
- Nachricht mit maximal 160 Zeichen schreiben
- Pro Betätigung des großen METTMAXXX-Logos wird genau eine SMS ausgelöst
- SMS-Versand über Android und die eigene SIM / Mobilfunkverbindung
- Zähler für ausgelöste SMS
- optionaler kurzer Signalton beim Senden
- schwarz-rotes Cyberdesign

## Berechtigungen

Die App verwendet:

- `android.permission.SEND_SMS` – erforderlich, damit Android den SMS-Versand über die App zulässt

Die App besitzt **keine INTERNET-Berechtigung** und benötigt für ihre Kernfunktion keinen eigenen Server, kein Benutzerkonto und keine Cloud.

## Inhalt dieses Pakets

- `AndroidManifest.xml`
- Java-Quellcode unter `src/`
- App-Icon unter `res/mipmap/`
- benötigte Grafiken unter `assets/`

## Sicherheit

Dieses Quellcode-Paket enthält **keine privaten Signierschlüssel, keine Keystores, keine Passwörter und keine Zugangsdaten**.

## Hinweis

Die App ist für den manuellen Versand normaler SMS gedacht. Sie ist nicht für anonymen Versand, das Fälschen von Absendernummern oder automatisierten Massenversand vorgesehen.
