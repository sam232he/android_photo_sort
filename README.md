# Photo Organizer - Android App

An Android application to organize your son's photos by age, automatically creating folders like "1st_month", "2nd_month", etc.

## Features

- **Age-based Organization**: Automatically calculates your son's age in each photo and organizes them into appropriate folders
- **Easy Photo Selection**: Select multiple photos from your gallery with a simple interface
- **Birth Date Selection**: Set your son's birth date for accurate age calculation
- **Smart Folder Naming**: Creates intuitive folder names like:
  - `1st_month`, `2nd_month`, `3rd_month` for the first year
  - `1st_year`, `2nd_year` for subsequent years
  - `1st_year_1st_month` for detailed organization

## How to Use

1. **Set Birth Date**: Tap on the birth date field to select your son's birth date
2. **Select Photos**: Tap "Select Photos" to choose photos from your gallery
3. **Review Selection**: Check the selected photos in the grid view
4. **Organize**: Tap "Organize by Age" to automatically sort photos into age-based folders

## Installation

1. Open this project in Android Studio
2. Build and run the app on your Android device
3. Grant storage permissions when prompted

## Permissions Required

- **Read External Storage**: To access your photos
- **Write External Storage**: To create organized folders
- **Read Media Images**: For Android 13+ devices

## Folder Structure

Photos will be organized in: `Pictures/Son_Photos/[age_folder]/`

Example folder structure:
```
Pictures/Son_Photos/
├── 1st_month/
├── 2nd_month/
├── 3rd_month/
├── ...
├── 1st_year/
├── 1st_year_1st_month/
├── 2nd_year/
└── ...
```

## Technical Details

- **Minimum Android Version**: API 24 (Android 7.0)
- **Target Android Version**: API 34 (Android 14)
- **Language**: Kotlin
- **UI Framework**: Material Design 3
- **Image Loading**: Glide library

## Building the App

1. Ensure you have Android Studio installed
2. Open the project folder in Android Studio
3. Sync the project with Gradle files
4. Build and run on your device or emulator

## Notes

- The app creates copies of your photos in organized folders - your original photos remain untouched
- Photos are organized based on their EXIF data (creation date)
- The app handles various photo formats supported by Android
