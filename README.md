> Note: SSTVEncoder is no longer under development. Please see [SSTVEncoder2](https://github.com/olgamiller/SSTVEncoder2).


SSTV Encoder
Copyright 2014 Olga Miller <olga.rgb@googlemail.com>

-------------- Description:

This application sends images via Slow Scan Television (SSTV).

Currently supported modes are:
    Martin Modes:  Martin 1, Martin 2
    PD Modes:      PD 50, PD 90, PD 120, PD 160, PD 180, PD 240, PD 290
    Scottie Modes: Scottie 1, Scottie 2, Scottie DX
    Robot Modes:   Robot 36 Color, Robot 72 Color
    Wraase Modes:  Wraase SC2 180

-------------- Usage:

Use the "Share" option of any application (like "Album", "Gallery" or "Photos")
to load an image.

Single click to add text.
Single click on text to edit.
Long click to move text.

-------------- Remarks:

After clicking on a mode the image will be scaled to that mode's native size.
To keep the aspect ratio, black borders will be added if necessary.
Original image can be resend using another mode without reloading.

The mode specifications are taken from the "Dayton Paper" of JL Barber:
http://www.barberdsp.com/files/Dayton%20Paper.pdf

-------------- SSTV Decoder:

SSTV images can be decoded for example using this application:
https://github.com/xdsopl/robot36/tree/android