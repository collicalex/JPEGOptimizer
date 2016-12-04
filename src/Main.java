import gui.windows.Gui;



public class Main {
	
	/*
	 * TODO:
	 *  - Check how to create an EXE from JAR
	 *  - Grab rotation in JPEG EXIFs
	 *  - Auto rotate image in Viewer
	 *  - Rotate option in viewer (R key ?)
	 *  - Zoom option in viewer (mouse wheel?)
	 *  - Add explanation page/window
	 *  - https://blog.axopen.com/2014/05/multithreading-bufferedimage-java-comparaison-dimage/
	 *  - http://www.impulseadventure.com/photo/jpeg-color-space.html
	 *  - JPEG format v9 can use 12bit per channel, see: http://www.tomshardware.fr/articles/jpeg-lossless-12bit,1-46742.html
	 */
	
	//-Xmx1024m
	public static void main(String[] args) {
		new Gui();
	}
}
