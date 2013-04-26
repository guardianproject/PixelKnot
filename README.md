PixelKnot
=========

image stego app with old school F5 steganography

## Dev Setup

Follow these steps to setup your dev environment:

1. Checkout PixelKnot git repo
2. Init and update git submodules

    cd PixelKnot
    git submodule update --init --recursive

3. Import Project

   **Using Eclipse**

    Create a new Eclipse workspace in the root directory (PixelKnot) of the repo.

    For each of the following directories, "Import -> Android -> Existing Android Code Into Workspace":

        external/ActionBarSherlock/library/
        external/F5Android/

    Then, "Import -> General -> Existing Projects Into Workspace" for the `app/` directory.

