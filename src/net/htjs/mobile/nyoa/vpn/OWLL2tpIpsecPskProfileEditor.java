package net.htjs.mobile.nyoa.vpn;

import xink.vpn.wrapper.L2tpIpsecPskProfile;
import xink.vpn.wrapper.VpnProfile;
import android.content.Context;

/*import xink.vpn.wrapper.L2tpIpsecPskProfile;
 import xink.vpn.wrapper.VpnProfile;
 import android.content.Context;
 */
/**
 * @author Whyonly
 * 
 */
public class OWLL2tpIpsecPskProfileEditor extends OWLVpnProfileEditor {

	public OWLL2tpIpsecPskProfileEditor(final Context context) {
		super(context);
	}

	@Override
	protected VpnProfile createProfile() {
		return new L2tpIpsecPskProfile(mContext);
	}

	@Override
	protected void doPopulateProfile() {
		L2tpIpsecPskProfile p = getProfile();

		p.setName("nyoa");
		p.setServerName("120.194.102.194");
		p.setDomainSuffices("");
		p.setUsername("test1@vpn");
		p.setPassword("test");

		p.setPresharedKey("abcde");
		boolean secretEnabled = false;
		p.setSecretEnabled(secretEnabled);
		p.setSecretString(secretEnabled ? "" : "");
	}

}