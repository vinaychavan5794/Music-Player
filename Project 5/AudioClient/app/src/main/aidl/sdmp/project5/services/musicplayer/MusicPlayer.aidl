
package sdmp.project5.services.musicplayer;

interface MusicPlayer {

       void play(int position);
       void pause(int position);
       void stop();
       void resume(int position);
       void release();
}
