# Releasing

Steps to put out a new release:

- Commit any changes
- Update any copyright notices
- Adjust the versionName to the current date (YYYY-MM-DD), and increment the
  versionCode (both are defined in build.gradle)
- If using Android Studio, run "Sync project with gradle files"
- Build and test
- Commit any remaining changes
- Tag the commit to be released off (VCS->Git->Tag in Android studio, the tag
  name should be the same as the versionName)
- Sign with the appropriate key and release
  - In Android Studio, Build->Generate Signed APK...
  - Use the correct keystore, with the AspireLiteKey

Release locations:

- Github's releases
- Android's web store
- Other? (F-Droid?)
