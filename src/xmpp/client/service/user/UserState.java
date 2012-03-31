package xmpp.client.service.user;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;

import xmpp.client.R;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;

public class UserState implements Parcelable {

	public static int modeToStatus(Presence.Mode mode) {
		switch (mode) {
		case away:
			return STATUS_IDLE;
		case dnd:
			return STATUS_DO_NOT_DISTURB;
		case xa:
			return STATUS_AWAY;
		case chat:
		case available:
		default:
			return STATUS_AVAILABLE;
		}
	}

	private final boolean mOnline;
	private String mStatusText;

	private int mStatus;
	public final static int STATUS_INVALID = Integer.MIN_VALUE;
	public final static int STATUS_INITIALIZING = -4;
	public final static int STATUS_CONNECTING = -3;
	public final static int STATUS_LOGGING_IN = -2;

	public final static int STATUS_LOADING_ROSTER = -1;
	public final static int STATUS_OFFLINE = ContactsContract.StatusUpdates.OFFLINE;
	public final static int STATUS_AVAILABLE = ContactsContract.StatusUpdates.AVAILABLE;
	public final static int STATUS_IDLE = ContactsContract.StatusUpdates.IDLE;
	public final static int STATUS_DO_NOT_DISTURB = ContactsContract.StatusUpdates.DO_NOT_DISTURB;

	public final static int STATUS_AWAY = ContactsContract.StatusUpdates.AWAY;

	public static UserState Invalid = new UserState(STATUS_INVALID, null);

	public static final Parcelable.Creator<UserState> CREATOR = new Parcelable.Creator<UserState>() {
		@Override
		public UserState createFromParcel(Parcel in) {
			return new UserState(in);
		}

		@Override
		public UserState[] newArray(int size) {
			return new UserState[size];
		}
	};

	public static int getStatusIconResourceID(int status) {
		return ContactsContract.StatusUpdates.getPresenceIconResourceId(status);
	}

	public static int getStatusTextResourceID(int status) {
		switch (status) {
		case STATUS_INITIALIZING:
			return R.string.process_initializing;
		case STATUS_LOGGING_IN:
			return R.string.process_logging_in;
		case STATUS_LOADING_ROSTER:
			return R.string.process_loading_roster;
		case STATUS_CONNECTING:
			return R.string.process_connecting;
		case STATUS_AVAILABLE:
			return R.string.status_available;
		case STATUS_AWAY:
			return R.string.status_away;
		case STATUS_IDLE:
			return R.string.status_idle;
		case STATUS_DO_NOT_DISTURB:
			return R.string.status_busy;
		case STATUS_OFFLINE:
		default:
			return R.string.status_offline;
		}
	}

	public static Presence.Mode statusToMode(int status) {
		switch (status) {
		case STATUS_AVAILABLE:
			return Mode.available;
		case STATUS_AWAY:
			return Mode.xa;
		case STATUS_IDLE:
			return Mode.away;
		case STATUS_DO_NOT_DISTURB:
			return Mode.dnd;
		}
		return null;
	}

	public UserState(int status, String statusText) {
		mOnline = (status != STATUS_OFFLINE);
		if (mOnline) {
			mStatusText = statusText;
			mStatus = status;
		}
	}

	private UserState(Parcel in) {
		mOnline = (Boolean) in.readValue(null);
		mStatusText = in.readString();
		mStatus = in.readInt();
	}

	public UserState(Presence p) {
		mOnline = (p.getType() == Type.available);
		if (mOnline) {
			mStatusText = p.getStatus();
			try {
				mStatus = modeToStatus(p.getMode());
			} catch (final NullPointerException ex) {
				mStatus = STATUS_AVAILABLE;
			}
		} else {
			mStatus = STATUS_OFFLINE;
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getCustomStatusText() {
		return mStatusText;
	}

	public int getRealStatus() {
		return mStatus;
	}

	public int getStatus() {
		if (mStatus == STATUS_LOADING_ROSTER) {
			return STATUS_AVAILABLE;
		}
		if (isTemporaryStatus()) {
			return STATUS_OFFLINE;
		}
		return getRealStatus();
	}

	public int getStatusIconResourceID() {
		return getStatusIconResourceID(getStatus());
	}

	public int getStatusPrecedence() {
		return ContactsContract.StatusUpdates
				.getPresencePrecedence(getStatus());
	}

	public CharSequence getStatusText(Context context) {
		if (mStatusText == null || mStatusText.isEmpty() || isTemporaryStatus()) {
			return context.getText(getStatusTextResourceID());
		}
		return mStatusText;
	}

	public int getStatusTextResourceID() {
		return getStatusTextResourceID(mStatus);
	}

	public boolean isOnline() {
		return mOnline;
	}

	public boolean isTemporaryStatus() {
		if (mStatus < 0) {
			return true;
		} else {
			return false;
		}
	}

	public Presence toPresence() {
		final Presence presence = new Presence(Type.available);
		if (isOnline() && !isTemporaryStatus()) {
			presence.setMode(statusToMode(mStatus));
		}
		if (mStatusText != null) {
			presence.setStatus(mStatusText);
		}
		presence.setPriority(1);
		return presence;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(mOnline);
		dest.writeString(mStatusText);
		dest.writeInt(mStatus);
	}

}