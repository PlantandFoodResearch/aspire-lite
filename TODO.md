# TODO

Remaining TODO items for this Android app.

## Pre release

- Fix 5->20
- Signing a test app - Jack Macensie?
- Add the legal disclaimer, and get any text reviewed
- Add a proper icon/branding
- Finish the README
- Add on Github
- Ensure that the magic numbers are documented

## Help/menu system

- Actually write some help
- Get the help reviewed


## UI

- First-time start message
  - Add an introduction for the first time that the app starts?
  - (Or, most likely) print a introduction message for the empty/reset state
- Review usability for the "flat" button styles
- Look at other ways to fix the reset button
  - Hide during data entry?
- Get Warrick to test on a tablet
- Figure out how to use styles
- Figure out how to set a palette
- Animations? (Where would be appropriate?)
- Fix the remaining hardcoded strings (log messages)
- Fix CHO result formatting (reduce insane number of decimal places)
- Keep the screen on while entering data?

### UX or discoverability

- Message formatting/text sizes (visibility?)
- Focus not moving as expected in the gridview
- Gridview not positioning the entry boxes well
- Scroll to newly added?
- Focus on newly added?
- Scroll to removed? Or remove focused?
- Provide some feedback for scrolling down for messages

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
- Use sp instead of dp where appropriate 


## Other

- Tests (check against database data, unit and ui tests)
  - Sarah has spreadsheet data for the tests
- Get Warrick to send out the app for testing
- Possibly restrict the range to being 2-32? Double check with Sarah
- Add documentation for the magic numbers/other decisions
  - 20 is apparently the minimum (Ruth Butler was the statistician?)
- PFR Aspire webpage link?
- Based on PFR science
- Add feedback links
  - Github issues?
  - Old Aspire email?
- Add support for importing/exporting data (from a clipboard? another app?)
- Region-specific models