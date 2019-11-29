package com.hon.fantasy.utils;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public interface Constants {
    String NAVIGATE_LIBRARY = "navigate_library";
    String NAVIGATE_PLAYLIST = "navigate_playlist";
    String NAVIGATE_QUEUE = "navigate_queue";
    String NAVIGATE_ALBUM = "navigate_album";
    String NAVIGATE_ARTIST = "navigate_artist";
    String NAVIGATE_NOWPLAYING = "navigate_nowplaying";
    String NAVIGATE_LYRICS = "navigate_lyrics";

    String NAVIGATE_PLAYLIST_RECENT = "navigate_playlist_recent";
    String NAVIGATE_PLAYLIST_LASTADDED = "navigate_playlist_lastadded";
    String NAVIGATE_PLAYLIST_TOPTRACKS = "navigate_playlist_toptracks";
    String NAVIGATE_PLAYLIST_USERCREATED = "navigate_playlist";
    String PLAYLIST_FOREGROUND_COLOR = "foreground_color";
    String PLAYLIST_NAME = "playlist_name";

    String ALBUM_ID = "album_id";
    String ARTIST_ID = "artist_id";
    String PLAYLIST_ID = "playlist_id";

    String FRAGMENT_ID = "fragment_id";
    String NOWPLAYING_FRAGMENT_ID = "nowplaying_fragment_id";

    String WITH_ANIMATIONS = "with_animations";

    String FANTASY1 = "fantasy1";
    String FANTASY2 = "fantasy2";
    String FANTASY3 = "fantasy3";
    String FANTASY4 = "fantasy4";
    String FANTASY5 = "fantasy5";
    String FANTASY6 = "fantasy6";

    String NAVIGATE_SETTINGS = "navigate_settings";
    String NAVIGATE_SEARCH = "navigate_search";

    String SETTINGS_STYLE_SELECTOR_NOWPLAYING = "style_selector_nowplaying";
    String SETTINGS_STYLE_SELECTOR_ARTIST = "style_selector_artist";
    String SETTINGS_STYLE_SELECTOR_ALBUM = "style_selector_album";
    String SETTINGS_STYLE_SELECTOR_WHAT = "style_selector_what";

    String SETTINGS_STYLE_SELECTOR = "settings_style_selector";

    int PLAYLIST_VIEW_DEFAULT = 0;
    int PLAYLIST_VIEW_LIST = 1;
    int PLAYLIST_VIEW_GRID = 2;

    int PLAYLIST_ALBUM_ART_TAG = 888;
    int ACTION_DELETE_PLAYLIST = 111;


    String ACTIVITY_TRANSITION = "activity_transition";

    int CAST_SERVER_PORT = 8080;

    // Music Service
    String PLAYSTATE_CHANGED = "com.frankhon.fantasy.playstatechanged";
    String POSITION_CHANGED = "com.frankhon.fantasy.positionchanged";
    String META_CHANGED = "com.frankhon.fantasy.metachanged";
    String QUEUE_CHANGED = "com.frankhon.fantasy.queuechanged";
    String PLAYLIST_CHANGED = "com.frankhon.fantasy.playlistchanged";
    String REPEATMODE_CHANGED = "com.frankhon.fantasy.repeatmodechanged";
    String SHUFFLEMODE_CHANGED = "com.frankhon.fantasy.shufflemodechanged";
    String TRACK_ERROR = "com.frankhon.fantasy.trackerror";
    String TIMBER_PACKAGE_NAME = "com.hon.fantasy";
    String MUSIC_PACKAGE_NAME = "com.android.music";
    String SERVICECMD = "com.frankhon.fantasy.musicservicecommand";
    String TOGGLE_PAUSE_ACTION = "com.frankhon.fantasy.togglepause";
    String PAUSE_ACTION = "com.frankhon.fantasy.pause";
    String STOP_ACTION = "com.frankhon.fantasy.stop";
    String PREVIOUS_ACTION = "com.frankhon.fantasy.previous";
    String PREVIOUS_FORCE_ACTION = "com.frankhon.fantasy.previous.force";
    String NEXT_ACTION = "fcom.frankhon.fantasy.next";
    String REPEAT_ACTION = "com.frankhon.fantasy.repeat";
    String SHUFFLE_ACTION = "com.frankhon.fantasy.shuffle";
    String FROM_MEDIA_BUTTON = "frommediabutton";
    String REFRESH = "com.frankhon.fantasy.refresh";
    String UPDATE_LOCKSCREEN = "com.frankhon.fantasy.updatelockscreen";
    String CMDNAME = "command";
    String CMDTOGGLEPAUSE = "togglepause";
    String CMDSTOP = "stop";
    String CMDPAUSE = "pause";
    String CMDPLAY = "play";
    String CMDPREVIOUS = "previous";
    String CMDNEXT = "next";
    String CMDNOTIF = "buttonId";
    String UPDATE_PREFERENCES = "updatepreferences";
    String CHANNEL_ID = "timber_channel_01";
    int NEXT = 2;
    int LAST = 3;
    int SHUFFLE_NONE = 0;
    int SHUFFLE_NORMAL = 1;
    int SHUFFLE_AUTO = 2;
    int REPEAT_NONE = 0;
    int REPEAT_CURRENT = 1;
    int REPEAT_ALL = 2;
    int MAX_HISTORY_SIZE = 1000;
}
