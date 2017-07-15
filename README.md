PixelKnot
=========

PixelKnot is an image stego app with old school F5 steganography.

<a href="https://f-droid.org/packages/info.guardianproject.pixelknot" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="100"/></a>
<a href="https://play.google.com/store/apps/details?id=info.guardianproject.pixelknot" target="_blank">
<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="100"/></a>

## Development Setup

Follow these steps to setup your development environment:

1. Checkout PixelKnot git repository
2. Init and update git submodules

    cd PixelKnot
    git submodule update --init --recursive

3. Build Project

   ./gradlew assembleDebug (for all debug builds)

   For a list of tasks that can be used above, see "./gradlew tasks".
