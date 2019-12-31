// MusicPlayer.aidl
package sdmp.project5.services.musicplayer;

// Declare any non-default types here with import statements

interface MusicPlayer {

   void play(int position);
   void pause(int position);
   void stop();
   void resume(int position);
   void release();

}
