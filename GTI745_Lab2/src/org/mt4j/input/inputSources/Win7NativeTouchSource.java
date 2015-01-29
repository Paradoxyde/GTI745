
package org.mt4j.input.inputSources;

import javax.swing.SwingUtilities;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.misc.basicStructures.HWND;
import org.xvolks.jnative.util.User32;

public class Win7NativeTouchSource {

	private static final String dllName = "Win7Touch";
	private static boolean libraryLoaded = false;
	public static String windowTitle;
	//private static final String canvasClassName = "SunAwtCanvas";
	private static final String canvasClassName = "SunAwtFrame";
	private static boolean libraryInitialized = false;

	public Native_WM_TOUCH_Event event;

	//TODO disable touch delay due to tap&hold gesture
	//-> windows tries to make a tap&hold gesture and doesent send WM_TOUCH! (TWF_WANTPALM? flick gesture? registerTouchWindow on toplvl frame?)
	//-> in control panel-> pen and touch-> disable "Enable multi-touch gestures and inking" ? Or Change "Touch actions"->"Settings..." ?

	// These are provided by the DLL
	private native boolean init( long HWND );
	private native boolean getSystemMetrics();
	private native boolean pollEvent( Native_WM_TOUCH_Event event );

	public void initialize( String winTitle ) {

		windowTitle = winTitle;

		if ( ! libraryLoaded ) {
			try {
				System.loadLibrary( dllName );
				libraryLoaded = true;
				System.out.println("Win7NativeTouchSource.initialize(): library " + dllName + ".dll loaded.");
			} catch ( Error e ) {
				e.printStackTrace();
			}
		}

		if ( ! libraryLoaded ) return;

		if ( libraryInitialized ) {
			System.out.println("Win7NativeTouchSource.initialize(): already initialized");
			return;
		}

		boolean touchAvailable = this.getSystemMetrics();
		//boolean touchAvailable = org.mt4j.input.inputSources.Win7NativeTouchSource.getSystemMetrics();
		if ( ! touchAvailable ) {
			// Windows 7 Touch Input currently not available
			System.out.println("Win7NativeTouchSource.initialize(): multitouch currently not available");
			return;
		}
		else {
			System.out.println("Win7NativeTouchSource.initialize(): multitouch available");
		}

		event = new Native_WM_TOUCH_Event();
		event.id = -1;
		event.type = -1;
		event.x = -1;
		event.y = -1;

		System.out.println("Win7NativeTouchSource.initialize(): A");
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				System.out.println("Win7NativeTouchSource.initialize(): B");
				HWND windowHandle = new HWND(0);
				int windowHandleNumber = 0;
				try {
					windowHandle = User32.FindWindow( null, Win7NativeTouchSource.windowTitle );
					windowHandleNumber = windowHandle.getValue();
					System.out.println("Win7NativeTouchSource.initialize(): windowHandleNumber: "+windowHandleNumber + " == 0x" + Integer.toHexString(windowHandleNumber) );
				}
				catch (NativeException e1) {
					e1.printStackTrace();
				}
				catch (IllegalAccessException e1) {
					e1.printStackTrace();
				}
				System.out.println("Win7NativeTouchSource.initialize(): C");

				/*
				//this always return 0...
				try {
					HWND appHWND = User32.GetActiveWindow();
					applicationWindowHandle = appHWND.getValue();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				*/

				try {
					System.out.println("Win7NativeTouchSource.initialize(): D");
					// try using spy++ (or WinSpy++) to check and debug
					HWND sunAwtCanvasHandle = User32.FindWindowEx(windowHandle, new HWND(0), canvasClassName, null); //Find child canvas
					System.out.println("Win7NativeTouchSource.initialize(): E");
					int sunAwtCanvasHandleNumber = sunAwtCanvasHandle.getValue();
					System.out.println("Win7NativeTouchSource.initialize(): sunAwtCanvasHandleNumber: "+sunAwtCanvasHandleNumber + " == 0x" + Integer.toHexString(sunAwtCanvasHandleNumber) );
					if ( sunAwtCanvasHandleNumber > 0 ) {
						System.out.println("Win7NativeTouchSource.initialize(): F1");
						// Initialize c++ core
						init(sunAwtCanvasHandleNumber);
						libraryInitialized = true;
						System.out.println("Win7NativeTouchSource.initialize(): G1");
					}
					else {
						System.out.println("Win7NativeTouchSource.initialize(): F2");
						init((long)(windowHandleNumber));
						libraryInitialized = true;
						System.out.println("Win7NativeTouchSource.initialize(): G2");
					}
				}
				catch (NativeException e) {
					e.printStackTrace();
				}
				catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				System.out.println("Win7NativeTouchSource.initialize(): H");
			}
		} );
	}

	// If an input event is available, this method returns true,
	// and the caller can retrieve the event information from the public data member ``event''.
	// If no input event is available, this returns false immediately (i.e. without blocking).
	public boolean pollForInputEvent() {
		if ( libraryInitialized ) {
			return pollEvent(event);
		}
		return false;
	}

	public class Native_WM_TOUCH_Event {
		public static final int TOUCH_DOWN = 0;
		public static final int TOUCH_MOVE = 1;
		public static final int TOUCH_UP = 2;
		public int type;
		public int id;
		public int x;
		public int y;
	}
}

