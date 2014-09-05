package net.htjs.mobile.nyoa.vpn;

import xink.vpn.VpnProfileRepository;
import xink.vpn.wrapper.InvalidProfileException;
import xink.vpn.wrapper.KeyStore;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnState;
import android.content.Context;

/*import xink.vpn.VpnProfileRepository;
 import xink.vpn.wrapper.InvalidProfileException;
 import xink.vpn.wrapper.KeyStore;
 import xink.vpn.wrapper.VpnProfile;
 import xink.vpn.wrapper.VpnState;
 import android.content.Context;*/

/**
 * @author Whyonly
 * 
 */
public abstract class OWLVpnProfileEditor {

	private VpnProfile profile;
	private VpnProfileRepository repository;
	private KeyStore keyStore;
	private Runnable resumeAction;

	protected Context mContext;

	public OWLVpnProfileEditor(final Context context) {
		mContext = context;
		repository = VpnProfileRepository.getInstance(context);
		keyStore = new KeyStore(context);
	}

	public VpnProfile getVpnProfile() {
		return this.profile;
	}

	public void onSave() {
		try {
			profile = createProfile();
			populateProfile();
			saveProfile();
		} catch (InvalidProfileException e) {
			throw e;
		}
	}

	private void populateProfile() {
		profile.setState(VpnState.IDLE);
		doPopulateProfile();
		repository.checkProfile(profile);
	}

	private void saveProfile() {
		repository.addVpnProfile(profile);
	}

	@SuppressWarnings("unchecked")
	protected <T extends VpnProfile> T getProfile() {
		return (T) profile;
	}

	protected abstract VpnProfile createProfile();

	protected abstract void doPopulateProfile();
}