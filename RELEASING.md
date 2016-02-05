# Releasing

TODO: Write this...

Steps to put out a new release:

- Commit any changes
- Update any copyright notices
- Adjust the versionName to the current date (YYYY-MM-DD), and increment the
  versionCode (both are defined in build.gradle)
- If using Android Studio, run "Sync project with gradle files"
- Build and run any tests (TODO: Flesh out)
- Commit any remaining changes
- Tag the commit to be released off (VCS->Git->Tag in Android studio, the tag
  name should be the same as the versionName)
- Sign with the appropriate key and release (TODO: Flesh out)

Release locations:

- Github's releases
- Android's web store
- Other? (F-Droid?)
