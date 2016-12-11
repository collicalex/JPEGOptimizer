package exif;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Exif {
	
	private File _file;
	private boolean _debug = true;
	
	public Exif(File file) throws IOException {
		_file = file;
		this.parse();
	}
	
	
	//-------------------------------------------------------------------------
	//-- Data with getters
	//-------------------------------------------------------------------------

	
	
	//-------------------------------------------------------------------------
	//-- Parser
	//-------------------------------------------------------------------------
	
	private void debug(String message, boolean isError) {
		if (_debug) {
			if (isError) {
				System.err.println(message);
			} else {
				System.out.println(message);
			}
		}
	}
	
	private void debug(String message) {
		debug(message, false);
	}
	
	//Start Of Image
	private boolean isSOI(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0xD8));
	}

	//End Of Image
	private boolean isEOI(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0xD9));
	}
	
	//Application specific
	private boolean isAPP(int b0, int b1) {
		return ((b0 == 0xFF) && ((b1 & 0xF0) == 0xE0)); //APPn 0xFFEn
	}
	
	//Define Quantization Table
	private boolean isDQT(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0xDB));
	}

	//Define Huffman Table
	private boolean isDHT(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0xC4));
	}
	
	//Define Restart Interval
	private boolean isDRI(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0xDD));
	}
	
	//Start Of Frame (0xC0 = Baseline DCT / 0xC2 = Progressive DCT)
	private boolean isSOF(int b0, int b1) {
		return ((b0 == 0xFF) && ((b1 & 0xF0) == 0xC0)); //Usualy its 0xC0 or 0xC2
	}
	
	//Start Of Scan
	private boolean isSOS(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0xDA));
	}

	//Comment
	private boolean isCOM(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0xFE));
	}

	private boolean isJPG0(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0xF0)); 
	}
	
	private boolean isJPG13(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0xFD)); 
	}
	
	//Define Arithmetic Table
	private boolean isDAC(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0xCC)); 
	}
	
	private boolean isDNL(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0xDC));
	}
	
	private boolean isDHP(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0xDE));
	}
	
	private boolean isEXP(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0xDF));
	}
	
	private boolean isRST(int b0, int b1) {
		if ((b0 == 0xFF) && ((b1 & 0xF0) == 0xD0)) {
			int rstType = b1 & 0x0F;
			if ((rstType >= 0) && (rstType <= 7)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isTEM(int b0, int b1) {
		return ((b0 == 0xFF) && (b1 == 0x01));
	}
	
	private boolean isOtherSegmentType(int b0, int b1) {
		if (isDQT(b0, b1)) {
			return true;
		} else if (isDQT(b0, b1)) {
			return true;
		} else if (isDHT(b0, b1)) {
			return true;
		} else if (isDRI(b0, b1)) {
			return true;
		} else if (isSOF(b0, b1)) {
			return true;
		} else if (isSOS(b0, b1)) {
			return true;	
		} else if (isCOM(b0, b1)) {
			return true;	
		} else if (isJPG0(b0, b1)) {
			return true;	
		} else if (isJPG13(b0, b1)) {
			return true;	
		} else if (isDAC(b0, b1)) {
			return true;			
		} else if (isDNL(b0, b1)) {
			return true;
		} else if (isDHP(b0, b1)) {
			return true;
		} else if (isEXP(b0, b1)) {
			return true;
		} else if (isRST(b0, b1)) {
			return true;
		} else if (isTEM(b0, b1)) {
			return true;
		} else {
			return false;
		}
	}

	
	/*
	 * Exif parser
	 * https://www.media.mit.edu/pia/Research/deepview/exif.html
	 * http://www.phidels.com/php/tutoriaux/zip/exif.zip
	 * http://dev.exiv2.org/projects/exiv2/wiki/The_Metadata_in_JPEG_files
	 * http://vip.sugovica.hu/Sardi/kepnezo/JPEG%20File%20Layout%20and%20Format.htm
	 * 
	 * FFD8 FFE1 LLLL   data......data   (FFXX LLLL data...... data)xN FFD9
	 * SOI  APP1 Length   EXIF data         N JPEG sections           EOI
	 *      [----> section EXIF <----]
	 *      
	 * Bellow is a LL(1) parser --> Fastest read
	 */

	private int skipBytes(FileInputStream in, long length)  throws IOException {
		if (length <= 0) {
			throw new IOException("Try to skip " + length + " bytes. Must be strictly positive.");
		} else if (length == 1) {
			return in.read();
		} else if (length == 2) {
			in.read();
			return in.read();
		} else { //length > 2
			//System.out.println("SKIP " + length + " BYTE" + (length > 1 ? "S" : ""));
			long currentPos = in.getChannel().position();
			in.getChannel().position(currentPos + length - 2); //remove 2 byte more, because we want to read it manually to have it in buff _b0 and _b1 for LL(1) parsing
			in.read();
			return in.read();
		}
	}
	
	private void parse() throws IOException {
		if ((_file == null) || (_file.exists() == false) || (_file.canRead() == false)) {
			return ;
		}
		
		System.out.println("EXIF Parse file '" + _file.getAbsolutePath() + "'");
		
		FileInputStream in = new FileInputStream(_file);
		try {
			int b0 = -1;
			int b1 = -1;
			do {
				b0 = b1;
				b1 = in.read(); 
				if (b1 != -1) {
					if (isSOI(b0, b1)) {
						debug("TAG : SOI");
						parse_SOI(in);
					}
				}
			} while (b1 != -1);

		} finally {
			in.close();
		}		
	}

	private void parse_SOI(FileInputStream in) throws IOException {
		//can read directly 1 byte more
		int b0;
		int b1 = in.read();
		do {
			b0 = b1;
			b1 = in.read();
			if (b1 != -1) {
				if (isEOI(b0, b1)) {
					debug("TAG : EOI (" + in.getChannel().position() + " / " + in.getChannel().size() + ")");
					return ;
				} else if (isAPP(b0, b1)) {
					int appType = b1 & 0x001F;
					debug("TAG : APP" + appType);
					parse_APP(in, appType);
				} else if (isOtherSegmentType(b0, b1)) {
					debug("TAG : other");
					in.getChannel().position(in.getChannel().size()); //when read another segment... just stop reading the file
				}
			}
		} while (b1 != -1);
	}
	
	private void parse_APP(FileInputStream in, int appType) throws IOException {
		//Read APP section length
		int b0 = in.read();
		int b1 = in.read();
		
		int appLength = (((b0 << 8) & 0xFF00) | (b1 & 0x00FF)); //contains the length of EXIF data part + 2 bytes (2 bytes = the length of the app1Lenght itself)
		
		if (appLength < 2) {
			throw new IOException("APPn length must be greater or equal to 2 bytes"); //2 bytes = the length of the APPnLength itself
		}
		
		debug("      APP" + appType + " Length : " + appLength);
		
		if (appType == 1) {
			parse_APP1(in, appLength);
		} else {
			skipBytes(in, appLength-2); //appLength-2 because appLenght contain itself size (which is 2) already read
			return ;
		}
	}
	
	private int decode(int b0, int b1, boolean isLittleEndian) {
		if (isLittleEndian) {
			return ((b1 << 8) & 0xFF00) | (b0 & 0x00FF);
		} else {
			return ((b0 << 8) & 0xFF00) | (b1 & 0x00FF);
		}
	}
	
	private int decode(int b0, int b1, int b2, int b3, boolean isLittleEndian) {
		if (isLittleEndian) {
			return ((b3 << 24) & 0xFF000000) | ((b2 << 16) & 0x00FF0000) | ((b1 << 8) & 0x0000FF00) | (b0 & 0x000000FF);
		} else {
			return ((b0 << 24) & 0xFF000000) | ((b1 << 16) & 0x00FF0000) | ((b2 << 8) & 0x0000FF00) | (b3 & 0x000000FF);
		}
	}
	
	private void parse_APP1(FileInputStream in, int appLength) throws IOException {
		if (appLength < 16) { //6 bytes for EXIF00 header + 8 bytes for TIFF header + 2 bytes (app1Lenght itself)
			//It's not an EXIF APP1 part, skip it!
			debug("      APP1 is length is not enough for 'Exif00' tag, skip APP1 block");
			skipBytes(in, appLength-2); //app1Length-2 because app1Lenght contain itself size (which is 2) already read
			return ;
		}
		
		//Read "Exif#0#0" header (6 bytes length)
		//45 78 69 66 00 00
		// E  x  i  f #0 #0
		int b0 = in.read();
		int b1 = in.read();
		int b2 = in.read();
		int b3 = in.read();
		int b4 = in.read();
		int b5 = in.read();
		
		debug("      APP1 Header tag is '" + (char)b0 + (char)b1 + (char)b2 + (char)b3 + (char)b4 + (char)b5 + "'");
		
		if ((b0 != 0x45) || (b1 != 0x78) || (b2 != 0x69) || (b3 != 0x66) || (b4 != 0x00) || (b5 != 0x00)) {
			//It's not an EXIF APP1 part, skip it!
			debug("      APP1 is not tag with 'Exif00' header; skip APP1 block");
			skipBytes(in, appLength-2-6); //app1Length-2 because app1Lenght contain itself size (which is 2) already read; and -6 because we just read 6 bytes to check EXIF00 header
			return ;
		}
		
	
		//Read TIFF header (8 bytes length)
		//
		//Big Endian = Motorola
		//4D 4D   00 2A   00 00 00 08
		// M  M   Value   offset IFD0
		//
		//Little Endian = Intel
		//49 49   2A 00   08 00 00 00
		// I  I   Value   offset IFD0
		
		//Read TIFF header : Part 1, Check if data are in Little Endian or in Big Endian (2 bytes)
		long tiffHeaderPosition = in.getChannel().position();
		b0 = in.read();
		b1 = in.read();
		if (b0 != b1) {
			throw new IOException("APP1 does not contain a correct TIFF header");
		}
		if ((b0 != 0x4D) && (b0 != 0x49)) {
			throw new IOException("APP1 does not contain a correct TIFF header (wrong little or big endian byte)");
		}
		
		boolean isLittleEndian = (b0 == 0x49);
		
		debug("      APP1 TIFF Header : alignment is "+ (isLittleEndian ? "Little Endian (Intel)" : "Big Endian (Motorola)"));

		//Read TIFF header : Part 2, check word control (2 bytes)
		b0 = in.read();
		b1 = in.read();
		if (isLittleEndian) {
			if ((b0 != 0x2A) && (b1 != 0x00)) {
				throw new IOException("APP1 does not contain a correct TIFF header (wrong word control, must be 0x2A00 in little endian, but is 0x" + String.format("%02X", b0) + String.format("%02X", b1) + ")");
			}
		} else {
			if ((b0 != 0x00) && (b1 != 0x2A)) {
				throw new IOException("APP1 does not contain a correct TIFF header (wrong word control, must be 0x002A in big endian, but is 0x" + String.format("%02X", b0) + String.format("%02X", b1) + ")");
			}
		}
		
		debug("      APP1 TIFF Header : found correct alignment word control value " + (isLittleEndian ? "0x2A00" : "0x002A"));
		
		//Read TIFF header : Part 3, get IFD0 offset (4 bytes)
		//IFD = Image File Directory
		//IFD0 offset is generally equal to 8 bytes (which is the TIFF header size, but can be greater)
		b0 = in.read();
		b1 = in.read();
		b2 = in.read();
		b3 = in.read();
		
		int offsetToIFD0 = decode(b0, b1, b2, b3, isLittleEndian);
		
		if (offsetToIFD0 < 8) {
			throw new IOException("OffsetToIFD0 must be at least 8 bytes as the offset itself is coded in 8 bytes length");
		}
		
		debug("      APP1 TIFF Header : IFD0 offset is " + offsetToIFD0);

		//Go to IFD0
		if (offsetToIFD0 > 8) {
			skipBytes(in, offsetToIFD0-8); //offsetToIFD0-8 because offsetToIFD0 contains itself size (which is 8) already read;
		}
		
		parse_IFD0(in, isLittleEndian, tiffHeaderPosition);
	}
	
	private String decodeIFDTag(int tag, int b0, int b1, boolean isLittleEndian) {
		if (tag == 0x010e) {
			return "ImageDescription (Describes image)"; 	
		} else if (tag == 0x010f) {
			return "Make (Shows manufacturer of digicam)";
		} else if (tag == 0x0110) {
			return "Model (Shows model number of digicam)";
		} else if (tag == 0x0112) {
			return "Orientation (The orientation of the camera relative to the scene, when the image was captured. The start point of stored data is, '1' means upper left, '3' lower right, '6' upper right, '8' lower left, '9' undefined.)";
		} else if (tag == 0x011a) {
			return "XResolution (Display/Print resolution of image. Large number of digicam uses 1/72inch, but it has no mean because personal computer doesn't use this value to display/print out)";
		} else if (tag == 0x011b) {
			return "YResolution (Display/Print resolution of image. Large number of digicam uses 1/72inch, but it has no mean because personal computer doesn't use this value to display/print out)";
		} else if (tag == 0x0128) {
			return "ResolutionUnit (Unit of XResolution(0x011a)/YResolution(0x011b). '1' means no-unit, '2' means inch, '3' means centimeter.)";
		} else if (tag == 0x0131) {
			return "Software (Shows firmware(internal software of digicam) version number.)";
		} else if (tag == 0x0132) {
			return "DateTime (Date/Time of image was last modified. Data format is YYYY:MM:DD HH:MM:SS+0x00, total 20bytes. In usual, it has the same value of DateTimeOriginal(0x9003))";
		} else if (tag == 0x013e) {
			return "WhitePoint (Defines chromaticity of white point of the image. If the image uses CIE Standard Illumination D65(known as international standard of 'daylight'), the values are '3127/10000,3290/10000')";
		} else if (tag == 0x013f) {
			return "PrimaryChromaticities (Defines chromaticity of the primaries of the image. If the image uses CCIR Recommendation 709 primearies, values are '640/1000,330/1000,300/1000,600/1000,150/1000,0/1000')";
		} else if (tag == 0x0211) {
			return "YCbCrCoefficients (When image format is YCbCr, this value shows a constant to translate it to RGB format. In usual, values are '0.299/0.587/0.114')";
		} else if (tag == 0x0213) {
			return "YCbCrPositioning (When image format is YCbCr and uses 'Subsampling'(cropping of chroma data, all the digicam do that), defines the chroma sample point of subsampling pixel array. '1' means the center of pixel array, '2' means the datum point.)";
		} else if (tag == 0x0214) {
			return "ReferenceBlackWhite (Shows reference value of black point/white point. In case of YCbCr format, first 2 show black/white of Y, next 2 are Cb, last 2 are Cr. In case of RGB format, first 2 show black/white of R, next 2 are G, last 2 are B.)";
		} else if (tag == 0x8298) {
			return "Copyright (Shows copyright information)";
		} else if (tag == 0x8769) {
			return "ExifOffset (Offset to Exif Sub IFD)";
		} else if (tag == 0x013B) {
			return "Artist";
		} else if (tag == 0x8825) {
			return "GPSInfo";
		} else if (tag == 0x829a) {
			return "ExposureTime (Exposure time (reciprocal of shutter speed). Unit is second.)";
		} else if (tag == 0x829d) {
			return "FNumber (The actual F-number(F-stop) of lens when the image was taken.)";
		} else if (tag == 0x8822) {
			return "ExposureProgram (Exposure program that the camera used when image was taken. '1' means manual control, '2' program normal, '3' aperture priority, '4' shutter priority, '5' program creative (slow program), '6' program action(high-speed program), '7' portrait mode, '8' landscape mode.)";
		} else if (tag == 0x8827) {
			return "ISOSpeedRatings (CCD sensitivity equivalent to Ag-Hr film speedrate.)";
		} else if (tag == 0x9000) {
			return "ExifVersion (Exif version number. Stored as 4bytes of ASCII character (like '0210'))";
		} else if (tag == 0x9003) {
			return "DateTimeOriginal (Date/Time of original image taken. This value should not be modified by user program.)";
		} else if (tag == 0x9004) {
			return "DateTimeDigitized (Date/Time of image digitized. Usually, it contains the same value of DateTimeOriginal(0x9003).)";
		} else if (tag == 0x9101) {
			return "ComponentConfiguration (Unknown. It seems value 0x00,0x01,0x02,0x03 always)";
		} else if (tag == 0x9102) {
			return "CompressedBitsPerPixel 	(The average compression ratio of JPEG.)";
		} else if (tag == 0x9201) {
			return "ShutterSpeedValue (Shutter speed. To convert this value to ordinary 'Shutter Speed'; calculate this value's power of 2, then reciprocal. For example, if value is '4', shutter speed is 1/(2^4)=1/16 second.)";
		} else if (tag == 0x9202) {
			return "ApertureValue (The actual aperture value of lens when the image was taken. To convert this value to ordinary F-number(F-stop), calculate this value's power of root 2 (=1.4142). For example, if value is '5', F-number is 1.4142^5 = F5.6.)";
		} else if (tag == 0x9203) {
			return "BrightnessValue (Brightness of taken subject, unit is EV.)";
		} else if (tag == 0x9204) {
			return "ExposureBiasValue (Exposure bias value of taking picture. Unit is EV)";
		} else if (tag == 0x9205) {
			return "MaxApertureValue (Maximum aperture value of lens. You can convert to F-number by calculating power of root 2 (same process of ApertureValue(0x9202))";
		} else if (tag == 0x9206) {
			return "SubjectDistance (Distance to focus point, unit is meter)";
		} else if (tag == 0x9207) {
			return "MeteringMode (Exposure metering method. '1' means average, '2' center weighted average, '3' spot, '4' multi-spot, '5' multi-segment.)";
		} else if (tag == 0x9208) {
			return "LightSource (Light source, actually this means white balance setting. '0' means auto, '1' daylight, '2' fluorescent, '3' tungsten, '10' flash.)";
		} else if (tag == 0x9209) {
			return "Flash ('1' means flash was used, '0' means not used)";
		} else if (tag == 0x920a) {
			return "FocalLength (Focal length of lens used to take image. Unit is millimeter)";
		} else if (tag == 0x927c) {
			return "MakerNote (Maker dependent internal data. Some of maker such as Olympus/Nikon/Sanyo etc. uses IFD format for this area)";
		} else if (tag == 0x9286) {
			return "UserComment (Stores user comment)";
		} else if (tag == 0xa000) {
			return "FlashPixVersion (Stores FlashPix version. Unknown but 4bytes of ASCII characters '0100'exists)";
		} else if (tag == 0xa001) {
			return "ColorSpace (Unknown, value is '1')";
		} else if (tag == 0xa002) {
			return "ExifImageWidth 	(Size of main image)";
		} else if (tag == 0xa003) {
			return "ExifImageHeight";
		} else if (tag == 0xa004) {
			return "RelatedSoundFile (If this digicam can record audio data with image, shows name of audio data.)";
		} else if (tag == 0xa005) {
			return "ExifInteroperabilityOffset (Extension of 'ExifR98', detail is unknown. This value is offset to IFD format data. Currently there are 2 directory entries, first one is Tag0x0001, value is 'R98', next is Tag0x0002, value is '0100')";
		} else if (tag == 0xa20e) {
			return "FocalPlaneXResolution (CCD's pixel density)";
		} else if (tag == 0xa20f) {
			return "FocalPlaneYResolution";
		} else if (tag == 0xa210) {
			return "FocalPlaneResolutionUnit (Unit of FocalPlaneXResoluton/FocalPlaneYResolution. '1' means no-unit, '2' inch, '3' centimeter)";
		} else if (tag == 0xa217) {
			return "SensingMethod 	(Shows type of image sensor unit. '2' means 1 chip color area sensor, most of all digicam use this type)";
		} else if (tag == 0xa300) {
			return "FileSource 	(Unknown but value is '3')";
		} else if (tag == 0xa301) {
			return "SceneType (Unknown but value is '1')";
		} else {
			if (isLittleEndian) {
				return "UNKNOWN TAG "+ String.format("%02X", b1) + " " + String.format("%02X", b0);
			} else {
				return "UNKNOWN TAG "+ String.format("%02X", b0) + " " + String.format("%02X", b1);
			}
		}
	}
	
	private String decodeIFDFormat(int format) {
		if (format == 1) {
			return "unsigned byte (length : 1 byte)";
		} else if (format == 2) {
			return "ascii strings (length : 1 byte)";
		} else if (format == 3) {
			return "unsigned short (length : 2 byte)";
		} else if (format == 4) {
			return "unsigned long (length : 4 byte)";
		} else if (format == 5) {
			return "unsigned rational (length : 8 byte)";
		} else if (format == 6) {
			return "signed byte (length : 1 byte)";
		} else if (format == 7) {
			return "undefined (length : 1 byte)";
		} else if (format == 8) {
			return "signed short (length : 2 byte)";
		} else if (format == 9) {
			return "signed long (length : 4 byte)";
		} else if (format == 10) {
			return "signed rational (length : 8 byte)";
		} else if (format == 11) {
			return "signed float (length : 4 byte)";
		} else if (format == 12) {
			return "double float (length : 8 byte)";
		} else if (format == 13) {
			return "offset to subdirectory (length : 4 byte)";
		} else {
			return "BUG";
		}
	}
	
	//IFD0 = EXIF DATA
	private void parse_IFD0(FileInputStream in, boolean isLittleEndian, long tiffHeaderPosition) throws IOException {
		parse_SubIFD(in, isLittleEndian, "IDF0", tiffHeaderPosition);
		
		int b0 = in.read();
		int b1 = in.read();
		int b2 = in.read();
		int b3 = in.read();
		
		int offsetToIFD1 = decode(b0, b1, b2, b3, isLittleEndian);

		debug("IFD0 Offset to IFD1 : " + offsetToIFD1 + " (" + String.format("%02X", b0) + " " + String.format("%02X", b1) + " " + String.format("%02X", b2) + " " + String.format("%02X", b3) + ")");
		
		if (offsetToIFD1 != 0) {
			skipBytes(in, offsetToIFD1);
			parse_IFD1(in, isLittleEndian);
		}
		
		//We have done reading EXIF, just put read cursor at end of file to finish!
		in.getChannel().position(in.getChannel().size());
	}
	
	private void parse_SubIFD(FileInputStream in, boolean isLittleEndian, String prefix, long tiffHeaderPosition) throws IOException { 
		int b0, b1, b2, b3;
		
		b0 = in.read();
		b1 = in.read();
		int nbIFDEntries = decode(b0, b1, isLittleEndian);
		
		debug(prefix + " Entries : " + nbIFDEntries);
		
		List<Long> subIDF= new LinkedList<Long>();

		for (int i = 1; i <= nbIFDEntries; ++i) {
			b0 = in.read();
			b1 = in.read();
			int tag = decode(b0, b1, isLittleEndian);
			
			b0 = in.read();
			b1 = in.read();
			int format = decode(b0, b1, isLittleEndian);
			if ((format < 1) || (format > 13)) {
				throw new IOException("IDF tag format must bet between [1-13], but is " + format);
			}
			
			b0 = in.read();
			b1 = in.read();
			b2 = in.read();
			b3 = in.read();
			int count = decode(b0, b1, b2, b3, isLittleEndian);

			b0 = in.read();
			b1 = in.read();
			b2 = in.read();
			b3 = in.read();
			int value = decode(b0, b1, b2, b3, isLittleEndian);
			
			if (tag == 0x8769) { //Offset to Exif Sub IFD
				if ((format != 4) && (format != 13)) {
					throw new IOException("IDF tag 'ExifOffset' (0x8769) must be in format 4 (unsigned long) or 13 (offset to subdirectory), but is " + format);
				}
				subIDF.add((long)value);
			} else {
				parseIFDData(i, tag, format, count, b0, b1, b2, b3, isLittleEndian, in, tiffHeaderPosition);
			}
		}
		
		for (Long offset : subIDF) {
			long position = in.getChannel().position();
			in.getChannel().position(tiffHeaderPosition + offset);
			parse_SubIFD(in, isLittleEndian, "Sub-IDF", tiffHeaderPosition);
			in.getChannel().position(position);
		}
	}
	
	private void parseIFDData(int idx, int tag, int format, int count, int b0, int b1, int b2, int b3, boolean isLittleEndian, FileInputStream in, long tiffHeaderPosition) throws IOException {
		if (decodeIFDTag(tag, b0, b1, isLittleEndian).startsWith("UNKNOWN TAG")) {
			debug("      " + String.format("%02d", idx) + " : TAG = " + decodeIFDTag(tag, b0, b1, isLittleEndian), true);
		} else {
			debug("      " + String.format("%02d", idx) + " : TAG = " + decodeIFDTag(tag, b0, b1, isLittleEndian));
		}
		debug("      " + String.format("%02d", idx) + " : FORMAT = " + decodeIFDFormat(format));
		debug("      " + String.format("%02d", idx) + " : COUNT = " + count);
		debug("      " + String.format("%02d", idx) + " : VALUE = " + decode(b0, b1, b2, b3, isLittleEndian) + " (" + String.format("%02X", b0) + " " + String.format("%02X", b1) + " " + String.format("%02X", b2) + " " + String.format("%02X", b3) + ")");

		Object value = null;
		
		if (format == 1) {
			//return "unsigned byte (length : 1 byte)";
		} else if (format == 2) { //"ascii strings (length : 1 byte)" 
			String str = "";
			if (count == 4) {
				//str += (char)b0 + (char)b1 + (char)b2 + (char)b3;
			} else if (count == 3) {
				//str += (char)b0 + (char)b1 + (char)b2;
			} else if (count == 2) {
				//str += (char)b0 + (char)b1;
			} else if (count == 1) {
				str += (char)b0;
			} else {
				long position = in.getChannel().position();
				int offset = decode(b0, b1, b2, b3, isLittleEndian);
				in.getChannel().position(tiffHeaderPosition + offset);
				for (int i = 0; i < count; ++i) {
					str += (char)in.read();
				}
				in.getChannel().position(position);
			}
			value = str;
		} else if (format == 3) { //"unsigned short (length : 2 byte)";
			if (count == 1) {
				value = new Integer(decode(b0, b1, isLittleEndian));
			} else {
				//TODO
			}
		} else if (format == 4) {
			//return "unsigned long (length : 4 byte)";
			if (count == 1) {
				value = decode(b0, b1, b2, b3, isLittleEndian);
			}
		} else if (format == 5) { //"unsigned rational (length : 8 byte)";
			long position = in.getChannel().position();
			int offset = decode(b0, b1, b2, b3, isLittleEndian);
			in.getChannel().position(tiffHeaderPosition + offset);
			
			int o1 = in.read();
			int o2 = in.read();
			int o3 = in.read();
			int o4 = in.read();
			int numerator = decode(o1, o2, o3, o4, isLittleEndian);
			
			int o5 = in.read();
			int o6 = in.read();
			int o7 = in.read();
			int o8 = in.read();
			int denominator = decode(o5, o6, o7, o8, isLittleEndian);
			
			String str = numerator + "/" + denominator;
			value = str;
			
			in.getChannel().position(position);
		} else if (format == 6) {
			//return "signed byte (length : 1 byte)";
		} else if (format == 7) {
			//return "undefined (length : 1 byte)";
		} else if (format == 8) {
			//return "signed short (length : 2 byte)";
		} else if (format == 9) {
			//return "signed long (length : 4 byte)";
		} else if (format == 10) {
			//return "signed rational (length : 8 byte)";
		} else if (format == 11) {
			//return "signed float (length : 4 byte)";
		} else if (format == 12) {
			//return "double float (length : 8 byte)";
		} else if (format == 13) {
			//return "offset to subdirectory (length : 4 byte)";
		} else {
			//return "BUG";
		}

		if (value == null) {
			debug("      " + String.format("%02d", idx) + " : Unable to decode format " + decodeIFDFormat(format) + " with count object " + count, true);
		}
		debug("      " + String.format("%02d", idx) + " : DECODED VALUE = " + value, value == null);
	}
	
	// IFD1 = Thumbnail
	private void parse_IFD1(FileInputStream in, boolean isLittleEndian) throws IOException { 
		
	}
	

	
}
