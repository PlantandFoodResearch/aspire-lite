# TODO

Remaining TODO items for this Android app.

## Pre release

- Get the help reviewed
- Finish README.md
- Ensure that the magic numbers are documented
- Ensure that the remaining documentation is online (and email to Warrick)
- Finish RELEASING.md


## UI

- Change the introduction text to something more useful
- Get Warrick to test on a tablet
- Keep the screen on while entering data?
- Log messages may end up being displayed to the user

### UX or discoverability

- Message formatting/text sizes (visibility?)
- Provide some feedback for scrolling down for messages
- Animating new items appearing/reset would be nice
- Focus behaviour after the screen rotates is odd

### Keyboard

- Custom keyboard
- Keyboard not vanishing on "tap-off" - difficult to fix?
- Rotating the screen retracts the keyboard (elements loose focus)


## Refactor

- Separate EntryAdapter from activity_main
- Review private/public interfaces
- Split out calculation code from activity_main
- General code cleanup
- Add more comments and fix/update existing
- Split out the persistence/resume code?
- Filenames should be constants
- Simplify the logging code to reduce getResources() usage
- Move the constants into the resource files
- Fix the TODO's in the code...


## Other

- Tests (check against database data, unit and ui tests)
  - Sarah has spreadsheet data for the tests
- Possibly restrict the range to being 2-32? Double check with Sarah
- Add support for importing/exporting data (from a clipboard? another app?)
- Region-specific models
- Fix any non-coding TODO's
