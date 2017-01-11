# android-report-battery-charge
Android app with a background service that reports battery charge to a web service

*Important*: I don’t have the slightest idea about Android. It works for me. If it doesn’t work for you, I most likely won’t be able to help! 🙁

I created this program to track my Venue 8 7840’s slowly dying battery. The service will put the data reported by the device in a RRDTool database.

## Things demonstrated
* Somewhat persistent background service
* Retrieve battery status notifications
* Send HTTP requests
* Copy text to clipboard

## Credits
* Persistent service: http://fabcirablog.weebly.com/blog/creating-a-never-ending-background-service-in-android

## License
MIT
