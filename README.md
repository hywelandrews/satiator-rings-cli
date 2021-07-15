# satiator-rings-cli #

Welcome to satiator-rings-cli, an alternative to the official [satiator-rings-config](https://github.com/retrohead/satiator-rings-config) tool.

This tool is useful if:
1) You are using a [Satiator](https://www.satiator.net/) to add drive emulation to your Sega Saturn
2) You are using the [Satiator Rings Menu](http://files-ds-scene.net/retrohead/satiator/) instead of the official Satiator menu
3) You want to add boxart using the command line (maybe because you are not running Windows)

## Usage ##

Requires >= Java 8

Create a directory to hold all boxart, with filenames as the Sega Saturn game id (i.e `T-12345.jpg`), 
a set exists on the [Internet Archive](https://archive.org/details/sega_saturn_covers) for convenience

```bash
$ unzip satiator-rings-cli.zip
$ ./satiator-rings upload --satiator /Volumes/SATIATOR --covers /path/to/covers
```

## What does it do? ##

satiator-rings-cli scans your SATIATOR volume and matches cover art based on the game Id and region, it will then convert 
images into the correct format and size and places the TGA files in the correct location on your SD card.

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with
any pull requests, please state that the contribution is your original work and that you license
the work to the project under the project's open source license. Whether or not you state this
explicitly, by submitting any copyrighted material via pull request, email, or other means you
agree to license the material under the project's open source license and warrant that you have the
legal authority to do so.

## License ##

This code is open source software licensed under the
[Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0) license.
