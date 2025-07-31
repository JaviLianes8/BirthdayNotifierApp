# BirthdayNotifierApp

Birthday Notifier is a simple Kotlin Android application used to send WhatsApp birthday greetings. It stores birthday data in Firebase Firestore and allows editing the list from the phone. The app schedules a daily alarm to check birthdays and can also be triggered manually.

## Features

- Google Sign-In using Firebase Authentication.
- Birthdays stored in Firestore for the logged user.
- Daily check via `AlarmManager` and `BroadcastReceiver` at 09:00.
- Sends a notification that opens WhatsApp with a pre‑filled message.
- Simple editor to add, edit or delete birthday entries.
- Manual trigger button for testing birthday notifications.

## Building

The project is a standard Gradle Android application. To build you need the Android SDK and a valid `google-services.json` for your Firebase project.

1. Install Android Studio or the command line SDK.
2. Create a `local.properties` file in the `BirthdayNotifierApp` directory with:
   ```
   sdk.dir=/path/to/Android/sdk
   ```
3. Place your `google-services.json` file under `BirthdayNotifierApp/app/`.
4. Run:
   ```
   ./gradlew assembleDebug
   ```

## Running

After installing the debug APK on a device, sign in with Google. Use the **Test** button to send notifications immediately or open the birthday list to manage your entries. The app will automatically run every day at 9 AM to send WhatsApp messages to matching birthdays.

