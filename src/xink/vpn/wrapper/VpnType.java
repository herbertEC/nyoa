package xink.vpn.wrapper;

import net.htjs.mobile.nyoa.R;
import xink.vpn.editor.L2tpIpsecPskProfileEditor;
import xink.vpn.editor.L2tpProfileEditor;
import xink.vpn.editor.PptpProfileEditor;
import xink.vpn.editor.VpnProfileEditor;

public enum VpnType {
    PPTP("PPTP", R.string.vpn_pptp, R.string.vpn_pptp_info, PptpProfile.class, PptpProfileEditor.class),
    L2TP("L2TP", R.string.vpn_l2tp, R.string.vpn_l2tp_info, L2tpProfile.class, L2tpProfileEditor.class),
    L2TP_IPSEC_PSK("L2TP/IPSec PSK", R.string.vpn_l2tp_psk, R.string.vpn_l2tp_psk_info, L2tpIpsecPskProfile.class, L2tpIpsecPskProfileEditor.class),
    // L2TP_IPSEC("L2TP/IPSec CRT", null)
    ;

    private String name;
    private Class<? extends VpnProfile> clazz;
    private boolean active;
    private int descRid;
    private int nameRid;
    private Class<? extends VpnProfileEditor> editorClass;

    VpnType(final String name, final int nameRid, final int descRid, final Class<? extends VpnProfile> clazz,
            final Class<? extends VpnProfileEditor> editorClass) {
        this.name = name;
        this.nameRid = nameRid;
        this.descRid = descRid;
        this.clazz = clazz;
        this.editorClass = editorClass;
    }

    public String getName() {
        return name;
    }

    public Class<? extends VpnProfile> getProfileClass() {
        return clazz;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean a) {
        this.active = a;
    }

    public int getNameRid() {
        return nameRid;
    }

    public int getDescRid() {
        return descRid;
    }

    public Class<? extends VpnProfileEditor> getEditorClass() {
        return editorClass;
    }

}
