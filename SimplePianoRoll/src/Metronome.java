/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Xearus
 */
class Metronome implements Runnable {

	SimplePianoRoll simplePianoRoll;

	Thread thread = null;
	boolean threadSuspended;

	private int Tempo = 150;
        public int getTempo() { return Tempo; }
        public void setTempo(int tempo) 
        { 
            Tempo = tempo;
        }

	public Metronome( SimplePianoRoll sp ) {
		simplePianoRoll = sp;
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

	public void startBackgroundWork() {
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
			while (true) {

				// Here's where the thread does some work
				synchronized( this ) {
                                        simplePianoRoll.midiChannels[0].noteOn( 21, Constant.midiVolume );
                                        thread.sleep( 1 );
                                        simplePianoRoll.midiChannels[0].noteOff( 21 );
				}

				// Now the thread checks to see if it should suspend itself
				if ( threadSuspended ) {
					synchronized( this ) {
						while ( threadSuspended ) {
							wait();
						}
					}
				}
				thread.sleep( Tempo - 1 );  // interval given in milliseconds
			}
		}
		catch (InterruptedException e) { }
	}

}
