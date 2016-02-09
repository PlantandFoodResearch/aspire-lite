# TODO

Remaining TODO items for this Android app.

## Pre release

- Signing a test app - Jack Macensie?
- Get the legal disclaimer text reviewed
- Get the help reviewed
- Add a proper icon/branding
- Finish the README
- Ensure that the magic numbers are documented
- Ensure that the remaining documentation is online
- Finish RELEASING.md


## GridView

The GridView needs replacing:

- It currently causes some crash due to a combination of a keyboard/scrolling/button
- Animating new items appearing/reset would be nice
- The entry boxes are not positioned well (centering?)
- The focus rules are wonky
- The first item in the GridView is broken after a couple of resets


## UI

- Change the introduction text to something more useful
- Get Warrick to test on a tablet
- Figure out how to use styles
- Figure out how to set a palette
- Keep the screen on while entering data?
- Log messages may end up being displayed to the user

### UX or discoverability

- Message formatting/text sizes (visibility?)
- Provide some feedback for scrolling down for messages
- Underline entry boxes is not very clear - add a hint into the first box, which vanishes when it
  is focused, or start with it being focused.

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
- MainActivity.updateResults marks the wrong thing as invalid (see the inline TODO)
- Simplify the logging code to reduce getResources() usage
- Fix the TODO's in the code...


## Other

- Tests (check against database data, unit and ui tests)
  - Sarah has spreadsheet data for the tests
- Get Warrick to send out the app for testing
- Possibly restrict the range to being 2-32? Double check with Sarah
- Add documentation for the magic numbers/other decisions
  - 20 is apparently the minimum (Ruth Butler was the statistician?)
- PFR Aspire webpage link?
- Add an email to the feedback page?
- Add support for importing/exporting data (from a clipboard? another app?)
- Region-specific models
- Fix any non-coding TODO's
