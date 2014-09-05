package net.htjs.mobile.nyoa.vpn;

import xink.vpn.wrapper.PptpProfile;
import xink.vpn.wrapper.VpnProfile;
import android.content.Context;

/*import xink.vpn.wrapper.PptpProfile;
import xink.vpn.wrapper.VpnProfile;
import android.content.Context;*/

/**
 * @author Whyonly
 * 
 */
public class OWLPptpProfileEditor extends OWLVpnProfileEditor {
	
    public OWLPptpProfileEditor(final Context context) {
        super(context);
    }

    @Override
    protected VpnProfile createProfile() {
        return new PptpProfile(mContext);
    }


    @Override
    protected void doPopulateProfile() {
        PptpProfile profile = getProfile();
        profile.setName("OWLPPTP");
        profile.setServerName("0.0.0.0");
        profile.setDomainSuffices("8.8.8.8");
        profile.setUsername("whyonly");
        profile.setPassword(".....");
        profile.setEncryptionEnabled(true);
    }

}