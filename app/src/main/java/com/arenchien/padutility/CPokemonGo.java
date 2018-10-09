package com.arenchien.padutility;

import java.util.List;

/**
 * Created by ArenChien on 2018/10/5.
 */

public class CPokemonGo {
    String m_strRoot = "/sdcard/Android/data/com.nianticlabs.pokemongo";
    String m_strAccount = "/Account";

    String m_kCopyFolder[] = new String[ 5 ];

    CPokemonGo() {
        m_kCopyFolder[ 0 ] = "/appdata/shared_prefs";
        m_kCopyFolder[ 1 ] = "/files/il2cpp";
        m_kCopyFolder[ 2 ] = "/files/remote_config_cache";
        m_kCopyFolder[ 3 ] = "/files/DiskCache";
        m_kCopyFolder[ 4 ] = "/cache";
    }
}
