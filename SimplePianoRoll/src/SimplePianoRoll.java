
import java.util.ArrayList;

import java.awt.Container;
import java.awt.Component;
import java.awt.Graphics;
// import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Math.min;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.ButtonGroup;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;
import javax.swing.JFileChooser;
import javax.sound.midi.*;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



/*
   The octave:
      pitch class     English name     French name
           0             C             do
           1             C#/Db         do diese / re bemol
           2             D             re
           3             D#/Eb         re diese / mi bemol
           4             E             mi
           5             F             fa
           6             F#/Gb         fa diese / sol bemol
           7             G             sol
           8             G#/Ab         sol diese / la bemol
           9             A             la
          10             A#/Bb         la diese / si bemol
          11             B             si
           0             C             do

   A grand piano keyboard has 88 keys:
                              Note Name     MIDI note number     Pitch class
      lowest key (1st key):       A0            21                     9
      middle C:                   C4            60                     0
      highest key (88th key):     C8           108                     0
*/


class Score {
	public static final int midiNoteNumberOfMiddleC = 60;

	public int numPitches = 88;
	public static final int pitchClassOfLowestPitch = 9; // 9==A==la
	public static final int midiNoteNumberOfLowestPitch = 21;
	public int numBeats = 64;
	public final int MAX_BEATS = 128;
	public boolean [][] grid;

	public static final int numPitchesInOctave = 12;
	public String [] namesOfPitchClasses;
	public boolean [] pitchClassesInMajorScale;
	public boolean [] pitchClassesToEmphasizeInMajorScale;

	public Score() {
		grid = new boolean[ MAX_BEATS ][ numPitches ];

		namesOfPitchClasses = new String[ numPitchesInOctave ];
		namesOfPitchClasses[ 0] = "C";
		namesOfPitchClasses[ 1] = "C#";
		namesOfPitchClasses[ 2] = "D";
		namesOfPitchClasses[ 3] = "D#";
		namesOfPitchClasses[ 4] = "E";
		namesOfPitchClasses[ 5] = "F";
		namesOfPitchClasses[ 6] = "F#";
		namesOfPitchClasses[ 7] = "G";
		namesOfPitchClasses[ 8] = "G#";
		namesOfPitchClasses[ 9] = "A";
		namesOfPitchClasses[10] = "A#";
		namesOfPitchClasses[11] = "B";

		pitchClassesInMajorScale = new boolean[ numPitchesInOctave ];
		pitchClassesInMajorScale[ 0] = true;
		pitchClassesInMajorScale[ 1] = false;
		pitchClassesInMajorScale[ 2] = true;
		pitchClassesInMajorScale[ 3] = false;
		pitchClassesInMajorScale[ 4] = true;
		pitchClassesInMajorScale[ 5] = true;
		pitchClassesInMajorScale[ 6] = false;
		pitchClassesInMajorScale[ 7] = true;
		pitchClassesInMajorScale[ 8] = false;
		pitchClassesInMajorScale[ 9] = true;
		pitchClassesInMajorScale[10] = false;
		pitchClassesInMajorScale[11] = true;

		pitchClassesToEmphasizeInMajorScale = new boolean[ numPitchesInOctave ];
		pitchClassesToEmphasizeInMajorScale[ 0] = true;
		pitchClassesToEmphasizeInMajorScale[ 1] = false;
		pitchClassesToEmphasizeInMajorScale[ 2] = false;
		pitchClassesToEmphasizeInMajorScale[ 3] = false;
		pitchClassesToEmphasizeInMajorScale[ 4] = true;
		pitchClassesToEmphasizeInMajorScale[ 5] = true;
		pitchClassesToEmphasizeInMajorScale[ 6] = false;
		pitchClassesToEmphasizeInMajorScale[ 7] = true;
		pitchClassesToEmphasizeInMajorScale[ 8] = false;
		pitchClassesToEmphasizeInMajorScale[ 9] = false;
		pitchClassesToEmphasizeInMajorScale[10] = false;
		pitchClassesToEmphasizeInMajorScale[11] = false;
	}
        
        public void setBeatCount(int count)
        {
            numBeats = count;
        }

	// returns -1 if out of bounds
	public int getMidiNoteNumberForMouseY( GraphicsWrapper gw, int mouse_y ) {
		float y = gw.convertPixelsToWorldSpaceUnitsY( mouse_y );
		int indexOfPitch = (int)(-y);
                if (indexOfPitch < 0)
                    indexOfPitch = 0;
                if (indexOfPitch >= numPitches)
                    indexOfPitch = numPitches - 1;
                
                return indexOfPitch + midiNoteNumberOfLowestPitch;
	}

	// returns -1 if out of bounds
	public int getBeatForMouseX( GraphicsWrapper gw, int mouse_x ) {
		float x = gw.convertPixelsToWorldSpaceUnitsX( mouse_x );
		int indexOfBeat = (int)x;
                if (indexOfBeat < 0)
                    indexOfBeat = 0;
                if (indexOfBeat >= numBeats)
                    indexOfBeat = numBeats - 1;
                
                return indexOfBeat;
	}

	public void draw(
		GraphicsWrapper gw,
		boolean highlightMajorCScale,
		int midiNoteNumber1ToHilite,
		int beat1ToHilite,
		int beat2ToHilite,
                MyCanvas canvas
	) {
		for ( int y = 0; y < numPitches; y++ ) {
			int pitchClass = ( y + pitchClassOfLowestPitch ) % numPitchesInOctave;
			int midiNoteNumber = y + midiNoteNumberOfLowestPitch;
			if ( midiNoteNumber == midiNoteNumber1ToHilite ) { // mouse cursor
				gw.setColor( 0, 1, 1 );
				gw.fillRect( 0, -y-0.8f, numBeats, 0.6f );
			}

			if ( midiNoteNumber == midiNoteNumberOfMiddleC ) {
				gw.setColor( 1, 1, 1 );
				gw.fillRect( 0, -y-0.7f, numBeats, 0.4f );
			}
			else if ( pitchClass == 0 && highlightMajorCScale ) {
				gw.setColor( 1, 1, 1 );
				gw.fillRect( 0, -y-0.6f, numBeats, 0.2f );
			}
			else if ( pitchClassesToEmphasizeInMajorScale[ pitchClass ] && highlightMajorCScale ) {
				gw.setColor( 0.6f, 0.6f, 0.6f );
				gw.fillRect( 0, -y-0.6f, numBeats, 0.2f );
			}
			else if ( pitchClassesInMajorScale[ pitchClass ] || ! highlightMajorCScale ) {
				gw.setColor( 0.6f, 0.6f, 0.6f );
				gw.fillRect( 0, -y-0.55f, numBeats, 0.1f );
			}
		}
		for ( int x = 0; x < numBeats; x++ ) {
			if ( x == beat1ToHilite ) { // mouse cursor
				gw.setColor( 0, 1, 1 );
				gw.fillRect( x+0.2f, -numPitches, 0.6f, numPitches );
			}

			if ( x == beat2ToHilite ) { // time cursor
				gw.setColor( 1, 0, 0 );
				gw.fillRect( x+0.45f, -numPitches, 0.1f, numPitches );
			}
			else if ( x % 4 == 0 ) {
				gw.setColor( 0.6f, 0.6f, 0.6f );
				gw.fillRect( x+0.45f, -numPitches, 0.1f, numPitches );
			}
		}
		gw.setColor( 0, 0, 0 );
		for ( int y = 0; y < numPitches; ++y ) {
			for ( int x = 0; x < numBeats; ++x ) {
                            if (!canvas.selectionActive || x < canvas.selectionX || x > canvas.selectionX + canvas.selectionW || y < canvas.selectionY || y > canvas.selectionY + canvas.selectionH)
                            {
                                if ( grid[x][y] )
                                {
                                    gw.fillRect( x+0.3f, -y-0.7f, 0.4f, 0.4f );

                                    if (x > 0 && grid[x-1][y])
                                        gw.fillRect( x-0.3f, -y-0.7f, 0.6f, 0.4f );
                                }
                            }
                            else
                            {
                                if ( canvas.selectionGrid[x - (int)canvas.selectionX][y - (int)canvas.selectionY] )
                                {
                                    gw.fillRect( x+0.3f, -y-0.7f, 0.4f, 0.4f );
                                }
                            }
			}
		}
	}

	public AlignedRectangle2D getBoundingRectangle() {
		return new AlignedRectangle2D(
			new Point2D(0,-numPitches),
			new Point2D(numBeats,0)
		);
	}

}

class MyCanvas extends JPanel implements KeyListener, MouseListener, MouseMotionListener, Runnable {

	SimplePianoRoll simplePianoRoll;
	GraphicsWrapper gw = new GraphicsWrapper();

	public Score score = new Score();

	Thread thread = null;
	boolean threadSuspended;

	int currentBeat = 0;

	public static final int RADIAL_MENU_PLAY = 0;
	public static final int RADIAL_MENU_STOP = 1;
	public static final int RADIAL_MENU_DRAW = 2;
	public static final int RADIAL_MENU_ERASE = 3;

	public static final int CONTROL_MENU_ZOOM = 0;
	public static final int CONTROL_MENU_PAN = 1;
	public static final int CONTROL_MENU_TEMPO = 2;
	public static final int CONTROL_MENU_TOTAL_DURATION = 3;
	public static final int CONTROL_MENU_TRANSPOSE = 4;

	RadialMenuWidget radialMenu = new RadialMenuWidget();
	ControlMenuWidget controlMenu = new ControlMenuWidget();

	int mouse_x, mouse_y, old_mouse_x, old_mouse_y;

	boolean isControlKeyDown = false;

	int beatOfMouseCursor = -1; // -1 for none
	int midiNoteNumberOfMouseCurser = -1; // -1 for none

	public MyCanvas( SimplePianoRoll sp ) {
		simplePianoRoll = sp;
		setBorder( BorderFactory.createLineBorder( Color.black ) );
		setBackground( Color.white );
		addKeyListener( this );
		addMouseListener( this );
		addMouseMotionListener( this );

		radialMenu.setItemLabelAndID( RadialMenuWidget.CENTRAL_ITEM, "",            RADIAL_MENU_STOP );
		radialMenu.setItemLabelAndID( 1,                             "Stop Music",  RADIAL_MENU_STOP );
		radialMenu.setItemLabelAndID( 3,                             "Draw Notes",  RADIAL_MENU_DRAW );
		radialMenu.setItemLabelAndID( 5,                             "Play Music",  RADIAL_MENU_PLAY );
		radialMenu.setItemLabelAndID( 7,                             "Erase Notes", RADIAL_MENU_ERASE );

		controlMenu.setItemLabelAndID( ControlMenuWidget.CENTRAL_ITEM, "", -1 );
		controlMenu.setItemLabelAndID( 1, "Tempo", CONTROL_MENU_TEMPO );
		controlMenu.setItemLabelAndID( 2, "Pan", CONTROL_MENU_PAN );
		controlMenu.setItemLabelAndID( 3, "Zoom", CONTROL_MENU_ZOOM );
		controlMenu.setItemLabelAndID( 5, "Total Duration", CONTROL_MENU_TOTAL_DURATION );
		controlMenu.setItemLabelAndID( 7, "Transpose", CONTROL_MENU_TRANSPOSE );

		gw.frame( score.getBoundingRectangle(), false );
	}
        
	public Dimension getPreferredSize() {
		return new Dimension( Constant.INITIAL_WINDOW_WIDTH, Constant.INITIAL_WINDOW_HEIGHT );
	}
	public void clear() {
		for ( int y = 0; y < score.numPitches; ++y )
			for ( int x = 0; x < score.numBeats; ++x )
				score.grid[x][y] = false;
		repaint();
	}
	public void frameAll() {
		gw.frame( score.getBoundingRectangle(), false );
		repaint();
	}
	public void paintComponent( Graphics g ) {
		super.paintComponent( g );
		gw.set( g );
		if ( getWidth() != gw.getWidth() || getHeight() != gw.getHeight() )
			gw.resize( getWidth(), getHeight() );
		gw.clear(0.4f,0.4f,0.4f);
		gw.setupForDrawing();
		gw.setCoordinateSystemToWorldSpaceUnits();
		gw.enableAlphaBlending();

		score.draw(
			gw,
			simplePianoRoll.highlightMajorScale,
			midiNoteNumberOfMouseCurser,
			beatOfMouseCursor,
			currentBeat,
                        this
		);
                
                if (isRMBDown || selectionActive)
                {
                    float drawX = selectionX;
                    float drawY = selectionY;
                    float drawW = selectionW;
                    float drawH = selectionH;
                    
                    if (drawW < 0)
                    {
                        drawX += drawW;
                        drawW *= -1f;
                    }
                    if (drawH > 0)
                    {
                        drawY += drawH;
                        drawH *= -1f;
                    }
                    gw.setColor( 0, 1, 0, 0.25f);
                    gw.fillRect(drawX, -drawY - 1, drawW + 1, -drawH + 1);
                }

		gw.setCoordinateSystemToPixels();

		if ( radialMenu.isVisible() )
			radialMenu.draw( gw );
		if ( controlMenu.isVisible() )
			controlMenu.draw( gw );

		if ( ! radialMenu.isVisible() && ! controlMenu.isVisible() ) {
			// draw datatip
			if ( midiNoteNumberOfMouseCurser >= 0 && beatOfMouseCursor >= 0 ) {
				final int margin = 5;
				final int x_offset = 15;

				String s = score.namesOfPitchClasses[
					( midiNoteNumberOfMouseCurser - score.midiNoteNumberOfLowestPitch + score.pitchClassOfLowestPitch )
					% score.numPitchesInOctave
				];
				int x0 = mouse_x + x_offset;
				int y0 = mouse_y - RadialMenuWidget.textHeight - 2*margin;
				int height = RadialMenuWidget.textHeight + 2*margin;
				int width = Math.round( gw.stringWidth( s ) + 2*margin );
				gw.setColor( 0, 0, 0, 0.6f );
				gw.fillRect( x0, y0, width, height );
				gw.setColor( 1, 1, 1 );
				gw.drawRect( x0, y0, width, height );
				gw.drawString( mouse_x + x_offset + margin, mouse_y - margin, s );
			}
		}
	}

	public void keyPressed( KeyEvent e ) {
		if ( e.getKeyCode() == KeyEvent.VK_CONTROL ) {
			isControlKeyDown = true;
			if (
				beatOfMouseCursor>=0
				&& simplePianoRoll.rolloverMode == SimplePianoRoll.RM_PLAY_NOTE_UPON_ROLLOVER_IF_SPECIAL_KEY_HELD_DOWN
			)
				playNote( midiNoteNumberOfMouseCurser );
		}
                
                if (e.getKeyCode() == KeyEvent.VK_C && isControlKeyDown)
                {
                    duplicateSelection();
                }
	}
	public void keyReleased( KeyEvent e ) {
		if ( e.getKeyCode() == KeyEvent.VK_CONTROL ) {
			isControlKeyDown = false;
			stopPlayingNote( midiNoteNumberOfMouseCurser );
		}
	}
	public void keyTyped( KeyEvent e ) {
	}


	public void mouseClicked( MouseEvent e ) { }
	public void mouseEntered( MouseEvent e ) { }
	public void mouseExited( MouseEvent e ) { }

	private void paint( int mouse_x, int mouse_y ) {
		int newBeatOfMouseCursor = score.getBeatForMouseX( gw, mouse_x );
		int newMidiNoteNumberOfMouseCurser = score.getMidiNoteNumberForMouseY( gw, mouse_y );
		if (
			newBeatOfMouseCursor != beatOfMouseCursor
			|| newMidiNoteNumberOfMouseCurser != midiNoteNumberOfMouseCurser
		) {
			beatOfMouseCursor = newBeatOfMouseCursor;
			midiNoteNumberOfMouseCurser = newMidiNoteNumberOfMouseCurser;
			repaint();
		}

		if ( beatOfMouseCursor >= 0 && midiNoteNumberOfMouseCurser >= 0 ) {
			if ( simplePianoRoll.dragMode == SimplePianoRoll.DM_DRAW_NOTES ) {
				if ( score.grid[beatOfMouseCursor][midiNoteNumberOfMouseCurser-score.midiNoteNumberOfLowestPitch] != true ) {
					score.grid[beatOfMouseCursor][midiNoteNumberOfMouseCurser-score.midiNoteNumberOfLowestPitch] = true;
					repaint();
				}
			}
			else if ( simplePianoRoll.dragMode == SimplePianoRoll.DM_ERASE_NOTES ) {
				if ( score.grid[beatOfMouseCursor][midiNoteNumberOfMouseCurser-score.midiNoteNumberOfLowestPitch] != false ) {
					score.grid[beatOfMouseCursor][midiNoteNumberOfMouseCurser-score.midiNoteNumberOfLowestPitch] = false;
					repaint();
				}
			}
		}
	}
        
        public boolean isLMBDown = false;
        public boolean isRMBDown = false;
        
        public boolean selectionActive = false;
        public boolean movingSelection = false;
        public float selectionX;
        public float selectionY;
        public float selectionW;
        public float selectionH;
        
	public boolean [][] selectionGrid;
        
        public void duplicateSelection()
        {
            for (int i = 0; i <= (int)selectionW; i++)
            {
                for (int j = 0; j <= (int)selectionH; j++)
                {
                    if (i + (int)selectionX >= 0 && i + (int)selectionX < score.MAX_BEATS && j + (int)selectionY >= 0 && j + (int)selectionY < score.grid[i + (int)selectionX].length)
                        score.grid[i + (int)selectionX][j + (int)selectionY] = selectionGrid[i][j];
                } 
            }
        }

	public void mousePressed( MouseEvent e ) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
                
		isControlKeyDown = e.isControlDown();
                
                if (SwingUtilities.isLeftMouseButton(e))
                    isLMBDown = true;
                if (SwingUtilities.isRightMouseButton(e) && !e.isShiftDown())
                {
                    if (selectionActive)
                    {
                        selectionActive = false;
                        duplicateSelection();
                    }
                    isRMBDown = true;
                    selectionX = score.getBeatForMouseX( gw, mouse_x );
                    selectionY = score.getMidiNoteNumberForMouseY( gw, mouse_y )-score.midiNoteNumberOfLowestPitch;
                    selectionW = 0;
                    selectionH = 0;
                    repaint();
                }
                if (SwingUtilities.isRightMouseButton(e) && e.isShiftDown() && selectionActive)
                {
                    movingSelection = true;
                }

		if ( radialMenu.isVisible() || (SwingUtilities.isLeftMouseButton(e) && e.isControlDown()) ) {
			int returnValue = radialMenu.pressEvent( mouse_x, mouse_y );
			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		if ( controlMenu.isVisible() || (SwingUtilities.isLeftMouseButton(e) && e.isShiftDown()) ) {
			int returnValue = controlMenu.pressEvent( mouse_x, mouse_y );
			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		if ( SwingUtilities.isLeftMouseButton(e) ) {
			paint( mouse_x, mouse_y );
		}
	}

	public void mouseReleased( MouseEvent e ) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();

		isControlKeyDown = e.isControlDown();

                if (SwingUtilities.isLeftMouseButton(e))
                    isLMBDown = false;
                if (SwingUtilities.isRightMouseButton(e))
                {
                    isRMBDown = false;
                    if (movingSelection)
                        movingSelection = false;
                    else
                    {
                        selectionW = score.getBeatForMouseX( gw, mouse_x ) - selectionX;
                        selectionH = (score.getMidiNoteNumberForMouseY( gw, mouse_y ) - score.midiNoteNumberOfLowestPitch) - selectionY;
                        if (selectionW < 0)
                        {
                            selectionX += selectionW;
                            selectionW *= -1f;
                        }
                        if (selectionH < 0)
                        {
                            selectionY += selectionH;
                            selectionH *= -1f;
                        }

                        if (selectionH > 0 || selectionW > 0)
                            selectionActive = true;

                        selectionGrid = new boolean[ (int)selectionW + 1 ][ (int)selectionH + 1 ];

                        for (int i = 0; i <= (int)selectionW; i++)
                        {
                            for (int j = 0; j <= (int)selectionH; j++)
                            {
                                if (i + (int)selectionX >= 0 && i + (int)selectionX < score.MAX_BEATS && j + (int)selectionY >= 0 && j + (int)selectionY < score.grid[i + (int)selectionX].length)
                                {
                                    selectionGrid[i][j] = score.grid[i + (int)selectionX][j + (int)selectionY];
                                    score.grid[i + (int)selectionX][j + (int)selectionY] = false;
                                }
                            } 
                        }

                        repaint();
                    }
                }
                
		if ( radialMenu.isVisible() ) {
			int returnValue = radialMenu.releaseEvent( mouse_x, mouse_y );

			int itemID = radialMenu.getIDOfSelection();
			if ( 0 <= itemID ) {
				switch ( itemID ) {
					case RADIAL_MENU_PLAY:
						simplePianoRoll.setMusicPlaying( true );
						break;
					case RADIAL_MENU_STOP:
						simplePianoRoll.setMusicPlaying( false );
						break;
					case RADIAL_MENU_DRAW:
						simplePianoRoll.setDragMode( SimplePianoRoll.DM_DRAW_NOTES );
						break;
					case RADIAL_MENU_ERASE:
						simplePianoRoll.setDragMode( SimplePianoRoll.DM_ERASE_NOTES );
						break;
				}
			}

			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		if ( controlMenu.isVisible() ) {
			int returnValue = controlMenu.releaseEvent( mouse_x, mouse_y );

			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
	}

	private void playNote( int midiNoteNumber ) {
		if ( Constant.USE_SOUND && midiNoteNumber >= 0 ) {
			simplePianoRoll.midiChannels[0].noteOn(midiNoteNumber,Constant.midiVolume);
		}
	}
	private void stopPlayingNote( int midiNoteNumber ) {
		if ( Constant.USE_SOUND && midiNoteNumber >= 0 ) {
			simplePianoRoll.midiChannels[0].noteOff(midiNoteNumber);
		}
	}

	public void mouseMoved( MouseEvent e ) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();

		requestFocusInWindow();

		isControlKeyDown = e.isControlDown();

		if ( radialMenu.isVisible() ) {
			int returnValue = radialMenu.moveEvent( mouse_x, mouse_y );
			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		if ( controlMenu.isVisible() ) {
			int returnValue = controlMenu.moveEvent( mouse_x, mouse_y );
			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		else {
			int newBeatOfMouseCursor = score.getBeatForMouseX( gw, mouse_x );
			int newMidiNoteNumberOfMouseCurser = score.getMidiNoteNumberForMouseY( gw, mouse_y );
			if ( newBeatOfMouseCursor != beatOfMouseCursor ) {
				beatOfMouseCursor = newBeatOfMouseCursor;
				repaint();
			}
			if ( newMidiNoteNumberOfMouseCurser != midiNoteNumberOfMouseCurser ) {
				stopPlayingNote( midiNoteNumberOfMouseCurser );
				midiNoteNumberOfMouseCurser = newMidiNoteNumberOfMouseCurser;
				if (
					beatOfMouseCursor>=0
					&& (
						simplePianoRoll.rolloverMode == SimplePianoRoll.RM_PLAY_NOTE_UPON_ROLLOVER
						|| (
							simplePianoRoll.rolloverMode == SimplePianoRoll.RM_PLAY_NOTE_UPON_ROLLOVER_IF_SPECIAL_KEY_HELD_DOWN
							&& isControlKeyDown
						)
					)
				)
					playNote( midiNoteNumberOfMouseCurser );
				repaint();
			}
		}

	}

	public void mouseDragged( MouseEvent e ) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		int delta_x = mouse_x - old_mouse_x;
		int delta_y = mouse_y - old_mouse_y;

		isControlKeyDown = e.isControlDown();
                
                if (isRMBDown)
                {
                    selectionW = score.getBeatForMouseX( gw, mouse_x ) - selectionX;
                    selectionH = (score.getMidiNoteNumberForMouseY( gw, mouse_y ) - score.midiNoteNumberOfLowestPitch) - selectionY;
                    repaint();
                }
                
                if (movingSelection)
                {
                    float oldPosX = score.getBeatForMouseX( gw, old_mouse_x );
                    float oldPosY = (score.getMidiNoteNumberForMouseY( gw, old_mouse_y));
                    float posX = score.getBeatForMouseX( gw, mouse_x ) ;
                    float posY = (score.getMidiNoteNumberForMouseY( gw, mouse_y ));
                    
                    if (oldPosX != posX)
                    {
                        selectionX += posX - oldPosX;
                    }
                    if (oldPosY != posY)
                    {
                        selectionY += posY - oldPosY;
                    }
                    
                    repaint();
                }

		if ( radialMenu.isVisible() ) {
			int returnValue = radialMenu.dragEvent( mouse_x, mouse_y );
			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		if ( controlMenu.isVisible() ) {
			if ( controlMenu.isInMenuingMode() ) {
				int returnValue = controlMenu.dragEvent( mouse_x, mouse_y );
				if ( returnValue == CustomWidget.S_REDRAW )
					repaint();
				if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
					return;
			}
			else {
				// use the drag event to change the appropriate parameter
				switch ( controlMenu.getIDOfSelection() ) {
				case CONTROL_MENU_PAN:
					gw.pan( delta_x, delta_y );
					break;
				case CONTROL_MENU_ZOOM:
					gw.zoomIn( (float)Math.pow( Constant.zoomFactorPerPixelDragged, delta_x-delta_y ) );
					break;
				default:
					// TODO XXX
					break;
				}
				repaint();
			}
		}
                else if (isLMBDown)
                {
			paint( mouse_x, mouse_y );
		}
	}

	public void startBackgroundWork() {
		currentBeat = 0;
		if ( thread == null ) {
			thread = new Thread( this );
			threadSuspended = false;
			thread.start();
		}
		else {
			if ( threadSuspended ) {
				threadSuspended = false;
				synchronized( this ) {
					notify();
				}
			}
		}
	}
	public void stopBackgroundWork() {
		threadSuspended = true;
	}
	public void run() {
		try {
			int sleepIntervalInMilliseconds = 150;
			while (true) {

				// Here's where the thread does some work
				synchronized( this ) {
					if ( Constant.USE_SOUND ) {
						for ( int i = 0; i < score.numPitches; ++i ) {
							if ( score.grid[currentBeat][i] )
                                                            if (currentBeat == score.numBeats - 1 || !score.grid[currentBeat + 1][i])
								simplePianoRoll.midiChannels[0].noteOff( i+score.midiNoteNumberOfLowestPitch );
						}
					}
					currentBeat += 1;
					if ( currentBeat >= score.numBeats )
						currentBeat = 0;
					if ( Constant.USE_SOUND ) {
						for ( int i = 0; i < score.numPitches; ++i ) {
							if ( score.grid[currentBeat][i] )
                                                            if (currentBeat == 0 || !score.grid[currentBeat - 1][i])
								simplePianoRoll.midiChannels[0].noteOn( i+score.midiNoteNumberOfLowestPitch, Constant.midiVolume );
						}
					}
				}
				repaint();

				// Now the thread checks to see if it should suspend itself
				if ( threadSuspended ) {
					synchronized( this ) {
						while ( threadSuspended ) {
							wait();
						}
					}
				}
				thread.sleep( simplePianoRoll.tempoSlider.getValue() );  // interval given in milliseconds
			}
		}
		catch (InterruptedException e) { }
	}

}

public class SimplePianoRoll implements ActionListener, ChangeListener, MouseListener {

        Metronome metronome;
	static final String applicationName = "Simple Piano Roll";

	JFrame frame;
	Container toolPanel;
	MyCanvas canvas;

	Synthesizer synthesizer;
	MidiChannel [] midiChannels;

	JMenuItem clearMenuItem;
	JMenuItem saveAsMidiMenuItem;
	JMenuItem saveAsMenuItem;
        JMenuItem loadMidiMenuItem;
	JMenuItem loadMenuItem;
	JMenuItem quitMenuItem;
	JCheckBoxMenuItem showToolsMenuItem;
	JCheckBoxMenuItem highlightMajorScaleMenuItem;
	JMenuItem frameAllMenuItem;
	JCheckBoxMenuItem autoFrameMenuItem;
	JMenuItem aboutMenuItem;

	JCheckBox playCheckBox;
	JCheckBox loopWhenPlayingCheckBox;

	JRadioButton drawNotesRadioButton;
	JRadioButton eraseNotesRadioButton;

	JRadioButton doNothingUponRolloverRadioButton;
	JRadioButton playNoteUponRolloverRadioButton;
	JRadioButton playNoteUponRolloverIfSpecialKeyHeldDownRadioButton;

        JSlider tempoSlider; 
        JLabel tempoLabel;
        
        JSlider beatsSlider; 
        JLabel beatsLabel;
        
        public static final int NOTE_ON = 0x90;
        public static final int NOTE_OFF = 0x80;

	public boolean isMusicPlaying = false;
	public boolean isMusicLoopedWhenPlayed = false;
	public boolean highlightMajorScale = true;
	public boolean isAutoFrameActive = true;

	// The DM_ prefix is for Drag Mode
	public static final int DM_DRAW_NOTES = 0;
	public static final int DM_ERASE_NOTES = 1;
	public int dragMode = DM_DRAW_NOTES;

	// The RM_ prefix is for Rollover Mode
	public static final int RM_DO_NOTHING_UPON_ROLLOVER = 0;
	public static final int RM_PLAY_NOTE_UPON_ROLLOVER = 1;
	public static final int RM_PLAY_NOTE_UPON_ROLLOVER_IF_SPECIAL_KEY_HELD_DOWN = 2;
	public int rolloverMode = RM_DO_NOTHING_UPON_ROLLOVER;

	public void setMusicPlaying( boolean flag ) {
		isMusicPlaying = flag;
		playCheckBox.setSelected( isMusicPlaying );
		if ( isMusicPlaying )
			canvas.startBackgroundWork();
		else
			canvas.stopBackgroundWork();
	}
	public void setDragMode( int newDragMode ) {
		dragMode = newDragMode;
		if ( dragMode == DM_DRAW_NOTES )
			drawNotesRadioButton.setSelected(true);
		else if ( dragMode == DM_ERASE_NOTES )
			eraseNotesRadioButton.setSelected(true);
		else assert false;
	}

	public void setRolloverMode( int newRolloverMode ) {
		rolloverMode = newRolloverMode;
		if ( rolloverMode == RM_DO_NOTHING_UPON_ROLLOVER )
			doNothingUponRolloverRadioButton.setSelected(true);
		else if ( rolloverMode == RM_PLAY_NOTE_UPON_ROLLOVER )
			playNoteUponRolloverRadioButton.setSelected(true);
		else if ( rolloverMode == RM_PLAY_NOTE_UPON_ROLLOVER_IF_SPECIAL_KEY_HELD_DOWN )
			playNoteUponRolloverIfSpecialKeyHeldDownRadioButton.setSelected(true);
		else assert false;
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if ( source == clearMenuItem ) {
			canvas.clear();
		}
                else if ( source == saveAsMenuItem ) {
                    JFileChooser fc = new JFileChooser();
                        
                    int returnVal = fc.showSaveDialog(frame);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try
                        {
                            FileOutputStream fos = new FileOutputStream(file);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            oos.writeObject(canvas.score.grid);
                            oos.close();
                        }
                        catch(Exception ex)
                        {
                            // Do not care much really
                        }
                    }

                }
                else if ( source == loadMenuItem ) {
                    JFileChooser fc = new JFileChooser();
                        
                    int returnVal = fc.showOpenDialog(frame);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try
                        {
                            FileInputStream fis = new FileInputStream(file);
                            ObjectInputStream ois = new ObjectInputStream(fis);
                            canvas.score.grid = (boolean[][])ois.readObject();
                            ois.close();
                        }
                        catch(Exception ex)
                        {
                            // Do not care much really
                        }
                    }

                }
                else if ( source == saveAsMidiMenuItem ) {
                        JFileChooser fc = new JFileChooser();
                        
                        int returnVal = fc.showSaveDialog(frame);

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = new File(fc.getSelectedFile().getAbsolutePath() + ".mid");
                            try
                            {

                                Sequence s;
                                s = new Sequence(javax.sound.midi.Sequence.PPQ,24);

                //****  Obtain a MIDI track from the sequence  ****
                                Track t = s.createTrack();

                //****  General MIDI sysex -- turn on General MIDI sound set  ****
                                byte[] b = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
                                SysexMessage sm = new SysexMessage();
                                sm.setMessage(b, 6);
                                MidiEvent me = new MidiEvent(sm,(long)0);
                                t.add(me);

                //****  set tempo (meta event)  ****
                                MetaMessage mt = new MetaMessage();
                                byte[] bt = {0x02, (byte)0x00, 0x00};
                                mt.setMessage(0x51 ,bt, 3);
                                me = new MidiEvent(mt,(long)0);
                                t.add(me);

                //****  set track name (meta event)  ****
                                mt = new MetaMessage();
                                String TrackName = new String("midifile track");
                                mt.setMessage(0x03 ,TrackName.getBytes(), TrackName.length());
                                me = new MidiEvent(mt,(long)0);
                                t.add(me);

                //****  set omni on  ****
                                ShortMessage mm = new ShortMessage();
                                mm.setMessage(0xB0, 0x7D,0x00);
                                me = new MidiEvent(mm,(long)0);
                                t.add(me);

                //****  set poly on  ****
                                mm = new ShortMessage();
                                mm.setMessage(0xB0, 0x7F,0x00);
                                me = new MidiEvent(mm,(long)0);
                                t.add(me);

                //****  set instrument to Piano  ****
                                mm = new ShortMessage();
                                mm.setMessage(0xC0, 0x00, 0x00);
                                me = new MidiEvent(mm,(long)0);
                                t.add(me);

                                
                                int tick = 0;
                                for (int i = 0; i < canvas.score.numBeats; ++i)
                                {
                                    for (int j = 0; j < canvas.score.numPitches; ++j)
                                    {
                                        if (canvas.score.grid[i][j])
                                        {
                            //****  note on - middle C  ****
                                            mm = new ShortMessage();
                                            mm.setMessage(NOTE_ON, j,0x60);
                                            me = new MidiEvent(mm,(long)tick);
                                            t.add(me);

                            //****  note off - middle C - 120 ticks later  ****
                                            mm = new ShortMessage();
                                            mm.setMessage(NOTE_OFF, j,0x40);
                                            me = new MidiEvent(mm,(long)tick + 150);
                                            t.add(me);
                                        }
                                    }
                                    
                                    tick += 150;
                                }

                //****  set end of track (meta event) 19 ticks later  ****
                                mt = new MetaMessage();
                                byte[] bet = {}; // empty array
                                mt.setMessage(0x2F,bet,0);
                                me = new MidiEvent(mt, (long)140);
                                t.add(me);
                                
                                
                                MidiSystem.write(s,1,file);
                            }
                            catch(Exception ex) { }
                            
                        }
                        
                }
                else if ( source == loadMidiMenuItem) {
                    JFileChooser fc = new JFileChooser();
                        
                    int returnVal = fc.showOpenDialog(frame);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        
                        canvas.clear();
                        try
                        {
                            Sequence sequence = MidiSystem.getSequence(file);

                            int beat = 0;
                            for (Track track :  sequence.getTracks()) {
                                for (int i=0; i < track.size(); i++) {
                                    MidiEvent event = track.get(i);
                                    MidiMessage message = event.getMessage();
                                    if (message instanceof ShortMessage) {
                                        ShortMessage sm = (ShortMessage) message;
                                        if (sm.getCommand() == NOTE_ON) {
                                            int key = sm.getData1();
                                            // int octave = (key / 12)-1;
                                            int note = key;
                                            // String noteName = canvas.score.namesOfPitchClasses[note];
                                            // int velocity = sm.getData2();
                                            beat = (int)(event.getTick() / 150);
                                            if (beat <= canvas.score.numBeats && note < canvas.score.numPitches)
                                            {
                                                canvas.score.grid[beat][note] = true;
                                            }
                                        } 
                                        // else if (sm.getCommand() == NOTE_OFF) {
                                        //    int key = sm.getData1();
                                        //    int octave = (key / 12)-1;
                                        //    int note = key % 12;
                                        //    String noteName = canvas.score.namesOfPitchClasses[note];
                                        //    int velocity = sm.getData2();
                                        //}
                                    }
                                }
                            }
                        } catch(Exception ex) {}
                    }
                }
		else if ( source == quitMenuItem ) {
			int response = JOptionPane.showConfirmDialog(
				frame,
				"Really quit?",
				"Confirm Quit",
				JOptionPane.YES_NO_OPTION
			);

			if (response == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		}
		else if ( source == showToolsMenuItem ) {
			Container pane = frame.getContentPane();
			if ( showToolsMenuItem.isSelected() ) {
				pane.removeAll();
				pane.add( toolPanel );
				pane.add( canvas );
			}
			else {
				pane.removeAll();
				pane.add( canvas );
			}
			frame.invalidate();
			frame.validate();
		}
		else if ( source == highlightMajorScaleMenuItem ) {
			highlightMajorScale = highlightMajorScaleMenuItem.isSelected();
			canvas.repaint();
		}
		else if ( source == frameAllMenuItem ) {
			canvas.frameAll();
			canvas.repaint();
		}
		else if ( source == autoFrameMenuItem ) {
			isAutoFrameActive = autoFrameMenuItem.isSelected();
			canvas.repaint();
		}
		else if ( source == aboutMenuItem ) {
			JOptionPane.showMessageDialog(
				frame,
				"'" + applicationName + "' Sample Program\n"
					+ "Original version written April 2011",
				"About",
				JOptionPane.INFORMATION_MESSAGE
			);
		}
		else if ( source == playCheckBox ) {
			isMusicPlaying = playCheckBox.isSelected();
			if ( isMusicPlaying )
				canvas.startBackgroundWork();
			else
				canvas.stopBackgroundWork();
		}
		else if ( source == loopWhenPlayingCheckBox ) {
			isMusicLoopedWhenPlayed = loopWhenPlayingCheckBox.isSelected();
		}
		else if ( source == drawNotesRadioButton ) {
			dragMode = DM_DRAW_NOTES;
		}
		else if ( source == eraseNotesRadioButton ) {
			dragMode = DM_ERASE_NOTES;
		}
		else if ( source == doNothingUponRolloverRadioButton ) {
			rolloverMode = RM_DO_NOTHING_UPON_ROLLOVER;
		}
		else if ( source == playNoteUponRolloverRadioButton ) {
			rolloverMode = RM_PLAY_NOTE_UPON_ROLLOVER;
		}
		else if ( source == playNoteUponRolloverIfSpecialKeyHeldDownRadioButton ) {
			rolloverMode = RM_PLAY_NOTE_UPON_ROLLOVER_IF_SPECIAL_KEY_HELD_DOWN;
		}
	}


	// For thread safety, this should be invoked
	// from the event-dispatching thread.
	//
	private void createUI() {
		if ( Constant.USE_SOUND ) {
			try {
				synthesizer = MidiSystem.getSynthesizer();
				synthesizer.open();
				midiChannels = synthesizer.getChannels();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ( ! SwingUtilities.isEventDispatchThread() ) {
			System.out.println(
				"Warning: UI is not being created in the Event Dispatch Thread!");
			assert false;
		}

		frame = new JFrame( applicationName );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		JMenuBar menuBar = new JMenuBar();
			JMenu menu = new JMenu("File");
				clearMenuItem = new JMenuItem("Clear");
				clearMenuItem.addActionListener(this);
				menu.add(clearMenuItem);
                                
                                loadMenuItem = new JMenuItem("Load");
                                loadMenuItem.addActionListener(this);
                                menu.add(loadMenuItem);
                                
				saveAsMenuItem = new JMenuItem("Save");
				saveAsMenuItem.addActionListener(this);
				menu.add(saveAsMenuItem);
                                
                                loadMidiMenuItem = new JMenuItem("Load MIDI");
                                loadMidiMenuItem.addActionListener(this);
                                menu.add(loadMidiMenuItem);
                                
				saveAsMidiMenuItem = new JMenuItem("Save As (MIDI)");
				saveAsMidiMenuItem.addActionListener(this);
				menu.add(saveAsMidiMenuItem);
                                
                                

				menu.addSeparator();

				quitMenuItem = new JMenuItem("Quit");
				quitMenuItem.addActionListener(this);
				menu.add(quitMenuItem);
			menuBar.add(menu);
			menu = new JMenu("View");
				showToolsMenuItem = new JCheckBoxMenuItem("Show Options");
				showToolsMenuItem.setSelected( true );
				showToolsMenuItem.addActionListener(this);
				menu.add(showToolsMenuItem);

				highlightMajorScaleMenuItem = new JCheckBoxMenuItem("Highlight Major C Scale");
				highlightMajorScaleMenuItem.setSelected( highlightMajorScale );
				highlightMajorScaleMenuItem.addActionListener(this);
				menu.add(highlightMajorScaleMenuItem);

				menu.addSeparator();

				frameAllMenuItem = new JMenuItem("Frame All");
				frameAllMenuItem.addActionListener(this);
				menu.add(frameAllMenuItem);

				autoFrameMenuItem = new JCheckBoxMenuItem("Auto Frame");
				autoFrameMenuItem.setSelected( isAutoFrameActive );
				autoFrameMenuItem.addActionListener(this);
				menu.add(autoFrameMenuItem);
			menuBar.add(menu);
			menu = new JMenu("Help");
				aboutMenuItem = new JMenuItem("About");
				aboutMenuItem.addActionListener(this);
				menu.add(aboutMenuItem);
			menuBar.add(menu);
		frame.setJMenuBar(menuBar);

		toolPanel = new JPanel();
		toolPanel.setLayout( new BoxLayout( toolPanel, BoxLayout.Y_AXIS ) );

		canvas = new MyCanvas(this);
                metronome = new Metronome((this));

		Container pane = frame.getContentPane();
		pane.setLayout( new BoxLayout( pane, BoxLayout.X_AXIS ) );
		pane.add( toolPanel );
		pane.add( canvas );

		playCheckBox = new JCheckBox("Play", isMusicPlaying );
		playCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		playCheckBox.addActionListener(this);
		toolPanel.add( playCheckBox );

		loopWhenPlayingCheckBox = new JCheckBox("Loop when playing", isMusicLoopedWhenPlayed );
		loopWhenPlayingCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		loopWhenPlayingCheckBox.addActionListener(this);
		toolPanel.add( loopWhenPlayingCheckBox );

                
                tempoSlider = new JSlider(JSlider.HORIZONTAL, 1, 1000, 150);
		tempoSlider.setAlignmentX( Component.LEFT_ALIGNMENT );
                tempoSlider.addChangeListener(this);
                tempoSlider.addMouseListener(this);
                toolPanel.add( tempoSlider );
                tempoLabel = new JLabel("Tempo: 150");
                toolPanel.add(tempoLabel);
		tempoLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
                
                beatsSlider = new JSlider(JSlider.HORIZONTAL, 4, canvas.score.MAX_BEATS, 64);
		beatsSlider.setAlignmentX( Component.LEFT_ALIGNMENT );
                beatsSlider.addChangeListener(this);
                beatsSlider.addMouseListener(this);
                toolPanel.add( beatsSlider );
                beatsLabel = new JLabel("Beats: 64");
                toolPanel.add(beatsLabel);
		beatsLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
                
		toolPanel.add( Box.createRigidArea(new Dimension(1,20)) );
		toolPanel.add( new JLabel("During dragging:") );

                
		ButtonGroup dragModeButtonGroup = new ButtonGroup();

			drawNotesRadioButton = new JRadioButton( "Draw Notes" );
			drawNotesRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
			drawNotesRadioButton.addActionListener(this);
			if ( dragMode == DM_DRAW_NOTES ) drawNotesRadioButton.setSelected(true);
			toolPanel.add( drawNotesRadioButton );
			dragModeButtonGroup.add( drawNotesRadioButton );

			eraseNotesRadioButton = new JRadioButton( "Erase Notes" );
			eraseNotesRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
			eraseNotesRadioButton.addActionListener(this);
			if ( dragMode == DM_ERASE_NOTES ) eraseNotesRadioButton.setSelected(true);
			toolPanel.add( eraseNotesRadioButton );
			dragModeButtonGroup.add( eraseNotesRadioButton );

		toolPanel.add( Box.createRigidArea(new Dimension(1,20)) );
		toolPanel.add( new JLabel("Upon cursor rollover:") );

		ButtonGroup rolloverModeButtonGroup = new ButtonGroup();

			doNothingUponRolloverRadioButton = new JRadioButton( "Do Nothing" );
			doNothingUponRolloverRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
			doNothingUponRolloverRadioButton.addActionListener(this);
			if ( rolloverMode == RM_DO_NOTHING_UPON_ROLLOVER ) doNothingUponRolloverRadioButton.setSelected(true);
			toolPanel.add( doNothingUponRolloverRadioButton );
			rolloverModeButtonGroup.add( doNothingUponRolloverRadioButton );

			playNoteUponRolloverRadioButton = new JRadioButton( "Play Pitch" );
			playNoteUponRolloverRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
			playNoteUponRolloverRadioButton.addActionListener(this);
			if ( rolloverMode == RM_PLAY_NOTE_UPON_ROLLOVER ) playNoteUponRolloverRadioButton.setSelected(true);
			toolPanel.add( playNoteUponRolloverRadioButton );
			rolloverModeButtonGroup.add( playNoteUponRolloverRadioButton );

			playNoteUponRolloverIfSpecialKeyHeldDownRadioButton = new JRadioButton( "Play Pitch if Ctrl down" );
			playNoteUponRolloverIfSpecialKeyHeldDownRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
			playNoteUponRolloverIfSpecialKeyHeldDownRadioButton.addActionListener(this);
			if ( rolloverMode == RM_PLAY_NOTE_UPON_ROLLOVER_IF_SPECIAL_KEY_HELD_DOWN )
				playNoteUponRolloverIfSpecialKeyHeldDownRadioButton.setSelected(true);
			toolPanel.add( playNoteUponRolloverIfSpecialKeyHeldDownRadioButton );
			rolloverModeButtonGroup.add( playNoteUponRolloverIfSpecialKeyHeldDownRadioButton );

		frame.pack();
		frame.setVisible( true );

		assert canvas.isFocusable();

	}

	public static void main( String[] args ) {
		// Schedule the creation of the UI for the event-dispatching thread.
		javax.swing.SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					SimplePianoRoll sp = new SimplePianoRoll();
					sp.createUI();
				}
			}
		);
	}

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == tempoSlider) {
            metronome.setTempo(tempoSlider.getValue());
            tempoLabel.setText("Tempo: " + tempoSlider.getValue());
        }
        
        if (e.getSource() == beatsSlider) {
            canvas.score.setBeatCount(beatsSlider.getValue());
            canvas.frameAll();
            beatsLabel.setText("Beats: " + beatsSlider.getValue());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getSource() == tempoSlider) {
            metronome.startBackgroundWork();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getSource() == tempoSlider) {
            metronome.stopBackgroundWork();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}

