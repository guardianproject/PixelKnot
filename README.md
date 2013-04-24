PixelKnot
=========

image stego app with old school F5 steganography

## Dev Setup

Follow these steps to setup your dev environment:

1. Checkout PixelKnot git repo
2. Init and update git submodules

    git submodule update --init --recursive

3. Import Project

   **Using Eclipse**

    I recommend using a new, fresh, empty workspace in Eclipse.

    Import into Eclipse (using the *"existing Android project"* option) the
    projects in the following order. Do not check "copy projects to workspace".

    Note:The import order is crucial!

        ./external/ActionBarSherlock/library
        ./external/F5Android
        ./ (the PixelKnot root dir)

