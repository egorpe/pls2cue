package re.system.pls2cue

import com.dd.plist.NSDictionary
import com.dd.plist.NSObject
import com.dd.plist.PropertyListParser
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.id3.AbstractID3v2Tag
import org.jaudiotagger.tag.id3.ID3v24Frames
import org.slf4j.LoggerFactory;
import org.slf4j.Logger
import java.nio.file.Paths
import java.text.SimpleDateFormat;

class Main {
    static Logger log = LoggerFactory.getLogger(Main.class)
    static String ITUNES_LIBRARY_PATH = "${System.properties['user.home']}/Music/iTunes/iTunes Library.xml"
    static NSDictionary library
    static String cueSheetText = ''
    static String savePath = "${System.properties['user.home']}/Desktop"

    static void main(String[] args) {
        System.setProperty('sun.java2d.cmm', 'sun.java2d.cmm.kcms.KcmsServiceProvider')

        log.info('iTunes Playlist To CD Insert, Version 1.0')
        log.info("Using iTunes library at: ${ITUNES_LIBRARY_PATH}")
        log.info("Processing playlist #${args[0]}")
        File file = new File(ITUNES_LIBRARY_PATH)
        library = PropertyListParser.parse(file);

        library.objectForKey('Playlists').array.each { playlist ->
            if (playlist.'Playlist ID'.intValue() == Integer.parseInt(args[0])) {
                processPlaylist(playlist)
            }
        }

        File cue = new File(savePath)
        cue << cueSheetText
    }

    static processPlaylist(playlist) {
        log.info("Found playlist \"${playlist.Name}\"")

        cueSheetText += "REM GENRE !!!CHANGEME!!!\n"
        def year = new SimpleDateFormat('yyyy').format(new Date())
        cueSheetText += "REM DATE ${year}\n"
        cueSheetText += "PERFORMER \"system:re\"\n"
        cueSheetText += "TITLE \"${playlist.Name}\"\n"
        cueSheetText += "FILE \"${playlist.Name}.mp3\" MP3\n"

        savePath += "/${playlist.Name.content.replace(':', '').replace('/', '-')}.cue"

        playlist.'Playlist Items'.array.eachWithIndex { item,i  ->
            processTrack(item.'Track ID', i)
        }
    }

    static processTrack(trackId, i) {
        NSDictionary tracks = library.objectForKey('Tracks')
        NSObject track = tracks.get((String)trackId)
        MP3File mp3File = AudioFileIO.read(Paths.get(new URL(track.Location.content).toURI()).toFile())
        AbstractID3v2Tag tag = mp3File.getID3v2Tag()

        cueSheetText += "  TRACK ${String.format('%02d', i + 1)} AUDIO\n"
        cueSheetText += "    TITLE \"${tag.getFirst(ID3v24Frames.FRAME_ID_TITLE)}\"\n"
        cueSheetText += "    PERFORMER \"${tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST)}\"\n"
        cueSheetText += "    INDEX 01 00:00:00\n"
    }

}

